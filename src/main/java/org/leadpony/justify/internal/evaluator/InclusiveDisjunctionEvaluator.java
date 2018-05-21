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

import java.util.Optional;

import org.leadpony.justify.core.Evaluator;

/**
 * @author leadpony
 */
class InclusiveDisjunctionEvaluator extends DisjunctionEvaluator {
    
    private InclusiveDisjunctionEvaluator(Combiner combiner) {
        super(combiner);
    }
    
    @Override
    protected boolean accumulateResult(Result result) {
        if (result == Result.TRUE) {
            this.numberOfTrues++;
        }
        return (this.numberOfTrues == 0);
    }
    
    static class Combiner extends LogicalCombiner {
        
        private boolean containsAlwaysTrue;
        
        @Override
        public Combiner append(Evaluator evaluator) {
            if (containsAlwaysTrue) {
                return this;
            } else if (evaluator == Evaluators.ALWAYS_TRUE) {
                this.containsAlwaysTrue = true;
                return this;
            }
            super.append(evaluator);
            return this;
        }
        
        @Override
        public Optional<Evaluator> getCombined() {
            if (this.containsAlwaysTrue) {
                return Optional.of(Evaluators.ALWAYS_TRUE);
            } else {
                return super.getCombined();
            }
        }

        @Override
        public AppendableEvaluator getAppendable() {
            return new InclusiveDisjunctionEvaluator(this);
        }
    }
}
