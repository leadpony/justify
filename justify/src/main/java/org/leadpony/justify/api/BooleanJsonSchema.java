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
package org.leadpony.justify.api;

import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * A boolean type of JSON schema.
 *
 * @author leadpony
 */
abstract class BooleanJsonSchema extends SpecialJsonSchema {

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public ValueType getJsonValueType() {
        return toJson().getValueType();
    }

    static class True extends BooleanJsonSchema {

        @Override
        public Evaluator createEvaluator(EvaluatorContext context, InstanceType type) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            return alwaysFalse(context);
        }

        @Override
        public JsonValue toJson() {
            return JsonValue.TRUE;
        }

        @Override
        public String toString() {
            return "true";
        }
    }

    static class False extends BooleanJsonSchema {

        @Override
        public Evaluator createEvaluator(EvaluatorContext context, InstanceType type) {
            return alwaysFalse(context);
        }

        @Override
        public Evaluator createNegatedEvaluator(EvaluatorContext context, InstanceType type) {
            return Evaluator.ALWAYS_TRUE;
        }

        @Override
        public JsonValue toJson() {
            return JsonValue.FALSE;
        }

        @Override
        public String toString() {
            return "false";
        }
    }
}
