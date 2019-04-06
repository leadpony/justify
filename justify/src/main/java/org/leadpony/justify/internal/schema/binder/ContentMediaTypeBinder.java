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
package org.leadpony.justify.internal.schema.binder;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.MediaType;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.Keyword;
import org.leadpony.justify.internal.keyword.assertion.content.ContentMediaType;
import org.leadpony.justify.internal.keyword.assertion.content.UnknownContentMediaType;
import org.leadpony.justify.spi.ContentMimeType;

/**
 * A binder type for "contentMediaType" keyword.
 *
 * @author leadpony
 */
@Spec({ SpecVersion.DRAFT_07 })
class ContentMediaTypeBinder extends AbstractBinder {

    @Override
    public String name() {
        return "contentMediaType";
    }

    @Override
    public void fromJson(JsonParser parser, BinderContext context) {
        Event event = parser.next();
        if (event == Event.VALUE_STRING) {
            String name = parser.getString();
            try {
                MediaType mediaType = MediaType.valueOf(name);
                ContentMimeType mimeType = context.getMimeType(mediaType.mimeType());
                Keyword keyword = (mimeType != null)
                        ? new ContentMediaType(mimeType, mediaType.parameters())
                        : new UnknownContentMediaType(name);
                context.addKeyword(keyword);
            } catch (IllegalArgumentException e) {
                context.addProblem(Message.SCHEMA_PROBLEM_CONTENTMEDIATYPE_INVALID);
            }
        } else {
            skipValue(event, parser);
        }
    }
}
