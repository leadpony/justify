/*
 * Copyright 2018-2019 the Justify authors.
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

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.net.URI;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.internal.base.ProblemBuilderFactory;

/**
 * Schema reference containing "$ref" keyword.
 *
 * @author leadpony
 */
public class SchemaReference extends AbstractJsonSchema {

    private final URI ref;
    private URI targetId;
    private JsonSchema referencedSchema;

    /**
     * Constructs this schema reference.
     *
     * @param result the result of the schema builder.
     * @param ref the value of the "$ref" keyword.
     */
    public SchemaReference(JsonSchemaBuilderResult result, URI ref) {
        super(result);
        this.targetId = this.ref = ref;
        this.referencedSchema = new NonexistentSchema();
    }

    /**
     * Returns the original value of the keyword "$ref".
     * @return the value of the keyword "$ref".
     */
    public URI ref() {
        return ref;
    }

    /**
     * Returns the URI of the referenced schema.
     *
     * @return the URI of the referenced schema.
     */
    public URI getTargetId() {
        return targetId;
    }

    /**
     * Assigns the URI of the referenced schema.
     *
     * @param targetId the URI of the referenced schema, cannot be {@code null}.
     */
    public void setTargetId(URI targetId) {
        requireNonNull(targetId, "targetId");
        this.targetId = targetId;
    }

    /**
     * Assigns the referenced schema.
     *
     * @param schema the referenced schema, cannot be {@code null}.
     */
    public void setReferencedSchema(JsonSchema schema) {
        requireNonNull(schema, "schema");
        this.referencedSchema = schema;
    }

    @Override
    public Evaluator createEvaluator(InstanceType type) {
        return referencedSchema.createEvaluator(type);
    }

    @Override
    public Evaluator createNegatedEvaluator(InstanceType type) {
        return referencedSchema.createNegatedEvaluator(type);
    }

    @Override
    protected void addToJson(JsonObjectBuilder builder) {
        builder.add("$ref", this.ref.toString());
        super.addToJson(builder);
    }

    /**
     * Nonexistent JSON Schema.
     *
     * @author leadpony
     */
    private class NonexistentSchema implements JsonSchema, Evaluator {

        @Override
        public Evaluator createEvaluator(InstanceType type) {
            return this;
        }

        @Override
        public Evaluator createNegatedEvaluator(InstanceType type) {
            return this;
        }

        @Override
        public JsonValue toJson() {
            return JsonValue.FALSE;
        }

        @Override
        public Result evaluate(Event event, JsonParser parser, int depth, ProblemDispatcher dispatcher) {
            Problem p = ProblemBuilderFactory.DEFAULT.createProblemBuilder(parser)
                    .withKeyword("$ref")
                    .withMessage("schema.problem.reference")
                    .withParameter("ref", ref())
                    .withParameter("targetId", getTargetId())
                    .build();
            dispatcher.dispatchProblem(p);
            return Result.FALSE;
        }
    }
}
