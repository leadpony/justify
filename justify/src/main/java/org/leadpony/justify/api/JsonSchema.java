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

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.leadpony.justify.api.keyword.Keyword;

import jakarta.json.JsonException;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * An immutable JSON schema.
 *
 * <p>
 * A JSON schema can be read from {@link java.io.InputStream} or
 * {@link java.io.Reader}. The following example shows how to read a JSON schema
 * from a {@link java.io.StringReader}:
 * </p>
 *
 * <pre>
 * <code>
 * JsonValidationService service = JsonValidationService.newInstance();
 * StringReader reader = new StringReader("{\"type\": \"integer\"}");
 * JsonSchema schema = service.readSchema(reader);
 * </code>
 * </pre>
 *
 * <p>
 * Alternatively, a JSON schema can be built programmatically with
 * {@link JsonSchemaBuilder} as follows.
 * </p>
 *
 * <pre>
 * <code>
 * JsonSchemaBuilderFactory factory = service.createSchemaBuilderFactory();
 * JsonSchemaBuilder builder = factory.createBuilder();
 * JsonSchema schema = builder.withType(InstanceType.INTEGER).build();
 * </code>
 * </pre>
 *
 * <p>
 * Any instance of this class is safe for use by multiple concurrent threads
 * after schema reading or schema building once completed.
 * </p>
 *
 * @author leadpony
 */
public interface JsonSchema extends EvaluatorSource {

    /**
     * Checks if this schema has an identifier assigned or not.
     *
     * @return {@code true} if this schema has an identifier. {@code false} if this
     *         schema does not have an identifier.
     */
    default boolean hasId() {
        return false;
    }

    /**
     * Checks if this schema has an identifier and the identifier is absolute.
     *
     * @return {@code true} if this schema has an absolute identifier. {@code false}
     *         if this schema does not have an absolute identifier.
     * @see #hasId()
     */
    default boolean hasAbsoluteId() {
        return hasId() && id().isAbsolute();
    }

    /**
     * Returns the identifier of this schema, which is supplied by "$id" keyword.
     *
     * @return the identifier of this schema, or {@code null} if not exists.
     */
    default URI id() {
        return null;
    }

    /**
     * Returns the version identifier of this schema, which is supplied by "$schema"
     * keyword.
     *
     * @return the version identifier of this schema, or {@code null} if not exists.
     */
    default URI schema() {
        return null;
    }

    /**
     * Returns the comment for this schema, which is supplied by "$comment" keyword.
     *
     * @return the comment for this schema, or {@code null} if not exists.
     */
    default String comment() {
        return null;
    }

    /**
     * Returns the title of this schema, which is supplied by "title" keyword.
     *
     * @return the title of this schema, or {@code null} if not exists.
     */
    default String title() {
        return null;
    }

    /**
     * Returns the description of this schema, which is supplied by "description"
     * keyword.
     *
     * @return the description of this schema, or {@code null} if not exists.
     */
    default String description() {
        return null;
    }

    /**
     * Returns the default value of this schema, which is supplied by "default"
     * keyword.
     *
     * @return the default value of this schema. or {@code null} if not exists.
     */
    default JsonValue defaultValue() {
        return null;
    }

    default Optional<String> getAnchor() {
        return Optional.empty();
    }

    default URI getBaseUri() {
        throw new IllegalStateException("No base URI");
    }

    /**
     * Checks if this schema is a boolean schema.
     *
     * @return {@code true} if this schema is a boolean schema, {@code false}
     *         otherwise.
     */
    boolean isBoolean();

    /**
     * Returns all the keywords contained by this schema as a immutable map.
     *
     * @return all the keywords contained by this schema.
     * @since 4.0
     */
    default Map<String, Keyword> getKeywordsAsMap() {
        return Collections.emptyMap();
    }

    /**
     * Checks if this schema contains the specified keyword or not.
     *
     * @param keyword the name of the keyword.
     * @return {@code true} if this schema contains the specified keyword,
     *         {@code false} otherwise.
     * @throws NullPointerException if the specified {@code keyword} is
     *                              {@code null}.
     */
    default boolean containsKeyword(String keyword) {
        return false;
    }

    /**
     * Returns the value of the specified schema keyword as a {@code JsonValue}.
     *
     * @param keyword the name of the schema keyword, such as {@code "type"}.
     * @return keyword value if this schema contains the keyword, or {@code null} if
     *         this schema does not contain the keyword.
     * @throws NullPointerException if the specified {@code keyword} is
     *                              {@code null}.
     */
    default JsonValue getKeywordValue(String keyword) {
        return null;
    }

    /**
     * Returns the value of the specified schema keyword as a {@code JsonValue}.
     *
     * @param keyword      the name of the schema keyword, such as {@code "type"}.
     * @param defaultValue the value to be returned if this schema does not contain
     *                     the keyword. This value can be {@code null}.
     * @return keyword value if this schema contains the keyword, or
     *         {@code defaultValue} if this schema does not contain the keyword.
     * @throws NullPointerException if the specified {@code keyword} is
     *                              {@code null}.
     */
    default JsonValue getKeywordValue(String keyword, JsonValue defaultValue) {
        return defaultValue;
    }

    /**
     * Returns all of the subschemas contained in this schema.
     *
     * @return the stream of subschemas contained in this schema.
     */
    default Stream<JsonSchema> getSubschemas() {
        return Stream.empty();
    }

    /**
     * Returns all of the subschemas of this schema which will be applied to the
     * same instance location as this one.
     *
     * @return the stream of subschemas.
     */
    default Stream<JsonSchema> getInPlaceSubschemas() {
        return Stream.empty();
    }

    /**
     * Returns the subschema at the location specified with a JSON pointer.
     *
     * @param jsonPointer the valid escaped JSON Pointer string. It must be an empty
     *                    string or a sequence of '/' prefixed tokens.
     * @return the subschema if found or empty if the specified subschema does not
     *         exist at the specified location.
     * @throws NullPointerException if the specified {@code jsonPointer} is
     *                              {@code null}.
     * @throws JsonException        if the specified {@code jsonPointer} is not a
     *                              valid JSON Pointer.
     * @since 4.0
     */
    default Optional<JsonSchema> findSchema(String jsonPointer) {
        Objects.requireNonNull(jsonPointer, "jsonPointer must not be null.");
        return jsonPointer.isEmpty() ? Optional.of(this) : Optional.empty();
    }

    /**
     * Collects all descendant schemas under this schema.
     *
     * @return the map of schemas whose keys are JSON pointers and values are
     *         schemas, including this schema.
     * @since 4.0
     */
    default Map<String, JsonSchema> collectSchemas() {
        Map<String, JsonSchema> schemas = new HashMap<>();
        schemas.put("", this);
        return schemas;
    }

    /**
     * Collects all identified schemas under this schema.
     *
     * @param baseUri the initial base URI.
     * @return the map of schemas whose keys are resolved URIs and values are
     *         schemas. The map may contains this schema.
     * @since 4.0
     */
    default Map<URI, JsonSchema> collectIdentifiedSchemas(URI baseUri) {
        return Collections.emptyMap();
    }

    /**
     * Walks the schema tree starting from this schema.
     *
     * @param visitor the schema visitor which will be invoked for each schema and
     *                keyword.
     */
    default void walkSchemaTree(JsonSchemaVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null.");
        JsonSchemaVisitor.Result result = visitor.visitSchema(this, "");
        if (result == JsonSchemaVisitor.Result.CONTINUE) {
            visitor.leaveSchema(this, "");
        }
    }

    /**
     * Returns the value type of JSON representing this schema.
     *
     * @return the value type of JSON.
     * @see #toJson()
     */
    ValueType getJsonValueType();

    /**
     * Returns the JSON representation of this schema.
     *
     * @return the JSON representation of this schema, never be {@code null}.
     */
    JsonValue toJson();

    /**
     * Returns the string representation of this schema.
     *
     * @return the string representation of this schema, never be {@code null}.
     * @throws JsonException if an error occurred while generating a JSON.
     */
    @Override
    String toString();

    /**
     * Returns a boolean schema instance representing the specified boolean value.
     *
     * @param value the boolean value.
     * @return {@link JsonSchema#TRUE} if the specified {@code value} is
     *         {@code true}, or {@link JsonSchema#FALSE} if the specified
     *         {@code value} is {@code false}.
     */
    static JsonSchema valueOf(boolean value) {
        return value ? JsonSchema.TRUE : JsonSchema.FALSE;
    }

    /**
     * The JSON schema which is always evaluated as true. Any JSON instance satisfy
     * this schema.
     */
    JsonSchema TRUE = new BooleanJsonSchema.True();

    /**
     * The JSON schema which is always evaluated as false. Any JSON instance does
     * not satisfy this schema.
     */
    JsonSchema FALSE = new BooleanJsonSchema.False();

    /**
     * The JSON schema represented by an empty JSON object. This schema is always
     * evaluated as true.
     */
    JsonSchema EMPTY = new EmptyJsonSchema();
}
