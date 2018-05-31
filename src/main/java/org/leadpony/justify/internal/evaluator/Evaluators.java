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

import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;

/**
 * Utility class operating on {@link Evaluator} instances.
 * 
 * @author leadpony
 */
public final class Evaluators {

    public static LogicalEvaluator newConjunctionEvaluator(InstanceType type, boolean extensible) {
        if (extensible) {
            return new ExtensibleConjunctionEvaluator(lastEventOf(type));
        } else if (type.isContainer()) {
            return new LongConjunctionEvaluator(lastEventOf(type));
        } else {
            return new ConjunctionEvaluator();
        }
    }
    
    public static LogicalEvaluator newDisjunctionEvaluator(InstanceType type, boolean extensible) {
        if (extensible) {
            return new ExtensibleDisjunctionEvaluator(lastEventOf(type));
        } else if (type.isContainer()) {
            return new LongDisjunctionEvaluator(lastEventOf(type));
        } else {
            return new DisjunctionEvaluator();
        }
    }

    public static LogicalEvaluator newExclusiveDisjunctionEvaluator(InstanceType type, boolean extensible) {
        if (extensible) {
            throw new UnsupportedOperationException("unsupported");
        } else if (type.isContainer()) {
            return new LongExclusiveDisjunctionEvaluator(lastEventOf(type));
        } else {
            return new ExclusiveDisjunctionEvaluator();
        }
    }

    private static Event lastEventOf(InstanceType type) {
        switch (type) {
        case ARRAY:
            return Event.END_ARRAY;
        case OBJECT:
            return Event.END_OBJECT;
        default:
            return null;
        }
    }
    
    private Evaluators() {}
}
