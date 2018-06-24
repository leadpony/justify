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

package org.leadpony.justify.internal.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

/**
 * Decorator class of {@link JsonProvider}.
 * 
 * @author leadpony
 */
public class JsonProviderDecorator extends JsonProvider {
    
    protected final JsonProvider real;
    
    public JsonProviderDecorator(JsonProvider real) {
        this.real = real;
    }
    
    public JsonProvider realProvider() {
        return real;
    }
    
    @Override
    public JsonArrayBuilder createArrayBuilder() {
        return real.createArrayBuilder();
    }

    @Override
    public JsonBuilderFactory createBuilderFactory(Map<String, ?> config) {
        return real.createBuilderFactory(config);
    }

    @Override
    public JsonGenerator createGenerator(Writer writer) {
        return createGeneratorFactory(null).createGenerator(writer);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) {
        return createGeneratorFactory(null).createGenerator(out);
    }

    @Override
    public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config) {
        return real.createGeneratorFactory(config);
    }

    @Override
    public JsonObjectBuilder createObjectBuilder() {
        return real.createObjectBuilder();
    }

    @Override
    public JsonParser createParser(Reader reader) {
        return createParserFactory(null).createParser(reader);
    }

    @Override
    public JsonParser createParser(InputStream in) {
        return createParserFactory(null).createParser(in);
    }

    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config) {
        return real.createParserFactory(config);
    }

    @Override
    public JsonReader createReader(Reader reader) {
        return createReaderFactory(null).createReader(reader);
    }

    @Override
    public JsonReader createReader(InputStream in) {
        return createReaderFactory(null).createReader(in);
    }

    @Override
    public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
        return real.createReaderFactory(config);
    }

    @Override
    public JsonWriter createWriter(Writer writer) {
        return createWriterFactory(null).createWriter(writer);
    }

    @Override
    public JsonWriter createWriter(OutputStream out) {
        return createWriterFactory(null).createWriter(out);
    }

    @Override
    public JsonWriterFactory createWriterFactory(Map<String, ?> config) {
        return real.createWriterFactory(config);
    }
}
