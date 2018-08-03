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

package org.leadpony.justify.internal.base;

import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

/**
 * Factory interface producing instances of {@link ProblemBuilder}.
 * 
 * @author leadpony
 */
public interface ProblemBuilderFactory {

    /**
     * Default instance of this type.
     */
    static ProblemBuilderFactory DEFAULT = new ProblemBuilderFactory() {};
    
    /**
     * Creates new instance of this builder.
     * 
     * @param parser the JSON parser, cannot be {@code null}.
     * @return newly created instance of {@link ProblemBuilder}.
     */
    default ProblemBuilder createProblemBuilder(JsonParser parser) {
        JsonLocation current = parser.getLocation();
        return new ProblemBuilder(SimpleJsonLocation.before(current));
    }

    /**
     * Creates new instance of this builder.
     * 
     * @param location the location where problem occurred, cannot be {@code null}.
     * @return newly created instance of {@link ProblemBuilder}.
     */
    default ProblemBuilder createProblemBuilder(JsonLocation location) {
        return new ProblemBuilder(location);
    }
}
