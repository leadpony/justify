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

import java.util.Set;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.keyword.assertion.Draft04Type;
import org.leadpony.justify.internal.keyword.assertion.Type;

/**
 * A binder type for "type" keyword in Draft-04.
 *
 * @author leadpony
 */
@Spec({ SpecVersion.DRAFT_04 })
class Draft04TypeBinder extends TypeBinder {

    protected Type createKeyword(InstanceType type) {
        return Draft04Type.of(type);
    }

    protected Type createKeyword(Set<InstanceType> types) {
        return Draft04Type.of(types);
    }
}
