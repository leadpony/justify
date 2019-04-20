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

import java.util.EnumSet;
import java.util.Set;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.assertion.Type;

/**
 * A binder type for "type" keyword.
 *
 * @author leadpony
 */
@Spec({ SpecVersion.DRAFT_06, SpecVersion.DRAFT_07 })
class TypeBinder extends AbstractBinder {

    @Override
    public String name() {
        return "type";
    }

    @Override
    public void fromJson(JsonParser parser, BindingContext context) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            try {
                context.addKeyword(createKeyword(getType(parser)));
            } catch (IllegalArgumentException e) {
                // Ignores the exception.
            }
        } else if (event == Event.START_ARRAY) {
            Set<InstanceType> types = EnumSet.noneOf(InstanceType.class);
            while ((event = parser.next()) != Event.END_ARRAY) {
                if (event == Event.VALUE_STRING) {
                    try {
                        types.add(getType(parser));
                    } catch (IllegalArgumentException e) {
                    }
                } else {
                    skipValue(event, parser);
                }
            }
            context.addKeyword(createKeyword(types));
        } else {
            skipValue(event, parser);
        }
    }

    private static final InstanceType getType(JsonParser parser) {
        String name = parser.getString().toUpperCase();
        return InstanceType.valueOf(name);
    }

    protected Type createKeyword(InstanceType type) {
        return Type.of(type);
    }

    protected Type createKeyword(Set<InstanceType> types) {
        return Type.of(types);
    }
}
