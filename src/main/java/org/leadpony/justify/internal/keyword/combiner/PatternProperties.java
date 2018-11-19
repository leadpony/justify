/*
 * Copyright 2018 the Justify authors.
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * @author leadpony
 */
public class PatternProperties extends BaseProperties<Pattern> {
    
    private boolean enabled;
    
    PatternProperties() {
        this.enabled = false;
    }

    @Override
    public String name() {
        return "patternProperties";
    }

    @Override
    public boolean canEvaluate() {
        return enabled;
    }
    
    @Override
    public void link(Map<String, Keyword> siblings) {
        super.link(siblings);
        enabled = !siblings.containsKey("properties");
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
    protected void findSubschemasFor(String keyName, Collection<JsonSchema> subschemas) {
        subschemas.isEmpty();
        for (Pattern pattern : propertyMap.keySet()) {
            Matcher m = pattern.matcher(keyName);
            if (m.find()) {
                subschemas.add(propertyMap.get(pattern));
            }
        }
    }
}
