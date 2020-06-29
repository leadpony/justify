/*
 * Copyright 2018, 2020 the Justify authors.
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
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.InvalidKeywordException;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.api.keyword.SubschemaParser;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;

/**
 * @author leadpony
 */
@KeywordClass("patternProperties")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class PatternProperties extends AbstractProperties<Pattern> {

    public static final KeywordType TYPE = new KeywordType() {

        @Override
        public String name() {
            return "patternProperties";
        }

        @Override
        public Keyword createKeyword(JsonValue jsonValue, SubschemaParser schemaParser) {
            if (jsonValue.getValueType() != ValueType.OBJECT) {
                throw new InvalidKeywordException("Must be an object");
            }
            Map<Pattern, JsonSchema> schemas = new LinkedHashMap<>();
            try {
                for (Map.Entry<String, JsonValue> entry : jsonValue.asJsonObject().entrySet()) {
                    String name = entry.getKey();
                    Pattern pattern = Pattern.compile(name);
                    JsonSchema schema = schemaParser.parseSubschema(entry.getValue(), name);
                    schemas.put(pattern, schema);
                }
                return new PatternProperties(jsonValue, schemas);
            } catch (PatternSyntaxException e) {
                throw new InvalidKeywordException("Must be a pattern", e);
            }
        }
    };

    private final Properties properties;

    public PatternProperties(JsonValue json, Map<Pattern, JsonSchema> propertyMap) {
        this(json, propertyMap, null, null);
    }

    public PatternProperties(JsonValue json, Map<Pattern, JsonSchema> propertyMap,
            Properties properties,
            AdditionalProperties additionalProperties) {
        super(json, propertyMap, additionalProperties);
        this.properties = properties;
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public boolean canEvaluate() {
        return properties == null;
    }

    @Override
    public Keyword withKeywords(Map<String, Keyword> siblings) {
        Properties properties = (Properties) siblings.get("properties");
        AdditionalProperties additionalProperties = getAdditionalProperties(siblings);
        if (properties != null || additionalProperties != null) {
            return new PatternProperties(getValueAsJson(), propertyMap,
                    properties, additionalProperties);
        } else {
            return this;
        }
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
