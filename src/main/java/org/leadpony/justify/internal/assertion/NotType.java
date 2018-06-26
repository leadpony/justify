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

package org.leadpony.justify.internal.assertion;

import java.util.Set;

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * @author leadpony
 */
class NotType extends Type {

    NotType(Set<InstanceType> types) {
        super(types);
    }
    
    @Override
    protected Result testType(InstanceType type, JsonParser parser, Reporter reporter) {
        if (contains(type)) {
            Problem p = ProblemBuilder.newBuilder(parser)
                    .withMessage("instance.problem.not.type")
                    .withParameter("actual", type)
                    .withParameter("expected", typeSet)
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        } else {
            return Result.TRUE;
        }
    }

    @Override
    protected AbstractAssertion createNegatedAssertion() {
        return new Type(this.typeSet);
    }
}
