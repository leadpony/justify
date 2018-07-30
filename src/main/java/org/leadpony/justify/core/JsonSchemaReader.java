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

import java.io.Closeable;
import java.util.NoSuchElementException;

import javax.json.JsonException;
import javax.json.stream.JsonParsingException;

/**
 * Reads a JSON schema from an input source. 
 * 
 * <p>
 * The following example shows how to read an JSON schema from a string: 
 * </p>
 * <pre><code>
 * StringReader reader = new StringReader("{\"type\": \"integer\"}");
 * JsonSchemaReader schemaReader = Jsonv.createSchemaReader(reader);
 * JsonSchema schema = schemaReader.read();
 * schemaReader.close();
 * </code></pre>
 * 
 * <p>Each instance of this type is NOT safe for use by multiple concurrent threads.</p>
 * 
 * @author leadpony
 */
public interface JsonSchemaReader extends Closeable {
    
     /**
     * Returns a JSON schema that is represented in the input source. 
     * This method needs to be called only once for a reader instance.
     * 
     * @return the JSON schema, never be {@code null}.
     * @throws JsonException if an I/O error occurs while reading.
     * @throws JsonParsingException if the parser encounters invalid JSON while reading.
     * @throws NoSuchElementException if the input is empty.
     * @throws JsonValidatingException if the reader found any problems during validation of the schema.
     * @throws IllegalStateException if read or close method is already called.
     */
    JsonSchema read();
    
    /**
     * Closes this reader and frees any resources associated with the reader. 
     * This method closes the underlying input source.
     * 
     * @throws JsonException if an I/O error occurs while closing this reader.
     */
    @Override
    void close();
    
    /**
     * Assigns a resolver of external JSON schemas to this reader.
     * 
     * @param resolver the resolver of external JSON schemas.
     * @return this reader.
     * @throws NullPointerException if the specified {@code resolver} was {@code null}.
     */
    JsonSchemaReader withSchemaResolver(JsonSchemaResolver resolver);
}
