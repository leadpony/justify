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

package org.leadpony.justify;

import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Utility class operating on Java Utility Logging.
 * 
 * @author leadpony
 */
public final class Loggers {
    
    static {
        try (InputStream ins = Loggers.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(ins);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Loggers() {
    }

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
