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
package org.leadpony.justify.internal.keyword.format;

import org.leadpony.justify.internal.base.AsciiCode;

/**
 * @author leadpony
 */
class DurationMatcher extends AbstractFormatMatcher {

    /**
     * Constructs this matcher.
     *
     * @param input the input character sequence.
     */
    DurationMatcher(CharSequence input) {
        super(input);
    }

    @Override
    protected boolean test() {
        if (hasNext('P')) {
            next();
            if (hasNext()) {
                if (date() || time() || week()) {
                    return !hasNext();
                }
            }
        }
        return false;
    }

    private boolean digits() {
        int length = 0;
        while (hasNext() && AsciiCode.isDigit(peek())) {
            next();
            ++length;
        }
        return length > 0;
    }

    private boolean date() {
        final int offset = pos();
        if (!digits()) {
            return false;
        }
        int units = 0;
        int c = next();
        if (c == 'Y') {
            ++units;
            if (digits()) {
                c = next();
            } else {
                return true;
            }
        }
        if (c == 'M') {
            ++units;
            if (digits()) {
                c = next();
            } else {
                return true;
            }
        }
        if (c == 'D') {
            ++units;
            time();
            return true;
        }
        if (units > 0) {
            return fail();
        }
        return backtrack(offset);
    }

    private boolean time() {
        if (hasNext('T')) {
            next();
        } else {
            return false;
        }
        if (digits()) {
            int c = next();
            if (c == 'H') {
                if (digits()) {
                    c = next();
                } else {
                    return true;
                }
            }
            if (c == 'M') {
                if (digits()) {
                    c = next();
                } else {
                    return true;
                }
            }
            if (c == 'S') {
                return true;
            }
        }
        return fail();
    }

    private boolean week() {
        final int offset = pos();
        if (digits() && next() == 'W') {
            return true;
        }
        return backtrack(offset);
    }
}
