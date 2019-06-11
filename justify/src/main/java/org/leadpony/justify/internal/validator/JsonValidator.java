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
import java.util.Map;

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.leadpony.justify.internal.base.json.DefaultPointerAwareJsonParser;
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
 * A JSON parser type with validation functionality.
 *
 * @author leadpony
 */
public class JsonValidator extends DefaultPointerAwareJsonParser
    implements EvaluatorContext, DefaultProblemDispatcher, ParserEventHandler {

    private final JsonSchema rootSchema;
    private ProblemHandler problemHandler;
    private ParserEventHandler eventHandler;
    private Evaluator evaluator;
    private int depth;

    private List<Problem> currentProblems = new ArrayList<>();

    /**
     * Constructs this parser.
     *
     * @param realParser   the underlying JSON parser.
     * @param rootSchema   the root JSON schema to be evaluated during validation.
     * @param jsonProvider the JSON provider.
     */
    public JsonValidator(JsonParser realParser, JsonSchema rootSchema, JsonProvider jsonProvider) {
        super(realParser, jsonProvider);
        this.rootSchema = rootSchema;
        this.problemHandler = this::throwProblems;
        this.eventHandler = this::handleFirstEvent;
    }

    /**
     * Assigns a problem handler this this parser.
     *
     * @param problemHandler the problem handler to be assigned.
     * @return this parser.
     */
    public JsonValidator withHandler(ProblemHandler problemHandler) {
        this.problemHandler = problemHandler;
        return this;
    }

    /* AbstractJsonParser */

    @Override
    protected Event process(Event event) {
        eventHandler.handleParserEvent(event, getParser());
        return event;
    }

    @Override
    protected void postprocess() {
        if (hasProblems()) {
            dispatchProblems();
        }
    }

    /* Evaluator.Context */

    @Override
    public JsonParser getParser() {
        return getCurrentParser();
    }

    @Override
    public boolean acceptsDefaultValues() {
        return false;
    }

    @Override
    public void putDefaultProperties(Map<String, JsonValue> defaultValues) {
        assert false;
    }

    @Override
    public void putDefaultItems(List<JsonValue> items) {
        assert false;
    }

    /* DefaultProblemDispatcher */

    @Override
    public void dispatchProblem(Problem problem) {
        requireNonNull(problem, "problem");
        this.currentProblems.add(problem);
    }

    private void handleFirstEvent(Event event, JsonParser parser) {
        InstanceType type = ParserEvents.toBroadInstanceType(event);
        this.evaluator = rootSchema.createEvaluator(this, type);
        if (this.evaluator != null) {
            handleParserEvent(event, parser);
        }
        if (this.evaluator != null) {
            this.eventHandler = this;
        } else {
            this.eventHandler = ParserEventHandler.IDLE;
        }
    }

    @Override
    public void handleParserEvent(Event event, JsonParser parser) {
        // Updates the JSON pointer.
        super.process(event);
        if (ParserEvents.isEndOfContainer(event)) {
            if (--depth == 0) {
                this.eventHandler = ParserEventHandler.IDLE;
            }
        }
        Result result = evaluator.evaluate(event, depth, this);
        if (ParserEvents.isStartOfContainer(event)) {
            ++depth;
        }
        if (result != Result.PENDING) {
            evaluator = null;
            this.eventHandler = ParserEventHandler.IDLE;
        }
        if (depth == 0) {
            assert this.evaluator == null;
        }
    }

    protected final boolean hasProblems() {
        return !currentProblems.isEmpty();
    }

    /**
     * Dispatches the found problems to the handlers.
     */
    protected void dispatchProblems() {
        this.problemHandler.handleProblems(currentProblems);
        currentProblems.clear();
    }

    /**
     * Assigns a handler of parser events.
     *
     * @param eventHandler the handler of parser events.
     */
    protected void setEventHandler(ParserEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * Defaultizes the handler of parser events.
     */
    protected void resetEventHandler() {
        if (this.evaluator != null) {
            this.eventHandler = this;
        } else {
            this.eventHandler = ParserEventHandler.IDLE;
        }
    }

    private void throwProblems(List<Problem> problems) {
        assert !problems.isEmpty();
        throw new JsonValidatingException(new ArrayList<>(problems));
    }
}
