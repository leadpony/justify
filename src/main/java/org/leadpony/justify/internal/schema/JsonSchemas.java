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
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

import org.leadpony.justify.core.JsonSchema;

/**
 * Utility class operating on {@link JsonSchema} instances.
 * 
 * @author leadpony
 */
public final class JsonSchemas {
    
    public static String toString(JsonSchema schema) {
        Objects.requireNonNull(schema, "schema must not be null.");
        StringWriter writer = new StringWriter();
        try (JsonGenerator generator = Json.createGenerator(writer)) {
            schema.toJson(generator);
        } catch (JsonException e) {
        }
        return writer.toString();
    }
    
    private JsonSchemas() {
    }
}
