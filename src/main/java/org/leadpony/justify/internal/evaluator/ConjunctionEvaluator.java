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

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;

/**
 * @author leadpony
 */
class ConjunctionEvaluator extends LogicalEvaluator {
    
    private int numberOfFalses;
    
    private ConjunctionEvaluator(Combiner combiner) {
        super(combiner);
    }

    @Override
    protected boolean accumulateResult(Result result) {
        if (result == Result.FALSE) {
            this.numberOfFalses++;
        }
        return true;
    }
    
    @Override
    protected Result getFinalResult(Consumer<Problem> consumer) {
        return (this.numberOfFalses == 0) ? Result.TRUE : Result.FALSE;
    }
    
    static class Combiner extends LogicalCombiner {

        @Override
        public Combiner append(Evaluator evaluator) {
            if (evaluator != Evaluators.ALWAYS_TRUE) {
                super.append(evaluator);
            }
            return this;
        }

        @Override
        public AppendableEvaluator getAppendable() {
            return new ConjunctionEvaluator(this); 
        }
    }
}
