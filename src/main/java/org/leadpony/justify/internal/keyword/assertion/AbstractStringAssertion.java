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
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;

/**
 * Assertion on values of string type.
 * 
 * @author leadpony
 */
abstract class AbstractStringAssertion implements Assertion, Evaluator {
    
    static enum Context {
        KEY,
        VALUE;
        
        String lowerName() {
            return name().toLowerCase();
        }
    };
    
    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender, JsonBuilderFactory builderFactory) {
        if (type == InstanceType.STRING) {
            appender.append(this);
        }
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
        assert event == Event.VALUE_STRING || event == Event.KEY_NAME;
        Context context = (event == Event.KEY_NAME) ? Context.KEY : Context.VALUE;
        return evaluateAgainstString(parser.getString(), context, parser, reporter);
    }
    
    /**
     * Evaluates this assertion on a string value.
     * 
     * @param value the value to apply this assertion.
     * @param context the context of the value.
     * @param parser the JSON parser.
     * @param reporter the reporter to which detected problems will be reported.
     * @return the result of the evaluation.
     */
    protected abstract Result evaluateAgainstString(String value, Context context, JsonParser parser, Reporter reporter);
}
