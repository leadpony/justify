/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.api;

import java.util.Map;
import java.util.Optional;

/**
 * A keyword which is also an {@link EvaluatorSource}.
 *
 * @author leadpony
 */
public interface EvaluationKeyword extends Keyword, EvaluatorSource {

    @Override
    default Keyword getSourceKeyword() {
        return this;
    }

    @Override
    default Optional<EvaluatorSource> getEvaluatorSource(Map<String, Keyword> siblings) {
        if (canEvaluate()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
