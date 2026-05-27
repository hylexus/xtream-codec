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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.*;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.ServerCommonReplyMessage;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestBody;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandlerMapping;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestEntity;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.AttachmentFileService;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 苏标附件服务处理器
 *
 * @author hylexus
 */
@Component
@Jt808RequestHandler
public class AttachmentFileHandler {
    private static final Logger log = LoggerFactory.getLogger(AttachmentFileHandler.class);
    // <terminalId, <fileName, AttachmentItem>>
    // 只是个示例而已 看你需求自己改造
    private final Cache<String, ConcurrentMap<String, BuiltinMessage1210.AttachmentItem>> attachmentItemCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();
    private final QuickStartAppProps quickStartAppProps;

    private final AttachmentFileService attachmentFileService;

    public AttachmentFileHandler(QuickStartAppProps quickStartAppProps, AttachmentFileService attachmentFileService) {
        this.quickStartAppProps = quickStartAppProps;
        this.attachmentFileService = attachmentFileService;
    }

    @Jt808RequestHandlerMapping(messageIds = 0x1210)
    @Jt808ResponseBody(messageId = 0x8001)
    public ServerCommonReplyMessage processMsg0x1210(Jt808RequestEntity<BuiltinMessage1210> requestEntity) {
        // 结果; 0:成功/确认; 1:失败; 2:消息有误; 3:不支持; 4:报警处理确认
        final BuiltinMessage1210 body = requestEntity.getBody();
        log.info("[0x1210] ==> {}", body);
        if (requestEntity.getServerType() == Jt808ServerType.INSTRUCTION_SERVER) {
            log.error("[0x1210] 不应该由指令服务器对应的端口处理");
            return ServerCommonReplyMessage.of(requestEntity, (byte) 3);
        }
        final Map<String, BuiltinMessage1210.AttachmentItem> itemMap = this.attachmentItemCache.get(requestEntity.getHeader().terminalId(), key -> new ConcurrentHashMap<>());
        for (final BuiltinMessage1210.AttachmentItem item : body.getAttachmentItemList()) {
            item.setGroup(body);
            itemMap.put(item.getFileName().trim(), item);
        }
        return ServerCommonReplyMessage.success(requestEntity);
    }

    @Jt808RequestHandlerMapping(messageIds = 0x1211)
    @Jt808ResponseBody(messageId = 0x8001)
    public ServerCommonReplyMessage processMsg0x1211(Jt808RequestEntity<BuiltinMessage1211> requestEntity) {
        log.info("[0x1211] ==> {}", requestEntity.getBody());
        // 结果; 0:成功/确认; 1:失败; 2:消息有误; 3:不支持; 4:报警处理确认
        if (requestEntity.getServerType() == Jt808ServerType.INSTRUCTION_SERVER) {
            log.error("[0x1211] 不应该由指令服务器对应的端口处理");
            return ServerCommonReplyMessage.of(requestEntity, (byte) 3);
        }
        final Map<String, BuiltinMessage1210.AttachmentItem> items = this.attachmentItemCache.getIfPresent(requestEntity.getHeader().terminalId());
        if (items == null) {
            log.error("[0x1211] 未找到对应的 0x1210 元数据: terminalId={}, fileName={}", requestEntity.getHeader().terminalId(), requestEntity.getBody().getFileName());
            return ServerCommonReplyMessage.of(requestEntity, (byte) 2);
        }
        final BuiltinMessage1210.AttachmentItem attachmentItem = items.get(requestEntity.getBody().getFileName().trim());
        if (attachmentItem == null) {
            log.error("[0x1211] 收到未知文件上传请求: terminalId={}, fileName={}", requestEntity.getHeader().terminalId(), requestEntity.getBody().getFileName());
            return ServerCommonReplyMessage.of(requestEntity, (byte) 2);
        }
        attachmentItem.setFileType(requestEntity.getBody().getFileType());
        try {
            this.attachmentFileService.createFileIfNecessary(requestEntity.getHeader().terminalId(), attachmentItem);
        } catch (IOException e) {
            log.error("创建文件失败: {}", e.getMessage());
            return ServerCommonReplyMessage.of(requestEntity, (byte) 1);
        }
        return ServerCommonReplyMessage.success(requestEntity);
    }

    @Jt808RequestHandlerMapping(messageIds = 0x1212)
    @Jt808ResponseBody(messageId = 0x9212)
    public BuiltinMessage9212 processMsg0x1212(Jt808RequestEntity<BuiltinMessage1212> requestEntity) {

        final BuiltinMessage1212 body = requestEntity.getBody();
        log.info("[0x1212] ==> {}", body);
        if (requestEntity.getServerType() == Jt808ServerType.INSTRUCTION_SERVER) {
            log.error("[0x1212] 不应该由指令服务器对应的端口处理");
            // 这里可以考虑关掉连接
            return null;
        }
        final ConcurrentMap<String, BuiltinMessage1210.AttachmentItem> items = this.attachmentItemCache.getIfPresent(requestEntity.getHeader().terminalId());
        if (items == null) {
            log.error("[0x1212] 未找到文件元数据, 附件上传之前没有没有发送 0x1210,0x1211 消息??? terminalId={},fileName={}", requestEntity.getHeader().terminalId(), body.getFileName());
            return null;
        }

        final BuiltinMessage1210.AttachmentItem attachmentItem = items.get(body.getFileName());
        if (attachmentItem == null) {
            log.error("[0x1212] 未找到文件元数据, 附件上传之前没有没有发送 0x1210,0x1211 消息??? terminalId={},fileName={}", requestEntity.getHeader().terminalId(), body.getFileName());
            return null;
        }

        final boolean retainLocalTemporaryFile = this.quickStartAppProps.getAttachmentServer().isRetainLocalTemporaryFile();
        try {
            this.attachmentFileService.moveFileToRemoteStorage(requestEntity, attachmentItem, !retainLocalTemporaryFile);
        } catch (Throwable throwable) {
            log.error("Error occurred while moveFileToRemoteStorage", throwable);
            throw new RuntimeException(throwable);
        }

        // 这里忽略了需要重传的文件的处理
        final BuiltinMessage9212 responseMessageBody = new BuiltinMessage9212();
        responseMessageBody.setFileName(body.getFileName());
        responseMessageBody.setFileType(body.getFileType());
        // 0x00：完成
        // 0x01：需要补传
        responseMessageBody.setUploadResult((short) 0x00);
        responseMessageBody.setPackageCountToReTransmit((short) 0);
        responseMessageBody.setRetransmitItemList(Collections.emptyList());
        return responseMessageBody;
    }

    /**
     * 这里对应的是苏标附件上传的码流: 0x31326364(并不是 1078 协议中的码流)
     */
    @Jt808RequestHandlerMapping(messageIds = 0x30316364, desc = "苏标扩展码流")
    public void processMsg30316364(Jt808Request request, @Jt808RequestBody BuiltinMessage30316364 body, @Nullable Jt808Session session) {
        log.info("[0x30316364] ==> {} -- {} -- {}", body.getFileName().trim(), body.getDataOffset(), body.getDataLength());
        if (session == null) {
            log.error("[0x30316364] session == null, 附件上传之前没有没有发送 0x1210,0x1211 消息???");
            return;
        }

        final Map<String, BuiltinMessage1210.AttachmentItem> itemMap = attachmentItemCache.getIfPresent(session.terminalId());
        if (itemMap == null) {
            log.error("[0x30316364] itemMap == null, 附件上传之前没有没有发送 0x1210,0x1211 消息???");
            return;
        }

        final BuiltinMessage1210.AttachmentItem item = itemMap.get(body.getFileName().trim());
        if (item == null) {
            log.error("[0x30316364] 收到未知附件上传消息: terminalId={},{}", request.terminalId(), body);
            return;
        }

        // 写入本地文件
        this.attachmentFileService.writeDataFragmentAsync(session, body, item);
    }
}
