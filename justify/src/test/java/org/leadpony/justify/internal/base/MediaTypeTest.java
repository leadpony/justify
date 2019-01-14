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
package org.leadpony.justify.internal.base;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link MediaType}.
 *
 * @author leadpony
 */
public class MediaTypeTest {

    @Test
    public void valueOf_shouldProduceMediaTypeWithoutParameters() {
        MediaType mediaType = MediaType.valueOf("application/json");

        assertThat(mediaType.mimeType()).isEqualTo("application/json");
        assertThat(mediaType.parameters()).isEmpty();
    }

    @Test
    public void valueOf_shouldProduceMediaTypeWithParameter() {
        MediaType mediaType = MediaType.valueOf("text/html; charset=utf-8");

        assertThat(mediaType.mimeType()).isEqualTo("text/html");
        assertThat(mediaType.parameters())
            .hasSize(1)
            .containsEntry("charset", "utf-8");
    }

    @Test
    public void valueOf_shouldProduceMediaTypeWithParameters() {
        MediaType mediaType = MediaType.valueOf("text/html; param1=foo; param2=bar");

        assertThat(mediaType.mimeType()).isEqualTo("text/html");
        assertThat(mediaType.parameters())
            .hasSize(2)
            .containsEntry("param1", "foo")
            .containsEntry("param2", "bar")
            ;
    }

    @Test
    public void valueOf_shouldThrowExceptionIfEmpty() {
        Throwable thrown = catchThrowable(()->{
            MediaType.valueOf("");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void valueOf_shouldThrowExceptionIfTypeIsEmpty() {
        Throwable thrown = catchThrowable(()->{
            MediaType.valueOf("; charset=utf-8");
        });
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
