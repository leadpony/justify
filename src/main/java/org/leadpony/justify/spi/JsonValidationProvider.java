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
package org.leadpony.justify.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

import javax.json.JsonException;

import org.leadpony.justify.api.JsonValidationService;

/**
 * Service provider for JSON validation objects. 
 * 
 * <p>
 * All the public methods in this class are safe for use by multiple concurrent threads.
 * This type is not intended to be used directly by end users.
 * </p>
 * 
 * @author leadpony
 * @see ServiceLoader
 */
public abstract class JsonValidationProvider {
    
    /**
     * Returns an instance of this provider class.
     * 
     * @return the instance of this provider class.     
     * @throws JsonException if there is no provider found.
     */
    public static JsonValidationProvider provider() {
        ServiceLoader<JsonValidationProvider> loader = ServiceLoader.load(JsonValidationProvider.class);
        Iterator<JsonValidationProvider> it = loader.iterator();
        if (it.hasNext()) {
            return it.next();
        } else {
            throw new JsonException("JSON validation provider was not found.");
        }
    }
    
    /**
     * Constructs this provider.
     */
    protected JsonValidationProvider() {
    }
    
    /**
     * Creates a new instance of {@link JsonValidationService}.
     * 
     * @return newly created instance of {@link JsonValidationService}.
     * @throws JsonException if an error was encountered while creating the instance.
     */
    public abstract JsonValidationService createService();
}
