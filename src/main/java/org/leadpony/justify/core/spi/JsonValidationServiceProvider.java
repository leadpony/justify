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
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.json.spi.JsonProvider;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;
import org.leadpony.justify.core.JsonSchemaException;
import org.leadpony.justify.core.JsonSchemaReader;
import org.leadpony.justify.core.JsonValidatorFactory;

/**
 * Service provider for JSON validation objects. 
 * 
 * <p>All the public methods in this class are safe for use by multiple concurrent threads.</p>
 * 
 * @author leadpony
 * @see ServiceLoader
 */
public abstract class JsonValidationServiceProvider {
    
    private static final ThreadLocal<JsonValidationServiceProvider> threadLocalProvider =
            ThreadLocal.withInitial(JsonValidationServiceProvider::createProvider);
    
    /**
     * Returns an instance of this provider class.
     * 
     * @return the instance of this provider class.
     * @throws JsonSchemaException if there is no provider found.
     */
    public static JsonValidationServiceProvider provider() {
        JsonValidationServiceProvider provider = threadLocalProvider.get();
        if (provider == null) {
            throw new JsonSchemaException("JSON schema provider is not installed.");
        }
        return provider;
    }
    
    /**
     * Constructs this provider.
     */
    protected JsonValidationServiceProvider() {
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
     * Creates a JSON schema reader from a reader. 
     * 
     * @param reader the reader from which a JSON schema is to be read.
     * @return newly created instance of JSON schema reader.
     * @throws NullPointerException if {@code reader} is {@code null}.
     */
    public abstract JsonSchemaReader createSchemaReader(Reader reader);

    public abstract JsonSchemaBuilderFactory createSchemaBuilderFactory();
    
    /**
     * Creates a new instance of JSON validator factory.
     * 
     * @param schema the JSON schema to apply when validating JSON instances.
     * @return the newly created instance of JSON validator factory.
     * @throws NullPointerException if the specified parameter was {@code null}.
     */
    public abstract JsonValidatorFactory createValidatorFactory(JsonSchema schema);

    /**
     * Initializes this provider immediately after its instantiation.  
     * 
     * @param jsonProvider the JSON provider to attach.
     */
    protected abstract void initialize(JsonProvider jsonProvider);
    
    /**
     * Creates an instance of this provider class for each thread.
     * 
     * @return the instance of this provider class.
     */
    private static JsonValidationServiceProvider createProvider() {
        JsonProvider jsonProvider = JsonProvider.provider();
        ServiceLoader<JsonValidationServiceProvider> loader = ServiceLoader.load(JsonValidationServiceProvider.class);
        Iterator<JsonValidationServiceProvider> it = loader.iterator();
        if (it.hasNext()) {
            JsonValidationServiceProvider found = it.next();
            found.initialize(jsonProvider);
            return found;
        } else {
            return null;
        }
    }
}
