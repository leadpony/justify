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
package org.leadpony.justify.api;

import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * An empty JSON schema.
 *
 * @author leadpony
 */
class EmptyJsonSchema implements JsonSchema {

    /* As a EvalautorSource */

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        return Evaluator.ALWAYS_TRUE;
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        return Evaluator.alwaysFalse(parent, this);
    }

    /* As a JsonSchema */

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public ValueType getJsonValueType() {
        return ValueType.OBJECT;
    }

    @Override
    public JsonValue toJson() {
        return JsonValue.EMPTY_JSON_OBJECT;
    }

    @Override
    public String toString() {
        return "{}";
    }
}
