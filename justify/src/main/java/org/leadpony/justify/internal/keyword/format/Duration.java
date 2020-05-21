/*
 * Copyright 2018-2020 the Justify authors.
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
package org.leadpony.justify.internal.keyword.format;

/**
 * A format attribute representing "duration".
 *
 * @author leadpony
 *
 * @see <a href="https://tools.ietf.org/html/rfc3339">RFC 3339</a>
 */
public class Duration extends AbstractFormatAttribute {

    static final Duration INSTANCE = new Duration();

    @Override
    public String name() {
        throw new UnsupportedOperationException();
    }

    @Override
    boolean test(String value) {
        return new DurationMatcher(value).matches();
    }
}
