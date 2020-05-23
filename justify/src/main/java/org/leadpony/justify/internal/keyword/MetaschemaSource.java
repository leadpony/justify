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
package org.leadpony.justify.internal.keyword;

import java.io.InputStream;

/**
 * @author leadpony
 */
public interface MetaschemaSource {

    String METASCHEMA_HOME = "/org/leadpony/justify/internal/schema/";

    String name();

    String getMetaschemaName();

    default InputStream getMetaschemaAsStream() {
        String name = new StringBuilder(METASCHEMA_HOME)
                .append(name().toLowerCase())
                .append('/')
                .append(getMetaschemaName())
                .append(".json")
                .toString();
        return getClass().getResourceAsStream(name);
    }
}
