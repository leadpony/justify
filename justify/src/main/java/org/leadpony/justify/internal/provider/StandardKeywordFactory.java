/*
 * Copyright 2018-2020 the Justify authors.
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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.JsonValue;

import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.KeywordType.CreationContext;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.KeywordFactory;
import org.leadpony.justify.internal.keyword.annotation.Default;
import org.leadpony.justify.internal.keyword.annotation.Description;
import org.leadpony.justify.internal.keyword.annotation.Title;
import org.leadpony.justify.internal.keyword.applicator.AdditionalItems;
import org.leadpony.justify.internal.keyword.applicator.AdditionalProperties;
import org.leadpony.justify.internal.keyword.applicator.AllOf;
import org.leadpony.justify.internal.keyword.applicator.AnyOf;
import org.leadpony.justify.internal.keyword.applicator.Contains;
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
import org.leadpony.justify.internal.keyword.assertion.ExclusiveMaximum;
import org.leadpony.justify.internal.keyword.assertion.ExclusiveMinimum;
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
import org.leadpony.justify.internal.keyword.core.Definitions;
import org.leadpony.justify.internal.keyword.core.Id;
import org.leadpony.justify.internal.keyword.core.LegacyId;
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
            LegacyId.class,
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
    };

    private final Map<String, KeywordType> types;

    StandardKeywordFactory(SpecVersion version) {
        this.types = findTypes(version);
    }

    @Override
    public Keyword createKeyword(String name, JsonValue value, CreationContext context) {
        KeywordType type = types.get(name);
        if (type == null) {
            return null;
        }
        try {
            return type.newInstance(value, context);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Map<String, KeywordType> findTypes(SpecVersion version) {
        Map<String, KeywordType> types = new HashMap<>();
        for (Class<?> clazz : KEYWORD_CLASSES) {
            for (Spec spec : clazz.getDeclaredAnnotationsByType(Spec.class)) {
                if (spec.value() == version) {
                    KeywordType type = getKeywordType(clazz);
                    types.put(type.name(), type);
                    break;
                }
            }
        }
        return types;
    }

    private static KeywordType getKeywordType(Class<?> clazz) {
        try {
            Field field = clazz.getField("TYPE");
            return (KeywordType) field.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
