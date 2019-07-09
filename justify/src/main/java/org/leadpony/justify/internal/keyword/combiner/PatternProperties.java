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

package org.leadpony.justify.internal.keyword.combiner;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.SchemaKeyword;

/**
 * @author leadpony
 */
@KeywordType("patternProperties")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class PatternProperties extends AbstractProperties<Pattern> {

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            if (value.getValueType() == ValueType.OBJECT) {
                Map<Pattern, JsonSchema> schemas = new LinkedHashMap<>();
                try {
                    for (Map.Entry<String, JsonValue> entry : value.asJsonObject().entrySet()) {
                        Pattern pattern = Pattern.compile(entry.getKey());
                        schemas.put(pattern, context.asJsonSchema(entry.getValue()));
                    }
                    return new PatternProperties(schemas);
                } catch (PatternSyntaxException e) {
                }
            }
            throw new IllegalArgumentException();
        };
    }

    public PatternProperties(Map<Pattern, JsonSchema> properties) {
        super(properties);
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
            for (Pattern key : propertyMap.keySet()) {
                if (key.pattern().equals(token)) {
                    return propertyMap.get(key);
                }
            }
        }
        return null;
    }

    @Override
    protected boolean findSubschemas(String keyName, Consumer<JsonSchema> consumer) {
        boolean found = false;
        for (Pattern pattern : propertyMap.keySet()) {
            Matcher m = pattern.matcher(keyName);
            if (m.find()) {
                consumer.accept(propertyMap.get(pattern));
                found = true;
            }
        }
        return found;
    }
}
