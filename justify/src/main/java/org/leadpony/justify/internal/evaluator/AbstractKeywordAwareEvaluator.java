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

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * An implementation of {@link Evaluator} which is provided by a keyword.
 *
 * @author leadpony
 */
public abstract class AbstractKeywordAwareEvaluator extends AbstractContextAwareEvaluator {

    // this can be null.
    private final Keyword keyword;

    protected AbstractKeywordAwareEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword) {
        super(context, schema);
        this.keyword = keyword;
    }

    protected final Keyword getKeyword() {
        return keyword;
    }

    @Override
    protected ProblemBuilder newProblemBuilder() {
        ProblemBuilder builder = super.newProblemBuilder();
        Keyword keyword = getKeyword();
        if (keyword != null) {
            builder.withKeyword(keyword.name());
        }
        return builder;
    }
}
