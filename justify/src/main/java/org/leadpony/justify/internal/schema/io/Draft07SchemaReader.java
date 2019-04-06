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

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.PatternSyntaxException;

import javax.json.JsonValue;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaBuilder;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.json.PointerAwareJsonParser;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.internal.validator.JsonValidator;

/**
 * Basic implementation of {@link JsonSchemaReader}.
 *
 * @author leadpony
 */
public class Draft07SchemaReader extends AbstractSchemaReader {

    private final DefaultSchemaBuilderFactory factory;

    @SuppressWarnings("serial")
    private static final Map<String, Object> defaultConfig = new HashMap<String, Object>() {{
        put(RESOLVERS, Collections.emptyList());
    }};

    /**
     * Constructs this schema reader.
     *
     * @param parser  the parser of JSON document.
     * @param factory the factory for producing schema builders.
     */
    public Draft07SchemaReader(PointerAwareJsonParser parser,
            DefaultSchemaBuilderFactory factory) {
        this(parser, factory, defaultConfig);
    }

    /**
     * Constructs this schema reader.
     *
     * @param parser  the parser of JSON document.
     * @param factory the factory for producing schema builders.
     * @param config  the configuration properties.
     */
    public Draft07SchemaReader(PointerAwareJsonParser parser,
            DefaultSchemaBuilderFactory factory,
            Map<String, Object> config) {
        super(parser, config);
        this.factory = factory;
    }

    /**
     * Constructs this schema reader.
     *
     * @param parser  the parser of JSON document, which has the validation
     *                capability.
     * @param factory the factory for producing schema builders.
     * @param config  the configuration properties.
     */
    public Draft07SchemaReader(JsonValidator parser,
            DefaultSchemaBuilderFactory factory,
            Map<String, Object> config) {
        this((PointerAwareJsonParser) parser, factory, config);
    }

    /* As a AbstractSchemaReader */

    @Override
    protected JsonSchema readObjectSchema() {
        Draft07SchemaBuilder builder = this.factory.createBuilder();
        JsonLocation refLocation = null;
        String refPointer = null;
        Event event = null;
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.END_OBJECT) {
                break;
            } else if (event == Event.KEY_NAME) {
                String keyName = parser.getString();
                populateSchema(keyName, builder);
                if (keyName.equals("$ref")) {
                    refLocation = parser.getLocation();
                    refPointer = parser.getPointer();
                }
            }
        }
        if (event != Event.END_OBJECT) {
            throw newParsingException();
        }
        JsonSchema schema = builder.build();
        if (schema.hasId()) {
            addIdentifiedSchema(schema);
        }
        if (schema instanceof SchemaReference) {
            SchemaReference reference = (SchemaReference) schema;
            addSchemaReference(reference, refLocation, refPointer);
        }
        return schema;
    }

    private void populateSchema(String keyName, Draft07SchemaBuilder builder) {
        switch (keyName) {
        case "$comment":
            addComment(builder);
            break;
        case "$id":
            addId(builder);
            break;
        case "$ref":
            addRef(builder);
            break;
        case "$schema":
            addSchema(builder);
            break;
        case "additionalItems":
            addAdditionalItems(builder);
            break;
        case "additionalProperties":
            addAdditionalProperties(builder);
            break;
        case "allOf":
            addAllOf(builder);
            break;
        case "anyOf":
            addAnyOf(builder);
            break;
        case "const":
            addConst(builder);
            break;
        case "contains":
            addContains(builder);
            break;
        case "contentEncoding":
            addContentEncoding(builder);
            break;
        case "contentMediaType":
            addContentMediaType(builder);
            break;
        case "default":
            addDefault(builder);
            break;
        case "definitions":
            addDefinitions(builder);
            break;
        case "dependencies":
            addDependencies(builder);
            break;
        case "description":
            addDescription(builder);
            break;
        case "else":
            addElse(builder);
            break;
        case "enum":
            addEnum(builder);
            break;
        case "exclusiveMaximum":
            addExclusiveMaximum(builder);
            break;
        case "exclusiveMinimum":
            addExclusiveMinimum(builder);
            break;
        case "format":
            addFormat(builder);
            break;
        case "if":
            addIf(builder);
            break;
        case "items":
            addItems(builder);
            break;
        case "maxContains":
            addMaxContains(builder);
            break;
        case "maximum":
            addMaximum(builder);
            break;
        case "maxItems":
            addMaxItems(builder);
            break;
        case "maxLength":
            addMaxLength(builder);
            break;
        case "maxProperties":
            addMaxProperties(builder);
            break;
        case "minContains":
            addMinContains(builder);
            break;
        case "minimum":
            addMinimum(builder);
            break;
        case "minItems":
            addMinItems(builder);
            break;
        case "minLength":
            addMinLength(builder);
            break;
        case "minProperties":
            addMinProperties(builder);
            break;
        case "multipleOf":
            addMultipleOf(builder);
            break;
        case "not":
            addNot(builder);
            break;
        case "oneOf":
            addOneOf(builder);
            break;
        case "pattern":
            addPattern(builder);
            break;
        case "patternProperties":
            addPatternProperties(builder);
            break;
        case "properties":
            addProperties(builder);
            break;
        case "propertyNames":
            addPropertyNames(builder);
            break;
        case "required":
            addRequired(builder);
            break;
        case "then":
            addThen(builder);
            break;
        case "title":
            addTitle(builder);
            break;
        case "type":
            addType(builder);
            break;
        case "uniqueItems":
            addUniqueItems(builder);
            break;
        default:
            addUnknown(keyName, builder);
            break;
        }
    }

    /* Core keywords */

    private void addSchema(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            try {
                builder.withSchema(new URI(parser.getString()));
            } catch (URISyntaxException e) {
                // Do nothing.
            }
        } else {
            skipValue(event);
        }
    }

    private void addId(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            try {
                builder.withId(new URI(parser.getString()));
            } catch (URISyntaxException e) {
                // Do nothing.
            }
        } else {
            skipValue(event);
        }
    }

    private void addComment(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withComment(parser.getString());
        } else {
            skipValue(event);
        }
    }

    private void addRef(Draft07SchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            try {
                builder.withRef(new URI(parser.getString()));
            } catch (URISyntaxException e) {
                // Do nothing.
            }
        } else {
            skipValue(event);
        }
    }

    /* Validation keywords */

    private void addType(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            consumeType(parser.getString(), builder::withType);
        } else if (event == Event.START_ARRAY) {
            Set<InstanceType> types = EnumSet.noneOf(InstanceType.class);
            while ((event = parser.next()) != Event.END_ARRAY) {
                if (event == Event.VALUE_STRING) {
                    consumeType(parser.getString(), types::add);
                } else {
                    skipValue(event);
                }
            }
            if (!types.isEmpty()) {
                builder.withType(types);
            }
        } else {
            skipValue(event);
        }
    }

    private void addEnum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            Set<JsonValue> values = new LinkedHashSet<>();
            while ((event = parser.next()) != Event.END_ARRAY) {
                values.add(parser.getValue());
            }
            if (!values.isEmpty()) {
                builder.withEnum(values);
            }
        } else {
            skipValue(event);
        }
    }

    private void addConst(JsonSchemaBuilder builder) {
        parser.next();
        builder.withConst(parser.getValue());
    }

    private void addMaximum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMaximum(parser.getBigDecimal());
        } else {
            skipValue(event);
        }
    }

    private void addExclusiveMaximum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withExclusiveMaximum(parser.getBigDecimal());
        } else {
            skipValue(event);
        }
    }

    private void addMinimum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withMinimum(parser.getBigDecimal());
        } else {
            skipValue(event);
        }
    }

    private void addExclusiveMinimum(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            builder.withExclusiveMinimum(parser.getBigDecimal());
        } else {
            skipValue(event);
        }
    }

    private void addMultipleOf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            BigDecimal value = parser.getBigDecimal();
            if (value.signum() > 0) {
                builder.withMultipleOf(value);
            }
        } else {
            skipValue(event);
        }
    }

    private void addMaxLength(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            int bound = parser.getInt();
            if (bound >= 0) {
                builder.withMaxLength(bound);
            }
        } else {
            skipValue(event);
        }
    }

    private void addMinLength(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            int bound = parser.getInt();
            if (bound >= 0) {
                builder.withMinLength(bound);
            }
        } else {
            skipValue(event);
        }
    }

    private void addPattern(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            try {
                builder.withPattern(parser.getString());
            } catch (PatternSyntaxException e) {
                // Do nothing.
            }
        } else {
            skipValue(event);
        }
    }

    private void addItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            List<JsonSchema> subschemas = readSchemaList();
            if (!subschemas.isEmpty()) {
                builder.withItemsArray(subschemas);
            }
        } else if (canReadSchema(event)) {
            builder.withItems(readSchema(event));
        } else {
            skipValue(event);
        }
    }

    private void addAdditionalItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canReadSchema(event)) {
            builder.withAdditionalItems(readSchema(event));
        } else {
            skipValue(event);
        }
    }

    private void addMaxItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            int value = parser.getInt();
            if (value >= 0) {
                builder.withMaxItems(value);
            }
        } else {
            skipValue(event);
        }
    }

    private void addMinItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            int value = parser.getInt();
            if (value >= 0) {
                builder.withMinItems(value);
            }
        } else {
            skipValue(event);
        }
    }

    private void addUniqueItems(JsonSchemaBuilder builder) {
        Event event = parser.next();
        switch (event) {
        case VALUE_TRUE:
            builder.withUniqueItems(true);
            break;
        case VALUE_FALSE:
            builder.withUniqueItems(false);
            break;
        default:
            skipValue(event);
            break;
        }
    }

    private void addContains(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canReadSchema(event)) {
            builder.withContains(readSchema(event));
        } else {
            skipValue(event);
        }
    }

    private void addMaxContains(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            int value = parser.getInt();
            if (value >= 0) {
                builder.withMaxContains(value);
            }
        } else {
            skipValue(event);
        }
    }

    private void addMinContains(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            int value = parser.getInt();
            if (value >= 0) {
                builder.withMinContains(value);
            }
        } else {
            skipValue(event);
        }
    }

    private void addMaxProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            int value = parser.getInt();
            if (value >= 0) {
                builder.withMaxProperties(value);
            }
        } else {
            skipValue(event);
        }
    }

    private void addMinProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_NUMBER) {
            int value = parser.getInt();
            if (value >= 0) {
                builder.withMinProperties(value);
            }
        } else {
            skipValue(event);
        }
    }

    private void addRequired(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            Set<String> names = new LinkedHashSet<>();
            while ((event = parser.next()) != Event.END_ARRAY) {
                if (event == Event.VALUE_STRING) {
                    names.add(parser.getString());
                } else {
                    skipValue(event);
                }
            }
            builder.withRequired(names);
        } else {
            skipValue(event);
        }
    }

    private void addProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            skipValue(event);
            return;
        }
        Map<String, JsonSchema> subschemas = new LinkedHashMap<>();
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                String name = parser.getString();
                event = parser.next();
                if (canReadSchema(event)) {
                    subschemas.put(name, readSchema(event));
                } else {
                    skipValue(event);
                }
            } else if (event == Event.END_OBJECT) {
                builder.withProperties(subschemas);
                break;
            }
        }
    }

    private void addPatternProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            skipValue(event);
            return;
        }
        Map<String, JsonSchema> subschemas = new HashMap<>();
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                String pattern = parser.getString();
                event = parser.next();
                if (canReadSchema(event)) {
                    subschemas.put(pattern, readSchema(event));
                } else {
                    skipValue(event);
                }
            } else if (event == Event.END_OBJECT) {
                try {
                    builder.withPatternProperties(subschemas);
                } catch (PatternSyntaxException e) {
                    // Do nothing.
                }
                break;
            }
        }
    }

    private void addAdditionalProperties(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canReadSchema(event)) {
            builder.withAdditionalProperties(readSchema(event));
        } else {
            skipValue(event);
        }
    }

    private void addDependencies(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            skipValue(event);
            return;
        }
        Map<String, Object> values = new HashMap<>();
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                String property = parser.getString();
                if (parser.hasNext()) {
                    event = parser.next();
                    if (canReadSchema(event)) {
                        values.put(property, readSchema(event));
                    } else if (event == Event.START_ARRAY) {
                        Set<String> required = new LinkedHashSet<>();
                        while (parser.hasNext()) {
                            event = parser.next();
                            if (event == Event.VALUE_STRING) {
                                required.add(parser.getString());
                            } else if (event == Event.END_ARRAY) {
                                values.put(property, required);
                                break;
                            }
                        }
                    }
                }
            } else if (event == Event.END_OBJECT) {
                builder.withDependencies(values);
                break;
            }
        }
    }

    private void addPropertyNames(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canReadSchema(event)) {
            builder.withPropertyNames(readSchema(event));
        } else {
            skipValue(event);
        }
    }

    private void addIf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canReadSchema(event)) {
            builder.withIf(readSchema(event));
        } else {
            skipValue(event);
        }
    }

    private void addThen(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canReadSchema(event)) {
            builder.withThen(readSchema(event));
        } else {
            skipValue(event);
        }
    }

    private void addElse(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canReadSchema(event)) {
            builder.withElse(readSchema(event));
        } else {
            skipValue(event);
        }
    }

    private void addAllOf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            List<JsonSchema> subschemas = readSchemaList();
            if (!subschemas.isEmpty()) {
                builder.withAllOf(subschemas);
            }
        } else {
            skipValue(event);
        }
    }

    private void addAnyOf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            List<JsonSchema> subschemas = readSchemaList();
            if (!subschemas.isEmpty()) {
                builder.withAnyOf(subschemas);
            }
        } else {
            skipValue(event);
        }
    }

    private void addOneOf(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.START_ARRAY) {
            List<JsonSchema> subschemas = readSchemaList();
            if (!subschemas.isEmpty()) {
                builder.withOneOf(subschemas);
            }
        } else {
            skipValue(event);
        }
    }

    private void addNot(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (canReadSchema(event)) {
            builder.withNot(readSchema(event));
        } else {
            skipValue(event);
        }
    }

    private void addFormat(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            String attribute = parser.getString();
            if (isStrictWithFormats()) {
                try {
                    builder.withFormat(attribute);
                } catch (IllegalArgumentException e) {
                    addProblem(createProblemBuilder(Message.SCHEMA_PROBLEM_FORMAT_UNKNOWN).withParameter("attribute",
                            attribute));
                }
            } else {
                builder.withLaxFormat(attribute);
            }
        } else {
            skipValue(event);
        }
    }

    private void addContentEncoding(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withContentEncoding(parser.getString());
        } else {
            skipValue(event);
        }
    }

    private void addContentMediaType(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            String value = parser.getString();
            try {
                builder.withContentMediaType(value);
            } catch (IllegalArgumentException e) {
                handleInvalidMediaType();
            }
        } else {
            skipValue(event);
        }
    }

    private void addDefinitions(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            skipValue(event);
            return;
        }
        Map<String, JsonSchema> schemas = new HashMap<>();
        while (parser.hasNext()) {
            event = parser.next();
            if (event == Event.KEY_NAME) {
                String name = parser.getString();
                event = parser.next();
                if (canReadSchema(event)) {
                    schemas.put(name, readSchema(event));
                } else {
                    skipValue(event);
                }
            } else if (event == Event.END_OBJECT) {
                builder.withDefinitions(schemas);
                break;
            }
        }
    }

    private void addTitle(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withTitle(parser.getString());
        } else {
            skipValue(event);
        }
    }

    private void addDescription(JsonSchemaBuilder builder) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            builder.withDescription(parser.getString());
        } else {
            skipValue(event);
        }
    }

    private void addDefault(JsonSchemaBuilder builder) {
        parser.next();
        builder.withDefault(parser.getValue());
    }

    private void addUnknown(String keyword, Draft07SchemaBuilder builder) {
        if (isStrictWithKeywords()) {
            addProblem(createProblemBuilder(Message.SCHEMA_PROBLEM_KEYWORD_UNKNOWN).withParameter("keyword", keyword));
        }
        if (parser.hasNext()) {
            Event event = parser.next();
            if (canReadSchema(event)) {
                builder.withUnknown(keyword, readSchema(event));
            } else {
                skipValue(event);
            }
        }
    }

    private static void consumeType(String name, Consumer<InstanceType> consumer) {
        try {
            InstanceType type = InstanceType.valueOf(name.toUpperCase());
            consumer.accept(type);
        } catch (IllegalArgumentException e) {
        }
    }

    private List<JsonSchema> readSchemaList() {
        List<JsonSchema> subschemas = new ArrayList<>();
        Event event = null;
        while ((event = parser.next()) != Event.END_ARRAY) {
            if (canReadSchema(event)) {
                subschemas.add(readSchema(event));
            } else {
                skipValue(event);
            }
        }
        return subschemas;
    }

    private void handleInvalidMediaType() {
        addProblem(Message.SCHEMA_PROBLEM_CONTENTMEDIATYPE_INVALID);
    }
}
