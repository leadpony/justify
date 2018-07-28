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

package org.leadpony.justify.internal.schema.io;

import java.util.ArrayList;
import java.util.List;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonValidatingException;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.schema.BasicSchemaBuilderFactory;
import org.leadpony.justify.internal.validator.ValidatingJsonParser;

/**
 * JSON schema reader which validates schema document while reading.
 * 
 * @author leadpony
 */
public class ValidatingSchemaReader extends BasicSchemaReader {
    
    private final ValidatingJsonParser parser;
    private List<Problem> problems = new ArrayList<>();

    /**
     * Constructs this schema reader.
     * 
     * @param parser the parser of JSON document.
     * @param factory the factory for producing schema builders.
     */
    public ValidatingSchemaReader(ValidatingJsonParser parser, BasicSchemaBuilderFactory factory) {
        super(parser, factory);
        this.parser = parser;
        parser.withHandler(problems::addAll);
    }

    @Override
    public JsonSchema read() {
        JsonSchema rootSchema = null;
        if (parser.hasNext()) {
            rootSchema = super.read();
        } else {
            addBlankSchemaProlem();
        }
        if (problems.isEmpty()) {
            return rootSchema;
        } else {
            throw new JsonValidatingException(
                    this.problems, 
                    parser.getLastCharLocation());
        }
    }

    @Override
    protected void addProblem(Problem problem) {
        this.problems.add(problem);
    }
    
    private void addBlankSchemaProlem() {
        Problem p = ProblemBuilder.newBuilder(parser.getLastCharLocation())
                .withMessage("schema.problem.blank")
                .build();
        addProblem(p);
    }
}
