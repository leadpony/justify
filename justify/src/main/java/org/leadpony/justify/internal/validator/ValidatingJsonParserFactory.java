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

package org.leadpony.justify.internal.validator;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemHandlerFactory;
import org.leadpony.justify.internal.base.json.JsonParserFactoryDecorator;

/**
 * Factory for creating JSON parsers which validate JSON document while parsing.
 *
 * @author leadpony
 */
public class ValidatingJsonParserFactory extends JsonParserFactoryDecorator {

    private final JsonSchema schema;
    private final JsonProvider jsonProvider;
    private final JsonBuilderFactory jsonBuidlerFactory;
    private final ProblemHandlerFactory handlerFactory;

    /**
     * Constructs this factory.
     *
     * @param schema             the JSON schema to be evaluated while parsing JSON
     *                           documents.
     * @param jsonProvider       the JSON provider.
     * @param jsonBuidlerFactory the JSON builder factory;
     * @param jsonParserFactory  the underlying JSON parser factory.
     * @param handlerFactory     the factory of problem handlers.
     */
    public ValidatingJsonParserFactory(JsonSchema schema,
            JsonProvider jsonProvider,
            JsonBuilderFactory jsonBuidlerFactory,
            JsonParserFactory jsonParserFactory,
            ProblemHandlerFactory handlerFactory) {
        super(jsonParserFactory);
        this.schema = schema;
        this.jsonProvider = jsonProvider;
        this.jsonBuidlerFactory = jsonBuidlerFactory;
        this.handlerFactory = handlerFactory;
    }

    @Override
    public ValidatingJsonParser createParser(Reader reader) {
        JsonParser parser = super.createParser(reader);
        return wrapParser(parser);
    }

    @Override
    public ValidatingJsonParser createParser(InputStream in) {
        JsonParser parser = super.createParser(in);
        return wrapParser(parser);
    }

    @Override
    public ValidatingJsonParser createParser(JsonObject obj) {
        JsonParser parser = super.createParser(obj);
        return wrapParser(parser);
    }

    @Override
    public ValidatingJsonParser createParser(JsonArray array) {
        JsonParser parser = super.createParser(array);
        return wrapParser(parser);
    }

    @Override
    public ValidatingJsonParser createParser(InputStream in, Charset charset) {
        JsonParser parser = super.createParser(in, charset);
        return wrapParser(parser);
    }

    private ValidatingJsonParser wrapParser(JsonParser parser) {
        ValidatingJsonParser wrapper = new ValidatingJsonParser(
                parser, this.schema, this.jsonProvider, this.jsonBuidlerFactory);
        return wrapper.withHandler(this.handlerFactory.createProblemHandler(wrapper));
    }
}