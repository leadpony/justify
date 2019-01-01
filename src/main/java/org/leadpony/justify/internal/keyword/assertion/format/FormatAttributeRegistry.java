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

    private static final FormatAttribute DATE = new Date();
    private static final FormatAttribute DATE_TIME = new DateTime();
    private static final FormatAttribute EMAIL = new Email();
    private static final FormatAttribute HOSTNAME = new Hostname();
    private static final FormatAttribute IPV4 = new Ipv4();
    private static final FormatAttribute IPV6 = new Ipv6();
    private static final FormatAttribute IDN_EMAIL = new IdnEmail();
    private static final FormatAttribute IDN_HOSTNAME =  new IdnHostname();
    private static final FormatAttribute IRI = new Iri();
    private static final FormatAttribute IRI_REFERENCE = new IriReference();
    private static final FormatAttribute JSON_POINTER = new JsonPointer();
    private static final FormatAttribute REGEX = new Regex();
    private static final FormatAttribute RELATIVE_JSON_POINTER = new RelativeJsonPointer();
    private static final FormatAttribute TIME = new Time();
    private static final FormatAttribute URI = new Uri();
    private static final FormatAttribute URI_REFERENCE = new UriReference();
    private static final FormatAttribute URI_TEMPLATE = new UriTemplate();
    
    public FormatAttributeRegistry() {
    }

    public void registeFormatAttribute(FormatAttribute attribute) {
        put(attribute.name(), attribute);
    }

    /**
     * Registers all builtin format attributes with this registry.
     * 
     * @return this registry.
     */
    public FormatAttributeRegistry registerDefault() {
        registeFormatAttribute(DATE);
        registeFormatAttribute(DATE_TIME);
        registeFormatAttribute(EMAIL);
        registeFormatAttribute(HOSTNAME);
        registeFormatAttribute(IPV4);
        registeFormatAttribute(IPV6);
        registeFormatAttribute(IDN_EMAIL);
        registeFormatAttribute(IDN_HOSTNAME);
        registeFormatAttribute(IRI);
        registeFormatAttribute(IRI_REFERENCE);
        registeFormatAttribute(JSON_POINTER);
        registeFormatAttribute(REGEX);
        registeFormatAttribute(RELATIVE_JSON_POINTER);
        registeFormatAttribute(TIME);
        registeFormatAttribute(URI);
        registeFormatAttribute(URI_REFERENCE);
        registeFormatAttribute(URI_TEMPLATE);
        return this;
    }

    public FormatAttributeRegistry registerProvidedFormatAttriutes() {
        ServiceLoader.load(FormatAttribute.class).forEach(this::registeFormatAttribute);
        return this;
    }
}
