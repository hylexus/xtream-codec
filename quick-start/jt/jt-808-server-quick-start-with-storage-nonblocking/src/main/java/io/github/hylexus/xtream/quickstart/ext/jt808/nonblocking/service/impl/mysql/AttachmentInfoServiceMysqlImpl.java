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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.impl.mysql;

import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.converter.Jt808EntityConverter;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.dto.Jt808AlarmAttachmentInfoDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.entity.Jt808AlarmAttachmentInfoEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.vo.Jt808AlarmAttachmentInfoVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.mysql.Jt808AlarmAttachmentInfoMapperMysql;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.AttachmentInfoService;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.utils.DatabaseRouter;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author hylexus
 */
public class AttachmentInfoServiceMysqlImpl implements AttachmentInfoService {
    private final Jt808AlarmAttachmentInfoMapperMysql mapper;
    private final String previewBaseUrl;

    public AttachmentInfoServiceMysqlImpl(
            Jt808AlarmAttachmentInfoMapperMysql mapper,
            String previewBaseUrl) {
        this.mapper = mapper;
        this.previewBaseUrl = previewBaseUrl;
    }

    @Override
    public Mono<Boolean> saveAlarmInfo(String terminalId, String filePath, BuiltinMessage1210.AttachmentItem attachmentItem) {
        final Jt808AlarmAttachmentInfoEntity entity = Jt808EntityConverter.toAlarmInfoEntity(terminalId, filePath, attachmentItem);
        return DatabaseRouter.TRACE_LOG_MYSQL.executeMono(this.mapper.insert(entity))
                .then(Mono.just(true));
    }

    @Override
    public Mono<PageableVo<Jt808AlarmAttachmentInfoVo>> listAlarmAttachmentInfo(Jt808AlarmAttachmentInfoDto dto) {
        return DatabaseRouter.TRACE_LOG_MYSQL.executeMono(this.mapper.count(dto)).flatMap(total -> {
            if (total <= 0) {
                return Mono.just(PageableVo.empty());
            }
            return DatabaseRouter.TRACE_LOG_MYSQL.executeFlux(this.mapper.list(dto)).collectList().map(list -> {
                final List<Jt808AlarmAttachmentInfoVo> records = list.stream().map(entity -> {
                    final Jt808AlarmAttachmentInfoVo vo = Jt808EntityConverter.armInfoEntityToVo(entity);
                    vo.setPreviewUrl(this.previewBaseUrl + "/" + entity.getFilePath());
                    return vo;
                }).toList();
                return PageableVo.of(total, records);
            });
        });
    }

}
