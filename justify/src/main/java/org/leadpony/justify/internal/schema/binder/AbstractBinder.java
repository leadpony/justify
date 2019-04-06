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
package org.leadpony.justify.internal.schema.binder;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;

/**
 * A skeletal implementation of {@link KeywordBinder}.
 *
 * @author leadpony
 */
abstract class AbstractBinder implements KeywordBinder {

    @Override
    public SpecVersion[] getSupportedVersions() {
        Spec spec = getClass().getAnnotation(Spec.class);
        if (spec == null) {
            return new SpecVersion[0];
        }
        return spec.value();
    }

    /**
     * Checks if this binder can read another subschema from the current location.
     *
     * @param event the parser event.
     * @return {@code true} if this binder can read a subschema, {@code false}
     *         otherwise.
     */
    protected boolean canReadSubschema(Event event) {
        return event == Event.START_OBJECT ||
                event == Event.VALUE_TRUE ||
                event == Event.VALUE_FALSE;
    }

    /**
     * Skips the current value.
     *
     * @param event  the current event.
     * @param parser the JSON parser.
     */
    protected void skipValue(Event event, JsonParser parser) {
        switch (event) {
        case START_ARRAY:
            parser.skipArray();
            break;
        case START_OBJECT:
            parser.skipObject();
            break;
        default:
            break;
        }
    }
}
