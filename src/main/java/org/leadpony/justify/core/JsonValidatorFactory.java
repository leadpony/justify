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

package org.leadpony.justify.core;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Objects;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.core.spi.JsonSchemaProvider;

/**
 * Factory for producing parsers and readers that will validate 
 * JSON instances based on the specified schema.
 * <p>
 * Any instance of this class is safe for use by multiple concurrent threads.
 * </p>
 * 
 * @author leadpony
 */
public interface JsonValidatorFactory {

    /**
     * Creates a new instance of this class.
     * 
     * @param schema the JSON schema to apply when validating JSON instances.
     * @return the newly created instance of this class.
     * @throws NullPointerException if the specified parameter was {@code null}.
     */
    static JsonValidatorFactory newFactory(JsonSchema schema) {
        return JsonSchemaProvider.provider().createValidatorFactory(schema);
    }
    
    JsonParserFactory createParserFactory(Map<String,?> config);
    
    default JsonParser createParser(InputStream in) {
        Objects.requireNonNull(in, "in must not be null.");
        return createParserFactory(null).createParser(in);
    }
    
    default JsonParser createParser(Reader reader) {
        Objects.requireNonNull(reader, "reader must not be null.");
        return createParserFactory(null).createParser(reader);
    }
    
    JsonProvider toJsonProvider();
}
