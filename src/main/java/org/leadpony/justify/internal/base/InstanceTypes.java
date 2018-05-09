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

package org.leadpony.justify.internal.base;

import java.util.HashMap;
import java.util.Map;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;

/**
 * @author leadpony
 */
public final class InstanceTypes {

    @SuppressWarnings("serial")
    private static final Map<Event, InstanceType> eventToTypeMap = new HashMap<Event, InstanceType>() {{
        put(Event.START_OBJECT, InstanceType.OBJECT);
        put(Event.START_ARRAY, InstanceType.ARRAY);
        put(Event.VALUE_NUMBER, InstanceType.NUMBER);
        put(Event.VALUE_STRING, InstanceType.STRING);
        put(Event.VALUE_TRUE, InstanceType.BOOLEAN);
        put(Event.VALUE_FALSE, InstanceType.BOOLEAN);
        put(Event.VALUE_NULL, InstanceType.NULL);
    }};

    public static InstanceType fromEvent(Event event, JsonParser parser) {
        InstanceType type = eventToTypeMap.get(event);
        if (type == InstanceType.NUMBER && parser.isIntegralNumber()) {
            type = InstanceType.INTEGER;
        }
        return type;
    }
    
    private InstanceTypes() {
    }
}
