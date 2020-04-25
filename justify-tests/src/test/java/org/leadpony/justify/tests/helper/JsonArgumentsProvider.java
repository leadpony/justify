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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;

/**
 * @author leadpony
 */
public class JsonArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<JsonSource> {

    private String[] names;

    @Override
    public void accept(JsonSource annotation) {
        this.names = annotation.value();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        Parameter[] params = testMethod.getParameters();
        Class<?> targetType = params[0].getType();
        if (JsonValue.class.isAssignableFrom(targetType)) {
            return provideJsonValueArguments(testClass);
        } else {
            return providePojoArguments(testClass, targetType);
        }
    }

    private Stream<? extends Arguments> provideJsonValueArguments(Class<?> testClass) {
        return Stream.of(names)
            .flatMap(name -> loadJsonArray(name, testClass).stream())
            .map(Arguments::arguments);
    }

    private Stream<? extends Arguments> providePojoArguments(Class<?> testClass, Class<?> targetType) {
        Jsonb jsonb = JsonbBuilder.create();
        return Stream.of(names)
                .flatMap(name -> {
                    InputStream in = testClass.getResourceAsStream(name);
                    List<?> objects = jsonb.fromJson(in, new ListType(targetType));
                    return objects.stream();
                })
                .map(Arguments::arguments);
    }

    private static JsonArray loadJsonArray(String name, Class<?> testClass) {
        InputStream in = testClass.getResourceAsStream(name);
        if (in == null) {
            throw new JUnitException(name);
        }
        try (JsonReader reader = Json.createReader(in)) {
            return reader.readArray();
        }
    }

    /**
     * @author leadpony
     */
    static class ListType implements ParameterizedType {

        private final Type elementType;

        ListType(Type elementType) {
            this.elementType = elementType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] {elementType};
        }

        @Override
        public Type getRawType() {
            return ArrayList.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
