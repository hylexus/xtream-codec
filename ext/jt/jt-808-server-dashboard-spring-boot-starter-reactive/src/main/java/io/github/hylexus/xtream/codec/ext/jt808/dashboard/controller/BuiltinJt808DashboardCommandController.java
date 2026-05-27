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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller;

import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpException;
import io.github.hylexus.xtream.codec.common.utils.FormatUtils;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.jt1078.BuiltinMessage9101;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.jt1078.BuiltinMessage9102;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.command.DashboardCommand9101Dto;
import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.dto.command.DashboardCommand9102Dto;
import io.github.hylexus.xtream.codec.ext.jt808.exception.Jt808SessionNotFoundException;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.Jt808CommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * @author hylexus
 */
@RestController
@RequestMapping("/dashboard-api/jt808/v1/command")
public class BuiltinJt808DashboardCommandController {
    private static final Logger log = LoggerFactory.getLogger(BuiltinJt808DashboardCommandController.class);
    private final Jt808CommandSender commandSender;

    public BuiltinJt808DashboardCommandController(Jt808CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    @PostMapping("/9102")
    public Mono<Object> command9102(@Validated @RequestBody DashboardCommand9102Dto dto) {
        final BuiltinMessage9102 command = new BuiltinMessage9102()
                .setChannelNumber(dto.getChannelNumber().shortValue())
                .setCommand(dto.getCommand().shortValue())
                .setMediaTypeToClose(dto.getMediaTypeToClose().shortValue())
                .setStreamType(dto.getStreamType().shortValue());

        return this.sendCommandAndWaitingResponse(dto.getSim(), 0x9102, command, dto.getTimeout());
    }

    @PostMapping("/9101")
    public Mono<Object> command9101(@Validated @RequestBody DashboardCommand9101Dto dto) {
        final BuiltinMessage9101 command = new BuiltinMessage9101()
                .setServerIp(dto.getJt1078ServerIp())
                .setServerPortTcp(dto.getJt1078ServerPortTcp())
                .setServerPortUdp(dto.getJt1078ServerPortUdp())
                .setChannelNumber(dto.getChannelNumber().shortValue())
                .setDataType(dto.getDataType().shortValue())
                .setStreamType(dto.getStreamType().shortValue());

        return this.sendCommandAndWaitingResponse(dto.getSim(), 0x9101, command, dto.getTimeout());
    }

    <R> Mono<R> sendCommandAndWaitingResponse(String sim, int messageId, Object command, Duration timeout) {
        return this.commandSender.<R>sendObjectAndWaitingResponseWithTerminalId(sim, Mono.just(command), messageId)
                .timeout(timeout)
                .doOnNext(response -> {
                    // ...
                    log.info("{}(0x{}) response: {}", messageId, FormatUtils.toHexString(messageId, 4), response);
                })
                .onErrorResume(Jt808SessionNotFoundException.class, e -> {
                    log.error("Jt808Session not found with terminalId: [{}]", sim);
                    return Mono.error(XtreamHttpException.badRequest("No Jt808Session found with terminalId: [" + sim + "]"));
                })
                .onErrorResume(TimeoutException.class, e -> {
                    log.error("Command {}(0x{}) response timeout({})", messageId, FormatUtils.toHexString(messageId, 4), timeout);
                    return Mono.error(XtreamHttpException.timeout("Command 0x9101 response timeout(" + timeout + ")"));
                });
    }

}
