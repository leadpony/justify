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

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.InstanceTypes;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * Evaluator which walks array items.
 * 
 * @author leadpony
 */
class ArrayWalker extends ContainerWalker {

    private final ItemSchemaFinder itemSchemaFinder;
    private int itemIndex;
    private boolean redundantItemFound;
    
    ArrayWalker(LogicalEvaluator evaluator, ItemSchemaFinder itemSchemaFinder) {
        super(evaluator);
        this.itemSchemaFinder = itemSchemaFinder;
    }
    
    @Override
    protected void update(Event event, JsonParser parser, Reporter reporter) {
        switch (event) {
        case END_ARRAY:
        case END_OBJECT:
            break;
        default:
            JsonSchema schema = itemSchemaFinder.findSchema(itemIndex);
            if (schema != null) {
                InstanceType type = InstanceTypes.fromEvent(event, parser); 
                appendChild(schema.createEvaluator(type));
            } else {
                reportRedundantItem(itemIndex, parser, reporter);
            }
            itemIndex++;
            break;
        }
    }
    
    private void reportRedundantItem(int index, JsonParser parser, Reporter reporter) {
        if (redundantItemFound) {
            return;
        }
        redundantItemFound = true;
        Problem p = ProblemBuilder.newBuilder(parser)
                .withMessage("instance.problem.redundant.item")
                .withParameter("index", index)
                .build();
        reporter.reportProblem(p);
    }
}