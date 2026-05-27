/*
 * Copyright 2024-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hylexus.xtream.codec.ext.jt808.codec.impl;


import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808BytesProcessor;
import io.github.hylexus.xtream.codec.ext.jt808.exception.Jt808MessageEscapeException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hylexus
 */
public class DefaultJt808BytesProcessor implements Jt808BytesProcessor {
    private static final Logger log = LoggerFactory.getLogger(DefaultJt808BytesProcessor.class);
    private final ByteBufAllocator allocator;

    private static final byte BYTE_7D = 0x7d;
    private static final byte BYTE_7E = 0x7e;

    public DefaultJt808BytesProcessor(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    @Override
    public ByteBuf doEscapeForReceive(ByteBuf byteBuf) throws Jt808MessageEscapeException {
        final int readableBytes = byteBuf.readableBytes();

        final byte delimiter = 0x7d;
        int from = byteBuf.readerIndex();
        int indexOf = byteBuf.indexOf(from, readableBytes, delimiter);
        if (indexOf < 0) {
            return byteBuf.retain();
        }

        final List<ByteBuf> byteBufList = new ArrayList<>();
        try {
            do {
                if (from == 0 && indexOf > 0) {
                    byteBufList.add(byteBuf.retainedSlice(from, indexOf));
                }
                final byte current = byteBuf.getByte(indexOf);
                final byte next = byteBuf.getByte(indexOf + 1);
                if (current == 0x7d) {
                    if (next == 0x01) {
                        // xxx7D01xxx --> xxx7Dxxx
                        byteBufList.add(allocator.buffer().writeByte(0x7d));
                        from = indexOf + 2;
                    } else if (next == 0x02) {
                        // xxx7D02xxx --> xxx7Exxx
                        byteBufList.add(allocator.buffer().writeByte(0x7e));
                        from = indexOf + 2;
                    } else {
                        log.warn("0x7d should be followed by 0x01 or 0x02, but 0x{}", FormatUtils.toHexString(next));
                        byteBufList.add(allocator.buffer().writeByte(0x7d));
                        from = indexOf + 1;
                    }
                }
                indexOf = byteBuf.indexOf(from, readableBytes, delimiter);
                if (from < indexOf) {
                    byteBufList.add(byteBuf.retainedSlice(from, indexOf - from));
                }
            } while (from < readableBytes && indexOf > 0);

            if (from < readableBytes) {
                byteBufList.add(byteBuf.retainedSlice(from, readableBytes - from));
            }
        } catch (Throwable e) {
            // 发生异常时将内部 component 释放掉
            XtreamBytes.releaseBufList(byteBufList);
            throw e;
        }
        return allocator.compositeBuffer(byteBufList.size()).addComponents(true, byteBufList);
    }

    @Override
    public ByteBuf doEscapeForSend(ByteBuf byteBuf) throws Jt808MessageEscapeException {
        int readableBytes = byteBuf.readableBytes();
        int from = 0;
        int indexOf = nextIndexOf(byteBuf, from, readableBytes);
        if (indexOf < 0) {
            return byteBuf;
        }

        final List<ByteBuf> bufList = new ArrayList<>();
        boolean hasError = false;
        try {
            do {
                if (from < indexOf) {
                    bufList.add(byteBuf.retainedSlice(from, indexOf - from));
                }
                final byte current = byteBuf.getByte(indexOf);
                if (current == BYTE_7D) {
                    bufList.add(allocator.buffer().writeByte(0x7d).writeByte(0x01));
                } else if (current == BYTE_7E) {
                    bufList.add(allocator.buffer().writeByte(0x7d).writeByte(0x02));
                }
                from = indexOf + 1;
            } while (from < readableBytes && (indexOf = nextIndexOf(byteBuf, from, readableBytes)) > 0);

            // 确保添加剩余的数据
            if (from < readableBytes) {
                bufList.add(byteBuf.retainedSlice(from, readableBytes - from));
            }
        } catch (Throwable throwable) {
            hasError = true;
            // 发生异常时将内部 component 释放掉
            XtreamBytes.releaseBufList(bufList);
            throw throwable;
        } finally {
            if (!hasError) {
                XtreamBytes.releaseBuf(byteBuf);
            }
        }
        return allocator.compositeBuffer(bufList.size()).addComponents(true, bufList);
    }


    private int nextIndexOf(ByteBuf byteBuf, int from, int to) {
        final int index1 = byteBuf.indexOf(from, to, BYTE_7E);
        final int index2 = byteBuf.indexOf(from, to, BYTE_7D);
        if (index1 < 0 && index2 < 0) {
            return -1;
        }
        if (index1 < 0) {
            return index2;
        }
        if (index2 < 0) {
            return index1;
        }
        return Math.min(index1, index2);
    }

    @Override
    public byte calculateCheckSum(ByteBuf byteBuf) {
        byte sum = 0;
        while (byteBuf.isReadable()) {
            sum ^= byteBuf.readByte();
        }
        byteBuf.resetReaderIndex();
        return sum;
    }
}

