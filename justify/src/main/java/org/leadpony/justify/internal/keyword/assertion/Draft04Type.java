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
package org.leadpony.justify.internal.keyword.assertion;

import java.util.Set;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;

/**
 * @author leadpony
 */
public class Draft04Type {

    public static Type of(InstanceType type) {
        return new Single(type);
    }

    public static Type of(Set<InstanceType> types) {
        if (types.size() == 1) {
            return new Single(types.iterator().next());
        } else {
            return new Multiple(types);
        }
    }

    private static InstanceType getNarrowType(InstanceType type, EvaluatorContext context) {
        if (type == InstanceType.NUMBER &&
                context.getParser().isIntegralNumber()) {
            return InstanceType.INTEGER;
        } else {
            return type;
        }
    }

    static class Single extends Type.Single {

        Single(InstanceType expectedType) {
            super(expectedType);
        }

        @Override
        protected InstanceType toNarrowType(InstanceType type, EvaluatorContext context) {
            return getNarrowType(type, context);
        }
    }

    static class Multiple extends Type.Multiple {

        Multiple(Set<InstanceType> types) {
            super(types);
        }

        @Override
        protected InstanceType toNarrowType(InstanceType type, EvaluatorContext context) {
            return getNarrowType(type, context);
        }
    }
}
