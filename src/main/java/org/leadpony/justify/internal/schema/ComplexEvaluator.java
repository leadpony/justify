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

package org.leadpony.justify.internal.schema;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.InstanceTypes;

/**
 * Evaluator for JSON schema having subschemas or child schemas.
 * 
 * @author leadpony
 */
public abstract class ComplexEvaluator extends SimpleEvaluator {

    protected final List<Evaluator> subschemaEvaluators;
    
    public static Evaluator newValuator(InstanceType type, SimpleSchema schema) {
        switch (type) {
        case ARRAY:
            return new ArrayVisitor(schema);
        case OBJECT:
            return new ObjectVisitor(schema);
        default:
            return new SimpleVisitor(type, schema);
        }
    }
    
    protected ComplexEvaluator(InstanceType type, SimpleSchema schema) {
        super(type, schema);
        this.subschemaEvaluators = createSubschemaEvaluators(type);
    }

    private List<Evaluator> createSubschemaEvaluators(InstanceType type) {
        List<Evaluator> evaluators = new LinkedList<>();
        for (JsonSchema schema : schema.subschemas()) {
            evaluators.add(schema.createEvaluator(type));
        }
        return evaluators;
    }
   
    protected boolean evaluateSubschemas(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        if (subschemaEvaluators.isEmpty()) {
            return true;
        }
        return invokeEvaluators(subschemaEvaluators, event, parser, depth, consumer);
    }

    private static class SimpleVisitor extends ComplexEvaluator {

        private SimpleVisitor(InstanceType type, SimpleSchema schema) {
            super(type, schema);
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            boolean continued = evaluateAssertions(event, parser, depth, consumer) &&
                             evaluateSubschemas(event, parser, depth, consumer); 
            return continued ? Result.TRUE : Result.FALSE;
        }
    }

    private abstract static class ContainerVisitor extends ComplexEvaluator {
        
        protected Evaluator child;
        
        protected ContainerVisitor(InstanceType type, SimpleSchema schema) {
            super(type, schema);
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            boolean continued = evaluateAssertions(event, parser, depth, consumer) &&
                             evaluateSubschemas(event, parser, depth, consumer) &&
                             invokeChildEvaluator(event, parser, depth, consumer);
            if (continued) {
                return canContinue(event, depth) ? Result.CONTINUED : Result.TRUE;
            } else {
                return Result.FALSE;
            }
        }

        private boolean invokeChildEvaluator(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
            if (depth == 1) {
                updateChild(event, parser);
            }
            if (child == null) {
                return true;
            }
            Result result = child.evaluate(event, parser, depth - 1, consumer);
            if (result != Result.CONTINUED) {
                child = null;
                return result != Result.FALSE;
            } else {
                return true;
            }
        }
        
        protected abstract void updateChild(Event event, JsonParser parser);

        protected abstract boolean canContinue(Event event, int depth);
        
    }
    
    private static class ArrayVisitor extends ContainerVisitor {

        private int itemIndex;
        
        private ArrayVisitor(SimpleSchema schema) {
            super(InstanceType.ARRAY, schema);
        }
        
        protected void updateChild(Event event, JsonParser parser) {
            switch (event) {
            case END_OBJECT:
            case END_ARRAY:
                break;
            default:
                child = findChild(InstanceTypes.fromEvent(event, parser), itemIndex);
                break;
            }
        }
        
        protected boolean canContinue(Event event, int depth) {
            return depth > 0 || event != Event.END_ARRAY;
        }
        
        protected Evaluator findChild(InstanceType type, int itemIndex) {
            JsonSchema child = schema.findChildSchema(itemIndex);
            if (child == null) {
                return null;
            }
            return child.createEvaluator(type);
        }
    }

    private static class ObjectVisitor extends ContainerVisitor {

        private String propertyName;
        
        private ObjectVisitor(SimpleSchema schema) {
            super(InstanceType.OBJECT, schema);
        }
        
        protected void updateChild(Event event, JsonParser parser) {
            switch (event) {
            case KEY_NAME:
                propertyName = parser.getString();
                child = null;
                break;
            case END_ARRAY:
            case END_OBJECT:
                break;
            default:
                child = findChild(InstanceTypes.fromEvent(event, parser), propertyName);
                break;
            }
        }
        
        protected boolean canContinue(Event event, int depth) {
            return depth > 0 || event != Event.END_OBJECT;
        }
        
        protected Evaluator findChild(InstanceType type, String propertyName) {
            JsonSchema child = schema.findChildSchema(propertyName);
            if (child == null) {
                return null;
            }
            return child.createEvaluator(type);
        }
    }
}
