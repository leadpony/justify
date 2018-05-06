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
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;
import org.leadpony.justify.core.JsonSchemaException;
import org.leadpony.justify.core.JsonValidatorFactory;

/**
 * Service provider for JSON validation objects. 
 * 
 * <p>All the methods in this class are safe for use by multiple concurrent threads.</p>
 * 
 * @author leadpony
 * @see ServiceLoader
 */
public abstract class JsonSchemaProvider {
    
    private static final ThreadLocal<JsonSchemaProvider> threadLocalProvider =
            ThreadLocal.withInitial(JsonSchemaProvider::createThreadLocalProvider);
    
    /**
     * Returns an instance of JSON schema provider.
     * 
     * @return the instance of JSON schema provider.
     */
    public static JsonSchemaProvider provider() {
        JsonSchemaProvider provider = threadLocalProvider.get();
        if (provider == null) {
            throw new JsonSchemaException("JSON schema provider is not installed.");
        }
        return provider;
    }
    
    /**
     * Constructs this provider.
     */
    protected JsonSchemaProvider() {
    }
    
    /**
     * Attaches JSON provider to this provider. 
     * 
     * @param jsonProvider JSON provider to attach.
     * @return this provider.
     */
    public abstract JsonSchemaProvider withProvider(JsonProvider jsonProvider);
    
    public abstract JsonSchema createBooleanSchema(boolean value);
    
    public abstract JsonSchema loadSchema(InputStream in);

    public abstract JsonSchema loadSchema(Reader reader);
    
    public abstract JsonSchemaBuilderFactory createSchemaBuilderFactory();
    
    /**
     * Creates a new instance of JSON validator factory.
     * 
     * @param schema the JSON schema to apply when validating JSON instances.
     * @return the newly created instance of JSON validator factory.
     * @throws NullPointerException if the specified parameter was {@code null}.
     */
    public abstract JsonValidatorFactory createValidatorFactory(JsonSchema schema);

    private static JsonSchemaProvider createThreadLocalProvider() {
        JsonProvider jsonProvider = JsonProvider.provider();
        ServiceLoader<JsonSchemaProvider> loader = ServiceLoader.load(JsonSchemaProvider.class);
        Iterator<JsonSchemaProvider> it = loader.iterator();
        if (it.hasNext()) {
            return it.next().withProvider(jsonProvider);
        } else {
            return null;
        }
    }
}
