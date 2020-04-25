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

import java.util.List;
import java.util.Map;

import jakarta.json.JsonValue;
import org.leadpony.justify.internal.keyword.AbstractKeyword;
import org.leadpony.justify.internal.keyword.Evaluatable;
import org.leadpony.justify.internal.keyword.SchemaKeyword;

/**
 * The type representing "minContains" keyword.
 *
 * @author leadpony
 */
public class MinContains extends AbstractKeyword {

    private final int limit;

    public MinContains(JsonValue json, int limit) {
        super(json);
        this.limit = limit;
    }

    /**
     * Returns the value of this keyword.
     *
     * @return the value of this keyword.
     */
    public int value() {
        return limit;
    }

    @Override
    public void addToEvaluatables(List<Evaluatable> evaluatables, Map<String, SchemaKeyword> keywords) {
    }
}
