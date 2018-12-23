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

package org.leadpony.justify.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.StringReader;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.json.stream.JsonParsingException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.leadpony.justify.api.JsonSchemaReader;
import org.leadpony.justify.api.JsonValidatingException;
import org.leadpony.justify.api.JsonValidationService;

/**
 * @author leadpony
 */
public class SchemaReaderTest {
  
    private static final Logger log = Logger.getLogger(SchemaReaderTest.class.getName());
    private static final JsonValidationService service = JsonValidationService.newInstance();
    
    public static Stream<Arguments> provideSchemas() {
        return Stream.of(
                Arguments.of("", JsonValidatingException.class),
                Arguments.of(" ", JsonValidatingException.class),
                Arguments.of(" {}", null),
                Arguments.of("{\"type\":", JsonParsingException.class),
                Arguments.of("{\"type\":\"number\"},", null)
                );
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("provideSchemas")
    public void testInvalidSchema(String schemaJson, Class<?> exceptionClass) {
        JsonSchemaReader reader = service.createSchemaReader(new StringReader(schemaJson));
        Throwable thrown = catchThrowable(()->reader.read());
        if (exceptionClass != null) {
            assertThat(thrown).isInstanceOf(exceptionClass);
            log.info(thrown.toString());
        }  else {
            assertThat(thrown).isNull();
        }
    }
}
