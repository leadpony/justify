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

import javax.json.JsonString;
import javax.json.JsonValue;

import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.Localizable;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A custom format attribute representing resource locations.
 *
 * @author leadpony
 */
public class PathOrUrlFormatAttribute implements FormatAttribute {

    @Override
    public String name() {
        return "pathOrUrl";
    }

    @Override
    public Localizable localizedName() {
        return locale->Message.PATH_OR_URL.toString();
    }

    @Override
    public InstanceType valueType() {
        return InstanceType.STRING;
    }

    @Override
    public boolean test(JsonValue value) {
        String string = ((JsonString)value).getString();
        try {
            Location.at(string);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
