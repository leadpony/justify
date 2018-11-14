/*
 * Copyright 2018 the Justify authors.
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

package org.leadpony.justify.internal.evaluator;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * @author leadpony
 */
public abstract class AbstractChildrenEvaluator implements Evaluator {
    
    private final boolean affirmative;
    private final AppendableLogicalEvaluator childrenEvaluator;
    
    protected AbstractChildrenEvaluator(boolean affirmative, InstanceType type, ProblemBuilderFactory problemBuilderFactory) {
        this.affirmative = affirmative;
        childrenEvaluator = affirmative ? 
                new ConjunctiveChildrenEvaluator(type) : 
                new DisjunctiveChildrenEvaluator(type);
        childrenEvaluator
            .withProblemBuilderFactory(problemBuilderFactory);
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        if (depth == 1) {
            update(event, parser, dispatcher);
        }
        return childrenEvaluator.evaluate(event, parser, depth, dispatcher);
    }
    
    public boolean isAffirmative() {
        return affirmative;
    }
    
    public JsonSchema getSchemaToFail() {
        return JsonSchema.valueOf(!isAffirmative());
    }
    
    protected void append(JsonSchema schema, InstanceType type) {
        assert schema != null;
        Evaluator evaluator = schema.evaluator(type, isAffirmative());
        append(evaluator);
    }

    protected void append(Evaluator evaluator) {
        assert evaluator != null;
        childrenEvaluator.append(evaluator);
    }
    
    protected abstract void update(Event event, JsonParser parser, ProblemDispatcher dispatcher);

    /**
     * @author leadpony
     */
    private static class ConjunctiveChildrenEvaluator extends LongConjunctiveEvaluator {
        
        ConjunctiveChildrenEvaluator(InstanceType type) {
            super(type);
        }

        @Override
        protected Result invokeChildEvaluator(Evaluator evaluator, Event event, JsonParser parser, int depth,
                ProblemDispatcher dispatcher) {
            assert depth > 0;
            return super.invokeChildEvaluator(evaluator, event, parser, depth - 1, dispatcher);
        }
    }

    /**
     * @author leadpony
     */
    private static class DisjunctiveChildrenEvaluator extends LongDisjunctiveEvaluator {

        DisjunctiveChildrenEvaluator(InstanceType type) {
            super(type);
        }

        @Override
        protected Result invokeChildEvaluator(Evaluator evaluator, Event event, JsonParser parser, int depth,
                ProblemDispatcher dispatcher) {
            assert depth > 0;
            return super.invokeChildEvaluator(evaluator, event, parser, depth - 1, dispatcher);
        }
    }
}
