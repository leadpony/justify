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
package org.leadpony.justify.internal.schema.io;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaResolver;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;
import org.leadpony.justify.internal.problem.ProblemRenderer;

/**
 * A minimal implementation of {@link JsonSchemaReader}.
 *
 * @author leadpony
 */
abstract class AbstractJsonSchemaReader implements JsonSchemaReader, ProblemBuilderFactory  {

    static final URI DEFAULT_INITIAL_BASE_URI = URI.create("");

    private final Map<String, Object> config;

    private final boolean strictWithKeywords;
    private final boolean strictWithFormats;
    private final List<JsonSchemaResolver> resolvers;

    private boolean alreadyRead;
    private boolean alreadyClosed;

    // problems found by the schema validator.
    private final List<Problem> problems = new ArrayList<>();

    @SuppressWarnings("unchecked")
    protected AbstractJsonSchemaReader(Map<String, Object> config) {
        this.config = config;
        this.strictWithKeywords = config.get(STRICT_KEYWORDS) == Boolean.TRUE;
        this.strictWithFormats = config.get(STRICT_FORMATS) == Boolean.TRUE;
        this.resolvers = (List<JsonSchemaResolver>) config.getOrDefault(RESOLVERS, Collections.emptyList());
    }

    @Override
    public final JsonSchema read() {
        if (this.alreadyClosed) {
            throw new IllegalStateException("already closed.");
        } else if (this.alreadyRead) {
            throw new IllegalStateException("already read.");
        }
        try {
            return readSchema();
        } finally {
            alreadyRead = true;
        }
    }

    @Override
    public final void close() {
        if (!this.alreadyClosed) {
            try {
                closeParser();
            } finally {
                alreadyClosed = true;
            }
        }
    }

    final Map<String, Object> getConfig() {
        return config;
    }

    final boolean isStrictWithKeywords() {
        return strictWithKeywords;
    }

    final boolean isStrictWithFormats() {
        return strictWithFormats;
    }

    final List<JsonSchemaResolver> getResolvers() {
        return resolvers;
    }

    protected void addProblem(Problem problem) {
        this.problems.add(problem);
    }

    protected void addProblem(ProblemBuilder problemBuilder) {
        addProblem(problemBuilder.build());
    }

    protected void addProblems(List<Problem> problems) {
        this.problems.addAll(problems);
    }

    protected void dispatchProblems() {
        if (!problems.isEmpty()) {
            throw new JsonValidatingException(this.problems, ProblemRenderer.DEFAULT_RENDERER);
        }
    }

    protected abstract JsonSchema readSchema();

    protected abstract void closeParser();
}
