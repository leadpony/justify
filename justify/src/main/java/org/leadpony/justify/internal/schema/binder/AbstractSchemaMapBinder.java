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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * @author leadpony
 */
abstract class AbstractSchemaMapBinder extends AbstractBinder {

    @Override
    public void fromJson(JsonParser parser, BinderContext context) {
        Event event = parser.next();
        if (event == Event.START_OBJECT) {
            Map<String, JsonSchema> subschemas = new LinkedHashMap<>();
            while (parser.hasNext()) {
                event = parser.next();
                if (event == Event.KEY_NAME) {
                    String name = parser.getString();
                    event = parser.next();
                    if (canReadSubschema(event)) {
                        subschemas.put(name, context.readSchema(event));
                    } else {
                        skipValue(event, parser);
                    }
                } else if (event == Event.END_OBJECT) {
                    context.addKeyword(createKeyword(subschemas));
                    break;
                }
            }
        } else {
            skipValue(event, parser);
        }
    }

    public abstract Keyword createKeyword(Map<String, JsonSchema> subschemas);
}
