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

import org.junit.Before;
import org.junit.Test;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.JsonSchema;
import org.leadpony.justify.core.JsonSchemaBuilder;
import org.leadpony.justify.core.JsonSchemaBuilderFactory;

import static org.assertj.core.api.Assertions.*;

/**
 * @author leadpony
 */
public class JsonSchemaBuilderTest {

    private JsonSchemaBuilderFactory factory;
    
    @Before
    public void setUp() {
        this.factory = Jsonv.createSchemaBuilder();
    }

    @Test
    public void build_shouldReturnNewSchema() {
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
        // Then
        assertThat(schema).isNotNull();
    }
}
