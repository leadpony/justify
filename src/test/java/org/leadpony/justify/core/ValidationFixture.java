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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.json.JsonValue;

/**
 * Test fixture.
 * 
 * @author leadpony
 */
class ValidationFixture extends Fixture {
    
    private final JsonValue schema;
    private final String schemaDescription; 
    private final JsonValue data;
    private final String dataDescription;
    private boolean valid;
    
    private ValidationFixture(String name, int index, JsonValue schema, String schemaDescription, JsonValue data, String dataDescription, boolean valid) {
        super(name, index);
        this.schema = schema;
        this.schemaDescription = schemaDescription;
        this.data = data;
        this.dataDescription = dataDescription;
        this.valid = valid;
    }
    
    @Override
    String description() {
        return dataDescription();
    }

    JsonValue schema() {
        return schema;
    }

    String schemaDescription() {
        return schemaDescription;
    }

    JsonValue data() {
        return data;
    }

    String dataDescription() {
        return dataDescription;
    }

    boolean isValid() {
        return valid;
    }
    
    static Stream<ValidationFixture> newStream(String name) {
        AtomicInteger counter = new AtomicInteger();
        return TestResources.readJsonArray(name).stream()
                .map(JsonValue::asJsonObject)
                .flatMap(schema->{
                    return schema.getJsonArray("tests").stream()
                        .map(JsonValue::asJsonObject)
                        .map(test->new ValidationFixture(
                                name,
                                counter.getAndIncrement(),
                                schema.getValue("/schema"),
                                schema.getString("description"),
                                test.get("data"),
                                test.getString("description"),
                                test.getBoolean("valid")
                                ));
                });
    }
}