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
package org.leadpony.justify.internal.schema.io;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonSchemaReaderFactoryBuilder;
import org.leadpony.justify.api.JsonSchemaResolver;

/**
 * A configuration for schema readers.
 *
 * @author leadpony
 */
class SchemaReaderConfiguration implements JsonSchemaReaderFactoryBuilder {

    static final SchemaReaderConfiguration DEFAULT = new SchemaReaderConfiguration();

    boolean strictWithKeywords = false;
    boolean strictWithFormats = false;
    List<JsonSchemaResolver> resolvers = new ArrayList<>();

    protected SchemaReaderConfiguration() {
    }

    @Override
    public JsonSchemaReaderFactory build() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonSchemaReaderFactoryBuilder withStrictWithKeywords(boolean strict) {
        checkState();
        strictWithKeywords = strict;
        return this;
    }

    @Override
    public JsonSchemaReaderFactoryBuilder withStrictWithFormats(boolean strict) {
        checkState();
        strictWithFormats = strict;
        return this;
    }

    @Override
    public JsonSchemaReaderFactoryBuilder withSchemaResolver(JsonSchemaResolver resolver) {
        checkState();
        requireNonNull(resolver, "resolver");
        resolvers.add(resolver);
        return this;
    }

    protected void checkState() {
    }
}
