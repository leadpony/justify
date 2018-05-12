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
import java.util.function.Consumer;

import javax.json.stream.JsonParser;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ValidationResult;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.InstanceTypes;
import org.leadpony.justify.internal.base.JsonParserDecorator;

/**
 * JSON parser with validation functionality.
 * 
 * @author leadpony
 */
class ValidatingJsonParser extends JsonParserDecorator 
    implements ValidationResult, Consumer<Problem> {
    
    private final JsonSchema rootSchema;
    private int depth;
    private Evaluator evaluator;

    private final List<Problem> problems = new ArrayList<>();
    
    ValidatingJsonParser(JsonParser real, JsonSchema rootSchema) {
        super(real);
        this.rootSchema = rootSchema;
    }
 
    @Override
    public Event next() {
        Event event = real.next();
        evaluateRoot(event, real);
        return event;
    }
  
    @Override
    public void accept(Problem problem) {
        problem.setLocation(real.getLocation());
        problems.add(problem);
    }
    
    @Override
    public boolean wasValid() {
        return problems.isEmpty();
    }

    @Override
    public Iterable<Problem> problems() {
        return Collections.unmodifiableList(problems);
    }

    private void evaluateRoot(Event event, JsonParser parser) {
        if (evaluator == null) {
            evaluator = createRootEvaluator(event, parser);
        }
        if (event == Event.END_ARRAY || event == Event.END_OBJECT) {
            --depth;
        }
        evaluator.evaluate(event, parser, depth, this);
        if (event == Event.START_ARRAY || event == Event.START_OBJECT) {
            ++depth;
        }
    }
    
    private Evaluator createRootEvaluator(Event event, JsonParser parser) {
        InstanceType type = InstanceTypes.fromEvent(event, parser);
        return rootSchema.createEvaluator(type);
    }
}
