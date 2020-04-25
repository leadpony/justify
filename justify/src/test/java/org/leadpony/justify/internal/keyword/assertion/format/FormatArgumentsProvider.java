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
package org.leadpony.justify.internal.keyword.assertion.format;

import java.io.InputStream;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * @author leadpony
 */
public class FormatArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<FormatSource> {

    private String[] names;

    @Override
    public void accept(FormatSource annotation) {
        this.names = annotation.value();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        return Stream.of(names).flatMap(name -> loadJson(name, testClass));
    }

    private Stream<Arguments> loadJson(String name, Class<?> from) {
        InputStream in = from.getResourceAsStream(name);
        try (JsonReader reader = Json.createReader(in)) {
            JsonArray array = reader.readArray();
            return array.stream()
                    .map(JsonValue::asJsonObject)
                    .map(object -> toArguments(object));
        }
    }

    private Arguments toArguments(JsonObject object) {
        return Arguments.of(
                object.getString("value"),
                object.getBoolean("valid"));
    }
}
