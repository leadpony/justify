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
package org.leadpony.justify.api;

import static org.assertj.core.api.Assertions.assertThat;
import javax.json.JsonValue.ValueType;
import org.junit.jupiter.api.Test;

/**
 * A test type for {@link JsonSchema#EMPTY}.
 *
 * @author leadpony
 */
public class EmptyJsonSchemaTest {

    private final ObjectJsonSchema schema = JsonSchema.EMPTY;

    @Test
    public void isBooleanShouldReturnFalse() {
        assertThat(schema.isBoolean()).isFalse();
    }

    @Test
    public void asObjectJsonSchemaShouldReturnSelf() {
        ObjectJsonSchema actual = schema.asObjectJsonSchema();
        assertThat(actual).isSameAs(schema);
    }

    @Test
    public void getJsonValueTypeShouldReturnObject() {
        assertThat(schema.getJsonValueType()).isEqualTo(ValueType.OBJECT);
    }

    @Test
    public void sizeShouldReturnZero() {
        assertThat(schema.size()).isEqualTo(0);
    }

    @Test
    public void isEmptyShouldReturnTrue() {
        assertThat(schema.isEmpty()).isTrue();
    }

    @Test
    public void keySetShouldReturnEmptySet() {
        assertThat(schema.keySet()).isEmpty();
    }

    @Test
    public void valuesShouldReturnEmptyCollection() {
        assertThat(schema.values()).isEmpty();
    }

    @Test
    public void entrySetShouldReturnEmptySet() {
        assertThat(schema.entrySet()).isEmpty();
    }

    @Test
    public void toStringShouldReturnEmptyObject() {
        assertThat(schema.toString()).isEqualTo("{}");
    }
}
