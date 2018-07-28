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

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.Evaluator;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.Problem;
import org.leadpony.justify.internal.base.ProblemBuilder;
import org.leadpony.justify.internal.keyword.Keyword;

/**
 * Schema reference containing  "$ref" keyword.
 * 
 * @author leadpony
 */
public class SchemaReference extends AbstractJsonSchema {
    
    private URI ref;
    @SuppressWarnings("unused")
    private final URI originalRef;
    private JsonSchema referencedSchema;
    
    public SchemaReference(URI ref, Map<String, Keyword> keywordMap, 
            JsonBuilderFactory builderFactory) {
        super(keywordMap, builderFactory);
        this.ref = this.originalRef = ref;
        this.referencedSchema = new NonExistentSchema();
    }
    
    public URI getRef() {
        return ref;
    }
    
    public void setRef(URI ref) {
        this.ref = ref;
    }
    
    public void setReferencedSchema(JsonSchema schema) {
        Objects.requireNonNull(schema, "schema must not be null.");
        this.referencedSchema = schema;
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        return referencedSchema.createEvaluator(type);
    }

    @Override
    public JsonSchema negate() {
        return referencedSchema.negate();
    }

    @Override
    protected void addToJson(JsonObjectBuilder builder) {
        builder.add("$ref", this.ref.toString());
        super.addToJson(builder);
    }
    
    private class NonExistentSchema implements JsonSchema, Evaluator {

        @Override
        public Evaluator createEvaluator(InstanceType type) {
            return this;
        }

        @Override
        public JsonSchema negate() {
            return this;
        }

        @Override
        public JsonValue toJson() {
            return JsonValue.FALSE;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, Reporter reporter) {
            Problem p = ProblemBuilder.newBuilder(parser)
                    .withKeyword("$ref")
                    .withMessage("schema.problem.dereference")
                    .withParameter("ref", ref)
                    .build();
            reporter.reportProblem(p);
            return Result.FALSE;
        }
    }
}
