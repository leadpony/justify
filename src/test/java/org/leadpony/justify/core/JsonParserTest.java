/*
 * Copyright 2018 the Justify authors.
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

package org.leadpony.justify.core;

import static org.assertj.core.api.Assertions.*;
import static org.leadpony.justify.core.Resources.newInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author leadpony
 */
public class JsonParserTest {
    
    private static JsonValidatorFactory factory;
    
    @BeforeClass
    public static void setUpOnce() throws IOException {
        JsonSchema schema = null;
        try (InputStream in = newInputStream("/example/person/schema.json")) {
            schema = JsonSchema.load(in);
        }
        factory = JsonValidatorFactory.newFactory(schema);
    }
    
    @Test
    public void parse_parseValidJson() throws IOException {
        String name = "/example/person/person-1-ok.json";
        List<Event> actual = new ArrayList<>();
        JsonParser parser = parseAndValidate(name, actual);
        JsonValidator validator = (JsonValidator)parser;
        Collection<Event> expected = getExpectedEvents(name);
        
        assertThat(actual).containsExactlyElementsOf(expected);
        assertThat(validator.hasProblem()).isFalse();
        assertThat(validator.problems()).isEmpty();
    }
    
    @Test
    public void parse_detectTypeMismatch() throws IOException {
        String name = "/example/person/person-2-ng.json";
        List<Event> actual = new ArrayList<>();
        JsonParser parser = parseAndValidate(name, actual);
        JsonValidator validator = (JsonValidator)parser;
        Collection<Event> expected = getExpectedEvents(name);
        
        assertThat(actual).containsExactlyElementsOf(expected);
        assertThat(validator.hasProblem()).isTrue();
        assertThat(validator.problems()).hasSize(1);
        printProblems(validator.problems());
    }

    @Test
    public void parse_detectIllegalNumber() throws IOException {
        String name = "/example/person/person-3-ng.json";
        List<Event> actual = new ArrayList<>();
        JsonParser parser = parseAndValidate(name, actual);
        JsonValidator validator = (JsonValidator)parser;
        Collection<Event> expected = getExpectedEvents(name);
        
        assertThat(actual).containsExactlyElementsOf(expected);
        assertThat(validator.hasProblem()).isTrue();
        assertThat(validator.problems()).hasSize(1);
        printProblems(validator.problems());
    }

    private JsonParser parseAndValidate(String name, Collection<Event> events) throws IOException {
        try (InputStream in = newInputStream(name)) {
            try (JsonParser parser = factory.createParser(in)) {
                while (parser.hasNext()) {
                    events.add(parser.next());
                }
                return parser;
            }
        }
    }

    private Collection<Event> getExpectedEvents(String name) throws IOException {
        List<Event> events = new ArrayList<>();
        try (InputStream in = newInputStream(name)) {
            try (JsonParser parser = Json.createParser(in)) {
                while (parser.hasNext()) {
                    events.add(parser.next());
                }
            }
        }
        return events;
    }
    
    private static void printProblems(Iterable<Problem> problem) {
        problem.forEach(System.err::println);
    }
}
