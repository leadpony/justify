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

package org.leadpony.justify.internal.schema;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * @author leadpony
 */
class NegatedComplexSchema extends ComplexSchema {

    NegatedComplexSchema(ComplexSchema original) {
        super(original);
        this.assertions.replaceAll(Assertion::negate);
        this.properties.replaceAll((k, v)->v.negate());
        this.items.replaceAll(JsonSchema::negate);
        this.subschemas.replaceAll(JsonSchema::negate);
    }

    @Override
    protected LogicalEvaluator createLogicalEvaluator(InstanceType type, boolean extensible) {
        return Evaluators.newDisjunctionEvaluator(type, extensible);
    } 
}
