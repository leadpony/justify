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
package org.leadpony.justify.internal.schema.binding;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.leadpony.justify.api.SpecVersion;

/**
 * A utility class for operating on {@link KeywordBinder}.
 *
 * @author leadpony
 */
public class KeywordBinders {

    private static final KeywordBinder[] binderList = {
            new AdditionalItemsBinder(),
            new AdditionalPropertiesBinder(),
            new AllOfBinder(),
            new AnyOfBinder(),
            new CommentBinder(),
            new ConstBinder(),
            new ContainsBinder(),
            new ContentEncodingBinder(),
            new ContentMediaTypeBinder(),
            new DefaultBinder(),
            new DefinitionsBinder(),
            new DependenciesBinder(),
            new DescriptionBinder(),
            new ElseBinder(),
            new EnumBinder(),
            new ExclusiveMaximumBinder(),
            new ExclusiveMinimumBinder(),
            new FormatBinder(),
            new IdBinder(),
            new IfBinder(),
            new ItemsBinder(),
            new MaximumBinder(),
            new MaxItemsBinder(),
            new MaxLengthBinder(),
            new MaxPropertiesBinder(),
            new MinimumBinder(),
            new MinItemsBinder(),
            new MinLengthBinder(),
            new MinPropertiesBinder(),
            new MultipleOfBinder(),
            new NotBinder(),
            new OneOfBinder(),
            new PatternBinder(),
            new PatternPropertiesBinder(),
            new PropertiesBinder(),
            new PropertyNamesBinder(),
            new RefBinder(),
            new RequiredBinder(),
            new SchemaBinder(),
            new ThenBinder(),
            new TitleBinder(),
            new TypeBinder(),
            new UniqueItemsBinder(),

            // For the Draft-04.
            new Draft04ExclusiveMaximumBinder(),
            new Draft04ExclusiveMinimumBinder(),
            new Draft04IdBinder(),
            new Draft04MaximumBinder(),
            new Draft04MinimumBinder(),
            new Draft04TypeBinder(),
    };

    private static final Map<SpecVersion, Map<String, KeywordBinder>> bindersBySpec = groupBindersBySpec();

    /**
     * Returns all keyword binders avaiable for the specified spec version.
     *
     * @param version the target spec version.
     * @return all keyword binders.
     */
    public static Map<String, KeywordBinder> getBinders(SpecVersion version) {
        return bindersBySpec.get(version);
    }

    private static Map<SpecVersion, Map<String, KeywordBinder>> groupBindersBySpec() {
        Map<SpecVersion, Map<String, KeywordBinder>> map = new HashMap<>();

        for (SpecVersion v : SpecVersion.values()) {
            map.put(v, new HashMap<>());
        }

        for (KeywordBinder b : binderList) {
            for (SpecVersion v : b.getSupportedVersions()) {
                map.get(v).put(b.name(), b);
            }
        }

        map.replaceAll((k, v) -> Collections.unmodifiableMap(v));

        return Collections.unmodifiableMap(map);
    }

    private KeywordBinders() {
    }
}
