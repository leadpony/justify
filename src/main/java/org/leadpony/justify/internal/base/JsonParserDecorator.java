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

import java.math.BigDecimal;

import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

/**
 * Decorator class of {@link JsonParser}.
 * 
 * @author leadpony
 */
public class JsonParserDecorator implements JsonParser {
    
    private final JsonParser real;
    private Event currentEvent;
    
    public JsonParserDecorator(JsonParser real) {
        this.real = real;
    }
    
    public JsonParser realParser() {
        return real;
    }

    @Override
    public void close() {
        real.close();
    }

    @Override
    public BigDecimal getBigDecimal() {
        return real.getBigDecimal();
    }

    @Override
    public int getInt() {
        return real.getInt();
    }

    @Override
    public JsonLocation getLocation() {
        return real.getLocation();
    }

    @Override
    public long getLong() {
        return real.getLong();
    }

    @Override
    public String getString() {
        return real.getString();
    }

    @Override
    public boolean hasNext() {
        return real.hasNext();
    }

    @Override
    public boolean isIntegralNumber() {
        return real.isIntegralNumber();
    }

    @Override
    public Event next() {
        return currentEvent = real.next();
    }

    @Override
    public void skipArray() {
        if (currentEvent == Event.START_ARRAY) {
            int depth = 1;
            while (hasNext()) {
                Event event = next();
                if (event == Event.END_ARRAY) {
                    if (--depth == 0) {
                        break;
                    }
                } else if (event == Event.START_ARRAY) {
                    ++depth;
                }
            }
        }
    }

    @Override
    public void skipObject() {
        if (currentEvent == Event.START_OBJECT) {
            int depth = 1;
            while (hasNext()) {
                Event event = next();
                if (event == Event.END_OBJECT) {
                    if (--depth == 0) {
                        break;
                    }
                } else if (event == Event.START_OBJECT) {
                    ++depth;
                }
            }
        }
    }
}
