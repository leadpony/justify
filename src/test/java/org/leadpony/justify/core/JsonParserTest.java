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

import static org.leadpony.justify.core.JsonAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

import org.junit.Test;

/**
 * @author leadpony
 */
public class JsonParserTest {
    
    private static final String PERSON_SCHEMA =
            "{" +
            "\"type\":\"object\"," +
            "\"properties\":{" +
            "\"name\": {\"type\":\"string\"}," +
            "\"age\": {\"type\":\"integer\", \"minimum\":0}" +
            "}," + 
            "\"required\":[\"name\"]" +
            "}";        

    @Test
    public void hasNext_returnsTrueAtFirst() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        JsonParser sut = createParser(instance, schema);
        boolean actual = sut.hasNext();
        sut.close();
        
        assertThat(actual).isTrue();
    }
    
    @Test
    public void hasNext_returnsFalseAtLast() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        JsonParser sut = createParser(instance, schema);
        sut.next();
        boolean actual = sut.hasNext();
        sut.close();
        
        assertThat(actual).isFalse();
    }
    
    @Test
    public void next_returnsAllEvents() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonParser parser = createParser(instance);
        List<JsonParser.Event> expected = new ArrayList<>();
        while (parser.hasNext()) {
            expected.add(parser.next());
        }
        parser.close();

        JsonParser sut = createParser(instance, schema);
        List<JsonParser.Event> actual = new ArrayList<>();
        while (sut.hasNext()) {
            actual.add(sut.next());
        }
        sut.close();
        
        assertThat(actual).containsExactlyElementsOf(expected);
        assertValid(sut);
    }

    @Test
    public void next_returnsAllEventsEventIfInvalid() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": \"young\"}";
        
        JsonParser parser = createParser(instance);
        List<JsonParser.Event> expected = new ArrayList<>();
        while (parser.hasNext()) {
            expected.add(parser.next());
        }
        parser.close();

        JsonParser sut = createParser(instance, schema);
        List<JsonParser.Event> actual = new ArrayList<>();
        while (sut.hasNext()) {
            actual.add(sut.next());
        }
        sut.close();
        
        assertThat(actual).containsExactlyElementsOf(expected);
        assertInvalid(sut);
    }
    
    @Test
    public void next_throwsExceptionIfDoesNotHaveNext() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";

        JsonParser sut = createParser(instance, schema);
        sut.next();
        Throwable thrown = catchThrowable(()->sut.next());
        sut.close();
        
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
        assertValid(sut);
    }
    
    @Test
    public void getLocation_returnsLocation() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";
        
        JsonParser parser = createParser(instance);
        parser.next();
        JsonLocation expected = parser.getLocation();
        parser.close();
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonLocation actual = sut.getLocation();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertValid(sut);
    }
    
    @Test
    public void getString_returnsString() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";

        JsonParser sut = createParser(instance, schema);
        sut.next();
        String actual = sut.getString();
        sut.close();
        
        assertThat(actual).isEqualTo("foo");
        assertValid(sut);
    }

    @Test
    public void getInt_returnsInteger() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        JsonParser sut = createParser(instance, schema);
        sut.next();
        int actual = sut.getInt();
        sut.close();
        
        assertThat(actual).isEqualTo(42);
        assertValid(sut);
    }

    @Test
    public void getLong_returnsLong() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "9223372036854775807";

        JsonParser sut = createParser(instance, schema);
        sut.next();
        long actual = sut.getLong();
        sut.close();
        
        assertThat(actual).isEqualTo(9223372036854775807L);
        assertValid(sut);
    }

    @Test
    public void getBigDecimal_returnsBigDecimal() {
        String schema = "{\"type\":\"number\"}";
        String instance = "12.34";

        JsonParser sut = createParser(instance, schema);
        sut.next();
        BigDecimal actual = sut.getBigDecimal();
        sut.close();
        
        assertThat(actual).isEqualTo(new BigDecimal("12.34"));
        assertValid(sut);
    }

    @Test
    public void getValue_returnsString() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";
        
        JsonParser parser = createParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertValid(sut);
    }

    @Test
    public void getValue_returnsInteger() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";
        
        JsonParser parser = createParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertValid(sut);
    }

    @Test
    public void getValue_returnsNumber() {
        String schema = "{\"type\":\"number\"}";
        String instance = "3.14";
        
        JsonParser parser = createParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertValid(sut);
    }

    @Test
    public void getValue_returnsTrue() {
        String schema = "{\"type\":\"boolean\"}";
        String instance = "true";
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(JsonValue.TRUE);
        assertValid(sut);
    }

    @Test
    public void getValue_returnsFalse() {
        String schema = "{\"type\":\"boolean\"}";
        String instance = "false";
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(JsonValue.FALSE);
        assertValid(sut);
    }

    @Test
    public void getValue_returnsNull() {
        String schema = "{\"type\":\"null\"}";
        String instance = "null";
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(JsonValue.NULL);
        assertValid(sut);
    }

    @Test
    public void getValue_returnsObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonParser parser = createParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();

        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertValid(sut);
    }

    @Test
    public void getValue_returnsArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";
        
        JsonParser parser = createParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();

        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertValid(sut);
    }

    @Test
    public void getObject_returnsObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonParser parser = createParser(instance);
        parser.next();
        JsonObject expected = parser.getObject();
        parser.close();

        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonObject actual = sut.getObject();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertValid(sut);
    }
    
    @Test
    public void getObject_throwsExceptionIfNotObject() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";

        JsonParser sut = createParser(instance, schema);
        sut.next();
        Throwable thrown = catchThrowable(()->sut.getObject());
        sut.close();
        
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertValid(sut);
    }

    @Test
    public void getArray_returnsArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";
        
        JsonParser parser = createParser(instance);
        parser.next();
        JsonArray expected = parser.getArray();
        parser.close();

        JsonParser sut = createParser(instance, schema);
        sut.next();
        JsonArray actual = sut.getArray();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertValid(sut);
    }

    @Test
    public void getArray_throwsExceptionIfNotArray() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";

        JsonParser sut = createParser(instance, schema);
        sut.next();
        Throwable thrown = catchThrowable(()->sut.getArray());
        sut.close();
        
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertValid(sut);
    }

    @Test
    public void skipObject_skipsObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        sut.skipObject();
        
        assertThat(sut.hasNext()).isFalse();
        assertValid(sut);

        sut.close();
    }

    @Test
    public void skipObject_skipsEvenIfNotWellFormed() {
        String schema = "{\"type\":\"object\"}";
        String instance = "{";
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        sut.skipObject();
        
        assertThat(sut.hasNext()).isFalse();
        assertValid(sut);

        sut.close();
    }

    @Test
    public void skipArray_skipsArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        sut.skipArray();
        
        assertThat(sut.hasNext()).isFalse();
        sut.close();
        assertValid(sut);
    }
    
    @Test
    public void skipArray_skipsEvenIfNotWellFormed() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[";
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        sut.skipArray();
        
        assertThat(sut.hasNext()).isFalse();
        assertValid(sut);

        sut.close();
    }

    @Test
    public void getArrayStream_returnsArrayStream() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[true,false,null]";
        
        JsonParser sut = createParser(instance, schema);
        sut.next();
        Stream<JsonValue> actual = sut.getArrayStream();
        
        assertThat(actual).containsExactly(JsonValue.TRUE, JsonValue.FALSE, JsonValue.NULL);
        assertValid(sut);
        sut.close();
    }
    
    @Test
    public void getObjectStream_returnsObjectStream() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonParser parser = createParser(instance);
        parser.next();
        Stream<Map.Entry<String, JsonValue>> expected = parser.getObjectStream();
        parser.close();

        JsonParser sut = createParser(instance, schema);
        sut.next();
        Stream<Map.Entry<String, JsonValue>> actual = sut.getObjectStream();

        assertThat(actual).containsExactlyElementsOf(
                expected.collect(Collectors.toList()));
        assertValid(sut);
        sut.close();
    }
    
    @Test
    public void getValueStream_returnsValueStream() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";
        
        JsonParser sut = createParser(instance, schema);
        Stream<JsonValue> actual = sut.getValueStream();
        
        assertThat(actual).containsExactly(Json.createValue(42));
        assertValid(sut);
        sut.close();
    }

    private JsonParser createParser(String instance) {
        return Json.createParser(new StringReader(instance));
    }
    
    private JsonParser createParser(String instance, String schema) {
        JsonValidatorFactory factory = JsonValidatorFactory.newFactory(readSchema(schema));
        return factory.createParser(new StringReader(instance));
    }
    
    private JsonSchema readSchema(String schema) {
        return JsonSchemaReader.readFrom(new StringReader(schema));
    }
    
    private static void assertValid(JsonParser parser) {
        JsonValidator validator = (JsonValidator)parser;
        assertThat(validator.hasProblem()).isFalse();
    }

    private static void assertInvalid(JsonParser parser) {
        JsonValidator validator = (JsonValidator)parser;
        assertThat(validator.hasProblem()).isTrue();
        validator.getProblems().forEach(System.out::println);
    }
}
