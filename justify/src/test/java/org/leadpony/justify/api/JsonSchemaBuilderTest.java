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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
public class JsonSchemaBuilderTest extends BaseTest {

    private static JsonSchemaBuilderFactory schemaBuilderfactory;
    private static JsonBuilderFactory jsonBuilderFactory;

    @BeforeAll
    public static void setUpOnce() {
        schemaBuilderfactory = SERVICE.createSchemaBuilderFactory();
        jsonBuilderFactory = Json.createBuilderFactory(null);
    }

    @AfterAll
    public static void tearDownOncce() {
        jsonBuilderFactory = null;
        schemaBuilderfactory = null;
    }

    @Test
    public void buildShouldReturnNewSchema() {
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
                .withRequired("firstName", "lastName");

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
    public void withIdShouldAddId() {
        JsonSchema schema = createSchemaBuilder()
                .withId(URI.create("https://example.com/example.schema.json"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("$id", "https://example.com/example.schema.json")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withSchemaShouldAddSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withSchema(URI.create("http://json-schema.org/draft-07/schema#"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("$schema", "http://json-schema.org/draft-07/schema#")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withSchemaShouldAddComment() {
        JsonSchema schema = createSchemaBuilder()
                .withComment("Not finished.")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("$comment", "Not finished.")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withTypeShouldAddType() {
        JsonSchema schema = createSchemaBuilder()
                .withType(InstanceType.STRING)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("type", "string")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withTypeShouldThrowIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withType();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withTypeShouldThrowIfNotUnique() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withType(InstanceType.STRING, InstanceType.NUMBER, InstanceType.STRING);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withTypeShouldAddSetOfTypes() {
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
    public void withTypeShouldThrowIfEmptySet() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withType(new LinkedHashSet<>());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withEnumShouldAddEnumerators() {
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
    public void withEnumShouldThrowIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withEnum();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withEnumShouldThrowIfNotUnique() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withEnum(JsonValue.TRUE, JsonValue.TRUE);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withEnumShouldAddSetOfEnumerators() {
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
    public void withEnumShouldThrowIfEmptySet() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withEnum(new LinkedHashSet<>());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withConstShouldAddConstant() {
        JsonSchema schema = createSchemaBuilder()
                .withConst(JsonValue.EMPTY_JSON_OBJECT)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("const", JsonValue.EMPTY_JSON_OBJECT)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMultipleOfShouldAddInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withMultipleOf(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("multipleOf", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMultipleOfShouldAddNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withMultipleOf(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("multipleOf", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMultipleOfShouldThrowIfZero() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withMultipleOf(0);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMultipleOfShouldThrowIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withMultipleOf(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMaximumShouldAddInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withMaximum(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maximum", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaximumShouldAddNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withMaximum(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maximum", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withExclusiveMaximumShouldAddInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withExclusiveMaximum(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("exclusiveMaximum", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withExclusiveMaximumShouldAddNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withExclusiveMaximum(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("exclusiveMaximum", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinimumShouldAddInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withMinimum(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minimum", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinimumShouldAddNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withMinimum(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minimum", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withExclusiveMinimumShouldAddInteger() {
        JsonSchema schema = createSchemaBuilder()
                .withExclusiveMinimum(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("exclusiveMinimum", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withExclusiveMinimumShouldAddNumber() {
        JsonSchema schema = createSchemaBuilder()
                .withExclusiveMinimum(new BigDecimal("3.14"))
                .build();

        JsonObject expected = createObjectBuilder()
                .add("exclusiveMinimum", new BigDecimal("3.14"))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxLengthShouldAddLength() {
        JsonSchema schema = createSchemaBuilder()
                .withMaxLength(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maxLength", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxLengthShouldThrowIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withMaxLength(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMinLengthShouldAddLength() {
        JsonSchema schema = createSchemaBuilder()
                .withMinLength(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minLength", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinLengthShouldThrowIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withMinLength(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withPatternShouldAddPattern() {
        JsonSchema schema = createSchemaBuilder()
                .withPattern("a*b")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("pattern", "a*b")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withPatternShouldThrowIfInvalidPattern() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withPattern("^(abc]");
        });

        assertThat(thrown).isInstanceOf(PatternSyntaxException.class);
    }

    @Test
    public void withItemsShouldAddSchemaForAllItems() {
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
    public void withItemsArrayShouldAddArrayOfSchemas() {
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
    public void withItemsArrayShouldThrowIfArrayIsEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withType(InstanceType.ARRAY).withItemsArray();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withItemsArrayShouldThrowIfListIsEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();
        Throwable thrown = catchThrowable(() -> {
            sut.withType(InstanceType.ARRAY).withItemsArray(Collections.emptyList());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withItemsArrayShouldAddListOfSchemas() {
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
    public void withAdditionalItemsShouldAddSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withAdditionalItems(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("additionalItems", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxItemsShouldAddCount() {
        JsonSchema schema = createSchemaBuilder()
                .withMaxItems(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maxItems", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxItemsShouldThrowIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withMaxItems(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMinItemsShouldAddCount() {
        JsonSchema schema = createSchemaBuilder()
                .withMinItems(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minItems", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinItemsShouldThrowIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withMinItems(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withUniqueItemsShouldAddBoolean() {
        JsonSchema schema = createSchemaBuilder()
                .withUniqueItems(true)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("uniqueItems", true)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withContainsShouldAddSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withContains(JsonSchema.EMPTY)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("contains", JsonValue.EMPTY_JSON_OBJECT)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxPropertiesShouldAddCount() {
        JsonSchema schema = createSchemaBuilder()
                .withMaxProperties(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("maxProperties", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMaxPropertiesShouldThrowIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withMaxProperties(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withMinPropertiesShouldAddCount() {
        JsonSchema schema = createSchemaBuilder()
                .withMinProperties(5)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("minProperties", 5)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withMinPropertiesShouldThrowIfNegative() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withMinProperties(-1);
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withRequiredShouldAddSingleProperty() {
        JsonSchema schema = createSchemaBuilder()
                .withRequired("foo")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("required", createArrayBuilder().add("foo").build())
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withRequiredShouldAddNoProperties() {
        JsonSchema schema = createSchemaBuilder()
                .withRequired()
                .build();

        JsonObject expected = createObjectBuilder()
                .add("required", JsonValue.EMPTY_JSON_ARRAY)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withRequiredShouldThrowIfNotUnique() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withRequired("foo", "bar", "foo");
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withRequiredShouldAddSetOfProperties() {
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
    public void withRequiredShouldAddEmptySetOfProperties() {
        JsonSchema schema = createSchemaBuilder()
                .withRequired(new LinkedHashSet<>())
                .build();

        JsonObject expected = createObjectBuilder()
                .add("required", JsonValue.EMPTY_JSON_ARRAY)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withPropertyShouldAddSingleProperty() {
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
    public void withPropertiesShouldAddSetOfProperties() {
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
    public void withPatternPropertyShouldAddSingleProperty() {
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
    public void withPatternPropertyShouldThrowIfInvalidPattern() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withPatternProperty("^(abc]", JsonSchema.EMPTY);
        });

        assertThat(thrown).isInstanceOf(PatternSyntaxException.class);
    }

    @Test
    public void withPatternPropertiesShouldAddSetOfProperties() {
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
    public void withAdditionalPropertiesShouldAddSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withAdditionalProperties(JsonSchema.FALSE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("additionalProperties", JsonValue.FALSE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDependencyShouldAddSchema() {
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
    public void withDependencyShouldAddProperties() {
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
    public void withDependencyShouldThrowIfNotUnique() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withDependency("foo", "bar", "baz", "bar");
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withDependencyShouldAddSetOfProperties() {
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
    public void withDependenciesShouldAddSchemaDependency() {
        Map<String, JsonSchema> map = new HashMap<>();
        map.put("a", JsonSchema.TRUE);
        map.put("b", JsonSchema.FALSE);

        JsonSchema schema = createSchemaBuilder()
                .withDependencies(map)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("dependencies", createObjectBuilder()
                        .add("a", JsonValue.TRUE)
                        .add("b", JsonValue.FALSE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDependenciesShouldAddPropertySetDependency() {
        Set<String> set = new LinkedHashSet<>();
        set.add("foo");
        set.add("bar");
        set.add("baz");

        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", set);

        JsonSchema schema = createSchemaBuilder()
                .withDependencies(map)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("dependencies", createObjectBuilder()
                        .add("a", createArrayBuilder().add("foo").add("bar").add("baz")))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDependenciesShouldAddDependencies() {
        Set<String> set = new LinkedHashSet<>();
        set.add("foo");
        set.add("bar");

        Map<String, Object> dependencies = new LinkedHashMap<>();
        dependencies.put("a", set);
        dependencies.put("b", JsonSchema.EMPTY);

        JsonSchema schema = createSchemaBuilder()
                .withDependencies(dependencies)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("dependencies", createObjectBuilder()
                        .add("a", createArrayBuilder().add("foo").add("bar"))
                        .add("b", JsonValue.EMPTY_JSON_OBJECT))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    /**
     * @author leadpony
     */
    enum DependenciesTestCase {
        EMPTY(
                map -> { },
                JsonValue.EMPTY_JSON_OBJECT
                ),
        EMPTY_SCHEMA(
                map -> {
                    map.put("a", JsonSchema.EMPTY);
                },
                createObjectBuilder()
                    .add("a", JsonValue.EMPTY_JSON_OBJECT)
                    .build()
                ),
        STRING_SET(
                map -> {
                    map.put("a", setOf("b", "c"));
                },
                createObjectBuilder()
                    .add("a", createArrayBuilder().add("b").add("c"))
                    .build()
                ),
        EMPTY_SET(
                map -> {
                    map.put("a", Collections.emptySet());
                },
                createObjectBuilder()
                    .add("a", JsonValue.EMPTY_JSON_ARRAY)
                    .build()
                ),
        SCHEMA_AND_STRING_SET(
                map -> {
                    map.put("a", JsonSchema.EMPTY);
                    map.put("b", setOf("c", "d"));
                },
                createObjectBuilder()
                    .add("a", JsonValue.EMPTY_JSON_OBJECT)
                    .add("b", createArrayBuilder().add("c").add("d"))
                    .build()
                );

        final Map<String, Object> values;
        final JsonValue json;

        DependenciesTestCase(Consumer<Map<String, Object>> consumer, JsonValue json) {
            this.values = new HashMap<>();
            consumer.accept(this.values);
            JsonObjectBuilder builder = createObjectBuilder();
            builder.add("dependencies", json);
            this.json = builder.build();
        }

        private static Set<?> setOf(Object... objects) {
            Set<Object> set = new HashSet<>();
            for (Object object : objects) {
                set.add(object);
            }
            return set;
        }
    }

    @ParameterizedTest
    @EnumSource(DependenciesTestCase.class)
    public void withDependenciesShouldAddDependencies(DependenciesTestCase test) {
        JsonSchemaBuilder builder = createSchemaBuilder();

        builder.withDependencies(test.values);

        JsonSchema schema = builder.build();
        assertThat(schema.toJson()).isEqualTo(test.json);
    }

    /**
     * @author leadpony
     */
    enum InvalidDependenciesTestCase {
        STRING(
                map -> {
                    map.put("a", "foo");
                }
                ),
        NULL(
                map -> {
                    map.put("a", null);
                }
                ),
        INTEGER_SET(
                map -> {
                    map.put("a", setOf(1, 2, 3));
                }
                ),
        NULL_SET(
                map -> {
                    map.put("a", setOf((Object) null));
                }
                );

        final Map<String, Object> values;

        InvalidDependenciesTestCase(Consumer<Map<String, Object>> consumer) {
            this.values = new HashMap<>();
            consumer.accept(this.values);
        }

        private static Set<?> setOf(Object... objects) {
            Set<Object> set = new HashSet<>();
            for (Object object : objects) {
                set.add(object);
            }
            return set;
        }
    }

    @ParameterizedTest
    @EnumSource(InvalidDependenciesTestCase.class)
    public void withDependenciesShouldShouldException(InvalidDependenciesTestCase test) {
        JsonSchemaBuilder builder = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            builder.withDependencies(test.values);
        });

        print(thrown);

        assertThat(thrown)
            .isNotNull()
            .isInstanceOf(ClassCastException.class);
    }

    @Test
    public void withPropertyNamesShouldAddSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withPropertyNames(JsonSchema.FALSE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("propertyNames", JsonValue.FALSE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withIfShouldAddSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withIf(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("if", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withThenShouldAddSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withThen(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("then", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withElseShouldAddSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withElse(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("else", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAllOfShouldAddSingleSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withAllOf(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("allOf", createArrayBuilder().add(JsonValue.TRUE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAllOfShouldThrowIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withAllOf();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withAllOfShouldAddSchemaList() {
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
    public void withAllOfShouldThrowIfEmptyList() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withAllOf(Collections.emptyList());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withAnyOfShouldAddSingleSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withAnyOf(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("anyOf", createArrayBuilder().add(JsonValue.TRUE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withAnyOfShouldThrowIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withAnyOf();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withAnyOfShouldAddSchemaList() {
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
    public void withAnyOfShouldThrowIfEmptyList() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withAnyOf(Collections.emptyList());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withOneOfShouldAddSingleSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withOneOf(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("oneOf", createArrayBuilder().add(JsonValue.TRUE))
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withOneOfShouldThrowIfEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withOneOf();
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withOneOfShouldAddSchemaList() {
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
    public void withOneOfShouldThrowIfEmptyList() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withOneOf(Collections.emptyList());
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withNotShouldAddSchema() {
        JsonSchema schema = createSchemaBuilder()
                .withNot(JsonSchema.TRUE)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("not", JsonValue.TRUE)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withFormatShouldAddAttriute() {
        JsonSchema schema = createSchemaBuilder()
                .withFormat("email")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("format", "email")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withFormatShouldThrowIfUnknownAttribute() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withFormat("unknown");
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withLaxFormatShouldAddKnownAttriute() {
        JsonSchema schema = createSchemaBuilder()
                .withLaxFormat("email")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("format", "email")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withLaxFormatShouldAddUnknownAttriute() {
        JsonSchema schema = createSchemaBuilder()
                .withLaxFormat("unknown")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("format", "unknown")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withContentEncodingShouldAddValue() {
        JsonSchema schema = createSchemaBuilder()
                .withContentEncoding("base64")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("contentEncoding", "base64")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withContentMediaTypeShouldAddValue() {
        JsonSchema schema = createSchemaBuilder()
                .withContentMediaType("application/json")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("contentMediaType", "application/json")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withContentMediaTypeShouldThrowIfInvalidMediaType() {
        JsonSchemaBuilder sut = createSchemaBuilder();

        Throwable thrown = catchThrowable(() -> {
            sut.withContentMediaType(";");
        });

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withDefinitionShouldAddDefinition() {
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
    public void withDefinitionsShouldAddDefinitions() {
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
    public void withTitleShouldAddTitle() {
        JsonSchema schema = createSchemaBuilder()
                .withTitle("untitled")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("title", "untitled")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDescriptionShouldAddDescription() {
        JsonSchema schema = createSchemaBuilder()
                .withDescription("detailed description")
                .build();

        JsonObject expected = createObjectBuilder()
                .add("description", "detailed description")
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withDefaultShouldAddDefaultValue() {
        JsonSchema schema = createSchemaBuilder()
                .withDefault(JsonValue.EMPTY_JSON_OBJECT)
                .build();

        JsonObject expected = createObjectBuilder()
                .add("default", JsonValue.EMPTY_JSON_OBJECT)
                .build();

        assertThat(schema.toJson()).isEqualTo(expected);
    }

    private static JsonSchemaBuilder createSchemaBuilder() {
        return schemaBuilderfactory.createBuilder();
    }

    private static JsonObjectBuilder createObjectBuilder() {
        return jsonBuilderFactory.createObjectBuilder();
    }

    private static JsonArrayBuilder createArrayBuilder() {
        return jsonBuilderFactory.createArrayBuilder();
    }
}
