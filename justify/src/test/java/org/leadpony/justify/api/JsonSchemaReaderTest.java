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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.StringReader;
import java.net.URI;
import java.util.stream.Stream;

import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.test.helper.JsonAssertions;
import org.leadpony.justify.test.helper.JsonResource;
import org.leadpony.justify.test.helper.JsonSource;

/**
 * A test class for testing the {@link JsonSchemaReader} implementation.
 *
 * @author leadpony
 */
public class JsonSchemaReaderTest extends BaseTest {

    public static Stream<Arguments> schemas() {
        return Stream.of(
                Arguments.of("", JsonParsingException.class),
                Arguments.of(" ", JsonParsingException.class),
                Arguments.of(" {}", null),
                Arguments.of("{\"type\":", JsonParsingException.class),
                Arguments.of("{\"type\":\"number\"", JsonParsingException.class),
                Arguments.of("{\"type\":\"number\"},", null));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("schemas")
    public void readShouldThrowIfSchemaIsInvalid(String schemaJson, Class<?> exceptionClass) {
        JsonSchemaReader reader = SERVICE.createSchemaReader(new StringReader(schemaJson));
        Throwable thrown = catchThrowable(() -> reader.read());
        if (exceptionClass != null) {
            assertThat(thrown).isInstanceOf(exceptionClass);
            print(thrown);
        } else {
            assertThat(thrown).isNull();
        }
    }

    @Test
    public void readShouldReadSchemaMetadata() {
        String source = "{"
                + "\"$schema\": \"http://json-schema.org/draft-07/schema#\","
                + "\"$id\": \"http://example.com/product.schema.json\","
                + "\"title\": \"Product\","
                + "\"description\": \"A product from Acme's catalog\","
                + "\"$comment\": \"As an example.\""
                + "}";
        JsonSchema schema = SERVICE.readSchema(new StringReader(source));

        assertThat(schema.hasId()).isTrue();
        assertThat(schema.schema()).isEqualTo(URI.create("http://json-schema.org/draft-07/schema#"));
        assertThat(schema.id()).isEqualTo(URI.create("http://example.com/product.schema.json"));
        assertThat(schema.comment()).isEqualTo("As an example.");
        assertThat(schema.title()).isEqualTo("Product");
        assertThat(schema.description()).isEqualTo("A product from Acme's catalog");

        JsonAssertions.assertThat(schema.toJson()).isEqualTo(source);
    }

    /**
     * @author leadpony
     */
    public static class StrictKeywordTestCase {

        public String description;
        public JsonValue schema;
        public int errors;

        @Override
        public String toString() {
            return description;
        }
    }

    @ParameterizedTest
    @JsonSource("jsonschemareadertest-unknownkeywords.json")
    public void readShouldThrowIfStrictWithKeywords(StrictKeywordTestCase test) {
        JsonSchemaReaderFactory factory = SERVICE.createSchemaReaderFactoryBuilder()
                .withStrictKeywords(true)
                .build();
        String json = test.schema.toString();
        JsonSchemaReader reader = factory.createSchemaReader(new StringReader(json));
        Throwable thrown = catchThrowable(() -> reader.read());
        if (test.errors > 0) {
            assertThat(thrown).isNotNull().isInstanceOf(JsonValidatingException.class);
            JsonValidatingException e = (JsonValidatingException) thrown;
            assertThat(e.getProblems()).hasSize(test.errors);
            print(thrown);
        } else {
            assertThat(thrown).isNull();
        }
    }

    @ParameterizedTest
    @JsonSource("jsonschemareadertest-unknownkeywords.json")
    public void readShouldNotThrowIfNotStrictWithKeywords(StrictKeywordTestCase test) {
        JsonSchemaReaderFactory factory = SERVICE.createSchemaReaderFactoryBuilder()
                .withStrictKeywords(false)
                .build();
        String json = test.schema.toString();
        JsonSchemaReader reader = factory.createSchemaReader(new StringReader(json));
        Throwable thrown = catchThrowable(() -> reader.read());
        assertThat(thrown).isNull();
    }

    private static final String SCHEMA_INCLUDING_UNKNOWN_FORMAT_ATTRIBUTE
        = "{ \"type\": \"string\", \"format\": \"foo\" }";

    @Test
    public void readShouldThrowIfStrictWithFormats() {
        String source = SCHEMA_INCLUDING_UNKNOWN_FORMAT_ATTRIBUTE;
        JsonSchemaReaderFactory factory = SERVICE.createSchemaReaderFactoryBuilder().withStrictFormats(true)
                .build();
        JsonSchemaReader reader = factory.createSchemaReader(new StringReader(source));
        Throwable thrown = catchThrowable(() -> reader.read());
        assertThat(thrown).isNotNull().isInstanceOf(JsonValidatingException.class);
        JsonValidatingException e = (JsonValidatingException) thrown;
        assertThat(e.getProblems()).hasSize(1);
        print(thrown);
    }

    @Test
    public void readShouldNotThrowIfNotStrictWithFormats() {
        String source = SCHEMA_INCLUDING_UNKNOWN_FORMAT_ATTRIBUTE;
        JsonSchemaReaderFactory factory = SERVICE.createSchemaReaderFactoryBuilder().withStrictFormats(false)
                .build();
        JsonSchemaReader reader = factory.createSchemaReader(new StringReader(source));
        Throwable thrown = catchThrowable(() -> reader.read());
        assertThat(thrown).isNull();
    }

    /**
     * @author leadpony
     */
    static class MetaschemaTestCase {

        final String description;
        final JsonValue schema;
        final JsonValue metaschema;
        final boolean valid;

        MetaschemaTestCase(String description, JsonValue schema, JsonValue metaschema, boolean valid) {
            this.description = description;
            this.schema = schema;
            this.metaschema = metaschema;
            this.valid = valid;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public static Stream<MetaschemaTestCase> readShouldValidateAgainstMetaschema() {
        return JsonResource.of("/org/leadpony/justify/api/jsonschemareadertest-metaschema.json")
            .asObjectStream()
            .map(object -> new MetaschemaTestCase(
                    object.getString("description"),
                    object.get("schema"),
                    object.get("metaschema"),
                    object.getBoolean("valid")
                    ));
    }

    @ParameterizedTest
    @MethodSource
    public void readShouldValidateAgainstMetaschema(MetaschemaTestCase test) {
        JsonSchema metaschema = readSchema(test.metaschema);

        JsonSchemaReaderFactory factory = SERVICE.createSchemaReaderFactoryBuilder()
                .withMetaschema(metaschema)
                .withSpecVersionDetection(false)
                .build();

        JsonSchemaReader reader = factory.createSchemaReader(new StringReader(test.schema.toString()));
        Throwable thrown = catchThrowable(() -> {
            reader.read();
        });
        reader.close();

        if (thrown != null) {
            print(thrown);
        }

        if (test.valid) {
            assertThat(thrown).isNull();
        } else {
            assertThat(thrown)
                .isNotNull()
                .isInstanceOf(JsonValidatingException.class);
        }
    }

    private static JsonSchema readSchema(JsonValue value) {
        return readSchema(value.toString());
    }

    private static JsonSchema readSchema(String string) {
        JsonSchemaReaderFactory factory = SERVICE.createSchemaReaderFactoryBuilder()
                .withSpecVersionDetection(false)
                .build();
        try (JsonSchemaReader reader = factory.createSchemaReader(new StringReader(string))) {
            return reader.read();
        }
    }
}
