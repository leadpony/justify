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

package org.leadpony.justify.internal.schema;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.assertion.Assertion;
import org.leadpony.justify.internal.assertion.AssertionEvaluator;

/**
 * @author leadpony
 */
public class SimpleEvaluator implements Evaluator {
   
    protected final SimpleSchema schema;
    protected final List<Evaluator> assertEvaluators;
    
    SimpleEvaluator(InstanceType type, SimpleSchema schema) {
        this.schema = schema;
        this.assertEvaluators = new LinkedList<>();
        createAssertionEvaluators(type, this.assertEvaluators);
    }

    @Override
    public Status evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        return evaluateAssertions(event, parser, depth, consumer) ?
                Status.TRUE : Status.FALSE;
    }

    private void createAssertionEvaluators(InstanceType type, Collection<Evaluator> assertEvaluators) {
        for (Assertion assertion : schema.assertions()) {
            if (assertion.canApplyTo(type)) {
                AssertionEvaluator evaluator = assertion.createEvaluator();
                assertEvaluators.add(evaluator);
            }
        }
    }

    protected boolean evaluateAssertions(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        if (assertEvaluators.isEmpty()) {
            return true;
        }
        if (depth <= 1) {
            if (!invokeEvaluators(assertEvaluators, event, parser, depth, consumer)) {
                return false;
            }
        }
        return true;
    }

    protected boolean invokeEvaluators(List<Evaluator> evaluators, Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        Iterator<Evaluator> it = evaluators.iterator();
        while (it.hasNext()) {
            Evaluator evaluator = it.next();
            Status status = evaluator.evaluate(event, parser, depth, consumer);
            if (status != Status.CONTINUED) {
                it.remove();
                if (status == Status.FALSE) {
                    return false;
                }
            }
        }
        return true;
    }
}
