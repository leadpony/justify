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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.json.JsonReader;
import javax.json.stream.JsonLocation;

import org.junit.jupiter.api.Test;

/**
 * A test class for testing the {@link Problem} implementation.
 *
 * @author leadpony
 */
public class ProblemTest {

    private static final JsonValidationService SERVICE = JsonValidationServices.get();

    private static final String SCHEMA = "{ \"properties\": { \"foo\": { \"type\": \"string\" } } }";
    private static final String INSTANCE = "{\n\"foo\": 42\n}";

    private static Problem createProblem(String schemaDoc, String instanceDoc) {
        JsonSchema schema = SERVICE.readSchema(new StringReader(schemaDoc));
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;
        try (JsonReader reader = SERVICE.createReader(new StringReader(instanceDoc), schema, handler)) {
            reader.readValue();
        }
        return problems.get(0);
    }

    @Test
    public void getMessageShouldReturnMessage() {
        Problem problem = createProblem(SCHEMA, INSTANCE);
        String message = problem.getMessage(Locale.ROOT);

        assertThat(message).isEqualTo("The value must be of string type, but actual type is integer.");
    }

    @Test
    public void getMessageShouldReturnDifferentMessageByLocale() {
        Problem problem = createProblem(SCHEMA, INSTANCE);
        String message1 = problem.getMessage(Locale.ENGLISH);
        String message2 = problem.getMessage(Locale.JAPANESE);

        assertThat(message1).isNotEmpty();
        assertThat(message2).isNotEmpty();
        assertThat(message1).isNotEqualTo(message2);
    }

    @Test
    public void getContextualMessageShouldReturnMessageIncludingLocation() {
        Problem problem = createProblem(SCHEMA, INSTANCE);
        String message = problem.getContextualMessage(Locale.ROOT);

        assertThat(message).startsWith("[2,9]");
    }

    @Test
    public void getLocationShouldReturnLocation() {
        Problem problem = createProblem(SCHEMA, INSTANCE);
        JsonLocation location = problem.getLocation();

        assertThat(location.getLineNumber()).isEqualTo(2);
        assertThat(location.getColumnNumber()).isEqualTo(9);
    }

    @Test
    public void getPointerShouldReturnPointer() {
        Problem problem = createProblem(SCHEMA, INSTANCE);
        String pointer = problem.getPointer();

        assertThat(pointer).isEqualTo("/foo");
    }

    @Test
    public void getSchemaShouldReturnSchema() {
        Problem problem = createProblem(SCHEMA, INSTANCE);
        JsonSchema schema = problem.getSchema();

        assertThat(schema).hasToString("{\"type\":\"string\"}");
    }

    @Test
    public void getSchemaShouldReturnBooleanSchema() {
        Problem problem = createProblem("false", INSTANCE);
        JsonSchema schema = problem.getSchema();

        assertThat(schema).isSameAs(JsonSchema.FALSE);
    }

    @Test
    public void getKeywordShouldReturnKeyword() {
        Problem problem = createProblem(SCHEMA, INSTANCE);
        String keyword = problem.getKeyword();

        assertThat(keyword).isEqualTo("type");
    }

    @Test
    public void getKeywordShouldReturnNull() {
        Problem problem = createProblem("false", INSTANCE);
        String keyword = problem.getKeyword();

        assertThat(keyword).isNull();
    }

    @Test
    public void isResolvableShouldReturnTrue() {
        Problem problem = createProblem(SCHEMA, INSTANCE);
        boolean actual = problem.isResolvable();

        assertThat(actual).isTrue();
    }

    @Test
    public void isResolvableShouldReturnFalse() {
        Problem problem = createProblem("false", INSTANCE);
        boolean actual = problem.isResolvable();

        assertThat(actual).isFalse();
    }
}
