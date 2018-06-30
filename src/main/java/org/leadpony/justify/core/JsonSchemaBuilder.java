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
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.json.JsonValue;

/**
 * Builder of JSON schema.
 * 
 * <p>Each instance of this type is NOT safe for use by multiple concurrent threads.</p>
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
    
    JsonSchemaBuilder withId(URI id);
    
    JsonSchemaBuilder withSchema(URI schema);

    JsonSchemaBuilder withTitle(String title);
    
    JsonSchemaBuilder withDescription(String description);
    
    /* Validation Keywords for Any Instance Type */
    
    JsonSchemaBuilder withType(InstanceType... types);

    JsonSchemaBuilder withType(Set<InstanceType> types);

    JsonSchemaBuilder withEnum(JsonValue... values);
    
    JsonSchemaBuilder withEnum(Set<JsonValue> values);

    JsonSchemaBuilder withConst(JsonValue value);
    
    /* Validation Keywords for Numeric Instances (number and integer) */

    default JsonSchemaBuilder withMultipleOf(long divisor) {
        return withMultipleOf(BigDecimal.valueOf(divisor));
    }

    default JsonSchemaBuilder withMultipleOf(double divisor) {
        return withMultipleOf(BigDecimal.valueOf(divisor));
    }

    JsonSchemaBuilder withMultipleOf(BigDecimal divisor);
    
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
    
    /* Validation Keywords for Strings */
    
    JsonSchemaBuilder withMaxLength(int bound);

    JsonSchemaBuilder withMinLength(int bound);

    JsonSchemaBuilder withPattern(String pattern);
    
    /* Validation Keywords for Arrays */
   
    JsonSchemaBuilder withItem(JsonSchema subschema);

    JsonSchemaBuilder withItems(List<JsonSchema> subschemas);
    
    JsonSchemaBuilder withAdditionalItems(JsonSchema subschema);

    JsonSchemaBuilder withMaxItems(int bound);

    JsonSchemaBuilder withMinItems(int bound);
    
    JsonSchemaBuilder withUniqueItems(boolean unique);
    
    /* Validation Keywords for Objects */
    
    JsonSchemaBuilder withMaxProperties(int bound);

    JsonSchemaBuilder withMinProperties(int bound);
    
    JsonSchemaBuilder withRequired(String... names);

    JsonSchemaBuilder withRequired(Set<String> names);

    JsonSchemaBuilder withProperty(String name, JsonSchema subschema);
    
    JsonSchemaBuilder withPatternProperty(String pattern, JsonSchema subschema);

    JsonSchemaBuilder withAdditionalProperties(JsonSchema subschema);
    
    /* Keywords for Applying Subschemas Conditionally */

    JsonSchemaBuilder withIf(JsonSchema subschema);

    JsonSchemaBuilder withThen(JsonSchema subschema);

    JsonSchemaBuilder withElse(JsonSchema subschema);
    
    /* Keywords for Applying Subschemas With Boolean Logic */
    
    JsonSchemaBuilder withAllOf(JsonSchema... subschemas);

    JsonSchemaBuilder withAllOf(Collection<JsonSchema> subschemas);

    JsonSchemaBuilder withAnyOf(JsonSchema... subschemas);

    JsonSchemaBuilder withAnyOf(Collection<JsonSchema> subschemas);

    JsonSchemaBuilder withOneOf(JsonSchema... subschemas);

    JsonSchemaBuilder withOneOf(Collection<JsonSchema> subschemas);

    /**
     * Appends "not" boolean logic schema.
     * 
     * @param subschema the subschema of the "not" schema, cannot be {@code null}.
     * @return this builder.
     * @throws NullPointerException one of parameters was {@code null}.
     */
    JsonSchemaBuilder withNot(JsonSchema subschema);
    
    /**
     * Appends a definition of schema.
     * 
     * @param name the name of the definition.
     * @param schema the schema to define.
     * @return this builder.
     * @throws NullPointerException one of parameters was {@code null}.
     */
    JsonSchemaBuilder withDefinition(String name, JsonSchema schema);
    
    JsonSchemaBuilder withSubschema(String jsonPointer, JsonSchema subschema);
}
