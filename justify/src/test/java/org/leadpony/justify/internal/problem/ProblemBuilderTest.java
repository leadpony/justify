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

package org.leadpony.justify.internal.problem;

import static org.assertj.core.api.Assertions.*;

import java.io.StringReader;
import java.util.Locale;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.base.Message;

/**
 * Test cases for {@link ProblemBuilder}.
 *
 * @author leadpony
 */
public class ProblemBuilderTest {

    private JsonParser parser;

    @BeforeEach
    public void setUp() {
        StringReader reader = new StringReader("{}");
        this.parser = Json.createParser(reader);
    }

    @AfterEach
    public void tearDown() {
        if (parser != null) {
            parser.close();
            parser = null;
        }
    }

    @Test
    public void build_shouldBuildProblem() {
        ProblemBuilderFactory factory = new ProblemBuilderFactory() {};
        ProblemBuilder builder = factory.createProblemBuilder(this.parser.getLocation());
        Problem problem = builder
                .withMessage(Message.INSTANCE_PROBLEM_TYPE)
                .withParameter("actual", InstanceType.STRING)
                .withParameter("expected", InstanceType.INTEGER)
                .build();

        String expectedMessage = "The value must be of integer type, but actual type is string.";
        assertThat(problem.getMessage(Locale.ROOT)).isEqualTo(expectedMessage);
        assertThat(problem.parametersAsMap())
            .hasSize(2)
            .containsKeys("actual", "expected")
            ;
    }
}
