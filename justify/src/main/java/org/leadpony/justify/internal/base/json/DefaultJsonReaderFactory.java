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

package org.leadpony.justify.internal.base.json;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

/**
 * The default implementation of {@link JsonReaderFactory}.
 *
 * @author leadpony
 */
public class DefaultJsonReaderFactory implements JsonReaderFactory {

    private final JsonParserFactory parserFactory;
    private final Map<String, ?> config;

    public DefaultJsonReaderFactory(JsonParserFactory parserFactory, Map<String, ?> config) {
        this.parserFactory = parserFactory;
        this.config = config;
    }

    @Override
    public JsonReader createReader(Reader reader) {
        JsonParser parser = parserFactory.createParser(reader);
        return new DefaultJsonReader(parser);
    }

    @Override
    public JsonReader createReader(InputStream in) {
        JsonParser parser = parserFactory.createParser(in);
        return new DefaultJsonReader(parser);
    }

    @Override
    public JsonReader createReader(InputStream in, Charset charset) {
        JsonParser parser = parserFactory.createParser(in, charset);
        return new DefaultJsonReader(parser);
    }

    @Override
    public Map<String, ?> getConfigInUse() {
        return config;
    }
}
