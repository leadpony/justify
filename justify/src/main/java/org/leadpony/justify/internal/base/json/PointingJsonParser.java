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

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;

/**
 * A JSON parser type which can track the current parsing position as a JSON
 * pointer.
 *
 * @author leadpony
 */
public class PointingJsonParser extends JsonParserDecorator {

    private JsonPointerBuilder pointerBuilder;
    private String cachedPointer;

    /**
     * Constructs this parser.
     *
     * @param real
     * @param builderFactory
     */
    public PointingJsonParser(JsonParser real, JsonBuilderFactory builderFactory) {
        super(real, builderFactory);
        this.pointerBuilder = JsonPointerBuilder.newInstance();
    }

    /**
     * Returns the current position as a JSON pointer.
     *
     * @return the JSON pointer which points to the current value.
     */
    public String getPointer() {
        if (cachedPointer != null) {
            return cachedPointer;
        }
        cachedPointer = pointerBuilder.toPointer();
        return cachedPointer;
    }

    @Override
    public Event next() {
        Event event = super.next();
        pointerBuilder = pointerBuilder.withEvent(event, realParser());
        cachedPointer = null;
        return event;
    }
}
