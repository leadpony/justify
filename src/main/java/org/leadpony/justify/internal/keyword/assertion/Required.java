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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;

/**
 * Assertion specified with "required" validation keyword.
 * 
 * @author leadpony
 */
class Required implements Assertion {
    
    protected final Set<String> names;
    
    Required(Set<String> names) {
        this.names = new LinkedHashSet<>(names);
    }
    
    @Override
    public String name() {
        return "required";
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender) {
        if (type == InstanceType.OBJECT) {
            appender.append(new PropertyEvaluator(names));
        }
    }

    @Override
    public Assertion negate() {
        return new NotRequired(names);
    }

    @Override
    public void addToJson(JsonObjectBuilder builder) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        names.forEach(arrayBuilder::add);
        builder.add(name(), arrayBuilder);
    }

    static class PropertyEvaluator implements ShallowEvaluator {
        
        protected final Set<String> remaining;
        
        PropertyEvaluator(Set<String> required) {
            this.remaining = new LinkedHashSet<>(required);
        }

        @Override
        public Result evaluateShallow(Event event, JsonParser parser, int depth, Reporter reporter) {
            if (event == Event.KEY_NAME) {
                remaining.remove(parser.getString());
                return test(parser, reporter, false);
            } else if (depth == 0 && event == Event.END_OBJECT) {
                return test(parser, reporter, true);
            } else {
                return Result.PENDING;
            }
        }
        
        protected Result test(JsonParser parser, Reporter reporter, boolean last) {
            if (remaining.isEmpty()) {
                return Result.TRUE;
            } else if (last) {
                Problem p = ProblemBuilder.newBuilder(parser)
                        .withMessage(remaining.size() > 1 ?
                                "instance.problem.required.plural" : "instance.problem.required")
                        .withParameter("expected", getRemaining())
                        .build();
                reporter.reportProblem(p);
                return Result.FALSE;
            } else {
                return Result.PENDING;
            }
        }
        
        /**
         * Returns remaining property name or a set of names.
         * @return a string or a set of names.
         */
        protected Object getRemaining() {
            return remaining.size() > 1 ? 
                    remaining : remaining.iterator().next();
        }
    }
}
