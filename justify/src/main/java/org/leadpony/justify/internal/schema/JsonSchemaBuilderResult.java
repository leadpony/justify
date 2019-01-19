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
package org.leadpony.justify.internal.schema;

import java.net.URI;
import java.util.Map;

import javax.json.JsonBuilderFactory;

import org.leadpony.justify.internal.keyword.Keyword;

/**
 * An interface for retrieving the result of the JSON schema builder.
 *
 * @author leadpony
 */
interface JsonSchemaBuilderResult {

    /**
     * Returns the value of "$id" keyword.
     *
     * @return the value of "$id" keyword.
     */
    URI getId();

    /**
     * Returns the value of "$schema" keyword.
     *
     * @return the value of "$schema" keyword.
     */
    URI getSchema();

    /**
     * Returns the value of "$comment" keyword.
     *
     * @return the value of "$comment" keyword.
     */
    String getComment();

    /**
     * Returns all the keywords found.
     *
     * @return all the keywords found.
     */
    Map<String, Keyword> getKeywords();

    /**
     * Returns the instance of {@link JsonBuilderFactory}.
     *
     * @return the instance of {@link JsonBuilderFactory}.
     */
    JsonBuilderFactory getBuilderFactory();
}
