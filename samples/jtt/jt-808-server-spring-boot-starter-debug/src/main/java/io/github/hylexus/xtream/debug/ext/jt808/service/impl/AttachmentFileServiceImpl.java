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

package io.github.hylexus.xtream.debug.ext.jt808.service.impl;


import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage30316364;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.location.AlarmIdentifier;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.debug.ext.jt808.domain.properties.DemoAppConfigProperties;
import io.github.hylexus.xtream.debug.ext.jt808.service.AttachmentFileService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Future;

/**
 * @author hylexus
 */
@Service
public class AttachmentFileServiceImpl implements AttachmentFileService {

    public static final Scheduler SCHEDULER = Schedulers.boundedElastic();
    private final DemoAppConfigProperties appProps;

    public AttachmentFileServiceImpl(DemoAppConfigProperties appProps) {
        this.appProps = appProps;
    }

    @Override
    public Mono<Integer> writeDataFragmentAsync(Jt808Session session, BuiltinMessage30316364 body, BuiltinMessage1210 group) {
        final AlarmIdentifier alarmIdentifier = group.getAlarmIdentifier();
        final LocalDateTime localDateTime = alarmIdentifier.getTime();
        // 这里就瞎写了一个路径  看你需求随便改
        final String filePath = appProps.getAttachmentServer().getTemporaryPath() + File.separator
                                + DateTimeFormatter.ofPattern("yyyyMMddHH").format(localDateTime) + File.separator
                                + session.terminalId() + File.separator
                                + group.getMessageType() + File.separator
                                // + group.getAlarmNo() + File.separator
                                + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(localDateTime) + "-" + group.getAlarmNo() + File.separator
                                + body.getFileName().trim();

        // 使用 AsynchronousFileChannel
        // return this.writeDataWithAsynchronousFileChannel(body, filePath);
        // 使用 RandomAccessFile
        return this.writeDataWithRandomAccessFile(body, filePath);
    }

    private Mono<Integer> writeDataWithRandomAccessFile(BuiltinMessage30316364 body, String filePath) {
        return Mono.<Integer>create(sink -> {
            try {
                createFileIfNecessary(filePath);
            } catch (IOException e) {
                sink.error(e);
                return;
            }

            try (final RandomAccessFile file = new RandomAccessFile(filePath, "rws")) {
                file.seek(body.getDataOffset());
                final int dataLength = (int) body.getDataLength();
                file.write(body.getData(), 0, dataLength);
                sink.success(dataLength);
            } catch (IOException e) {
                sink.error(e);
            }
            // 将阻塞操作调度到单独的线程池中执行
        }).subscribeOn(SCHEDULER);
    }

    @SuppressWarnings("unused")
    private Mono<Integer> writeDataWithAsynchronousFileChannel(BuiltinMessage30316364 body, String filePath) {
        return Mono.<Integer>create(sink -> {
            try {
                createFileIfNecessary(filePath);
            } catch (IOException e) {
                sink.error(e);
                return;
            }

            try (final AsynchronousFileChannel channel = AsynchronousFileChannel.open(
                    Path.of(filePath),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ,
                    StandardOpenOption.SPARSE
            )) {
                final ByteBuffer buffer = ByteBuffer.wrap(body.getData());
                final Future<Integer> writeFuture = channel.write(buffer, body.getDataOffset());
                final Integer dataSize = writeFuture.get();
                sink.success(dataSize);
            } catch (Throwable e) {
                sink.error(e);
            }
            // 将阻塞操作调度到单独的线程池中执行
        }).subscribeOn(SCHEDULER);
    }

    static void createFileIfNecessary(String filePath) throws IOException {
        final File tempFile = new File(filePath);
        if (!tempFile.getParentFile().exists()) {
            if (!tempFile.getParentFile().mkdirs()) {
                throw new RuntimeException("新建目录失败:" + tempFile);
            }
        }
        if (!tempFile.exists()) {
            if (!tempFile.createNewFile()) {
                throw new RuntimeException("新建文件失败:" + tempFile);
            }
        }
    }

}
