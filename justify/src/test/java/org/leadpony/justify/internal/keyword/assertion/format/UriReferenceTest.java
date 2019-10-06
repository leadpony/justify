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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * A test class for {@link UriReference}.
 *
 * @author leadpony
 */
public class UriReferenceTest {

    // System under test
    private static UriReference sut;

    @BeforeAll
    public static void setUpOnce() {
        sut = new UriReference(true);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @UriSource(value = "uri.json", validOnly = true)
    public void testUri(String value, boolean relative, boolean valid) {
        boolean actual = sut.test(value);
        assertThat(actual).isEqualTo(valid);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @UriSource({
        "uri-ref.json",
        "/com/sporkmonger/addressable/uri.json"
    })
    public void testUriReference(String value, boolean relative, boolean valid) {
        boolean actual = sut.test(value);
        assertThat(actual).isEqualTo(valid);
    }
}
