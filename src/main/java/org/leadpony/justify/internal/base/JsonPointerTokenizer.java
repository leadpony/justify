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

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.json.JsonException;

/**
 * Tokenizer for a JSON pointer.
 * 
 * @author leadpony
 */
public class JsonPointerTokenizer implements Iterator<String> {

    private final String jsonPointer;
    private int offset;
    
    public JsonPointerTokenizer(String jsonPointer) {
        if (!jsonPointer.isEmpty() && !jsonPointer.startsWith("/")) {
            throw new JsonException("Invalid JSON pointer: " + jsonPointer);
        }
        this.jsonPointer = jsonPointer;
        this.offset = 0;
    }

    @Override
    public boolean hasNext() {
        return offset < jsonPointer.length();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        String token = null;
        int end = jsonPointer.indexOf('/', offset + 1);
        if (end >= 0) {
            token = jsonPointer.substring(offset + 1, end);
            offset = end;
        } else {
            token = jsonPointer.substring(offset + 1);
            offset = jsonPointer.length();
        }
        return decode(token);
    }
    
    /**
     * Returns the remaining part of the JSON pointer.
     * 
     * @return the remaining part of the JSON pointer. It may be empty.
     */
    public String remaining() {
        return jsonPointer.substring(offset);
    }

    /**
     * Decodes a reference token.
     * 
     * @param token the reference token to decode.
     * @return decoded reference token.
     */
    private static String decode(String token) {
        return token.replaceAll("~1", "/").replaceAll("~0", "~");
    }
}
