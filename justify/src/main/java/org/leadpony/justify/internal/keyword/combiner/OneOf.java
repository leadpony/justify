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

package org.leadpony.justify.internal.keyword.combiner;

import java.util.Collection;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.evaluator.Evaluators;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * Boolean logic specified with "oneOf" validation keyword.
 *
 * @author leadpony
 */
public class OneOf extends NaryBooleanLogic {

    public OneOf(Collection<JsonSchema> subschemas) {
        super(subschemas);
    }

    @Override
    public String name() {
        return "oneOf";
    }

    @Override
    protected LogicalEvaluator createLogicalEvaluator(EvaluatorContext context, InstanceType type) {
        return Evaluators.exclusive(context, type,
                getSubschemas().map(s -> s.createEvaluator(context, type)),
                getSubschemas().map(s -> s.createNegatedEvaluator(context, type)));
    }

    @Override
    protected LogicalEvaluator createNegatedLogicalEvaluator(EvaluatorContext context, InstanceType type) {
        LogicalEvaluator evaluator = Evaluators.notExclusive(context, type);
        getSubschemas()
                .map(s -> s.createNegatedEvaluator(context, type))
                .forEach(evaluator::append);
        return evaluator;
    }
}
