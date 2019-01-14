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

package org.leadpony.justify.api;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

/**
 * Utility class operating on test resources.
 * 
 * @author leadpony
 */
class TestResources {

    public static InputStream newInputStream(String name) {
        return TestResources.class.getResourceAsStream(name);
    }
    
    public static JsonArray readJsonArray(String name) {
        try (JsonReader reader = Json.createReader(newInputStream(name))) {
            return reader.readArray();
        }
    }

    public static Path pathToResource(String name) {
        return Paths.get("target/test-classes", name.substring(1));
    }
}
