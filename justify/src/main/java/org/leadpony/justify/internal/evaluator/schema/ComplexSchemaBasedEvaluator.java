/*
 * Copyright 2020 the Justify authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.leadpony.justify.internal.evaluator.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.EvaluatorSource;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.evaluator.DeferredEvaluator;
import org.leadpony.justify.internal.evaluator.UnsupportedTypeEvaluator;
import org.leadpony.justify.internal.problem.DefaultProblemDispatcher;
import org.leadpony.justify.internal.problem.ProblemBranch;
import org.leadpony.justify.internal.problem.ProblemBuilder;

import jakarta.json.stream.JsonParser.Event;

/**
 * @author leadpony
 */
public abstract class ComplexSchemaBasedEvaluator extends AbstractSchemaBasedEvaluator {

    public static Evaluator of(Collection<EvaluatorSource> sources,
            Evaluator parent,
            JsonSchema schema,
            InstanceType type) {

        ComplexSchemaBasedEvaluator self = createEvaluator(parent, schema, type);
        self.addChildren(sources, type);
        if (self.isEmpty()) {
            return Evaluator.ALWAYS_TRUE;
        }
        return self;
    }

    public static Evaluator ofNegated(Collection<EvaluatorSource> sources,
            Evaluator parent,
            JsonSchema schema,
            InstanceType type) {

        ComplexSchemaBasedEvaluator self = createNegatedEvaluator(parent, schema, type);
        self.addChildren(sources, type);
        return self;
    }

    private static ComplexSchemaBasedEvaluator createEvaluator(Evaluator parent, JsonSchema schema,
            InstanceType type) {
        switch (type) {
        case ARRAY:
            return new CollectionTypeEvaluator(parent, schema, Event.END_ARRAY);
        case OBJECT:
            return new CollectionTypeEvaluator(parent, schema, Event.END_OBJECT);
        default:
            return new SimpleTypeEvaluator(parent, schema);
        }
    }

    private static ComplexSchemaBasedEvaluator createNegatedEvaluator(Evaluator parent, JsonSchema schema,
            InstanceType type) {
        switch (type) {
        case ARRAY:
            return new NegatedCollectionTypeEvaluator(parent, schema, Event.END_ARRAY);
        case OBJECT:
            return new NegatedCollectionTypeEvaluator(parent, schema, Event.END_OBJECT);
        default:
            return new NegatedSimpleTypeEvaluator(parent, schema);
        }
    }

    protected final Collection<Evaluator> children = new ArrayList<>();

    protected ComplexSchemaBasedEvaluator(Evaluator parent, JsonSchema schema) {
        super(parent, schema);
    }

    private boolean isEmpty() {
        return children.isEmpty();
    }

    private void addChildren(Collection<EvaluatorSource> sources, InstanceType type) {
        for (EvaluatorSource source : sources) {
            addChild(source, type);
        }
    }

    protected void addChild(EvaluatorSource source, InstanceType type) {
        if (source.supportsType(type)) {
            Evaluator child = source.createEvaluator(this, type);
            if (child != Evaluator.ALWAYS_TRUE) {
                this.children.add(child);
            }
        }
    }

    private static final class SimpleTypeEvaluator extends ComplexSchemaBasedEvaluator {

        private SimpleTypeEvaluator(Evaluator parent, JsonSchema schema) {
            super(parent, schema);
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            Result result = Result.TRUE;
            for (Evaluator child : this.children) {
                if (child.evaluate(event, depth, dispatcher) == Result.FALSE) {
                    result = Result.FALSE;
                }
            }
            return result;
        }
    }

    private static final class CollectionTypeEvaluator extends ComplexSchemaBasedEvaluator {

        private final Event closingEvent;
        private Result result = Result.TRUE;

        private CollectionTypeEvaluator(Evaluator parent, JsonSchema schema, Event closingEvent) {
            super(parent, schema);
            this.closingEvent = closingEvent;
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            if (this.children.isEmpty()) {
                return result;
            }

            invokeChildren(event, depth, dispatcher);

            if (depth == 0 && event == this.closingEvent) {
                return result;
            }

            return Result.PENDING;
        }

        private void invokeChildren(Event event, int depth, ProblemDispatcher dispatcher) {
            Iterator<Evaluator> it = this.children.iterator();
            while (it.hasNext()) {
                Evaluator child = it.next();
                Result result = child.evaluate(event, depth, dispatcher);
                if (result != Result.PENDING) {
                    it.remove();
                    if (result == Result.FALSE) {
                        this.result = Result.FALSE;
                    }
                }
            }
        }
    }

    private abstract static class NegatedComplexSchemaBasedEvaluator extends ComplexSchemaBasedEvaluator {

        private final List<ProblemBranch> branches = new ArrayList<>();

        protected NegatedComplexSchemaBasedEvaluator(Evaluator parent, JsonSchema schema) {
            super(parent, schema);
        }

        protected void addProblemBranch(ProblemBranch branch) {
            assert !branch.isEmpty();
            this.branches.add(branch);
        }

        protected void dispatchAllProblems(ProblemDispatcher dispatcher) {
            List<ProblemBranch> branches = this.branches.stream()
                    .filter(ProblemBranch::isResolvable)
                    .collect(Collectors.toList());

            if (branches.isEmpty()) {
                branches = this.branches;
            }

            assert !branches.isEmpty();

            ProblemBuilder builder = createProblemBuilder()
                    .withMessage(Message.INSTANCE_PROBLEM_ANYOF)
                    .withBranches(branches);

            dispatcher.dispatchProblem(builder.build());
        }
    }

    private static final class NegatedSimpleTypeEvaluator extends NegatedComplexSchemaBasedEvaluator
            implements DefaultProblemDispatcher {

        private ProblemBranch branch;

        private NegatedSimpleTypeEvaluator(Evaluator parent, JsonSchema schema) {
            super(parent, schema);
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            for (Evaluator child : this.children) {
                Result result = child.evaluate(event, depth, this);
                if (result == Result.TRUE) {
                    return Result.TRUE;
                } else if (result == Result.FALSE) {
                    addProblemBranch(this.branch);
                    this.branch = null;
                }
            }
            dispatchAllProblems(dispatcher);
            return Result.FALSE;
        }

        @Override
        public void dispatchProblem(Problem problem) {
            if (branch == null) {
                branch = new ProblemBranch();
            }
            branch.add(problem);
        }

        @Override
        protected void addChild(EvaluatorSource source, InstanceType type) {
            Evaluator child;
            if (source.supportsType(type)) {
                child = source.createNegatedEvaluator(this, type);
            } else {
                child = new UnsupportedTypeEvaluator(this, source, type);
            }
            this.children.add(child);
        }
    }

    private static final class NegatedCollectionTypeEvaluator extends NegatedComplexSchemaBasedEvaluator {

        private final Event closingEvent;

        NegatedCollectionTypeEvaluator(Evaluator parent, JsonSchema schema, Event closingEvent) {
            super(parent, schema);
            this.closingEvent = closingEvent;
        }

        @Override
        public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
            Iterator<Evaluator> it = this.children.iterator();
            while (it.hasNext()) {
                Evaluator child = it.next();
                Result result = child.evaluate(event, depth, dispatcher);
                if (result == Result.TRUE) {
                    return Result.TRUE;
                } else if (result == Result.FALSE) {
                    addProblemBranch(((DeferredEvaluator) child).problems());
                    it.remove();
                }
            }

            if (depth == 0 && event == this.closingEvent) {
                dispatchAllProblems(dispatcher);
                return Result.FALSE;
            }

            return Result.PENDING;
        }

        @Override
        protected void addChild(EvaluatorSource source, InstanceType type) {
            DeferredEvaluator deferred = new DeferredEvaluator(this);
            Evaluator evaluator;
            if (source.supportsType(type)) {
                evaluator = source.createNegatedEvaluator(deferred, type);
            } else {
                evaluator = new UnsupportedTypeEvaluator(deferred, source, type);
            }
            deferred.setEvaluator(evaluator);
            this.children.add(deferred);
        }
    }
}
