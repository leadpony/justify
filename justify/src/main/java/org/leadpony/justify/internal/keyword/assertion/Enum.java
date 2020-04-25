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

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * An assertion specified with "enum" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("enum")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class Enum extends AbstractEqualityAssertion {

    private final Set<JsonValue> expected;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            if (value.getValueType() == ValueType.ARRAY) {
                Set<JsonValue> values = new LinkedHashSet<>();
                for (JsonValue item : value.asJsonArray()) {
                    values.add(item);
                }
                return new Enum(value, values);
            }
            throw new IllegalArgumentException();
        };
    }

    public Enum(JsonValue json, Set<JsonValue> expected) {
        super(json);
        this.expected = expected;
    }

    @Override
    protected boolean testValue(JsonValue value) {
        for (JsonValue expected : this.expected) {
            if (value.equals(expected)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Problem createProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_ENUM)
            .withParameter("expected", this.expected)
            .build();
    }

    @Override
    protected Problem createNegatedProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_NOT_ENUM)
        .withParameter("expected", this.expected)
        .build();
    }
}
