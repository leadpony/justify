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
package org.leadpony.justify.cli;

import java.io.PrintStream;

/**
 * A colorful version of {@link Console}.
 *
 * @author leadpony
 */
public class ColorConsole extends Console {

    private static final String RESET = "\u001b[0m";

    private Color color;

    /**
     * Constructs this console.
     *
     * @param out the standard output.
     * @param err the error output.
     */
    ColorConsole(PrintStream out, PrintStream err) {
        super(out, err);
        this.color = Color.DEFAULT;
    }

    @Override
    Console withColor(Color color) {
        if (color != null) {
            this.color = color;
        }
        return this;
    }

    @Override
    protected String decorate(String line) {
        if (color == Color.DEFAULT) {
            return line;
        }
        Color color = this.color;
        this.color = Color.DEFAULT;
        return new StringBuilder(color.code()).append(line).append(RESET).toString();
    }
}
