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

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link InputStream} type which can be reset once.
 *
 * @author leadpony
 */
public class ResettableInputStream extends FilterInputStream {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public ResettableInputStream(InputStream in) {
        super(in);
    }

    @Override
    public final int read() throws IOException {
        int b = super.read();
        if (b >= 0) {
            out.write(b);
        }
        return b;
    }

    @Override
    public final int read(byte[] b) throws IOException {
        int result = super.read(b);
        if (result > 0) {
            out.write(b);
        }
        return result;
    }

    @Override
    public final int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result > 0) {
            out.write(b, off, len);
        }
        return result;
    }

    @Override
    public final long skip(long n) throws IOException {
        throw new UnsupportedOperationException();
    }

    public final InputStream createResettedStream() {
        return new SecondInputStream(in, out.toByteArray());
    }

    /**
     *
     * @author leadpony
     */
    private static final class SecondInputStream extends FilterInputStream {

        private final byte[] buffer;
        private int index;

        private SecondInputStream(InputStream in, byte[] buffer) {
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
        public int read(byte[] b, int off, int len) throws IOException {
            if (index < buffer.length) {
                if (index + len <= buffer.length) {
                    System.arraycopy(buffer, index, b, off, len);
                    index += len;
                    return len;
                } else {
                    final int firstLen = buffer.length - index;
                    System.arraycopy(buffer, index, b, off, firstLen);
                    index += firstLen;
                    int secondLen = super.read(b, off + firstLen, len - firstLen);
                    if (secondLen < 0) {
                        return firstLen;
                    } else {
                        return firstLen + secondLen;
                    }
                }
            } else {
                return super.read(b, off, len);
            }
        }

        @Override
        public long skip(long n) throws IOException {
            if (index < buffer.length) {
                if (index + n <= buffer.length) {
                    index += n;
                    return n;
                } else {
                    long firstLen = buffer.length - index;
                    index = buffer.length;
                    return firstLen + super.skip(n - firstLen);
                }
            } else {
                return super.skip(n);
            }
        }
    }
}
