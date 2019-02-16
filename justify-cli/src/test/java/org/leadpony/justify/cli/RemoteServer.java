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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 * A HTTP server supplying remote JSON files.
 *
 * @author leadpony
 */
class RemoteServer {

    private static final Path DOCUMENT_ROOT = Paths.get("target", "test-classes");

    private final Server server;

    /**
     * Constructs this server.
     *
     * @param port the port to listen.
     * @throws Exception if this server has failed to start.
     */
    RemoteServer(int port) throws Exception {
        server = new Server(port);
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setResourceBase(DOCUMENT_ROOT.toString());
        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler);
        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);

        server.start();
    }

    /**
     * Shutdown this server.
     *
     * @throws Exception if this server has failed to stop.
     */
    void shutdown() throws Exception {
        server.stop();
    }
}
