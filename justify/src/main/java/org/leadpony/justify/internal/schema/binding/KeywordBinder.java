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
package org.leadpony.justify.internal.schema.binding;

import javax.json.stream.JsonParser;

import org.leadpony.justify.api.SpecVersion;

/**
 * A binder type for schema keywords.
 *
 * @author leadpony
 */
public interface KeywordBinder {

    /**
     * Returns the name of the keyword.
     *
     * @return the name of the keyword.
     */
    String name();

    /**
     * Returns the supported schema versions.
     *
     * @return the supported schema versions.
     */
    default SpecVersion[] getSupportedVersions() {
        return SpecVersion.values();
    }

    /**
     * Instantiates the keyword from the parser.
     *
     * @param parser the JSON parser.
     * @param context the binder context.
     */
    void fromJson(JsonParser parser, BindingContext context);
}
