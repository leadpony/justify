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

package org.leadpony.justify.internal.keyword.assertion;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;

/**
 * Assertion specified with "pattern" validation keyword.
 * 
 * @author leadpony
 */
class Pattern extends AbstractStringAssertion {
    
    private final java.util.regex.Pattern pattern; 
    
    Pattern(java.util.regex.Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public String name() {
        return "pattern";
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), pattern.toString());
    }
    
    @Override
    protected Result evaluateAgainst(String value, Event event, JsonParser parser, ProblemDispatcher dispatcher) {
        if (testValue(value)) {
            return Result.TRUE;
        } else {
            Problem p = createProblemBuilder(parser, event)
                    .withMessage("instance.problem.pattern")
                    .withParameter("pattern", pattern.toString())
                    .build();
            dispatcher.dispatchProblem(p);
            return Result.FALSE;
        }
    }

    @Override
    protected Result evaluateNegatedAgainst(String value, Event event, JsonParser parser, ProblemDispatcher dispatcher) {
        if (testValue(value)) {
            Problem p = createProblemBuilder(parser, event)
                    .withMessage("instance.problem.not.pattern")
                    .withParameter("pattern", pattern.toString())
                    .build();
            dispatcher.dispatchProblem(p);
            return Result.FALSE;
        } else {
            return Result.TRUE;
        }
    }
    
    private boolean testValue(String value) {
        return pattern.matcher(value).find();
    }
}
