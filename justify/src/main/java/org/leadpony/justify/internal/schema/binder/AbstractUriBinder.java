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
package org.leadpony.justify.internal.schema.binder;

import java.net.URI;
import java.net.URISyntaxException;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.internal.keyword.Keyword;

/**
 * @author leadpony
 */
abstract class AbstractUriBinder extends AbstractBinder {

    @Override
    public void fromJson(JsonParser parser, BinderContext context) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            try {
                URI uri = parse(parser.getString());
                Keyword keyword = createKeyword(uri);
                addKeyword(keyword, context);
            } catch (URISyntaxException e) {
                // Ignores the exception
            }
        } else {
            skipValue(event, parser);
        }
    }

    protected abstract Keyword createKeyword(URI value);

    protected void addKeyword(Keyword keyword, BinderContext context) {
        context.addKeyword(keyword);
    }

    /**
     * Parses the given value into a URI.
     * This method can parse fragment-only URI which is not percent-encoded.
     *
     * @param value the value to parse.
     * @return newly created URI.
     * @throws URISyntaxException if the {@code value} is not a valid URI.
     */
    private static URI parse(String value) throws URISyntaxException {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            if (value.startsWith("#")) {
                return new URI(null, null, value.substring(1));
            }
            throw e;
        }
    }
}
