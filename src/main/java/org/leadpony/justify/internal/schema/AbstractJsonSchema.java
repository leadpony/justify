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

import java.io.StringWriter;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.JsonSchema;

/**
 * Skeletal implementation of {@link JsonSchema}.
 * 
 * @author leadpony
 */
abstract class AbstractJsonSchema implements JsonSchema {
    
    private final JsonProvider jsonProvider;

    protected AbstractJsonSchema(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    protected AbstractJsonSchema(AbstractJsonSchema other) {
        this.jsonProvider = other.jsonProvider;
    }

    @Override
    public JsonValue toJson() {
        JsonObjectBuilder builder = getJsonProvider().createObjectBuilder();
        addToJson(builder);
        return builder.build();
    }
    
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try (JsonGenerator generator = getJsonProvider().createGenerator(writer)) {
            generator.write(toJson());
        }
        return writer.toString();
    }
    
    protected JsonProvider getJsonProvider() {
        return jsonProvider;
    }
    
    /**
     * Adds this schema to the JSON representation.
     * 
     * @param builder the builder for building JSON object, never be {@code null}.
     */
    protected abstract void addToJson(JsonObjectBuilder builder); 
}
