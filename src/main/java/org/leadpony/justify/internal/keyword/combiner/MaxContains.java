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

import javax.json.JsonObjectBuilder;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.internal.evaluator.EvaluatorAppender;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * @author leadpony
 */
class MaxContains implements Keyword {
    
    private final int value;
    
    MaxContains(int value) {
        this.value = value;
    }

    int value() {
        return value;
    }
    
    @Override
    public String name() {
        return "maxContains";
    }

    @Override
    public boolean canEvaluate() {
        return false;
    }
    
    @Override
    public void createEvaluator(InstanceType type, EvaluatorAppender appender) {
    }

    @Override
    public void addToJson(JsonObjectBuilder builder) {
        builder.add(name(), value);
    }
}
