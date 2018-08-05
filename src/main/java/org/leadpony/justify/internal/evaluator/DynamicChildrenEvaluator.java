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

package org.leadpony.justify.internal.evaluator;

import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * @author leadpony
 */
public abstract class DynamicChildrenEvaluator implements Evaluator {
    
    private final boolean affirmative;
    private final DynamicLogicalEvaluator childrenEvaluator;
    
    protected DynamicChildrenEvaluator(boolean affirmative, Event stopEvent, ProblemBuilderFactory problemFactory) {
        this.affirmative = affirmative;
        if (affirmative) {
            childrenEvaluator = new DynamicConjunctionEvaluator(stopEvent);
        } else {
            childrenEvaluator = new DynamicDisjunctionEvaluator(stopEvent, problemFactory);
        }
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> reporter) {
        if (depth == 1) {
            update(event, parser, reporter);
        }
        return childrenEvaluator.evaluate(event, parser, depth, reporter);
    }
    
    public boolean isAffirmative() {
        return affirmative;
    }
    
    public JsonSchema getSchemaToFail() {
        return JsonSchema.valueOf(!isAffirmative());
    }
    
    protected void append(JsonSchema schema, InstanceType type) {
        assert schema != null;
        Evaluator evaluator = schema.createEvaluator(type, Evaluators.asFactory(), isAffirmative());
        append(evaluator);
    }

    protected void append(Evaluator evaluator) {
        assert evaluator != null;
        childrenEvaluator.append(evaluator);
    }
    
    protected abstract void update(Event event, JsonParser parser, Consumer<Problem> reporter);
}
