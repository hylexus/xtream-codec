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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage9212;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1211;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1212;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage30316364;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.ServerCommonReplyMessage;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestBody;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandlerMapping;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestEntity;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ServerType;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.AttachmentFileService;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
    public Mono<ServerCommonReplyMessage> processMsg0x1210(Jt808RequestEntity<BuiltinMessage1210> requestEntity) {
        final BuiltinMessage1210 body = requestEntity.getBody();
        log.info("0x1210 ==> {}", body);
        if (requestEntity.getServerType() == Jt808ServerType.INSTRUCTION_SERVER) {
            log.error("{}", "0x1210 不应该由指令服务器对应的端口处理");
        }
        final Map<String, BuiltinMessage1210.AttachmentItem> itemMap = this.attachmentItemCache.get(requestEntity.getHeader().terminalId(), key -> new ConcurrentHashMap<>());
        for (final BuiltinMessage1210.AttachmentItem item : body.getAttachmentItemList()) {
            item.setGroup(body);
            itemMap.put(item.getFileName().trim(), item);
        }
        return Mono.just(ServerCommonReplyMessage.success(requestEntity));
    }

    @Jt808RequestHandlerMapping(messageIds = 0x1211)
    @Jt808ResponseBody(messageId = 0x8001)
    public Mono<ServerCommonReplyMessage> processMsg0x1211(Jt808RequestEntity<BuiltinMessage1211> requestEntity) {
        log.info("0x1211 ==> {}", requestEntity.getBody());
        if (requestEntity.getServerType() == Jt808ServerType.INSTRUCTION_SERVER) {
            log.error("{}", "0x1211 不应该由指令服务器对应的端口处理");
        }
        final Map<String, BuiltinMessage1210.AttachmentItem> items = this.attachmentItemCache.getIfPresent(requestEntity.getHeader().terminalId());
        if (items == null) {
            return Mono.just(ServerCommonReplyMessage.success(requestEntity));
        }
        final BuiltinMessage1210.AttachmentItem attachmentItem = items.get(requestEntity.getBody().getFileName().trim());
        if (attachmentItem != null) {
            attachmentItem.setFileType(requestEntity.getBody().getFileType());
        }
        return Mono.just(ServerCommonReplyMessage.success(requestEntity));
    }

    @Jt808RequestHandlerMapping(messageIds = 0x1212)
    @Jt808ResponseBody(messageId = 0x9212)
    public Mono<BuiltinMessage9212> processMsg0x1212(Jt808RequestEntity<BuiltinMessage1212> requestEntity) {
        final BuiltinMessage1212 body = requestEntity.getBody();
        log.info("0x1212 ==> {}", body);
        if (requestEntity.getServerType() == Jt808ServerType.INSTRUCTION_SERVER) {
            log.error("{}", "0x1212 不应该由指令服务器对应的端口处理");
        }
        final ConcurrentMap<String, BuiltinMessage1210.AttachmentItem> items = this.attachmentItemCache.getIfPresent(requestEntity.getHeader().terminalId());
        // 这里可能会出现空指针(示例项目里不做处理，你自己看情况处理)
        assert items != null;
        final BuiltinMessage1210.AttachmentItem attachmentItem = items.get(body.getFileName());
        final boolean retainLocalTemporaryFile = this.quickStartAppProps.getAttachmentServer().isRetainLocalTemporaryFile();
        return this.attachmentFileService.moveFileToRemoteStorage(requestEntity, attachmentItem, !retainLocalTemporaryFile)
                .onErrorResume(Throwable.class, throwable -> {
                    log.error("Error occurred while moveFileToRemoteStorage", throwable);
                    return Mono.just(false);
                })
                .then(Mono.defer(() -> {
                    final BuiltinMessage9212 responseMessageBody = new BuiltinMessage9212();
                    responseMessageBody.setFileName(body.getFileName());
                    responseMessageBody.setFileType(body.getFileType());
                    // 0x00：完成
                    // 0x01：需要补传
                    responseMessageBody.setUploadResult((short) 0x00);
                    responseMessageBody.setPackageCountToReTransmit((short) 0);
                    responseMessageBody.setRetransmitItemList(Collections.emptyList());
                    return Mono.just(responseMessageBody);
                }))
                .doFinally(signalType -> {
                    final String filename = body.getFileName().trim();
                    log.info("0x1212 ==> 删除缓存 {}", filename);
                    items.remove(filename);
                });
    }

    /**
     * 这里对应的是苏标附件上传的码流: 0x31326364(并不是 1078 协议中的码流)
     */
    @Jt808RequestHandlerMapping(messageIds = 0x30316364, desc = "苏标扩展码流")
    public Mono<Void> processMsg30316364(Jt808Request request, @Jt808RequestBody BuiltinMessage30316364 body, @Nullable Jt808Session session) {
        if (session == null) {
            log.warn("session == null, 附件上传之前没有没有发送 0x1210,0x1211 消息???");
            return Mono.empty();
        }

        log.info("0x30316364 ==> {} -- {} -- {}", body.getFileName().trim(), body.getDataOffset(), body.getDataLength());

        final Map<String, BuiltinMessage1210.AttachmentItem> itemMap = attachmentItemCache.getIfPresent(session.terminalId());
        if (itemMap == null) {
            return Mono.empty();
        }

        final BuiltinMessage1210.AttachmentItem item = itemMap.get(body.getFileName().trim());
        if (item == null) {
            log.error("收到未知附件上传消息: {}", body);
            return Mono.empty();
        }

        // 写入本地文件
        return this.attachmentFileService.writeDataFragmentAsync(session, body, item)
                .flatMap(dataSize -> {
                    // ...
                    return Mono.empty();
                });
    }
}
