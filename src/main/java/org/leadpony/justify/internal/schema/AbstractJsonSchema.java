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

import org.leadpony.justify.core.JsonSchema;

/**
 * Skeletal implementation of {@link JsonSchema}.
 * 
 * @author leadpony
 */
abstract class AbstractJsonSchema implements JsonSchema {

    private AbstractJsonSchema negated;
    
    @Override
    public JsonSchema negate() {
        if (this.negated == null) {
            this.negated = createNegatedSchema();
            this.negated.negated = this;
        }
        return this.negated;
    }
    
    @Override
    public String toString() {
        return JsonSchemas.toString(this);
    }
    
    protected abstract AbstractJsonSchema createNegatedSchema();
}
