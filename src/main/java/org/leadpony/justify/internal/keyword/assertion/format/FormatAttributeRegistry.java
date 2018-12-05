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

import java.util.HashMap;
import java.util.ServiceLoader;

import org.leadpony.justify.spi.FormatAttribute;

/**
 * Registry of format attributes.
 * 
 * @author leadpony
 */
@SuppressWarnings("serial")
public class FormatAttributeRegistry extends HashMap<String, FormatAttribute> {
    
    public FormatAttributeRegistry() {
        registerAttributes();
        registerDefaultAttributes();
    }
    
    private void registerAttributes() {
        ServiceLoader<FormatAttribute> loader = ServiceLoader.load(FormatAttribute.class);
        for (FormatAttribute attribute : loader) {
            register(attribute);
        }
    }
    
    private void registerDefaultAttributes() {
        registerIfNotExist(new Date());
        registerIfNotExist(new DateTime());
        registerIfNotExist(new Email());
        registerIfNotExist(new Hostname());
        registerIfNotExist(new Ipv4());
        registerIfNotExist(new Ipv6());
        registerIfNotExist(new IdnEmail());
        registerIfNotExist(new IdnHostname());
        registerIfNotExist(new Iri());
        registerIfNotExist(new IriReference());
        registerIfNotExist(new JsonPointer());
        registerIfNotExist(new Regex());
        registerIfNotExist(new RelativeJsonPointer());
        registerIfNotExist(new Time());
        registerIfNotExist(new Uri());
        registerIfNotExist(new UriReference());
        registerIfNotExist(new UriTemplate());
    }
    
    private void registerIfNotExist(FormatAttribute attribute) {
        if (!containsKey(attribute.name())) {
            register(attribute);
        }
    }
    
    private void register(FormatAttribute attribute) {
        put(attribute.name(), attribute);
    }
}
