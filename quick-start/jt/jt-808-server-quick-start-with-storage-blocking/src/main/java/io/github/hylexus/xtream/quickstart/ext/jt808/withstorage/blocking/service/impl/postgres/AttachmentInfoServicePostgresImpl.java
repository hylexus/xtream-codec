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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.postgres;

import cn.mybatis.mp.datasource.routing.DS;
import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.converter.Jt808EntityConverter;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.dto.Jt808AlarmAttachmentInfoDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.entity.Jt808AlarmAttachmentInfoEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808AlarmAttachmentInfoVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.postgres.Jt808AlarmAttachmentInfoMapperPostgres;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.AttachmentInfoService;

import java.util.List;

@DS("trace-log-postgres")
public class AttachmentInfoServicePostgresImpl implements AttachmentInfoService {
    private final Jt808AlarmAttachmentInfoMapperPostgres mapper;
    private final String previewBaseUrl;

    public AttachmentInfoServicePostgresImpl(
            Jt808AlarmAttachmentInfoMapperPostgres mapper,
            String previewBaseUrl) {
        this.mapper = mapper;
        this.previewBaseUrl = previewBaseUrl;
    }

    @Override
    public boolean saveAlarmInfo(String terminalId, String filePath, BuiltinMessage1210.AttachmentItem attachmentItem) {
        final Jt808AlarmAttachmentInfoEntity entity = Jt808EntityConverter.toAlarmInfoEntity(terminalId, filePath, attachmentItem);
        this.mapper.insert(entity);
        return true;
    }

    @Override
    public PageableVo<Jt808AlarmAttachmentInfoVo> listAlarmAttachmentInfo(Jt808AlarmAttachmentInfoDto dto) {
        // noinspection Duplicates
        final long total = this.mapper.count(dto);
        if (total <= 0) {
            return PageableVo.empty();
        }

        final List<Jt808AlarmAttachmentInfoVo> records = this.mapper.list(dto).stream().map(entity -> {
            final Jt808AlarmAttachmentInfoVo vo = Jt808EntityConverter.armInfoEntityToVo(entity);
            vo.setPreviewUrl(this.previewBaseUrl + "/" + entity.getFilePath());
            return vo;
        }).toList();

        return PageableVo.of(total, records);
    }

}
