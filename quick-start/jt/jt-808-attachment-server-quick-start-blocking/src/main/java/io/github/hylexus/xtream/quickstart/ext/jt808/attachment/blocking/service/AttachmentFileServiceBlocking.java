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

package io.github.hylexus.xtream.quickstart.ext.jt808.attachment.blocking.service;


import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1212;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage30316364;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestEntity;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Future;

/**
 * @author hylexus
 */
@Service
public class AttachmentFileServiceBlocking {

    public static final Scheduler SCHEDULER = Schedulers.boundedElastic();
    private static final Logger log = LoggerFactory.getLogger(AttachmentFileServiceBlocking.class);
    private final String temporaryPath;

    public AttachmentFileServiceBlocking(@Value("${demo-app.attachment-server.temporary-path}") String temporaryPath) {
        this.temporaryPath = temporaryPath;
        final File file = new File(temporaryPath);
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("创建临时目录失败:" + temporaryPath);
        }
    }

    private String generateAlarmFilePath(String terminalId, BuiltinMessage1210.AttachmentItem attachmentItem) {
        final String time = DateTimeFormatter.ofPattern("yyyyMMddHH").format(attachmentItem.getGroup().getAlarmIdentifier().getTime());
        return time + File.separator
               + terminalId + File.separator
               + DateTimeFormatter.ofPattern("mm").format(attachmentItem.getGroup().getAlarmIdentifier().getTime())
               + "-"
               + attachmentItem.getGroup().getAlarmNo() + File.separator
               + attachmentItem.getFileName();
    }

    // OSS中的存储路径
    private String generateAlarmFileRemotePath(String terminalId, BuiltinMessage1210.AttachmentItem attachmentItem) {
        return this.generateAlarmFilePath(terminalId, attachmentItem);
    }

    // 本地临时文件路径
    private String generateAlarmFileLocalPath(String terminalId, BuiltinMessage1210.AttachmentItem attachmentItem) {
        return this.temporaryPath + File.separator
               + "alarm" + File.separator
               + this.generateAlarmFilePath(terminalId, attachmentItem);
    }

    public int writeDataFragmentAsync(Jt808Session session, BuiltinMessage30316364 body, BuiltinMessage1210.AttachmentItem attachmentItem) {
        // 本地临时文件路径
        final String filePath = this.generateAlarmFileLocalPath(session.terminalId(), attachmentItem);

        // 使用 AsynchronousFileChannel
        // return this.writeDataWithAsynchronousFileChannel(body, filePath);
        // 使用 RandomAccessFile
        return this.writeDataWithRandomAccessFile(body, filePath);
    }

    public boolean moveFileToRemoteStorage(Jt808RequestEntity<BuiltinMessage1212> requestEntity, BuiltinMessage1210.AttachmentItem attachmentItem, boolean deleteLocalFile) {
        final String terminalId = requestEntity.getHeader().terminalId();
        final String localFilePath = this.generateAlarmFileLocalPath(terminalId, attachmentItem);
        final String remoteFilePath = this.generateAlarmFileRemotePath(terminalId, attachmentItem);
        if (deleteLocalFile) {
            log.info("Moving file(DeleteLocalFile) from {} to remote storage: {}", localFilePath, remoteFilePath);
        } else {
            log.info("Moving file(RetainLocalFile) from {} to remote storage: {}", localFilePath, remoteFilePath);
        }
        return this.uploadToOss(deleteLocalFile, localFilePath, remoteFilePath);
    }

    private boolean uploadToOss(boolean deleteLocalFile, String localFilePath, String remoteFilePath) {
        log.info("Uploading file to remote storage: {}", remoteFilePath);
        // todo 这里省略上传到其他存储介质(OSS,Minio,...)的过程
        if (deleteLocalFile) {
            try {
                Files.delete(Paths.get(localFilePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private int writeDataWithRandomAccessFile(BuiltinMessage30316364 body, String filePath) {
        // 文件已经在 0x1211 消息中创建过了
        try (final RandomAccessFile file = new RandomAccessFile(filePath, "rws")) {
            file.seek(body.getDataOffset());
            final int dataLength = (int) body.getDataLength();
            file.write(body.getData(), 0, dataLength);
            return dataLength;
        } catch (IOException e) {
            log.error("Error writing data to file: {}", e.getMessage());
            return 0;
        }
    }

    @SuppressWarnings("unused")
    private int writeDataWithAsynchronousFileChannel(BuiltinMessage30316364 body, String filePath) {
        // 文件已经在 0x1211 消息中创建过了
        try (final AsynchronousFileChannel channel = AsynchronousFileChannel.open(
                Path.of(filePath),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.SPARSE
        )) {
            final ByteBuffer buffer = ByteBuffer.wrap(body.getData());
            final Future<Integer> writeFuture = channel.write(buffer, body.getDataOffset());
            return writeFuture.get();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String createFileIfNecessary(String terminalId, BuiltinMessage1210.AttachmentItem attachmentItem) throws IOException {
        final String filePath = this.generateAlarmFileLocalPath(terminalId, attachmentItem);
        this.createFileIfNecessary(filePath);
        return filePath;
    }

    void createFileIfNecessary(String filePath) throws IOException {
        final File tempFile = new File(filePath);
        if (tempFile.exists()) {
            return;
        }
        if (!tempFile.getParentFile().exists()) {
            synchronized (this) {
                if (!tempFile.getParentFile().exists()) {
                    if (!tempFile.getParentFile().mkdirs()) {
                        throw new RuntimeException("新建目录失败:" + tempFile.getParentFile());
                    }
                }
            }
        }

        if (!tempFile.exists()) {
            synchronized (this) {
                if (!tempFile.exists()) {
                    if (!tempFile.createNewFile()) {
                        throw new RuntimeException("新建文件失败:" + tempFile);
                    }
                }
            }
        }
    }

}
