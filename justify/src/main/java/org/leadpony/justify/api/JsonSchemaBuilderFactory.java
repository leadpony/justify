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

/**
 * Factory for creating {@link JsonSchemaBuilder} instances.
 * 
 * <p>
 * The following example shows how to create an instance of {@link JsonSchemaBuilder}.
 * </p>
 * <pre><code>
 * JsonValidationService service = JsonValidationService.newInstance();
 * JsonSchemaBuilderFactory factory = service.createSchemaBuilderFactory();
 * JsonSchemaBuilder builder = factory.createBuilder();
 * </code></pre>
 * 
 * <p>
 * Any instance of this class is safe for use by multiple concurrent threads.
 * For most use cases, only one instance of this class is required within the application.
 * </p>
 * 
 * @author leadpony
 */
public interface JsonSchemaBuilderFactory {

    /**
     * Creates a new instance of JSON schema builder.
     * 
     * @return the newly created instance of JSON schema builder.
     */
    JsonSchemaBuilder createBuilder();
}
