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
package org.leadpony.justify.tests.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.tests.helper.ApiTest;
import org.leadpony.justify.tests.helper.ProblemPrinter;
import org.leadpony.justify.tests.helper.JsonExample;

/**
 * A test class for testing the examples provided by json-schema.org.
 *
 * @author leadpony
 */
@ApiTest
public class KnownExampleTest {

    private static JsonValidationService service;
    private static ProblemPrinter printer;

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateUsingJsonParser(JsonExample example) {
        JsonSchema schema = service.readSchema(example.getSchemaAsStream());
        InputStream in = example.getAsStream();

        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;
        try (JsonParser parser = service.createParser(in, schema, handler)) {
            while (parser.hasNext()) {
                parser.next();
            }
        }

        printer.print(problems);

        assertThat(problems.isEmpty()).isEqualTo(example.isValid());
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateUsingJsonReader(JsonExample example) {
        JsonSchema schema = service.readSchema(example.getSchemaAsStream());
        InputStream in = example.getAsStream();

        List<Problem> problems = new ArrayList<>();
        ProblemHandler handler = problems::addAll;
        JsonValue value = null;
        try (JsonReader reader = service.createReader(in, schema, handler)) {
            value = reader.readValue();
        }

        printer.print(problems);

        assertThat(value).isNotNull();
        assertThat(problems.isEmpty()).isEqualTo(example.isValid());
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateUsingJsonParserFromValue(JsonExample example) {
        JsonSchema schema = service.readSchema(example.getSchemaAsStream());
        JsonValue value = example.getAsJson();

        List<Problem> problems = validateValue(value, schema);

        assertThat(problems.isEmpty()).isEqualTo(example.isValid());
    }

    private static List<Problem> validateValue(JsonValue value, JsonSchema schema) {
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

        printer.print(problems);

        return problems;
    }
}
