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

import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.core.Id;
import org.leadpony.justify.internal.keyword.core.Ref;
import org.leadpony.justify.internal.keyword.core.Schema;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A context for binders.
 *
 * @author leadpony
 */
public interface BindingContext {

    /**
     * Reads a subschema from the current location.
     *
     * @param the next parser event.
     * @return read schema.
     */
    JsonSchema readSchema(Event event);

    /**
     * Returns the format attribute specified by the name.
     *
     * @param name the name of the format attribute.
     * @return found format attribute.
     */
    FormatAttribute getFormatAttribute(String name);

    ContentEncodingScheme getEncodingScheme(String name);

    ContentMimeType getMimeType(String value);

    void addKeyword(Keyword keyword);

    void addKeyword(Id keyword);

    void addKeyword(Ref keyword);

    void addKeyword(Schema keyword);

    void addProblem(Message message);
}
