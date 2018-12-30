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
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * @author leadpony
 */
public class JsonSchemaBuilderTest {

    private static final JsonValidationService service = JsonValidationService.newInstance();
    private JsonSchemaBuilderFactory schemaBuilderfactory;
    private JsonBuilderFactory jsonBuilderFactory;

    @BeforeEach
    public void setUp() {
        this.schemaBuilderfactory = service.createSchemaBuilderFactory();
        this.jsonBuilderFactory = Json.createBuilderFactory(null);
    }
    
    @AfterEach
    public void tearDown() {
        this.jsonBuilderFactory = null;
        this.schemaBuilderfactory = null;
    }

    @Test
    public void build_returnsNewSchema() {
        // Given
        JsonSchemaBuilder sut = createSchemaBuilder();
        sut.withTitle("Person")
           .withType(InstanceType.OBJECT)
           .withProperty("firstName", 
                   createSchemaBuilder().withType(InstanceType.STRING).build())
           .withProperty("lastName", 
                   createSchemaBuilder().withType(InstanceType.STRING).build())
           .withProperty("age", 
                   createSchemaBuilder()
                       .withDescription("Age in years")
                       .withType(InstanceType.INTEGER)
                       .withMinimum(0)
                       .build())
           .withRequired("firstName", "lastName")
           ;
        // When
        JsonSchema schema = sut.build();
        
        JsonObject expected = createObjectBuilder()
                .add("title", "Person")
                .add("type", "object")
                .add("properties", createObjectBuilder()
                        .add("firstName", createObjectBuilder()
                                .add("type", "string")
                                .build())
                        .add("lastName", createObjectBuilder()
                                .add("type", "string")
                                .build())
                        .add("age", createObjectBuilder()
                                .add("description", "Age in years")
                                .add("type", "integer")
                                .add("minimum", 0)
                                .build())
                        .build())
                .add("required", createArrayBuilder()
                        .add("firstName")
                        .add("lastName")
                        .build())
                .build();
        // Then
        assertThat(schema).isNotNull();
        assertThat(schema.toJson()).isEqualTo(expected);
    }
    
    @Test
    public void withItems_shouldAddSchemaForAllItems() {
        JsonSchemaBuilder sut = createSchemaBuilder();
        JsonSchema schema = sut.withType(InstanceType.ARRAY)
           .withItems(JsonSchema.TRUE)
           .build();
        
        JsonObject expected = createObjectBuilder()
                .add("type", "array")
                .add("items", true)
                .build();
        
        assertThat(schema.toJson()).isEqualTo(expected);
    }

    @Test
    public void withItemsArray_shouldAddArrayOfSchemas() {
        JsonSchemaBuilder sut = createSchemaBuilder();
        JsonSchema schema = sut.withType(InstanceType.ARRAY)
           .withItemsArray(JsonSchema.TRUE, JsonSchema.FALSE, JsonSchema.EMPTY)
           .build();
        
        JsonObject expected = createObjectBuilder()
                .add("type", "array")
                .add("items", createArrayBuilder()
                        .add(JsonValue.TRUE)
                        .add(JsonValue.FALSE)
                        .add(JsonValue.EMPTY_JSON_OBJECT)
                        .build())
                .build();
        
        assertThat(schema.toJson()).isEqualTo(expected);
    }
    
    @Test
    public void withItemsArray_shouldThrowIfArrayIsEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();
        Throwable thrown = catchThrowable(()->{
            sut.withType(InstanceType.ARRAY).withItemsArray();
        });
        
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withItemsArray_shouldThrowIfListIsEmpty() {
        JsonSchemaBuilder sut = createSchemaBuilder();
        Throwable thrown = catchThrowable(()->{
            sut.withType(InstanceType.ARRAY).withItemsArray(Collections.emptyList());
        });
        
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void withItemsArray_shouldAddListOfSchemas() {
        List<JsonSchema> schemas = Arrays.asList(JsonSchema.TRUE, JsonSchema.FALSE, JsonSchema.EMPTY);
        JsonSchemaBuilder sut = createSchemaBuilder();
        JsonSchema schema = sut.withType(InstanceType.ARRAY)
           .withItemsArray(schemas)
           .build();
        
        JsonObject expected = createObjectBuilder()
                .add("type", "array")
                .add("items", createArrayBuilder()
                        .add(JsonValue.TRUE)
                        .add(JsonValue.FALSE)
                        .add(JsonValue.EMPTY_JSON_OBJECT)
                        .build())
                .build();
        
        assertThat(schema.toJson()).isEqualTo(expected);
    }

    private JsonSchemaBuilder createSchemaBuilder() {
        return schemaBuilderfactory.createBuilder();
    }
    
    private JsonObjectBuilder createObjectBuilder() {
        return jsonBuilderFactory.createObjectBuilder();
    }
    
    private JsonArrayBuilder createArrayBuilder() {
        return jsonBuilderFactory.createArrayBuilder();
    }
}
