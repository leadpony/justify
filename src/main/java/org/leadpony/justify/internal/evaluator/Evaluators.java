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

    public static LogicalEvaluator.Builder newConjunctionEvaluatorBuilder(InstanceType type) {
        if (type.isContainer()) {
            return LongConjunctionEvaluator.builder(lastEventOf(type));
        } else {
            return ConjunctionEvaluator.builder();
        }
    }
    
    public static ExtendableLogicalEvaluator newConjunctionChildEvaluator(InstanceType type) {
        return new ConjunctionChildEvaluator(lastEventOf(type));
    }
    
    public static LogicalEvaluator.Builder newDisjunctionEvaluatorBuilder(InstanceType type) {
        if (type.isContainer()) {
            return LongDisjunctionEvaluator.builder(lastEventOf(type));
        } else {
            return DisjunctionEvaluator.builder();
        }
    }

    public static ExtendableLogicalEvaluator newDisjunctionChildEvaluator(InstanceType type) {
        return new DisjunctionChildEvaluator(lastEventOf(type));
    }

    public static LogicalEvaluator.Builder newExclusiveDisjunctionEvaluatorBuilder(InstanceType type) {
        if (type.isContainer()) {
            return LongExclusiveDisjunctionEvaluator.builder(lastEventOf(type));
        } else {
            return ExclusiveDisjunctionEvaluator.builder();
        }
    }

    private Evaluators() {
    }
}
