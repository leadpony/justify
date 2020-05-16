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

package org.leadpony.justify.internal.keyword;

import java.util.EnumSet;
import java.util.Set;

import org.leadpony.justify.api.EvaluatorSource;
import org.leadpony.justify.api.InstanceType;

/**
 * A evaluator source targetting only JSON arrays.
 *
 * @author leadpony
 */
public interface ArrayEvaluatorSource extends EvaluatorSource {

    @Override
    default boolean supportsType(InstanceType type) {
        return type == InstanceType.ARRAY;
    }

    @Override
    default Set<InstanceType> getSupportedTypes() {
        return EnumSet.of(InstanceType.ARRAY);
    }
}
