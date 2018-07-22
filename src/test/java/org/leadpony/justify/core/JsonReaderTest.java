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

import static org.leadpony.justify.core.JsonSchemas.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.leadpony.justify.Loggers;

/**
 * Tests for {@link JsonReader} validating JSON instance. 
 * 
 * @author leadpony
 */
public class JsonReaderTest {
   
    private static final Logger log = Loggers.getLogger(JsonReaderTest.class);
    
    @Test
    public void read_readArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";
        
        JsonReader reader = newReader(instance);
        JsonStructure expected = reader.read();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(instance, schema, problems::addAll);
        JsonStructure actual = sut.read();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void read_readObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonReader reader = newReader(instance);
        JsonStructure expected = reader.read();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(instance, schema, problems::addAll);
        JsonStructure actual = sut.read();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void readArray_readsArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";
        
        JsonReader reader = newReader(instance);
        JsonArray expected = reader.readArray();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(instance, schema, problems::addAll);
        JsonArray actual = sut.readArray();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void readObject_readsObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonReader reader = newReader(instance);
        JsonObject expected = reader.readObject();
        reader.close();

        List<Problem> problems = new ArrayList<>();
        JsonReader sut = newReader(instance, schema, problems::addAll);
        JsonObject actual = sut.readObject();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }
    
    @RunWith(Parameterized.class)
    public static class ReadValueTest {
        
        private final String schema;
        private final String instance;
        private final boolean valid;
        
        public ReadValueTest(String schema, String instance, boolean valid) {
            this.schema = schema;
            this.instance = instance;
            this.valid = valid;
        }
   
        @Parameters
        public static Iterable<Object[]> parameters() {
            return Arrays.asList(new Object[][] {
                { "{\"type\":\"boolean\"}", "true", true },
                { "{\"type\":\"string\"}", "true", false },
                { "{\"type\":\"boolean\"}", "false", true },
                { "{\"type\":\"string\"}", "false", false },
                { "{\"type\":\"null\"}", "null", true },
                { "{\"type\":\"string\"}", "null", false },
                { "{\"type\":\"string\"}", "\"foo\"", true },
                { "{\"type\":\"integer\"}", "\"foo\"", false },
                { "{\"type\":\"integer\"}", "42", true },
                { "{\"type\":\"integer\"}", "9223372036854775807", true },
                { "{\"type\":\"string\"}", "42", false },
                { "{\"type\":\"number\"}", "3.14", true },
                { "{\"type\":\"string\"}", "3.14", false },
                { INTEGER_ARRAY_SCHEMA, "[1,2,3]", true },
                { INTEGER_ARRAY_SCHEMA, "[\"foo\",\"bar\"]", false },
                { PERSON_SCHEMA, "{\"name\":\"John Smith\", \"age\": 46}", true },
                { PERSON_SCHEMA, "{\"name\":\"John Smith\", \"age\": \"46\"}", false },
            });
        }
        
        @Test
        public void readValue_readsValue() {
            JsonReader reader = newReader(instance);
            JsonValue expected = reader.readValue();
            reader.close();

            List<Problem> problems = new ArrayList<>();
            JsonReader sut = newReader(instance, schema, problems::addAll);
            JsonValue actual = sut.readValue();
            sut.close();
            
            assertThat(actual).isEqualTo(expected);
            assertThat(problems.isEmpty()).isEqualTo(valid);
            printProblems(problems);
        }

        @Test
        public void readValue_throwsExceptionIfInvalid() {
            JsonReader reader = newReader(instance);
            JsonValue expected = reader.readValue();
            reader.close();

            List<Problem> problems = new ArrayList<>();
            JsonReader sut = newReader(instance, schema, null);
            JsonValue actual = null;
            try {
                actual = sut.readValue();
            } catch (JsonValidatingException e) {
                problems.addAll(e.getProblems());
            }
            sut.close();
            
            if (actual != null) {
                assertThat(actual).isEqualTo(expected);
                assertThat(problems).isEmpty();
            } else {
                assertThat(problems).isNotEmpty();
            }
            printProblems(problems);
        }
    }
    
    private static void printProblems(List<Problem> problems) {
        if (!problems.isEmpty()) {
            Jsonv.createProblemPrinter(log::info).accept(problems);
        }
    }
}
