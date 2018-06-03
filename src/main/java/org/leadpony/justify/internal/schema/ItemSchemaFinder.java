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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.base.CompositeIterator;

/**
 * @author leadpony
 */
abstract class ItemSchemaFinder implements Iterable<JsonSchema> {
    
    static ItemSchemaFinder of(JsonSchema schema) {
        return new CommonItemSchemaFinder(schema);
    }

    static ItemSchemaFinder of(List<JsonSchema> schemas, JsonSchema additional) {
        return new SeparateItemSchemaFinder(schemas, additional);
    }

    abstract JsonSchema findSchema(int index);
    
    abstract ItemSchemaFinder negate();
    
    abstract void toJson(JsonGenerator generator);
    
    private static class CommonItemSchemaFinder extends ItemSchemaFinder {
        
        private final Optional<JsonSchema> schema;

        private CommonItemSchemaFinder(JsonSchema schema) {
            this(Optional.ofNullable(schema));
        }
        
        private CommonItemSchemaFinder(Optional<JsonSchema> schema) {
            this.schema = schema;
        }

        @Override
        public Iterator<JsonSchema> iterator() {
            return schema
                    .map(s->Collections.singleton(s).iterator())
                    .orElse(Collections.emptyIterator());
        }

        @Override
        JsonSchema findSchema(int index) {
            return schema.orElse(JsonSchema.TRUE);
        }
        
        @Override
        ItemSchemaFinder negate() {
            return new CommonItemSchemaFinder(schema.map(JsonSchema::negate));
        }

        @Override
        void toJson(JsonGenerator generator) {
            schema.ifPresent(schema->{
                generator.writeKey("items");
                schema.toJson(generator);
            });
        }
    }

    private static class SeparateItemSchemaFinder extends ItemSchemaFinder {
        
        private final List<JsonSchema> schemas;
        private final Optional<JsonSchema> additional;

        private SeparateItemSchemaFinder(List<JsonSchema> schemas, JsonSchema additional) {
            this(schemas, Optional.ofNullable(additional));
        }
        
        private SeparateItemSchemaFinder(List<JsonSchema> schemas, Optional<JsonSchema> additional) {
            assert schemas != null;
            this.schemas = schemas;
            this.additional = additional;
        }

        @Override
        public Iterator<JsonSchema> iterator() {
            return new CompositeIterator<JsonSchema>().add(schemas).add(additional);
        }

        @Override
        JsonSchema findSchema(int index) {
            if (index < schemas.size()) {
                return schemas.get(index);
            } else {
                return additional.orElse(JsonSchema.TRUE);
            }
        }
        
        @Override
        ItemSchemaFinder negate() {
            return new SeparateItemSchemaFinder(
                    schemas.stream().map(JsonSchema::negate).collect(Collectors.toList()),
                    additional.map(JsonSchema::negate)
                    );
        }

        @Override
        void toJson(JsonGenerator generator) {
            generator.writeKey("items");
            generator.writeStartArray();
            schemas.forEach(s->s.toJson(generator));
            generator.writeEnd();
            additional.ifPresent(schema->{
                generator.writeKey("additionalItems");
                schema.toJson(generator);
            });
        }
    }
}
