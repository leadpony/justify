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

package org.leadpony.justify.internal.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.ProblemDispatcher;

/**
 * Evaluator for "allOf" boolean logic.
 * 
 * @author leadpony
 */
public class ConjunctiveEvaluator extends AbstractLogicalEvaluator implements AppendableLogicalEvaluator {

    protected final List<Evaluator> children = new ArrayList<>();
    protected int evaluationsAsInvalid;
    
    ConjunctiveEvaluator() {
    }
    
    ConjunctiveEvaluator(Stream<JsonSchema> children, InstanceType type, boolean affirmative) {
        children.map(s->s.evaluator(type, Evaluators.asFactory(), affirmative))
            .forEach(this::append);
    }
    
    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        for (Evaluator child : children) {
            if (child.evaluate(event, parser, depth, dispatcher) == Result.FALSE) {
                evaluationsAsInvalid++;
            }
        }
        return (evaluationsAsInvalid == 0) ? Result.TRUE : Result.FALSE;
    }

    @Override
    public void append(Evaluator evaluator) {
        if (evaluator == Evaluators.ALWAYS_TRUE || evaluator == Evaluators.ALWAYS_IGNORED) {
            return;
        }
        this.children.add(evaluator);
    }
}
