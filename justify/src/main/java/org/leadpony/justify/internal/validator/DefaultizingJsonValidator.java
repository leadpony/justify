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
package org.leadpony.justify.internal.validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.json.DefaultValueParser;

/**
 * A JSON validator which will fill the missing values with default values.
 *
 * @author leadpony
 */
public class DefaultizingJsonValidator extends JsonValidator {

    private final JsonParser realParser;
    private final Map<String, JsonValue> defaultProperties = new LinkedHashMap<>();
    private final List<JsonValue> defaultItems = new ArrayList<>();
    private boolean defaultValuesInserted;

    /**
     * Constructs this parser.
     *
     * @param realParser the underlying JSON parser.
     * @param rootSchema the root JSON schema to be evaluated during validation.
     * @param jsonProvider the JSON provider.
     */
    public DefaultizingJsonValidator(JsonParser realParser, JsonSchema rootSchema, JsonProvider jsonProvider) {
        super(realParser, rootSchema, jsonProvider);
        this.realParser = realParser;
    }

    public boolean isFilling() {
        return getCurrentParser() != realParser;
    }

    @Override
    public void putDefaultProperties(Map<String, JsonValue> properties) {
        defaultProperties.putAll(properties);
        defaultValuesInserted = true;
    }

    @Override
    public void putDefaultItems(List<JsonValue> items) {
        if (defaultItems.isEmpty()) {
            defaultItems.addAll(items);
        } else {
            final int size = items.size();
            for (int i = 0; i < size; i++) {
                if (i < defaultItems.size()) {
                    defaultItems.set(i, items.get(i));
                } else {
                    defaultItems.add(items.get(i));
                }
            }
        }
        defaultValuesInserted = true;
    }

    @Override
    protected Event process(Event event) {
        super.process(event);
        if (defaultValuesInserted) {
            replaceParser();
            defaultValuesInserted = false;
            return getCurrentParser().next();
        }
        return event;
    }

    @Override
    protected void dispatchProblems() {
        if (!isFilling()) {
            super.dispatchProblems();
        }
    }

    private void replaceParser() {
        setCurrentParser(createDefaultValueParser());
        setEventHandler(this::handleEventFromDefaultValues);
    }

    private JsonParser createDefaultValueParser() {
        if (!defaultProperties.isEmpty()) {
            return DefaultValueParser.fillingWith(defaultProperties, getJsonProvider());
        } else if (!defaultItems.isEmpty()) {
            return DefaultValueParser.fillingWith(defaultItems, getJsonProvider());
        }
        throw new IllegalStateException();
    }

    private void handleEventFromDefaultValues(Event event, JsonParser parser) {
        if (!getCurrentParser().hasNext()) {
            setCurrentParser(realParser);
            resetEventHandler();
            this.defaultProperties.clear();
            this.defaultItems.clear();
            if (hasProblems()) {
                dispatchProblems();
            }
        }
    }
}
