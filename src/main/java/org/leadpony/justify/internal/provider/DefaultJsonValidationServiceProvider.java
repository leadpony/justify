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

package org.leadpony.justify.internal.provider;

import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.internal.schema.SchemaLoader;
import org.leadpony.justify.internal.validator.DefaultJsonValidatorFactory;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;
import org.leadpony.justify.core.JsonValidatorFactory;
import org.leadpony.justify.core.spi.JsonValidationServiceProvider;
import org.leadpony.justify.internal.schema.DefaultSchemaBuilderFactory;
import org.leadpony.justify.internal.schema.JsonSchemas;

/**
 * Default implementation of {@link JsonValidationServiceProvider}.
 * 
 * @author leadpony
 */
public class DefaultJsonValidationServiceProvider extends JsonValidationServiceProvider {
    
    private JsonProvider jsonProvider;
    private JsonParserFactory parserFactory;
    
    public DefaultJsonValidationServiceProvider() {
    }

    @Override
    public JsonSchema emptySchema() {
        return JsonSchemas.EMPTY;
    }
    
    @Override
    public JsonSchema alwaysTrueSchema() {
        return JsonSchemas.ALWAYS_TRUE;
    }
    
    @Override
    public JsonSchema alwaysFalseSchema() {
        return JsonSchemas.ALWAYS_FALSE;
    }

    @Override
    public JsonSchema loadSchema(InputStream in) {
        Objects.requireNonNull(in, "in must not be null.");
        try (JsonParser parser = this.parserFactory.createParser(in)) {
            return loadSchema(parser);
        }
    }

    @Override
    public JsonSchema loadSchema(Reader reader) {
        Objects.requireNonNull(reader, "reader must not be null.");
        try (JsonParser parser = this.parserFactory.createParser(reader)) {
            return loadSchema(parser);
        }
    }
    
    @Override
    public JsonSchemaBuilderFactory createSchemaBuilderFactory() {
        return new DefaultSchemaBuilderFactory(this.jsonProvider);
    }

    @Override
    public JsonValidatorFactory createValidatorFactory(JsonSchema schema) {
        return new DefaultJsonValidatorFactory(schema, this.jsonProvider);
    }
    
    @Override
    protected JsonValidationServiceProvider withBaseProvider(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.parserFactory = jsonProvider.createParserFactory(null);
        return this;
    }
    
    private JsonSchema loadSchema(JsonParser parser) {
        JsonSchemaBuilderFactory factory = createSchemaBuilderFactory();
        SchemaLoader loader = new SchemaLoader(parser, factory, this.jsonProvider);
        return loader.load();
    }
}
