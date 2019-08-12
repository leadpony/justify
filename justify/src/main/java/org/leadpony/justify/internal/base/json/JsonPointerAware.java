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
package org.leadpony.justify.internal.base.json;

/**
 * A type which is aware of JSON pointer.
 *
 * @author leadpony
 */
public interface JsonPointerAware {

    /**
     * Returns the current position as a JSON pointer.
     *
     * @return the JSON pointer which points to the current value. {@code null} will
     *         be returned before any event.
     */
    String getPointer();
}
