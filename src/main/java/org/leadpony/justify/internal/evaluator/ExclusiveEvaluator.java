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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Evaluator for "oneOf" boolean logic.
 * 
 * @author leadpony
 */
public class ExclusiveEvaluator extends AbstractLogicalEvaluator {

    private final List<DeferredEvaluator> children;
    private final List<DeferredEvaluator> negated;
    private List<DeferredEvaluator> good;
    private List<DeferredEvaluator> bad;
    private long evaluationsAsTrue;
    private final InstanceMonitor monitor;
    
    ExclusiveEvaluator(Stream<JsonSchema> children, InstanceType type) {
        this.children = new ArrayList<>();
        this.negated = new ArrayList<>();
        children.forEach(child->{
            this.children.add(new DeferredEvaluator(
                    child.createEvaluator(type)));
            this.negated.add(new DeferredEvaluator(
                    child.createNegatedEvaluator(type)));
        });
        this.monitor = InstanceMonitor.of(type);
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        evaluateAll(event, parser, depth, dispatcher);
        evaluateAllNegated(event, parser, depth, dispatcher);
        if (monitor.isCompleted(event, depth)) {
            if (evaluationsAsTrue == 1) {
                return Result.TRUE;
            } else if (evaluationsAsTrue < 1) {
                return reportTooFewValid(parser, dispatcher);
            } else {
                return reportTooManyValid(parser, dispatcher);
            }
        }
        return Result.PENDING;
    }

    private void evaluateAll(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        Iterator<DeferredEvaluator> it = children.iterator();
        while (it.hasNext()) {
            DeferredEvaluator current = it.next();
            Result result = current.evaluate(event, parser, depth, dispatcher);
            if (result != Result.PENDING) {
                if (result == Result.TRUE) {
                    evaluationsAsTrue++;
                } else if (result == Result.FALSE) {
                    addBad(current);
                }
                it.remove();
            }
        }
    }

    private void evaluateAllNegated(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        Iterator<DeferredEvaluator> it = negated.iterator();
        while (it.hasNext()) {
            DeferredEvaluator current = it.next();
            Result result = current.evaluate(event, parser, depth, dispatcher);
            if (result != Result.PENDING) {
                if (result == Result.FALSE) {
                    addGood(current);
                }
                it.remove();
            }
        }
    }
    
    private void addGood(DeferredEvaluator evaluator) {
        if (this.good == null) {
            this.good = new ArrayList<>();
        }
        this.good.add(evaluator);
    }

    private void addBad(DeferredEvaluator evaluator) {
        if (this.bad == null) {
            this.bad = new ArrayList<>();
        }
        this.bad.add(evaluator);
    }
    
    private Result reportTooFewValid(JsonParser parser, ProblemDispatcher dispatcher) {
        if (bad.size() == 1) {
            bad.get(0).problems().forEach(dispatcher::dispatchProblem);
        } else {
            ProblemBuilder builder = createProblemBuilder(parser)
                    .withMessage("instance.problem.oneOf.few");
            bad.stream()
                .map(DeferredEvaluator::problems)
                .filter(Objects::nonNull)
                .forEach(builder::withBranch);
            dispatcher.dispatchProblem(builder.build());
        }
        return Result.FALSE;
    }

    protected Result reportTooManyValid(JsonParser parser, ProblemDispatcher dispatcher) {
        assert good.size() > 1;
        ProblemBuilder builder = createProblemBuilder(parser)
                .withMessage("instance.problem.oneOf.many");
        good.stream()
            .map(DeferredEvaluator::problems)
            .filter(Objects::nonNull)
            .forEach(builder::withBranch);
        dispatcher.dispatchProblem(builder.build());
        return Result.FALSE;
    }
}
