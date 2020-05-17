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
import java.util.Map;
import java.util.function.Function;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType;

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

        Keyword map(JsonValue json, T value);
    }

    /**
     * A specialized mapper which maps an int value to a keyword.
     *
     * @author leadpony
     */
    @FunctionalInterface
    public interface IntMapper {

        Keyword map(JsonValue json, int value);
    }

    /**
     * A specialized mapper which maps a boolean value to a keyword.
     *
     * @author leadpony
     */
    @FunctionalInterface
    public interface BooleanMapper {

        Keyword map(JsonValue json, boolean value);
    }

    /**
     * Returns a keyword type which maps a JSON value to a keyword.
     *
     * @param name   the name of the keyword.
     * @param mapper the mapper object.
     * @return the instance of keyword type.
     */
    public static KeywordType mappingJson(String name, Function<JsonValue, Keyword> mapper) {
        return new AbstractKeywordType(name) {
            @Override
            public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
                return mapper.apply(jsonValue);
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
        return new AbstractKeywordType(name) {
            @Override
            public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
                if (jsonValue.getValueType() == ValueType.STRING) {
                    JsonString string = (JsonString) jsonValue;
                    return mapper.map(string, string.getString());
                }
                throw new IllegalArgumentException();
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
        return new AbstractKeywordType(name) {
            @Override
            public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
                if (jsonValue.getValueType() == ValueType.NUMBER) {
                    JsonNumber number = (JsonNumber) jsonValue;
                    return mapper.map(number, number.bigDecimalValue());
                }
                throw new IllegalArgumentException();
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
        return new AbstractKeywordType(name) {
            @Override
            public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
                if (jsonValue.getValueType() == ValueType.NUMBER) {
                    try {
                        int intValue = ((JsonNumber) jsonValue).intValueExact();
                        if (intValue >= 0) {
                            return mapper.map(jsonValue, intValue);
                        }
                    } catch (ArithmeticException e) {
                    }
                }
                throw new IllegalArgumentException();
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
        return new AbstractKeywordType(name) {
            @Override
            public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
                switch (jsonValue.getValueType()) {
                case TRUE:
                    return mapper.map(jsonValue, true);
                case FALSE:
                    return mapper.map(jsonValue, false);
                default:
                    throw new IllegalArgumentException();
                }
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
        return new AbstractKeywordType(name) {
            @Override
            public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
                if (jsonValue.getValueType() == ValueType.STRING) {
                    JsonString string = (JsonString) jsonValue;
                    URI uri = URI.create(string.getString());
                    return mapper.map(string, uri);
                }
                throw new IllegalArgumentException();
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
    public static KeywordType mappingSchema(String name, Mapper<JsonSchema> mapper) {
        return new AbstractKeywordType(name) {
            @Override
            public Keyword newInstance(JsonValue value, CreationContext context) {
                return mapper.map(value, context.asJsonSchema(value));
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
        return new AbstractKeywordType(name) {
            @Override
            public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
                if (jsonValue.getValueType() == ValueType.ARRAY) {
                    Collection<JsonSchema> schemas = new ArrayList<>();
                    for (JsonValue item : jsonValue.asJsonArray()) {
                        schemas.add(context.asJsonSchema(item));
                    }
                    return mapper.map(jsonValue, schemas);
                }
                throw new IllegalArgumentException();
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
        return new AbstractKeywordType(name) {
            @Override
            public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
                if (jsonValue.getValueType() == ValueType.OBJECT) {
                    Map<String, JsonSchema> schemas = new LinkedHashMap<>();
                    for (Map.Entry<String, JsonValue> entry : jsonValue.asJsonObject().entrySet()) {
                        schemas.put(entry.getKey(),
                                context.asJsonSchema(entry.getValue()));
                    }
                    return mapper.map(jsonValue, schemas);
                } else {
                    throw new IllegalArgumentException();
                }
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

    private KeywordTypes() {
    }

    /**
     * @author leadpony
     */
    private abstract static class AbstractKeywordType implements KeywordType {

        private final String name;

        protected AbstractKeywordType(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }
    }
}
