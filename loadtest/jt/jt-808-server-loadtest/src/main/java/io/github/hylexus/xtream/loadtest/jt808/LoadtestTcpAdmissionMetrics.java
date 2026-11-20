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

package io.github.hylexus.xtream.loadtest.jt808;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import reactor.netty.Connection;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 按 TCP 连接统计就绪、首帧解码和首个出站报文写成功三个阶段。
 * 首个出站报文耗时从连接就绪回调开始，统计到 Netty 写入完成。
 *
 * @author Codex (AI)
 */
final class LoadtestTcpAdmissionMetrics {
    private static final AttributeKey<ConnectionTiming> TIMING = AttributeKey.valueOf(LoadtestTcpAdmissionMetrics.class, "timing");
    private final LongAdder readyConnections = new LongAdder();
    private final LongAdder firstDecodedFrames = new LongAdder();
    // 下标取微秒值的二进制位数。0 单独成桶，后续各桶覆盖 1、2～3、4～7 等区间。
    private final LongAdder[] firstResponseMicros = new LongAdder[Long.SIZE];
    private final FirstResponseObserver firstResponseObserver = new FirstResponseObserver();

    LoadtestTcpAdmissionMetrics() {
        Arrays.setAll(firstResponseMicros, ignored -> new LongAdder());
    }

    void onConnection(Connection connection) {
        // 用单调时钟记录经过时间，避免系统时间调整影响计时。
        connection.channel().attr(TIMING).set(new ConnectionTiming(System.nanoTime()));
        connection.addHandlerLast("loadtest-first-response", firstResponseObserver);
        readyConnections.increment();
    }

    void onFrameDecoded(Channel channel) {
        final var timing = channel.attr(TIMING).get();
        // 同一 TCP 连接按序解码。首帧计数发生在校验码和业务检查之前。
        if (timing != null && !timing.firstFrameDecoded) {
            timing.firstFrameDecoded = true;
            firstDecodedFrames.increment();
        }
    }

    LoadtestMetrics.TcpAdmissionSnapshot snapshot() {
        // 分位和总数使用同一份桶计数快照，避免并发累加时两者取样不一致。
        final long[] samples = new long[firstResponseMicros.length];
        long total = 0;
        for (int i = 0; i < samples.length; i++) {
            samples[i] = firstResponseMicros[i].sum();
            total += samples[i];
        }
        return new LoadtestMetrics.TcpAdmissionSnapshot(
                readyConnections.sum(), firstDecodedFrames.sum(), total,
                percentileUpperBound(samples, total - total / 20),
                percentileUpperBound(samples, total - total / 100)
        );
    }

    private static long percentileUpperBound(long[] samples, long rank) {
        int bucket = 0;
        while (rank > samples[bucket]) {
            rank -= samples[bucket];
            bucket++;
        }
        // 只保存分桶计数，所以用命中桶的上界近似分位值。空样本返回 0。
        return (1L << bucket) - 1;
    }

    @ChannelHandler.Sharable
    private final class FirstResponseObserver extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) {
            final var attribute = context.channel().attr(TIMING);
            final var timing = attribute.get();
            if (timing == null || !(message instanceof ByteBuf)) {
                context.write(message, promise);
                return;
            }
            // Netty 允许传入不可监听的 void promise，这里需要观察写入结果。
            final ChannelPromise tracked = promise.unvoid();
            tracked.addListener(result -> {
                // 写失败时保留计时。多个写入回调中只有成功清除计时状态的那个计入样本。
                if (result.isSuccess() && attribute.compareAndSet(timing, null)) {
                    final long micros = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - timing.readyAtNanos);
                    firstResponseMicros[Long.SIZE - Long.numberOfLeadingZeros(micros)].increment();
                    if (!context.isRemoved()) {
                        // 首次采样完成后，从当前连接移除观察器以缩短后续写路径。
                        context.pipeline().remove(this);
                    }
                }
            });
            context.write(message, tracked);
        }
    }

    private static final class ConnectionTiming {
        private final long readyAtNanos;
        private boolean firstFrameDecoded;

        private ConnectionTiming(long readyAtNanos) {
            this.readyAtNanos = readyAtNanos;
        }
    }
}
