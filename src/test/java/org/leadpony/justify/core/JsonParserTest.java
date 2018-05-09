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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.Json;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author leadpony
 */
@RunWith(Parameterized.class)
public class JsonParserTest extends AbstractSpecTest {

    private static final String[] NAMES = {
        "/additional/person.json"
    };
   
    public JsonParserTest(String name, int testIndex, String description, Fixture fixture) {
        super(name, testIndex, description, fixture);
    }

    @Parameters(name = "{0}@{1}: {2}")
    public static Iterable<Object[]> parameters() {
        return parameters(NAMES);
    }
    
    @Test
    public void testParserEvent() {
        List<Event> events = new ArrayList<>();
        JsonParser parser = createValidator();
        while (parser.hasNext()) {
            events.add(parser.next());
        }
        parser.close();
        Collection<Event> expected = getExpectedEvents(fixture.instance());
        
        assertThat(events).containsExactlyElementsOf(expected);
    }

    private Collection<Event> getExpectedEvents(JsonValue instance) {
        List<Event> events = new ArrayList<>();
        StringReader reader = new StringReader(instance.toString());
        try (JsonParser parser = Json.createParser(reader)) {
            while (parser.hasNext()) {
                events.add(parser.next());
            }
        }
        return events;
    }
}
