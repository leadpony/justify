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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaBuilder;
import org.leadpony.justify.api.JsonSchemaBuilderFactory;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * A test class for testing the {@link JsonSchemaBuilder} implementation.
 *
 * @author leadpony
 */
public class JsonSchemaBuilderTest {

    private static final JsonValidationService service = JsonValidationServices.get();

    private JsonSchemaBuilderFactory schemaBuilderfactory;
    private JsonBuilderFactory jsonBuilderFactory;

    @BeforeEach
    public void setUp() {
        this.schemaBuilderfactory = service.createSchemaBuilderFactory();
        this.jsonBuilderFactory = Json.createBuilderFactory(null);
    }

    @AfterEach
    public void tearDown() {
        this.jsonBuilderFactory = null;
        this.schemaBuilderfactory = null;
    }

    @Test
    public void build_returnsNewSchema() {
        // Given
        JsonSchemaBuilder sut = createSchemaBuilder();
        sut.withTitle("Person")
           .withType(InstanceType.OBJECT)
           .withProperty("firstName",
                   createSchemaBuilder().withType(InstanceType.STRING).build())
           .withProperty("lastName",
                   createSchemaBuilder().withType(InstanceType.STRING).build())
           .withProperty("age",
                   createSchemaBuilder()
                       .withDescription("Age in years")
                       .withType(InstanceType.INTEGER)
                       .withMinimum(0)
                       .build())
           .withRequired("firstName", "lastName")
           ;
        // When
        JsonSchema schema = sut.build();

        JsonObject expected = createObjectBuilder()
                .add("title", "Person")
                .add("type", "object")
                .add("properties", createObjectBuilder()
                        .add("firstName", createObjectBuilder()
                                .add("type", "string")
                                .build())
                        .add("lastName", createObjectBuilder()
                                .add("type", "string")
                                .build())
                        .add("age", createObjectBuilder()
                                .add("description", "Age in years")
                                .add("type", "integer")
                                .add("minimum", 0)
                                .build())
                        .build())
                .add("required", createArrayBuilder()
                        .add("firstName")
                        .add("lastName")
                        .build())
                .build();
        // Then
        assertThat(schema).isNotNull();
        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withId_addsId() {
        JsonSchema schema = createSchemaBuilder()
                .withId(URI.create("https://example.com/example.schema.json"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("$id", "https://example.com/example.schema.json")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withSchema_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withSchema(URI.create("http://json-schema.org/draft-07/schema#"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("$schema", "http://json-schema.org/draft-07/schema#")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withSchema_addsComment() {
        JsonSchema schema = createSchemaBuilder()
                .withComment("Not finished.")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("$comment", "Not finished.")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withType_addsType() {
        JsonSchema schema = createSchemaBuilder()
                .withType(InstanceType.STRING)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("type", "string")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withType_throwsIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withType();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withType_throwsIfNotUnique() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withType(InstanceType.STRING, InstanceType.NUMBER, InstanceType.STRING);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withType_addsSetOfTypes() {
        Set<InstanceType> typeSet = new LinkedHashSet<>();
        typeSet.add(InstanceType.ARRAY);
        typeSet.add(InstanceType.OBJECT);

        JsonSchema schema = createSchemaBuilder()
                .withType(typeSet)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("type", createArrayBuilder().add("array").add("object"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withType_throwsIfEmptySet() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withType(new LinkedHashSet<>());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withEnum_addsEnumerators() {
        JsonSchema schema = createSchemaBuilder()
                .withEnum(JsonValue.TRUE, JsonValue.FALSE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("enum", createArrayBuilder()
                        .add(JsonValue.TRUE)
                        .add(JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withEnum_throwsIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withEnum();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withEnum_throwsIfNotUnique() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withEnum(JsonValue.TRUE, JsonValue.TRUE);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withEnum_addsSetOfEnumerators() {
        Set<JsonValue> valueSet = new LinkedHashSet<>();
        valueSet.add(JsonValue.TRUE);
        valueSet.add(JsonValue.FALSE);

        JsonSchema schema = createSchemaBuilder()
                .withEnum(valueSet)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("enum", createArrayBuilder()
                        .add(JsonValue.TRUE)
                        .add(JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withEnum_throwsIfEmptySet() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withEnum(new LinkedHashSet<>());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withConst_addsConstant() {
        JsonSchema schema = createSchemaBuilder()
                .withConst(JsonValue.EMPTY_JSON_OBJECT)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("const", JsonValue.EMPTY_JSON_OBJECT)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMultipleOf_addsInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withMultipleOf(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("multipleOf", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMultipleOf_addsNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withMultipleOf(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("multipleOf", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMultipleOf_throwsIfZero() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withMultipleOf(0);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMultipleOf_throwsIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withMultipleOf(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMaximum_addsInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withMaximum(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maximum", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaximum_addsNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withMaximum(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maximum", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withExclusiveMaximum_addsInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withExclusiveMaximum(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("exclusiveMaximum", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withExclusiveMaximum_addsNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withExclusiveMaximum(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("exclusiveMaximum", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinimum_addsInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withMinimum(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minimum", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinimum_addsNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withMinimum(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minimum", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withExclusiveMinimum_addsInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withExclusiveMinimum(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("exclusiveMinimum", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withExclusiveMinimum_addsNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withExclusiveMinimum(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("exclusiveMinimum", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxLength_addsLength() {
        JsonSchema schema = createSchemaBuilder()
                .withMaxLength(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maxLength", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxLength_throwsIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withMaxLength(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMinLength_addsLength() {
        JsonSchema schema = createSchemaBuilder()
                .withMinLength(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minLength", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinLength_throwsIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withMinLength(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withPattern_addsPattern() {
        JsonSchema schema = createSchemaBuilder()
                .withPattern("a*b")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("pattern", "a*b")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withPattern_throwsIfInvalidPattern() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withPattern("^(abc]");
        });

        assertThat(thrown).isInstanceOf(PatternSyntaxException.class);
    }

    @Test
    public void withItems_addsSchemaForAllItems() {
        JsonSchema schema = createSchemaBuilder()
                .withType(InstanceType.ARRAY)
                .withItems(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("type", "array")
                .add("items", true)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withItemsArray_addsArrayOfSchemas() {
        JsonSchema schema = createSchemaBuilder()
                .withType(InstanceType.ARRAY)
                .withItemsArray(JsonSchema.TRUE, JsonSchema.FALSE, JsonSchema.EMPTY)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("type", "array")
                .add("items", createArrayBuilder()
                        .add(JsonValue.TRUE)
                        .add(JsonValue.FALSE)
                        .add(JsonValue.EMPTY_JSON_OBJECT)
                        .build())
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withItemsArray_throwsIfArrayIsEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withType(InstanceType.ARRAY).withItemsArray();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withItemsArray_throwsIfListIsEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();
        Throwable thrown = catchThrowable(()->{
            sut.withType(InstanceType.ARRAY).withItemsArray(Collections.emptyList());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withItemsArray_addsListOfSchemas() {
        List<JsonSchema> schemas = Arrays.asList(JsonSchema.TRUE, JsonSchema.FALSE, JsonSchema.EMPTY);
        JsonSchemaBuilder sut = createSchemaBuilder();
        JsonSchema schema = sut.withType(InstanceType.ARRAY)
           .withItemsArray(schemas)
           .build();

        JsonObject expected = createObjectBuilder()
                .add("type", "array")
                .add("items", createArrayBuilder()
                        .add(JsonValue.TRUE)
                        .add(JsonValue.FALSE)
                        .add(JsonValue.EMPTY_JSON_OBJECT)
                        .build())
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAdditionalItems_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withAdditionalItems(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("additionalItems", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxItems_addsCount() {
        JsonSchema schema = createSchemaBuilder()
                .withMaxItems(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maxItems", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxItems_throwsIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withMaxItems(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMinItems_addsCount() {
        JsonSchema schema = createSchemaBuilder()
                .withMinItems(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minItems", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinItems_throwsIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withMinItems(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withUniqueItems_addsBoolean() {
        JsonSchema schema = createSchemaBuilder()
                .withUniqueItems(true)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("uniqueItems", true)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withContains_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withContains(JsonSchema.EMPTY)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("contains", JsonValue.EMPTY_JSON_OBJECT)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxProperties_addsCount() {
        JsonSchema schema = createSchemaBuilder()
                .withMaxProperties(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maxProperties", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxProperties_throwsIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withMaxProperties(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMinProperties_addsCount() {
        JsonSchema schema = createSchemaBuilder()
                .withMinProperties(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minProperties", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinProperties_throwsIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withMinProperties(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withRequired_addsSingleProperty() {
        JsonSchema schema = createSchemaBuilder()
                .withRequired("foo")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("required", createArrayBuilder().add("foo").build())
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withRequired_addsNoProperties() {
        JsonSchema schema = createSchemaBuilder()
                .withRequired()
                .build();

        JsonObject expected = createObjectBuilder()
                .add("required", JsonValue.EMPTY_JSON_ARRAY)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withRequired_throwsIfNotUnique() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withRequired("foo", "bar", "foo");
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withRequired_addsSetOfProperties() {
        Set<String> propertySet = new LinkedHashSet<>();
        propertySet.add("foo");
        propertySet.add("bar");

        JsonSchema schema = createSchemaBuilder()
                .withRequired(propertySet)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("required", createArrayBuilder()
                        .add("foo")
                        .add("bar"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withRequired_addsEmptySetOfProperties() {
        JsonSchema schema = createSchemaBuilder()
                .withRequired(new LinkedHashSet<>())
                .build();

        JsonObject expected = createObjectBuilder()
                .add("required", JsonValue.EMPTY_JSON_ARRAY)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withProperty_addsSingleProperty() {
        JsonSchema schema = createSchemaBuilder()
                .withProperty("foo", JsonSchema.EMPTY)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("properties", createObjectBuilder()
                        .add("foo", JsonValue.EMPTY_JSON_OBJECT))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withProperties_addsSetOfProperties() {
        Map<String, JsonSchema> properties = new HashMap<>();
        properties.put("foo", JsonSchema.TRUE);
        properties.put("bar", JsonSchema.FALSE);

        JsonSchema schema = createSchemaBuilder()
                .withProperties(properties)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("properties", createObjectBuilder()
                    .add("foo", JsonValue.TRUE)
                    .add("bar", JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withPatternProperty_addsSingleProperty() {
        JsonSchema schema = createSchemaBuilder()
                .withPatternProperty("a*b", JsonSchema.EMPTY)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("patternProperties", createObjectBuilder()
                        .add("a*b", JsonValue.EMPTY_JSON_OBJECT))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withPatternProperty_throwsIfInvalidPattern() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withPatternProperty("^(abc]", JsonSchema.EMPTY);
        });

        assertThat(thrown).isInstanceOf(PatternSyntaxException.class);
    }

    @Test
    public void withPatternProperties_addsSetOfProperties() {
        Map<String, JsonSchema> properties = new HashMap<>();
        properties.put("^[a-z]", JsonSchema.TRUE);
        properties.put("^[A-Z]", JsonSchema.FALSE);

        JsonSchema schema = createSchemaBuilder()
                .withPatternProperties(properties)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("patternProperties", createObjectBuilder()
                    .add("^[a-z]", JsonValue.TRUE)
                    .add("^[A-Z]", JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAdditionalProperties_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withAdditionalProperties(JsonSchema.FALSE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("additionalProperties", JsonValue.FALSE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDependency_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withDependency("foo", JsonSchema.FALSE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("dependencies", createObjectBuilder()
                        .add("foo", JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDependency_addsProperties() {
        JsonSchema schema = createSchemaBuilder()
                .withDependency("foo", "bar", "baz")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("dependencies", createObjectBuilder()
                        .add("foo", createArrayBuilder().add("bar").add("baz")))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDependency_throwsIfNotUnique() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withDependency("foo", "bar", "baz", "bar");
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withDependency_addsSetOfProperties() {
        Set<String> propertySet = new LinkedHashSet<>();
        propertySet.add("bar");
        propertySet.add("baz");

        JsonSchema schema = createSchemaBuilder()
                .withDependency("foo", propertySet)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("dependencies", createObjectBuilder()
                        .add("foo", createArrayBuilder().add("bar").add("baz")))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDependencies_addsDependencies() {
        Set<String> propertySet = new LinkedHashSet<>();
        propertySet.add("baz");

        Map<String, Object> dependencies = new LinkedHashMap<>();
        dependencies.put("foo", propertySet);
        dependencies.put("bar", JsonSchema.EMPTY);

        JsonSchema schema = createSchemaBuilder()
                .withDependencies(dependencies)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("dependencies", createObjectBuilder()
                        .add("foo", createArrayBuilder().add("baz"))
                        .add("bar", JsonValue.EMPTY_JSON_OBJECT))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withPropertyNames_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withPropertyNames(JsonSchema.FALSE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("propertyNames", JsonValue.FALSE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withIf_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withIf(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("if", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withThen_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withThen(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("then", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withElse_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withElse(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("else", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAllOf_addsSingleSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withAllOf(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("allOf", createArrayBuilder().add(JsonValue.TRUE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAllOf_throwsIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withAllOf();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withAllOf_addsSchemaList() {
        List<JsonSchema> schemas = Arrays.asList(JsonSchema.TRUE, JsonSchema.FALSE);

        JsonSchema schema = createSchemaBuilder()
                .withAllOf(schemas)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("allOf", createArrayBuilder()
                        .add(JsonValue.TRUE)
                        .add(JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAllOf_throwsIfEmptyList() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withAllOf(Collections.emptyList());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withAnyOf_addsSingleSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withAnyOf(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("anyOf", createArrayBuilder().add(JsonValue.TRUE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAnyOf_throwsIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withAnyOf();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withAnyOf_addsSchemaList() {
        List<JsonSchema> schemas = Arrays.asList(JsonSchema.TRUE, JsonSchema.FALSE);

        JsonSchema schema = createSchemaBuilder()
                .withAnyOf(schemas)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("anyOf", createArrayBuilder()
                        .add(JsonValue.TRUE)
                        .add(JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAnyOf_throwsIfEmptyList() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withAnyOf(Collections.emptyList());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withOneOf_addsSingleSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withOneOf(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("oneOf", createArrayBuilder().add(JsonValue.TRUE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withOneOf_throwsIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withOneOf();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withOneOf_addsSchemaList() {
        List<JsonSchema> schemas = Arrays.asList(JsonSchema.TRUE, JsonSchema.FALSE);

        JsonSchema schema = createSchemaBuilder()
                .withOneOf(schemas)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("oneOf", createArrayBuilder()
                        .add(JsonValue.TRUE)
                        .add(JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withOneOf_throwsIfEmptyList() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withOneOf(Collections.emptyList());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withNot_addsSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withNot(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("not", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withFormat_addsAttriute() {
        JsonSchema schema = createSchemaBuilder()
                .withFormat("email")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("format", "email")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withFormat_throwsIfUnknownAttribute() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withFormat("unknown");
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withLaxFormat_addsKnownAttriute() {
        JsonSchema schema = createSchemaBuilder()
                .withLaxFormat("email")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("format", "email")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withLaxFormat_addsUnknownAttriute() {
        JsonSchema schema = createSchemaBuilder()
                .withLaxFormat("unknown")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("format", "unknown")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withContentEncoding_addsValue() {
        JsonSchema schema = createSchemaBuilder()
                .withContentEncoding("base64")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("contentEncoding", "base64")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withContentMediaType_addsValue() {
        JsonSchema schema = createSchemaBuilder()
                .withContentMediaType("application/json")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("contentMediaType", "application/json")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withContentMediaType_throwsIfInvalidMediaType() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(()->{
            sut.withContentMediaType(";");
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    public void withDefinition_addsDefinition() {
        JsonSchema schema = createSchemaBuilder()
                .withDefinition("foo", JsonSchema.EMPTY)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("definitions", createObjectBuilder()
                        .add("foo", JsonValue.EMPTY_JSON_OBJECT))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDefinitions_addsDefinitions() {
        Map<String, JsonSchema> definitions = new LinkedHashMap<>();
        definitions.put("foo", JsonSchema.TRUE);
        definitions.put("bar", JsonSchema.FALSE);

        JsonSchema schema = createSchemaBuilder()
                .withDefinitions(definitions)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("definitions", createObjectBuilder()
                        .add("foo", JsonValue.TRUE)
                        .add("bar", JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withTitle_addsTitle() {
        JsonSchema schema = createSchemaBuilder()
                .withTitle("untitled")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("title", "untitled")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDescription_addsDescription() {
        JsonSchema schema = createSchemaBuilder()
                .withDescription("detailed description")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("description", "detailed description")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDefault_addsDefaultValue() {
        JsonSchema schema = createSchemaBuilder()
                .withDefault(JsonValue.EMPTY_JSON_OBJECT)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("default", JsonValue.EMPTY_JSON_OBJECT)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    private JsonSchemaBuilder createSchemaBuilder() {
        return schemaBuilderfactory.createBuilder();
    }

    private JsonObjectBuilder createObjectBuilder() {
        return jsonBuilderFactory.createObjectBuilder();
    }

    private JsonArrayBuilder createArrayBuilder() {
        return jsonBuilderFactory.createArrayBuilder();
    }
}
