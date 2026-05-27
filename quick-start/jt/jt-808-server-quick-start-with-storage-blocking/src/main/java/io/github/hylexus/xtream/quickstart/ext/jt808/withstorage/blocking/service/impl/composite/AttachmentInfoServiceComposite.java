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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.composite;

import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.dto.Jt808AlarmAttachmentInfoDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808AlarmAttachmentInfoVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.AttachmentInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author hylexus
 */
public class AttachmentInfoServiceComposite implements AttachmentInfoService {
    private static final Logger log = LoggerFactory.getLogger(AttachmentInfoServiceComposite.class);
    private final List<AttachmentInfoService> delegates;

    public AttachmentInfoServiceComposite(List<AttachmentInfoService> delegates) {
        this.delegates = delegates;
    }

    @Override
    public boolean saveAlarmInfo(String terminalId, String filePath, BuiltinMessage1210.AttachmentItem attachmentItem) {
        for (final AttachmentInfoService service : this.delegates) {
            try {
                service.saveAlarmInfo(terminalId, filePath, attachmentItem);
            } catch (Throwable e) {
                log.error("error occurred while saveAlarmInfo", e);
            }
        }
        return true;
    }

    @Override
    public PageableVo<Jt808AlarmAttachmentInfoVo> listAlarmAttachmentInfo(Jt808AlarmAttachmentInfoDto dto) {
        throw new UnsupportedOperationException();
    }

}
