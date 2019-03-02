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

package org.leadpony.justify.internal.base.json;

import javax.json.stream.JsonLocation;

/**
 * A simple implementation of {@link JsonLocation}.
 *
 * @author leadpony
 */
public class SimpleJsonLocation implements JsonLocation {

    /**
     * An unknown location.
     */
    public static final JsonLocation UNKNOWN = new SimpleJsonLocation(-1, -1, -1);

    private final long lineNumber;
    private final long columnNumber;
    private final long streamOffset;

    /**
     * Constructs this location.
     *
     * @param lineNumber the line number which starts with 1 for the first line.
     * @param columnNumber the column number which starts with 1 for the first column.
     * @param streamOffset the stream offset in the input source.
     */
    public SimpleJsonLocation(long lineNumber, long columnNumber, long streamOffset) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.streamOffset = streamOffset;
    }

    public static JsonLocation before(JsonLocation other) {
        long lineNumber = other.getLineNumber();
        long columnNumber = other.getColumnNumber();
        long streamOffset = other.getStreamOffset();
        if (columnNumber > 1) {
            --columnNumber;
        }
        if (streamOffset > 0) {
            --streamOffset;
        }
        return new SimpleJsonLocation(lineNumber, columnNumber, streamOffset);
    }

    @Override
    public long getLineNumber() {
        return lineNumber;
    }

    @Override
    public long getColumnNumber() {
        return columnNumber;
    }

    @Override
    public long getStreamOffset() {
        return streamOffset;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(line no=").append(lineNumber)
               .append(", column no=").append(columnNumber)
               .append(", offset=").append(streamOffset)
               .append(")");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (columnNumber ^ (columnNumber >>> 32));
        result = prime * result + (int) (lineNumber ^ (lineNumber >>> 32));
        result = prime * result + (int) (streamOffset ^ (streamOffset >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof JsonLocation)) {
            return false;
        }
        JsonLocation other = (JsonLocation)obj;
        return lineNumber == other.getLineNumber() &&
               columnNumber == other.getColumnNumber() &&
               streamOffset == other.getStreamOffset();
    }
}
