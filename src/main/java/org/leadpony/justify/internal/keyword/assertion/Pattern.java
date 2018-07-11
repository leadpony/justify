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

import java.util.regex.Matcher;

import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

/**
 * Assertion specified with "pattern" validation keyword.
 * 
 * @author leadpony
 */
class Pattern extends ShallowAssertion {
    
    private final java.util.regex.Pattern pattern; 
    
    Pattern(java.util.regex.Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public String name() {
        return "pattern";
    }

    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender) {
        if (type == InstanceType.STRING) {
            appender.append(this);
        }
    }
    
    @Override
    public void addToJson(JsonObjectBuilder builder) {
        builder.add(name(), pattern.toString());
    }
    
    @Override
    protected Result evaluateShallow(Event event, JsonParser parser, int depth, Reporter reporter) {
        Matcher m = pattern.matcher(parser.getString());
        if (m.find()) {
            return Result.TRUE;
        } else {
            Problem p = ProblemBuilder.newBuilder(parser)
                    .withMessage("instance.problem.pattern")
                    .withParameter("pattern", pattern.toString())
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        }
    }
}
