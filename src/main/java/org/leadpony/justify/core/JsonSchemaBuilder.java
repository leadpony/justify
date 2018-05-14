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

package org.leadpony.justify.core;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

/**
 * Builder of JSON schemas.
 * 
 * @author leadpony
 */
public interface JsonSchemaBuilder {
    
    /**
     * Builds a new instance of {@link JsonSchema}.
     * 
     * @return newly created instance of {@link JsonSchema}.
     */
    JsonSchema build();
    
    JsonSchemaBuilder withTitle(String title);
    
    JsonSchemaBuilder withDescription(String description);
    
    JsonSchemaBuilder withType(InstanceType... types);

    JsonSchemaBuilder withType(Set<InstanceType> types);

    JsonSchemaBuilder withRequired(String... names);

    JsonSchemaBuilder withRequired(Set<String> names);

    /**
     * Specifies the upper bound for numeric value.
     * 
     * @param bound the upper bound of long type.
     * @return this builder.
     */
    default JsonSchemaBuilder withMaximum(long bound) {
        return withMaximum(BigDecimal.valueOf(bound));
    }

    /**
     * Specifies the upper bound of numeric value.
     * 
     * @param bound the upper bound of double type.
     * @return this builder.
     */
    default JsonSchemaBuilder withMaximum(double bound) {
        return withMaximum(BigDecimal.valueOf(bound));
    }

    /**
     * Specifies the upper bound for numeric value.
     * 
     * @param bound the upper bound of numeric value.
     * @return this builder.
     */
    JsonSchemaBuilder withMaximum(BigDecimal bound);

    /**
     * Specifies the upper bound for numeric value.
     * 
     * @param bound the upper bound of long type.
     * @return this builder.
     */
    default JsonSchemaBuilder withExclusiveMaximum(long bound) {
        return withExclusiveMaximum(BigDecimal.valueOf(bound));
    }

    /**
     * Specifies the upper bound for numeric value.
     * 
     * @param bound the upper bound of double type.
     * @return this builder.
     */
    default JsonSchemaBuilder withExclusiveMaximum(double bound) {
        return withExclusiveMaximum(BigDecimal.valueOf(bound));
    }

    /**
     * Specifies the upper bound for numeric value.
     * 
     * @param bound the upper bound of {@link BigDecimal} type.
     * @return this builder.
     */
    JsonSchemaBuilder withExclusiveMaximum(BigDecimal bound);

    /**
     * Specifies the lower bound for numeric value.
     * 
     * @param bound the lower bound of long type.
     * @return this builder.
     */
    default JsonSchemaBuilder withMinimum(long bound) {
        return withMinimum(BigDecimal.valueOf(bound));
    }

    /**
     * Specifies the lower bound for numeric value.
     * 
     * @param bound the lower bound of double type.
     * @return this builder.
     */
    default JsonSchemaBuilder withMinimum(double bound) {
        return withMinimum(BigDecimal.valueOf(bound));
    }
    
    /**
     * Specifies the lower bound for numeric value.
     * 
     * @param bound the lower bound of {@link BigDecimal} type.
     * @return this builder.
     */
    JsonSchemaBuilder withMinimum(BigDecimal bound);
    
    /**
     * Specifies the lower bound for numeric value.
     * 
     * @param bound the lower bound of long type.
     * @return this builder.
     */
    default JsonSchemaBuilder withExclusiveMinimum(long bound) {
        return withExclusiveMinimum(BigDecimal.valueOf(bound));
    }

    /**
     * Specifies the lower bound for numeric value.
     * 
     * @param bound the lower bound of double type.
     * @return this builder.
     */
    default JsonSchemaBuilder withExclusiveMinimum(double bound) {
        return withExclusiveMinimum(BigDecimal.valueOf(bound));
    }
    
    /**
     * Specifies the lower bound for numeric value.
     * 
     * @param bound the lower bound of {@link BigDecimal} type.
     * @return this builder.
     */
    JsonSchemaBuilder withExclusiveMinimum(BigDecimal bound);
    
    JsonSchemaBuilder withMaxLength(int bound);

    JsonSchemaBuilder withMinLength(int bound);

    JsonSchemaBuilder withProperty(String name, JsonSchema subschema);

    JsonSchemaBuilder withItem(JsonSchema subschema);

    JsonSchemaBuilder withItems(JsonSchema... subschemas);
    
    JsonSchemaBuilder withAllOf(JsonSchema... subschemas);

    JsonSchemaBuilder withAllOf(Collection<JsonSchema> subschemas);

    JsonSchemaBuilder withAnyOf(JsonSchema... subschemas);

    JsonSchemaBuilder withAnyOf(Collection<JsonSchema> subschemas);

    JsonSchemaBuilder withOneOf(JsonSchema... subschemas);

    JsonSchemaBuilder withOneOf(Collection<JsonSchema> subschemas);
}
