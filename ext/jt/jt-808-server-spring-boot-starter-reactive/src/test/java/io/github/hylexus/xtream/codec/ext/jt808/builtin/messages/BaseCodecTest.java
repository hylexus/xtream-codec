/*
 * Copyright 2024 the original author or authors.
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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages;

import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.core.XtreamCacheableClassPredicate;
import io.github.hylexus.xtream.codec.core.impl.DefaultFieldCodecRegistry;
import io.github.hylexus.xtream.codec.core.impl.SimpleBeanMetadataRegistry;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.DefaultJt808BytesProcessor;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.DefaultJt808RequestCombiner;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.DefaultJt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.DefaultJt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.*;
import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.annotation.AnnotatedElementUtils;
import reactor.netty.NettyInbound;

import java.time.Duration;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author hylexus
 */
public class BaseCodecTest {
    protected String terminalId2011 = "013912344321";
    protected String terminalId2013 = "013912344323";
    protected String terminalId2019 = "00000000013912344329";

    ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
    Jt808RequestDecoder decoder = new DefaultJt808RequestDecoder(new DefaultJt808BytesProcessor(allocator), new Jt808MessageEncryptionHandler.NoOps(), new DefaultJt808RequestCombiner(allocator, 100, Duration.ofSeconds(100)));
    NettyInbound nettyInbound;
    protected EntityCodec entityCodec = new EntityCodec(new SimpleBeanMetadataRegistry(
            new DefaultFieldCodecRegistry(),
            new XtreamCacheableClassPredicate.Default()
    ));
    protected Jt808ResponseEncoder responseEncoder;

    @BeforeEach
    protected void doBeforeEach() {
        this.nettyInbound = mock(NettyInbound.class);
        this.responseEncoder = new DefaultJt808ResponseEncoder(
                allocator,
                increment -> 0,
                entityCodec,
                new DefaultJt808BytesProcessor(allocator),
                new Jt808MessageEncryptionHandler.NoOps()
        );
        when(nettyInbound.withConnection(any())).thenReturn(nettyInbound);
    }

    protected interface CodecValidator<T> {
        void accept(T expected, T actual, String hexString);
    }

    protected <T> void codec(Jt808ProtocolVersion version, String terminalId, T instance, CodecValidator<T> assertion) {
        final ByteBuf buffer = allocator.buffer();
        try {
            final int versionValue = version.versionValue();
            this.entityCodec.encode(versionValue, instance, buffer.retain());
            final String hexString = this.encode(buffer.slice(), version, terminalId, 0x0200);
            @SuppressWarnings("unchecked") final Class<T> cls = (Class<T>) instance.getClass();
            final T decode = this.entityCodec.decode(versionValue, cls, buffer);
            assertion.accept(instance, decode, hexString);
        } finally {
            XtreamBytes.releaseBuf(buffer);
            // Assertions.assertEquals(0, buffer.refCnt());
        }
    }

    protected Jt808Request decodeAsRequest(String hex) {
        final ByteBuf payload = XtreamBytes.byteBufFromHexString(allocator, hex);
        try {
            return decoder.decode(Jt808ServerType.INSTRUCTION_SERVER, null, allocator, nettyInbound, XtreamRequest.Type.TCP, payload, null, null);
        } finally {
            XtreamBytes.releaseBuf(payload);
        }
    }

    protected <T> T decodeAsEntity(Class<T> entityClass, String hex) {
        return decodeAsEntity(null, entityClass, hex);
    }

    protected <T> T decodeAsEntity(Jt808ProtocolVersion protocolVersion, Class<T> entityClass, String hex) {
        Jt808Request jt808Request = null;
        hex = hex.toLowerCase();
        if (hex.startsWith("7e")) {
            hex = hex.substring(2);
        }
        if (hex.endsWith("7e")) {
            hex = hex.substring(0, hex.length() - 2);
        }
        try {
            jt808Request = this.decodeAsRequest(hex);
            final int version = protocolVersion != null
                    ? protocolVersion.versionValue()
                    : jt808Request.version();
            return entityCodec.decode(version, entityClass, jt808Request.payload().slice());
        } finally {
            if (jt808Request != null) {
                jt808Request.release();
                Assertions.assertEquals(0, jt808Request.payload().refCnt());
            }
        }
    }

    protected String encode(Object instance, Jt808ProtocolVersion version, String terminalId) {
        final Jt808ResponseBody annotation = AnnotatedElementUtils.findMergedAnnotation(instance.getClass(), Jt808ResponseBody.class);
        return this.encode(instance, version, terminalId, Objects.requireNonNull(annotation).messageId());
    }

    protected String encode(Object instance, Jt808ProtocolVersion version, String terminalId, int messageId) {
        ByteBuf encoded = null;
        try {
            final Jt808MessageDescriber describer = new Jt808MessageDescriber(messageId, version, terminalId);
            encoded = responseEncoder.encode(instance, describer);
            return FormatUtils.toHexString(encoded);
        } finally {
            if (encoded != null) {
                XtreamBytes.releaseBuf(encoded);
                Assertions.assertEquals(0, encoded.refCnt());
            }
        }
    }
}
