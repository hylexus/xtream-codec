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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.controller;

import io.github.hylexus.xtream.codec.ext.jt808.boot.condition.ConditionalOnJt808Server;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.BuiltinMessage8104;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808CommandSender;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.domain.dto.Command9104Dto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@ConditionalOnJt808Server(serverType = ConditionalOnJt808Server.ServerType.INSTRUCTION_SERVER,protocolType = ConditionalOnJt808Server.ProtocolType.ANY)
@RestController
@RequestMapping("/api/jt-808-quick-start-with-storage/v1/command")
public class DemoCommandController {
    private static final Logger log = LoggerFactory.getLogger(DemoCommandController.class);
    private final Jt808CommandSender jt808CommandSender;

    public DemoCommandController(Jt808CommandSender jt808CommandSender) {
        this.jt808CommandSender = jt808CommandSender;
    }

    @PostMapping("/8104")
    public Mono<Object> command8104(@RequestBody Command9104Dto dto) {
        return this.jt808CommandSender.sendObjectAndWaitingResponse(
                        dto.getSessionId(),
                        Mono.just(new BuiltinMessage8104()),
                        0x0104
                )
                .timeout(dto.getTimeout())
                .doOnNext(response -> {
                    // ...
                    log.info("0x8104 response: {}", response);
                });
    }

}
