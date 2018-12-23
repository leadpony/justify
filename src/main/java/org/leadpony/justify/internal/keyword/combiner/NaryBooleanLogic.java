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

package org.leadpony.justify.internal.keyword.combiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * N-ary boolean logic.
 * This class is the abstract base class for {@link AllOf}, {@link AnyOf} and {@link OneOf}.
 * 
 * @author leadpony
 */
abstract class NaryBooleanLogic extends Combiner {
   
    private final List<JsonSchema> subschemas;
    
    protected NaryBooleanLogic(Collection<JsonSchema> subschemas) {
        this.subschemas = new ArrayList<>(subschemas);
    }

    @Override
    protected Evaluator doCreateEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return createLogicalEvaluator(type).withProblemBuilderFactory(this);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(InstanceType type, JsonBuilderFactory builderFactory) {
        return createNegatedLogicalEvaluator(type).withProblemBuilderFactory(this);
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        JsonArrayBuilder arrayBuilder = builderFactory.createArrayBuilder();
        this.subschemas.stream()
            .map(JsonSchema::toJson)
            .forEach(arrayBuilder::add);
        builder.add(name(), arrayBuilder);
    }

    @Override
    public boolean hasSubschemas() {
        return !subschemas.isEmpty();
    }

    @Override
    public Stream<JsonSchema> subschemas() {
        return this.subschemas.stream();
    }
   
    @Override
    public JsonSchema getSubschema(Iterator<String> jsonPointer) {
        if (jsonPointer.hasNext()) {
            try {
                int index = Integer.parseInt(jsonPointer.next());
                if (index < subschemas.size()) {
                    return subschemas.get(index);
                }
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }
    
    /**
     * Creates a new evaluator for this boolean logic.
     * @param type the type of the instance to validate.
     * @return newly created evaluator.
     */
    protected abstract LogicalEvaluator createLogicalEvaluator(InstanceType type);

    /**
     * Creates a new evaluator for the negated version of this boolean logic.
     * @param type the type of the instance to validate.
     * @return newly created evaluator.
     */
    protected abstract LogicalEvaluator createNegatedLogicalEvaluator(InstanceType type);
}
