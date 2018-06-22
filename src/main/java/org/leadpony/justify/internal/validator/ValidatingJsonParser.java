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
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Evaluator.Result;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonValidator;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.InstanceTypes;
import org.leadpony.justify.internal.base.JsonParserDecorator;
import org.leadpony.justify.internal.base.BasicProblemReporter;

/**
 * JSON parser with validation functionality.
 * 
 * @author leadpony
 */
public class ValidatingJsonParser extends JsonParserDecorator 
        implements JsonValidator, BasicProblemReporter {
    
    private BiConsumer<Event, JsonParser> eventHandler;
    private final JsonSchema rootSchema;
    private Evaluator evaluator;
    private int depth;

    private final List<Problem> problems = new ArrayList<>();
    
    ValidatingJsonParser(JsonParser real, JsonSchema rootSchema, JsonProvider jsonProvider) {
        super(real, jsonProvider);
        this.rootSchema = rootSchema;
        this.eventHandler = this::handleEventFirst;
    }
 
    @Override
    public Event next() {
        Event event = super.next();
        eventHandler.accept(event, realParser());
        return event;
    }
  
    @Override
    public void reportProblem(Problem problem) {
        assert problem != null;
        this.problems.add(problem);
    }
    
    @Override
    public boolean hasProblem() {
        return problems.size() > 0;
    }

    @Override
    public List<Problem> getProblems() {
        return Collections.unmodifiableList(problems);
    }

    private void handleEventFirst(Event event, JsonParser parser) {
        InstanceType type = InstanceTypes.fromEvent(event, parser);
        this.evaluator = rootSchema.createEvaluator(type);
        if (this.evaluator != null) {
            handleEvent(event, parser);
        }
        if (depth > 0) {
            this.eventHandler = this::handleEvent;
        } else {
            this.eventHandler = this::handleNone;
        }
    }

    private void handleEvent(Event event, JsonParser parser) {
        if (event == Event.END_ARRAY || event == Event.END_OBJECT) {
            if (--depth == 0) {
                this.eventHandler = this::handleNone;
            }
        }
        Result result = evaluator.evaluate(event, parser, depth, this);
        if (event == Event.START_ARRAY || event == Event.START_OBJECT) {
            ++depth;
        }
        if (depth == 0) {
            assert result != Result.PENDING : result;
        }
    }

    private void handleNone(Event event, JsonParser parser) {
    }
}
