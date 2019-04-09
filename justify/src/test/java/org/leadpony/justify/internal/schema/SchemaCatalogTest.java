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
package org.leadpony.justify.internal.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonBuilderFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.core.Id;

/**
 * @author leadpony
 */
public class SchemaCatalogTest {

    private static JsonBuilderFactory jsonBuilderFactory;

    @BeforeAll
    public static void setUp() {
        jsonBuilderFactory = Json.createBuilderFactory(Collections.emptyMap());
    }

    @Test
    public void resolveSchema_returnsSchema() {
        SchemaCatalog catalog = new SchemaCatalog();
        JsonSchema schema = createSchema(URI.create("http://example.com/root.json#"));
        catalog.addSchema(schema);

        JsonSchema actual = catalog.resolveSchema(URI.create("http://example.com/root.json"));
        assertThat(actual).isEqualTo(schema);
    }

    private static JsonSchema createSchema(URI id) {
        Map<String, Keyword> keywords = new HashMap<>();
        keywords.put("$id", new Id(id));
        return BasicSchema.newSchema(keywords, jsonBuilderFactory);
    }
}
