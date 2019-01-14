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

package org.leadpony.justify.internal.base;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.InstanceType;

/**
 * @author leadpony
 */
public final class ParserEvents {

    @SuppressWarnings("serial")
    private static final Map<Event, InstanceType> eventToTypeMap = new EnumMap<Event, InstanceType>(Event.class) {{
        put(Event.START_OBJECT, InstanceType.OBJECT);
        put(Event.START_ARRAY, InstanceType.ARRAY);
        put(Event.KEY_NAME, InstanceType.STRING);
        put(Event.VALUE_NUMBER, InstanceType.NUMBER);
        put(Event.VALUE_STRING, InstanceType.STRING);
        put(Event.VALUE_TRUE, InstanceType.BOOLEAN);
        put(Event.VALUE_FALSE, InstanceType.BOOLEAN);
        put(Event.VALUE_NULL, InstanceType.NULL);
    }};
    
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
     * @param parser the parser which produced the event.
     * @return the instance of {@link InstanceType} or {@code null}.
     */
    public static InstanceType toInstanceType(Event event, JsonParser parser) {
        InstanceType type = eventToTypeMap.get(event);
        if (type == InstanceType.NUMBER) {
            if (parser.isIntegralNumber()) {
                type = InstanceType.INTEGER;
            } else {
                BigDecimal value = parser.getBigDecimal().stripTrailingZeros();
                if (value.scale() == 0) {
                    type = InstanceType.INTEGER;
                }
            }
        }
        return type;
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
    
    private ParserEvents() {
    }
}
