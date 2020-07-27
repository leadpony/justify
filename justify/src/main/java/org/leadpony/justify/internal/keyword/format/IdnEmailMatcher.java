/*
 * Copyright 2018, 2020 the Justify authors.
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

import org.leadpony.justify.internal.base.text.Idna;

/**
 * Matcher for internationalized email addresses.
 *
 * @author leadpony
 */
class IdnEmailMatcher extends EmailMatcher {

    IdnEmailMatcher(CharSequence input) {
        super(input);
    }

    @Override
    protected boolean checkQuotedLetter(int c) {
        if (c < 128) {
            return super.checkQuotedLetter(c);
        } else {
            return true;
        }
    }

    @Override
    protected boolean checkAtomLetter(int c) {
        if (c < 128) {
            return super.checkAtomLetter(c);
        } else {
            return true;
        }
    }

    @Override
    protected boolean checkDomainLiteralLetter(int c) {
        if (c < 128) {
            return super.checkDomainLiteralLetter(c);
        } else {
            return true;
        }
    }

    @Override
    protected boolean checkCommentLetter(int c) {
        if (c < 128) {
            return super.checkCommentLetter(c);
        } else {
            return true;
        }
    }

    @Override
    protected boolean checkHostname(int start, int end) {
        return Idna.IDNA2008.verifyName(input().subSequence(start, end));
    }
}
