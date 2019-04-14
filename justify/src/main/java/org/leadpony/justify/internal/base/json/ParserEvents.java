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

package org.leadpony.justify.internal.base.json;

import java.util.EnumSet;

import javax.json.JsonValue;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.InstanceType;

/**
 * @author leadpony
 */
public final class ParserEvents {

    private static final EnumSet<Event> valueEvents = EnumSet.of(
            Event.START_ARRAY,
            Event.START_OBJECT,
            Event.VALUE_STRING,
            Event.VALUE_NUMBER,
            Event.VALUE_TRUE,
            Event.VALUE_FALSE,
            Event.VALUE_NULL);

    /**
     * Converts given parser event to {@link InstanceType}.
     *
     * <p>
     * According to the JSON Schema Test Suite, 1.0 must be treated as an integer
     * rather than a number.
     * </p>
     *
     * @param event the event to convert.
     * @return the instance of {@link InstanceType} or {@code null}.
     */
    public static InstanceType toBroadInstanceType(Event event) {
        switch (event) {
        case START_ARRAY:
            return InstanceType.ARRAY;
        case START_OBJECT:
            return InstanceType.OBJECT;
        case KEY_NAME:
        case VALUE_STRING:
            return InstanceType.STRING;
        case VALUE_NUMBER:
            return InstanceType.NUMBER;
        case VALUE_TRUE:
        case VALUE_FALSE:
            return InstanceType.BOOLEAN;
        case VALUE_NULL:
            return InstanceType.NULL;
        default:
            return null;
        }
    }

    public static boolean isStartOfContainer(Event event) {
        return event == Event.START_ARRAY || event == Event.START_OBJECT;
    }

    public static boolean isEndOfContainer(Event event) {
        return event == Event.END_ARRAY || event == Event.END_OBJECT;
    }

    public static boolean isValue(Event event) {
        return valueEvents.contains(event);
    }

    /**
     * Returns the parser event producing the specified JSON value.
     *
     * @param value the JSON value in the instance, cannot be {@code null}.
     * @return the parser event.
     */
    public static Event fromValue(JsonValue value) {
        switch (value.getValueType()) {
        case ARRAY:
            return Event.START_ARRAY;
        case OBJECT:
            return Event.START_OBJECT;
        case STRING:
            return Event.VALUE_STRING;
        case NUMBER:
            return Event.VALUE_NUMBER;
        case TRUE:
            return Event.VALUE_TRUE;
        case FALSE:
            return Event.VALUE_FALSE;
        case NULL:
            return Event.VALUE_NULL;
        }
        throw new IllegalStateException();
    }

    private ParserEvents() {
    }
}
