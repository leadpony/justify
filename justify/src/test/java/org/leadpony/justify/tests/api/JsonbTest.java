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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.spi.JsonProvider;

import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A test class for tesing validations using {@link Jsonb}.
 *
 * @author leadpony
 */
public class JsonbTest extends BaseTest {

    private static final String PERSON_SCHEMA = "{"
            + "\"type\":\"object\","
            + "\"properties\":{"
            + "\"name\": {\"type\":\"string\"},"
            + "\"age\": {\"type\":\"integer\", \"minimum\":0}"
            + "},"
            + "\"required\":[\"name\"]"
            + "}";

    @Test
    public void fromJsonShouldDeserialize() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";

        JsonSchema s = SERVICE.readSchema(new StringReader(schema));
        List<Problem> problems = new ArrayList<>();
        JsonProvider provider = SERVICE.createJsonProvider(s, parser -> problems::addAll);
        Jsonb jsonb = JsonbBuilder.newBuilder().withProvider(provider).build();
        Person person = jsonb.fromJson(instance, Person.class);

        assertThat(person.name).isEqualTo("John Smith");
        assertThat(person.age).isEqualTo(46);
        assertThat(problems).isEmpty();
    }

    @Test
    public void fromJsonShouldThrowExceptionIfInvalid() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": \"46\"}";

        JsonSchema s = SERVICE.readSchema(new StringReader(schema));
        List<Problem> problems = new ArrayList<>();
        JsonProvider provider = SERVICE.createJsonProvider(s, parser -> problems::addAll);
        Jsonb jsonb = JsonbBuilder.newBuilder().withProvider(provider).build();
        Person person = jsonb.fromJson(instance, Person.class);

        assertThat(person.name).isEqualTo("John Smith");
        assertThat(person.age).isEqualTo(46);
        assertThat(problems).isNotEmpty();

        print(problems);
    }

    /**
     * A POJO class.
     *
     * @author leadpony
     */
    public static class Person {
        public String name;
        public int age;
    }
}
