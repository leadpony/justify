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

package org.leadpony.justify.internal.base;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.InstanceType;

/**
 * @author leadpony
 */
public class MessageTest {

    @Test
    public void format_returnsFormattedMessage() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("actual", InstanceType.STRING);
        parameters.put("expected", InstanceType.INTEGER);
        
        Message sut = Message.get("instance.problem.type", Locale.ROOT);
        
        String message = sut.format(parameters);
        
        String expectedMessage = "The value must be of integer type, but actual type is string.";
        assertThat(message).isEqualTo(expectedMessage);
    }
}
