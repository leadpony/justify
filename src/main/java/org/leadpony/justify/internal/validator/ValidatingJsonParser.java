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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonValidatingException;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.JsonParserDecorator;
import org.leadpony.justify.internal.base.ProblemReporter;

/**
 * JSON parser with validation functionality.
 * 
 * @author leadpony
 */
public class ValidatingJsonParser extends JsonParserDecorator implements ProblemReporter {
    
    private final JsonSchema rootSchema;
    private Consumer<? super List<Problem>> problemHandler;
    private BiConsumer<Event, JsonParser> eventHandler;
    private Evaluator evaluator;
    private int depth;

    private List<Problem> currentProblems = new ArrayList<>();
    private Event eventNotDelivered;

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
    public ValidatingJsonParser withHandler(Consumer<? super List<Problem>> problemHandler) {
        this.problemHandler = (problemHandler != null) 
                ? problemHandler : this::throwProblems;
        return this;
    }
    
    @Override
    public Event next() {
        Event event = null;
        if (eventNotDelivered != null) {
            event = eventNotDelivered;
            eventNotDelivered = null;
            return event;
        }
        currentProblems.clear();
        event = super.next();
        eventHandler.accept(event, realParser());
        if (!currentProblems.isEmpty()) {
            dispatchProblems(event);
        }
        return event;
    }
  
    @Override
    public void reportProblem(Problem problem) {
        Objects.requireNonNull(problem, "problem must not be null.");
        this.currentProblems.add(problem);
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
        eventNotDelivered = event;
        this.problemHandler.accept(currentProblems);
        eventNotDelivered = null;
    }
    
    private void throwProblems(List<Problem> problems) {
        throw new JsonValidatingException(problems, realParser().getLocation());
    }
}