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
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * A location interface.
 *
 * @author leadpony
 */
interface Location {

    /**
     * Returns a location.
     *
     * @param location the path or URL.
     * @return newly created location.
     * @throws IllegalArgumentException if the specified {@code location} is invalid.
     */
    static Location at(String location) {
        Objects.requireNonNull(location, "location must not be null.");
        if (location.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Matcher m = RemoteLocation.URL_PATTERN.matcher(location);
        if (m.lookingAt()) {
            try {
                return new RemoteLocation(new URL(location));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            try {
                return new LocalLocation(Paths.get(location));
            } catch (InvalidPathException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Opens an input stream from this location,
     *
     * @return newly created input stream.
     * @throws IOException if an I/O error has occurred.
     */
    InputStream openStream() throws IOException;

    /**
     * Resolves the other location against this location.
     *
     * @param other the location to resolve.
     * @return the resolved location.
     */
    default Location resolve(Location other) {
        return other;
    }

    /**
     * Returns this location as a URL.
     *
     * @return the URL of this location.
     * @throws MalformedURLException if this location does not have a valid URL.
     */
    URL toURL() throws MalformedURLException;
}
