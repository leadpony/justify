/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.internal.evaluator.schema;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.EvaluatorSource;
import org.leadpony.justify.internal.evaluator.UnsupportedTypeEvaluator;

import jakarta.json.stream.JsonParser.Event;

/**
 * @author leadpony
 */
public final class SimpleSchemaBasedEvaluator extends AbstractSchemaBasedEvaluator {

    public static Evaluator of(EvaluatorSource source, Evaluator parent,
            JsonSchema schema,
            InstanceType type) {
        if (source.supportsType(type)) {
            SimpleSchemaBasedEvaluator self = new SimpleSchemaBasedEvaluator(parent, schema);
            self.child = source.createEvaluator(self, type);
            return self;
        } else {
            return Evaluator.ALWAYS_TRUE;
        }
    }

    public static Evaluator ofNegated(EvaluatorSource source, Evaluator parent,
            JsonSchema schema,
            InstanceType type) {
        SimpleSchemaBasedEvaluator self = new SimpleSchemaBasedEvaluator(parent, schema);
        if (source.supportsType(type)) {
            self.child = source.createNegatedEvaluator(self, type);
        } else {
            self.child = new UnsupportedTypeEvaluator(self, source, type);
        }
        return self;
    }

    private Evaluator child;

    private SimpleSchemaBasedEvaluator(Evaluator parent, JsonSchema schema) {
        super(parent, schema);
    }

    @Override
    public Result evaluate(Event event, int depth) {
        return child.evaluate(event, depth);
    }
}
