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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonReaderFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A test class for {@link JsonValidatorFactoryBuilder}.
 *
 * @author leadpony
 */
public class JsonValidatorFactoryBuilderTest {

    private static final JsonValidationService service = JsonValidationServices.get();

    private JsonValidatorFactoryBuilder sut;

    @BeforeEach
    public void setUp() {
        sut = service.createValidatorFactoryBuilder(JsonSchema.FALSE);
    }

    @Test
    public void getAsMap_returnsEmptyMapByDefault() {
        assertThat(sut.getAsMap()).isEmpty();
    }

    @Test
    public void getAsMap_returnsFilledMap() {
        Map<String, Object> expected = new HashMap<>();
        expected.put(JsonValidatorFactoryBuilder.DEFAULT_VALUES, Boolean.TRUE);

        sut.withDefaultValues(true);
        assertThat(sut.getAsMap()).containsExactlyEntriesOf(expected);
    }

    @Test
    public void getProperty_returnsEmptyIfNotExist() {
        assertThat(sut.getProperty("nonexistent")).isEmpty();
    }

    @Test
    public void getProperty_returnsValueIfExists() {
        sut.withDefaultValues(true);

        assertThat(sut.getProperty(JsonValidatorFactoryBuilder.DEFAULT_VALUES))
            .contains(Boolean.TRUE);
    }

    @Test
    public void setProperty_assignsPropertyValue() {
        sut.setProperty("foo", true);

        assertThat(sut.getProperty("foo")).contains(true);
    }

    @Test
    public void withProperties_assignsMultipleProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("foo", true);
        properties.put("bar", false);

        Map<String, Object> expected = new HashMap<>();
        expected.put("foo", true);
        expected.put("bar", false);
        expected.put(JsonValidatorFactoryBuilder.DEFAULT_VALUES, true);

        sut.withDefaultValues(true).withProperties(properties);

        assertThat(sut.getAsMap()).containsExactlyEntriesOf(expected);
    }

    @Test
    public void withProblemHandler_assignsProblemHandler() {
        List<Problem> problems = new ArrayList<>();
        sut.withProblemHandler(problems::addAll);

        JsonParserFactory factory = sut.buildParserFactory();
        JsonParser parser = factory.createParser(new StringReader("{}"));
        while (parser.hasNext()) {
            parser.next();
        }

        assertThat(problems).hasSize(1);
    }

    @Test
    public void withProblemHandlerFactory_assignsProblemHandlerFactory() {
        ProblemHandlerFactoryMock handlerFactory = new ProblemHandlerFactoryMock();
        sut.withProblemHandlerFactory(handlerFactory);

        JsonParserFactory factory = sut.buildParserFactory();
        JsonParser parser = factory.createParser(new StringReader("{}"));
        while (parser.hasNext()) {
            parser.next();
        }

        assertThat(handlerFactory.created).isEqualTo(1);
        assertThat(handlerFactory.problems).hasSize(1);
    }

    @Test
    public void buildParserFactory_returnsParserFactory() {
        JsonParserFactory factory = sut.buildParserFactory();
        assertThat(factory).isNotNull();
    }

    @Test
    public void buildReaderFactory_returnsParserFactory() {
        JsonReaderFactory factory = sut.buildReaderFactory();
        assertThat(factory).isNotNull();
    }

    private static class ProblemHandlerFactoryMock implements ProblemHandlerFactory {

        int created;
        List<Problem> problems = new ArrayList<>();

        @Override
        public ProblemHandler createProblemHandler(JsonParser parser) {
            created++;
            return problems::addAll;
        }
    }
}
