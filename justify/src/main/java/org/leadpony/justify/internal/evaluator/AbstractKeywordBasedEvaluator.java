/*
 * Copyright 2018, 2020 the Justify authors.
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
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.base.json.SimpleJsonLocation;
import org.leadpony.justify.internal.problem.ProblemBuilder;

import jakarta.json.stream.JsonLocation;

/**
 * An implementation of {@link Evaluator} which is provided by a keyword.
 *
 * @author leadpony
 */
public abstract class AbstractKeywordBasedEvaluator extends AbstractEvaluator {

    private final Keyword keyword;
    private EvaluatorContext context;

    protected AbstractKeywordBasedEvaluator(Evaluator parent, Keyword keyword) {
        super(parent);
        this.keyword = keyword;
    }

    @Override
    public final EvaluatorContext getContext() {
        if (context != null) {
            return context;
        }
        this.context = getParent().getContext();
        return context;
    }

    public final Keyword getKeyword() {
        return keyword;
    }

    protected ProblemBuilder newProblemBuilder() {
        EvaluatorContext context = getContext();
        JsonLocation location = getParser().getLocation();
        String pointer = context.getPointer();
        ProblemBuilder builder = new ProblemBuilder(SimpleJsonLocation.before(location), pointer)
            .withSchema(getSchema())
            .withKeyword(getKeyword().name());
        return builder;
    }
}
