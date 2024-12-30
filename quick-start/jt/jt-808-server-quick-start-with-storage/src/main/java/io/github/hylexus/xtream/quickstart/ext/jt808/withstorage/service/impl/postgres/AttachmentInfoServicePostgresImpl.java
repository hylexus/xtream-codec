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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.postgres;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.domain.converter.Jt808EntityConverter;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.domain.entity.Jt808AlarmAttachmentInfoEntity;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.mapper.postgres.Jt808AlarmAttachmentInfoMapperPostgres;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.AttachmentInfoService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.utils.DatabaseRouter;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
public class AttachmentInfoServicePostgresImpl implements AttachmentInfoService {
    private final Jt808AlarmAttachmentInfoMapperPostgres mapper;

    public AttachmentInfoServicePostgresImpl(Jt808AlarmAttachmentInfoMapperPostgres mapper) {
        this.mapper = mapper;
    }

    @Override
    public Mono<Boolean> saveAlarmInfo(String terminalId, String filePath, BuiltinMessage1210.AttachmentItem attachmentItem) {
        final Jt808AlarmAttachmentInfoEntity entity = Jt808EntityConverter.toAlarmInfoEntity(terminalId, filePath, attachmentItem);
        return DatabaseRouter.TRACE_LOG_POSTGRES.executeMono(this.mapper.insert(entity))
                .then(Mono.just(true));
    }

}
