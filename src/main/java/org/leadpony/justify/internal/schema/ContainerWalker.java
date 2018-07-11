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

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.internal.evaluator.ExtendableLogicalEvaluator;

/**
 * Evaluator which walks a container (array or object).
 * 
 * @author leadpony
 */
abstract class ContainerWalker implements Evaluator {

    private final ExtendableLogicalEvaluator logical;
    
    protected ContainerWalker(ExtendableLogicalEvaluator logical) {
        this.logical = logical;
    }

    @Override
    public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
        if (depth == 1) {
            update(event, parser, reporter);
        }
        return logical.evaluate(event, parser, depth, reporter);
    }
    
    protected void appendChild(Evaluator child) {
        if (child == null) {
            return;
        }
        this.logical.append((event, parser, depth, reporter)->{
            assert depth > 0;
            return child.evaluate(event, parser, depth - 1, reporter);
        });
    }
    
    protected abstract void update(Event event, JsonParser parser, Reporter reporter);
}
