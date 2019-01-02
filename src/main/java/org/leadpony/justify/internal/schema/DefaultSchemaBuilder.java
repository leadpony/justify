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

import static org.leadpony.justify.internal.base.Arguments.requireNonEmpty;
import static org.leadpony.justify.internal.base.Arguments.requireNonNegative;
import static org.leadpony.justify.internal.base.Arguments.requireNonNull;
import static org.leadpony.justify.internal.base.Arguments.requirePositive;
import static org.leadpony.justify.internal.base.Arguments.requireUnique;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.json.JsonBuilderFactory;
import javax.json.JsonValue;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaBuilder;
import org.leadpony.justify.internal.base.MediaType;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.annotation.Default;
import org.leadpony.justify.internal.keyword.annotation.Description;
import org.leadpony.justify.internal.keyword.annotation.Title;
import org.leadpony.justify.internal.keyword.assertion.Assertions;
import org.leadpony.justify.internal.keyword.assertion.content.ContentAttributeRegistry;
import org.leadpony.justify.internal.keyword.assertion.content.ContentEncoding;
import org.leadpony.justify.internal.keyword.assertion.content.ContentMediaType;
import org.leadpony.justify.internal.keyword.assertion.content.UnknownContentEncoding;
import org.leadpony.justify.internal.keyword.assertion.content.UnknownContentMediaType;
import org.leadpony.justify.internal.keyword.assertion.format.Format;
import org.leadpony.justify.internal.keyword.assertion.format.FormatAttributeRegistry;
import org.leadpony.justify.internal.keyword.combiner.Combiners;
import org.leadpony.justify.internal.keyword.combiner.Definitions;
import org.leadpony.justify.internal.keyword.combiner.Dependencies;
import org.leadpony.justify.internal.keyword.combiner.PatternProperties;
import org.leadpony.justify.internal.keyword.combiner.Properties;

/**
 * The default implementation of {@link EnhancedSchemaBuilder}.
 *
 * @author leadpony
 */
class DefaultSchemaBuilder implements EnhancedSchemaBuilder {

    private final JsonBuilderFactory builderFactory;
    private final FormatAttributeRegistry formatRegistry;
    private final ContentAttributeRegistry contentRegistry;

    private boolean empty;
    private URI id;
    private URI schema;
    private final Map<String, Keyword> keywords = new LinkedHashMap<>();
    private URI ref;

    /**
     * Constructs this builder.
     *
     * @param builderFactory the factory for producing builders of JSON values.
     * @param formatRegistry  the registry managing all format attributes.
     * @param contentRegistry the registry managing all content attributes.
     */
    public DefaultSchemaBuilder(JsonBuilderFactory builderFactory, FormatAttributeRegistry formatRegistry, ContentAttributeRegistry contentRegistry) {
        this.builderFactory = builderFactory;
        this.formatRegistry = formatRegistry;
        this.contentRegistry = contentRegistry;
        this.empty = true;
    }

    URI getId() {
        return id;
    }

    URI getSchema() {
        return schema;
    }

    Map<String, Keyword> getKeywordMap() {
        return keywords;
    }

    JsonBuilderFactory getBuilderFactory() {
        return builderFactory;
    }

    @Override
    public JsonSchema build() {
        if (empty) {
            return JsonSchema.EMPTY;
        } else if (ref != null) {
            return new SchemaReference(ref, this.keywords, getBuilderFactory());
        } else {
            return new BasicSchema(this);
        }
    }

    @Override
    public JsonSchemaBuilder withId(URI id) {
        requireNonNull(id, "id");
        this.id = id;
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withSchema(URI schema) {
        requireNonNull(schema, "schema");
        this.schema = schema;
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withType(InstanceType... types) {
        requireNonNull(types, "types");
        requireNonEmpty(types, "types");
        Set<InstanceType> set = requireUnique(types, "types");
        return withType(set);
    }

    @Override
    public JsonSchemaBuilder withType(Set<InstanceType> types) {
        requireNonNull(types, "types");
        requireNonEmpty(types, "types");
        addKeyword(Assertions.type(types));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withEnum(JsonValue... values) {
        requireNonNull(values, "values");
        requireNonEmpty(values, "values");
        Set<JsonValue> set = requireUnique(values, "values");
        return withEnum(set);
    }

    @Override
    public JsonSchemaBuilder withEnum(Set<JsonValue> values) {
        requireNonNull(values, "values");
        requireNonEmpty(values, "values");
        addKeyword(Assertions.enum_(values));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withConst(JsonValue value) {
        requireNonNull(value, "value");
        addKeyword(Assertions.const_(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMultipleOf(long value) {
        requirePositive(value, "value");
        return withMultipleOf(BigDecimal.valueOf(value));
    }

    @Override
    public JsonSchemaBuilder withMultipleOf(double value) {
        requirePositive(value, "value");
        return withMultipleOf(BigDecimal.valueOf(value));
    }

    @Override
    public JsonSchemaBuilder withMultipleOf(BigDecimal value) {
        requireNonNull(value, "value");
        requirePositive(value, "value");
        addKeyword(Assertions.multipleOf(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMaximum(BigDecimal value) {
        requireNonNull(value, "value");
        addKeyword(Assertions.maximum(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withExclusiveMaximum(BigDecimal value) {
        requireNonNull(value, "value");
        addKeyword(Assertions.exclusiveMaximum(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMinimum(BigDecimal value) {
        requireNonNull(value, "value");
        addKeyword(Assertions.minimum(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withExclusiveMinimum(BigDecimal value) {
        requireNonNull(value, "value");
        addKeyword(Assertions.exclusiveMinimum(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMaxLength(int value) {
        requireNonNegative(value, "value");
        addKeyword(Assertions.maxLength(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMinLength(int value) {
        requireNonNegative(value, "value");
        addKeyword(Assertions.minLength(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withPattern(String pattern) {
        requireNonNull(pattern, "pattern");
        Pattern compiled = Pattern.compile(pattern);
        addKeyword(Assertions.pattern(compiled));
        return nonemptyBuilder();
    }

    /* Validation Keywords for Arrays */

    @Override
    public JsonSchemaBuilder withItems(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.items(subschema));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withItemsArray(JsonSchema... subschemas) {
        requireNonNull(subschemas, "subschemas");
        return withItemsArray(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withItemsArray(List<JsonSchema> subschemas) {
        requireNonNull(subschemas, "subschemas");
        requireNonEmpty(subschemas, "subschemas");
        addKeyword(Combiners.items(subschemas));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withAdditionalItems(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.additionalItems(subschema));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMaxItems(int value) {
        requireNonNegative(value, "value");
        addKeyword(Assertions.maxItems(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMinItems(int value) {
        requireNonNegative(value, "value");
        addKeyword(Assertions.minItems(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withUniqueItems(boolean unique) {
        addKeyword(Assertions.uniqueItems(unique));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withContains(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.contains(subschema));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMaxContains(int value) {
        requireNonNegative(value, "value");
        addKeyword(Assertions.maxContains(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMinContains(int value) {
        requireNonNegative(value, "value");
        addKeyword(Assertions.minContains(value));
        return nonemptyBuilder();
    }

    /* Validation Keywords for Objects */

    @Override
    public JsonSchemaBuilder withMaxProperties(int value) {
        requireNonNegative(value, "value");
        addKeyword(Assertions.maxProperties(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withMinProperties(int value) {
        requireNonNegative(value, "value");
        addKeyword(Assertions.minProperties(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withRequired(String... names) {
        requireNonNull(names, "names");
        Set<String> set = requireUnique(names, "names");
        return withRequired(set);
    }

    @Override
    public JsonSchemaBuilder withRequired(Set<String> names) {
        requireNonNull(names, "names");
        addKeyword(Assertions.required(names));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withProperty(String name, JsonSchema subschema) {
        requireNonNull(name, "name");
        requireNonNull(subschema, "subschema");
        Properties properties = requireKeyword("properties", Combiners::properties);
        properties.addProperty(name, subschema);
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withPatternProperty(String pattern, JsonSchema subschema) {
        requireNonNull(pattern, "pattern");
        requireNonNull(subschema, "subschema");
        Pattern compiled = Pattern.compile(pattern);
        PatternProperties properties = requireKeyword("patternProperties", Combiners::patternProperties);
        properties.addProperty(compiled, subschema);
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withAdditionalProperties(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.additionalProperties(subschema));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withDependency(String name, JsonSchema subschema) {
        requireNonNull(name, "name");
        requireNonNull(subschema, "subschema");
        Dependencies keyword = requireKeyword("dependencies", Combiners::dependencies);
        keyword.addDependency(name, subschema);
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withDependency(String name, String... requiredProperties) {
        requireNonNull(name, "name");
        requireNonNull(requiredProperties, "requiredProperties");
        return withDependency(name, requireUnique(requiredProperties, "requiredProperties"));
    }

    @Override
    public JsonSchemaBuilder withDependency(String name, Set<String> requiredProperties) {
        requireNonNull(name, "name");
        requireNonNull(requiredProperties, "requiredProperties");
        Dependencies keyword = requireKeyword("dependencies", Combiners::dependencies);
        keyword.addDependency(name, requiredProperties);
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withPropertyNames(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.propertyNames(subschema));
        return nonemptyBuilder();
    }

    /* Keywords for Applying Subschemas Conditionally */

    @Override
    public JsonSchemaBuilder withIf(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.if_(subschema));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withThen(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.then_(subschema));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withElse(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.else_(subschema));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withAllOf(JsonSchema... subschemas) {
        requireNonNull(subschemas, "subschemas");
        requireNonEmpty(subschemas, "subschemas");
        return withAllOf(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withAllOf(List<JsonSchema> subschemas) {
        requireNonNull(subschemas, "subschemas");
        requireNonEmpty(subschemas, "subschemas");
        addKeyword(Combiners.allOf(subschemas));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withAnyOf(JsonSchema... subschemas) {
        requireNonNull(subschemas, "subschemas");
        requireNonEmpty(subschemas, "subschemas");
        return withAnyOf(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withAnyOf(List<JsonSchema> subschemas) {
        requireNonNull(subschemas, "subschemas");
        requireNonEmpty(subschemas, "subschemas");
        addKeyword(Combiners.anyOf(subschemas));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withOneOf(JsonSchema... subschemas) {
        requireNonNull(subschemas, "subschemas");
        requireNonEmpty(subschemas, "subschemas");
        return withOneOf(Arrays.asList(subschemas));
    }

    @Override
    public JsonSchemaBuilder withOneOf(List<JsonSchema> subschemas) {
        requireNonNull(subschemas, "subschemas");
        requireNonEmpty(subschemas, "subschemas");
        addKeyword(Combiners.oneOf(subschemas));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withNot(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.not(subschema));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withFormat(String attribute) {
        requireNonNull(attribute, "attribute");
        if (formatRegistry.containsKey(attribute)) {
            addKeyword(new Format(formatRegistry.get(attribute)));
        } else {
            throw new IllegalArgumentException("\"" + attribute + "\" is an uknown format attribute.");
        }
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withContentEncoding(String value) {
        requireNonNull(value, "value");
        if (contentRegistry.containsEncodingScheme(value)) {
            addKeyword(new ContentEncoding(contentRegistry.findEncodingScheme(value)));
        } else {
            addKeyword(new UnknownContentEncoding(value));
        }
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withContentMediaType(String value) {
        requireNonNull(value, "value");
        MediaType mediaType = MediaType.valueOf(value);
        String mimeType = mediaType.mimeType();
        if (contentRegistry.containsMimeType(mimeType)) {
            addKeyword(new ContentMediaType(contentRegistry.findMimeType(mimeType), mediaType.parameters()));
        } else {
            addKeyword(new UnknownContentMediaType(value));
        }
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withDefinition(String name, JsonSchema schema) {
        requireNonNull(name, "name");
        requireNonNull(schema, "schema");
        Definitions definitions = requireKeyword("definitions", Combiners::definitions);
        definitions.addDefinition(name, schema);
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withTitle(String title) {
        requireNonNull(title, "title");
        addKeyword(new Title(title));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withDescription(String description) {
        requireNonNull(description, "description");
        addKeyword(new Description(description));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withDefault(JsonValue value) {
        requireNonNull(value, "value");
        addKeyword(new Default(value));
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withRef(URI ref) {
        requireNonNull(ref, "ref");
        this.ref = ref;
        return nonemptyBuilder();
    }

    @Override
    public JsonSchemaBuilder withUnknown(String name, JsonSchema subschema) {
        requireNonNull(name, "name");
        requireNonNull(subschema, "subschema");
        addKeyword(Combiners.unknown(name, subschema));
        return nonemptyBuilder();
    }

    private void addKeyword(Keyword keyword) {
        this.keywords.put(keyword.name(), keyword);
    }

    @SuppressWarnings("unchecked")
    private <T extends Keyword> T requireKeyword(String name, Supplier<T> supplier) {
        T keyword = null;
        if (keywords.containsKey(name)) {
            keyword = (T) keywords.get(name);
        } else {
            keyword = supplier.get();
            keywords.put(name, keyword);
        }
        return keyword;
    }

    /**
     * Marks this builder as non-empty.
     *
     * @return this builder.
     */
    private JsonSchemaBuilder nonemptyBuilder() {
        this.empty = false;
        return this;
    }
}
