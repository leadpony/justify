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

package org.leadpony.justify.internal.keyword.annotation;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * "description" annotation keyword.
 * 
 * @author leadpony
 */
public class Default implements Annotation<JsonValue> {
    
    private final JsonValue value;
    
    public Default(JsonValue value) {
        this.value = value;
    }

    @Override
    public String name() {
        return "default";
    }

    @Override
    public void addToJson(JsonObjectBuilder builder) {
        builder.add(name(), value());
    }
    
    @Override
    public JsonValue value() {
        return value;
    }
}
