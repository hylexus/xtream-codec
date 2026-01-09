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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.collector;

import io.github.hylexus.xtream.codec.common.utils.XtreamBytes;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.AudioPackage;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.Jt1078AudioCodec;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.BuiltinAudioFormatOptions;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.aac.AacJt1078AudioCodec;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.adpcm.AdpcmImaJt1078AudioCodec;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.g711.G711ALawJt1078AudioCodec;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.g711.G711MuLawJt1078AudioCodec;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.FlvEncoder;
import io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.impl.DefaultFlvEncoder;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.*;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.DefaultJt1078Subscriber;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.H264Jt1078SubscriberCreator;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078DataType;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078PayloadType;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Request;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author hylexus
 * @see <a href="https://gitee.com/ldming/JT1078">https://gitee.com/ldming/JT1078</a>
 * @see <a href="https://gitee.com/matrixy/jtt1078-video-server">https://gitee.com/matrixy/jtt1078-video-server</a>
 */
public class H264ToFlvJt1078ChannelCollector implements Jt1078ChannelCollector {
    private static final Logger log = LoggerFactory.getLogger(H264ToFlvJt1078ChannelCollector.class);
    private static final Set<Jt1078PayloadType> SUPPORTED_PAYLOAD_TYPES = Set.of(
            Jt1078PayloadType.H264,
            Jt1078PayloadType.ADPCMA,
            Jt1078PayloadType.G_711A,
            Jt1078PayloadType.G_711U,
            Jt1078PayloadType.AAC
    );

    protected final ConcurrentMap<String, H264ToFlvSubscriber> subscribers = new ConcurrentHashMap<>();
    protected final Jt1078Channel.ChannelKey channelKey;

    protected final Scheduler scheduler;
    protected final H264Jt1078SubscriberCreator subscriberCreator;

    protected @Nullable Jt1078AudioCodec audioCodec;
    protected long firstVideoTimestamp = -1L;
    protected FlvEncoder flvEncoder;

    public H264ToFlvJt1078ChannelCollector(Jt1078Channel.ChannelKey channelKey, Scheduler scheduler, Jt1078SubscriberCreator creator) {
        this.subscriberCreator = (H264Jt1078SubscriberCreator) creator;
        this.channelKey = channelKey;
        this.scheduler = scheduler;
        this.flvEncoder = new DefaultFlvEncoder(this.subscriberCreator.h264Meta().naluDecoderRingBufferSize());
    }

    protected boolean isSupportedPayloadType(Jt1078PayloadType payloadType) {
        return SUPPORTED_PAYLOAD_TYPES.contains(payloadType);
    }

    @Override
    public Scheduler scheduler() {
        return this.scheduler;
    }

    @Override
    public void collect(Jt1078Request request) {
        final Jt1078PayloadType payloadType = request.header().payloadType();
        if (!this.isSupportedPayloadType(payloadType)) {
            this.doErrorLogIfNecessary(request, payloadType);
            return;
        }

        switch (request.dataType()) {
            case Jt1078DataType.AUDIO -> this.collectAudioData(request);
            case Jt1078DataType.VIDEO_I,
                 Jt1078DataType.VIDEO_B,
                 Jt1078DataType.VIDEO_P -> this.collectVideoData(request);
            case TRANSPARENT_TRANSMISSION -> {
                // ignored
            }
            case null -> throw new IllegalStateException("DataType is null");
        }
    }

    protected void collectVideoData(Jt1078Request request) {
        final long timestamp = request.header().timestamp().orElseThrow();
        if (this.firstVideoTimestamp == -1L) {
            this.firstVideoTimestamp = timestamp;
        }
        final List<ByteBuf> bufList = this.flvEncoder.encodeVideoTag(timestamp - this.firstVideoTimestamp, request.body());
        for (final ByteBuf byteBuf : bufList) {
            try {
                for (final H264ToFlvSubscriber subscriber : this.subscribers.values()) {
                    final byte[] videoData = XtreamBytes.getBytes(byteBuf);
                    subscriber.sendVideoData(timestamp, videoData, request.payloadType());
                }
            } finally {
                XtreamBytes.releaseBuf(byteBuf);
            }
        }
    }

    protected void collectAudioData(Jt1078Request request) {
        final Jt1078AudioCodec jt1078AudioCodec = this.getAudioCodec(request.payloadType());
        final AudioPackage pcm = jt1078AudioCodec.toPcm(new AudioPackage(BuiltinAudioFormatOptions.ADPCM_IMA_MONO, request.payload()));
        try {
            final long timestamp = request.header().timestamp().orElseThrow();
            for (final H264ToFlvSubscriber subscriber : this.subscribers.values()) {
                subscriber.sendAudioData(timestamp, pcm.shallowCopy(), request.payloadType());
            }
        } finally {
            pcm.close();
        }
    }

    @Override
    public Jt1078Subscriber doSubscribe(Jt1078SubscriberCreator creator) {
        final String uuid = this.channelKey.generateSubscriberId();

        final Flux<Jt1078Subscription> dataStream = Flux
                .<Jt1078Subscription>create(fluxSink -> {
                    log.info("new subscriber created with id: {}", uuid);
                    final H264ToFlvSubscriber subscriber = createSubscriber(uuid, creator, fluxSink);
                    synchronized (this.subscribers) {
                        this.subscribers.put(uuid, subscriber);
                    }
                })
                // todo 缓冲区配置
                // .onBackpressureBuffer(1024, byteArrayJt1078Subscription -> {
                //     // ...
                //     log.error("onBackpressureDrop, channelKey={}", channelKey);
                // }, BufferOverflowStrategy.DROP_OLDEST)
                .publishOn(this.scheduler())
                .timeout(creator.timeout())
                .doFinally(signalType -> {
                    log.info("Subscriber {} removed", uuid);
                    synchronized (this.subscribers) {
                        final H264ToFlvSubscriber removed = this.subscribers.remove(uuid);
                        if (removed != null) {
                            removed.close();
                        }
                        // todo 状态测试
                        // 通过 Jt1078Channel.close(Jt1078SubscriberCloseException) 关闭时 Collector 会从 Channel 上移除
                        // 但是 通过下面两个方法关闭时 Collector 没有从 Channel 上移除，这里重置一下状态
                        // Jt1078Channel.unsubscribe(String, Jt1078SubscriberCloseException)
                        // Jt1078Channel.unsubscribe(String)
                        if (this.subscribers.isEmpty()) {
                            this.resetStatus();
                        }
                    }
                });
        return new DefaultJt1078Subscriber(uuid, dataStream);
    }

    protected void resetStatus() {
        this.flvEncoder = new DefaultFlvEncoder(this.subscriberCreator.h264Meta().naluDecoderRingBufferSize());
    }

    @Override
    public void unsubscribe(String id, Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason) {
        final H264ToFlvSubscriber subscriber = this.subscribers.get(id);
        if (subscriber != null) {
            if (reason == null) {
                subscriber.sink().complete();
            } else {
                subscriber.sink().error(reason);
            }
        }
    }

    @Override
    public void close(Jt1078Subscriber.@Nullable Jt1078SubscriberCloseException reason) {
        final var iterator = this.subscribers.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, H264ToFlvSubscriber> entry = iterator.next();
            iterator.remove();
            final H264ToFlvSubscriber internalSubscriber = entry.getValue();
            try {
                if (reason == null) {
                    internalSubscriber.sink().complete();
                } else {
                    internalSubscriber.sink().error(reason);
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        this.flvEncoder.close();
    }

    @Override
    public long count(Predicate<Jt1078SubscriberDescriptor> predicate) {
        return this.subscribers.values().stream().filter(predicate).count();
    }

    @Override
    public Stream<Jt1078SubscriberDescriptor> list() {
        return this.subscribers.values().stream().map(Function.identity());
    }

    protected H264ToFlvSubscriber createSubscriber(String uuid, Jt1078SubscriberCreator creator, FluxSink<Jt1078Subscription> fluxSink) {
        return new H264ToFlvSubscriber(uuid, (H264Jt1078SubscriberCreator) creator, this.flvEncoder, fluxSink);
    }

    private static final Map<Jt1078PayloadType, Boolean> WARNING_FLAGS = new HashMap<>();

    private void doErrorLogIfNecessary(Jt1078Request request, Jt1078PayloadType payloadType) {
        final Boolean flag = WARNING_FLAGS.get(payloadType);
        if (flag == null || !flag) {
            WARNING_FLAGS.put(payloadType, true);
            log.error("Unsupported payloadType : {}. sim = {}, channelNumber = {}, dataType = {}, request = {}", payloadType, request.sim(), request.channelNumber(), request.header().dataType(), request);
        }
    }

    protected Jt1078AudioCodec getAudioCodec(Jt1078PayloadType payloadType) {
        if (this.audioCodec != null) {
            return this.audioCodec;
        }
        if (Jt1078PayloadType.ADPCMA.value() == payloadType.value()) {
            return new AdpcmImaJt1078AudioCodec();
        } else if (Jt1078PayloadType.G_711A.value() == payloadType.value()) {
            return new G711ALawJt1078AudioCodec();
        } else if (Jt1078PayloadType.G_711U.value() == payloadType.value()) {
            return new G711MuLawJt1078AudioCodec();
        } else if (Jt1078PayloadType.AAC.value() == payloadType.value()) {
            return new AacJt1078AudioCodec();
        }
        return Jt1078AudioCodec.SILENCE;
    }

}
