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

import java.util.List;
import java.util.stream.Collectors;

import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.JsonSchema;

/**
 * @author leadpony
 */
abstract class ItemSchemaFinder {
    
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
        
        private final JsonSchema schema;

        private CommonItemSchemaFinder(JsonSchema schema) {
            this.schema = schema;
        }

        @Override
        JsonSchema findSchema(int index) {
            return (schema != null) ? schema : JsonSchemas.ALWAYS_TRUE;
        }
        
        @Override
        ItemSchemaFinder negate() {
            return new CommonItemSchemaFinder(
                    (schema != null) ? schema.negate() : null);
        }

        @Override
        void toJson(JsonGenerator generator) {
            if (schema != null) {
                generator.writeKey("items");
                schema.toJson(generator);
            }
        }
    }

    private static class SeparateItemSchemaFinder extends ItemSchemaFinder {
        
        private final List<JsonSchema> schemas;
        private final JsonSchema additional;

        private SeparateItemSchemaFinder(List<JsonSchema> schemas, JsonSchema additional) {
            assert schemas != null;
            this.schemas = schemas;
            this.additional = additional;
        }

        @Override
        JsonSchema findSchema(int index) {
            if (index < schemas.size()) {
                return schemas.get(index);
            } else if (additional != null) {
                return additional;
            } else {
                return JsonSchemas.ALWAYS_TRUE;
            }
        }
        
        @Override
        ItemSchemaFinder negate() {
            return new SeparateItemSchemaFinder(
                    schemas.stream().map(JsonSchema::negate).collect(Collectors.toList()),
                    (additional != null) ? additional.negate() : null
                            );
        }

        @Override
        void toJson(JsonGenerator generator) {
            generator.writeKey("items");
            generator.writeStartArray();
            schemas.forEach(s->s.toJson(generator));
            generator.writeEnd();
            if (additional != null) {
                generator.writeKey("additionalItems");
                additional.toJson(generator);
            }
        }
    }
}
