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
package org.leadpony.justify.internal.schema.io;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParsingException;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonSchemaResolver;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.URIs;
import org.leadpony.justify.internal.base.json.PointerAwareJsonParser;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;
import org.leadpony.justify.internal.schema.Resolvable;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.internal.validator.JsonValidator;

/**
 * A skeletal implementation of {@link JsonSchemaReader}.
 *
 * @author leadpony
 */
abstract class AbstractSchemaReader implements JsonSchemaReader, ProblemBuilderFactory, ProblemHandler {

    static final URI DEFAULT_INITIAL_BASE_URI = URI.create("");

    protected final PointerAwareJsonParser parser;

    private boolean alreadyRead;
    private boolean alreadyClosed;

    // schemas having $id keyword.
    private final Set<JsonSchema> identifiedSchemas = new HashSet<>();

    private final Map<URI, JsonSchema> idSchemaMap = new HashMap<>();
    private final List<Reference> references = new ArrayList<>();

    private final boolean strictWithKeywords;
    private final boolean strictWithFormats;
    private final List<JsonSchemaResolver> resolvers;

    // problems found by the schema validator.
    private final List<Problem> problems = new ArrayList<>();

    private URI initialBaseUri = DEFAULT_INITIAL_BASE_URI;

    protected AbstractSchemaReader(PointerAwareJsonParser parser) {
        this(parser, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    protected AbstractSchemaReader(PointerAwareJsonParser parser, Map<String, Object> config) {
        this.parser = parser;
        this.strictWithKeywords = config.get(STRICT_KEYWORDS) == Boolean.TRUE;
        this.strictWithFormats = config.get(STRICT_FORMATS) == Boolean.TRUE;
        this.resolvers = (List<JsonSchemaResolver>)config.getOrDefault(RESOLVERS, Collections.emptyList());
    }

    protected AbstractSchemaReader(JsonValidator parser, Map<String, Object> config) {
        this((PointerAwareJsonParser)parser, config);
        parser.withHandler(this);
    }

    final boolean isStrictWithKeywords() {
        return strictWithKeywords;
    }

    final boolean isStrictWithFormats() {
        return strictWithFormats;
    }

    /* As a JsonSchemaReader */

    @Override
    public JsonSchema read() {
        if (this.alreadyClosed) {
            throw new IllegalStateException("already closed.");
        } else if (this.alreadyRead) {
            throw new IllegalStateException("already read.");
        }
        JsonSchema schema = readRootSchema();
        if (schema != null) {
            postprocess(schema);
        }
        this.alreadyRead = true;
        dispatchProblems();
        return schema;
    }

    @Override
    public void close() {
        if (!this.alreadyClosed) {
            this.parser.close();
            this.alreadyClosed = true;
        }
    }

    /* As a ProblemHandler */

    @Override
    public void handleProblems(List<Problem> problems) {
        this.problems.addAll(problems);
    }

    /* */

    protected JsonSchema readRootSchema() {
        if (parser.hasNext()) {
            Event event = parser.next();
            if (canReadSchema(event)) {
                try {
                    return readSchema(event);
                } catch (NoSuchElementException e) {
                    throw newParsingException();
                }
            }
        } else {
            addProblem(Message.SCHEMA_PROBLEM_EMPTY);
        }
        return null;
    }

    protected JsonSchema readSchema(Event event) {
        switch (event) {
        case VALUE_TRUE:
            return JsonSchema.TRUE;
        case VALUE_FALSE:
            return JsonSchema.FALSE;
        case START_OBJECT:
            return readObjectSchema();
        default:
            return null;
        }
    }

    protected abstract JsonSchema readObjectSchema();

    protected static boolean canReadSchema(Event event) {
        return event == Event.START_OBJECT ||
               event == Event.VALUE_TRUE ||
               event == Event.VALUE_FALSE;
    }

    protected void skipValue(Event event) {
        switch (event) {
        case START_ARRAY:
            parser.skipArray();
            break;
        case START_OBJECT:
            parser.skipObject();
            break;
        default:
            break;
        }
    }

    protected void addIdentifiedSchema(JsonSchema schema) {
        this.identifiedSchemas.add(schema);
    }

    protected void addSchemaReference(SchemaReference ref, JsonLocation location, String pointer) {
        this.references.add(new Reference(ref, location, pointer));
    }

    protected ProblemBuilder createProblemBuilder(Message message) {
        if (!(parser instanceof JsonValidator)) {
            throw new IllegalStateException();
        }
        EvaluatorContext context = (EvaluatorContext)parser;
        return createProblemBuilder(context).withMessage(message);
    }

    /**
     * Adds a problem built by the problem builder.
     *
     * @param problemBuilder the builder to build the problem.
     */
    protected void addProblem(ProblemBuilder problemBuilder) {
        this.problems.add(problemBuilder.build());
    }

    protected void addProblem(Message message) {
        addProblem(createProblemBuilder(message));
    }

    protected JsonParsingException newParsingException() {
        String message = Message.SCHEMA_PROBLEM_EOI.getLocalized();
        return new JsonParsingException(message, parser.getLocation());
    }

    private void dispatchProblems() {
        if (!problems.isEmpty()) {
            throw new JsonValidatingException(this.problems);
        }
    }

    private void postprocess(JsonSchema schema) {
        makeIdentifiersAbsolute(schema, initialBaseUri);
        resolveAllReferences();
        checkInfiniteRecursiveLoop();
    }

    private void makeIdentifiersAbsolute(JsonSchema root, URI baseUri) {
        if (root instanceof Resolvable) {
            ((Resolvable) root).resolve(baseUri);
        }

        for (JsonSchema schema : this.identifiedSchemas) {
            addIdentifiedSchema(schema.id(), schema);
        }

        if (!this.identifiedSchemas.contains(root)) {
            addIdentifiedSchema(baseUri, root);
        }
    }

    private void addIdentifiedSchema(URI id, JsonSchema schema) {
        this.idSchemaMap.put(URIs.withFragment(id), schema);
    }

    private void resolveAllReferences() {
        for (Reference context : this.references) {
            SchemaReference reference = context.getReference();
            URI targetId = reference.getTargetId();
            JsonSchema schema = dereferenceSchema(targetId);
            if (schema != null) {
                reference.setReferencedSchema(schema);
            } else {
                addProblem(createProblemBuilder(context.getLocation(), context.getPointer())
                        .withMessage(Message.SCHEMA_PROBLEM_REFERENCE)
                        .withParameter("ref", reference.ref())
                        .withParameter("targetId", targetId));
            }
        }
    }

    private JsonSchema dereferenceSchema(URI ref) {
        ref = URIs.withFragment(ref);
        String fragment = ref.getFragment();
        if (fragment.startsWith("/")) {
            JsonSchema schema = resolveSchema(URIs.withEmptyFragment(ref));
            if (schema != null) {
                return schema.getSubschemaAt(fragment);
            }
            return null;
        } else {
            return resolveSchema(ref);
        }
    }

    private JsonSchema resolveSchema(URI id) {
        JsonSchema schema = this.idSchemaMap.get(id);
        if (schema != null) {
            return schema;
        }
        for (JsonSchemaResolver resolver : this.resolvers) {
            schema = resolver.resolveSchema(id);
            if (schema != null) {
                return schema;
            }
        }
        return null;
    }

    private void checkInfiniteRecursiveLoop() {
        InfiniteLoopDetector detector = new InfiniteLoopDetector();
        for (Reference context : this.references) {
            SchemaReference reference = context.getReference();
            if (detector.detectInfiniteLoop(reference)) {
                addProblem(createProblemBuilder(context.getLocation(), context.getPointer())
                        .withMessage(Message.SCHEMA_PROBLEM_REFERENCE_LOOP));
            }
        }
    }

    /**
     * A type representing found schema reference.
     *
     * @author leadpony
     */
    private static class Reference {

        private final SchemaReference reference;
        private final JsonLocation location;
        private final String pointer;

        Reference(SchemaReference reference, JsonLocation location, String pointer) {
            this.reference = reference;
            this.location = location;
            this.pointer = pointer;
        }

        SchemaReference getReference() {
            return reference;
        }

        JsonLocation getLocation() {
            return location;
        }

        String getPointer() {
            return pointer;
        }
    }
}
