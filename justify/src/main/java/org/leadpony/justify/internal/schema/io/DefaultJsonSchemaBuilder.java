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

import static org.leadpony.justify.internal.base.Arguments.requireNonEmpty;
import static org.leadpony.justify.internal.base.Arguments.requireNonNegative;
import static org.leadpony.justify.internal.base.Arguments.requireNonNull;
import static org.leadpony.justify.internal.base.Arguments.requirePositive;
import static org.leadpony.justify.internal.base.Arguments.requireUnique;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.json.JsonValue;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaBuilder;
import org.leadpony.justify.internal.base.MediaType;
import org.leadpony.justify.internal.base.json.JsonService;
import org.leadpony.justify.internal.keyword.SchemaKeyword;
import org.leadpony.justify.internal.keyword.annotation.Default;
import org.leadpony.justify.internal.keyword.annotation.Description;
import org.leadpony.justify.internal.keyword.annotation.Title;
import org.leadpony.justify.internal.keyword.assertion.Const;
import org.leadpony.justify.internal.keyword.assertion.Enum;
import org.leadpony.justify.internal.keyword.assertion.ExclusiveMaximum;
import org.leadpony.justify.internal.keyword.assertion.ExclusiveMinimum;
import org.leadpony.justify.internal.keyword.assertion.MaxContains;
import org.leadpony.justify.internal.keyword.assertion.MaxItems;
import org.leadpony.justify.internal.keyword.assertion.MaxLength;
import org.leadpony.justify.internal.keyword.assertion.MaxProperties;
import org.leadpony.justify.internal.keyword.assertion.Maximum;
import org.leadpony.justify.internal.keyword.assertion.MinContains;
import org.leadpony.justify.internal.keyword.assertion.MinItems;
import org.leadpony.justify.internal.keyword.assertion.MinLength;
import org.leadpony.justify.internal.keyword.assertion.MinProperties;
import org.leadpony.justify.internal.keyword.assertion.Minimum;
import org.leadpony.justify.internal.keyword.assertion.MultipleOf;
import org.leadpony.justify.internal.keyword.assertion.Required;
import org.leadpony.justify.internal.keyword.assertion.Type;
import org.leadpony.justify.internal.keyword.assertion.UniqueItems;
import org.leadpony.justify.internal.keyword.assertion.content.ContentEncoding;
import org.leadpony.justify.internal.keyword.assertion.content.ContentMediaType;
import org.leadpony.justify.internal.keyword.assertion.content.UnknownContentEncoding;
import org.leadpony.justify.internal.keyword.assertion.content.UnknownContentMediaType;
import org.leadpony.justify.internal.keyword.assertion.format.Format;
import org.leadpony.justify.internal.keyword.combiner.AdditionalItems;
import org.leadpony.justify.internal.keyword.combiner.AdditionalProperties;
import org.leadpony.justify.internal.keyword.combiner.AllOf;
import org.leadpony.justify.internal.keyword.combiner.AnyOf;
import org.leadpony.justify.internal.keyword.combiner.Contains;
import org.leadpony.justify.internal.keyword.combiner.Definitions;
import org.leadpony.justify.internal.keyword.combiner.Dependencies;
import org.leadpony.justify.internal.keyword.combiner.Else;
import org.leadpony.justify.internal.keyword.combiner.If;
import org.leadpony.justify.internal.keyword.combiner.Items;
import org.leadpony.justify.internal.keyword.combiner.Not;
import org.leadpony.justify.internal.keyword.combiner.OneOf;
import org.leadpony.justify.internal.keyword.combiner.PatternProperties;
import org.leadpony.justify.internal.keyword.combiner.Properties;
import org.leadpony.justify.internal.keyword.combiner.PropertyNames;
import org.leadpony.justify.internal.keyword.combiner.Then;
import org.leadpony.justify.internal.keyword.core.Comment;
import org.leadpony.justify.internal.keyword.core.Id;
import org.leadpony.justify.internal.keyword.core.Schema;
import org.leadpony.justify.internal.schema.BasicSchema;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * The default implementation of {@link JsonSchemaBuilder}.
 *
 * @author leadpony
 */
class DefaultJsonSchemaBuilder implements JsonSchemaBuilder {

    private final JsonService jsonService;
    private final SchemaSpec spec;
    private final Map<String, SchemaKeyword> keywords = new LinkedHashMap<>();
    private URI id;

    private final Map<String, KeywordBuilder> builders = new HashMap<>();

    /**
     * Constructs this builder.
     *
     * @param jsonService the JSON service.
     * @param spec        the schema specification.
     */
    DefaultJsonSchemaBuilder(JsonService jsonService, SchemaSpec spec) {
        this.jsonService = jsonService;
        this.spec = spec;
    }

    /* As a JsonSchemaBuilder */

    @Override
    public JsonSchema build() {
        finishBuilders();
        if (keywords.isEmpty()) {
            return JsonSchema.EMPTY;
        } else if (keywords.containsKey("$ref")) {
            return new SchemaReference(id, keywords, jsonService);
        } else {
            return BasicSchema.newSchema(id, keywords, jsonService);
        }
    }

    @Override
    public JsonSchemaBuilder withId(URI id) {
        requireNonNull(id, "id");
        addKeyword(new Id(id));
        this.id = id;
        return this;
    }

    @Override
    public JsonSchemaBuilder withSchema(URI schema) {
        requireNonNull(schema, "schema");
        addKeyword(new Schema(schema));
        return this;
    }

    @Override
    public JsonSchemaBuilder withComment(String comment) {
        requireNonNull(comment, "comment");
        addKeyword(new Comment(comment));
        return this;
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
        addKeyword(Type.of(types));
        return this;
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
        addKeyword(new Enum(values));
        return this;
    }

    @Override
    public JsonSchemaBuilder withConst(JsonValue value) {
        requireNonNull(value, "value");
        addKeyword(new Const(value));
        return this;
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
        addKeyword(new MultipleOf(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMaximum(BigDecimal value) {
        requireNonNull(value, "value");
        addKeyword(new Maximum(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withExclusiveMaximum(BigDecimal value) {
        requireNonNull(value, "value");
        addKeyword(new ExclusiveMaximum(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMinimum(BigDecimal value) {
        requireNonNull(value, "value");
        addKeyword(new Minimum(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withExclusiveMinimum(BigDecimal value) {
        requireNonNull(value, "value");
        addKeyword(new ExclusiveMinimum(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMaxLength(int value) {
        requireNonNegative(value, "value");
        addKeyword(new MaxLength(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMinLength(int value) {
        requireNonNegative(value, "value");
        addKeyword(new MinLength(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withPattern(String pattern) {
        requireNonNull(pattern, "pattern");
        Pattern compiled = Pattern.compile(pattern);
        addKeyword(new org.leadpony.justify.internal.keyword.assertion.Pattern(compiled));
        return this;
    }

    /* Validation Keywords for Arrays */

    @Override
    public JsonSchemaBuilder withItems(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(Items.of(subschema));
        return this;
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
        addKeyword(Items.of(subschemas));
        return this;
    }

    @Override
    public JsonSchemaBuilder withAdditionalItems(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(new AdditionalItems(subschema));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMaxItems(int value) {
        requireNonNegative(value, "value");
        addKeyword(new MaxItems(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMinItems(int value) {
        requireNonNegative(value, "value");
        addKeyword(new MinItems(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withUniqueItems(boolean unique) {
        addKeyword(new UniqueItems(unique));
        return this;
    }

    @Override
    public JsonSchemaBuilder withContains(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(new Contains(subschema));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMaxContains(int value) {
        requireNonNegative(value, "value");
        addKeyword(new MaxContains(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMinContains(int value) {
        requireNonNegative(value, "value");
        addKeyword(new MinContains(value));
        return this;
    }

    /* Validation Keywords for Objects */

    @Override
    public JsonSchemaBuilder withMaxProperties(int value) {
        requireNonNegative(value, "value");
        addKeyword(new MaxProperties(value));
        return this;
    }

    @Override
    public JsonSchemaBuilder withMinProperties(int value) {
        requireNonNegative(value, "value");
        addKeyword(new MinProperties(value));
        return this;
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
        addKeyword(new Required(names));
        return this;
    }

    @Override
    public JsonSchemaBuilder withProperty(String name, JsonSchema subschema) {
        requireNonNull(name, "name");
        requireNonNull(subschema, "subschema");
        getBuilder("properties", PropertiesBuilder::new)
                .append(name, subschema);
        return this;
    }

    @Override
    public JsonSchemaBuilder withProperties(Map<String, JsonSchema> subschemas) {
        requireNonNull(subschemas, "subschemas");
        getBuilder("properties", PropertiesBuilder::new)
                .append(subschemas);
        return this;
    }

    @Override
    public JsonSchemaBuilder withPatternProperty(String pattern, JsonSchema subschema) {
        requireNonNull(pattern, "pattern");
        requireNonNull(subschema, "subschema");
        Pattern compiled = Pattern.compile(pattern);
        getBuilder("patternProperties", PatternPropertiesBuilder::new)
                .append(compiled, subschema);
        return this;
    }

    @Override
    public JsonSchemaBuilder withPatternProperties(Map<String, JsonSchema> subschemas) {
        requireNonNull(subschemas, "subschemas");
        Map<Pattern, JsonSchema> compiledMap = new HashMap<>();
        subschemas.forEach((pattern, subschema) -> {
            compiledMap.put(Pattern.compile(pattern), subschema);
        });
        getBuilder("patternProperties", PatternPropertiesBuilder::new)
                .append(compiledMap);
        return this;
    }

    @Override
    public JsonSchemaBuilder withAdditionalProperties(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(new AdditionalProperties(subschema));
        return this;
    }

    @Override
    public JsonSchemaBuilder withDependency(String name, JsonSchema subschema) {
        requireNonNull(name, "name");
        requireNonNull(subschema, "subschema");
        getBuilder("dependencies", DependenciesBuilder::new)
                .append(name, subschema);
        return this;
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
        getBuilder("dependencies", DependenciesBuilder::new)
                .append(name, requiredProperties);
        return this;
    }

    @Override
    public JsonSchemaBuilder withDependencies(Map<String, Object> values) {
        requireNonNull(values, "values");
        getBuilder("dependencies", DependenciesBuilder::new)
                .append(values);
        return this;
    }

    @Override
    public JsonSchemaBuilder withPropertyNames(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(new PropertyNames(subschema));
        return this;
    }

    /* Keywords for Applying Subschemas Conditionally */

    @Override
    public JsonSchemaBuilder withIf(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(new If(subschema));
        return this;
    }

    @Override
    public JsonSchemaBuilder withThen(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(new Then(subschema));
        return this;
    }

    @Override
    public JsonSchemaBuilder withElse(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(new Else(subschema));
        return this;
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
        addKeyword(new AllOf(subschemas));
        return this;
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
        addKeyword(new AnyOf(subschemas));
        return this;
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
        addKeyword(new OneOf(subschemas));
        return this;
    }

    @Override
    public JsonSchemaBuilder withNot(JsonSchema subschema) {
        requireNonNull(subschema, "subschema");
        addKeyword(new Not(subschema));
        return this;
    }

    @Override
    public JsonSchemaBuilder withFormat(String attribute) {
        requireNonNull(attribute, "attribute");
        FormatAttribute foundAttribute = spec.getFormatAttribute(attribute);
        if (foundAttribute != null) {
            Format format = Format.of(foundAttribute);
            addKeyword(format);
        } else {
            throw new IllegalArgumentException("\"" + attribute + "\" is not recognized as a format attribute.");
        }
        return this;
    }

    @Override
    public JsonSchemaBuilder withLaxFormat(String attribute) {
        requireNonNull(attribute, "attribute");
        Format format = null;
        FormatAttribute foundAttribute = spec.getFormatAttribute(attribute);
        if (foundAttribute != null) {
            format = Format.of(foundAttribute);
        } else {
            format = Format.of(attribute);
        }
        addKeyword(format);
        return this;
    }

    @Override
    public JsonSchemaBuilder withContentEncoding(String value) {
        requireNonNull(value, "value");
        ContentEncodingScheme scheme = spec.getEncodingScheme(value);
        if (scheme != null) {
            addKeyword(new ContentEncoding(scheme));
        } else {
            addKeyword(new UnknownContentEncoding(value));
        }
        return this;
    }

    @Override
    public JsonSchemaBuilder withContentMediaType(String value) {
        requireNonNull(value, "value");
        MediaType mediaType = MediaType.valueOf(value);
        String mimeType = mediaType.mimeType();
        ContentMimeType foundMimeType = spec.getMimeType(mimeType);
        if (foundMimeType != null) {
            addKeyword(new ContentMediaType(foundMimeType, mediaType.parameters()));
        } else {
            addKeyword(new UnknownContentMediaType(value));
        }
        return this;
    }

    @Override
    public JsonSchemaBuilder withDefinition(String name, JsonSchema schema) {
        requireNonNull(name, "name");
        requireNonNull(schema, "schema");
        getBuilder("definitions", DefinitionsBuilder::new)
                .append(name, schema);
        return this;
    }

    @Override
    public JsonSchemaBuilder withDefinitions(Map<String, JsonSchema> schemas) {
        requireNonNull(schemas, "schemas");
        getBuilder("definitions", DefinitionsBuilder::new)
                .append(schemas);
        return this;
    }

    @Override
    public JsonSchemaBuilder withTitle(String title) {
        requireNonNull(title, "title");
        addKeyword(new Title(title));
        return this;
    }

    @Override
    public JsonSchemaBuilder withDescription(String description) {
        requireNonNull(description, "description");
        addKeyword(new Description(description));
        return this;
    }

    @Override
    public JsonSchemaBuilder withDefault(JsonValue value) {
        requireNonNull(value, "value");
        addKeyword(new Default(value));
        return this;
    }

    private void addKeyword(SchemaKeyword keyword) {
        this.keywords.put(keyword.name(), keyword);
    }

    @SuppressWarnings("unchecked")
    private <T extends KeywordBuilder> T getBuilder(String name, Supplier<T> supplier) {
        T builder = null;
        if (builders.containsKey(name)) {
            builder = (T) builders.get(name);
        } else {
            builder = supplier.get();
            builders.put(name, builder);
        }
        return builder;
    }

    private void finishBuilders() {
        for (KeywordBuilder builder : builders.values()) {
            SchemaKeyword keyword = builder.build();
            keywords.put(keyword.name(), keyword);
        }
    }

    /**
     * A builder of a keyword.
     *
     * @author leadpony
     */
    private interface KeywordBuilder {

        SchemaKeyword build();
    }

    /**
     * A skeletal implementation of {@link KeywordBuilder}.
     *
     * @author leadpony
     *
     * @param <K> the type of the key.
     * @param <V> the type of the value.
     */
    private abstract static class AbstractKeywordBuilder<K, V> implements KeywordBuilder {

        protected final Map<K, V> map = new LinkedHashMap<>();

        void append(K key, V value) {
            this.map.put(key, value);
        }

        void append(Map<K, V> map) {
            this.map.putAll(map);
        }
    }

    /**
     * A builder of "properties" keyword.
     *
     * @author leadpony
     */
    private static class PropertiesBuilder extends AbstractKeywordBuilder<String, JsonSchema> {

        public SchemaKeyword build() {
            return new Properties(map);
        }
    }

    /**
     * A builder of "patternProperties" keyword.
     *
     * @author leadpony
     */
    private static class PatternPropertiesBuilder extends AbstractKeywordBuilder<Pattern, JsonSchema> {

        public SchemaKeyword build() {
            return new PatternProperties(map);
        }
    }

    /**
     * A builder of "definitions" keyword.
     *
     * @author leadpony
     */
    private static class DefinitionsBuilder extends AbstractKeywordBuilder<String, JsonSchema> {

        public SchemaKeyword build() {
            return new Definitions(map);
        }
    }

    /**
     * A builder of "dependencies" keyword.
     *
     * @author leadpony
     */
    private static class DependenciesBuilder extends AbstractKeywordBuilder<String, Object> {

        @Override
        public SchemaKeyword build() {
            return new Dependencies(map);
        }
    }
}
