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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaBuilder;
import org.leadpony.justify.api.JsonSchemaBuilderFactory;
import org.leadpony.justify.api.JsonValidationService;

import static org.assertj.core.api.Assertions.*;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

/**
 * @author leadpony
 */
public class JsonSchemaBuilderTest {

    private static final JsonValidationService service = JsonValidationService.newInstance();
    private JsonSchemaBuilderFactory factory;
    
    @BeforeEach
    public void setUp() {
        this.factory = service.createSchemaBuilderFactory();
    }
    
    @AfterEach
    public void tearDown() {
        this.factory = null;
    }

    @Test
    public void build_returnsNewSchema() {
        // Given
        JsonSchemaBuilder sut = this.factory.createBuilder();
        sut.withTitle("Person")
           .withType(InstanceType.OBJECT)
           .withProperty("firstName", 
                   factory.createBuilder().withType(InstanceType.STRING).build())
           .withProperty("lastName", 
                   factory.createBuilder().withType(InstanceType.STRING).build())
           .withProperty("age", 
                   factory.createBuilder()
                       .withDescription("Age in years")
                       .withType(InstanceType.INTEGER)
                       .withMinimum(0)
                       .build())
           .withRequired("firstName", "lastName")
           ;
        // When
        JsonSchema schema = sut.build();
        JsonObject expected = buildExpectedObject();
        // Then
        assertThat(schema).isNotNull();
        assertThat(schema.toJson()).isEqualTo(expected);
    }
    
    private static JsonObject buildExpectedObject() {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
        return factory.createObjectBuilder()
            .add("title", "Person")
            .add("type", "object")
            .add("properties", factory.createObjectBuilder()
                    .add("firstName", factory.createObjectBuilder()
                            .add("type", "string")
                            .build())
                    .add("lastName", factory.createObjectBuilder()
                            .add("type", "string")
                            .build())
                    .add("age", factory.createObjectBuilder()
                            .add("description", "Age in years")
                            .add("type", "integer")
                            .add("minimum", 0)
                            .build())
                    .build())
            .add("required", factory.createArrayBuilder()
                    .add("firstName")
                    .add("lastName")
                    .build())
            .build();
    }
}
