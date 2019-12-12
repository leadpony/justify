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

import java.util.regex.PatternSyntaxException;

import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.regex.Ecma262Pattern;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.problem.ProblemBuilder;

/**
 * Assertion specified with "pattern" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("pattern")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
@Spec(SpecVersion.AJV_EXTENSION_PROPOSAL)
public class Pattern extends AbstractStringAssertion {

    private final java.util.regex.Pattern pattern;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        return (value, context) -> {
            if (value.getValueType() == ValueType.STRING) {
                String string = ((JsonString) value).getString();
                try {
                    return new Pattern(value, Ecma262Pattern.compile(string));
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            throw new IllegalArgumentException();
        };
    }

    public Pattern(JsonValue json, java.util.regex.Pattern pattern) {
        super(json);
        this.pattern = pattern;
    }

    @Override
    protected boolean testValue(String value) {
        return pattern.matcher(value).find();
    }

    @Override
    protected Problem createProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_PATTERN)
            .withParameter("pattern", pattern.toString())
            .build();
    }

    @Override
    protected Problem createNegatedProblem(ProblemBuilder builder) {
        return builder.withMessage(Message.INSTANCE_PROBLEM_NOT_PATTERN)
            .withParameter("pattern", pattern.toString())
            .build();
    }
}
