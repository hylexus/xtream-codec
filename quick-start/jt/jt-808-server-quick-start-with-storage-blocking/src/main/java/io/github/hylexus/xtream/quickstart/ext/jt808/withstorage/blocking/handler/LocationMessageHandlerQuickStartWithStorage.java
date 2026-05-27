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

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request.BuiltinMessage0200;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.ServerCommonReplyMessage;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandlerMapping;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestEntity;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Jt808RequestHandler
public class LocationMessageHandlerQuickStartWithStorage {
    private static final Logger log = LoggerFactory.getLogger(LocationMessageHandlerQuickStartWithStorage.class);
    private final LocationService locationService;

    public LocationMessageHandlerQuickStartWithStorage(LocationService locationService) {
        this.locationService = locationService;
    }

    @Jt808RequestHandlerMapping(messageIds = 0x0200)
    @Jt808ResponseBody(messageId = 0x8001)
    public ServerCommonReplyMessage processMessage0200V2019(
            Jt808Session session,
            Jt808RequestEntity<BuiltinMessage0200> requestEntity) {
        log.info("0x0200: {}", requestEntity);
        final byte result = this.locationService.processLocationMessage(session, requestEntity.getBody());
        return ServerCommonReplyMessage.of(requestEntity, result);
    }

}
