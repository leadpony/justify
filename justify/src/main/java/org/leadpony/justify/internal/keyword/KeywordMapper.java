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
import java.util.LinkedHashMap;
import java.util.List;
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
                return map(((JsonString) value).getString());
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a string to a keyword.
         *
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(String value);
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
                return map(((JsonNumber) value).bigDecimalValue());
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a {@code BigDecimal} to a keyword.
         *
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(BigDecimal value);
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
                        return map(intValue);
                    }
                } catch (ArithmeticException e) {
                }
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a non-negative integer to a keyword.
         *
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(int value);
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
                return map(true);
            case FALSE:
                return map(false);
            default:
                throw new IllegalArgumentException();
            }
        }

        /**
         * Maps a boolean to a keyword.
         *
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(boolean value);
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
                URI uri = URI.create(((JsonString) value).getString());
                return map(uri);
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a URI to a keyword.
         *
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(URI value);
    }

    /**
     * A mapper which maps a JSON schema to a keyword.
     *
     * @author leadpony
     */
    interface FromSchema extends KeywordMapper {

        @Override
        default SchemaKeyword map(JsonValue value, CreationContext context) {
            return map(context.asJsonSchema(value));
        }

        /**
         * Maps a JSON schema to a keyword.
         *
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(JsonSchema value);
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
                List<JsonSchema> schemas = new ArrayList<>();
                for (JsonValue item : value.asJsonArray()) {
                    schemas.add(context.asJsonSchema(item));
                }
                return map(schemas);
            }
            throw new IllegalArgumentException();
        }

        /**
         * Maps a list of JSON schemas to a keyword.
         *
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(List<JsonSchema> value);
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
                return map(schemas);
            } else {
                throw new IllegalArgumentException();
            }
        }

        /**
         * Maps a map of JSON schemas to a keyword.
         *
         * @param value the value to be converted to a keyword.
         * @return newly created keyword.
         */
        SchemaKeyword map(Map<String, JsonSchema> value);
    }
}
