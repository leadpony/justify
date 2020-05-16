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

package org.leadpony.justify.internal.keyword.applicator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.KeywordMapper;

/**
 * @author leadpony
 */
@KeywordType("patternProperties")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class PatternProperties extends AbstractProperties<Pattern> {

    private Properties properties;

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
                    return new PatternProperties(value, schemas);
                } catch (PatternSyntaxException e) {
                }
            }
            throw new IllegalArgumentException();
        };
    }

    public PatternProperties(JsonValue json, Map<Pattern, JsonSchema> properties) {
        super(json, properties);
    }

    @Override
    public Keyword link(Map<String, Keyword> siblings) {
        super.link(siblings);
        this.properties = (Properties) siblings.get("properties");
        return this;
    }

    @Override
    public boolean canEvaluate() {
        return this.properties == null;
    }

    @Override
    public Optional<JsonSchema> findSchema(String token) {
        for (Pattern key : propertyMap.keySet()) {
            if (key.pattern().equals(token)) {
                return Optional.of(propertyMap.get(key));
            }
        }
        return Optional.empty();
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
