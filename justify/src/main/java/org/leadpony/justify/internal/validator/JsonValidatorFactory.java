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
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.ProblemHandlerFactory;
import org.leadpony.justify.api.ValidationConfig;
import org.leadpony.justify.internal.base.json.JsonParserFactoryDecorator;

/**
 * Factory for creating JSON parsers which validate JSON document while parsing.
 *
 * @author leadpony
 */
public class JsonValidatorFactory extends JsonParserFactoryDecorator {

    private final JsonSchema schema;
    private final JsonProvider jsonProvider;
    private final ProblemHandlerFactory handlerFactory;
    private final Map<String, ?> properties;

    private static final ProblemHandlerFactory DEFAULT_HANDLER_FACTORY = parser -> problems -> {
    };

    /**
     * Constructs this factory.
     *
     * @param schema            the JSON schema to be evaluated while parsing JSON
     *                          documents.
     * @param jsonProvider      the JSON provider.
     * @param jsonParserFactory the underlying JSON parser factory.
     * @param handlerFactory    the factory of problem handlers.
     * @param properties        the configuration properties.
     */
    public JsonValidatorFactory(
            JsonSchema schema,
            JsonProvider jsonProvider,
            JsonParserFactory jsonParserFactory,
            ProblemHandlerFactory handlerFactory,
            Map<String, Object> properties) {
        super(jsonParserFactory);
        this.schema = schema;
        this.jsonProvider = jsonProvider;
        this.handlerFactory = handlerFactory;
        this.properties = properties;
    }

    /**
     * Constructs this factory.
     *
     * @param jsonProvider      the JSON provider.
     * @param jsonParserFactory the underlying JSON parser factory.
     * @param properties        the configuration properties.
     */
    public JsonValidatorFactory(
            JsonProvider jsonProvider,
            JsonParserFactory realFactory,
            Map<String, ?> properties) {
        super(realFactory);

        this.jsonProvider = jsonProvider;
        this.properties = properties;

        this.schema = (JsonSchema) properties.get(ValidationConfig.SCHEMA);
        ProblemHandlerFactory handlerFactory = (ProblemHandlerFactory) properties
                .get(ValidationConfig.PROBLEM_HANDLER_FACTORY);
        this.handlerFactory = (handlerFactory != null) ? handlerFactory : DEFAULT_HANDLER_FACTORY;

        assert this.schema != null;
    }

    @Override
    public JsonValidator createParser(Reader reader) {
        JsonParser parser = super.createParser(reader);
        return createValiator(parser);
    }

    @Override
    public JsonValidator createParser(InputStream in) {
        JsonParser parser = super.createParser(in);
        return createValiator(parser);
    }

    @Override
    public JsonValidator createParser(JsonObject obj) {
        JsonParser parser = super.createParser(obj);
        return createValiator(parser);
    }

    @Override
    public JsonValidator createParser(JsonArray array) {
        JsonParser parser = super.createParser(array);
        return createValiator(parser);
    }

    @Override
    public JsonValidator createParser(InputStream in, Charset charset) {
        JsonParser parser = super.createParser(in, charset);
        return createValiator(parser);
    }

    private boolean usesDefaultValues() {
        Object value = properties.get(ValidationConfig.DEFAULT_VALUES);
        return value == Boolean.TRUE;
    }

    private JsonValidator createValiator(JsonParser parser) {
        JsonValidator validator = newValidator(parser);
        return validator.withHandler(this.handlerFactory.createProblemHandler(validator));
    }

    private JsonValidator newValidator(JsonParser parser) {
        if (usesDefaultValues()) {
            return new DefaultizingJsonValidator(parser, this.schema, this.jsonProvider);
        } else {
            return new JsonValidator(parser, this.schema, jsonProvider);
        }
    }
}