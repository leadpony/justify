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

package org.leadpony.justify.core;

import org.junit.Test;
import org.leadpony.justify.core.JsonSchema;

import static org.assertj.core.api.Assertions.*;
import static org.leadpony.justify.core.Resources.newInputStream;
import static org.leadpony.justify.core.Resources.newReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author leadpony
 */
public class JsonSchemaTest {

    @Test
    public void empty_shouldReturnEmptySchema() {
        JsonSchema schema = JsonSchema.empty();
        
        assertThat(schema).hasToString("{}");
    }

    @Test
    public void valueOf_shouldReturnTrueBooleanSchema() {
        JsonSchema schema = JsonSchema.valueOf(true);
        
        assertThat(schema).hasToString("true");
    }

    @Test
    public void valueOf_shouldReturnFalseBooleanSchema() {
        JsonSchema schema = JsonSchema.valueOf(false);
        
        assertThat(schema).hasToString("false");
    }

    @Test
    public void load_shouldLoadJsonSchemaFromStream() {
        JsonSchema schema = null;
        try (InputStream in = newInputStream("/example/person/schema.json")) {
            schema = JsonSchema.load(in);
        } catch (IOException e) {
        }
        
        assertThat(schema).isNotNull();
    }
    
    @Test
    public void load_shouldLoadJsonSchemaFromReader() {
        JsonSchema schema = null;
        try (Reader reader = newReader("/example/person/schema.json")) {
            schema = JsonSchema.load(reader);
        } catch (IOException e) {
        }
        
        assertThat(schema).isNotNull();
    }
}
