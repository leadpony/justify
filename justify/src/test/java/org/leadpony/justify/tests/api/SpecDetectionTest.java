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
package org.leadpony.justify.tests.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.tests.helper.ApiTest;
import org.leadpony.justify.tests.helper.ProblemPrinter;

/**
 * A test type for testing the auto detection of spec versions.
 *
 * @author leadpony
 */
@ApiTest
public class SpecDetectionTest {

    private static JsonValidationService service;
    private static ProblemPrinter printer;

    public static Stream<Arguments> supportedVersions() {
        return Stream.of(
                Arguments.of(SpecVersion.DRAFT_04, "detect-draft04.schema.json"),
                Arguments.of(SpecVersion.DRAFT_04, "detect-draft06.schema.json"),
                Arguments.of(SpecVersion.DRAFT_04, "detect-draft07.schema.json"),
                Arguments.of(SpecVersion.DRAFT_04, "detect-unspecified.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-draft04.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-draft06.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-draft07.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-unspecified.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-draft04.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-draft06.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-draft07.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-unspecified.schema.json"));
    }

    @ParameterizedTest
    @MethodSource("supportedVersions")
    public void streamShouldDetectVersion(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, true);
        InputStream in = createStream(schemaName);
        try (JsonSchemaReader reader = factory.createSchemaReader(in)) {
            reader.read();
        }
    }

    @ParameterizedTest
    @MethodSource("supportedVersions")
    public void streamWithCharsetShouldDetectVersion(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, true);
        InputStream in = createStream(schemaName);
        try (JsonSchemaReader reader = factory.createSchemaReader(in, StandardCharsets.UTF_8)) {
            reader.read();
        }
    }

    @ParameterizedTest
    @MethodSource("supportedVersions")
    public void readerShouldDetectVersion(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, true);
        Reader in = createReader(schemaName);
        try (JsonSchemaReader reader = factory.createSchemaReader(in)) {
            reader.read();
        }
    }

    public static Stream<Arguments> unsupportedVersions() {
        return Stream.of(
                Arguments.of(SpecVersion.DRAFT_04, "detect-unsupported.schema.json"),
                Arguments.of(SpecVersion.DRAFT_04, "detect-illegal.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-unsupported.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-illegal.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-unsupported.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-illegal.schema.json"));
    }

    @ParameterizedTest
    @MethodSource("unsupportedVersions")
    public void streamShouldThrowIfUnsupportedVersion(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, true);
        InputStream in = createStream(schemaName);
        JsonSchemaReader reader = factory.createSchemaReader(in);

        Throwable thrown = catchThrowable(() -> {
            reader.read();
        });

        reader.close();

        assertThat(thrown).isInstanceOf(JsonValidatingException.class);

        JsonValidatingException e = (JsonValidatingException) thrown;
        printer.print(e.getProblems());
    }

    @ParameterizedTest
    @MethodSource("unsupportedVersions")
    public void readerShouldThrowIfUnsupportedVersion(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, true);
        Reader in = createReader(schemaName);
        JsonSchemaReader reader = factory.createSchemaReader(in);

        Throwable thrown = catchThrowable(() -> {
            reader.read();
        });

        reader.close();

        assertThat(thrown).isInstanceOf(JsonValidatingException.class);

        JsonValidatingException e = (JsonValidatingException) thrown;
        printer.print(e.getProblems());
    }

    public static Stream<Arguments> matchedVersions() {
        return Stream.of(
                Arguments.of(SpecVersion.DRAFT_04, "detect-draft04.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-draft06.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-draft07.schema.json"));
    }

    @ParameterizedTest
    @MethodSource("matchedVersions")
    public void streamWithoutDetectionShouldSucceed(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, false);
        InputStream in = createStream(schemaName);
        try (JsonSchemaReader reader = factory.createSchemaReader(in)) {
            reader.read();
        }
    }

    @ParameterizedTest
    @MethodSource("matchedVersions")
    public void readerWithoutDetectionShouldSucceed(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, false);
        Reader in = createReader(schemaName);
        try (JsonSchemaReader reader = factory.createSchemaReader(in)) {
            reader.read();
        }
    }

    public static Stream<Arguments> unmatchedVersions() {
        return Stream.of(
                Arguments.of(SpecVersion.DRAFT_04, "detect-draft06.schema.json"),
                Arguments.of(SpecVersion.DRAFT_04, "detect-draft07.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-draft04.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-draft07.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-draft04.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-draft06.schema.json"));
    }

    @ParameterizedTest
    @MethodSource("unmatchedVersions")
    @Disabled
    public void streamWithoutDetectionShouldThrow(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, false);
        InputStream in = createStream(schemaName);
        JsonSchemaReader reader = factory.createSchemaReader(in);

        Throwable thrown = catchThrowable(() -> {
            reader.read();
        });

        reader.close();

        assertThat(thrown)
            .isNotNull()
            .isInstanceOf(JsonValidatingException.class);

        JsonValidatingException e = (JsonValidatingException) thrown;
        printer.print(e.getProblems());
    }

    @ParameterizedTest
    @MethodSource("unmatchedVersions")
    @Disabled
    public void readerWithoutDetectionShouldThrow(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, false);
        Reader in = createReader(schemaName);
        JsonSchemaReader reader = factory.createSchemaReader(in);

        Throwable thrown = catchThrowable(() -> {
            reader.read();
        });

        reader.close();

        assertThat(thrown)
            .isNotNull()
            .isInstanceOf(JsonValidatingException.class);

        JsonValidatingException e = (JsonValidatingException) thrown;
        printer.print(e.getProblems());
    }

    /* helpers */

    private static JsonSchemaReaderFactory createFactory(SpecVersion defaultVersion, boolean detection) {
        return service.createSchemaReaderFactoryBuilder()
                .withDefaultSpecVersion(defaultVersion)
                .withStrictKeywords(true)
                .withSchemaValidation(true)
                .withSpecVersionDetection(detection)
                .build();
    }

    private InputStream createStream(String name) {
        return getClass().getResourceAsStream(name);
    }

    private Reader createReader(String name) {
        return new InputStreamReader(createStream(name), StandardCharsets.UTF_8);
    }
}
