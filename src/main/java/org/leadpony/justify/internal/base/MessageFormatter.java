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

import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Formatter of message.
 * 
 * @author leadpony
 */
class MessageFormatter {

    private final String input;
    private final Function<String, String> resolver;
    private int offset;
  
    /**
     * Constructs this formatter.
     * 
     * @param input the original message.
     * @param resolver the type to resolve variables to their contents. 
     */
    MessageFormatter(String input, Function<String, String> resolver) {
        this.input = input;
        this.resolver = resolver;
        this.offset= 0;
    }
  
    /**
     * Formats the message.
     * 
     * @return the formatted message.
     */
    String format() {
        StringBuilder builder = new StringBuilder();
        while (hasNext()) {
            char c = next();
            if (c == '$' && hasNext()) {
                c = next();
                if (c == '{') {
                    builder.append(placeholder());
                } else {
                    builder.append('$').append(c);
                }
            } else {
                builder.append(c); 
            }
        }
        return builder.toString();
    }
    
    private String placeholder() {
        StringBuilder builder = new StringBuilder();
        while (hasNext()) {
            char c = next();
            if (c == '}') {
                break;
            } else if (c == '$' && hasNext()) {
                c = next();
                if (c == '{') {
                    String expanded = placeholder(); 
                    builder.append(unquote(expanded));
                } else {
                    builder.append('$');
                    if (c == '}') {
                        break;
                    } else {
                        builder.append(c);
                    }
                }
            } else {
                builder.append(c); 
            }
        }
        return resolve(builder.toString());
    }
    
    private String resolve(String name) {
        String[] tokens = name.split(":");
        String content = resolver.apply(tokens[0]);
        if (tokens.length > 1) {
            if (tokens[1].equals("captal")) {
                content = capitalizeFirst(content);
            }
        }
        return content;
    }
    
    private static String capitalizeFirst(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        char[] chars = string.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    
    private static String unquote(String string) {
        if (string.startsWith("\"") && string.endsWith("\"")) {
            return string.substring(1, string.length() - 1);
        } else {
            return string;
        }
    }
    
    private boolean hasNext() {
        return offset < input.length();
    }
    
    private char next() {
        if (offset < input.length()) {
            return input.charAt(offset++);
        } else {
            throw new NoSuchElementException();
        }
    }
}
