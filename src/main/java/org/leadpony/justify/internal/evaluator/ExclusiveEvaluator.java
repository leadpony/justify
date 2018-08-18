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
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Evaluator for "oneOf" boolean logic.
 * 
 * @author leadpony
 */
public class ExclusiveEvaluator extends AbstractLogicalEvaluator {

    private final List<RetainingEvaluator> children;
    private final List<RetainingEvaluator> negated;
    private List<RetainingEvaluator> good;
    private List<RetainingEvaluator> bad;
    private long evaluationsAsTrue;
    private final Event stopEvent;
    
    ExclusiveEvaluator(Stream<JsonSchema> children, InstanceType type) {
        this.children = new ArrayList<>();
        this.negated = new ArrayList<>();
        children.forEach(child->{
            this.children.add(new RetainingEvaluator(
                    child.evaluator(type, Evaluators.asFactory(), true)));
            this.negated.add(new RetainingEvaluator(
                    child.evaluator(type, Evaluators.asFactory(), false)));
        });
        this.stopEvent = ParserEvents.lastEventOf(type);
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        evaluateAll(event, parser, depth, reporter);
        evaluateAllNegated(event, parser, depth, reporter);
        if (this.stopEvent == null || (depth == 0 && event == this.stopEvent)) {
            if (evaluationsAsTrue == 1) {
                return Result.TRUE;
            } else if (evaluationsAsTrue < 1) {
                return reportTooFewValid(parser, reporter);
            } else {
                return reportTooManyValid(parser, reporter);
            }
        }
        return Result.PENDING;
    }

    private void evaluateAll(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        Iterator<RetainingEvaluator> it = children.iterator();
        while (it.hasNext()) {
            RetainingEvaluator current = it.next();
            Result result = current.evaluate(event, parser, depth, reporter);
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

    private void evaluateAllNegated(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        Iterator<RetainingEvaluator> it = negated.iterator();
        while (it.hasNext()) {
            RetainingEvaluator current = it.next();
            Result result = current.evaluate(event, parser, depth, reporter);
            if (result != Result.PENDING) {
                if (result == Result.FALSE) {
                    addGood(current);
                }
                it.remove();
            }
        }
    }
    
    private void addGood(RetainingEvaluator evaluator) {
        if (this.good == null) {
            this.good = new ArrayList<>();
        }
        this.good.add(evaluator);
    }

    private void addBad(RetainingEvaluator evaluator) {
        if (this.bad == null) {
            this.bad = new ArrayList<>();
        }
        this.bad.add(evaluator);
    }
    
    private Result reportTooFewValid(JsonParser parser, Consumer<Problem> reporter) {
        if (bad.size() == 1) {
            bad.get(0).problems().forEach(reporter::accept);
        } else {
            ProblemBuilder builder = createProblemBuilder(parser)
                    .withMessage("instance.problem.oneOf");
            bad.stream()
                .map(RetainingEvaluator::problems)
                .filter(Objects::nonNull)
                .forEach(builder::withSubproblems);
            reporter.accept(builder.build());
        }
        return Result.FALSE;
    }

    protected Result reportTooManyValid(JsonParser parser, Consumer<Problem> reporter) {
        assert good.size() > 1;
        for (int i = 1; i < good.size(); i++) {
            good.get(i).problems().forEach(reporter::accept);
        }
        return Result.FALSE;
    }
}
