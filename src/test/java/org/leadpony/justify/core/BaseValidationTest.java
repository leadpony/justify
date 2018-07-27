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

import org.junit.Test;
import org.leadpony.justify.Loggers;

/**
 * @author leadpony
 */
public abstract class BaseValidationTest {
    
    private static final Logger log = Loggers.getLogger(BaseValidationTest.class);
    
    private final ValidationFixture fixture;

    private static JsonValue lastValue;
    private static JsonSchema lastSchema;
    
    protected BaseValidationTest(String name, String description, ValidationFixture fixture) {
        this.fixture = fixture;
    }
    
    public ValidationFixture getFixture() {
        return fixture;
    }
    
    @Test
    public void testValidationResult() {
        List<Problem> problems = new ArrayList<>();
        JsonParser parser = createValidatingParser(Jsonv.createProblemCollector(problems));
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();
        assertThat(problems.isEmpty()).isEqualTo(getFixture().isValid());
        printProblems(problems);
    }
    
    protected static Iterable<Object[]> fixtures(String[] names) {
        Stream<ValidationFixture> stream = Stream.of(names).flatMap(ValidationFixture::newStream);
        return ()->stream.map(Fixture::toArguments).iterator();
    }
    
    protected JsonParser createValidatingParser(Consumer<List<Problem>> handler) {
        JsonSchema schema = getSchema();
        StringReader reader = new StringReader(fixture.data().toString());
        return Jsonv.createParser(reader, schema, handler);
    }
    
    private JsonSchema getSchema() {
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

    protected void printProblems(List<Problem> problems) {
        if (problems.isEmpty()) {
            return;
        }
        log.info(getFixture().displayName() + ": Validation found the following problem(s).");
        Jsonv.createProblemPrinter(log::info).accept(problems);
    }
}
