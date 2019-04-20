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

import java.util.ArrayList;
import java.util.List;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * A minimal implementation of {@link JsonSchemaReader}.
 *
 * @author leadpony
 */
abstract class AbstractSchemaReader implements JsonSchemaReader, ProblemBuilderFactory  {

    private boolean alreadyRead;
    private boolean alreadyClosed;

    // problems found by the schema validator.
    private final List<Problem> problems = new ArrayList<>();

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
            throw new JsonValidatingException(this.problems);
        }
    }

    protected abstract JsonSchema readSchema();

    protected abstract void closeParser();
}
