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

package org.leadpony.justify.internal.evaluator;

import javax.json.stream.JsonParser;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * @author leadpony
 */
public abstract class AbstractDisjunctivePropertiesEvaluator extends AbstractDisjunctiveChildrenEvaluator {

    public AbstractDisjunctivePropertiesEvaluator(ProblemBuilderFactory problemBuilderFactory) {
        super(InstanceType.OBJECT, problemBuilderFactory);
    }

    @Override
    protected void dispatchDefaultProblem(JsonParser parser, ProblemDispatcher dispatcher) {
        ProblemBuilder b = createProblemBuilder(parser)
                .withMessage("instance.problem.object.empty");
        dispatcher.dispatchProblem(b.build());
    }
}
