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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author leadpony
 */
public class JsonPointerBuilderTest {

    public static Stream<Arguments> fixtures() {
        InputStream in = JsonValueParserTest.class.getResourceAsStream("json-pointer.json");
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray().stream()
                .map(JsonValue::asJsonObject)
                .map(object->{
                    JsonValue value = object.get("data");
                    List<String> events = object.get("pointers")
                            .asJsonArray().stream()
                            .map(event->((JsonString)event).getString())
                            .collect(Collectors.toList());
                    return Arguments.of(value, events);
                });
        }
    }

    @ParameterizedTest
    @MethodSource("fixtures")
    public void testToPointer(JsonValue json, List<String> pointers) {
        String source = json.toString();
        List<String> actual = new ArrayList<>();
        JsonPointerBuilder builder = JsonPointerBuilder.newInstance();
        try (JsonParser parser = Json.createParser(new StringReader(source))) {
            while (parser.hasNext()) {
                Event event = parser.next();
                builder = builder.withEvent(event, parser);
                actual.add(builder.toPointer());
            }
        }
        assertThat(actual).containsExactlyElementsOf(pointers);
    }
}
