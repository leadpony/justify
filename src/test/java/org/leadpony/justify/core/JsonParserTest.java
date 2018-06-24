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
import static org.leadpony.justify.core.JsonSchemas.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
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
 * Tests for {@link JsonParser} validating JSON instance. 
 * 
 * @author leadpony
 */
public class JsonParserTest {
    
    private static final Consumer<Problem> EMPTY_HANDLER = problem->{};
    
    @Test
    public void hasNext_returnsTrueAtFirst() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        JsonParser sut = newParser(instance, schema, EMPTY_HANDLER);
        boolean actual = sut.hasNext();
        sut.close();
        
        assertThat(actual).isTrue();
    }
    
    @Test
    public void hasNext_returnsFalseAtLast() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        JsonParser sut = newParser(instance, schema, EMPTY_HANDLER);
        sut.next();
        boolean actual = sut.hasNext();
        sut.close();
        
        assertThat(actual).isFalse();
    }
    
    @Test
    public void next_returnsAllEvents() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonParser parser = newParser(instance);
        List<JsonParser.Event> expected = new ArrayList<>();
        while (parser.hasNext()) {
            expected.add(parser.next());
        }
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        List<JsonParser.Event> actual = new ArrayList<>();
        while (sut.hasNext()) {
            actual.add(sut.next());
        }
        sut.close();
        
        assertThat(actual).containsExactlyElementsOf(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void next_returnsAllEventsEventIfInvalid() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": \"young\"}";
        
        JsonParser parser = newParser(instance);
        List<JsonParser.Event> expected = new ArrayList<>();
        while (parser.hasNext()) {
            expected.add(parser.next());
        }
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        List<JsonParser.Event> actual = new ArrayList<>();
        while (sut.hasNext()) {
            actual.add(sut.next());
        }
        sut.close();
        
        assertThat(actual).containsExactlyElementsOf(expected);
        assertThat(problems).isNotEmpty();
    }
    
    @Test
    public void next_throwsExceptionIfDoesNotHaveNext() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        Throwable thrown = catchThrowable(()->sut.next());
        sut.close();
        
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
        assertThat(problems).isEmpty();
    }
    
    @Test
    public void getLocation_returnsLocation() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";
        
        JsonParser parser = newParser(instance);
        parser.next();
        JsonLocation expected = parser.getLocation();
        parser.close();
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonLocation actual = sut.getLocation();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }
    
    @Test
    public void getString_returnsString() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        String actual = sut.getString();
        sut.close();
        
        assertThat(actual).isEqualTo("foo");
        assertThat(problems).isEmpty();
    }

    @Test
    public void getInt_returnsInteger() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        int actual = sut.getInt();
        sut.close();
        
        assertThat(actual).isEqualTo(42);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getLong_returnsLong() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "9223372036854775807";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        long actual = sut.getLong();
        sut.close();
        
        assertThat(actual).isEqualTo(9223372036854775807L);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getBigDecimal_returnsBigDecimal() {
        String schema = "{\"type\":\"number\"}";
        String instance = "12.34";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        BigDecimal actual = sut.getBigDecimal();
        sut.close();
        
        assertThat(actual).isEqualTo(new BigDecimal("12.34"));
        assertThat(problems).isEmpty();
    }

    @Test
    public void getValue_returnsString() {
        String schema = "{\"type\":\"string\"}";
        String instance = "\"foo\"";
        
        JsonParser parser = newParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getValue_returnsInteger() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";
        
        JsonParser parser = newParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getValue_returnsNumber() {
        String schema = "{\"type\":\"number\"}";
        String instance = "3.14";
        
        JsonParser parser = newParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getValue_returnsTrue() {
        String schema = "{\"type\":\"boolean\"}";
        String instance = "true";
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(JsonValue.TRUE);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getValue_returnsFalse() {
        String schema = "{\"type\":\"boolean\"}";
        String instance = "false";
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(JsonValue.FALSE);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getValue_returnsNull() {
        String schema = "{\"type\":\"null\"}";
        String instance = "null";
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(JsonValue.NULL);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getValue_returnsObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonParser parser = newParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getValue_returnsArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";
        
        JsonParser parser = newParser(instance);
        parser.next();
        JsonValue expected = parser.getValue();
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonValue actual = sut.getValue();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getObject_returnsObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonParser parser = newParser(instance);
        parser.next();
        JsonObject expected = parser.getObject();
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonObject actual = sut.getObject();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }
    
    @Test
    public void getObject_throwsExceptionIfNotObject() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        Throwable thrown = catchThrowable(()->sut.getObject());
        sut.close();
        
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getObject_throwsExceptionIfNotClosed() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46";
        
        JsonParser parser = newParser(instance);
        parser.next();
        Throwable expected = catchThrowable(()->parser.getObject());
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        Throwable actual = catchThrowable(()->sut.getObject());
        sut.close();
        
        assertThat(actual)
            .hasSameClassAs(expected)
            .hasMessage(expected.getMessage());
    }

    @Test
    public void getArray_returnsArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";
        
        JsonParser parser = newParser(instance);
        parser.next();
        JsonArray expected = parser.getArray();
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        JsonArray actual = sut.getArray();
        sut.close();
        
        assertThat(actual).isEqualTo(expected);
        assertThat(problems).isEmpty();
    }
    
    @Test
    public void getArray_throwsExceptionIfNotArray() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        Throwable thrown = catchThrowable(()->sut.getArray());
        sut.close();
        
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertThat(problems).isEmpty();
    }

    @Test
    public void getArray_throwsExceptionIfNotClosed() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3";
        
        JsonParser parser = newParser(instance);
        parser.next();
        Throwable expected = catchThrowable(()->parser.getArray());
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        Throwable actual = catchThrowable(()->sut.getArray());
        sut.close();
        
        assertThat(actual)
            .hasSameClassAs(expected)
            .hasMessage(expected.getMessage());
    }

    @Test
    public void skipObject_skipsObject() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        sut.skipObject();
        
        assertThat(sut.hasNext()).isFalse();
        assertThat(problems).isEmpty();

        sut.close();
    }

    @Test
    public void skipObject_skipsEvenIfNotWellFormed() {
        String schema = "{\"type\":\"object\"}";
        String instance = "{";
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        sut.skipObject();
        
        assertThat(sut.hasNext()).isFalse();
        assertThat(problems).isEmpty();

        sut.close();
    }

    @Test
    public void skipArray_skipsArray() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[1,2,3]";
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        sut.skipArray();
        
        assertThat(sut.hasNext()).isFalse();
        assertThat(problems).isEmpty();

        sut.close();
    }
    
    @Test
    public void skipArray_skipsEvenIfNotWellFormed() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[";
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        sut.skipArray();
        
        assertThat(sut.hasNext()).isFalse();
        assertThat(problems).isEmpty();

        sut.close();
    }

    @Test
    public void getArrayStream_returnsArrayStream() {
        String schema = "{\"type\":\"array\"}";
        String instance = "[true,false,null]";
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        Stream<JsonValue> actual = sut.getArrayStream();
        
        assertThat(actual).containsExactly(JsonValue.TRUE, JsonValue.FALSE, JsonValue.NULL);
        assertThat(problems).isEmpty();
        sut.close();
    }
    
    @Test
    public void getObjectStream_returnsObjectStream() {
        String schema = PERSON_SCHEMA;
        String instance = "{\"name\":\"John Smith\", \"age\": 46}";
        
        JsonParser parser = newParser(instance);
        parser.next();
        Stream<Map.Entry<String, JsonValue>> expected = parser.getObjectStream();
        parser.close();

        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        sut.next();
        Stream<Map.Entry<String, JsonValue>> actual = sut.getObjectStream();

        assertThat(actual).containsExactlyElementsOf(
                expected.collect(Collectors.toList()));
        assertThat(problems).isEmpty();
        sut.close();
    }
    
    @Test
    public void getValueStream_returnsValueStream() {
        String schema = "{\"type\":\"integer\"}";
        String instance = "42";
        
        List<Problem> problems = new ArrayList<>();
        JsonParser sut = newParser(instance, schema, problems::add);
        Stream<JsonValue> actual = sut.getValueStream();
        
        assertThat(actual).containsExactly(Json.createValue(42));
        assertThat(problems).isEmpty();
        sut.close();
    }
}
