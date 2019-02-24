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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * A remote location which can be specified with a {@code URI}.
 *
 * @author leadpony
 */
class RemoteLocation implements Location {

    public static final Pattern URL_PATTERN = Pattern.compile("^(https?|file|jar):");

    private final URL url;

    /**
     * Constructs this location.
     *
     * @param url the URL of this location.
     */
    RemoteLocation(URL url) {
        this.url = url;
    }

    @Override
    public InputStream openStream() throws IOException {
        return url.openStream();
    }

    @Override
    public Location resolve(Location other) {
        if (other instanceof LocalLocation) {
            Path otherPath = ((LocalLocation) other).path();
            if (otherPath.isAbsolute()) {
                return other;
            } else {
                try {
                    URL resolved = url.toURI().resolve(otherPath.toString()).toURL();
                    return new RemoteLocation(resolved);
                } catch (MalformedURLException | URISyntaxException e) {
                    return other;
                }
            }
        } else {
            return other;
        }
    }

    @Override
    public URL toURL() {
        return url;
    }

    @Override
    public String toString() {
        return url.toString();
    }
}
