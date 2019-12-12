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
package org.leadpony.justify.internal.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonValue;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.KeywordFactory;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.SchemaKeyword;
import org.leadpony.justify.internal.keyword.annotation.Default;
import org.leadpony.justify.internal.keyword.annotation.Description;
import org.leadpony.justify.internal.keyword.annotation.Title;
import org.leadpony.justify.internal.keyword.applicator.AdditionalItems;
import org.leadpony.justify.internal.keyword.applicator.AdditionalProperties;
import org.leadpony.justify.internal.keyword.applicator.AllOf;
import org.leadpony.justify.internal.keyword.applicator.AnyOf;
import org.leadpony.justify.internal.keyword.applicator.Contains;
import org.leadpony.justify.internal.keyword.applicator.Definitions;
import org.leadpony.justify.internal.keyword.applicator.Dependencies;
import org.leadpony.justify.internal.keyword.applicator.Else;
import org.leadpony.justify.internal.keyword.applicator.If;
import org.leadpony.justify.internal.keyword.applicator.Items;
import org.leadpony.justify.internal.keyword.applicator.Not;
import org.leadpony.justify.internal.keyword.applicator.OneOf;
import org.leadpony.justify.internal.keyword.applicator.PatternProperties;
import org.leadpony.justify.internal.keyword.applicator.Properties;
import org.leadpony.justify.internal.keyword.applicator.PropertyNames;
import org.leadpony.justify.internal.keyword.applicator.Then;
import org.leadpony.justify.internal.keyword.assertion.Const;
import org.leadpony.justify.internal.keyword.assertion.Draft04Maximum;
import org.leadpony.justify.internal.keyword.assertion.Draft04Minimum;
import org.leadpony.justify.internal.keyword.assertion.Draft04Type;
import org.leadpony.justify.internal.keyword.assertion.ExclusiveFormatMaximum;
import org.leadpony.justify.internal.keyword.assertion.ExclusiveFormatMinimum;
import org.leadpony.justify.internal.keyword.assertion.ExclusiveMaximum;
import org.leadpony.justify.internal.keyword.assertion.ExclusiveMinimum;
import org.leadpony.justify.internal.keyword.assertion.FormatMaximum;
import org.leadpony.justify.internal.keyword.assertion.FormatMinimum;
import org.leadpony.justify.internal.keyword.assertion.MaxItems;
import org.leadpony.justify.internal.keyword.assertion.MaxLength;
import org.leadpony.justify.internal.keyword.assertion.MaxProperties;
import org.leadpony.justify.internal.keyword.assertion.Maximum;
import org.leadpony.justify.internal.keyword.assertion.MinItems;
import org.leadpony.justify.internal.keyword.assertion.MinLength;
import org.leadpony.justify.internal.keyword.assertion.MinProperties;
import org.leadpony.justify.internal.keyword.assertion.Minimum;
import org.leadpony.justify.internal.keyword.assertion.MultipleOf;
import org.leadpony.justify.internal.keyword.assertion.Pattern;
import org.leadpony.justify.internal.keyword.assertion.Required;
import org.leadpony.justify.internal.keyword.assertion.Type;
import org.leadpony.justify.internal.keyword.assertion.UniqueItems;
import org.leadpony.justify.internal.keyword.assertion.content.ContentEncoding;
import org.leadpony.justify.internal.keyword.assertion.content.ContentMediaType;
import org.leadpony.justify.internal.keyword.assertion.format.Format;
import org.leadpony.justify.internal.keyword.core.Comment;
import org.leadpony.justify.internal.keyword.core.Id;
import org.leadpony.justify.internal.keyword.core.Ref;
import org.leadpony.justify.internal.keyword.core.Schema;

/**
 * A factory of keywords defined by the specification.
 *
 * @author leadpony
 */
class StandardKeywordFactory implements KeywordFactory {

    private static final Class<?>[] KEYWORD_CLASSES = {
            Comment.class,
            Id.class,
            Ref.class,
            Schema.class,

            AdditionalItems.class,
            AdditionalProperties.class,
            AllOf.class,
            AnyOf.class,
            Comment.class,
            Const.class,
            Contains.class,
            ContentEncoding.class,
            ContentMediaType.class,
            Default.class,
            Definitions.class,
            Dependencies.class,
            Description.class,
            Draft04Maximum.class,
            Draft04Maximum.ExclusiveMaximum.class,
            Draft04Minimum.class,
            Draft04Minimum.ExclusiveMinimum.class,
            Draft04Type.class,
            Else.class,
            org.leadpony.justify.internal.keyword.assertion.Enum.class,
            ExclusiveMaximum.class,
            ExclusiveMinimum.class,
            Format.class,
            If.class,
            Items.class,
            Maximum.class,
            MaxItems.class,
            MaxLength.class,
            MaxProperties.class,
            Minimum.class,
            MinItems.class,
            MinLength.class,
            MinProperties.class,
            MultipleOf.class,
            Not.class,
            OneOf.class,
            Pattern.class,
            PatternProperties.class,
            PropertyNames.class,
            Properties.class,
            Required.class,
            Then.class,
            Title.class,
            Type.class,
            UniqueItems.class,

            // AJV extension
            FormatMinimum.class,
            FormatMaximum.class,
            ExclusiveFormatMinimum.class,
            ExclusiveFormatMaximum.class,
    };

    private final Map<String, KeywordMapper> mappers;

    StandardKeywordFactory(Map<String, KeywordMapper> mappers) {
        this.mappers = mappers;
    }

    StandardKeywordFactory(SpecVersion version) {
        this.mappers = findMappers(version);
    }

    @Override
    public SchemaKeyword createKeyword(String name, JsonValue value, CreationContext context) {
        KeywordMapper mapper = mappers.get(name);
        if (mapper == null) {
            return null;
        }
        try {
            return mapper.map(value, context);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Map<String, KeywordMapper> findMappers(SpecVersion version) {
        Map<String, KeywordMapper> mappers = new HashMap<>();
        for (Class<?> clazz : KEYWORD_CLASSES) {
            for (Spec spec : clazz.getAnnotationsByType(Spec.class)) {
                if (spec.value() == version) {
                    KeywordType keywordType = clazz.getAnnotation(KeywordType.class);
                    String name = spec.name();
                    if (name.isEmpty()) {
                        name = keywordType.value();
                    }
                    mappers.put(name, getMapper(clazz, name));
                    break;
                }
            }
        }
        return mappers;
    }

    private static KeywordMapper getMapper(Class<?> clazz, String name) {
        try {
            try {
                Method method = clazz.getMethod("mapper", String.class);
                return (KeywordMapper) method.invoke(null, name);
            } catch (NoSuchMethodException e) {
                Method method = clazz.getMethod("mapper");
                return (KeywordMapper) method.invoke(null);
            }
        } catch (IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
