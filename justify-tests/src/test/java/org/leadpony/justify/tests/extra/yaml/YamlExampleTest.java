/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.tests.extra.yaml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.tests.extra.json.JsonExampleTest;
import org.leadpony.justify.tests.helper.JsonExample;
import org.leadpony.justify.tests.helper.ValidationServiceType;

/**
 * @author leadpony
 */
public class YamlExampleTest extends JsonExampleTest {

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateYamlAgainstJsonSchemaUsingJsonParser(JsonExample example) {
        validateUsingJsonParser(example.getYamlAsStream(), example.getJsonSchemaAsStream(), example.isValid());
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateYamlAgainstJsonSchemaUsingJsonReader(JsonExample example) {
        validateUsingJsonReader(example.getYamlAsStream(), example.getJsonSchemaAsStream(), example.isValid());
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateJsonAgainstYamlSchemaUsingJsonParser(JsonExample example) {
        validateUsingJsonParser(example.getJsonAsStream(), example.getYamlSchemaAsStream(), example.isValid());
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateJsonAgainstYamlSchemaUsingJsonReader(JsonExample example) {
        validateUsingJsonReader(example.getJsonAsStream(), example.getYamlSchemaAsStream(), example.isValid());
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateYamlAgainstYamlSchemaUsingJsonParser(JsonExample example) {
        validateUsingJsonParser(example.getYamlAsStream(), example.getYamlSchemaAsStream(), example.isValid());
    }

    @ParameterizedTest()
    @EnumSource(JsonExample.class)
    public void validateYamlAgainstYamlSchemaUsingJsonReader(JsonExample example) {
        validateUsingJsonReader(example.getYamlAsStream(), example.getYamlSchemaAsStream(), example.isValid());
    }

    @Override
    protected JsonValidationService createService() {
        return ValidationServiceType.YAML.getService();
    }
}
