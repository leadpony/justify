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

import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;

/**
 * "type" assertion specialized for single type.
 *  
 * @author leadpony
 */
class SingleType extends AbstractAssertion implements Evaluator {
    
    protected final InstanceType type;
    
    SingleType(InstanceType type) {
        this.type = type;
    }
    
    @Override
    public String name() {
        return "type";
    }

    @Override
    public Evaluator createEvaluator(InstanceType type, JsonBuilderFactory builderFactory, boolean affirmative) {
        return affirmative ? this : this::evaluateNegated;
    }
    
    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add("type", type.name().toLowerCase());
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        InstanceType type = ParserEvents.toInstanceType(event, parser);
        if (type == null || testType(type)) {
            return Result.TRUE;
        } else {
            Problem p = createProblemBuilder(parser)
                    .withMessage("instance.problem.type")
                    .withParameter("actual", type)
                    .withParameter("expected", this.type)
                    .build();
            reporter.accept(p);
            return Result.FALSE;
        }
    }
    
    private Result evaluateNegated(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        InstanceType type = ParserEvents.toInstanceType(event, parser);
        if (type == null || !testType(type)) {
            return Result.TRUE; 
        } else {
            Problem p = createProblemBuilder(parser)
                    .withMessage("instance.problem.not.type")
                    .withParameter("expected", this.type)
                    .build();
            reporter.accept(p);
            return Result.FALSE;
        }
    }

    private boolean testType(InstanceType type) {
        if (type == this.type) {
            return true;
        } else if (type == InstanceType.INTEGER) {
            return this.type == InstanceType.NUMBER;
        } else {
            return false;
        }
    }
}
