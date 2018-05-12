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

import static org.assertj.core.api.Assertions.*;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.json.JsonObject;
import javax.json.stream.JsonParser;

import org.junit.Test;

/**
 * @author leadpony
 */
public abstract class AbstractSpecTest {

    protected final String name;
    protected final int testIndex;
    protected final String description;
    protected final Fixture fixture;

    private static JsonObject lastObject;
    private static JsonSchema lastSchema;
    
    protected AbstractSpecTest(String name, int testIndex, String description, Fixture fixture) {
        this.name = name;
        this.testIndex = testIndex;
        this.description = description;
        this.fixture = fixture;
    }
    
    @Test
    public void testValidationResult() {
        JsonParser parser = createValidator();
        while (parser.hasNext()) {
            parser.next();
        }
        parser.close();
        ValidationResult result = (ValidationResult)parser;
        assertThat(result.wasValid()).isEqualTo(fixture.result());
        if (!result.wasValid()) {
            printProblems(result);
        }
    }
    
    protected JsonParser createValidator() {
        JsonSchema schema = loadSchema(fixture.schema());
        JsonValidatorFactory factory = JsonValidatorFactory.newFactory(schema);
        StringReader instanceReader = new StringReader(fixture.instance().toString());
        return factory.createParser(instanceReader);
    }
    
    private JsonSchema loadSchema(JsonObject object) {
        if (object == lastObject) {
            return lastSchema;
        }
        StringReader reader = new StringReader(object.toString());
        // caches for future use.
        lastObject = object;
        lastSchema = JsonSchema.load(reader);
        return lastSchema;
    }
    
    protected void printProblems(ValidationResult result) {
        result.problems().forEach(System.err::println);
    }
    
    protected static Iterable<Object[]> parameters(String[] names) {
        return ()->new ParameterSetIterator(names);
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
                    fixture.instanceDescription(),
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
