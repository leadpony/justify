/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.internal.keyword;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.InvalidKeywordException;
import org.leadpony.justify.api.keyword.SubschemaParser;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;

import jakarta.json.JsonNumber;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * A utility class for {@link KeywordType}.
 *
 * @author leadpony
 */
public final class KeywordTypes {

    /**
     * A generic object which maps a value of the specified type to a keyword.
     *
     * @author leadpony
     *
     * @param <T> the type of the input value.
     */
    @FunctionalInterface
    public interface Mapper<T> {

        Keyword map(JsonValue jsonValue, T value);
    }

    /**
     * A specialized mapper which maps an int value to a keyword.
     *
     * @author leadpony
     */
    @FunctionalInterface
    public interface IntMapper {

        Keyword map(JsonValue jsonValue, int value);
    }

    /**
     * A specialized mapper which maps a boolean value to a keyword.
     *
     * @author leadpony
     */
    @FunctionalInterface
    public interface BooleanMapper {

        Keyword map(JsonValue jsonValue, boolean value);
    }

    /**
     * Returns a keyword type which maps a JSON value to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingJsonValue(String name, Function<JsonValue, Keyword> mapper) {
        return new JsonValueKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            public Keyword map(JsonValue jsonValue) {
                return mapper.apply(jsonValue);
            }
        };
    }

    public static KeywordType mappingJsonValueSet(String name, Mapper<Set<JsonValue>> mapper) {
        return new JsonValueSetKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, Set<JsonValue> value) {
                return mapper.map(jsonValue, value);
            }
        };
    }

    /**
     * Returns a keyword type which maps a string to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingString(String name, Mapper<String> mapper) {
        return new StringKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, String value) {
                return mapper.map(jsonValue, value);
            }
        };
    }

    public static KeywordType mappingStringSet(String name, Mapper<Set<String>> mapper) {
        return new StringSetKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, Set<String> value) {
                return mapper.map(jsonValue, value);
            }
        };
    }

    /**
     * Returns a keyword type which maps a {@link BigDecimal} to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingNumber(String name, Mapper<BigDecimal> mapper) {
        return new NumberKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, BigDecimal value) {
                return mapper.map(jsonValue, value);
            }
        };
    }

    /**
     * Returns a keyword type which maps a non-negative integer to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingNonNegativeInteger(String name, IntMapper mapper) {
        return new NonNegativeIntegerKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, int value) {
                return mapper.map(jsonValue, value);
            }
        };
    }

    /**
     * Returns a keyword type which maps a boolean to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingBoolean(String name, BooleanMapper mapper) {
        return new BooleanKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, boolean value) {
                return mapper.map(jsonValue, value);
            }
        };
    }

    /**
     * Returns a keyword type which maps a {@link URI} to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingUri(String name, Mapper<URI> mapper) {
        return new UriKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, URI value) {
                return mapper.map(jsonValue,  value);
            }
        };
    }

    /**
     * Returns a keyword type which maps a {@link JsonSchema} to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingSchema(String name, Function<JsonSchema, Keyword> mapper) {
        return new SchemaKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, JsonSchema value) {
                return mapper.apply(value);
            }
        };
    }

    /**
     * Returns a keyword type which maps a collection of {@link JsonSchema}s to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingSchemaList(String name, Mapper<Collection<JsonSchema>> mapper) {
        return new SchemaListKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, Collection<JsonSchema> value) {
                return mapper.map(jsonValue, value);
            }
        };
    }

    /**
     * Returns a keyword type which maps a map of {@link JsonSchema}s to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingSchemaMap(String name, Mapper<Map<String, JsonSchema>> mapper) {
        return new SchemaMapKeywordType() {

            @Override
            public String name() {
                return name;
            }

            @Override
            protected Keyword map(JsonValue jsonValue, Map<String, JsonSchema> value) {
                return mapper.map(jsonValue, value);
            }
        };
    }

    public static Map<String, KeywordType> toMap(KeywordType... types) {
        Map<String, KeywordType> map = new HashMap<>();
        for (KeywordType type : types) {
            map.put(type.name(), type);
        }
        return Collections.unmodifiableMap(map);
    }

    private abstract static class MappingKeywordType<T> implements KeywordType {

        protected abstract Keyword map(JsonValue jsonValue, T value);
    }

    public abstract static class NumberKeywordType extends MappingKeywordType<BigDecimal> {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() == ValueType.NUMBER) {
                JsonNumber number = (JsonNumber) jsonValue;
                return map(jsonValue, number.bigDecimalValue());
            }
            throw fail("Not a number");
        }
    }

    public abstract static class StringKeywordType extends MappingKeywordType<String> {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() == ValueType.STRING) {
                JsonString string = (JsonString) jsonValue;
                return map(jsonValue, string.getString());
            }
            throw fail("Not a string");
        }
    }

    public abstract static class StringSetKeywordType extends MappingKeywordType<Set<String>> {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() == ValueType.ARRAY) {
                Set<String> strings = new LinkedHashSet<>();
                for (JsonValue item : jsonValue.asJsonArray()) {
                    if (item.getValueType() == ValueType.STRING) {
                        strings.add(((JsonString) item).getString());
                    } else {
                        throw fail("Not a string");
                    }
                }
                return map(jsonValue, strings);
            }
            throw fail("Not an array");
        }
    }

    public abstract static class UriKeywordType extends MappingKeywordType<URI> {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() == ValueType.STRING) {
                JsonString string = (JsonString) jsonValue;
                return map(jsonValue, URI.create(string.getString()));
            }
            throw fail("Not a string");
        }
    }

    public abstract static class SchemaKeywordType extends MappingKeywordType<JsonSchema> {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            return map(jsonValue, schemaParser.parseSubschema(jsonValue));
        }
    }

    public abstract static class SchemaListKeywordType extends MappingKeywordType<Collection<JsonSchema>> {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() == ValueType.ARRAY) {
                Collection<JsonSchema> schemas = new ArrayList<>();
                int i = 0;
                for (JsonValue item : jsonValue.asJsonArray()) {
                    schemas.add(schemaParser.parseSubschema(item, i++));
                }
                return map(jsonValue, schemas);
            }
            throw fail("Not an array");
        }
    }

    public abstract static class SchemaMapKeywordType extends MappingKeywordType<Map<String, JsonSchema>> {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() == ValueType.OBJECT) {
                Map<String, JsonSchema> schemas = new LinkedHashMap<>();
                for (Map.Entry<String, JsonValue> entry : jsonValue.asJsonObject().entrySet()) {
                    String name = entry.getKey();
                    schemas.put(name, schemaParser.parseSubschema(entry.getValue(), name));
                }
                return map(jsonValue, schemas);
            }
            throw fail("Not an object");
        }
    }

    public abstract static class JsonValueSetKeywordType extends MappingKeywordType<Set<JsonValue>> {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() == ValueType.ARRAY) {
                return map(jsonValue, new LinkedHashSet<>(jsonValue.asJsonArray()));
            }
            throw fail("Not an array");
        }
    }

    public abstract static class JsonValueKeywordType implements KeywordType {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            return map(jsonValue);
        }

        protected abstract Keyword map(JsonValue jsonValue);
    }

    public abstract static class NonNegativeIntegerKeywordType implements KeywordType {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() == ValueType.NUMBER) {
                JsonNumber number = (JsonNumber) jsonValue;
                try {
                    int intValue = number.intValueExact();
                    if (intValue >= 0) {
                        return map(jsonValue, intValue);
                    } else {
                        throw fail("Must not be a negative integer");
                    }
                } catch (ArithmeticException e) {
                    throw fail("Must be an integer", e);
                }
            }
            throw fail("Must be a number");
        }

        protected abstract Keyword map(JsonValue jsonValue, int value);
    }

    public abstract static class BooleanKeywordType implements KeywordType {

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            switch (jsonValue.getValueType()) {
            case TRUE:
                return map(jsonValue, true);
            case FALSE:
                return map(jsonValue, false);
            default:
                throw fail("Must be a boolean");
            }
        }

        protected abstract Keyword map(JsonValue jsonValue, boolean value);
    }

    private static InvalidKeywordException fail(String message) {
        return new InvalidKeywordException(message);
    }

    private static InvalidKeywordException fail(String message, Throwable cause) {
        return new InvalidKeywordException(message, cause);
    }

    private KeywordTypes() {
    }
}
