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

import org.leadpony.justify.internal.base.AsciiCode;

/**
 * @author leadpony
 *
 */
final class UuidMatcher extends FormatMatcher {

    /**
     * Constructs this matcher.
     *
     * @param input the input character sequence.
     */
    UuidMatcher(CharSequence input) {
        super(input);
    }

    @Override
    boolean all() {
        return timeLow()
            && hyphen()
            && timeMid()
            && hyphen()
            && timeHighAndVersion()
            && hyphen()
            && clockSeqAndReserved()
            && clockSeqLow()
            && hyphen()
            && node()
            && !hasNext();
    }

    private boolean timeLow() {
        return hexOctet(4);
    }

    private boolean timeMid() {
        return hexOctet(2);
    }

    private boolean timeHighAndVersion() {
        return hexOctet(2);
    }

    private boolean clockSeqAndReserved() {
        return hexOctet();
    }

    private boolean clockSeqLow() {
        return hexOctet();
    }

    private boolean node() {
        return hexOctet(6);
    }

    private boolean hyphen() {
        return hasNext() && next() == '-';
    }

    private boolean hexOctet(int count) {
        while (count-- > 0) {
            if (!hexOctet()) {
                return false;
            }
        }
        return true;
    }

    private boolean hexOctet() {
        return hexDigit() && hexDigit();
    }

    private boolean hexDigit() {
        return hasNext() && AsciiCode.isHexDigit(next());
    }
}
