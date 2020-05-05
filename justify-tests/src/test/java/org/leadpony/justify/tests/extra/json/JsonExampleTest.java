/*
 * Copyright 2018-2020 the Justify authors.
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
package org.leadpony.justify.tests.extra.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.tests.helper.JsonExample;
import org.leadpony.justify.tests.helper.Loggable;
import org.leadpony.justify.tests.helper.ValidationServiceType;

/**
 * A test class for testing the examples provided by json-schema.org.
 *
 * @author leadpony
 */
public class JsonExampleTest implements Loggable {

    private JsonValidationService service;

    @BeforeEach
    public void setUp() {
        service = createService();
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateJsonAgainstJsonSchemaUsingJsonParser(JsonExample example) {
        validateUsingJsonParser(example.getJsonAsStream(), example.getJsonSchemaAsStream(), example.isValid());
    }

    protected void validateUsingJsonParser(InputStream stream, InputStream schemaStream, boolean valid) {
        JsonSchema schema = service.readSchema(schemaStream);

        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;
        try (JsonParser parser = service.createParser(stream, schema, handler)) {
            while (parser.hasNext()) {
                parser.next();
            }
        }

        printProblems(problems);

        assertThat(problems.isEmpty()).isEqualTo(valid);
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateJsonAgainstJsonSchemaUsingJsonReader(JsonExample example) {
        validateUsingJsonReader(example.getJsonAsStream(), example.getJsonSchemaAsStream(), example.isValid());
    }

    protected void validateUsingJsonReader(InputStream stream, InputStream schemaStream, boolean valid) {
        JsonSchema schema = service.readSchema(schemaStream);

        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;
        JsonValue value = null;
        try (JsonReader reader = service.createReader(stream, schema, handler)) {
            value = reader.readValue();
        }

        printProblems(problems);

        assertThat(value).isNotNull();
        assertThat(problems.isEmpty()).isEqualTo(valid);
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateUsingJsonParserFromValue(JsonExample example) {
        JsonSchema schema = service.readSchema(example.getJsonSchemaAsStream());
        JsonValue value = example.getAsJson();

        List<Problem> problems = validateValue(value, schema);

        assertThat(problems.isEmpty()).isEqualTo(example.isValid());
    }

    private List<Problem> validateValue(JsonValue value, JsonSchema schema) {
        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;

        JsonParserFactory factory = service.createParserFactory(null, schema, p -> handler);
        JsonParser parser = null;
        switch (value.getValueType()) {
        case ARRAY:
            parser = factory.createParser((JsonArray) value);
            break;
        case OBJECT:
            parser = factory.createParser((JsonObject) value);
            break;
        default:
            throw new IllegalArgumentException();
        }

        while (parser.hasNext()) {
            parser.next();
        }

        parser.close();

        printProblems(problems);

        return problems;
    }

    protected JsonValidationService createService() {
        return ValidationServiceType.DEFAULT.getService();
    }
}
