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

package org.leadpony.justify.api;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaReader;

/**
 * A test class for testing remote schema resolutions using embedded web server.
 *
 * @author leadpony
 */
public class RemoteSchemaTest extends BaseValidationTest {

    private static final Logger log = Logger.getLogger(RemoteSchemaTest.class.getName());

    private static final String[] TESTS = { "/org/json_schema/tests/draft7/refRemote.json", };

    private static Server server;

    private static JsonSchemaReaderFactory readerFactory;

    public static Stream<ValidationFixture> provideFixtures() {
        return Stream.of(TESTS).flatMap(ValidationFixture::newStream);
    }

    @BeforeAll
    public static void setUpOnce() throws Exception {

        readerFactory = service.createSchemaReaderFactoryBuilder().withSchemaResolver(RemoteSchemaTest::resolveSchema)
                .build();

        server = new Server(1234);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        Path basePath = Paths.get("target/test-classes", "org/json_schema/remotes");
        resourceHandler.setResourceBase(basePath.toString());
        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler);
        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);

        server.start();
    }

    @AfterAll
    public static void tearDownOnce() throws Exception {
        server.stop();
    }

    @Override
    protected JsonSchemaReader createSchemaReader(Reader reader) {
        return readerFactory.createSchemaReader(reader);
    }

    private static JsonSchema resolveSchema(URI id) {
        try (InputStream in = id.toURL().openStream()) {
            return service.readSchema(in);
        } catch (Exception e) {
            log.severe(e.toString());
            return null;
        }
    }
}
