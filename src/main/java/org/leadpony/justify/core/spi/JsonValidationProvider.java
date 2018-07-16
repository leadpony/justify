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
package org.leadpony.justify.core.spi;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;
import org.leadpony.justify.core.JsonSchemaException;
import org.leadpony.justify.core.JsonSchemaReader;
import org.leadpony.justify.core.Problem;

/**
 * Service provider for JSON validation objects. 
 * 
 * <p>All the public methods in this class are safe for use by multiple concurrent threads.</p>
 * 
 * @author leadpony
 * @see ServiceLoader
 */
public abstract class JsonValidationProvider {
    
    private static final ThreadLocal<JsonValidationProvider> threadLocalProvider =
            ThreadLocal.withInitial(JsonValidationProvider::createProvider);
    
    /**
     * Returns an instance of this provider class.
     * 
     * @return the instance of this provider class.
     * @throws JsonSchemaException if there is no provider found.
     */
    public static JsonValidationProvider provider() {
        JsonValidationProvider provider = threadLocalProvider.get();
        if (provider == null) {
            throw new JsonSchemaException("JSON schema provider is not installed.");
        }
        return provider;
    }
    
    /**
     * Constructs this provider.
     */
    protected JsonValidationProvider() {
    }
    
    /**
     * Creates a JSON schema reader from a byte stream. 
     * The character encoding of the stream is determined as described in RFC 7159.
     * 
     * @param in the byte stream from which a JSON schema is to be read.
     * @return newly created instance of JSON schema reader.
     * @throws NullPointerException if {@code in} is {@code null}.
     */
    public abstract JsonSchemaReader createSchemaReader(InputStream in);
    
    /**
     * Creates a JSON schema reader from a byte stream. 
     * The bytes of the stream are decoded to characters using the specified charset.
     * 
     * @param in the byte stream from which a JSON schema is to be read.
     * @param charset the character set.
     * @return newly created instance of JSON schema reader.
     * @throws NullPointerException if specified {@code in} or {@code charset} is {@code null}.
     */
    public abstract JsonSchemaReader createSchemaReader(InputStream in, Charset charset);

    /**
     * Creates a JSON schema reader from a reader. 
     * 
     * @param reader the reader from which a JSON schema is to be read.
     * @return newly created instance of JSON schema reader.
     * @throws NullPointerException if {@code reader} is {@code null}.
     */
    public abstract JsonSchemaReader createSchemaReader(Reader reader);

    /**
     * Creates a factory for producing JSON schema builders.
     *  
     * @return newly created instance of JSON schema builder factory.
     */
    public abstract JsonSchemaBuilderFactory createSchemaBuilderFactory();
    
    public abstract JsonParserFactory createParserFactory(Map<String,?> config, JsonSchema schema, 
            Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier);
   
    public abstract JsonParser createParser(InputStream in, JsonSchema schema, Consumer<? super List<Problem>> handler);
  
    public abstract JsonParser createParser(InputStream in, Charset charset, JsonSchema schema, Consumer<? super List<Problem>> handler);

    public abstract JsonParser createParser(Reader reader, JsonSchema schema, Consumer<? super List<Problem>> handler);
 
    public abstract JsonReaderFactory createReaderFactory(Map<String, ?> config, JsonSchema schema,
            Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier);

    public abstract JsonReader createReader(InputStream in, JsonSchema schema, Consumer<? super List<Problem>> handler);

    public abstract JsonReader createReader(InputStream in, Charset charset, JsonSchema schema, Consumer<? super List<Problem>> handler);
  
    public abstract JsonReader createReader(Reader reader, JsonSchema schema, Consumer<? super List<Problem>> handler);
    
    public abstract JsonProvider createJsonProvider(JsonSchema schema,
            Function<JsonParser, Consumer<? super List<Problem>>> handlerSupplier);
    
    /**
     * Creates a new instance of problem printer.
     * 
     * @param lineConsumer the object which will consume the line to print.
     * @return the newly created instance of problem printer.
     * @throws NullPointerException if the specified {@code lineConsumer} was {@code null}.
     */
    public abstract Consumer<List<Problem>> createProblemPrinter(Consumer<String> lineConsumer);

    /**
     * Initializes this provider immediately after its instantiation.  
     * 
     * @param jsonProvider the JSON provider to attach.
     * @throws NullPointerException if the specified {@code jsonProvider} was {@code null}.
     */
    protected abstract void initialize(JsonProvider jsonProvider);
    
    /**
     * Creates an instance of this provider class for each thread.
     * 
     * @return the instance of this provider class.
     */
    private static JsonValidationProvider createProvider() {
        JsonProvider jsonProvider = JsonProvider.provider();
        ServiceLoader<JsonValidationProvider> loader = ServiceLoader.load(JsonValidationProvider.class);
        Iterator<JsonValidationProvider> it = loader.iterator();
        if (it.hasNext()) {
            JsonValidationProvider found = it.next();
            found.initialize(jsonProvider);
            return found;
        } else {
            return null;
        }
    }
}
