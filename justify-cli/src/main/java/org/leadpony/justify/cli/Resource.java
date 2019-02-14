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
import java.util.regex.Matcher;

/**
 * A resource interface.
 *
 * @author leadpony
 */
interface Resource {

    /**
     * Returns the resource at the specified location.
     *
     * @param location the path or URL of the resource.
     * @return newly created resource.
     * @throws IllegalArgumentException if the specified {@code location} is invalid.
     */
    static Resource at(String location) {
        Matcher m = RemoteResource.URL_PATTERN.matcher(location);
        if (m.lookingAt()) {
            try {
                return new RemoteResource(new URL(location));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException();
            }
        } else {
            try {
                return new LocalResource(Paths.get(location));
            } catch (InvalidPathException e) {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Opens an input stream from this resource,
     *
     * @return newly created input stream.
     * @throws IOException if an I/O error has occurred.
     */
    InputStream openStream() throws IOException;

    /**
     * Returns the URL of this resource.
     *
     * @return the URL of this resource.
     * @throws MalformedURLException if this resource does not have a valid URL.
     */
    URL toURL() throws MalformedURLException;
}
