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
import java.util.Objects;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.leadpony.justify.internal.validator.DefaultJsonValidatorFactory;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaReader;
import org.leadpony.justify.core.JsonSchemaResolver;
import org.leadpony.justify.core.JsonValidatorFactory;
import org.leadpony.justify.core.spi.JsonValidationServiceProvider;
import org.leadpony.justify.internal.schema.BasicSchemaBuilderFactory;
import org.leadpony.justify.internal.schema.io.DefaultSchemaReader;

/**
 * Default implementation of {@link JsonValidationServiceProvider}.
 * 
 * @author leadpony
 */
public class DefaultJsonValidationServiceProvider 
        extends JsonValidationServiceProvider implements JsonSchemaResolver {
    
    private JsonProvider jsonProvider;
    private JsonParserFactory parserFactory;
    
    private JsonSchema metaschema;
    
    private static final String METASCHEMA_NAME = "metaschema-draft-07.json";
    
    public DefaultJsonValidationServiceProvider() {
    }

    @Override
    public JsonSchemaReader createReader(InputStream in) {
        Objects.requireNonNull(in, "in must not be null.");
        JsonParser parser = this.parserFactory.createParser(in);
        return createSchemaReaderWithResolver(parser);
    }
  
    @Override
    public JsonSchemaReader createReader(Reader reader) {
        Objects.requireNonNull(reader, "reader must not be null.");
        JsonParser parser = this.parserFactory.createParser(reader);
        return createSchemaReaderWithResolver(parser);
    }
    
    @Override
    public BasicSchemaBuilderFactory createSchemaBuilderFactory() {
        return new BasicSchemaBuilderFactory(this.jsonProvider);
    }

    @Override
    public JsonValidatorFactory createValidatorFactory(JsonSchema schema) {
        return new DefaultJsonValidatorFactory(schema, this.jsonProvider);
    }
    
    @Override
    protected void initialize(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.parserFactory = jsonProvider.createParserFactory(null);
        this.metaschema = loadMetaschema(METASCHEMA_NAME);
    }
    
    @Override
    public JsonSchema resolveSchema(URI id) {
        if (id.equals(metaschema.id())) {
            return metaschema;
        } else {
            return null;
        }
    }
    
    private JsonSchema loadMetaschema(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        JsonParser parser = this.parserFactory.createParser(in);
        try (JsonSchemaReader reader = createSchemaReader(parser)) {
            return reader.read();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private JsonSchemaReader createSchemaReader(JsonParser parser) {
        return new DefaultSchemaReader(parser, this.createSchemaBuilderFactory());
    }
    
    private JsonSchemaReader createSchemaReaderWithResolver(JsonParser parser) {
        return createSchemaReader(parser)
                .withSchemaResolver(this);
    }
}
