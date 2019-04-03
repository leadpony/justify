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

import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * @author leadpony
 */
public interface SchemaSpec {

    boolean supportsFormatAttribute(String name);

    /**
     * Returns the format attribute of the specified name.
     *
     * @param name the name of the format attribute.
     * @return found format attribute, or {@code null}.
     */
    FormatAttribute getFormatAttribute(String name);

    boolean supportsEncodingScheme(String name);

    ContentEncodingScheme getEncodingScheme(String name);

    boolean supportsMimeType(String value);

    ContentMimeType getMimeType(String value);
}
