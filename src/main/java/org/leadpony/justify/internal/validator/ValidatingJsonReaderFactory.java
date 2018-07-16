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

package org.leadpony.justify.internal.validator;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;

import javax.json.JsonReaderFactory;

/**
 * JsonReaderFactory for creating JSON readers 
 * which validate JSON documents while reading.
 * 
 * @author leadpony
 */
public class ValidatingJsonReaderFactory implements JsonReaderFactory {

    private final ValidatingJsonParserFactory parserFactory;
    private final Map<String, ?> config;
    
    public ValidatingJsonReaderFactory(ValidatingJsonParserFactory parserFactory, Map<String, ?> config) {
        this.parserFactory = parserFactory;
        this.config = config;
    }

    @Override
    public ValidatingJsonReader createReader(Reader reader) {
        ValidatingJsonParser parser = parserFactory.createParser(reader);
        return new ValidatingJsonReader(parser);
    }

    @Override
    public ValidatingJsonReader createReader(InputStream in) {
        ValidatingJsonParser parser = parserFactory.createParser(in);
        return new ValidatingJsonReader(parser);
    }

    @Override
    public ValidatingJsonReader createReader(InputStream in, Charset charset) {
        ValidatingJsonParser parser = parserFactory.createParser(in, charset);
        return new ValidatingJsonReader(parser);
    }

    @Override
    public Map<String, ?> getConfigInUse() {
        return config;
    }
}
