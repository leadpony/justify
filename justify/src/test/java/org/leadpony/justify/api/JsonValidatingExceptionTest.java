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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.junit.jupiter.params.ParameterizedTest;
import org.leadpony.justify.test.helper.JsonSource;

/**
 * @author leadpony
 */
public class JsonValidatingExceptionTest {

    private static final Logger LOG = Logger.getLogger(JsonValidatingExceptionTest.class.getName());
    private static final JsonValidationService SERVICE = JsonValidationService.newInstance();

    /**
     * @author leadpony
     */
    public static class ExceptionTestCase {

        public String title;
        public JsonValue schema;
        public int problems;
        public int lines;

        @Override
        public String toString() {
            return title;
        }
    }

    /**
     * @author leadpony
     */
    public static class InstanceExceptionTestCase extends ExceptionTestCase {
        public JsonValue data;
    }

    @ParameterizedTest
    @JsonSource("jsonvalidatingexceptiontest-schema.json")
    public void getProblemsShouldReturnNumberOfTopLevelProblems(ExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema);
        assertThat(thrown.getProblems()).hasSize(test.problems);
    }

    @ParameterizedTest
    @JsonSource("jsonvalidatingexceptiontest-instance.json")
    public void getProblemsShouldReturnNumberOfTopLevelProblems(InstanceExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema, test.data);
        assertThat(thrown.getProblems()).hasSize(test.problems);
    }

    @ParameterizedTest
    @JsonSource("jsonvalidatingexceptiontest-schema.json")
    public void getMessageShouldReturnMessageFromAllProblems(ExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema);

        assertThat(thrown).isNotNull();

        String message = thrown.getMessage();
        LOG.info(message);

        assertThat(message.split("\n")).hasSize(test.lines);
    }

    @ParameterizedTest
    @JsonSource("jsonvalidatingexceptiontest-instance.json")
    public void getMessageShouldReturnMessageFromAllProblems(InstanceExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema, test.data);

        assertThat(thrown).isNotNull();

        String message = thrown.getMessage();
        LOG.info(message);

        assertThat(message.split("\n")).hasSize(test.lines);
    }

    @ParameterizedTest
    @JsonSource("jsonvalidatingexceptiontest-schema.json")
    public void printProblemsShouldOutputToPrintStreamAsExpected(ExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema);

        assertThat(thrown).isNotNull();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thrown.printProblems(new PrintStream(stream));
        String message = stream.toString();
        LOG.info(message);

        assertThat(message.split("\n")).hasSize(test.lines);
    }

    @ParameterizedTest
    @JsonSource("jsonvalidatingexceptiontest-instance.json")
    public void printProblemsShouldOutputToPrintStreamAsExpected(InstanceExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema, test.data);

        assertThat(thrown).isNotNull();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thrown.printProblems(new PrintStream(stream));
        String message = stream.toString();
        LOG.info(message);

        assertThat(message.split("\n")).hasSize(test.lines);
    }

    @ParameterizedTest
    @JsonSource("jsonvalidatingexceptiontest-schema.json")
    public void printProblemsShouldOutputToPrintWriterAsExpected(ExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema);

        assertThat(thrown).isNotNull();

        StringWriter writer = new StringWriter();
        thrown.printProblems(new PrintWriter(writer));
        String message = writer.toString();
        LOG.info(message);

        assertThat(message.split("\n")).hasSize(test.lines);
    }

    @ParameterizedTest
    @JsonSource("jsonvalidatingexceptiontest-instance.json")
    public void printProblemsShouldOutputToPrintWriterAsExpected(InstanceExceptionTestCase test) {
        JsonValidatingException thrown = catchJsonValidatingException(test.schema, test.data);

        assertThat(thrown).isNotNull();

        StringWriter writer = new StringWriter();
        thrown.printProblems(new PrintWriter(writer));
        String message = writer.toString();
        LOG.info(message);

        assertThat(message.split("\n")).hasSize(test.lines);
    }

    private static JsonValidatingException catchJsonValidatingException(JsonValue schema) {
        try {
            SERVICE.readSchema(new StringReader(schema.toString()));
            return null;
        } catch (JsonValidatingException e) {
            return e;
        }
    }

    private static JsonValidatingException catchJsonValidatingException(JsonValue schema, JsonValue data) {
        JsonSchema s = SERVICE.readSchema(new StringReader(schema.toString()));
        try (JsonReader reader = SERVICE.createReader(
                new StringReader(data.toString()), s, ProblemHandler.throwing())) {
            reader.read();
            return null;
        } catch (JsonValidatingException e) {
            return e;
        }
    }
}
