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
package org.leadpony.justify.internal.base.json;

import static org.assertj.core.api.Assertions.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A test class for {@link JsonValueParser}.
 *
 * @author leadpony
 */
public class JsonValueParserTest {

    public static Stream<Arguments> fixtures() {
        InputStream in = JsonValueParserTest.class.getResourceAsStream("jsonvalue.json");
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray().stream()
                .map(JsonValue::asJsonObject)
                .map(object->{
                    JsonValue value = object.get("value");
                    List<Event> events = object.get("events")
                            .asJsonArray().stream()
                            .map(event->((JsonString)event).getString())
                            .map(Event::valueOf)
                            .collect(Collectors.toList());
                    return Arguments.of(value, events);
                });
        }
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    public void next_shouldReturnEvent(JsonValue value, List<Event> events) {
        List<Event> actual = new ArrayList<>();
        JsonParser parser = createParser(value);
        while (parser.hasNext()) {
            actual.add(parser.next());
        }

        assertThat(actual).containsExactlyElementsOf(events);
    }

    private static JsonParser createParser(JsonValue value) {
        switch (value.getValueType()) {
        case ARRAY:
            return new JsonValueParser((List<JsonValue>)(JsonArray) value);
            //return DefaultValueParser.fillingWith((JsonArray) value, provider);
        case OBJECT:
            return new JsonValueParser((Map<String, JsonValue>)(JsonObject) value);
            //return DefaultValueParser.fillingWith((JsonObject) value, provider);
        default:
            throw new UnsupportedOperationException();
        }
    }
}
