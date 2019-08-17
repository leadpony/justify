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
package org.leadpony.justify.tests.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.api.ProblemHandlerFactory;
import org.leadpony.justify.api.ValidationConfig;

/**
 * A test class for {@link ValidationConfig}.
 *
 * @author leadpony
 */
public class ValidationConfigTest extends BaseTest {

    private ValidationConfig sut;

    @BeforeEach
    public void setUp() {
        sut = SERVICE.createValidationConfig();
    }

    @Test
    public void getAsMapShouldReturnEmptyMapByDefault() {
        assertThat(sut.getAsMap()).isEmpty();
    }

    @Test
    public void getAsMapShouldReturnFilledMap() {
        Map<String, Object> expected = new HashMap<>();
        expected.put(ValidationConfig.DEFAULT_VALUES, Boolean.TRUE);

        sut.withDefaultValues(true);
        assertThat(sut.getAsMap()).containsExactlyEntriesOf(expected);
    }

    @Test
    public void getPropertyShouldReturnEmptyIfNotExist() {
        assertThat(sut.getProperty("nonexistent")).isEmpty();
    }

    @Test
    public void getPropertyShouldReturnValueIfExists() {
        sut.withDefaultValues(true);

        assertThat(sut.getProperty(ValidationConfig.DEFAULT_VALUES))
            .contains(Boolean.TRUE);
    }

    @Test
    public void setPropertyShouldAssignPropertyValue() {
        sut.setProperty("foo", true);

        assertThat(sut.getProperty("foo")).contains(true);
    }

    @Test
    public void withPropertiesShouldAssignMultipleProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("foo", true);
        properties.put("bar", false);

        Map<String, Object> expected = new HashMap<>();
        expected.put("foo", true);
        expected.put("bar", false);
        expected.put(ValidationConfig.DEFAULT_VALUES, true);

        sut.withDefaultValues(true).withProperties(properties);

        assertThat(sut.getAsMap()).isEqualTo(expected);
    }

    @Test
    public void withProblemHandlerShouldAssignProblemHandler() {
        List<Problem> problems = new ArrayList<>();

        sut.withSchema(JsonSchema.FALSE);
        sut.withProblemHandler(problems::addAll);

        JsonParserFactory factory = SERVICE.createParserFactory(sut.getAsMap());
        JsonParser parser = factory.createParser(new StringReader("{}"));
        while (parser.hasNext()) {
            parser.next();
        }

        assertThat(problems).hasSize(1);
    }

    @Test
    public void withProblemHandlerFactoryShouldAssignProblemHandlerFactory() {
        ProblemHandlerFactoryMock handlerFactory = new ProblemHandlerFactoryMock();

        sut.withSchema(JsonSchema.FALSE);
        sut.withProblemHandlerFactory(handlerFactory);

        JsonParserFactory factory = SERVICE.createParserFactory(sut.getAsMap());
        JsonParser parser = factory.createParser(new StringReader("{}"));
        while (parser.hasNext()) {
            parser.next();
        }

        assertThat(handlerFactory.created).isEqualTo(1);
        assertThat(handlerFactory.problems).hasSize(1);
    }

    /**
     * A mock class of {@link ProblemHandlerFactory}.
     *
     * @author leadpony
     */
    private static class ProblemHandlerFactoryMock implements ProblemHandlerFactory {

        private int created;
        private List<Problem> problems = new ArrayList<>();

        @Override
        public ProblemHandler createProblemHandler(JsonParser parser) {
            created++;
            return problems::addAll;
        }
    }
}
