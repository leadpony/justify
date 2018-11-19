/*
 * Copyright 2018 the Justify authors.
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

package org.leadpony.justify.internal.evaluator;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.core.ProblemDispatcher;

/**
 * @author leadpony
 */
public interface ChildrenEvaluator extends AppendableLogicalEvaluator {

    /**
     * Updates children of this evaluator.
     * @param event the event triggered by the JSON parser, cannot be {@code null}.
     * @param parser the JSON parser, cannot be {@code null}.
     * @param dispatcher the dispatcher of the found problems, cannot be {@code null}.
     */
    void updateChildren(Event event, JsonParser parser, ProblemDispatcher dispatcher);
}
