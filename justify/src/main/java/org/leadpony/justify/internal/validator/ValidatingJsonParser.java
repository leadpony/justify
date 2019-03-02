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

package org.leadpony.justify.internal.validator;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.json.JsonPointer;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.leadpony.justify.internal.base.json.JsonParserDecorator;
import org.leadpony.justify.internal.base.json.JsonPointerBuilder;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.problem.DefaultProblemDispatcher;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.Evaluator.Result;

/**
 * JSON parser with validation functionality.
 *
 * @author leadpony
 */
public class ValidatingJsonParser extends JsonParserDecorator implements EvaluatorContext, DefaultProblemDispatcher {

    private final JsonSchema rootSchema;
    @SuppressWarnings("unused")
    private final JsonProvider jsonProvider;
    private ProblemHandler problemHandler;
    private BiConsumer<Event, JsonParser> eventHandler;
    private Evaluator evaluator;
    private int depth;

    private JsonPointerBuilder jsonPointerBuilder;
    private List<Problem> currentProblems = new ArrayList<>();

    /**
     * Constructs this parser.
     *
     * @param real the underlying JSON parser.
     * @param rootSchema the root JSON schema to be evaluated during validation.
     * @param jsonProvider the JSON provider.
     */
    public ValidatingJsonParser(JsonParser real, JsonSchema rootSchema, JsonProvider jsonProvider) {
        super(real, jsonProvider.createBuilderFactory(null));
        this.rootSchema = rootSchema;
        this.jsonProvider = jsonProvider;
        this.problemHandler = this::throwProblems;
        this.eventHandler = this::handleEventFirst;
        this.jsonPointerBuilder = JsonPointerBuilder.newInstane();
    }

    /**
     * Assigns a problem handler this this parser.
     *
     * @param problemHandler the problem handler to be assigned.
     * @return this parser.
     */
    public ValidatingJsonParser withHandler(ProblemHandler problemHandler) {
        this.problemHandler = problemHandler;
        return this;
    }

    @Override
    public Event next() {
        currentProblems.clear();
        Event event = super.next();
        updateJsonPointer(event, realParser());
        eventHandler.accept(event, realParser());
        if (!currentProblems.isEmpty()) {
            dispatchProblems();
        }
        return event;
    }

    /* Evaluator.Context */

    @Override
    public JsonParser getParser() {
        return realParser();
    }

    @Override
    public JsonPointer getPointer() {
        return jsonProvider.createPointer(jsonPointerBuilder.toPointer());
    }

    /* DefaultProblemDispatcher */

    @Override
    public void dispatchProblem(Problem problem) {
        requireNonNull(problem, "problem");
        this.currentProblems.add(problem);
    }

    private void updateJsonPointer(Event event, JsonParser parser) {
        jsonPointerBuilder = jsonPointerBuilder.withEvent(event, parser);
    }

    private void handleEventFirst(Event event, JsonParser parser) {
        InstanceType type = ParserEvents.toInstanceType(event, parser);
        this.evaluator = rootSchema.createEvaluator(type);
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
        Result result = evaluator.evaluate(event, this, depth, this);
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

    private void dispatchProblems() {
        this.problemHandler.handleProblems(currentProblems);
    }

    private void throwProblems(List<Problem> problems) {
        assert !problems.isEmpty();
        throw new JsonValidatingException(problems);
    }
}
