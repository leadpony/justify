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
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Evaluator for "anyOf" boolean logic.
 * 
 * @author leadpony
 */
class DisjunctiveEvaluator extends AbstractLogicalEvaluator implements AppendableLogicalEvaluator {
    
    protected final List<RetainingEvaluator> children = new ArrayList<>();
    private List<RetainingEvaluator> badEvaluators;
    
    DisjunctiveEvaluator() {
    }
    
    DisjunctiveEvaluator(Stream<JsonSchema> children, InstanceType type, boolean affirmative) {
        children.map(s->s.evaluator(type, Evaluators.asFactory(), affirmative))
            .forEach(this::append);
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        for (RetainingEvaluator child : children) {
            Result result = child.evaluate(event, parser, depth, reporter);
            if (result == Result.TRUE) {
                return Result.TRUE;
            } else {
                addBadEvaluator(child);
            }
        }
        return reportProblems(parser, reporter);
    }

    @Override
    public void append(Evaluator evaluator) {
        this.children.add(new RetainingEvaluator(evaluator));
    }
    
    protected void addBadEvaluator(RetainingEvaluator evaluator) {
        if (this.badEvaluators == null) {
            this.badEvaluators = new ArrayList<>();
        }
        badEvaluators.add(evaluator);
    }
    
    protected Result reportProblems(JsonParser parser, Consumer<Problem> reporter) {
        int count = (badEvaluators != null) ? 
                badEvaluators.size() : 0;
        if (count == 0) {
            ProblemBuilder builder = createProblemBuilder(parser)
                    .withMessage("instance.problem.anyOf.none");
            reporter.accept(builder.build());
        } else if (count == 1) {
            badEvaluators.get(0).problems().forEach(reporter::accept);
        } else {
            ProblemBuilder builder = createProblemBuilder(parser)
                    .withMessage("instance.problem.anyOf");
            badEvaluators.stream()
                .map(RetainingEvaluator::problems)
                .filter(Objects::nonNull)
                .forEach(builder::withSubproblems);
            reporter.accept(builder.build());
        }
        return Result.FALSE;
    }
}
