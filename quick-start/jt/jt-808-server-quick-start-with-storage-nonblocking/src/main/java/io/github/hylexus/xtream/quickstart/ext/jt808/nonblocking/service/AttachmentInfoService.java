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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo.PageableVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.dto.Jt808AlarmAttachmentInfoDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.vo.Jt808AlarmAttachmentInfoVo;
import reactor.core.publisher.Mono;

public interface AttachmentInfoService {

    Mono<Boolean> saveAlarmInfo(String terminalId, String filePath, BuiltinMessage1210.AttachmentItem attachmentItem);

    Mono<PageableVo<Jt808AlarmAttachmentInfoVo>> listAlarmAttachmentInfo(Jt808AlarmAttachmentInfoDto dto);
}
