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

package org.leadpony.justify.internal.keyword.assertion;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ParserEvents;
import org.leadpony.justify.internal.base.ProblemBuilder;

/**
 * Assertion specified with "type" validation keyword.
 * 
 * @author leadpony
 */
class Type extends ShallowAssertion {
    
    protected final Set<InstanceType> typeSet;
    
    Type(Set<InstanceType> types) {
        this.typeSet = new LinkedHashSet<>(types);
    }

    @Override
    public String name() {
        return "type";
    }
    
    @Override
    public Assertion negate() {
        return new NotType(this.typeSet);
    }

    @Override
    public void addToJson(JsonObjectBuilder builder) {
        if (typeSet.size() <= 1) {
            InstanceType type = typeSet.iterator().next();
            builder.add("type", type.name().toLowerCase());
        } else {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            typeSet.stream()
                .map(InstanceType::name)
                .map(String::toLowerCase)
                .forEach(arrayBuilder::add);
            builder.add("type", arrayBuilder);
        }
    }
    
    @Override
    protected Result evaluateShallow(Event event, JsonParser parser, int depth, Reporter reporter) {
        InstanceType type = ParserEvents.toInstanceType(event, parser);
        if (type != null) {
            return testType(type, parser, reporter);
        } else {
            return Result.TRUE;
        }
    }
  
    protected boolean contains(InstanceType type) {
        return typeSet.contains(type) ||
               (type == InstanceType.INTEGER && typeSet.contains(InstanceType.NUMBER));
    }
    
    protected Object getExpectedTypes() {
        return typeSet.size() > 1 ? typeSet : typeSet.iterator().next();
    }

    protected Result testType(InstanceType type, JsonParser parser, Reporter reporter) {
        if (contains(type)) {
            return Result.TRUE;
        } else {
            Problem p = ProblemBuilder.newBuilder(parser)
                    .withMessage(typeSet.size() > 1 ?
                            "instance.problem.type.plural" :
                            "instance.problem.type")
                    .withParameter("actual", type)
                    .withParameter("expected", getExpectedTypes())
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        }
    }
}
