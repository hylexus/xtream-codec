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

package io.github.hylexus.xtream.debug.ext.jt1078.utils;

import io.github.hylexus.xtream.codec.common.utils.Numbers;
import io.github.hylexus.xtream.debug.ext.jt1078.model.dto.DemoVideoStreamSubscriberDto;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class WebSocketUtils {
    public static DemoVideoStreamSubscriberDto createForReactiveSession(WebSocketSession session, UriTemplate uriTemplate) {
        final URI uri = session.getHandshakeInfo().getUri();
        final Map<String, String> values = uriTemplate.match(uri.getPath());
        final String sim = values.getOrDefault("sim", "018930946552");
        final short channel = Numbers.parseInteger(values.get("channel")).orElseThrow().shortValue();
        final String query = uri.getQuery();
        if (!StringUtils.hasText(query)) {
            return new DemoVideoStreamSubscriberDto().setSim(sim).setChannel(channel);
        }

        final String[] arrays = query.split("&");
        final Map<String, String> params = new HashMap<>();
        for (final String item : arrays) {
            final String[] split = item.split("=");
            if (split.length == 2) {
                params.put(split[0], split[1]);
            }
        }

        return new DemoVideoStreamSubscriberDto()
                .setSim(sim)
                .setChannel(channel)
                .setTimeout(Numbers.parseInteger(params.get("timeout")).orElse(10))
                .setByteArrayAsBase64(Numbers.parseBoolean(params.get("byteArrayAsBase64")).orElse(false))
                .setNaluDecoderRingBufferSize(Numbers.parseInteger(params.get("naluDecoderRingBufferSize")).orElse(1 << 18))
                .setNaluDecoderRingBufferSize(Numbers.parseInteger(params.get("naluDecoderRingBufferSize")).orElse(1 << 18))
                .setHasAudio(Numbers.parseBoolean(params.get("hasAudio")).orElse(true))
                .setHasVideo(Numbers.parseBoolean(params.get("hasVideo")).orElse(true))
                ;

    }
}
