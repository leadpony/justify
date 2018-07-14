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

package org.leadpony.justify.internal.base;

import static org.assertj.core.api.Assertions.*;

import java.io.StringReader;
import java.util.Locale;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.leadpony.justify.core.InstanceType;
import org.leadpony.justify.core.Problem;

/**
 * Test cases for {@link ProblemBuilder}.
 * 
 * @author leadpony
 */
public class ProblemBuilderTest {

    private JsonParser parser;
    
    @Before
    public void setUp() {
        StringReader reader = new StringReader("{}");
        this.parser = Json.createParser(reader);
    }
    
    @After
    public void tearDown() {
        if (parser != null) {
            parser.close();
            parser = null;
        }
    }
    
    @Test
    public void build_shouldBuildProblem() {
        ProblemBuilder builder = ProblemBuilder.newBuilder(this.parser);
        Problem problem = builder
                .withMessage("instance.problem.type")
                .withParameter("actual", InstanceType.STRING)
                .withParameter("expected", InstanceType.INTEGER)
                .build();
        
        String expectedMessage = "Type of value must be integer, but actual type was string.";
        assertThat(problem.getMessage(Locale.ENGLISH)).isEqualTo(expectedMessage);
        assertThat(problem.parametersAsMap())
            .hasSize(2)
            .containsKeys("actual", "expected")
            ;
    }
}
