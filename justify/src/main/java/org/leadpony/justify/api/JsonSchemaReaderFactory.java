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

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

import javax.json.JsonException;

/**
 * A factory interface for creating {@link JsonSchemaReader} instances.
 *
 * <p>
 * The following code sample shows how to read a JSON schema from a string:
 * </p>
 *
 * <pre>
 * <code>
 * JsonValidationService service = JsonValidationService.newInstance();
 * JsonSchemaReaderFactory factory = service.createSchemaReaderFactory();
 * StringReader reader = new StringReader("{\"type\": \"integer\"}");
 * try (JsonSchemaReader reader = factory.createSchemaReader(reader)) {
 *     JsonSchema schema = reader.read();
 * }
 * </code>
 * </pre>
 *
 * <p>
 * Any instance of this class is safe for use by multiple concurrent threads.
 * </p>
 *
 * @author leadpony
 */
public interface JsonSchemaReaderFactory {

    /**
     * Creates a JSON schema reader from a byte stream. The character encoding of
     * the stream is determined as described in RFC 7159.
     *
     * @param in the byte stream from which a JSON schema is to be read.
     * @return newly created instance of JSON schema reader. It must be closed by
     *         the method caller after use.
     * @throws NullPointerException if the specified {@code in} is {@code null}.
     * @see JsonSchemaReader
     */
    JsonSchemaReader createSchemaReader(InputStream in);

    /**
     * Creates a JSON schema reader from a byte stream encoded by the specified
     * charset. The bytes of the stream will be decoded to characters using the
     * specified charset.
     *
     * @param in      the byte stream from which a JSON schema is to be read.
     * @param charset the character set.
     * @return newly created instance of JSON schema reader. It must be closed by
     *         the method caller after use.
     * @throws NullPointerException if the specified {@code in} or {@code charset}
     *                              is {@code null}.
     * @see JsonSchemaReader
     */
    JsonSchemaReader createSchemaReader(InputStream in, Charset charset);

    /**
     * Creates a JSON schema reader from a character stream.
     *
     * @param reader the character stream from which a JSON schema is to be read.
     * @return newly created instance of JSON schema reader. It must be closed by
     *         the method caller after use.
     * @throws NullPointerException if the specified {@code reader} is {@code null}.
     * @see JsonSchemaReader
     */
    JsonSchemaReader createSchemaReader(Reader reader);

    /**
     * Creates a JSON schema reader from a path.
     *
     * @param path the path from which a JSON schema is to be read.
     * @return newly created instance of JSON schema reader. It must be closed by
     *         the method caller after use.
     * @throws JsonException        if an I/O error occurs while creating reader.
     * @throws NullPointerException if the specified {@code path} is {@code null}.
     * @see JsonSchemaReader
     */
    JsonSchemaReader createSchemaReader(Path path);
}
