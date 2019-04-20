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
package org.leadpony.justify.internal.schema.binding;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.combiner.Dependencies;

/**
 * A binder type for "dependencies" keyword.
 *
 * @author leadpony
 */
@Spec({ SpecVersion.DRAFT_04, SpecVersion.DRAFT_06, SpecVersion.DRAFT_07 })
class DependenciesBinder extends AbstractBinder {

    @Override
    public String name() {
        return "dependencies";
    }

    @Override
    public void fromJson(JsonParser parser, BindingContext context) {
        Event event = parser.next();
        if (event == Event.START_OBJECT) {
            Map<String, Object> values = new HashMap<>();
            while (parser.hasNext()) {
                event = parser.next();
                if (event == Event.KEY_NAME) {
                    String property = parser.getString();
                    if (parser.hasNext()) {
                        event = parser.next();
                        if (canReadSubschema(event)) {
                            values.put(property, context.readSchema(event));
                        } else if (event == Event.START_ARRAY) {
                            Set<String> required = new LinkedHashSet<>();
                            while (parser.hasNext()) {
                                event = parser.next();
                                if (event == Event.VALUE_STRING) {
                                    required.add(parser.getString());
                                } else if (event == Event.END_ARRAY) {
                                    values.put(property, required);
                                    break;
                                }
                            }
                        }
                    }
                } else if (event == Event.END_OBJECT) {
                    context.addKeyword(new Dependencies(values));
                    break;
                }
            }
        } else {
            skipValue(event, parser);
        }
    }
}
