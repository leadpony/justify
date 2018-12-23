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

package org.leadpony.justify.internal.keyword.assertion.format;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author leadpony
 */
class VerboseUriReferenceMatcher extends UriReferenceMatcher {
 
    private static final Logger log = Logger.getLogger(VerboseUriReferenceMatcher.class.getName());
    
    private final List<String> components = new ArrayList<>();

    VerboseUriReferenceMatcher(CharSequence input) {
        super(input);
    }

    @Override
    boolean all() {
        boolean result = super.all();
        if (result) {
            printComponents();
        }
        return result;
    }
    
    @Override
    boolean scheme() {
        final int start = pos();
        if (super.scheme()) {
            addComponent(start);
            return true;
        }
        return false;
    }

    @Override
    boolean userinfo() {
        final int start = pos();
        if (super.userinfo()) {
            addComponent(start);
            return true;
        }
        return false;
    }

    @Override
    boolean host() {
        final int start = pos();
        if (super.host()) {
            addComponent(start);
            return true;
        }
        return false;
    }

    @Override
    void port() {
        final int start = pos();
        super.port();
        addComponent(start);
    }

    @Override
    boolean pathAbempty() {
        final int start = pos();
        if (super.pathAbempty()) {
            addComponent(start);
            return true;
        }
        return false;
    }

    @Override
    boolean pathNoscheme() {
        final int start = pos();
        if (super.pathNoscheme()) {
            addComponent(start);
            return true;
        }
        return false;
    }
    
    @Override
    boolean pathAbsolute() {
        final int start = pos();
        if (super.pathAbsolute()) {
            addComponent(start);
            return true;
        }
        return false;
    }

    @Override
    boolean pathRootless() {
        final int start = pos();
        if (super.pathRootless()) {
            addComponent(start);
            return true;
        }
        return false;
    }

    @Override
    boolean pathEmpty() {
        final int start = pos();
        if (super.pathEmpty()) {
            addComponent(start);
            return true;
        }
        return false;
    }

    @Override
    boolean query() {
        final int start = pos();
        if (super.query()) {
            addComponent(start);
            return true;
        }
        return false;
    }

    @Override
    boolean fragment() {
        final int start = pos();
        if (super.fragment()) {
            addComponent(start);
            return true;
        }
        return false;
    }

    @Override
    boolean relativeRef() {
        components.clear();
        return super.relativeRef();
    }
    
    private void addComponent(int offset) {
        components.add(extract(offset, pos()));
    }

    private void printComponents() {
        if (!log.isLoggable(Level.FINE)) {
            return;
        }
        StringBuilder b = new StringBuilder();
        b.append(input())
         .append(" -> ")
         .append(components.stream().collect(Collectors.joining(", ", "[", "]")))
         ;
        log.fine(b.toString());
    }
}
