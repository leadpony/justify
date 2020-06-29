/*
 * Copyright 2018, 2020 the Justify authors.
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
package org.leadpony.justify.internal.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.keyword.IdKeyword;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.keyword.core.Id;

/**
 * @author leadpony
 */
public class SchemaCatalogTest {

    private static JsonProvider jsonProvider;

    @BeforeAll
    public static void setUp() {
        jsonProvider = JsonProvider.provider();
    }

    @Test
    public void resolveSchemaShouldReturnSchema() {
        SchemaCatalog catalog = new SchemaCatalog();
        JsonSchema schema = createSchema(URI.create("http://example.com/root.json#"));
        catalog.addSchema(schema);

        JsonSchema actual = catalog.resolveSchema(URI.create("http://example.com/root.json"));
        assertThat(actual).isEqualTo(schema);
    }

    private static JsonSchema createSchema(URI uri) {
        JsonObjectBuilder builder = jsonProvider.createObjectBuilder();
        Map<String, Keyword> keywords = new HashMap<>();
        JsonValue json = jsonProvider.createValue(uri.toString());
        IdKeyword id = new Id(json, uri);
        keywords.put("$id", id);
        builder.add("$id", json);
        return BasicJsonSchema.of(keywords, id, builder.build());
    }
}
