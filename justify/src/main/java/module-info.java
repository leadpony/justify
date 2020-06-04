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

/**
 * Defines the API for JSON validation.
 */
module org.leadpony.justify {
    exports org.leadpony.justify.api;
    exports org.leadpony.justify.api.keyword;
    exports org.leadpony.justify.spi;

    requires com.ibm.icu;
    requires transitive jakarta.json;
    requires java.logging;

    uses org.leadpony.justify.spi.ContentEncodingScheme;
    uses org.leadpony.justify.spi.ContentMimeType;
    uses org.leadpony.justify.spi.FormatAttribute;
    uses org.leadpony.justify.spi.JsonValidationProvider;

    provides org.leadpony.justify.spi.JsonValidationProvider
        with org.leadpony.justify.internal.provider.DefaultJsonValidationProvider;
}