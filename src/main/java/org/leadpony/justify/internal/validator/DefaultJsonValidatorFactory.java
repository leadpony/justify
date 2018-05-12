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

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonValidatorFactory;
import org.leadpony.justify.internal.base.JsonParserFactoryDecorator;
import org.leadpony.justify.internal.base.JsonProviderDecorator;

/**
 * Default implementation of {@link JsonValidatorFactory}.
 * 
 * @author leadpony
 */
public class DefaultJsonValidatorFactory implements JsonValidatorFactory {
    
    private final JsonSchema rootSchema;
    private final JsonProvider jsonProvider;
    
    public DefaultJsonValidatorFactory(JsonSchema schema, JsonProvider jsonProvider) {
        this.rootSchema = schema;
        this.jsonProvider = jsonProvider;
    }

    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config) {
        JsonParserFactory real = jsonProvider.createParserFactory(config);
        return new ValidatingJsonParserFactory(real);
    }
    
    @Override
    public JsonProvider toJsonProvider() {
        return new ValidatingJsonProvider(jsonProvider);
    }
    
    private class ValidatingJsonParserFactory extends JsonParserFactoryDecorator {
        
        ValidatingJsonParserFactory(JsonParserFactory real) {
            super(real);
        }

        @Override
        public JsonParser createParser(Reader reader) {
            JsonParser parser = super.createParser(reader);
            return wrapParser(parser);
        }

        @Override
        public JsonParser createParser(InputStream in) {
            JsonParser parser = super.createParser(in);
            return wrapParser(parser);
        }

        @Override
        public JsonParser createParser(JsonObject obj) {
            JsonParser parser = super.createParser(obj);
            return wrapParser(parser);
        }

        @Override
        public JsonParser createParser(JsonArray array) {
            JsonParser parser = super.createParser(array);
            return wrapParser(parser);
        }

        @Override
        public JsonParser createParser(InputStream in, Charset charset) {
            JsonParser parser = super.createParser(in, charset);
            return wrapParser(parser);
        }

        private JsonParser wrapParser(JsonParser parser) {
            return new ValidatingJsonParser(parser, rootSchema);
        }
    }
    
    private class ValidatingJsonProvider extends JsonProviderDecorator {

        ValidatingJsonProvider(JsonProvider real) {
            super(real);
        }
 
        @Override
        public JsonParserFactory createParserFactory(Map<String, ?> config) {
            return DefaultJsonValidatorFactory.this.createParserFactory(config);
        }
    }
}
