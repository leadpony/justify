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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A utility class operating on instances of {@link URI}.
 *
 * @author leadpony
 */
public final class URIs {

    public static URI withFragment(URI uri) {
        if (uri.getFragment() == null) {
            return uri.resolve("#");
        } else {
            return uri;
        }
    }

    public static URI withEmptyFragment(URI uri) {
        return uri.resolve("#");
    }

    public static URI removeFragment(URI uri) {
        String fragment = uri.getFragment();
        if (fragment != null) {
            try {
                return new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
            } catch (URISyntaxException e) {
                // Never happens
                throw new IllegalArgumentException(e);
            }
        } else {
            return uri;
        }
    }

    /**
     * Removes the fragment if it is empty.
     *
     * @param uri the original URI.
     * @return the URI after modification.
     */
    public static URI removeEmptyFragment(URI uri) {
        String fragment = uri.getFragment();
        if ("".equals(fragment)) {
            return removeFragment(uri);
        } else {
            return uri;
        }
    }

    public static boolean compare(URI x, URI y) {
        if (!x.isAbsolute() || !y.isAbsolute()) {
            return false;
        }
        return withFragment(x).compareTo(withFragment(y)) == 0;
    }

    private URIs() {
    }
}
