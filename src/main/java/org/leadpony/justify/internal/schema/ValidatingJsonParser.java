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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonValidator;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.JsonParserDecorator;

/**
 * JSON parser with validation functionality.
 * 
 * @author leadpony
 */
class ValidatingJsonParser extends JsonParserDecorator 
    implements JsonValidator, Consumer<Problem> {
    
    private final JsonSchema rootSchema;

    private final List<Problem> problems = new ArrayList<>();
    
    private Visitor visitor;
    
    @SuppressWarnings("serial")
    private static final Map<Event, InstanceType> eventToTypeMap = new HashMap<Event, InstanceType>() {{
        put(Event.START_OBJECT, InstanceType.OBJECT);
        put(Event.START_ARRAY, InstanceType.ARRAY);
        put(Event.VALUE_NUMBER, InstanceType.NUMBER);
        put(Event.VALUE_STRING, InstanceType.STRING);
        put(Event.VALUE_TRUE, InstanceType.BOOLEAN);
        put(Event.VALUE_FALSE, InstanceType.BOOLEAN);
        put(Event.VALUE_NULL, InstanceType.NULL);
    }};

    ValidatingJsonParser(JsonParser real, JsonSchema rootSchema) {
        super(real);
        this.rootSchema = rootSchema;
        this.visitor = new RootVisitor(this.rootSchema);
    }
 
    @Override
    public Event next() {
        Event event = real.next();
        processEvent(event);
        return event;
    }
  
    @Override
    public void accept(Problem problem) {
        problem.setLocation(real.getLocation());
        problems.add(problem);
    }
    
    @Override
    public boolean hasProblem() {
        return !problems.isEmpty();
    }

    @Override
    public Iterable<Problem> problems() {
        return Collections.unmodifiableList(problems);
    }

    private void processEvent(Event event) {
        Visitor nextVisitor = this.visitor.visit(event);
        this.visitor = nextVisitor;
    }
    
    private void evaluateLeafInstance(Event event, Collection<JsonSchema> schemas) {
        InstanceType type = mapToType(event);
        for (JsonSchema schema : schemas) {
            for (Evaluator evaluator : schema.createEvaluators(type)) {
                evaluator.evaluate(event, real, this);
            }
        }
    }
    
    private InstanceType mapToType(Event event) {
        InstanceType type = eventToTypeMap.get(event);
        if (type == InstanceType.NUMBER && real.isIntegralNumber()) {
            return InstanceType.INTEGER;
        }
        return type;
    }
    
    /**
     * Visitor of JSON instance.
     */
    private static abstract class Visitor {
        
        protected final Visitor parent;
        protected final Collection<JsonSchema> schemas;
        
        protected Visitor(JsonSchema schema) {
            this.parent = null;
            this.schemas = Arrays.asList(schema);
        }
        
        protected Visitor(Visitor parent, Collection<JsonSchema> schemas) {
            this.parent = parent;
            this.schemas = schemas;
        }
        
        abstract Visitor visit(Event event);
    }
    
    private class RootVisitor extends Visitor {
        
        RootVisitor(JsonSchema schema) {
            super(schema);
        }

        @Override
        public Visitor visit(Event event) {
            if (event == Event.START_OBJECT) {
                return new ObjectVisitor(null, this.schemas);
            } else if (event == Event.START_ARRAY) {
                return new ArrayVisitor(null, this.schemas);
            } else {
                evaluateLeafInstance(event, schemas);
                return null;
            }
        }
    }
    
    private class ObjectVisitor extends Visitor {

        private String propertyName;
        
        ObjectVisitor(Visitor parent, Collection<JsonSchema> schemas) {
            super(parent, schemas);
        }
        
        @Override
        public Visitor visit(Event event) {
            switch (event) {
            case START_OBJECT:
                return new ObjectVisitor(this, findChildSchemas(propertyName));
            case START_ARRAY:
                return new ArrayVisitor(this, findChildSchemas(propertyName));
            case END_OBJECT:
                return parent;
            case KEY_NAME:
                this.propertyName = real.getString();
                return this;
            default:
                evaluateLeafInstance(event, findChildSchemas(propertyName));
                return this;
            }
        }
        
        private Collection<JsonSchema> findChildSchemas(String propertyName) {
            List<JsonSchema> children = new ArrayList<>();
            for (JsonSchema schema: this.schemas) {
                schema.collectChildSchema(propertyName, children);
            }
            return children;
        }
    }

    private class ArrayVisitor extends Visitor {

        private int itemIndex;
        
        ArrayVisitor(Visitor parent, Collection<JsonSchema> schemas) {
            super(parent, schemas);
        }

        @Override
        public Visitor visit(Event event) {
            switch (event) {
            case START_OBJECT:
                return new ObjectVisitor(this, findChildSchemas(itemIndex++));
            case START_ARRAY:
                return new ArrayVisitor(this, findChildSchemas(itemIndex++));
            case END_ARRAY:
                return parent;
            default:
                evaluateLeafInstance(event, findChildSchemas(itemIndex++));
                return this;
            }
        }
        
        private Collection<JsonSchema> findChildSchemas(int itemIndex) {
            List<JsonSchema> children = new ArrayList<>();
            for (JsonSchema schema: this.schemas) {
                schema.collectChildSchema(itemIndex, children);
            }
            return children;
        }
    }
}
