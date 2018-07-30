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
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.StringReader;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.json.stream.JsonParsingException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.Loggers;

/**
 * @author leadpony
 */
public class SchemaReaderTest {
  
    private static final Logger log = Loggers.getLogger(SchemaReaderTest.class);
    
    public static Stream<String> provideSchemas() {
        return Stream.of(
                "",
                "{\"type\":"
                );
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("provideSchemas")
    public void testInvalidSchema(String schemaJson) {
        JsonSchemaReader reader = Jsonv.createSchemaReader(new StringReader(schemaJson));
        Throwable thrown = catchThrowable(()->reader.read());
        assertThat(thrown)
            .isInstanceOfAny(NoSuchElementException.class, JsonParsingException.class);
        log.info(thrown.toString());
    }
}
