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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.URIs;
import org.leadpony.justify.internal.base.json.PointerAwareJsonParser;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.combiner.Unknown;
import org.leadpony.justify.internal.keyword.core.Id;
import org.leadpony.justify.internal.keyword.core.Ref;
import org.leadpony.justify.internal.keyword.core.Schema;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.schema.binder.KeywordBinder;
import org.leadpony.justify.internal.schema.BasicSchema;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.internal.schema.binder.BinderContext;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A generic schema reader for all specification versions.
 *
 * @author leadpony
 */
public class GenericSchemaReader extends AbstractBasicSchemaReader {

    private final JsonBuilderFactory jsonBuilderFactory;
    private final SchemaSpec spec;
    private final Map<String, KeywordBinder> binders;

    public GenericSchemaReader(
            PointerAwareJsonParser parser,
            JsonBuilderFactory jsonBuilderFactory,
            SchemaSpec spec,
            Map<String, Object> config) {
        super(parser, config);
        this.jsonBuilderFactory = jsonBuilderFactory;
        this.spec = spec;
        this.binders = spec.getKeywordBinders();
    }

    /* As a AbstractSchemaReader */

    @Override
    protected JsonSchema readObjectSchema() {
        SchemaBuilder builder = new SchemaBuilder();

        Event event = null;
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.END_OBJECT) {
                break;
            } else if (event == Event.KEY_NAME) {
                String name = parser.getString();
                parseKeyword(name, builder);
            }
        }

        if (event != Event.END_OBJECT) {
            throw newParsingException();
        }

        JsonSchema schema = builder.build(jsonBuilderFactory);
        if (schema.hasId()) {
            addIdentifiedSchema(schema);
        }

        return schema;
    }

    protected FormatAttribute getFormatAttribute(String name) {
        FormatAttribute attribute = spec.getFormatAttribute(name);
        if (attribute == null && isStrictWithFormats()) {
            addProblem(createProblemBuilder(Message.SCHEMA_PROBLEM_FORMAT_UNKNOWN)
                    .withParameter("attribute", name));
        }
        return attribute;
    }

    protected ContentEncodingScheme getEncodingScheme(String name) {
        return spec.getEncodingScheme(name);
    }

    protected ContentMimeType getMimeType(String value) {
        return spec.getMimeType(value);
    }

    private void parseKeyword(String name, BinderContext context) {
        KeywordBinder binder = binders.get(name);
        if (binder != null) {
            binder.fromJson(parser, context);
        } else {
            parseUnknownKeyword(name, context);
        }
    }

    private void parseUnknownKeyword(String keyword, BinderContext context) {
        if (isStrictWithKeywords()) {
            ProblemBuilder builder = createProblemBuilder(Message.SCHEMA_PROBLEM_KEYWORD_UNKNOWN)
                    .withParameter("keyword", keyword);
            addProblem(builder);
        }
        Event event = parser.next();
        if (canReadSchema(event)) {
            context.addKeyword(new Unknown(keyword, readSchema(event)));
        } else {
            skipValue(event);
        }
    }

    private void checkMetaschemaId(URI actual) {
        if (!actual.isAbsolute()) {
            return;
        }
        URI expected = spec.getVersion().id();
        if (URIs.COMPARATOR.compare(expected, actual) != 0) {
            ProblemBuilder builder = createProblemBuilder(Message.SCHEMA_PROBLEM_VERSION_UNEXPECTED);
            builder.withParameter("expected", expected)
                   .withParameter("actual", actual);
            addProblem(builder);
            dispatchProblems();
        }
    }

    @SuppressWarnings("serial")
    private class SchemaBuilder extends LinkedHashMap<String, Keyword> implements BinderContext {

        private URI id;
        // The location of "$ref".
        private JsonLocation refLocation;
        // The pointer of "$ref".
        private String refPointer;

        JsonSchema build(JsonBuilderFactory builderFactory) {
            if (isEmpty()) {
                return JsonSchema.EMPTY;
            } else if (containsKey("$ref")) {
                SchemaReference reference = new SchemaReference(this.id, this, builderFactory);
                addSchemaReference(reference, refLocation, refPointer);
                return reference;
            } else {
                return BasicSchema.newSchema(this.id, this, builderFactory);
            }
        }

        /* As a BinderContext */

        @Override
        public JsonSchema readSchema(Event event) {
            return GenericSchemaReader.this.readSchema(event);
        }

        @Override
        public FormatAttribute getFormatAttribute(String name) {
            return GenericSchemaReader.this.getFormatAttribute(name);
        }

        @Override
        public ContentEncodingScheme getEncodingScheme(String name) {
            return GenericSchemaReader.this.getEncodingScheme(name);
        }

        @Override
        public ContentMimeType getMimeType(String value) {
            return GenericSchemaReader.this.getMimeType(value);
        }

        @Override
        public void addKeyword(Keyword keyword) {
            put(keyword.name(), keyword);
        }

        @Override
        public void addKeyword(Id keyword) {
            put(keyword.name(), keyword);
            this.id = keyword.value();
        }

        @Override
        public void addKeyword(Ref keyword) {
            put(keyword.name(), keyword);
            this.refLocation = parser.getLocation();
            this.refPointer = parser.getPointer();
        }

        @Override
        public void addKeyword(Schema keyword) {
            put(keyword.name(), keyword);
            checkMetaschemaId(keyword.value());
        }

        @Override
        public void addProblem(Message message) {
            GenericSchemaReader.this.addProblem(message);
        }
    }
}
