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

package org.leadpony.justify.internal.keyword.assertion.format;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.leadpony.justify.spi.FormatAttribute;

/**
 * The registry of format attributes.
 *
 * @author leadpony
 */
public abstract class FormatAttributeRegistry {

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

    private static final FormatAttribute[] INTERNAL = {
            DATE,
            DATE_TIME,
            EMAIL,
            HOSTNAME,
            IPV4,
            IPV6,
            IDN_EMAIL,
            IDN_HOSTNAME,
            IRI,
            IRI_REFERENCE,
            JSON_POINTER,
            REGEX,
            RELATIVE_JSON_POINTER,
            TIME,
            URI,
            URI_REFERENCE,
            URI_TEMPLATE
    };

    protected final Set<FormatAttribute> external;

    public static FormatAttributeRegistry getDefault() {
        return new DefaultFormatAttributeRegistry();
    }

    protected FormatAttributeRegistry(Set<FormatAttribute> external) {
        this.external = external;
    }

    public Map<String, FormatAttribute> createMap() {
        return merge(INTERNAL, this.external);
    }

    public abstract FormatAttributeRegistry withCustomFormatAttributes(boolean active);

    protected Map<String, FormatAttribute> internalAsMap() {
        Map<String, FormatAttribute> map = new HashMap<>();
        for (FormatAttribute attribute : INTERNAL) {
            map.put(attribute.name(), attribute);
        }
        return map;
    }

    protected Map<String, FormatAttribute> allAsMap() {
        Map<String, FormatAttribute> map = internalAsMap();
        for (FormatAttribute attribute: this.external) {
            map.put(attribute.name(), attribute);
        }
        return map;
    }

    private static Map<String, FormatAttribute> merge(FormatAttribute[] internal, Set<FormatAttribute> external) {
        Map<String, FormatAttribute> map = new HashMap<>();
        for (FormatAttribute attribute : internal) {
            map.put(attribute.name(), attribute);
        }
        for (FormatAttribute attribute : external) {
            map.put(attribute.name(), attribute);
        }
        return map;
    }

    private static class DefaultFormatAttributeRegistry extends FormatAttributeRegistry {

        private final Map<String, FormatAttribute> defaultMap;

        DefaultFormatAttributeRegistry() {
            super(findExternal());
            this.defaultMap = Collections.unmodifiableMap(allAsMap());
        }

        @Override
        public Map<String, FormatAttribute> createMap() {
            return defaultMap;
        }

        @Override
        public FormatAttributeRegistry withCustomFormatAttributes(boolean active) {
            if (active) {
                return this;
            } else {
                return custom().withCustomFormatAttributes(active);
            }
        }

        private static Set<FormatAttribute> findExternal() {
            Set<FormatAttribute> external = new HashSet<>();
            ServiceLoader<FormatAttribute> loader = ServiceLoader.load(FormatAttribute.class);
            for (FormatAttribute loaded : loader) {
                external.add(loaded);
            }
            return Collections.unmodifiableSet(external);
        }

        private FormatAttributeRegistry custom() {
            return new CustomFormatAttributeRegistry(external);
        }
    }

    private static class CustomFormatAttributeRegistry extends FormatAttributeRegistry {

        private boolean cutsomFormatAttributes = true;

        CustomFormatAttributeRegistry(Set<FormatAttribute> external) {
            super(external);
        }

        @Override
        public Map<String, FormatAttribute> createMap() {
            Map<String, FormatAttribute> map = cutsomFormatAttributes ? allAsMap() : internalAsMap();
            return Collections.unmodifiableMap(map);
        }

        @Override
        public FormatAttributeRegistry withCustomFormatAttributes(boolean active) {
            this.cutsomFormatAttributes = active;
            return this;
        }
    }
}
