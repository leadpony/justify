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
package org.leadpony.justify.tests.helper;

import java.util.ServiceLoader;
import java.util.function.Predicate;

import org.leadpony.justify.api.JsonValidationService;
import jakarta.json.spi.JsonProvider;

/**
 * @author leadpony
 */
public enum ValidationServiceType {
    DEFAULT(createJsonProvider()),
    YAML(createYamlProvider());

    private static final String YAML_PROVIDER = "org.leadpony.joy.yaml.YamlProvider";

    private final JsonProvider jsonProvider;
    private final JsonValidationService service;

    ValidationServiceType(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
        this.service = JsonValidationService.newInstance(jsonProvider);
    }

    public JsonProvider getJsonProvider() {
        return jsonProvider;
    }

    public JsonValidationService getService() {
        return service;
    }

    private static JsonProvider createJsonProvider() {
        return findJsonProvider(provider -> !supportsYaml(provider));
    }

    private static JsonProvider createYamlProvider() {
        return findJsonProvider(ValidationServiceType::supportsYaml);
    }

    private static JsonProvider findJsonProvider(Predicate<ServiceLoader.Provider<JsonProvider>> predicate) {
        ServiceLoader<JsonProvider> loader = ServiceLoader.load(JsonProvider.class);
        return loader.stream()
                .filter(predicate)
                .findFirst()
                .map(ServiceLoader.Provider::get)
                .orElseThrow();
    }

    private static boolean supportsYaml(ServiceLoader.Provider<JsonProvider> provider) {
        return provider.type().getName().equals(YAML_PROVIDER);
    }
}
