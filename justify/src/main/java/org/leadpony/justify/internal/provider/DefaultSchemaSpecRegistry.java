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
package org.leadpony.justify.internal.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.schema.SchemaCatalog;
import org.leadpony.justify.internal.schema.SchemaSpec;
import org.leadpony.justify.internal.schema.SchemaSpecRegistry;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * The default implementation of {@link SchemaSpecRegistry}.
 *
 * @author leadpony
 */
final class DefaultSchemaSpecRegistry implements SchemaSpecRegistry {

    private final SchemaCatalog catalog;
    private final Map<SpecVersion, SchemaSpec> specs = new HashMap<>();

    private static final Map<String, FormatAttribute> CUSTOM_FORMAT_ATTRIBUTES
        = findFormatAttributes();

    /**
     * Loads new instance of this class.
     *
     * @return newly created instance.
     */
    static SchemaSpecRegistry load(SchemaCatalog catalog) {
        DefaultSchemaSpecRegistry registry = new DefaultSchemaSpecRegistry(catalog);
        registry.installSpecs();
        return registry;
    }

    private DefaultSchemaSpecRegistry(SchemaCatalog catalog) {
        this.catalog = catalog;
    }

    /* As a SchemaSpecRegistry */

    @Override
    public SchemaSpec getSpec(SpecVersion version, boolean full) {
        SchemaSpec spec = specs.get(version);
        if (full) {
            spec = new CustomSchemaSpec(spec, CUSTOM_FORMAT_ATTRIBUTES);
        }
        return spec;
    }

    @Override
    public SchemaCatalog getMetaschemaCatalog() {
        return catalog;
    }

    /* */

    private void installSpecs() {
        for (SchemaSpec spec : StandardSchemaSpec.values()) {
            specs.put(spec.getVersion(), spec);
        }
    }

    private static Map<String, FormatAttribute> findFormatAttributes() {
        Map<String, FormatAttribute> map = new HashMap<>();
        ServiceLoader.load(FormatAttribute.class)
            .forEach(a -> map.put(a.name(), a));
        return Collections.unmodifiableMap(map);
    }
}
