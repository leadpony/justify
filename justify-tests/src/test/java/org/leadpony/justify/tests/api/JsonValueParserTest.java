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

import java.util.ArrayList;
import java.util.List;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.json.stream.JsonParserFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.jsonp.testsuite.tests.AbstractJsonValueParserTest;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ValidationConfig;
import org.leadpony.justify.tests.helper.Loggable;
import org.leadpony.justify.tests.helper.ValidationServiceType;

/**
 * Tests for {@link JsonParser} which parses in-memory JSON structures.
 *
 * @author leadpony
 */
public class JsonValueParserTest extends AbstractJsonValueParserTest implements Loggable {

    private static final JsonValidationService SERVICE = ValidationServiceType.DEFAULT.getService();
    private static final ProblemHandler PRINTER = SERVICE.createProblemPrinter(LOG::info);

    @ParameterizedTest
    @EnumSource(ParserEventTestCase.class)
    public void nextShouldReturnEventEvenIfInvalid(ParserEventTestCase test) {
        JsonParser parser = createParser(test.value, JsonSchema.FALSE);

        List<Event> actual = new ArrayList<>();
        while (parser.hasNext()) {
            actual.add(parser.next());
        }
        parser.close();

        assertThat(actual).containsExactly(test.events);
    }

    @Override
    protected JsonParser createParser(JsonStructure value) {
        return createParser(value, JsonSchema.TRUE);
    }

    private static JsonParser createParser(JsonStructure value, JsonSchema schema) {
        ValidationConfig config = SERVICE.createValidationConfig();
        config.withSchema(schema);
        config.withProblemHandler(PRINTER);
        JsonParserFactory factory = SERVICE.createParserFactory(config.getAsMap());
        switch (value.getValueType()) {
        case ARRAY:
            return factory.createParser((JsonArray) value);
        case OBJECT:
            return factory.createParser((JsonObject) value);
        default:
            return null;
        }
    }
}
