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

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.leadpony.justify.core.JsonSchema;

/**
 * Skeletal implementation of {@link JsonSchema}.
 * 
 * @author leadpony
 */
abstract class AbstractJsonSchema implements JsonSchema {
    
    private final JsonBuilderFactory builderFactory;

    protected AbstractJsonSchema(JsonBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
    }

    protected AbstractJsonSchema(AbstractJsonSchema other) {
        this.builderFactory = other.builderFactory;
    }

    @Override
    public JsonValue toJson() {
        JsonObjectBuilder builder = builderFactory.createObjectBuilder();
        addToJson(builder);
        return builder.build();
    }
    
    @Override
    public String toString() {
        return toJson().toString();
    }
    
    /**
     * Returns the factory for producing builders of JSON instances.
     * 
     * @return the JSON builder factory.
     */
    protected JsonBuilderFactory getBuilderFactory() {
        return builderFactory;
    }
    
    /**
     * Adds this schema to the JSON representation.
     * 
     * @param builder the builder for building JSON object, never be {@code null}.
     */
    protected abstract void addToJson(JsonObjectBuilder builder); 
}
