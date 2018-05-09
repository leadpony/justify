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
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ValidationResult;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.InstanceTypes;
import org.leadpony.justify.internal.base.JsonParserDecorator;

/**
 * JSON parser with validation functionality.
 * 
 * @author leadpony
 */
class ValidatingJsonParser extends JsonParserDecorator 
    implements ValidationResult, Consumer<Problem> {
    
    private final JsonSchema rootSchema;

    private final List<Problem> problems = new ArrayList<>();
    
    private Visitor visitor;
    
    ValidatingJsonParser(JsonParser real, JsonSchema rootSchema) {
        super(real);
        this.rootSchema = rootSchema;
        this.visitor = new RootVisitor(this.rootSchema);
    }
 
    @Override
    public Event next() {
        Event event = real.next();
        if (this.visitor != null) {
            visitInstance(event);
        }
        return event;
    }
  
    @Override
    public void accept(Problem problem) {
        problem.setLocation(real.getLocation());
        problems.add(problem);
    }
    
    @Override
    public boolean wasSuccess() {
        return problems.isEmpty();
    }

    @Override
    public Iterable<Problem> problems() {
        return Collections.unmodifiableList(problems);
    }

    private void visitInstance(Event event) {
        Visitor nextVisitor = this.visitor.visit(event);
        if (nextVisitor != this.visitor && nextVisitor != null) {
            nextVisitor.visit(event);
        }
        this.visitor = nextVisitor;
    }
    
    private void validateLeafInstance(Event event, Collection<JsonSchema> schemas) {
        InstanceType type = InstanceTypes.fromEvent(event, real);
        for (JsonSchema schema : schemas) {
            for (Evaluator evaluator : schema.createEvaluators(type)) {
                evaluator.evaluate(event, real, this);
            }
        }
    }
    
    /**
     * Visitor of JSON instance.
     */
    private static abstract class Visitor {
        
        private final Collection<JsonSchema> schemas;
        private final int depth;
        private final Visitor parent;
        
        protected Visitor(JsonSchema schema) {
            this.schemas = Arrays.asList(schema);
            this.depth = 0;
            this.parent = null;
        }
        
        protected Visitor(Collection<JsonSchema> schemas, int depth, Visitor parent) {
            this.schemas = schemas;
            this.depth = depth;
            this.parent = parent;
        }
        
        Collection<JsonSchema> schemas() {
            return schemas;
        }
        
        int depth() {
            return depth;
        }

        Visitor parent() {
            return parent;
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
                return new ObjectVisitor(schemas(), 0, this);
            } else if (event == Event.START_ARRAY) {
                return new ArrayVisitor(schemas(), 0, this);
            } else {
                validateLeafInstance(event, schemas());
                return null;
            }
        }
    }
    
    private abstract class ContainerVisitor extends Visitor {
        
        private final List<Evaluator> evaluators;

        protected ContainerVisitor(Collection<JsonSchema> schemas, int depth, Visitor parent, InstanceType type) {
            super(schemas, depth, parent);
            this.evaluators = schemas.stream()
                    .flatMap(schema->schema.createEvaluators(type).stream())
                    .collect(Collectors.toList());
        }
        
        protected void validateContainer(Event event) {
            Iterator<Evaluator> it = evaluators.iterator();
            while (it.hasNext()) {
                Evaluator e = it.next();
                Evaluator.Status status = e.evaluate(event, real, ValidatingJsonParser.this);
                if (status != Evaluator.Status.CONTINUED) {
                    it.remove();
                }
            }
        }
    }
    
    private class ObjectVisitor extends ContainerVisitor {

        private String propertyName;
        
        ObjectVisitor(Collection<JsonSchema> schemas, int depth, Visitor parent) {
            super(schemas, depth, parent, InstanceType.OBJECT);
        }
        
        @Override
        public Visitor visit(Event event) {
            validateContainer(event);
            switch (event) {
            case START_OBJECT:
                return new ObjectVisitor(findChildSchemas(propertyName), depth() + 1, this);
            case START_ARRAY:
                return new ArrayVisitor(findChildSchemas(propertyName), depth() + 1, this);
            case END_OBJECT:
                return parent();
            case KEY_NAME:
                this.propertyName = real.getString();
                return this;
            default:
                validateLeafInstance(event, findChildSchemas(propertyName));
                return this;
            }
        }
        
        private Collection<JsonSchema> findChildSchemas(String propertyName) {
            List<JsonSchema> children = new ArrayList<>();
            for (JsonSchema schema: schemas()) {
                schema.collectChildSchema(propertyName, children);
            }
            return children;
        }
    }

    private class ArrayVisitor extends ContainerVisitor {

        private int itemIndex;
        
        ArrayVisitor(Collection<JsonSchema> schemas, int depth, Visitor parent) {
            super(schemas, depth, parent, InstanceType.ARRAY);
        }

        @Override
        public Visitor visit(Event event) {
            validateContainer(event);
            switch (event) {
            case START_OBJECT:
                return new ObjectVisitor(findChildSchemas(itemIndex++), depth() + 1, this);
            case START_ARRAY:
                return new ArrayVisitor(findChildSchemas(itemIndex++), depth() + 1, this);
            case END_ARRAY:
                return parent();
            default:
                validateLeafInstance(event, findChildSchemas(itemIndex));
                return this;
            }
        }
        
        private Collection<JsonSchema> findChildSchemas(int itemIndex) {
            List<JsonSchema> children = new ArrayList<>();
            for (JsonSchema schema: schemas()) {
                schema.collectChildSchema(itemIndex, children);
            }
            return children;
        }
    }
}
