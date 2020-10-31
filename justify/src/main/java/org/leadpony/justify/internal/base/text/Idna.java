/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.internal.base.text;

import java.util.regex.Pattern;

import com.ibm.icu.text.IDNA;

/**
 * @author leadpony
 */
public enum Idna {

    IDNA2008 {

        private final IDNA idna = IDNA.getUTS46Instance(
                IDNA.USE_STD3_RULES
                | IDNA.CHECK_BIDI
                | IDNA.CHECK_CONTEXTJ
                | IDNA.NONTRANSITIONAL_TO_ASCII
                | IDNA.NONTRANSITIONAL_TO_UNICODE
                | IDNA.CHECK_CONTEXTO);

        @Override
        public boolean verifyLabel(String label) {
            if (label.startsWith(ACE_PREFIX)) {
                return verifyALabel(label);
            } else {
                return verifyULabel(label);
            }
        }

        private boolean verifyALabel(String label) {
            StringBuilder builder = new StringBuilder();
            IDNA.Info info = new IDNA.Info();
            this.idna.labelToUnicode(label, builder, info);
            return !info.hasErrors();
        }

        private boolean verifyULabel(String label) {
            boolean asciiOnly = true;
            boolean ldhLabel = true;
            final int length = label.length();
            for (int i = 0; i < length; i++) {
                char c = label.charAt(i);
                if (c < 128) {
                    if (!AsciiCode.isAlphanumeric(c) && c != '-') {
                        ldhLabel = false;
                    }
                } else {
                    asciiOnly = false;
                    IdnProperty property = IdnProperty.of(c);
                    if (property == IdnProperty.DISALLOWED || property == IdnProperty.UNASSIGNED) {
                        return false;
                    }
                }
            }

            if (asciiOnly) {
                return ldhLabel && verifyAsciiULabel(label);
            } else {
                return verifyNonAsciiULabel(label);
            }
        }

        private boolean verifyNonAsciiULabel(String label) {
            StringBuilder builder = new StringBuilder();
            IDNA.Info info = new IDNA.Info();
            this.idna.labelToASCII(label, builder, info);
            return !info.hasErrors();
        }
    };

    private static final String ACE_PREFIX = "xn--";
    private static final int MAX_LABEL_LENGTH = 63;
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    public boolean verifyName(CharSequence name) {
        String[] labels = DOT_PATTERN.split(name, -1);
        for (String label : labels) {
            if (label.isEmpty() || !verifyLabel(label)) {
                return false;
            }
        }
        return true;
    }

    public abstract boolean verifyLabel(String label);

    private static boolean verifyAsciiULabel(String label) {
        final int length = label.length();
        if (length > MAX_LABEL_LENGTH) {
            return false;
        }
        if (length >= 4 && label.charAt(2) == '-' && label.charAt(3) == '-') {
            return false;
        }
        if (label.startsWith("-") || label.endsWith("-")) {
            return false;
        }
        return true;
    }
}
