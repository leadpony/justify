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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Base type of validation test.
 * 
 * @author leadpony
 */
public abstract class BaseValidationTest {
    
    private static final Logger log = Logger.getLogger(BaseValidationTest.class.getName());
    
    public static final Jsonv jsonv = Jsonv.newInstance();
    private static final ProblemHandler printer = jsonv.createProblemPrinter(log::info);

    private static JsonValue lastValue;
    private static JsonSchema lastSchema;
    
    private List<Problem> problems;
    
    @BeforeEach
    public void setUp() {
        problems = new ArrayList<>();
    }
    
    @ParameterizedTest
    @MethodSource("provideFixtures")
    public void testValidationWithSchema(ValidationFixture fixture) {
        Assumptions.assumeTrue(fixture.index() >= 0);
        JsonSchema schema = getSchema(fixture.schema());
        JsonValue data = fixture.data();
        JsonParser parser = createValidatingParser(data, schema);
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();
        assertThat(problems.isEmpty()).isEqualTo(fixture.getDataValidity());
        for (Problem problem : problems) {
            assertThat(problem.getSchema()).isNotNull();
        }
        printProblems(fixture, problems);
    }
    
    @ParameterizedTest
    @MethodSource("provideFixtures")
    public void testValidationWithNegatedSchema(ValidationFixture fixture) {
        Assumptions.assumeTrue(fixture.index() >= 0);
        JsonSchema schema = negate(getSchema(fixture.schema()));
        JsonValue data = fixture.data();
        JsonParser parser = createValidatingParser(data, schema);
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();
        assertThat(problems.isEmpty()).isEqualTo(!fixture.getDataValidity());
        for (Problem problem : problems) {
            assertThat(problem.getSchema()).isNotNull();
        }
        printProblems(fixture, problems);
    }

    @Disabled
    public void disabledTest() {
    }

    private JsonSchema getSchema(JsonValue value) {
        if (value == lastValue) {
            return lastSchema;
        } else {
            JsonSchema schema = readSchema(value.toString());
            lastValue = value;
            lastSchema = schema;
            return schema;
        }
    }
    
    private JsonSchema readSchema(String value) {
        StringReader reader = new StringReader(value);
        try (JsonSchemaReader schemaReader = createSchemaReader(reader)) {
            return schemaReader.read();
        } catch (JsonValidatingException e) {
            throw e;
        }
    }
  
    private JsonParser createValidatingParser(JsonValue data, JsonSchema schema) {
        StringReader reader = new StringReader(data.toString());
        return jsonv.createParser(reader, schema, problems::addAll);
    }
    
    private static JsonSchema negate(JsonSchema original) {
        JsonSchemaBuilderFactory schemaBuilderFactory = jsonv.createSchemaBuilderFactory();
        return schemaBuilderFactory.createBuilder()
                .withNot(original)
                .build();
    }
    
    protected JsonSchemaReader createSchemaReader(Reader reader) {
        return jsonv.createSchemaReader(reader);
    }

    protected void printProblems(Fixture fixture, List<Problem> problems) {
        if (problems.isEmpty() || !log.isLoggable(Level.INFO)) {
            return;
        }
        log.info("\n# " + fixture.displayName());
        printer.handleProblems(problems);
    }
}
