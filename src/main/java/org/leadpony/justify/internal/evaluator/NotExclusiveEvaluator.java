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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.core.ProblemDispatcher;

/**
 * @author leadpony
 */
class NotExclusiveEvaluator extends AbstractLogicalEvaluator {

    protected final List<RetainingEvaluator> children;
    protected List<RetainingEvaluator> badEvaluators;
    
    NotExclusiveEvaluator(Stream<JsonSchema> children, InstanceType type) {
        this.children = children
                .map(s->s.evaluator(type, Evaluators.asFactory(), false))
                .map(RetainingEvaluator::new)
                .collect(Collectors.toList());
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
        Iterator<RetainingEvaluator> it = children.iterator();
        while (it.hasNext()) {
            RetainingEvaluator current = it.next();
            if (current.evaluate(event, parser, depth, dispatcher) == Result.FALSE) {
                addBadEvaluator(current);
            }
        }
        if (badEvaluators == null || badEvaluators.size() != 1) {
            return Result.TRUE;
        } else {
            return dispatchProblems(badEvaluators.get(0), dispatcher);
        }
    }

    protected void addBadEvaluator(RetainingEvaluator evaluator) {
        if (this.badEvaluators == null) {
            this.badEvaluators = new ArrayList<>();
        }
        this.badEvaluators.add(evaluator);
    }
    
    protected Result dispatchProblems(RetainingEvaluator evaluator, ProblemDispatcher dispatcher) {
        List<Problem> problems = evaluator.problems();
        problems.forEach(dispatcher::dispatchProblem);
        return Result.FALSE;
    }
}
