/*
 * Copyright 2019 the JSON-P Test Suite Authors.
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
package org.leadpony.jsonp.testsuite.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link JsonParser} which parses in-memory JSON structures.
 *
 * @author leadpony
 */
public abstract class AbstractJsonValueParserTest {

    private static final JsonBuilderFactory BUILDER_FACTORY = Json.createBuilderFactory(null);

    /**
     * Test cases for {@link JsonParser#hasNext()} and {@link JsonParser#next()}.
     *
     * @author leadpony
     */
    public enum ParserEventTestCase {
        EMPTY_ARRAY(
            JsonValue.EMPTY_JSON_ARRAY,
            Event.START_ARRAY,
            Event.END_ARRAY),

        SIMPLE_ARRAY(
            array(b -> {
                b.add("hello").add(42).add(true).add(false).addNull();
            }),
            Event.START_ARRAY,
            Event.VALUE_STRING,
            Event.VALUE_NUMBER,
            Event.VALUE_TRUE,
            Event.VALUE_FALSE,
            Event.VALUE_NULL,
            Event.END_ARRAY),

        ARRAY_OF_EMPTY_ARRAYS(
            array(b -> {
                b.add(JsonValue.EMPTY_JSON_ARRAY);
                b.add(JsonValue.EMPTY_JSON_ARRAY);
                b.add(JsonValue.EMPTY_JSON_ARRAY);
            }),
            Event.START_ARRAY,
            Event.START_ARRAY,
            Event.END_ARRAY,
            Event.START_ARRAY,
            Event.END_ARRAY,
            Event.START_ARRAY,
            Event.END_ARRAY,
            Event.END_ARRAY),

        ARRAY_OF_EMPTY_OBJECTS(
            array(b -> {
                b.add(JsonValue.EMPTY_JSON_OBJECT);
                b.add(JsonValue.EMPTY_JSON_OBJECT);
                b.add(JsonValue.EMPTY_JSON_OBJECT);
            }),
            Event.START_ARRAY,
            Event.START_OBJECT,
            Event.END_OBJECT,
            Event.START_OBJECT,
            Event.END_OBJECT,
            Event.START_OBJECT,
            Event.END_OBJECT,
            Event.END_ARRAY),

        ARRAY_OF_ARRAYS(
            array(b -> {
                b.add(array(b2 -> b2.add(1).add(2).add(3)));
                b.add(array(b2 -> b2.add(4).add(5).add(6)));
            }),
            Event.START_ARRAY,
            Event.START_ARRAY,
            Event.VALUE_NUMBER,
            Event.VALUE_NUMBER,
            Event.VALUE_NUMBER,
            Event.END_ARRAY,
            Event.START_ARRAY,
            Event.VALUE_NUMBER,
            Event.VALUE_NUMBER,
            Event.VALUE_NUMBER,
            Event.END_ARRAY,
            Event.END_ARRAY),

        ARRAY_OF_OBJECTS(
            array(b -> {
                b.add(object(b2 -> b2.add("a", "hello").add("b", 42)));
                b.add(object(b2 -> b2.add("c", true).add("d", false).addNull("e")));
            }),
            Event.START_ARRAY,
            Event.START_OBJECT,
            Event.KEY_NAME,
            Event.VALUE_STRING,
            Event.KEY_NAME,
            Event.VALUE_NUMBER,
            Event.END_OBJECT,
            Event.START_OBJECT,
            Event.KEY_NAME,
            Event.VALUE_TRUE,
            Event.KEY_NAME,
            Event.VALUE_FALSE,
            Event.KEY_NAME,
            Event.VALUE_NULL,
            Event.END_OBJECT,
            Event.END_ARRAY),

        EMPTY_OBJECT(
            JsonValue.EMPTY_JSON_OBJECT,
            Event.START_OBJECT,
            Event.END_OBJECT),

        SIMPLE_OBJECT(
            object(b -> {
                b.add("a", "hello");
                b.add("b", 42);
                b.add("c", true);
                b.add("d", false);
                b.addNull("e");
            }),
            Event.START_OBJECT,
            Event.KEY_NAME,
            Event.VALUE_STRING,
            Event.KEY_NAME,
            Event.VALUE_NUMBER,
            Event.KEY_NAME,
            Event.VALUE_TRUE,
            Event.KEY_NAME,
            Event.VALUE_FALSE,
            Event.KEY_NAME,
            Event.VALUE_NULL,
            Event.END_OBJECT),

        OBJECT_OF_EMPTY_ARRAYS(
            object(b -> {
                b.add("a", JsonValue.EMPTY_JSON_ARRAY);
                b.add("b", JsonValue.EMPTY_JSON_ARRAY);
            }),
            Event.START_OBJECT,
            Event.KEY_NAME,
            Event.START_ARRAY,
            Event.END_ARRAY,
            Event.KEY_NAME,
            Event.START_ARRAY,
            Event.END_ARRAY,
            Event.END_OBJECT),

        OBJECT_OF_EMPTY_OBJECTS(
            object(b -> {
                b.add("a", JsonValue.EMPTY_JSON_OBJECT);
                b.add("b", JsonValue.EMPTY_JSON_OBJECT);
            }),
            Event.START_OBJECT,
            Event.KEY_NAME,
            Event.START_OBJECT,
            Event.END_OBJECT,
            Event.KEY_NAME,
            Event.START_OBJECT,
            Event.END_OBJECT,
            Event.END_OBJECT),

        OBJECT_OF_ARRAYS(
            object(b -> {
                b.add("a", array(b2 -> b2.add("hello").add(42)));
                b.add("b", array(b2 -> b2.add(true).add(false).addNull()));
            }),
            Event.START_OBJECT,
            Event.KEY_NAME,
            Event.START_ARRAY,
            Event.VALUE_STRING,
            Event.VALUE_NUMBER,
            Event.END_ARRAY,
            Event.KEY_NAME,
            Event.START_ARRAY,
            Event.VALUE_TRUE,
            Event.VALUE_FALSE,
            Event.VALUE_NULL,
            Event.END_ARRAY,
            Event.END_OBJECT),

        OBJECT_OF_OBJECTS(
            object(b -> {
                b.add("a", object(b2 -> b2.add("a1", "hello").add("a2", 42)));
                b.add("b", object(b2 -> b2.add("b1", true).add("b2", false).addNull("b3")));
            }),
            Event.START_OBJECT,
            Event.KEY_NAME,
            Event.START_OBJECT,
            Event.KEY_NAME,
            Event.VALUE_STRING,
            Event.KEY_NAME,
            Event.VALUE_NUMBER,
            Event.END_OBJECT,
            Event.KEY_NAME,
            Event.START_OBJECT,
            Event.KEY_NAME,
            Event.VALUE_TRUE,
            Event.KEY_NAME,
            Event.VALUE_FALSE,
            Event.KEY_NAME,
            Event.VALUE_NULL,
            Event.END_OBJECT,
            Event.END_OBJECT);

        public final JsonStructure value;
        public final Event[] events;

        ParserEventTestCase(JsonStructure value, Event... events) {
            this.value = value;
            this.events = events;
        }
    }

    @ParameterizedTest
    @EnumSource(ParserEventTestCase.class)
    public void hasNextShouldReturnBooleanAsExpected(ParserEventTestCase test) {
        JsonParser parser = createParser(test.value);

        int events = test.events.length;
        while (events-- > 0) {
            assertThat(parser.hasNext()).isTrue();
            parser.next();
        }
        boolean actual = parser.hasNext();
        parser.close();

        assertThat(actual).isFalse();
    }

    @ParameterizedTest
    @EnumSource(ParserEventTestCase.class)
    public void nextShouldReturnEventsAsExpected(ParserEventTestCase test) {
        List<Event> actual = new ArrayList<>();
        try (JsonParser parser = createParser(test.value)) {
            while (parser.hasNext()) {
                actual.add(parser.next());
            }
        }

        assertThat(actual).containsExactly(test.events);
    }

    @ParameterizedTest
    @EnumSource(ParserEventTestCase.class)
    public void nextShouldThrowNoSuchElementExceptionAfterFinalEvent(ParserEventTestCase test) {
        Throwable thrown = null;

        try (JsonParser parser = createParser(test.value)) {
            while (parser.hasNext()) {
                parser.next();
            }

            thrown = catchThrowable(() -> parser.next());
        }

        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Test cases for {@link JsonParser#getString()}.
     *
     * @author leadpony
     */
    public enum StringTestCase {
        STRING_AS_FIRST_ITEM(
            array(b -> b.add("hello").add("world")),
            2,
            "hello"),

        STRING_AS_LAST_ITEM(
            STRING_AS_FIRST_ITEM.json,
            3,
            "world"),

        NUMBER_AS_FIRST_ITEM(
            array(b -> b.add(42).add(new BigDecimal("3.14"))),
            2,
            "42"),

        NUMBER_AS_LAST_ITEM(
            NUMBER_AS_FIRST_ITEM.json,
            3,
            "3.14"),

        STRING_AS_FIRST_PROPERTY_VALUE(
            object(b -> b.add("a", "hello").add("b", "world")),
            3,
            "hello"),

        STRING_AS_LAST_PROPERTY_VALUE(
            STRING_AS_FIRST_PROPERTY_VALUE.json,
            5,
            "world"),

        NUMBER_AS_FIRST_PROPERTY_VALUE(
            object(b -> b.add("a", 42).add("b", new BigDecimal("3.14"))),
            3,
            "42"),

        NUMBER_AS_LAST_PROPERTY_VALUE(
            NUMBER_AS_FIRST_PROPERTY_VALUE.json,
            5,
            "3.14"),

        STRING_AS_FIRST_PROPERTY_KEY(
            object(b -> b.add("hello", 1).add("world", 2)),
            2,
            "hello"),

        STRING_AS_LAST_PROPERTY_KEY(
            STRING_AS_FIRST_PROPERTY_KEY.json,
            4,
            "world");

        final JsonStructure json;
        final int iterations;
        final String value;

        StringTestCase(JsonStructure json, int iterations, String value) {
            this.json = json;
            this.iterations = iterations;
            this.value = value;
        }
    }

    @ParameterizedTest
    @EnumSource(StringTestCase.class)
    public void getStringShouldReturnStringAsExpected(StringTestCase test) {
        String actual = extractValue(test.json, test.iterations,
            JsonParser::getString);

        assertThat(actual).isEqualTo(test.value);
    }

    /**
     * Test cases for {@link JsonParser#isIntegralNumber()}.
     *
     * @author leadpony
     */
    public enum IsIntegralNumberTestCase {

        NUMBER_AS_FIRST_ITEM(
            array(b -> b.add(42).add(new BigDecimal("3.14"))),
            2,
            true),

        NUMBER_AS_LAST_ITEM(
            NUMBER_AS_FIRST_ITEM.json,
            3,
            false),

        NUMBER_AS_FIRST_PROPERTY_VALUE(
            object(b -> b.add("a", 42).add("b", new BigDecimal("3.14"))),
            3,
            true),

        NUMBER_AS_LAST_PROPERTY_VALUE(
            NUMBER_AS_FIRST_PROPERTY_VALUE.json,
            5,
            false);

        final JsonStructure json;
        final int iterations;
        final boolean value;

        IsIntegralNumberTestCase(JsonStructure json, int iterations, boolean value) {
            this.json = json;
            this.iterations = iterations;
            this.value = value;
        }
    }

    @ParameterizedTest
    @EnumSource(IsIntegralNumberTestCase.class)
    public void isIntegralNumberShouldReturnBooleanAsExpected(IsIntegralNumberTestCase test) {
        boolean actual = extractValue(test.json, test.iterations,
            JsonParser::isIntegralNumber);

        assertThat(actual).isEqualTo(test.value);
    }

    /**
     * Test cases for {@link JsonParser#getInt()}.
     *
     * @author leadpony
     */
    public enum IntTestCase {

        NUMBER_AS_FIRST_ITEM(
            array(b -> b.add(Integer.MAX_VALUE).add(Integer.MIN_VALUE)),
            2,
            Integer.MAX_VALUE),

        NUMBER_AS_LAST_ITEM(
            NUMBER_AS_FIRST_ITEM.json,
            3,
            Integer.MIN_VALUE),

        NUMBER_AS_FIRST_PROPERTY_VALUE(
            object(b -> b.add("a", Integer.MAX_VALUE).add("b", Integer.MIN_VALUE)),
            3,
            Integer.MAX_VALUE),

        NUMBER_AS_LAST_PROPERTY_VALUE(
            NUMBER_AS_FIRST_PROPERTY_VALUE.json,
            5,
            Integer.MIN_VALUE);

        final JsonStructure json;
        final int iterations;
        final int value;

        IntTestCase(JsonStructure json, int iterations, int value) {
            this.json = json;
            this.iterations = iterations;
            this.value = value;
        }
    }

    @ParameterizedTest
    @EnumSource(IntTestCase.class)
    public void getIntShouldReturnIntAsExpected(IntTestCase test) {
        int actual = extractValue(test.json, test.iterations,
            JsonParser::getInt);

        assertThat(actual).isEqualTo(test.value);
    }

    /**
     * Test cases for {@link JsonParser#getLong()}.
     *
     * @author leadpony
     */
    public enum LongTestCase {

        NUMBER_AS_FIRST_ITEM(
            array(b -> b.add(Long.MAX_VALUE).add(Long.MIN_VALUE)),
            2,
            Long.MAX_VALUE),

        NUMBER_AS_LAST_ITEM(
            NUMBER_AS_FIRST_ITEM.json,
            3,
            Long.MIN_VALUE),

        NUMBER_AS_FIRST_PROPERTY_VALUE(
            object(b -> b.add("a", Long.MAX_VALUE).add("b", Long.MIN_VALUE)),
            3,
            Long.MAX_VALUE),

        NUMBER_AS_LAST_PROPERTY_VALUE(
            NUMBER_AS_FIRST_PROPERTY_VALUE.json,
            5,
            Long.MIN_VALUE);

        final JsonStructure json;
        final int iterations;
        final long value;

        LongTestCase(JsonStructure json, int iterations, long value) {
            this.json = json;
            this.iterations = iterations;
            this.value = value;
        }
    }

    @ParameterizedTest
    @EnumSource(LongTestCase.class)
    public void getLongShouldReturnLongAsExpected(LongTestCase test) {
        long actual = extractValue(test.json, test.iterations,
            JsonParser::getLong);

        assertThat(actual).isEqualTo(test.value);
    }

    /**
     * Test cases for {@link JsonParser#getBigDecimal()}.
     *
     * @author leadpony
     */
    public enum BigDecimalTestCase {

        ZERO_IN_ARRAY(
            array(b -> b.add(0)),
            2,
            BigDecimal.ZERO),

        ONE_IN_ARRAY(
            array(b -> b.add(1)),
            2,
            BigDecimal.ONE),

        TEN_IN_ARRAY(
            array(b -> b.add(10)),
            2,
            BigDecimal.TEN),

        NUMBER_AS_FIRST_ITEM(
            array(b -> b.add(42).add(new BigDecimal("3.14"))),
            2,
            new BigDecimal(42)),

        NUMBER_AS_LAST_ITEM(
            NUMBER_AS_FIRST_ITEM.json,
            3,
            new BigDecimal("3.14")),

        NUMBER_AS_FIRST_PROPERTY_VALUE(
            object(b -> b.add("a", 42).add("b", new BigDecimal("3.14"))),
            3,
            new BigDecimal(42)),

        NUMBER_AS_LAST_PROPERTY_VALUE(
            NUMBER_AS_FIRST_PROPERTY_VALUE.json,
            5,
            new BigDecimal("3.14"));

        final JsonStructure json;
        final int iterations;
        final BigDecimal value;

        BigDecimalTestCase(JsonStructure json, int iterations, BigDecimal value) {
            this.json = json;
            this.iterations = iterations;
            this.value = value;
        }
    }

    @ParameterizedTest
    @EnumSource(BigDecimalTestCase.class)
    public void getBigDecimalShouldReturnBigDecimalAsExpected(BigDecimalTestCase test) {
        BigDecimal actual = extractValue(test.json, test.iterations,
            JsonParser::getBigDecimal);

        assertThat(actual).isEqualTo(test.value);
    }

    @ParameterizedTest
    @EnumSource(ParserEventTestCase.class)
    public void getLocationShouldReturnUnknownLocation(ParserEventTestCase test) {
        JsonParser parser = createParser(test.value);
        while (parser.hasNext()) {
            parser.next();
            JsonLocation actual = parser.getLocation();
            assertThat(actual).isNotNull();
        }
        parser.close();
    }

    @ParameterizedTest
    @EnumSource(ParserEventTestCase.class)
    public void getLocationShouldReturnUnknownLocationAtStart(ParserEventTestCase test) {
        JsonParser parser = createParser(test.value);
        JsonLocation actual = parser.getLocation();
        parser.close();

        assertThat(actual).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(ParserEventTestCase.class)
    public void getLocationShouldReturnUnknownLocationAtEnd(ParserEventTestCase test) {
        JsonParser parser = createParser(test.value);
        while (parser.hasNext()) {
            parser.next();
        }
        JsonLocation actual = parser.getLocation();
        parser.close();

        assertThat(actual).isNotNull();
    }

    /**
     * Test cases for {@link JsonParser#getValue()}.
     *
     * @author leadpony
     */
    public enum ValueTestCase {
        SIMPLE_ARRAY(
            array(b -> b.add("hello").add(42).add(true).add(false).addNull())),

        STRING_IN_ARRAY(
            SIMPLE_ARRAY.json,
            2,
            Json.createValue("hello")),

        NUMBER_IN_ARRAY(
            SIMPLE_ARRAY.json,
            3,
            Json.createValue(42)),

        TRUE_IN_ARRAY(
            SIMPLE_ARRAY.json,
            4,
            JsonValue.TRUE),

        FALSE_IN_ARRAY(
            SIMPLE_ARRAY.json,
            5,
            JsonValue.FALSE),

        NULL_IN_ARRAY(
            SIMPLE_ARRAY.json,
            6,
            JsonValue.NULL),

        SIMPLE_OBJECT(
            object(b -> b.add("a", "hello")
                .add("b", 42)
                .add("c", true)
                .add("d", false)
                .addNull("e"))),

        STRING_IN_OBJECT(
            SIMPLE_OBJECT.json,
            3,
            Json.createValue("hello")),

        NUMBER_IN_OBJECT(
            SIMPLE_OBJECT.json,
            5,
            Json.createValue(42)),

        TRUE_IN_OBJECT(
            SIMPLE_OBJECT.json,
            7,
            JsonValue.TRUE),

        FALSE_IN_OBJECT(
            SIMPLE_OBJECT.json,
            9,
            JsonValue.FALSE),

        NULL_IN_OBJECT(
            SIMPLE_OBJECT.json,
            11,
            JsonValue.NULL),

        ARRAY_IN_ARRAY(
            array(b -> b.add(SIMPLE_ARRAY.json).add(SIMPLE_OBJECT.json)),
            2,
            SIMPLE_ARRAY.json),

        OBJECT_IN_ARRAY(
            array(b -> b.add(SIMPLE_ARRAY.json).add(SIMPLE_OBJECT.json)),
            9,
            SIMPLE_OBJECT.json);

        final JsonStructure json;
        final int iterations;
        final JsonValue value;

        ValueTestCase(JsonStructure json) {
            this.json = json;
            this.iterations = 1;
            this.value = json;
        }

        ValueTestCase(JsonStructure json, int iterations, JsonValue value) {
            this.json = json;
            this.iterations = iterations;
            this.value = value;
        }

        boolean isArray() {
            return value.getValueType() == ValueType.ARRAY;
        }

        boolean isObject() {
            return value.getValueType() == ValueType.OBJECT;
        }
    }

    @ParameterizedTest
    @EnumSource(ValueTestCase.class)
    public void getValueShouldReturnJsonValueAsExpected(ValueTestCase test) {
        JsonValue actual = extractValue(test.json, test.iterations,
            JsonParser::getValue);

        assertThat(actual).isEqualTo(test.value);
    }

    public static Stream<ValueTestCase> getArrayShouldReturnArrayAsExpected() {
        return Stream.of(ValueTestCase.values())
            .filter(ValueTestCase::isArray);
    }

    @ParameterizedTest
    @MethodSource
    public void getArrayShouldReturnArrayAsExpected(ValueTestCase test) {
        JsonArray actual = extractValue(test.json, test.iterations,
            JsonParser::getArray);

        assertThat(actual).isEqualTo(test.value);
        assertThat(actual.getValueType()).isSameAs(ValueType.ARRAY);
    }

    public static Stream<ValueTestCase> getObjectShouldReturnObjectAsExpected() {
        return Stream.of(ValueTestCase.values())
            .filter(ValueTestCase::isObject);
    }

    @ParameterizedTest
    @MethodSource
    public void getObjectShouldReturnObjectAsExpected(ValueTestCase test) {
        JsonObject actual = extractValue(test.json, test.iterations,
            JsonParser::getObject);

        assertThat(actual).isEqualTo(test.value);
        assertThat(actual.getValueType()).isSameAs(ValueType.OBJECT);
    }

    /**
     * Test cases for {@link JsonParser#getArrayStream()}.
     *
     * @author leadpony
     */
    public enum GetArrayStreamTestCase {
        ARRAY(
            array(b -> {
                b.add("hello");
                b.add(42);
                b.add(true);
                b.add(false);
                b.addNull();
            }),
            1,
            Json.createValue("hello"),
            Json.createValue(42),
            JsonValue.TRUE,
            JsonValue.FALSE,
            JsonValue.NULL),

        ARRAY_IN_ARRAY(
            array(b -> {
                b.add(array(b2 -> b2.add("hello").add(42)));
                b.add(array(b2 -> b2.add(true).add(false).addNull()));
            }),
            2,
            Json.createValue("hello"),
            Json.createValue(42)),

        SECOND_ARRAY_IN_ARRAY(
            ARRAY_IN_ARRAY.value,
            6,
            JsonValue.TRUE,
            JsonValue.FALSE,
            JsonValue.NULL),

        ARRAY_IN_OBJECT(
            object(b -> {
                b.add("a", array(b2 -> b2.add("hello").add(42)));
                b.add("b", array(b2 -> b2.add(true).add(false).addNull()));
            }),
            3,
            Json.createValue("hello"),
            Json.createValue(42)),

        SECOND_ARRAY_IN_OBJECT(
            ARRAY_IN_OBJECT.value,
            8,
            JsonValue.TRUE,
            JsonValue.FALSE,
            JsonValue.NULL),

        EMPTY_ARRAY(JsonValue.EMPTY_JSON_ARRAY, 1);

        final JsonStructure value;
        final int iterations;
        final JsonValue[] expected;

        GetArrayStreamTestCase(JsonStructure value, int iterations, JsonValue... expected) {
            this.value = value;
            this.iterations = iterations;
            this.expected = expected;
        }
    }

    @ParameterizedTest
    @EnumSource(GetArrayStreamTestCase.class)
    public void getArrayStreamShouldReturnArrayStreamAsExpected(GetArrayStreamTestCase test) {
        JsonParser parser = createParser(test.value);

        int iterations = test.iterations;
        while (iterations-- > 0) {
            parser.next();
        }

        Stream<JsonValue> actual = parser.getArrayStream();
        assertThat(actual).containsExactly(test.expected);

        parser.close();
    }

    /**
     * Test cases for {@link JsonParser#getObjectStream()}.
     *
     * @author leadpony
     */
    public enum GetObjectArrayTestCase {
        OBJECT(
            object(b -> {
                b.add("a", "hello");
                b.add("b", 42);
                b.add("c", true);
                b.add("d", false);
                b.addNull("e");
            }),
            1,
            entry("a", Json.createValue("hello")),
            entry("b", Json.createValue(42)),
            entry("c", JsonValue.TRUE),
            entry("d", JsonValue.FALSE),
            entry("e", JsonValue.NULL)),

        OBJECT_IN_ARRAY(
            array(b -> {
                b.add(object(b2 -> b2.add("a", "hello").add("b", 42)));
                b.add(object(b2 -> b2.add("c", true).add("d", false).addNull("e")));
            }),
            2,
            entry("a", Json.createValue("hello")),
            entry("b", Json.createValue(42))),

        SECOND_OBJECT_IN_ARRAY(
            OBJECT_IN_ARRAY.value,
            8,
            entry("c", JsonValue.TRUE),
            entry("d", JsonValue.FALSE),
            entry("e", JsonValue.NULL)),

        OBJECT_IN_OBJECT(
            object(b -> {
                b.add("p", object(b2 -> b2.add("a", "hello").add("b", 42)));
                b.add("q", object(b2 -> b2.add("c", true).add("d", false).addNull("e")));
            }),
            3,
            entry("a", Json.createValue("hello")),
            entry("b", Json.createValue(42))),

        SECOND_OBJECT_IN_OBJECT(
            OBJECT_IN_OBJECT.value,
            10,
            entry("c", JsonValue.TRUE),
            entry("d", JsonValue.FALSE),
            entry("e", JsonValue.NULL)),

        EMPTY_OBJECT(JsonValue.EMPTY_JSON_OBJECT, 1);

        final JsonStructure value;
        final int iterations;
        final Map.Entry<String, JsonValue>[] expected;

        @SafeVarargs
        GetObjectArrayTestCase(JsonStructure value, int iterations, Map.Entry<String, JsonValue>... expected) {
            this.value = value;
            this.iterations = iterations;
            this.expected = expected;
        }

        private static Map.Entry<String, JsonValue> entry(String key, JsonValue value) {
            return new AbstractMap.SimpleImmutableEntry<>(key, value);
        }
    }

    @ParameterizedTest
    @EnumSource(GetObjectArrayTestCase.class)
    public void getObjectStreamShouldReturnObjectStreamAsExpected(GetObjectArrayTestCase test) {
        JsonParser parser = createParser(test.value);

        int iterations = test.iterations;
        while (iterations-- > 0) {
            parser.next();
        }

        Stream<Map.Entry<String, JsonValue>> actual = parser.getObjectStream();
        assertThat(actual).containsExactly(test.expected);

        parser.close();
    }

    /**
     * Test cases for {@link JsonParser#skipArray()}.
     *
     * @author leadpony
     */
    public enum SkipArrayTestCase {
        ARRAY(
            array(b -> b.add(1).add(2).add(3)),
            1,
            null),

        ARRAY_IN_ARRAY(
            array(b -> b.add(ARRAY.value).add(ARRAY.value)),
            2,
            Event.START_ARRAY),

        SECOND_ARRAY_IN_ARRAY(
            ARRAY_IN_ARRAY.value,
            7,
            Event.END_ARRAY),

        ARRAY_IN_OBJECT(
            object(b -> b.add("a", ARRAY.value).add("b", ARRAY.value)),
            3,
            Event.KEY_NAME),

        SECOND_ARRAY_IN_OBJECT(
            ARRAY_IN_OBJECT.value,
            9,
            Event.END_OBJECT);

        final JsonStructure value;
        final int iterations;
        final Event expected;

        SkipArrayTestCase(JsonStructure value, int iterations, Event expected) {
            this.value = value;
            this.iterations = iterations;
            this.expected = expected;
        }
    }

    @ParameterizedTest
    @EnumSource(SkipArrayTestCase.class)
    public void skipArrayShouldSkipArrayAsExpected(SkipArrayTestCase test) {
        JsonParser parser = createParser(test.value);

        int iterations = test.iterations;
        while (iterations-- > 0) {
            parser.next();
        }

        parser.skipArray();

        Event actual = parser.hasNext() ? parser.next() : null;

        parser.close();

        assertThat(actual).isEqualTo(test.expected);
    }

    /**
     * Test cases for {@link JsonParser#skipArray()}.
     *
     * @author leadpony
     */
    public enum SkipObjectTestCase {
        OBJECT(
            object(b -> b.add("a", 365).add("b", "hello")),
            1,
            null),

        OBJECT_IN_ARRAY(
            array(b -> b.add(OBJECT.value).add(OBJECT.value)),
            2,
            Event.START_OBJECT),

        SECOND_OBJECT_IN_ARRAY(
            OBJECT_IN_ARRAY.value,
            8,
            Event.END_ARRAY),

        OBJECT_IN_OBJECT(
            object(b -> b.add("p", OBJECT.value).add("q", OBJECT.value)),
            3,
            Event.KEY_NAME),

        SECOND_OBJECT_IN_OBJECT(
            OBJECT_IN_OBJECT.value,
            10,
            Event.END_OBJECT);

        final JsonStructure value;
        final int iterations;
        final Event expected;

        SkipObjectTestCase(JsonStructure value, int iterations, Event expected) {
            this.value = value;
            this.iterations = iterations;
            this.expected = expected;
        }
    }

    @ParameterizedTest
    @EnumSource(SkipObjectTestCase.class)
    public void skipObjectShouldSkipObjectAsExpected(SkipObjectTestCase test) {
        JsonParser parser = createParser(test.value);

        int iterations = test.iterations;
        while (iterations-- > 0) {
            parser.next();
        }

        parser.skipObject();

        Event actual = parser.hasNext() ? parser.next() : null;

        parser.close();

        assertThat(actual).isEqualTo(test.expected);
    }

    private <T> T extractValue(JsonStructure value, int iterations, Function<JsonParser, T> mapper) {
        try (JsonParser parser = createParser(value)) {
            while (iterations-- > 0) {
                parser.next();
            }
            return mapper.apply(parser);
        }
    }

    protected abstract JsonParser createParser(JsonStructure value);

    /**
     * Builds an JSON array.
     *
     * @param consumer the function to build an array.
     * @return JSON array built.
     */
    private static JsonArray array(Consumer<JsonArrayBuilder> consumer) {
        JsonArrayBuilder builder = BUILDER_FACTORY.createArrayBuilder();
        consumer.accept(builder);
        return builder.build();
    }

    /**
     * Builds an JSON object.
     *
     * @param consumer the function to build an object.
     * @return JSON object built.
     */
    private static JsonObject object(Consumer<JsonObjectBuilder> consumer) {
        JsonObjectBuilder builder = BUILDER_FACTORY.createObjectBuilder();
        consumer.accept(builder);
        return builder.build();
    }
}
