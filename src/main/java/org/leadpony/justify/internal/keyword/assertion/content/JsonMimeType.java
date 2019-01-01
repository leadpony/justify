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
package org.leadpony.justify.internal.keyword.assertion.content;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Map;

import javax.json.JsonException;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.leadpony.justify.spi.ContentMimeType;

/**
 * MIME type for "applicaiton/json".
 *
 * @author leadpony
 */
class JsonMimeType implements ContentMimeType {

    private final JsonProvider jsonProvider;

    JsonMimeType(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    @Override
    public String toString() {
        return "application/json";
    }

    @Override
    public boolean test(String content) {
        try (JsonParser parser = jsonProvider.createParser(new StringReader(content))) {
            return parseAllWith(parser);
        } catch (JsonException e) {
            return false;
        }
    }

    @Override
    public boolean test(byte[] decodedContent, Map<String, String> parameters) {
        try (JsonParser parser = jsonProvider.createParser(new ByteArrayInputStream(decodedContent))) {
            return parseAllWith(parser);
        } catch (JsonException e) {
            return false;
        }
    }

    private static boolean parseAllWith(JsonParser parser) {
        while (parser.hasNext()) {
            parser.next();
        }
        return true;
    }
}
