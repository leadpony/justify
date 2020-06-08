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
package org.leadpony.justify.api.keyword;

import org.leadpony.justify.api.BaseJsonSchemaBuilder;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

/**
 * A definition of a keyword.
 *
 * @author leadpony
 * @since 4.0
 */
public interface KeywordType {

    /**
     * Returns the name of the keyword.
     *
     * @return the name of the keyword, cannot be {@code null}.
     */
    String name();

    /**
     * Parses a keyword in a schema.
     *
     * @param parser  the dedicated parser for parsing this type of keyword.
     * @param factory the factory of JSON value builders.
     * @param builder the builder of the current schema.
     */
    default void parse(KeywordParser parser, JsonBuilderFactory factory, BaseJsonSchemaBuilder builder) {
        builder.addKeyword(parse(parser, factory));
    }

    /**
     * Parses a keyword in a schema.
     *
     * @param parser  the dedicated parser for parsing this type of keyword.
     * @param factory the factory of JSON value builders.
     * @return the parsed keyword. cannot be {@code null}.
     */
    default Keyword parse(KeywordParser parser, JsonBuilderFactory factory) {
        parser.next();
        return parse(parser.getValue());
    }

    /**
     * Parses a value of a keyword.
     *
     * @param jsonValue the value to parse.
     * @return the parsed keyword. cannot be {@code null}.
     */
    default Keyword parse(JsonValue jsonValue) {
        return failed(jsonValue);
    }

    /**
     * Called when parsing the keyword has failed.
     *
     * @param parser the parser for parsing this type of keyword.
     * @return a failed keyword, cannot be {@code null}.
     */
    default Keyword failed(KeywordParser parser) {
        return failed(parser.getValue());
    }

    /**
     * Called when parsing the keyword has failed.
     *
     * @param jsonValue the value of the keyword.
     * @return a failed keyword, cannot be {@code null}.
     */
    default Keyword failed(JsonValue jsonValue) {
        return Keyword.unrecognized(name(), jsonValue);
    }

    /**
     * Called when parsing the keyword has failed.
     *
     * @param parser  the parser for parsing this type of keyword.
     * @param builder the builder of the value given for the keyword.
     * @return a failed keyword, cannot be {@code null}.
     */
    default Keyword failed(KeywordParser parser, JsonArrayBuilder builder) {
        do {
            builder.add(parser.getValue());
        } while (parser.hasNext() && parser.next() != Event.END_ARRAY);
        return failed(builder.build());
    }

    /**
     * Called when parsing the keyword has failed.
     *
     * @param parser  the parser for parsing this type of keyword.
     * @param builder the builder of the value given for the keyword.
     * @param name    the name of the current property in the object-type value.
     * @return a failed keyword, cannot be {@code null}.
     */
    default Keyword failed(KeywordParser parser, JsonObjectBuilder builder, String name) {
        builder.add(name, parser.getValue());
        while (parser.hasNext() && parser.next() != Event.END_OBJECT) {
            builder.add(parser.getString(), parser.getValue());
        }
        return failed(builder.build());
    }
}
