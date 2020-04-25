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
package org.leadpony.justify.tests.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import jakarta.json.JsonValue.ValueType;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;

/**
 * A test type for {@link JsonSchema#TRUE}.
 *
 * @author leadpony
 */
public class TrueJsonSchemaTest {

    private final JsonSchema schema = JsonSchema.TRUE;

    @Test
    public void isBooleanShouldReturnTrue() {
        assertThat(schema.isBoolean()).isTrue();
    }

    @Test
    public void asObjectJsonSchemaShouldThrowClassCastException() {
        Throwable thrown = catchThrowable(() -> {
            schema.asObjectJsonSchema();
        });
        assertThat(thrown).isInstanceOf(ClassCastException.class);
    }

    @Test
    public void getJsonValueTypeShouldReturnTrue() {
        assertThat(schema.getJsonValueType()).isEqualTo(ValueType.TRUE);
    }

    @Test
    public void toStringShouldReturnEmptyObject() {
        assertThat(schema.toString()).isEqualTo("true");
    }
}
