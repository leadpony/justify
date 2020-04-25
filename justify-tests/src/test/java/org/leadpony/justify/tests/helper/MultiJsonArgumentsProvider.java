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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;

/**
 * An {@link ArgumentsProvider} for {@link MultiJsonSource}.
 *
 * @author leadpony
 */
public class MultiJsonArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<MultiJsonSource> {

    private String[] names;

    @Override
    public void accept(MultiJsonSource annotation) {
        this.names = annotation.value();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        Parameter[] params = testMethod.getParameters();
        return Stream.of(names).flatMap(name -> {
            try (DocumentReader reader = createReader(name, testClass)) {
                return readDocuments(name, reader, params);
            } catch (IOException e) {
                throw new JUnitException(name, e);
            }
        });
    }

    private static DocumentReader createReader(String name, Class<?> testClass) {
        InputStream in = testClass.getResourceAsStream(name);
        if (in == null) {
            throw new JUnitException(name);
        }
        return new DocumentReader(in);
    }

    private Stream<Arguments> readDocuments(String resourceName, DocumentReader reader, Parameter[] params)
            throws IOException {
        List<Arguments> arguments = new ArrayList<>();
        int index = 0;
        while (!reader.isFinished()) {
            Object[] objects = new Object[params.length];
            objects[0] = buildDisplayName(resourceName, index++);
            for (int i = 1; i < params.length; i++) {
                String doc = reader.readDocument();
                objects[i] = convertDocument(doc, params[i].getType());
            }
            arguments.add(Arguments.of(objects));
        }
        return arguments.stream();
    }

    private static String buildDisplayName(String resourceName, int index) {
        int startIndex = resourceName.lastIndexOf('/') + 1;
        int endIndex = resourceName.lastIndexOf('.');
        String baseName  = resourceName.substring(startIndex, endIndex);
        return new StringBuilder(baseName)
                .append('[').append(index).append(']')
                .toString();
    }

    private Object convertDocument(String doc, Class<?> target) {
        if (target == String.class) {
            return doc;
        } else if (target == JsonArray.class) {
            return toJsonArray(doc);
        } else if (target == JsonObject.class) {
            return toJsonObject(doc);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private JsonArray toJsonArray(String string) {
        try (JsonReader reader = createJsonReader(string)) {
            return reader.readArray();
        }
    }

    private JsonObject toJsonObject(String string) {
        try (JsonReader reader = createJsonReader(string)) {
            return reader.readObject();
        }
    }

    private JsonReader createJsonReader(String string) {
        return Json.createReader(new StringReader(string));
    }

    /**
     * @author leadpony
     */
    private static class DocumentReader implements Closeable {

        private static final Pattern SEPARATOR = Pattern.compile("---\\s*");

        private final BufferedReader reader;
        private boolean eof;

        private static final Charset CAHRSET = StandardCharsets.UTF_8;

        DocumentReader(InputStream in) {
            this.reader = new BufferedReader(new InputStreamReader(in, CAHRSET));
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        String readDocument() throws IOException {
            StringBuilder builder = new StringBuilder();
            for (;;) {
                String line = reader.readLine();
                if (line == null) {
                    eof = true;
                    break;
                } else if (SEPARATOR.matcher(line).matches()) {
                    break;
                }
                builder.append(line).append('\n');
            }
            return builder.toString();
        }

        boolean isFinished() {
            return eof;
        }
    }
}
