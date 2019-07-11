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
package org.leadpony.justify.internal.keyword;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.keyword.KeywordFactory.CreationContext;

/**
 * A mapper of a schema keyword.
 *
 * @author leadpony
 */
public interface KeywordMapper {

    SchemaKeyword map(JsonValue value, CreationContext context);

    /**
     * A mapper which maps a string to a keyword.
     *
     * @author leadpony
     */
    interface FromString extends KeywordMapper {

        @Override
        default SchemaKeyword map(JsonValue value, CreationContext context) {
            if (value.getValueType() == ValueType.STRING) {
                JsonString string = (JsonString) value;
                return map(string, string.getString());
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a string to a keyword.
         *
         * @param json the original JSON value.
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(JsonValue json, String value);
    }

    /**
     * A mapper which maps a {@code BigDecimal} to a keyword.
     *
     * @author leadpony
     */
    interface FromNumber extends KeywordMapper {

        @Override
        default SchemaKeyword map(JsonValue value, CreationContext context) {
            if (value.getValueType() == ValueType.NUMBER) {
                JsonNumber number = (JsonNumber) value;
                return map(number, number.bigDecimalValue());
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a {@code BigDecimal} to a keyword.
         *
         * @param json the original JSON value.
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(JsonValue json, BigDecimal value);
    }

    /**
     * A mapper which maps a non-negative integer to a keyword.
     *
     * @author leadpony
     */
    interface FromNonNegativeInteger extends KeywordMapper {

        @Override
        default SchemaKeyword map(JsonValue value, CreationContext context) {
            if (value.getValueType() == ValueType.NUMBER) {
                try {
                    int intValue = ((JsonNumber) value).intValueExact();
                    if (intValue >= 0) {
                        return map(value, intValue);
                    }
                } catch (ArithmeticException e) {
                }
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a non-negative integer to a keyword.
         *
         * @param json the original JSON value.
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(JsonValue json, int value);
    }

    /**
     * A mapper which maps a boolean to a keyword.
     *
     * @author leadpony
     */
    interface FromBoolean extends KeywordMapper {

        @Override
        default SchemaKeyword map(JsonValue value, CreationContext context) {
            switch (value.getValueType()) {
            case TRUE:
                return map(value, true);
            case FALSE:
                return map(value, false);
            default:
                throw new IllegalArgumentException();
            }
        }

        /**
         * Maps a boolean to a keyword.
         *
         * @param json the original JSON value.
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(JsonValue json, boolean value);
    }


    /**
     * A mapper which maps a URI to a keyword.
     *
     * @author leadpony
     */
    interface FromUri extends KeywordMapper {

        @Override
        default SchemaKeyword map(JsonValue value, CreationContext context) {
            if (value.getValueType() == ValueType.STRING) {
                JsonString string = (JsonString) value;
                URI uri = URI.create(string.getString());
                return map(string, uri);
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a URI to a keyword.
         *
         * @param json the original JSON value.
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(JsonValue json, URI value);
    }

    /**
     * A mapper which maps a JSON schema to a keyword.
     *
     * @author leadpony
     */
    interface FromSchema extends KeywordMapper {

        @Override
        default SchemaKeyword map(JsonValue value, CreationContext context) {
            return map(value, context.asJsonSchema(value));
        }

        /**
         * Maps a JSON schema to a keyword.
         *
         * @param json the original JSON value.
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(JsonValue json, JsonSchema value);
    }

    /**
     * A mapper which maps a list of JSON schemas to a keyword.
     *
     * @author leadpony
     */
    interface FromSchemaList extends KeywordMapper {

        @Override
        default SchemaKeyword map(JsonValue value, CreationContext context) {
            if (value.getValueType() == ValueType.ARRAY) {
                Collection<JsonSchema> schemas = new ArrayList<>();
                for (JsonValue item : value.asJsonArray()) {
                    schemas.add(context.asJsonSchema(item));
                }
                return map(value, schemas);
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a list of JSON schemas to a keyword.
         *
         * @param json the original JSON value.
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(JsonValue json, Collection<JsonSchema> value);
    }

    /**
     * A mapper which maps a map of JSON schemas to a keyword.
     *
     * @author leadpony
     */
    interface FromSchemaMap extends KeywordMapper {

        @Override
        default SchemaKeyword map(JsonValue value, CreationContext context) {
            if (value.getValueType() == ValueType.OBJECT) {
                Map<String, JsonSchema> schemas = new LinkedHashMap<>();
                for (Map.Entry<String, JsonValue> entry : value.asJsonObject().entrySet()) {
                    schemas.put(
                            entry.getKey(),
                            context.asJsonSchema(entry.getValue()));
                }
                return map(value, schemas);
            } else {
                throw new IllegalArgumentException();
            }
        }

        /**
         * Maps a map of JSON schemas to a keyword.
         *
         * @param json the original JSON value.
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(JsonValue json, Map<String, JsonSchema> value);
    }
}
