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
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.json.JsonValue;

/**
 * The type for building a JSON schema programmatically.
 * 
 * <p>
 * Instances of this type can be created by the factory class {@link JsonSchemaBuilderFactory}.
 * </p> 
 * <p>
 * The following example shows how to build a JSON schema with this builder.
 * </p>
 * <pre><code>
 * JsonSchemaBuilderFactory factory = Jsonv.createSchemaBuilder();
 * JsonSchemaBuilder builder = factory.createBuilder();
 * JsonSchema schema = builder
 *         .withType(InstanceType.OBJECT)
 *         .withProperty("firstName", 
 *             factory.createBuilder().withType(InstanceType.STRING).build())
 *         .withProperty("lastName", 
 *             factory.createBuilder().withType(InstanceType.STRING).build())
 *         .withProperty("age", 
 *             factory.createBuilder()
 *                 .withType(InstanceType.INTEGER)
 *                 .withMinimum(0)
 *                 .build())
 *         .withRequired("firstName", "lastName")
 *         .build();
 * </code></pre>
 * 
 * <p>
 * For more information about the keywords composing the JSON schema,
 * please see <a href="http://json-schema.org/">JSON Schema Specification</a>.
 * </p>
 * 
 * <p>Each instance of this type is NOT safe for use by multiple concurrent threads.</p>
 * 
 * @author leadpony
 * @see <a href="http://json-schema.org/">JSON Schema Specification</a>
 */
public interface JsonSchemaBuilder {
    
    /**
     * Builds a new instance of {@link JsonSchema}.
     * 
     * @return newly created instance of {@link JsonSchema}, never be {@code null}.
     */
    JsonSchema build();
    
    /**
     * Adds the "$id" keyword to the schema.
     * 
     * @param id the identifier of the schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code id} is {@code null}.
     */
    JsonSchemaBuilder withId(URI id);
    
    /**
     * Adds the "$schema" keyword to the schema.
     * <p>
     * The "$schema" keyword should be used in a root schema.
     * It must not appear in subschemas.
     * </p>
     * 
     * @param schema the version identifier of the schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code schema} is {@code null}.
     */
    JsonSchemaBuilder withSchema(URI schema);

    /* Validation Keywords for Any Instance Type */
    
    /**
     * Adds the "type" keyword to the schema.
     * The type are specified as an array.
     * 
     * @param types the array of types. 
     *              At least one element is needed and elements must be unique.
     * @return this builder.
     * @throws NullPointerException if the specified {@code types} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code types} is empty,
     *                                  or some types are not unique.
     */
    JsonSchemaBuilder withType(InstanceType... types);

    /**
     * Adds the "type" keyword to the schema.
     * The type are specified as a set.
     * 
     * @param types the set of types. At least one element is needed.
     * @return this builder.
     * @throws NullPointerException if the specified {@code types} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code types} is empty.
     */
    JsonSchemaBuilder withType(Set<InstanceType> types);

    /**
     * Adds the "enum" keyword to the schema.
     * The values are specified as an array.
     * 
     * @param values the values in the enumeration. 
     *               At least one element is needed and elements must be unique.
     * @return this builder.
     * @throws NullPointerException if the specified {@code values} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code values} is empty.
     *                                  or some values are not unique.
     */
    JsonSchemaBuilder withEnum(JsonValue... values);
    
    /**
     * Adds the "enum" keyword to the schema.
     * The values are specified as a set.
     * 
     * @param values the values in the enumeration. At least one element is needed.
     * @return this builder.
     * @throws NullPointerException if the specified {@code values} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code values} is empty.
     */
    JsonSchemaBuilder withEnum(Set<JsonValue> values);

    /**
     * Adds the "const" keyword to the schema.
     * 
     * @param value the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    JsonSchemaBuilder withConst(JsonValue value);
    
    /* Validation Keywords for Numeric Instances (number and integer) */

    /**
     * Adds the "multipleOf" keyword to the schema.
     * The value is specified as long type.
     * 
     * @param value the value of the keyword. This must be greater than 0.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is not greater than 0.
     */
    JsonSchemaBuilder withMultipleOf(long value);

    /**
     * Adds the "multipleOf" keyword to the schema.
     * The value is specified as double type.
     * 
     * @param value the value of the keyword. This must be greater than 0.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is not greater than 0.
     */
    JsonSchemaBuilder withMultipleOf(double value);

    /**
     * Adds the "multipleOf" keyword to the schema.
     * The value is specified as {@link BigDecimal} type.
     * 
     * @param value the value of the keyword. This must be greater than 0.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code value} is not greater than 0.
     */
    JsonSchemaBuilder withMultipleOf(BigDecimal value);
    
    /**
     * Adds the "maximum" keyword to the schema.
     * The value is specified as long type.
     * <p>
     * This keyword specifies an inclusive upper limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     */
    default JsonSchemaBuilder withMaximum(long value) {
        return withMaximum(BigDecimal.valueOf(value));
    }

    /**
     * Adds the "maximum" keyword to the schema.
     * The value is specified as double type.
     * <p>
     * This keyword specifies an inclusive upper limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     */
    default JsonSchemaBuilder withMaximum(double value) {
        return withMaximum(BigDecimal.valueOf(value));
    }

    /**
     * Adds the "maximum" keyword to the schema.
     * The value is specified as {@link BigDecimal} type.
     * <p>
     * This keyword specifies an inclusive upper limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    JsonSchemaBuilder withMaximum(BigDecimal value);

    /**
     * Adds the "exclusiveMaximum" keyword to the schema.
     * The value is specified as long type.
     * <p>
     * This keyword specifies an exclusive upper limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     */
    default JsonSchemaBuilder withExclusiveMaximum(long value) {
        return withExclusiveMaximum(BigDecimal.valueOf(value));
    }

    /**
     * Adds the "exclusiveMaximum" keyword to the schema.
     * The value is specified as double type.
     * <p>
     * This keyword specifies an exclusive upper limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     */
    default JsonSchemaBuilder withExclusiveMaximum(double value) {
        return withExclusiveMaximum(BigDecimal.valueOf(value));
    }

    /**
     * Adds the "exclusiveMaximum" keyword to the schema.
     * The value is specified as {@link BigDecimal} type.
     * <p>
     * This keyword specifies an exclusive upper limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    JsonSchemaBuilder withExclusiveMaximum(BigDecimal value);

    /**
     * Adds the "minimum" keyword to the schema.
     * The value is specified as long type.
     * <p>
     * This keyword specifies an inclusive lower limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     */
    default JsonSchemaBuilder withMinimum(long value) {
        return withMinimum(BigDecimal.valueOf(value));
    }

    /**
     * Adds the "minimum" keyword to the schema.
     * The value is specified as double type.
     * <p>
     * This keyword specifies an inclusive lower limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     */
    default JsonSchemaBuilder withMinimum(double value) {
        return withMinimum(BigDecimal.valueOf(value));
    }
    
    /**
     * Adds the "minimum" keyword to the schema.
     * The value is specified as {@link BigDecimal} type.
     * <p>
     * This keyword specifies an inclusive lower limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    JsonSchemaBuilder withMinimum(BigDecimal value);
    
    /**
     * Adds the "exclusiveMinimum" keyword to the schema.
     * The value is specified as long type.
     * <p>
     * This keyword specifies an exclusive lower limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     */
    default JsonSchemaBuilder withExclusiveMinimum(long value) {
        return withExclusiveMinimum(BigDecimal.valueOf(value));
    }

    /**
     * Adds the "exclusiveMinimum" keyword to the schema.
     * The value is specified as double type.
     * <p>
     * This keyword specifies an exclusive lower limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     */
    default JsonSchemaBuilder withExclusiveMinimum(double value) {
        return withExclusiveMinimum(BigDecimal.valueOf(value));
    }
    
    /**
     * Adds the "exclusiveMinimum" keyword to the schema.
     * The value is specified as {@link BigDecimal} type.
     * <p>
     * This keyword specifies an exclusive lower limit for a numeric instance.
     * </p>
     * 
     * @param value the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    JsonSchemaBuilder withExclusiveMinimum(BigDecimal value);
    
    /* Validation Keywords for Strings */
    
    /**
     * Adds the "maxLength" keyword to the schema.
     * <p>
     * This keyword specifies an upper limit of length for a string instance.
     * </p>
     * 
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMaxLength(int value);

    /**
     * Adds the "minLength" keyword to the schema.
     * <p>
     * This keyword specifies a lower limit of length for a string instance.
     * </p>
     * 
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMinLength(int value);

    /**
     * Adds the "pattern" keyword to the schema.
     * <p>
     * This keyword specifies the pattern of string instance as a regular expression.
     * </p>
     * 
     * @param pattern the regular expression which will be tested against a string instance.
     * @return this builder.
     * @throws NullPointerException if the specified {@code pattern} is {@code null}.
     * @throws PatternSyntaxException if the specified {@code pattern} is not a valid regular expression.
     */
    JsonSchemaBuilder withPattern(String pattern);
    
    /* Validation Keywords for Arrays */
   
    /**
     * Adds the "items" keyword to the schema.
     * The specified single subschema is used for all array items.
     * 
     * @param subschema the subschema as the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withItem(JsonSchema subschema);

    /**
     * Adds the "items" keyword to the schema.
     * The value is specified as an ordered list of subschemas.
     * 
     * @param subschemas the list of subschemas as the value of the keyword. 
     *                   At least one element is needed.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschemas} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is empty.
     */
    JsonSchemaBuilder withItems(List<JsonSchema> subschemas);
    
    /**
     * Adds the "additionalItems" keyword to the schema.
     * 
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withAdditionalItems(JsonSchema subschema);

    /**
     * Adds the "maxItems" keyword to the schema.
     * 
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMaxItems(int value);

    /**
     * Adds the "minItems" keyword to the schema.
     * 
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMinItems(int value);
    
    /**
     * Adds the "uniqueItems" keyword to the schema.
     * <p>
     * This keyword specifies whether elements in the array should be unique or not.
     * </p>
     * 
     * @param unique the value of the keyword.
     * @return this builder.
     */
    JsonSchemaBuilder withUniqueItems(boolean unique);
    
    /**
     * Adds the "contains" keyword to the schema.
     * 
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withContains(JsonSchema subschema);
    
    /**
     * Adds the "maxContains" keyword to the schema.
     * 
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     * @since draft-08
     */
    JsonSchemaBuilder withMaxContains(int value);

    /**
     * Adds the "minContains" keyword to the schema.
     * 
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     * @since draft-08
     */
    JsonSchemaBuilder withMinContains(int value);

    /* Validation Keywords for Objects */
    
    /**
     * Adds the "maxProperties" keyword to the schema.
     * 
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMaxProperties(int value);

    /**
     * Adds the "minProperties" keyword to the schema.
     * 
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMinProperties(int value);
    
    /**
     * Adds the "required" keyword to the schema.
     * <p>
     * This keyword specifies the required properties in an object.
     * </p>
     * 
     * @param names the value of the keyword. The names must be unique.
     * @return this builder.
     * @throws NullPointerException if the specified {@code names} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code values} are not unique.
     */
    JsonSchemaBuilder withRequired(String... names);

    /**
     * Adds the "required" keyword to the schema.
     * <p>
     * This keyword specifies the required properties in an object.
     * </p>
     * 
     * @param names the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code names} is {@code null}.
     */
    JsonSchemaBuilder withRequired(Set<String> names);

    /**
     * Adds an entry of the "properties" keyword to the schema.
     * 
     * @param name the name of the property.
     * @param subschema the subschema for the property.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} or {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withProperty(String name, JsonSchema subschema);
    
    /**
     * Adds an entry of the "patternProperties" keyword to the schema.
     * 
     * @param pattern the name pattern the property.
     * @param subschema the subschema for the property.
     * @return this builder.
     * @throws NullPointerException if the specified {@code pattern} or {@code subschema} is {@code null}.
     * @throws PatternSyntaxException if the specified {@code pattern} is not a valid regular expression.
     */
    JsonSchemaBuilder withPatternProperty(String pattern, JsonSchema subschema);

    /**
     * Adds the "additionalProperties" keyword to the schema.
     * 
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withAdditionalProperties(JsonSchema subschema);
   
    /**
     * Adds an entry of the "dependencies" keyword to the schema.
     * 
     * @param name the name of the dependency property.
     * @param subschema the schema to be evaluated against the entire object.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} or {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withDependency(String name, JsonSchema subschema);
    
    /**
     * Adds an entry of the "dependencies" keyword to the schema.
     * 
     * @param name the name of the dependency property.
     * @param requiredProperties the required properties in the object.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} or {@code requiredProperties} is {@code null}.
     */
    JsonSchemaBuilder withDependency(String name, Set<String> requiredProperties);

    /**
     * Adds the "propertyNames" keyword to the schema.
     * 
     * @param subschema the subschema to be evaluated against all property names in an object.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withPropertyNames(JsonSchema subschema);
    
    /* Keywords for Applying Subschemas Conditionally */

    /**
     * Adds the "if" keyword to the schema.
     * 
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withIf(JsonSchema subschema);

    /**
     * Adds the "then" keyword to the schema.
     * <p>
     * This keyword has no effect when "if" is absent.
     * </p>
     * 
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withThen(JsonSchema subschema);

    /**
     * Adds the "else" keyword to the schema.
     * <p>
     * This keyword has no effect when "if" is absent.
     * </p>
     * 
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withElse(JsonSchema subschema);
    
    /* Keywords for Applying Subschemas With Boolean Logic */
    
    /**
     * Adds the "allOf" keyword to the schema.
     * The value is specifies as an array.
     * 
     * @param subschemas the array of the subschemas.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschemas} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is empty.
     */
    JsonSchemaBuilder withAllOf(JsonSchema... subschemas);

    /**
     * Adds the "allOf" keyword to the schema.
     * The value is specifies as a list.
     * 
     * @param subschemas the list of the subschemas.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschemas} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is empty.
     */
    JsonSchemaBuilder withAllOf(List<JsonSchema> subschemas);

    /**
     * Adds the "anyOf" keyword to the schema.
     * The value is specifies as an array.
     * 
     * @param subschemas the array of the subschemas.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschemas} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is empty.
     */
    JsonSchemaBuilder withAnyOf(JsonSchema... subschemas);

    /**
     * Adds the "anyOf" keyword to the schema.
     * The value is specifies as a list.
     * 
     * @param subschemas the list of the subschemas.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschemas} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is empty.
     */
    JsonSchemaBuilder withAnyOf(List<JsonSchema> subschemas);

    /**
     * Adds the "oneOf" keyword to the schema.
     * The value is specifies as an array.
     * 
     * @param subschemas the array of the subschemas.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschemas} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is empty.
     */
    JsonSchemaBuilder withOneOf(JsonSchema... subschemas);

    /**
     * Adds the "oneOf" keyword to the schema.
     * The value is specifies as a list.
     * 
     * @param subschemas the list of the subschemas.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschemas} is {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is empty.
     */
    JsonSchemaBuilder withOneOf(List<JsonSchema> subschemas);

    /**
     * Adds the "not" keyword to the schema.
     * 
     * @param subschema the subschema to be negated.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withNot(JsonSchema subschema);
    
    /**
     * Adds an entry of "definitions" keyword to the schema.
     * 
     * @param name the name of the definition to be added.
     * @param schema the schema to be defined.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} or {@code schema} is {@code null}.
     */
    JsonSchemaBuilder withDefinition(String name, JsonSchema schema);
    
    /* Keywords for annotation */
    
    /**
     * Adds the "title" keyword to the schema.
     * 
     * @param title the title of the schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code title} is {@code null}.
     */
    JsonSchemaBuilder withTitle(String title);
    
    /**
     * Adds the "description" keyword to the schema.
     * 
     * @param description the description of the schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code description} is {@code null}.
     */
    JsonSchemaBuilder withDescription(String description);
    
    /**
     * Adds the "default" keyword to the schema.
     * 
     * @param value the default value.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    JsonSchemaBuilder withDefault(JsonValue value);
}
