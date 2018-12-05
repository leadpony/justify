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

package org.leadpony.justify.api;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;

/**
 * @author leadpony
 */
public class JsonSchemaTest {

    @Test
    public void empty_shouldReturnEmptySchema() {
        JsonSchema schema = JsonSchema.EMPTY;
        
        assertThat(schema).hasToString("{}");
    }

    @Test
    public void valueOf_shouldReturnTrueBooleanSchema() {
        JsonSchema schema = JsonSchema.TRUE;
        
        assertThat(schema).hasToString("true");
    }

    @Test
    public void valueOf_shouldReturnFalseBooleanSchema() {
        JsonSchema schema = JsonSchema.FALSE;
        
        assertThat(schema).hasToString("false");
    }
}
