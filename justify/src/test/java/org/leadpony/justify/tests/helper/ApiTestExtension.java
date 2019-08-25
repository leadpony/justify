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
package org.leadpony.justify.tests.helper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;

/**
 * @author leadpony
 */
public class ApiTestExtension implements BeforeAllCallback {

    private static JsonValidationService service;

    public ApiTestExtension() {
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        JsonValidationService service = getService();
        Logger log = Logger.getLogger(testClass.getName());
        Class<?> targetType = testClass;
        while (targetType != Object.class) {
            for (Field field : targetType.getDeclaredFields()) {
                Class<?> fieldType = field.getType();
                if (fieldType.isAssignableFrom(Logger.class)) {
                    assignFieldValue(field, log);
                } else if (fieldType.isAssignableFrom(JsonValidationService.class)) {
                    assignFieldValue(field, service);
                } else if (fieldType.isAssignableFrom(ProblemPrinter.class)) {
                    assignFieldValue(field, new LogProblemPrinter(log));
                }
            }
            targetType = targetType.getSuperclass();
        }
    }

    private static JsonValidationService getService() {
        if (service == null) {
            service = JsonValidationService.newInstance();
        }
        return service;
    }

    private static void assignFieldValue(Field field, Object value)
            throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        field.set(null, value);
    }

    /**
     * @author leadpony
     */
    private static final class LogProblemPrinter implements ProblemPrinter {

        private final Logger log;

        private LogProblemPrinter(Logger log) {
            this.log = log;
        }

        @Override
        public void print(List<Problem> problems) {
            for (Problem problem : problems) {
                problem.print(log::info);
            }
        }
    }
}
