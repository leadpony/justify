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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * Evaluator which walks object properties.
 * 
 * @author leadpony
 */
class ObjectWalker extends ContainerWalker {

    private final PropertySchemaFinder propertySchemaFinder;
    private final List<JsonSchema> foundSchemas = new ArrayList<>();

    ObjectWalker(LogicalEvaluator evaluator, PropertySchemaFinder propertySchemaFinder) {
        super(evaluator);
        this.propertySchemaFinder = propertySchemaFinder;
    }

    @Override
    protected void update(Event event, JsonParser parser, Reporter reporter) {
        switch (event) {
        case KEY_NAME:
            String name = parser.getString();
            if (!propertySchemaFinder.findSchema(name, this.foundSchemas)) {
                reportRedundantProperty(name, parser, reporter);
            }
            break;
        case END_ARRAY:
        case END_OBJECT:
            break;
        default:
            if (!foundSchemas.isEmpty()) {
                appendEvaluators(event, parser);
            }
            break;
        }
    }
    
    private void appendEvaluators(Event event, JsonParser parser) {
        InstanceType type = ParserEvents.toInstanceType(event, parser); 
        this.foundSchemas.stream()
            .map(s->s.createEvaluator(type))
            .filter(Objects::nonNull)
            .forEach(this::appendChild);
        this.foundSchemas.clear();
    }
    
    private static void reportRedundantProperty(String name, JsonParser parser, Reporter reporter) {
        Problem p = ProblemBuilder.newBuilder(parser)
                .withMessage("instance.problem.redundant.property")
                .withParameter("name", name)
                .build();
        reporter.reportProblem(p);
    }
}