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

import java.util.function.Consumer;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * @author leadpony
 */
abstract class ContainerVisitor implements Evaluator {

    private final LogicalEvaluator logical;
    private Evaluator child;
    
    protected ContainerVisitor(LogicalEvaluator logical) {
        this.logical = logical;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        if (depth == 1) {
            update(event, parser);
        }
        return logical.evaluate(event, parser, depth, consumer);
    }
    
    protected void appendChild(Evaluator child) {
        assert this.child == null;
        this.child = child;
        this.logical.append(this::evaluateChild);
    }
    
    private Result evaluateChild(Event event, JsonParser parser, int depth, Consumer<Problem> consumer) {
        assert depth > 0;
        Result result = this.child.evaluate(event, parser, depth - 1, consumer);
        if (result == Result.TRUE || result == Result.FALSE) {
            this.child = null;
        }
        return result;
    }
    
    protected abstract void update(Event event, JsonParser parser);
}
