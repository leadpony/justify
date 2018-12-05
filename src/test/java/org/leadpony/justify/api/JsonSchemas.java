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

/**
 * JSON schema samples.
 * 
 * @author leadpony
 */
interface JsonSchemas {

    String PERSON_SCHEMA =
            "{" +
            "\"type\":\"object\"," +
            "\"properties\":{" +
            "\"name\": {\"type\":\"string\"}," +
            "\"age\": {\"type\":\"integer\", \"minimum\":0}" +
            "}," + 
            "\"required\":[\"name\"]" +
            "}";        
 
    String INTEGER_ARRAY_SCHEMA =
            "{" +
            "\"type\":\"array\"," +
            "\"items\":{\"type\":\"integer\"}" +
            "}";
}
