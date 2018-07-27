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

package org.leadpony.justify.core;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author leadpony
 */
@RunWith(Parameterized.class)
public class RemoteSchemaTest extends BaseValidationTest {
    
    private static final Logger log = Logger.getLogger(RemoteSchemaTest.class.getName());

    private static final String[] TESTS = {
            "/official/tests/draft7/refRemote.json",
        };
    
    private static Server server;
  
    @Parameters(name = "{0} {1}")
    public static Iterable<Object[]> parameters() {
        return fixtures(TESTS);
    }
    
    @BeforeClass
    public static void setUpOnce() throws Exception {
        server = new Server(1234);
        
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        Path basePath = TestResources.pathToResource("/official/remotes");
        resourceHandler.setResourceBase(basePath.toString());
        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler);
        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);
        
        server.start();
    }
    
    @AfterClass
    public static void tearDownOnce() throws Exception {
        server.stop();
    }
    
    public RemoteSchemaTest(String name, String description, ValidationFixture fixture) {
        super(name, description, fixture);
    }

    @Override
    protected JsonSchemaReader createSchemaReader(Reader reader) {
        return super.createSchemaReader(reader)
                .withSchemaResolver(RemoteSchemaTest::resolveSchema);
    }
    
    private static JsonSchema resolveSchema(URI id) {
        try (InputStream in = id.toURL().openStream()) {
            return Jsonv.readSchema(in);
        } catch (Exception e) {
            log.severe(e.toString());
            return null;
        }
    }
}
