/*
 * Copyright 2020 the Justify authors.
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

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.base.Message;

/**
 * A skeletal evaluator for "dependencies", "dependentSchemas", and
 * "dependentRequired" keywords.
 *
 * @author leadpony
 */
public abstract class AbstractPropertyDependentEvaluator extends AbstractKeywordBasedEvaluator {

    private final String propertyName;

    protected AbstractPropertyDependentEvaluator(Evaluator parent, Keyword keyword,
            String propertyName) {
        super(parent, keyword);
        this.propertyName = propertyName;
    }

    public final String getPropertyName() {
        return propertyName;
    }

    protected final Evaluator.Result dispatchMissingPropertyProblem(ProblemDispatcher dispatcher) {
        Problem p = newProblemBuilder()
                .withMessage(Message.INSTANCE_PROBLEM_REQUIRED)
                .withParameter("required", propertyName)
                .build();
        dispatcher.dispatchProblem(p);
        return Evaluator.Result.FALSE;
    }
}
