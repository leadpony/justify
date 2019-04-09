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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.JsonBuilderFactory;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.json.PointerAwareJsonParser;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.combiner.Unknown;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.schema.binder.KeywordBinder;
import org.leadpony.justify.internal.schema.BasicSchema;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.internal.schema.binder.BinderContext;
import org.leadpony.justify.internal.validator.JsonValidator;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A generic schema reader for all specification versions.
 *
 * @author leadpony
 */
public class GenericSchemaReader extends AbstractSchemaReader implements BinderContext {

    private final JsonBuilderFactory jsonBuilderFactory;
    private final SchemaSpec spec;
    private final Map<String, KeywordBinder> binders;

    // Current schema builder.
    private SimpleSchemaBuilder builder;

    public GenericSchemaReader(
            JsonValidator parser,
            JsonBuilderFactory jsonBuilderFactory,
            SchemaSpec spec,
            Map<String, Object> config) {
        super(parser, config);
        this.jsonBuilderFactory = jsonBuilderFactory;
        this.spec = spec;
        this.binders = spec.getKeywordBinders();
    }

    public GenericSchemaReader(
            PointerAwareJsonParser parser,
            JsonBuilderFactory jsonBuilderFactory,
            SchemaSpec spec) {
        super(parser);
        this.jsonBuilderFactory = jsonBuilderFactory;
        this.spec = spec;
        this.binders = spec.getKeywordBinders();
    }

    /* As a AbstractSchemaReader */

    @Override
    protected JsonSchema readObjectSchema() {
        SimpleSchemaBuilder parentBuilder = this.builder;
        this.builder = new SimpleSchemaBuilder();
        try {
            return buildObjectSchema(builder);
        } finally {
            this.builder = parentBuilder;
        }
    }

    /* As a BinderContext */

    @Override
    public JsonSchema readSchema(Event event) {
        return super.readSchema(event);
    }

    @Override
    public FormatAttribute getFormatAttribute(String name) {
        FormatAttribute attribute = spec.getFormatAttribute(name);
        if (attribute == null && isStrictWithFormats()) {
            addProblem(createProblemBuilder(Message.SCHEMA_PROBLEM_FORMAT_UNKNOWN)
                    .withParameter("attribute", name));
        }
        return attribute;
    }


    @Override
    public ContentEncodingScheme getEncodingScheme(String name) {
        return spec.getEncodingScheme(name);
    }

    @Override
    public ContentMimeType getMimeType(String value) {
        return spec.getMimeType(value);
    }

    @Override
    public void addKeyword(Keyword keyword) {
        this.builder.put(keyword.name(), keyword);
    }

    @Override
    public void addRefKeyword(Keyword keyword) {
        addKeyword(keyword);
        this.builder.setRefLocation(parser.getLocation(), parser.getPointer());
    }

    @Override
    public void addProblem(Message message) {
        super.addProblem(message);
    }

    private JsonSchema buildObjectSchema(SimpleSchemaBuilder builder) {
        Event event = null;
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.END_OBJECT) {
                break;
            } else if (event == Event.KEY_NAME) {
                String name = parser.getString();
                parseKeyword(name);
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

    private void parseKeyword(String name) {
        KeywordBinder binder = binders.get(name);
        if (binder != null) {
            binder.fromJson(parser, this);
        } else {
            parseUnknownKeyword(name);
        }
    }

    private void parseUnknownKeyword(String keyword) {
        if (isStrictWithKeywords()) {
            ProblemBuilder builder = createProblemBuilder(Message.SCHEMA_PROBLEM_KEYWORD_UNKNOWN)
                    .withParameter("keyword", keyword);
            addProblem(builder);
        }
        Event event = parser.next();
        if (canReadSchema(event)) {
            addKeyword(new Unknown(keyword, readSchema(event)));
        } else {
            skipValue(event);
        }
    }

    @SuppressWarnings("serial")
    private class SimpleSchemaBuilder extends LinkedHashMap<String, Keyword> {

        // The location of "$ref".
        private JsonLocation refLocation;
        // The pointer of "$ref".
        private String refPointer;

        JsonSchema build(JsonBuilderFactory builderFactory) {
            if (isEmpty()) {
                return JsonSchema.EMPTY;
            } else if (containsKey("$ref")) {
                SchemaReference reference = new SchemaReference(this, builderFactory);
                addSchemaReference(reference, refLocation, refPointer);
                return reference;
            } else {
                return BasicSchema.newSchema(this, builderFactory);
            }
        }

        void setRefLocation(JsonLocation location, String pointer) {
            this.refLocation = location;
            this.refPointer = pointer;
        }
    }
}
