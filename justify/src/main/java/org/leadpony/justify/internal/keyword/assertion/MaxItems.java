/*
 * Copyright 2018-2019 the Justify authors.
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

package org.leadpony.justify.internal.keyword.assertion;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;
import org.leadpony.justify.internal.keyword.ArrayKeyword;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * Assertion specified with "maxItems" validation keyword.
 * 
 * @author leadpony
 */
class MaxItems extends AbstractAssertion implements ArrayKeyword {

    private final int limit;
    
    MaxItems(int limit) {
        this.limit = limit;
    }

    @Override
    public String name() {
        return "maxItems";
    }
  
    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return new AssertionEvaluator(limit, this);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return new MinItems.AssertionEvaluator(limit + 1, this);   
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), limit);
    }
    
    static class AssertionEvaluator implements ShallowEvaluator { 

        private final int maxItems;
        private final ProblemBuilderFactory factory;
        private int currentCount;
        
        AssertionEvaluator(int maxItems, ProblemBuilderFactory factory) {
            this.maxItems = maxItems;
            this.factory = factory;
        }

        @Override
        public Result evaluateShallow(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1) {
                if (ParserEvents.isValue(event)) {
                    ++currentCount;
                }
            } else if (depth == 0 && event == Event.END_ARRAY) {
                if (currentCount <= maxItems) {
                    return Result.TRUE;
                } else {
                    Problem p = factory.createProblemBuilder(parser)
                            .withMessage("instance.problem.maxItems")
                            .withParameter("actual", currentCount)
                            .withParameter("limit", maxItems)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            }
            return Result.PENDING;
        }
    }
}
