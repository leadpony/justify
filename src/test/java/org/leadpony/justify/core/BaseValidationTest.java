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
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.Loggers;

/**
 * @author leadpony
 */
public abstract class BaseValidationTest {
    
    private static final Logger log = Loggers.getLogger(BaseValidationTest.class);
    
    private static JsonValue lastValue;
    private static JsonSchema lastSchema;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideFixtures")
    public void testValidationResult(String displayName, ValidationFixture fixture) {
        List<Problem> problems = new ArrayList<>();
        JsonParser parser = createValidatingParser(fixture, Jsonv.createProblemCollector(problems));
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();
        assertThat(problems.isEmpty()).isEqualTo(fixture.getDataValidity());
        printProblems(fixture, problems);
    }
    
    protected static Stream<Arguments> fixtures(String[] names) {
        return Stream.of(names)
                .flatMap(ValidationFixture::newStream)
                .map(Fixture::toArguments);
    }
    
    protected JsonParser createValidatingParser(ValidationFixture fixture, Consumer<List<Problem>> handler) {
        JsonSchema schema = getSchema(fixture);
        StringReader reader = new StringReader(fixture.data().toString());
        return Jsonv.createParser(reader, schema, handler);
    }
    
    private JsonSchema getSchema(ValidationFixture fixture) {
        JsonValue value = fixture.schema();
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
  
    protected JsonSchemaReader createSchemaReader(Reader reader) {
        return Jsonv.createSchemaReader(reader);
    }

    protected void printProblems(ValidationFixture fixture, List<Problem> problems) {
        if (problems.isEmpty()) {
            return;
        }
        log.info(fixture.displayName());
        Jsonv.createProblemPrinter(log::info).accept(problems);
    }
}
