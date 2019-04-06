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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.assertion.Enum;

/**
 * A binder type for "enum" keyword.
 *
 * @author leadpony
 */
@Spec({SpecVersion.DRAFT_07})
class EnumBinder extends AbstractBinder {

    @Override
    public String name() {
        return "enum";
    }

    @Override
    public void fromJson(JsonParser parser, BinderContext context) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            Set<JsonValue> values = new LinkedHashSet<>();
            while (parser.next() != Event.END_ARRAY) {
                values.add(parser.getValue());
            }
            if (!values.isEmpty()) {
                context.addKeyword(new Enum(values));
            }
        } else {
            skipValue(event, parser);
        }
    }
}
