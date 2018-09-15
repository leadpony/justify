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

package org.leadpony.justify.internal.keyword.assertion.format;

import java.io.InputStream;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

/**
 * @author leadpony
 */
public class RegexFixture {

    private final String pattern;
    private final boolean valid;
    private final String flags;
    private boolean skip;

    static Stream<RegexFixture> load(String name) {
        InputStream in = RegexFixture.class.getResourceAsStream(name);
        try (JsonReader reader = Json.createReader(in)) {
            JsonArray array = reader.readArray();
            return array.stream()
                    .map(JsonValue::asJsonObject)
                    .map(RegexFixture::new)
                    .filter(f->!f.skip)
                    ;
        }
    }
    
    /**
     * Constructs this fixture.
     * 
     * @param object the JSON object containing the fixture.
     */
    private RegexFixture(JsonObject object) {
        this.pattern = object.getString("pattern");
        this.valid = object.getBoolean("valid");
        if (object.containsKey("flags")) {
            this.flags = object.getString("flags");
        } else {
            this.flags = "";
        }
        if (object.containsKey("skip")) {
            this.skip = object.getBoolean("skip");
        }
    }
  
    String pattern() {
        return pattern;
    }
    
    boolean result() {
        return valid;
    }
    
    String flags() {
        return flags;
    }

    @Override
    public String toString() {
        return "/" + pattern() + "/" + flags();
    }
}
