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

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
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
     * Returns the identifier of the metaschema for this vocabulary.
     *
     * @return the identifier of the metaschema as a URI.
     */
    URI getMetaschemaId();

    /**
     * Returns whether this vocabulary is public or not.
     *
     * @return {@code true} if this vocabulary is public, {@code false} otherwise.
     */
    boolean isPublic();

    /**
     * Returns all keywords defined in this vocabulary.
     *
     * @param config the configiration properties given to the schema reader.
     * @param valueSetLoader the loader of keyword value sets.
     * @return the collection of keywords, never be {@code null}.
     */
    Collection<KeywordType> getKeywordTypes(Map<String, Object> config, KeywordValueSetLoader valueSetLoader);

    /**
     * Returns the metaschema of this vocabulary as a stream.
     *
     * @return the input stream as a source.
     */
    InputStream getMetaschemaAsStream();
}
