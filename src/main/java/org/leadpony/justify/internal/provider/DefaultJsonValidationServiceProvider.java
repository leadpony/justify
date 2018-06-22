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
import java.net.URI;
import java.util.Collections;
import java.util.Objects;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.leadpony.justify.internal.validator.DefaultJsonValidatorFactory;
import org.leadpony.justify.internal.validator.ValidatingJsonParser;
import org.leadpony.justify.internal.validator.ValidatingJsonParserFactory;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;
import org.leadpony.justify.core.JsonSchemaReader;
import org.leadpony.justify.core.JsonSchemaResolver;
import org.leadpony.justify.core.JsonValidatorFactory;
import org.leadpony.justify.core.spi.JsonValidationServiceProvider;
import org.leadpony.justify.internal.schema.BasicSchemaBuilderFactory;
import org.leadpony.justify.internal.schema.io.BasicSchemaReader;
import org.leadpony.justify.internal.schema.io.ValidatingSchemaReader;

/**
 * Default implementation of {@link JsonValidationServiceProvider}.
 * 
 * @author leadpony
 */
public class DefaultJsonValidationServiceProvider 
        extends JsonValidationServiceProvider implements JsonSchemaResolver {
    
    private JsonProvider jsonProvider;
    
    private JsonSchema metaschema;
    private ValidatingJsonParserFactory parserFactory;
    
    private static final String METASCHEMA_NAME = "metaschema-draft-07.json";
    
    public DefaultJsonValidationServiceProvider() {
    }

    @Override
    public JsonSchemaReader createSchemaReader(InputStream in) {
        Objects.requireNonNull(in, "in must not be null.");
        ValidatingJsonParser parser = this.parserFactory.createParser(in);
        return createValidatingSchemaReader(parser);
    }
  
    @Override
    public JsonSchemaReader createSchemaReader(Reader reader) {
        Objects.requireNonNull(reader, "reader must not be null.");
        ValidatingJsonParser parser = this.parserFactory.createParser(reader);
        return createValidatingSchemaReader(parser);
    }
    
    @Override
    public JsonSchemaBuilderFactory createSchemaBuilderFactory() {
        return createBasicSchemaBuilderFactory();
    }

    @Override
    public JsonValidatorFactory createValidatorFactory(JsonSchema schema) {
        Objects.requireNonNull(schema, "schema must not be null.");
        return createDefaultJsonValidatorFactory(schema);
    }
    
    @Override
    protected void initialize(JsonProvider jsonProvider) {
        Objects.requireNonNull(jsonProvider, "jsonProvider must not be null.");
        this.jsonProvider = jsonProvider;
        this.metaschema = loadMetaschema(METASCHEMA_NAME);
        this.parserFactory = createDefaultJsonValidatorFactory(this.metaschema)
                .createParserFactory(Collections.emptyMap());
    }
    
    @Override
    public JsonSchema resolveSchema(URI id) {
        Objects.requireNonNull(id, "id must not be null.");
        if (id.equals(metaschema.id())) {
            return metaschema;
        } else {
            return null;
        }
    }
    
    private JsonSchema loadMetaschema(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        JsonParser parser = this.jsonProvider.createParser(in);
        try (JsonSchemaReader reader = new BasicSchemaReader(parser, createBasicSchemaBuilderFactory())) {
            return reader.read();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @SuppressWarnings("resource")
    private JsonSchemaReader createValidatingSchemaReader(ValidatingJsonParser parser) {
        return new ValidatingSchemaReader(parser, createBasicSchemaBuilderFactory())
                    .withSchemaResolver(this);
    }
    
    private DefaultJsonValidatorFactory createDefaultJsonValidatorFactory(JsonSchema schema) {
        return new DefaultJsonValidatorFactory(schema, this.jsonProvider);
    }

    private BasicSchemaBuilderFactory createBasicSchemaBuilderFactory() {
        return new BasicSchemaBuilderFactory(this.jsonProvider);
    }
}
