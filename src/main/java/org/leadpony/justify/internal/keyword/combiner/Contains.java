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

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.internal.base.DefaultProblemDispatcher;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;
import org.leadpony.justify.internal.evaluator.AbstractChildrenEvaluator;
import org.leadpony.justify.internal.keyword.ArrayKeyword;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * @author leadpony
 */
class Contains extends UnaryCombiner implements ArrayKeyword {
    
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
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return new ItemsSchemaEvaluator();
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return new NegatedItemSchemaEvaluator(this, getSubschema());
    }

    @Override
    public void link(Map<String, Keyword> siblings) {
    }
    
    private class ItemsSchemaEvaluator implements Evaluator, DefaultProblemDispatcher {
        
        private final List<Problem> problems = new ArrayList<>();
        private final List<List<Problem>> accumulatedProblems = new ArrayList<>();
        private int numberOfTruths;
        private Evaluator itemEvaluator;
        
        ItemsSchemaEvaluator() {
        }
        
        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            if (itemEvaluator == null) {
                itemEvaluator = createItemSchemaEvaluator(event, parser, depth);
            }
            if (itemEvaluator != null) {
                evaluateItemschema(event, parser, depth);
            }
            if (depth == 0 && event == Event.END_ARRAY) {
                return assertCountInRange(numberOfTruths, parser, dispatcher);
            }
            return Result.PENDING;
        }
        
        @Override
        public void dispatchProblem(Problem problem) {
            problems.add(problem);
        }

        private Evaluator createItemSchemaEvaluator(Event event, JsonParser parser, int depth) {
            if (depth == 1 && ParserEvents.isValue(event)) {
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                return getSubschema().evaluator(type, true);
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
        
        protected Result assertCountInRange(int numberOfTruths, JsonParser parser, ProblemDispatcher dispatcher) {
            if (numberOfTruths < min) {
                reportTooFewTruths(numberOfTruths, parser, dispatcher);
                return Result.FALSE;
            }
            return Result.TRUE;
        }

        private void reportTooFewTruths(int numberOfTruths, JsonParser parser, ProblemDispatcher dispatcher) {
            ProblemBuilder builder = createProblemBuilder(parser);
            builder.withMessage("instance.problem.cotains")
                   .withParameter("limit", min)
                   .withParameter("actual", numberOfTruths);
            accumulatedProblems.forEach(builder::withBranch);
            dispatcher.dispatchProblem(builder.build());
        }
    }
    
    private static class NegatedItemSchemaEvaluator extends AbstractChildrenEvaluator {

        private final JsonSchema subschema;
        
        NegatedItemSchemaEvaluator(ProblemBuilderFactory problemFactory, JsonSchema subschema) {
            super(true,InstanceType.ARRAY, problemFactory);
            this.subschema = subschema;
        }

        @Override
        protected void update(Event event, JsonParser parser, ProblemDispatcher dispatcher) {
            if (ParserEvents.isValue(event)) {
                InstanceType type = ParserEvents.toInstanceType(event, parser);
                append(subschema.evaluator(type, false));
            }
        }
    }
}
