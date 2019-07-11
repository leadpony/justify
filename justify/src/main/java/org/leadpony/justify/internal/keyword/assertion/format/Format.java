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

package org.leadpony.justify.internal.keyword.assertion.format;

import java.util.List;
import java.util.Map;

import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.SchemaKeyword;
import org.leadpony.justify.internal.keyword.assertion.AbstractAssertion;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * An assertion representing "format" keyword.
 *
 * @author leadpony
 */
@KeywordType("format")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Format extends AbstractAssertion {

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            if (value.getValueType() == ValueType.STRING) {
                String name = ((JsonString) value).getString();
                FormatAttribute attribute = context.getFormateAttribute(name);
                if (attribute != null) {
                    return new EvaluatableFormat(value, attribute);
                } else {
                    return new Format(value, name);
                }
            } else {
                throw new IllegalArgumentException();
            }
        };
    }

    public Format(JsonValue json, String attribute) {
        super(json);
    }

    @Override
    public void addToEvaluatables(List<Evaluatable> evaluatables, Map<String, SchemaKeyword> keywords) {
    }
}
