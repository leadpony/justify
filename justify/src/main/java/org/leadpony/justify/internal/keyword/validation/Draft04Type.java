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
package org.leadpony.justify.internal.keyword.validation;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.api.keyword.KeywordType;
import org.leadpony.justify.internal.annotation.KeywordClass;
import org.leadpony.justify.internal.annotation.Spec;

/**
 * @author leadpony
 */
@KeywordClass("type")
@Spec(SpecVersion.DRAFT_04)
public final class Draft04Type {

    public static final KeywordType TYPE = new Type.TypeKeywordType() {

        @Override
        protected Keyword map(JsonValue jsonValue, InstanceType type) {
            return new Single(jsonValue, type);
        }

        @Override
        protected Keyword map(JsonValue jsonValue, Set<InstanceType> types) {
            return new Multiple(jsonValue, types);
        }
    };

    private static InstanceType getNarrowType(InstanceType type, EvaluatorContext context) {
        if (type != InstanceType.NUMBER) {
            return type;
        }
        JsonParser parser = context.getParser();
        if (parser.isIntegralNumber()) {
            return InstanceType.INTEGER;
        } else {
            BigDecimal value = parser.getBigDecimal();
            if (value.scale() <= 0) {
                return InstanceType.INTEGER;
            }
            return type;
        }
    }

    private Draft04Type() {
    }

    /**
     * A type keyword with a single value.
     *
     * @author leadpony
     */
    static class Single extends Type.Single {

        Single(JsonValue json, InstanceType expectedType) {
            super(json, expectedType);
        }

        @Override
        protected InstanceType toNarrowType(InstanceType type, EvaluatorContext context) {
            return getNarrowType(type, context);
        }
    }

    /**
     * A type keyword with multiples values.
     *
     * @author leadpony
     */
    static class Multiple extends Type.Multiple {

        Multiple(JsonValue json, Set<InstanceType> types) {
            super(json, types);
        }

        @Override
        protected InstanceType toNarrowType(InstanceType type, EvaluatorContext context) {
            return getNarrowType(type, context);
        }
    }
}
