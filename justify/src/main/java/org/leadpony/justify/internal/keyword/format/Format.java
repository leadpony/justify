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

package org.leadpony.justify.internal.keyword.format;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * An assertion representing "format" keyword.
 *
 * @author leadpony
 */
@KeywordClass("format")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Format extends AbstractAssertionKeyword {

    static class FormatType implements KeywordType {

        private final Map<String, FormatAttribute> attributeMap;

        FormatType() {
            this.attributeMap = Collections.emptyMap();
        }

        FormatType(FormatAttribute[] attributes) {
            Map<String, FormatAttribute> attributeMap = new HashMap<>();
            for (FormatAttribute attribute : attributes) {
                attributeMap.put(attribute.name(), attribute);
            }
            this.attributeMap = attributeMap;
        }

        @Override
        public String name() {
            return "format";
        }

        @Override
        public Keyword newInstance(JsonValue jsonValue, CreationContext context) {
            return Format.newInstance(jsonValue, context);
        }
    }

    public static final KeywordType TYPE = new FormatType();

    private static Keyword newInstance(JsonValue jsonValue, KeywordType.CreationContext context) {
        if (jsonValue.getValueType() == ValueType.STRING) {
            String name = ((JsonString) jsonValue).getString();
            FormatAttribute attribute = context.getFormateAttribute(name);
            if (attribute != null) {
                return new RecognizedFormat(jsonValue, attribute);
            } else {
                return new Format(jsonValue, name);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Format(JsonValue json, String attribute) {
        super(json);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public boolean canEvaluate() {
        return false;
    }
}
