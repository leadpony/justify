/*
 * Copyright 2018-2019 the Justify authors.
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

package org.leadpony.justify.api;

import java.io.Closeable;

import jakarta.json.JsonException;
import jakarta.json.stream.JsonParsingException;

/**
 * A reader interface for reading a JSON schema from an input source.
 *
 * <p>
 * The following example shows how to read a JSON schema from a string:
 * </p>
 *
 * <pre>
 * <code>
 * JsonValidationService service = JsonValidationService.newInstance();
 * StringReader reader = new StringReader("{\"type\": \"integer\"}");
 * JsonSchemaReader schemaReader = service.createSchemaReader(reader);
 * JsonSchema schema = schemaReader.read();
 * schemaReader.close();
 * </code>
 * </pre>
 *
 * <p>
 * Each instance of this type is NOT safe for use by multiple concurrent
 * threads.
 * </p>
 *
 * @author leadpony
 */
public interface JsonSchemaReader extends Closeable {

    /**
     * The property used to specify whether the schema reader is strict with
     * keywords or not.
     */
    String STRICT_KEYWORDS = "org.leadpony.justify.api.JsonSchemaReader.STRICT_KEYWORDS";

    /**
     * The property used to specify whether the schema reader is strict with formats
     * or not.
     */
    String STRICT_FORMATS = "org.leadpony.justify.api.JsonSchemaReader.STRICT_FORMATS";

    /**
     * The property used to specify whether the schema reader uses custom format
     * attributes or not.
     */
    String CUSTOM_FORMATS = "org.leadpony.justify.api.JsonSchemaReader.CUSTOM_FORMATS";

    /**
     * The property used to specify the list of schema resolvers.
     */
    String RESOLVERS = "org.leadpony.justify.api.JsonSchemaReader.RESOLVERS";

    /**
     * The property used to specify the default version of the JSON Schema
     * specification.
     */
    String DEFAULT_SPEC_VERSION = "org.leadpony.justify.api.JsonSchemaReader.DEFAULT_SPEC_VERSION";

    /**
     * The property used to specify whether the schema reader validates the schema
     * against the metaschema or not.
     */
    String SCHEMA_VALIDATION = "org.leadpony.justify.api.JsonSchemaReader.SCHEMA_VALIDATION";

    /**
     * The property used to specify whether the automatic detection of specification
     * version is enabled or not.
     */
    String SPEC_VERSION_DETECTION = "org.leadpony.justify.api.JsonSchemaReader.SPEC_VERSION_DETECTION";

    /**
     * The property used to specify the metaschema to be used when validating the schema.
     */
    String METASCHEMA = "org.leadpony.justify.api.JsonSchemaReader.METASCHEMA";

    /**
     * Returns a JSON schema that is represented in the input source. This method
     * needs to be called only once for a reader instance.
     *
     * @return the JSON schema, never be {@code null}.
     * @throws JsonException           if an I/O error occurs while reading.
     * @throws JsonParsingException    if the parser encounters invalid JSON while
     *                                 reading.
     * @throws JsonValidatingException if the reader found any problems during
     *                                 validation of the schema.
     * @throws IllegalStateException   if {@link #read()} or {@link #close()} method
     *                                 is already called.
     */
    JsonSchema read();

    /**
     * Closes this reader and frees any resources associated with this reader. This
     * method closes the underlying input source.
     *
     * @throws JsonException if an I/O error occurs while closing this reader.
     */
    @Override
    void close();
}
