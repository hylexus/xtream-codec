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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.mapper.mysql;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.dto.Jt808AlarmAttachmentInfoDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.entity.Jt808AlarmAttachmentInfoEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author hylexus
 */
@Mapper
public interface Jt808AlarmAttachmentInfoMapperMysql {

    @Insert("""
            insert into jt_808_alarm_attachment_info (id, terminal_id, file_name, file_type, file_size, file_path,
                                                      alarm_no, alarm_time, alarm_sequence, attachment_count, client_id,
                                                      created_at)
            values (#{id}, #{terminalId}, #{fileName}, #{fileType}, #{fileSize}, #{filePath},
                    #{alarmNo}, #{alarmTime}, #{alarmSequence}, #{attachmentCount}, #{clientId},
                    #{createdAt})
            """)
    Mono<Void> insert(Jt808AlarmAttachmentInfoEntity entity);

    Mono<Long> count(@Param("dto") Jt808AlarmAttachmentInfoDto dto);

    Flux<Jt808AlarmAttachmentInfoEntity> list(@Param("dto") Jt808AlarmAttachmentInfoDto dto);
}
