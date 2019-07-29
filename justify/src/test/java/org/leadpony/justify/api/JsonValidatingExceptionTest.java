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
import java.io.StringReader;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.json.JsonReader;
import javax.json.JsonValue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.test.helper.JsonResource;

/**
 * @author leadpony
 */
public class JsonValidatingExceptionTest {

    private static final Logger LOG = Logger.getLogger(JsonValidatingExceptionTest.class.getName());
    private static final JsonValidationService SERVICE = JsonValidationService.newInstance();
    private static final ProblemHandler PRINTER = SERVICE.createProblemPrinter(LOG::info);

    /**
     * @author leadpony
     */
    private static class ExceptionTestCase {

        final JsonValue schema;
        final JsonValue data;
        final int problems;
        final int lines;

        ExceptionTestCase(JsonValue schema, JsonValue data, int problems, int lines) {
            this.schema = schema;
            this.data = data;
            this.problems = problems;
            this.lines = lines;
        }
    }

    public static Stream<ExceptionTestCase> provideTestCases() {
        return JsonResource.of("/org/leadpony/justify/api/jsonvalidatingexception.json")
            .asObjectStream()
            .map(object -> new ExceptionTestCase(
                    object.get("schema"),
                    object.get("data"),
                    object.getInt("problems"),
                    object.getInt("lines")));
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    public void getProblemsShouldReturnTopLevelProblems(ExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema, test.data);

        assertThat(thrown.getProblems()).hasSize(test.problems);
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    @Disabled
    public void getMessageShouldReturnMessageFromAllProblems(ExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema, test.data);

        assertThat(thrown).isNotNull();

        PRINTER.handleProblems(thrown.getProblems());

        String message = thrown.getMessage();
        LOG.info(message);

        assertThat(message.split("\n")).hasSize(test.lines);
    }

    private JsonValidatingException catchJsonValidatingException(JsonValue schema, JsonValue data) {
        JsonSchema s = SERVICE.readSchema(new StringReader(schema.toString()));
        try (JsonReader reader = SERVICE.createReader(
                new StringReader(data.toString()), s, ProblemHandler.throwing())) {
            reader.read();
        } catch (JsonValidatingException e) {
            return e;
        }
        return null;
    }
}
