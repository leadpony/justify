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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.json.JsonValue;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.AbstractKeyword;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.KeywordMapper;
import org.leadpony.justify.internal.keyword.SchemaKeyword;

/**
 * An assertion keyword representing "minimum" for Draft-04.
 *
 * @author leadpony
 */
@KeywordType("minimum")
@Spec(SpecVersion.DRAFT_04)
public class Draft04Minimum extends Maximum {

    private boolean exclusive = false;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromNumber mapper = Draft04Minimum::new;
        return mapper;
    }

    public Draft04Minimum(JsonValue json, BigDecimal limit) {
        super(json, limit);
    }

    @Override
    public void addToEvaluatables(List<Evaluatable> evaluatables, Map<String, SchemaKeyword> keywords) {
        if (keywords.containsKey("exclusiveMinimum")) {
            ExclusiveMinimum keyword = (ExclusiveMinimum) keywords.get("exclusiveMinimum");
            exclusive = keyword.value;
        }
        super.addToEvaluatables(evaluatables, keywords);
    }

    @Override
    protected boolean testValue(BigDecimal actual, BigDecimal limit) {
        if (this.exclusive) {
            return actual.compareTo(limit) > 0;
        } else {
            return actual.compareTo(limit) >= 0;
        }
    }

    @Override
    protected Message getMessageForTest() {
        return exclusive
                ? Message.INSTANCE_PROBLEM_EXCLUSIVEMINIMUM
                : Message.INSTANCE_PROBLEM_MINIMUM;
    }

    @Override
    protected Message getMessageForNegatedTest() {
        return exclusive
                ? Message.INSTANCE_PROBLEM_MAXIMUM
                : Message.INSTANCE_PROBLEM_EXCLUSIVEMAXIMUM;
    }

    /**
     * A keyword of "exclusiveMinimum" in the Draft-04.
     *
     * @author leadpony
     */
    @KeywordType("exclusiveMinimum")
    @Spec(SpecVersion.DRAFT_04)
    public static class ExclusiveMinimum extends AbstractKeyword {

        private final boolean value;

        public static KeywordMapper mapper() {
            KeywordMapper.FromBoolean mapper = ExclusiveMinimum::new;
            return mapper;
        }

        public ExclusiveMinimum(JsonValue json, boolean value) {
            super(json);
            this.value = value;
        }
    }
}
