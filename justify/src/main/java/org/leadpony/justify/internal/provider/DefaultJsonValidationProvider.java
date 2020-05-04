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

package org.leadpony.justify.internal.provider;

import jakarta.json.spi.JsonProvider;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.spi.JsonValidationProvider;

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
    public JsonValidationService createService() {
        return createService(JsonProvider.provider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonValidationService createService(JsonProvider jsonProvider) {
        requireNonNull(jsonProvider, "jsonProvider");
        return new DefaultJsonValidationService(jsonProvider);
    }
}
