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

package io.github.hylexus.xtream.debug.ext.jt808.handler;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.request.BuiltinMessage0200;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response.ServerCommonReplyMessage;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestBody;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandler;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808RequestHandlerMapping;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Request;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestEntity;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.debug.ext.jt808.message.DemoLocationMsg03;
import io.github.hylexus.xtream.debug.ext.jt808.service.DemoLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Jt808RequestHandler
public class DemoLocationMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(DemoLocationMessageHandler.class);
    private final DemoLocationService locationService;

    public DemoLocationMessageHandler(DemoLocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * 位置上报(V2019)
     * <p>
     * 7e02004086010000000001893094655200E4000000000000000101D907F2073D336C000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000FA160400000000170200001803000000EA10FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF02020000EF0400000000F31B017118000000000000000000000000000000000000000000000000567e
     */
    @Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.AUTO_DETECTION)
    @Jt808ResponseBody(messageId = 0x8001, maxPackageSize = 1000)
    public Mono<ServerCommonReplyMessage> processMessage0200V2019(
            Jt808Session session,
            Jt808Request request,
            @Jt808RequestBody BuiltinMessage0200 body,
            Jt808RequestEntity<BuiltinMessage0200> requestEntity,
            DemoLocationMsg03 message) {
        log.info("v2019-0x0200: {}", body);
        log.info("v2019-0x0200: {}", requestEntity);
        log.info("v2019-0x0200: {}", message);
        return this.locationService.processLocationMessage(session, body).map(result -> {
            // ...
            return ServerCommonReplyMessage.of(request, result);
        });
    }

    // /**
    //  * 位置上报(V2019)
    //  * <p>
    //  * 7e02004086010000000001893094655200E4000000000000000101D907F2073D336C000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000FA160400000000170200001803000000EA10FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF02020000EF0400000000F31B017118000000000000000000000000000000000000000000000000567e
    //  */
    // @Jt808RequestHandlerMapping(scheduler = "my-virtual-thread-scheduler-1", messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
    // public ServerCommonReplyMessage processMessage0200V2019(
    //         Jt808Session session,
    //         Jt808Request request,
    //         @Jt808RequestBody BuiltinMessage0200 body,
    //         Jt808RequestEntity<BuiltinMessage0200> requestEntity,
    //         DemoLocationMsg03 message) {
    //     log.info("v2019-0x0200: {}", body);
    //     log.info("v2019-0x0200: {}", requestEntity);
    //     log.info("v2019-0x0200: {}", message);
    //     log.info("v2019-0x0200: threadName={} thread={}", Thread.currentThread().getName(),Thread.currentThread());
    //     // 当前方法被调度到了 `my-virtual-thread-scheduler-1` 这个基于虚拟线程的调度器上
    //     // 所以这里可以阻塞
    //     final Byte result = this.locationService.processLocationMessage(session, body).block();
    //     return ServerCommonReplyMessage.of(request, Objects.requireNonNull(result));
    // }

    // /**
    //  * 位置上报(V2019)
    //  * <p>
    //  * 7e02004086010000000001893094655200E4000000000000000101D907F2073D336C000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000FA160400000000170200001803000000EA10FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF02020000EF0400000000F31B017118000000000000000000000000000000000000000000000000567e
    //  */
    // @Jt808RequestHandlerMapping(messageIds = 0x0200, versions = Jt808ProtocolVersion.VERSION_2019)
    // public Mono<Jt808ResponseEntity<ServerCommonReplyMessage>> processMessage0200V2019(
    //         // public Jt808ResponseEntity<ServerCommonReplyMessage> processMessage0200V2019(
    //         Jt808Session session,
    //         Jt808Request request,
    //         @Jt808RequestBody BuiltinMessage0200 body,
    //         Jt808RequestEntity<BuiltinMessage0200> requestEntity,
    //         DemoLocationMsg03 message) {
    //     log.info("v2019-0x0200: {}", body);
    //     log.info("v2019-0x0200: {}", requestEntity);
    //     log.info("v2019-0x0200: {}", message);
    //
    //     // return Mono.just(responseEntity);
    //     return this.locationService.processLocationMessage(session, body).map(result -> {
    //         // ...
    //         return Jt808ResponseEntity
    //                 .messageId(0x8001)
    //                 .maxPackageSize(1000)
    //                 .body(ServerCommonReplyMessage.of(request, (byte) 0));
    //     });
    // }
}
