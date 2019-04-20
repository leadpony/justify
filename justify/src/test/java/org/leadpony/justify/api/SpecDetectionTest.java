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
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A test type for testing the auto detection of spec versions.
 *
 * @author leadpony
 */
public class SpecDetectionTest {

    static final Logger log = Logger.getLogger(SpecDetectionTest.class.getName());

    static final JsonValidationService service = JsonValidationServices.get();
    static final ProblemHandler printer = service.createProblemPrinter(log::info);

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
                Arguments.of(SpecVersion.DRAFT_07, "detect-unspecified.schema.json")
                );
    }

    @ParameterizedTest
    @MethodSource("supportedVersions")
    public void stream_detectsVersion(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, true);
        InputStream in = createStream(schemaName);
        try (JsonSchemaReader reader = factory.createSchemaReader(in)) {
            reader.read();
        }
    }

    @ParameterizedTest
    @MethodSource("supportedVersions")
    public void streamWithCharset_detectsVersion(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, true);
        InputStream in = createStream(schemaName);
        try (JsonSchemaReader reader = factory.createSchemaReader(in, StandardCharsets.UTF_8)) {
            reader.read();
        }
    }

    @ParameterizedTest
    @MethodSource("supportedVersions")
    public void reader_detectsVersion(SpecVersion defaultVersion, String schemaName) {
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
                Arguments.of(SpecVersion.DRAFT_07, "detect-illegal.schema.json")
                );
    }

    @ParameterizedTest
    @MethodSource("unsupportedVersions")
    public void stream_throwsIfUnsupportedVersion(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, true);
        InputStream in = createStream(schemaName);
        JsonSchemaReader reader = factory.createSchemaReader(in);

        Throwable thrown = catchThrowable(()->{
            reader.read();
        });

        reader.close();

        assertThat(thrown).isInstanceOf(JsonValidatingException.class);

        JsonValidatingException e = (JsonValidatingException)thrown;
        printer.handleProblems(e.getProblems());
    }

    @ParameterizedTest
    @MethodSource("unsupportedVersions")
    public void reader_throwsIfUnsupportedVersion(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, true);
        Reader in = createReader(schemaName);
        JsonSchemaReader reader = factory.createSchemaReader(in);

        Throwable thrown = catchThrowable(()->{
            reader.read();
        });

        reader.close();

        assertThat(thrown).isInstanceOf(JsonValidatingException.class);

        JsonValidatingException e = (JsonValidatingException)thrown;
        printer.handleProblems(e.getProblems());
    }

    public static Stream<Arguments> matchedVersions() {
        return Stream.of(
                Arguments.of(SpecVersion.DRAFT_04, "detect-draft04.schema.json"),
                Arguments.of(SpecVersion.DRAFT_06, "detect-draft06.schema.json"),
                Arguments.of(SpecVersion.DRAFT_07, "detect-draft07.schema.json")
                );
    }

    @ParameterizedTest
    @MethodSource("matchedVersions")
    public void streamWithoutDetection_succeeds(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, false);
        InputStream in = createStream(schemaName);
        try (JsonSchemaReader reader = factory.createSchemaReader(in)) {
            reader.read();
        }
    }

    @ParameterizedTest
    @MethodSource("matchedVersions")
    public void readerWithoutDetection_succeeds(SpecVersion defaultVersion, String schemaName) {
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
                Arguments.of(SpecVersion.DRAFT_07, "detect-draft06.schema.json")
                );
    }

    @ParameterizedTest
    @MethodSource("unmatchedVersions")
    public void streamWithoutDetection_throws(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, false);
        InputStream in = createStream(schemaName);
        JsonSchemaReader reader = factory.createSchemaReader(in);

        Throwable thrown = catchThrowable(()->{
            reader.read();
        });

        reader.close();

        assertThat(thrown).isInstanceOf(JsonValidatingException.class);

        JsonValidatingException e = (JsonValidatingException)thrown;
        printer.handleProblems(e.getProblems());
    }

    @ParameterizedTest
    @MethodSource("unmatchedVersions")
    public void readerWithoutDetection_throws(SpecVersion defaultVersion, String schemaName) {
        JsonSchemaReaderFactory factory = createFactory(defaultVersion, false);
        Reader in = createReader(schemaName);
        JsonSchemaReader reader = factory.createSchemaReader(in);

        Throwable thrown = catchThrowable(()->{
            reader.read();
        });

        reader.close();

        assertThat(thrown).isInstanceOf(JsonValidatingException.class);

        JsonValidatingException e = (JsonValidatingException)thrown;
        printer.handleProblems(e.getProblems());
    }

    /* helpers */

    private static JsonSchemaReaderFactory createFactory(SpecVersion defaultVersion, boolean detection) {
        return service.createSchemaReaderFactoryBuilder()
                .withDefaultSpecVersion(defaultVersion)
                .withStrictWithKeywords(true)
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
