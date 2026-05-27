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

package io.github.hylexus.xtream.codec.ext.jt808.spec;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @param <T> 消息体类型
 * @author hylexus
 * @see io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseEntityHandlerResultHandler
 */
public class Jt808ResponseEntity<T> {
    protected final int flowId;
    protected final Integer messageId;
    protected final int maxPackageSize;
    protected final byte reversedBit15InHeader;
    protected final byte encryptionType;
    protected final T body;

    public Jt808ResponseEntity(Integer messageId, T body) {
        this(-1, messageId, 1024, (byte) 0b0, (byte) 0b000, body);
    }

    public Jt808ResponseEntity(int flowId, Integer messageId, int maxPackageSize, byte reversedBit15InHeader, byte encryptionType, T body) {
        this.flowId = flowId;
        this.messageId = messageId;
        this.maxPackageSize = maxPackageSize;
        this.reversedBit15InHeader = reversedBit15InHeader;
        this.encryptionType = encryptionType;
        this.body = body;
    }

    public int getFlowId() {
        return flowId;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public int getMaxPackageSize() {
        return maxPackageSize;
    }

    public byte getReversedBit15InHeader() {
        return reversedBit15InHeader;
    }

    public byte getEncryptionType() {
        return encryptionType;
    }

    public T getBody() {
        return body;
    }

    public static Builder newBuilder() {
        return new DefaultBuilder();
    }

    public static Builder messageId(Integer messageId) {
        return new DefaultBuilder().messageId(messageId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Jt808ResponseEntity.class.getSimpleName() + "[", "]")
                .add("flowId=" + flowId)
                .add("messageId=" + messageId)
                .add("maxPackageSize=" + maxPackageSize)
                .add("reversedBit15InHeader=" + reversedBit15InHeader)
                .add("encryptionType=" + encryptionType)
                .add("body=" + body)
                .toString();
    }

    public interface Builder {
        Builder flowId(int flowId);

        Builder messageId(Integer messageId);

        Builder maxPackageSize(int maxPackageSize);

        Builder reversedBit15InHeader(byte reversedBit15InHeader);

        Builder encryptionType(byte encryptionType);

        default <T> Jt808ResponseEntity<T> body(T body) {
            return this.build(body);
        }

        <T> Jt808ResponseEntity<T> build(T body);
    }

    static class DefaultBuilder implements Builder {
        private @Nullable Integer messageId;
        private int maxPackageSize = 1024;
        private byte reversedBit15InHeader = 0b0;
        private byte encryptionType = 0b000;
        private int flowId = -1;

        @Override
        public Builder flowId(int flowId) {
            this.flowId = flowId;
            return this;
        }

        @Override
        public Builder messageId(Integer messageId) {
            this.messageId = Objects.requireNonNull(messageId, "messageId must not be null");
            return this;
        }

        @Override
        public Builder maxPackageSize(int maxPackageSize) {
            if (maxPackageSize < 200) {
                throw new IllegalArgumentException("maxPackageSize must be greater than 200");
            }
            this.maxPackageSize = maxPackageSize;
            return this;
        }

        @Override
        public Builder reversedBit15InHeader(byte reversedBit15InHeader) {
            this.reversedBit15InHeader = reversedBit15InHeader;
            return this;
        }

        @Override
        public Builder encryptionType(byte encryptionType) {
            this.encryptionType = encryptionType;
            return this;
        }

        @Override
        public <T> Jt808ResponseEntity<T> build(T body) {
            return new Jt808ResponseEntity<>(
                    this.flowId,
                    Objects.requireNonNull(this.messageId, "messageId must not be null"),
                    this.maxPackageSize,
                    this.reversedBit15InHeader,
                    this.encryptionType,
                    body
            );
        }

    }
}
