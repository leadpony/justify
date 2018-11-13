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

package org.leadpony.justify.internal.validator;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonValidatingException;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.ProblemDispatcher;
import org.leadpony.justify.core.ProblemHandler;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.JsonParserDecorator;
import org.leadpony.justify.internal.evaluator.Evaluators;

/**
 * JSON parser with validation functionality.
 * 
 * @author leadpony
 */
public class ValidatingJsonParser extends JsonParserDecorator implements ProblemDispatcher {
    
    private final JsonSchema rootSchema;
    private ProblemHandler problemHandler;
    private BiConsumer<Event, JsonParser> eventHandler;
    private Evaluator evaluator;
    private int depth;

    private List<Problem> currentProblems = new ArrayList<>();

    /**
     * Constructs this parser.
     * 
     * @param real the underlying JSON parser.
     * @param rootSchema the root JSON schema to be evaluated during validation.
     * @param builderFactory the JSON builder factory.
     */
    ValidatingJsonParser(JsonParser real, JsonSchema rootSchema, JsonBuilderFactory builderFactory) {
        super(real, builderFactory);
        this.rootSchema = rootSchema;
        this.problemHandler = this::throwProblems;
        this.eventHandler = this::handleEventFirst;
    }
    
    /**
     * Assigns a problem handler this this parser.
     * 
     * @param problemHandler the problem handler to be assigned.
     * @return this parser.
     */
    public ValidatingJsonParser withHandler(ProblemHandler problemHandler) {
        this.problemHandler = (problemHandler != null) 
                ? problemHandler : this::throwProblems;
        return this;
    }
    
    @Override
    public Event next() {
        currentProblems.clear();
        Event event = super.next();
        eventHandler.accept(event, realParser());
        if (!currentProblems.isEmpty()) {
            dispatchProblems(event);
        }
        return event;
    }
  
    @Override
    public void dispatchProblem(Problem problem) {
        requireNonNull(problem, "problem");
        this.currentProblems.add(problem);
    }
    
    private void handleEventFirst(Event event, JsonParser parser) {
        InstanceType type = ParserEvents.toInstanceType(event, parser);
        this.evaluator = rootSchema.evaluator(type, Evaluators.asFactory(), true);
        if (this.evaluator != null) {
            handleEvent(event, parser);
        }
        if (this.evaluator != null) {
            this.eventHandler = this::handleEvent;
        } else {
            this.eventHandler = this::handleNone;
        }
    }

    private void handleEvent(Event event, JsonParser parser) {
        if (ParserEvents.isEndOfContainer(event)) {
            if (--depth == 0) {
                this.eventHandler = this::handleNone;
            }
        }
        Result result = evaluator.evaluate(event, parser, depth, this);
        if (ParserEvents.isStartOfContainer(event)) {
            ++depth;
        }
        if (result != Result.PENDING) {
            evaluator = null;
            this.eventHandler = this::handleNone;
        }
        if (depth == 0) {
            assert this.evaluator == null;
        }
    }

    private void handleNone(Event event, JsonParser parser) {
    }
    
    private void dispatchProblems(Event event) {
        this.problemHandler.handleProblems(currentProblems);
    }
    
    private void throwProblems(List<Problem> problems) {
        throw new JsonValidatingException(problems, getLastCharLocation());
    }
}
