/*
 * Copyright 2018-2002 the Justify authors.
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
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.EvaluatorSource;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordParser;
import org.leadpony.justify.api.keyword.KeywordType;
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
        public Keyword parse(KeywordParser parser, JsonBuilderFactory factory) {
            if (parser.next() == Event.START_OBJECT) {
                JsonObjectBuilder builder = factory.createObjectBuilder();
                Map<Pattern, JsonSchema> schemas = new LinkedHashMap<>();
                while (parser.hasNext() && parser.next() != Event.END_OBJECT) {
                    String name = parser.getString();
                    Pattern pattern = Pattern.compile(name);
                    parser.next();
                    JsonSchema schema = parser.getSchema();
                    schemas.put(pattern, schema);
                    builder.add(name, schema.toJson());
                }
                return new PatternProperties(builder.build(), schemas);
            }
            throw new IllegalStateException();
        }
    };

    private Properties properties;

    public PatternProperties(JsonValue json, Map<Pattern, JsonSchema> properties) {
        super(json, properties);
    }

    @Override
    public KeywordType getType() {
        return TYPE;
    }

    @Override
    public boolean canEvaluate() {
        return true;
    }

    @Override
    public Optional<EvaluatorSource> getEvaluatorSource(Map<String, Keyword> siblings) {
        super.getEvaluatorSource(siblings);
        this.properties = (Properties) siblings.get("properties");
        if (this.properties == null) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
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
