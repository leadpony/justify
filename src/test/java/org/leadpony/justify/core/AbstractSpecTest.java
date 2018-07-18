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

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import org.leadpony.justify.Loggers;

/**
 * @author leadpony
 */
abstract class AbstractSpecTest {
    
    private static final Logger log = Loggers.getLogger(AbstractSpecTest.class);
    
    private final String name;
    private final int testIndex;
    private final String description;
    private final Fixture fixture;

    private static JsonValue lastValue;
    private static JsonSchema lastSchema;
    
    protected AbstractSpecTest(String name, int testIndex, String description, Fixture fixture) {
        this.name = name;
        this.testIndex = testIndex;
        this.description = description;
        this.fixture = fixture;
    }
    
    public String getName() {
        return name;
    }
    
    public int getTestIndex() {
        return testIndex;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Fixture getFixture() {
        return fixture;
    }
    
    protected static Iterable<Object[]> fixtures(String[] names) {
        return ()->new ParameterSetIterator(names);
    }
    
    protected JsonParser createValidatingParser(Consumer<List<Problem>> handler) {
        JsonSchema schema = getSchema();
        StringReader reader = new StringReader(fixture.instance().toString());
        return Jsonv.createParser(reader, schema, handler);
    }
    
    private JsonSchema getSchema() {
        JsonValue value = fixture.schema();
        if (value == lastValue) {
            return lastSchema;
        } else {
            JsonSchema schema = readSchema(value);
            lastValue = value;
            lastSchema = schema;
            return schema;
        }
    }
    
    private JsonSchema readSchema(JsonValue value) {
        StringReader reader = new StringReader(value.toString());
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
        String prefix = "(" + getName() + "@" + getTestIndex() + ")";
        ProblemHandlers.printingWith(line->log.info(prefix + line))
            .accept(problems);
    }
    
    private static class ParameterSetIterator implements Iterator<Object[]> {
        
        private final Iterator<String> outerIterator;
        private Iterator<Fixture> innerIterator;
        private String name;
        private int testIndex;
        
        ParameterSetIterator(String[] names) {
            this.outerIterator = Arrays.asList(names).iterator();
            this.innerIterator = nextIterator();
        }

        @Override
        public boolean hasNext() {
            while (!innerIterator.hasNext()) {
                if (!outerIterator.hasNext()) {
                    return false;
                }
                innerIterator = nextIterator();
            }
            return true;
        }

        @Override
        public Object[] next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Fixture fixture = innerIterator.next();
            return new Object[] {
                    shortNameOf(this.name),
                    this.testIndex++,
                    fixture.description(),
                    fixture
            };
        }
        
        private Iterator<Fixture> nextIterator() {
            List<Fixture> fixtures = null;
            if (outerIterator.hasNext()) {
                this.name = outerIterator.next();
                fixtures = Fixture.load(this.name);
            } else {
                fixtures =  Collections.emptyList();
            }
            this.testIndex = 0;
            return fixtures.iterator();
        }
        
        private String shortNameOf(String name) {
            int beginIndex = name.lastIndexOf('/') + 1;
            int endIndex = name.lastIndexOf('.');
            return name.substring(beginIndex, endIndex);
        }
    }
}
