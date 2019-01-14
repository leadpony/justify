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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.ParserEvents;

/**
 * Assertion representing "type" keyword.
 *  
 * @author leadpony
 */
abstract class Type extends AbstractAssertion {
    
    @Override
    public String name() {
        return "type";
    }
    
    /**
     * Type assertion specialized for single type.
     * 
     * @author leadpony
     */
    static class Single extends Type implements Evaluator {
        
        private final InstanceType type;
        
        Single(InstanceType type) {
            this.type = type;
        }
        
        @Override
        protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
            return this;
        }
        
        @Override
        protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
            return this::evaluateNegated;
        }

        @Override
        public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            builder.add("type", type.name().toLowerCase());
        }
    
        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            InstanceType type = ParserEvents.toInstanceType(event, parser);
            if (type == null || testType(type)) {
                return Result.TRUE;
            } else {
                Problem p = createProblemBuilder(parser)
                        .withMessage("instance.problem.type")
                        .withParameter("actual", type)
                        .withParameter("expected", this.type)
                        .build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            }
        }
        
        private Result evaluateNegated(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            InstanceType type = ParserEvents.toInstanceType(event, parser);
            if (type == null || !testType(type)) {
                return Result.TRUE; 
            } else {
                Problem p = createProblemBuilder(parser)
                        .withMessage("instance.problem.not.type")
                        .withParameter("expected", this.type)
                        .build();
                dispatcher.dispatchProblem(p);
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

    /**
     * Type assertion specialized for multiple types.
     * 
     * @author leadpony
     */
    static class Multiple extends Type implements Evaluator {
        
        private final Set<InstanceType> typeSet;
        
        Multiple(Set<InstanceType> types) {
            this.typeSet = new LinkedHashSet<>(types);
        }
        
        @Override
        protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
            return this;
        }

        @Override
        protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
            return this::evaluateNegated;
        }

        @Override
        public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
            JsonArrayBuilder arrayBuilder = builderFactory.createArrayBuilder();
            typeSet.stream()
                .map(InstanceType::name)
                .map(String::toLowerCase)
                .forEach(arrayBuilder::add);
            builder.add("type", arrayBuilder);
        }
        
        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            InstanceType type = ParserEvents.toInstanceType(event, parser);
            if (type != null) {
                return assertTypeMatches(type, parser, dispatcher);
            } else {
                return Result.TRUE;
            }
        }
        
        private Result evaluateNegated(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            InstanceType type = ParserEvents.toInstanceType(event, parser);
            if (type != null) {
                return assertTypeNotMatches(type, parser, dispatcher);
            } else {
                return Result.TRUE;
            }
        }

        private boolean contains(InstanceType type) {
            return typeSet.contains(type) ||
                   (type == InstanceType.INTEGER && typeSet.contains(InstanceType.NUMBER));
        }

        private Result assertTypeMatches(InstanceType type, JsonParser parser, ProblemDispatcher dispatcher) {
            if (contains(type)) {
                return Result.TRUE;
            } else {
                Problem p = createProblemBuilder(parser)
                        .withMessage("instance.problem.type.plural")
                        .withParameter("actual", type)
                        .withParameter("expected", typeSet)
                        .build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            }
        }
        
        private Result assertTypeNotMatches(InstanceType type, JsonParser parser, ProblemDispatcher dispatcher) {
            if (contains(type)) {
                Problem p = createProblemBuilder(parser)
                        .withMessage("instance.problem.not.type.plural")
                        .withParameter("actual", type)
                        .withParameter("expected", typeSet)
                        .build();
                dispatcher.dispatchProblem(p);
                return Result.FALSE;
            } else {
                return Result.TRUE;
            }
        }
    }
}
