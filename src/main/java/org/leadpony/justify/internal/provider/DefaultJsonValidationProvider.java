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

package org.leadpony.justify.internal.provider;

import javax.json.spi.JsonProvider;

import org.leadpony.justify.core.Jsonv;
import org.leadpony.justify.core.spi.JsonValidationProvider;

/**
 * Default implementation of {@link JsonValidationProvider}.
 * 
 * @author leadpony
 */
public class DefaultJsonValidationProvider extends JsonValidationProvider {

    /**
     * Constructs this object.
     */
    public DefaultJsonValidationProvider() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Jsonv createJsonv() {
        JsonProvider provider = JsonProvider.provider();
        return new DefaultJsonv(provider);
    }
}
