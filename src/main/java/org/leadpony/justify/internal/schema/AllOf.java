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

import java.util.Collection;
import java.util.function.BinaryOperator;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.JsonSchema;

/**
 * Boolean logic schema described by "allOf" keyword.
 * 
 * @author leadpony
 */
public class AllOf extends NaryBooleanLogicSchema {

    public AllOf(Collection<JsonSchema> subschemas) {
        super(subschemas);
    }

    @Override
    public String name() {
        return "allOf";
    }

    @Override
    protected BinaryOperator<Evaluator> accumulator() {
        return Evaluator::and;
    }
}
