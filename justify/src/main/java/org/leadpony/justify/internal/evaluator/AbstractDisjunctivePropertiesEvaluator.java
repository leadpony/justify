/*
 * Copyright 2018-2020 the Justify authors.
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

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * @author leadpony
 */
public abstract class AbstractDisjunctivePropertiesEvaluator extends AbstractDisjunctiveChildrenEvaluator {

    public AbstractDisjunctivePropertiesEvaluator(Evaluator parent, Keyword keyword) {
        super(parent, keyword, Event.END_OBJECT);
    }

    @Override
    protected void dispatchDefaultProblem(ProblemDispatcher dispatcher) {
        ProblemBuilder b = newProblemBuilder()
                .withMessage(Message.INSTANCE_PROBLEM_OBJECT_EMPTY);
        dispatcher.dispatchProblem(b.build());
    }
}
