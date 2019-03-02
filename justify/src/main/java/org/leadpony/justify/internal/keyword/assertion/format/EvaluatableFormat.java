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

package org.leadpony.justify.internal.keyword.assertion.format;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.JsonBuilderFactory;
import javax.json.JsonValue;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A format which can be evaluated.
 *
 * @author leadpony
 */
public class EvaluatableFormat extends Format implements Evaluator {

    private final FormatAttribute attribute;

    public EvaluatableFormat(FormatAttribute attribute) {
        super(attribute.name());
        this.attribute = attribute;
    }

    @Override
    public boolean supportsType(InstanceType type) {
        return type == attribute.valueType();
    }

    @Override
    public Set<InstanceType> getSupportedTypes() {
        return EnumSet.of(attribute.valueType());
    }

    @Override
    public void addToEvaluatables(List<Keyword> evaluatables, Map<String, Keyword> keywords) {
        evaluatables.add(this);
    }

    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return this;
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return this::evaluateNegated;
    }

    @Override
    public Result evaluate(Event event, EvaluatorContext context, int depth, ProblemDispatcher dispatcher) {
        JsonValue value = context.getParser().getValue();
        if (attribute.test(value)) {
            return Result.TRUE;
        } else {
            ProblemBuilder builder = createProblemBuilder(context)
                    .withMessage("instance.problem.format");
            dispatcher.dispatchProblem(builder.build());
            return Result.FALSE;
        }
    }

    private Result evaluateNegated(Event event, EvaluatorContext context, int depth, ProblemDispatcher dispatcher) {
        JsonValue value = context.getParser().getValue();
        if (attribute.test(value)) {
            ProblemBuilder builder = createProblemBuilder(context)
                    .withMessage("instance.problem.not.format");
            dispatcher.dispatchProblem(builder.build());
            return Result.FALSE;
        } else {
            return Result.TRUE;
        }
    }

    @Override
    public ProblemBuilder createProblemBuilder(EvaluatorContext context) {
        return super.createProblemBuilder(context)
                    .withParameter("attribute", attribute.name())
                    .withParameter("localizedAttribute", attribute.localizedName());
    }
}
