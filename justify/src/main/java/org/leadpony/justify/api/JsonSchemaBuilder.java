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

package org.leadpony.justify.api;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import jakarta.json.JsonValue;

/**
 * A builder interface for building a JSON schema programmatically.
 *
 * <p>
 * Instances of this type can be created by the factory class
 * {@link JsonSchemaBuilderFactory}.
 * </p>
 * <p>
 * The following code sample shows how to build a JSON schema using this
 * builder.
 * </p>
 *
 * <pre>
 * <code>
 * JsonValidationService service = JsonValidationService.newInstance();
 * JsonSchemaBuilderFactory factory = service.createSchemaBuilderFactory();
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
 * </code>
 * </pre>
 *
 * <p>
 * For more information about the keywords composing the JSON schema, please see
 * <a href="http://json-schema.org/">JSON Schema Specification</a>.
 * </p>
 *
 * <p>
 * Each instance of this type is NOT safe for use by multiple concurrent
 * threads.
 * </p>
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
     * Adds a "$id" keyword to the schema.
     *
     * @param id the identifier of the schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code id} is {@code null}.
     */
    JsonSchemaBuilder withId(URI id);

    /**
     * Adds a "$schema" keyword to the schema.
     * <p>
     * The "$schema" keyword should be used in a root schema. It must not appear in
     * subschemas.
     * </p>
     *
     * @param schema the version identifier of the schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code schema} is {@code null}.
     */
    JsonSchemaBuilder withSchema(URI schema);

    /**
     * Adds a "$comment" keyword to the schema.
     *
     * @param comment the comment for the schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code comment} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withComment(String comment);

    /* Validation Keywords for Any Instance Type */

    /**
     * Adds a "type" keyword to the schema. The type are specified as an array.
     *
     * @param types the array of types. At least one element is needed and elements
     *              must be unique.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code types} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code types} is empty, or
     *                                  some types are not unique.
     */
    JsonSchemaBuilder withType(InstanceType... types);

    /**
     * Adds a "type" keyword to the schema. The type are specified as a set.
     *
     * @param types the set of types. At least one element is needed.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code types} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code types} is empty.
     */
    JsonSchemaBuilder withType(Set<InstanceType> types);

    /**
     * Adds an "enum" keyword to the schema. The values are specified as an array.
     *
     * @param values the values in the enumeration. At least one element is needed
     *               and elements must be unique.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code values} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code values} is empty. or
     *                                  some values are not unique.
     */
    JsonSchemaBuilder withEnum(JsonValue... values);

    /**
     * Adds an "enum" keyword to the schema. The values are specified as a set.
     *
     * @param values the values in the enumeration. At least one element is needed.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code values} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code values} is empty.
     */
    JsonSchemaBuilder withEnum(Set<JsonValue> values);

    /**
     * Adds a "const" keyword to the schema.
     *
     * @param value the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    JsonSchemaBuilder withConst(JsonValue value);

    /* Validation Keywords for Numeric Instances (number and integer) */

    /**
     * Adds a "multipleOf" keyword to the schema. The value is specified as long
     * type.
     *
     * @param value the value of the keyword. This must be greater than 0.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is not
     *                                  greater than 0.
     */
    JsonSchemaBuilder withMultipleOf(long value);

    /**
     * Adds a "multipleOf" keyword to the schema. The value is specified as double
     * type.
     *
     * @param value the value of the keyword. This must be greater than 0.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is not
     *                                  greater than 0.
     */
    JsonSchemaBuilder withMultipleOf(double value);

    /**
     * Adds a "multipleOf" keyword to the schema. The value is specified as
     * {@link BigDecimal} type.
     *
     * @param value the value of the keyword. This must be greater than 0.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code value} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code value} is not
     *                                  greater than 0.
     */
    JsonSchemaBuilder withMultipleOf(BigDecimal value);

    /**
     * Adds a "maximum" keyword to the schema. The value is specified as long type.
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
     * Adds a "maximum" keyword to the schema. The value is specified as double
     * type.
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
     * Adds a "maximum" keyword to the schema. The value is specified as
     * {@link BigDecimal} type.
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
     * Adds an "exclusiveMaximum" keyword to the schema. The value is specified as
     * long type.
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
     * Adds an "exclusiveMaximum" keyword to the schema. The value is specified as
     * double type.
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
     * Adds an "exclusiveMaximum" keyword to the schema. The value is specified as
     * {@link BigDecimal} type.
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
     * Adds a "minimum" keyword to the schema. The value is specified as long type.
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
     * Adds a "minimum" keyword to the schema. The value is specified as double
     * type.
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
     * Adds a "minimum" keyword to the schema. The value is specified as
     * {@link BigDecimal} type.
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
     * Adds an "exclusiveMinimum" keyword to the schema. The value is specified as
     * long type.
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
     * Adds an "exclusiveMinimum" keyword to the schema. The value is specified as
     * double type.
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
     * Adds an "exclusiveMinimum" keyword to the schema. The value is specified as
     * {@link BigDecimal} type.
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
     * Adds a "maxLength" keyword to the schema.
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
     * Adds a "minLength" keyword to the schema.
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
     * Adds a "pattern" keyword to the schema.
     * <p>
     * This keyword specifies the pattern of string instance as a regular
     * expression.
     * </p>
     *
     * @param pattern the regular expression which will be tested against a string
     *                instance.
     * @return this builder.
     * @throws NullPointerException   if the specified {@code pattern} is
     *                                {@code null}.
     * @throws PatternSyntaxException if the specified {@code pattern} is not a
     *                                valid regular expression.
     */
    JsonSchemaBuilder withPattern(String pattern);

    /* Validation Keywords for Arrays */

    /**
     * Adds an "items" keyword to the schema. The specified single subschema is used
     * for all array items.
     *
     * @param subschema the subschema as the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withItems(JsonSchema subschema);

    /**
     * Adds an "items" keyword to the schema. The value is specified as an array of
     * subschemas.
     *
     * @param subschemas the array of subschemas as the value of the keyword. At
     *                   least one element is needed.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code subschemas} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is
     *                                  empty.
     */
    JsonSchemaBuilder withItemsArray(JsonSchema... subschemas);

    /**
     * Adds an "items" keyword to the schema. The value is specified as an ordered
     * list of subschemas.
     *
     * @param subschemas the list of subschemas as the value of the keyword. At
     *                   least one element is needed.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code subschemas} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is
     *                                  empty.
     */
    JsonSchemaBuilder withItemsArray(List<JsonSchema> subschemas);

    /**
     * Adds an "additionalItems" keyword to the schema.
     *
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withAdditionalItems(JsonSchema subschema);

    /**
     * Adds a "maxItems" keyword to the schema.
     *
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMaxItems(int value);

    /**
     * Adds a "minItems" keyword to the schema.
     *
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMinItems(int value);

    /**
     * Adds a "uniqueItems" keyword to the schema.
     * <p>
     * This keyword specifies whether elements in the array should be unique or not.
     * </p>
     *
     * @param unique the value of the keyword.
     * @return this builder.
     */
    JsonSchemaBuilder withUniqueItems(boolean unique);

    /**
     * Adds a "contains" keyword to the schema.
     *
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withContains(JsonSchema subschema);

    /**
     * Adds a "maxContains" keyword to the schema.
     *
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     * @since draft-08
     */
    JsonSchemaBuilder withMaxContains(int value);

    /**
     * Adds a "minContains" keyword to the schema.
     *
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     * @since draft-08
     */
    JsonSchemaBuilder withMinContains(int value);

    /* Validation Keywords for Objects */

    /**
     * Adds a "maxProperties" keyword to the schema.
     *
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMaxProperties(int value);

    /**
     * Adds a "minProperties" keyword to the schema.
     *
     * @param value the value of the keyword. This must be a non-negative integer.
     * @return this builder.
     * @throws IllegalArgumentException if the specified {@code value} is negative.
     */
    JsonSchemaBuilder withMinProperties(int value);

    /**
     * Adds a "required" keyword to the schema.
     * <p>
     * This keyword specifies the required properties in an object.
     * </p>
     *
     * @param names the value of the keyword. The names must be unique.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code names} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code values} are not
     *                                  unique.
     */
    JsonSchemaBuilder withRequired(String... names);

    /**
     * Adds a "required" keyword to the schema.
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
     * @param name      the name of the property.
     * @param subschema the subschema for the property.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} or
     *                              {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withProperty(String name, JsonSchema subschema);

    /**
     * Adds a "properties" keyword to the schema.
     *
     * @param subschemas the object mapping a property name to a subschema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschemas} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withProperties(Map<String, JsonSchema> subschemas);

    /**
     * Adds an entry of the "patternProperties" keyword to the schema.
     *
     * @param pattern   the name pattern the property.
     * @param subschema the subschema for the property.
     * @return this builder.
     * @throws NullPointerException   if the specified {@code pattern} or
     *                                {@code subschema} is {@code null}.
     * @throws PatternSyntaxException if the specified {@code pattern} is not a
     *                                valid regular expression.
     */
    JsonSchemaBuilder withPatternProperty(String pattern, JsonSchema subschema);

    /**
     * Adds a "patternProperties" keyword to the schema.
     *
     * @param subschemas the object mapping a name pattern to a subschema.
     * @return this builder.
     * @throws NullPointerException   if the specified {@code subschemas} is
     *                                {@code null}.
     * @throws PatternSyntaxException if any pattern is not a valid regular
     *                                expression.
     */
    JsonSchemaBuilder withPatternProperties(Map<String, JsonSchema> subschemas);

    /**
     * Adds an "additionalProperties" keyword to the schema.
     *
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withAdditionalProperties(JsonSchema subschema);

    /**
     * Adds an entry of the "dependencies" keyword to the schema.
     *
     * @param name      the name of the dependency property.
     * @param subschema the schema to be evaluated against the entire object.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} or
     *                              {@code subschema} is {@code null}.
     */
    JsonSchemaBuilder withDependency(String name, JsonSchema subschema);

    /**
     * Adds an entry of the "dependencies" keyword to the schema.
     *
     * @param name               the name of the dependency property.
     * @param requiredProperties the required properties in the object.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code name} or
     *                                  {@code requiredProperties} is {@code null}.
     * @throws IllegalArgumentException if any element in {@code requiredProperties}
     *                                  is not unique.
     */
    JsonSchemaBuilder withDependency(String name, String... requiredProperties);

    /**
     * Adds an entry of the "dependencies" keyword to the schema.
     *
     * @param name               the name of the dependency property.
     * @param requiredProperties the required properties in the object.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} or
     *                              {@code requiredProperties} is {@code null}.
     */
    JsonSchemaBuilder withDependency(String name, Set<String> requiredProperties);

    /**
     * Adds a "dependencies" keyword to the schema.
     *
     * @param values the object mapping a property name to a value. Each value of
     *               the map must be a {@link JsonSchema} or a {@code Set<String>}.
     * @return this builder.
     * @throws NullPointerException if the specified {@code values} is {@code null}.
     * @throws ClassCastException if any value of the map is of unexpected type.
     */
    JsonSchemaBuilder withDependencies(Map<String, ?> values);

    /**
     * Adds a "propertyNames" keyword to the schema.
     *
     * @param subschema the subschema to be evaluated against all property names in
     *                  an object.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withPropertyNames(JsonSchema subschema);

    /* Keywords for Applying Subschemas Conditionally */

    /**
     * Adds an "if" keyword to the schema.
     *
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withIf(JsonSchema subschema);

    /**
     * Adds a "then" keyword to the schema.
     * <p>
     * This keyword has no effect when "if" is absent.
     * </p>
     *
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withThen(JsonSchema subschema);

    /**
     * Adds an "else" keyword to the schema.
     * <p>
     * This keyword has no effect when "if" is absent.
     * </p>
     *
     * @param subschema the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withElse(JsonSchema subschema);

    /* Keywords for Applying Subschemas With Boolean Logic */

    /**
     * Adds an "allOf" keyword to the schema. The value is specifies as an array.
     *
     * @param subschemas the array of the subschemas.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code subschemas} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is
     *                                  empty.
     */
    JsonSchemaBuilder withAllOf(JsonSchema... subschemas);

    /**
     * Adds an "allOf" keyword to the schema. The value is specifies as a list.
     *
     * @param subschemas the list of the subschemas.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code subschemas} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is
     *                                  empty.
     */
    JsonSchemaBuilder withAllOf(List<JsonSchema> subschemas);

    /**
     * Adds an "anyOf" keyword to the schema. The value is specifies as an array.
     *
     * @param subschemas the array of the subschemas.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code subschemas} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is
     *                                  empty.
     */
    JsonSchemaBuilder withAnyOf(JsonSchema... subschemas);

    /**
     * Adds an "anyOf" keyword to the schema. The value is specifies as a list.
     *
     * @param subschemas the list of the subschemas.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code subschemas} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is
     *                                  empty.
     */
    JsonSchemaBuilder withAnyOf(List<JsonSchema> subschemas);

    /**
     * Adds a "oneOf" keyword to the schema. The value is specifies as an array.
     *
     * @param subschemas the array of the subschemas.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code subschemas} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is
     *                                  empty.
     */
    JsonSchemaBuilder withOneOf(JsonSchema... subschemas);

    /**
     * Adds a "oneOf" keyword to the schema. The value is specifies as a list.
     *
     * @param subschemas the list of the subschemas.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code subschemas} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code subschemas} is
     *                                  empty.
     */
    JsonSchemaBuilder withOneOf(List<JsonSchema> subschemas);

    /**
     * Adds a "not" keyword to the schema.
     *
     * @param subschema the subschema to be negated.
     * @return this builder.
     * @throws NullPointerException if the specified {@code subschema} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withNot(JsonSchema subschema);

    /**
     * Adds a "format" keyword to the schema. This method throws an exception if the
     * specified {@code attribute} is not recognized as a formate attribute.
     *
     * @param attribute the format attribute such as "date-time".
     * @return this builder.
     * @throws NullPointerException     if the specified {@code attribute} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code attribute} is not
     *                                  recogznied as a format attribute.
     * @see #withLaxFormat
     */
    JsonSchemaBuilder withFormat(String attribute);

    /**
     * Adds a "format" keyword to the schema. This method does not throw an
     * exception even if the specified {@code attribute} is not recognized as a
     * formate attribute.
     *
     * @param attribute the format attribute such as "date-time".
     * @return this builder.
     * @throws NullPointerException if the specified {@code attribute} is
     *                              {@code null}.
     * @see #withFormat
     */
    JsonSchemaBuilder withLaxFormat(String attribute);

    /**
     * Adds a "contentEncoding" keyword to the schema.
     *
     * @param value the value of the keyword.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    JsonSchemaBuilder withContentEncoding(String value);

    /**
     * Adds a "contentMediaType" keyword to the schema.
     *
     * @param value the value of the keyword.
     * @return this builder.
     * @throws NullPointerException     if the specified {@code value} is
     *                                  {@code null}.
     * @throws IllegalArgumentException if the specified {@code value} is not a
     *                                  media type.
     */
    JsonSchemaBuilder withContentMediaType(String value);

    /**
     * Adds an entry of "definitions" keyword to the schema.
     *
     * @param name   the name of the definition to be added.
     * @param schema the schema to define.
     * @return this builder.
     * @throws NullPointerException if the specified {@code name} or {@code schema}
     *                              is {@code null}.
     */
    JsonSchemaBuilder withDefinition(String name, JsonSchema schema);

    /**
     * Adds a "definitions" keyword to the schema.
     *
     * @param schemas the object mapping a name to a schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code schemas} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withDefinitions(Map<String, JsonSchema> schemas);

    /* Keywords for annotation */

    /**
     * Adds a "title" keyword to the schema.
     *
     * @param title the title of the schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code title} is {@code null}.
     */
    JsonSchemaBuilder withTitle(String title);

    /**
     * Adds a "description" keyword to the schema.
     *
     * @param description the description of the schema.
     * @return this builder.
     * @throws NullPointerException if the specified {@code description} is
     *                              {@code null}.
     */
    JsonSchemaBuilder withDescription(String description);

    /**
     * Adds a "default" keyword to the schema.
     *
     * @param value the default value.
     * @return this builder.
     * @throws NullPointerException if the specified {@code value} is {@code null}.
     */
    JsonSchemaBuilder withDefault(JsonValue value);
}
