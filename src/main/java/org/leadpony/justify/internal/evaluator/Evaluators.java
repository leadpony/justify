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

import static org.leadpony.justify.internal.base.ParserEvents.lastEventOf;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;

/**
 * Utility class operating on {@link Evaluator} instances.
 * 
 * @author leadpony
 */
public final class Evaluators {

    private Evaluators() {
    }

    public static LogicalEvaluator.Builder newConjunctionEvaluatorBuilder(InstanceType type) {
        return ConjunctionEvaluator.builder(type);
    }
    
    public static DynamicLogicalEvaluator newConjunctionChildEvaluator(InstanceType type) {
        return new DynamicConjunctionEvaluator(lastEventOf(type));
    }
    
    public static LogicalEvaluator.Builder newDisjunctionEvaluatorBuilder(InstanceType type) {
        return DisjunctionEvaluator.builder(type);
    }

    public static DynamicLogicalEvaluator newDisjunctionChildEvaluator(InstanceType type) {
        return new DynamicDisjunctionEvaluator(lastEventOf(type));
    }

    public static LogicalEvaluator.Builder newExclusiveDisjunctionEvaluatorBuilder(InstanceType type) {
        return ExclusiveDisjunctionEvaluator.builder(type);
    }
}