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

package org.leadpony.justify.internal.schema;

import javax.json.JsonBuilderFactory;

import org.leadpony.justify.core.JsonSchemaBuilderFactory;
import org.leadpony.justify.internal.keyword.assertion.format.FormatAttributeRegistry;

/**
 * The default implementation of {@link JsonSchemaBuilderFactory}.
 * 
 * @author leadpony
 */
public class BasicSchemaBuilderFactory implements JsonSchemaBuilderFactory {
    
    private final JsonBuilderFactory builderFactory;
    private final FormatAttributeRegistry formatRegistry;
    
    /**
     * Constructs this factory.
     * 
     * @param builderFactory the factory for producing builders of JSON values.
     */
    public BasicSchemaBuilderFactory(JsonBuilderFactory builderFactory, FormatAttributeRegistry formatRegistry) {
        this.builderFactory = builderFactory;
        this.formatRegistry = formatRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultSchemaBuilder createBuilder() {
        return new DefaultSchemaBuilder(builderFactory, formatRegistry);
    }
}
