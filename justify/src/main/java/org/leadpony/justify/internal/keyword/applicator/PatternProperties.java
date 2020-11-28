/*
 * Copyright 2018-2020 the Justify authors.
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

package org.leadpony.justify.internal.keyword.applicator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.SchemaKeyword;
import org.leadpony.regexp4j.RegExp;
import org.leadpony.regexp4j.SyntaxError;

/**
 * @author leadpony
 */
@KeywordType("patternProperties")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class PatternProperties extends AbstractProperties<RegExp> {

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            if (value.getValueType() == ValueType.OBJECT) {
                Map<RegExp, JsonSchema> schemas = new LinkedHashMap<>();
                try {
                    for (Map.Entry<String, JsonValue> entry : value.asJsonObject().entrySet()) {
                        RegExp regex = new RegExp(entry.getKey());
                        schemas.put(regex, context.asJsonSchema(entry.getValue()));
                    }
                    return new PatternProperties(value, schemas);
                } catch (SyntaxError e) {
                }
            }
            throw new IllegalArgumentException();
        };
    }

    public PatternProperties(JsonValue json, Map<RegExp, JsonSchema> properties) {
        super(json, properties);
    }

    @Override
    public void addToEvaluatables(List<Evaluatable> evaluatables, Map<String, SchemaKeyword> keywords) {
        super.addToEvaluatables(evaluatables, keywords);
        if (!keywords.containsKey("properties")) {
            evaluatables.add(this);
        }
    }

    @Override
    public JsonSchema getSubschema(Iterator<String> jsonPointer) {
        if (jsonPointer.hasNext()) {
            String token = jsonPointer.next();
            for (RegExp regex : propertyMap.keySet()) {
                if (regex.getSource().equals(token)) {
                    return propertyMap.get(regex);
                }
            }
        }
        return null;
    }

    @Override
    protected boolean findSubschemas(String keyName, Consumer<JsonSchema> consumer) {
        boolean found = false;
        for (RegExp regex : propertyMap.keySet()) {
            if (regex.test(keyName)) {
                consumer.accept(propertyMap.get(regex));
                found = true;
            }
        }
        return found;
    }
}
