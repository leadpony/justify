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

import java.util.HashMap;
import java.util.Map;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * @author leadpony
 */
public class FormatAttributes {

    private static final FormatAttribute[] attributeList = {
            new Date(),
            new DateTime(),
            new Email(),
            new Hostname(),
            new Ipv4(),
            new Ipv6(),
            new IdnEmail(),
            new IdnHostname(),
            new Iri(),
            new IriReference(),
            new JsonPointer(),
            new Regex(),
            new RelativeJsonPointer(),
            new Time(),
            new Uri(),
            new UriReference(),
            new UriTemplate()
    };

    private static final Map<SpecVersion, Map<String, FormatAttribute>> attributesBySpec = groupAttributesBySpec();

    public static Map<String, FormatAttribute> getAttributes(SpecVersion version) {
        return attributesBySpec.get(version);
    }

    private static Map<SpecVersion, Map<String, FormatAttribute>> groupAttributesBySpec() {
        Map<SpecVersion, Map<String, FormatAttribute>> map = new HashMap<>();

        for (SpecVersion version : SpecVersion.values()) {
            map.put(version, new HashMap<>());
        }

        for (FormatAttribute attribute : attributeList) {
            Spec spec = attribute.getClass().getAnnotation(Spec.class);
            if (spec != null) {
                for (SpecVersion version : spec.value()) {
                    map.get(version).put(attribute.name(), attribute);
                }
            }
        }
        return map;
    }

    private FormatAttributes() {
    }
}
