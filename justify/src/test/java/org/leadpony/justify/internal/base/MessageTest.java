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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.InstanceType;

/**
 * @author leadpony
 */
public class MessageTest {

    private enum Message implements BaseMessage {
        ERROR
        ;

        @Override
        public ResourceBundle getBundle(Locale locale) {
            return ResourceBundle.getBundle("org/leadpony/justify/internal/messages-test");
        }
    }

    @Test
    public void getLocalized_shouldReturnPattern() {
        Message sut = Message.ERROR;

        String message = sut.getLocalized(Locale.ROOT);

        assertThat(message).isEqualTo("It must be of {expected} type, but actual type is {actual}.");
    }

    @Test
    public void format_shouldReturnFormattedMessage() {
        Map<String, Object> args = new HashMap<>();
        args.put("actual", InstanceType.STRING);
        args.put("expected", InstanceType.INTEGER);

        Message sut = Message.ERROR;

        String message = sut.format(args, Locale.ROOT);

        assertThat(message).isEqualTo("It must be of integer type, but actual type is string.");
    }
}
