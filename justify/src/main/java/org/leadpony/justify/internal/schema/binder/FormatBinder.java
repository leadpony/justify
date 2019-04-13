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
import org.leadpony.justify.internal.keyword.assertion.format.Format;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A binder type for "format" keyword.
 *
 * @author leadpony
 */
@Spec({ SpecVersion.DRAFT_04, SpecVersion.DRAFT_06, SpecVersion.DRAFT_07 })
class FormatBinder extends AbstractBinder {

    @Override
    public String name() {
        return "format";
    }

    @Override
    public void fromJson(JsonParser parser, BinderContext context) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            String name = parser.getString();
            FormatAttribute attribute = context.getFormatAttribute(name);
            if (attribute != null) {
                context.addKeyword(Format.of(attribute));
            } else {
                context.addKeyword(Format.of(name));
            }
        } else {
            skipValue(event, parser);
        }
    }
}
