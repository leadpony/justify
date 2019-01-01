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

package org.leadpony.justify.internal.keyword.annotation;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

/**
 * "description" annotation keyword.
 * 
 * @author leadpony
 */
public class Description extends Annotation<String> {
    
    private final String value;
    
    public Description(String value) {
        this.value = value;
    }

    @Override
    public String name() {
        return "description";
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), value());
    }
    
    @Override
    public String value() {
        return value;
    }
}
