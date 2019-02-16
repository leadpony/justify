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
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A local resource whose location is specified with a {@code Path}.
 *
 * @author leadpony
 */
class LocalResource implements Resource {

    private final Path path;

    /**
     * Constructs this resource.
     *
     * @param path the path where this resource resides.
     */
    LocalResource(Path path) {
        this.path = path;
    }

    @Override
    public InputStream openStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return path.toUri().toURL();
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
