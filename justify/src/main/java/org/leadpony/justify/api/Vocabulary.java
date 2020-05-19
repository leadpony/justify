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

import java.net.URI;
import java.util.Map;

/**
 * A schema vocabulary wchich is a set of keywords, their syntax, and their
 * semantics..
 *
 * @author leadpony
 */
public interface Vocabulary {

    /**
     * Returns the identifier of this vocabulary as a URI.
     *
     * @return the identifier of this vocabulary as a URI.
     */
    URI getId();

    /**
     * Returns all keywords provided by this vocabulary.
     *
     * @return all keywords as a map, never be {@code null}.
     */
    Map<String, KeywordType> asMap();
}