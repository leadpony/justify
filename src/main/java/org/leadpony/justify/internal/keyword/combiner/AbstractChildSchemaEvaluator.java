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

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.internal.evaluator.DynamicLogicalEvaluator;

/**
 * @author leadpony
 */
abstract class AbstractChildSchemaEvaluator implements Evaluator {
    
    private final DynamicLogicalEvaluator dynamicEvaluator;
    
    protected AbstractChildSchemaEvaluator(DynamicLogicalEvaluator dynamicEvaluator) {
        this.dynamicEvaluator = dynamicEvaluator;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
        if (depth == 1) {
            update(event, parser, reporter);
        }
        return dynamicEvaluator.evaluate(event, parser, depth, reporter);
    }
    
    /**
     * Appends evaluator for a child instance.
     * 
     * @param evaluator the evaluator to append, cannot be {@code null}.
     */
    protected void appendChild(Evaluator evaluator) {
        assert evaluator != null;
        dynamicEvaluator.append(evaluator);
    }
    
    protected abstract void update(Event event, JsonParser parser, Reporter reporter);
}
