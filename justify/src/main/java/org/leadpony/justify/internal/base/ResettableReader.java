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
package org.leadpony.justify.internal.base;

import java.io.CharArrayWriter;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author leadpony
 */
public class ResettableReader extends FilterReader {

    private final CharArrayWriter writer = new CharArrayWriter();

    public ResettableReader(Reader in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c >= 0) {
            writer.write(c);
        }
        return c;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int actualLen = super.read(cbuf, off, len);
        if (actualLen > 0) {
            writer.write(cbuf, off, actualLen);
        }
        return actualLen;
    }

    @Override
    public long skip(long n) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        // Keeps the underlying reader open.
    }

    public Reader createResettedReader() {
        return new ResettedReader(in, writer.toCharArray());
    }

    private static class ResettedReader extends FilterReader {

        private final char[] buffer;
        private int index;

        private ResettedReader(Reader in, char[] buffer) {
            super(in);
            this.buffer = buffer;
        }

        @Override
        public int read() throws IOException {
            if (index < buffer.length) {
                return buffer[index++];
            } else {
                return super.read();
            }
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (index < buffer.length) {
                if (index + len <= buffer.length) {
                    System.arraycopy(buffer, index, cbuf, off, len);
                    index += len;
                    return len;
                } else {
                    final int first =  buffer.length - index;
                    System.arraycopy(buffer, index, cbuf, off, first);
                    index += first;
                    int second = super.read(cbuf, off + first, len - first);
                    if (second < 0) {
                        return first;
                    } else {
                        return first + second;
                    }
                }
            } else {
                return super.read(cbuf, off, len);
            }
        }

        @Override
        public long skip(long n) throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
