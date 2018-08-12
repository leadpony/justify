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

package org.leadpony.justify.internal.keyword.combiner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.base.ProblemReporter;
import org.leadpony.justify.internal.evaluator.DynamicChildrenEvaluator;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * @author leadpony
 */
class Contains extends UnaryCombiner {
    
    private int min;
    
    Contains(JsonSchema subschema) {
        super(subschema);
        this.min = 1;
    }

    @Override
    public String name() {
        return "contains";
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory, boolean affrimative) {
        if (type == InstanceType.ARRAY) {
            Evaluator evaluator = affrimative ? 
                    new ItemsSchemaEvaluator() : 
                    new NegatedItemSchemaEvaluator(this, getSubschema());
            appender.append(evaluator);
        }
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
    }
    
    private class ItemsSchemaEvaluator implements Evaluator, ProblemReporter {
        
        private final List<Problem> problems = new ArrayList<>();
        private final List<List<Problem>> accumulatedProblems = new ArrayList<>();
        private int numberOfTruths;
        private Evaluator itemEvaluator;
        
        ItemsSchemaEvaluator() {
        }
        
        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
            if (itemEvaluator == null) {
                itemEvaluator = createItemSchemaEvaluator(event, parser, depth);
            }
            if (itemEvaluator != null) {
                evaluateItemschema(event, parser, depth);
            }
            if (depth == 0 && event == Event.END_ARRAY) {
                return assertCountInRange(numberOfTruths, parser, reporter);
            }
            return Result.PENDING;
        }
        
        @Override
        public void accept(Problem problem) {
            problems.add(problem);
        }

        private Evaluator createItemSchemaEvaluator(Event event, JsonParser parser, int depth) {
            if (depth == 1 && ParserEvents.isValue(event)) {
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                return getSubschema().createEvaluator(type, getEvaluatorFactory(), true);
            } else {
                return null;
            }
        }
        
        private void evaluateItemschema(Event event, JsonParser parser, int depth) {
            Result result = itemEvaluator.evaluate(event, parser, depth - 1, this);
            if (result != Result.PENDING) {
                itemEvaluator = null;
                if (result == Result.TRUE) {
                    ++numberOfTruths;
                } else if (result == Result.FALSE) {
                    accumulatedProblems.add(new ArrayList<Problem>(problems));
                    problems.clear();
                }
            }
        }
        
        protected Result assertCountInRange(int numberOfTruths, JsonParser parser, Consumer<Problem> reporter) {
            if (numberOfTruths < min) {
                reportTooFewTruths(numberOfTruths, parser, reporter);
                return Result.FALSE;
            }
            return Result.TRUE;
        }

        private void reportTooFewTruths(int numberOfTruths, JsonParser parser, Consumer<Problem> reporter) {
            ProblemBuilder builder = createProblemBuilder(parser);
            builder.withMessage("instance.problem.cotains")
                   .withParameter("expected", min)
                   .withParameter("actual", numberOfTruths);
            accumulatedProblems.forEach(builder::withSubproblems);
            reporter.accept(builder.build());
        }
    }
    
    private static class NegatedItemSchemaEvaluator extends DynamicChildrenEvaluator {

        private final JsonSchema subschema;
        
        NegatedItemSchemaEvaluator(ProblemBuilderFactory problemFactory, JsonSchema subschema) {
            super(true,InstanceType.ARRAY, problemFactory);
            this.subschema = subschema;
        }

        @Override
        protected void update(Event event, JsonParser parser, Consumer<Problem> reporter) {
            if (ParserEvents.isValue(event)) {
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                append(subschema.createEvaluator(type, Evaluators.asFactory(), false));
            }
        }
    }
}
