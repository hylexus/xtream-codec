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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.utils;

import io.github.hylexus.xtream.codec.base.web.exception.XtreamBadRequestException;
import io.github.hylexus.xtream.codec.common.utils.Numbers;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto.Jt1078VideoStreamSubscriberDto;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.H264Jt1078SubscriberCreator;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Jt1078DashboardUtils {

    private Jt1078DashboardUtils() {
        throw new UnsupportedOperationException();
    }

    public static Jt1078VideoStreamSubscriberDto parseJt1078VideoStreamSubscriberDto(WebSocketSession session, UriTemplate uriTemplate) {
        final URI uri = session.getHandshakeInfo().getUri();
        final Map<String, String> values = uriTemplate.match(uri.getPath());
        final String sim = values.get("sim");
        if (!StringUtils.hasText(sim)) {
            throw new XtreamBadRequestException("path param `sim` is missing");
        }

        final short channel = Numbers.parseInteger(values.get("channel"))
                .orElseThrow(() -> new XtreamBadRequestException("path param `channel` is missing"))
                .shortValue();

        final String queryString = uri.getQuery();
        if (!StringUtils.hasText(queryString)) {
            return new Jt1078VideoStreamSubscriberDto().setSim(sim).setChannel(channel);
        }

        final String[] arrays = queryString.split("&");
        final Map<String, String> params = new HashMap<>();
        for (final String item : arrays) {
            final String[] split = item.split("=");
            if (split.length == 2) {
                params.put(split[0], split[1]);
            }
        }

        return new Jt1078VideoStreamSubscriberDto()
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

    public static Jt1078VideoStreamSubscriberDto parseJt1078VideoStreamSubscriberDto(org.springframework.web.socket.WebSocketSession session, UriTemplate uriTemplate) {
        final URI uri = session.getUri();
        final Map<String, String> values = uriTemplate.match(uri.getPath());
        final String sim = values.get("sim");
        if (!StringUtils.hasText(sim)) {
            throw new XtreamBadRequestException("path param `sim` is missing");
        }

        final short channel = Numbers.parseInteger(values.get("channel"))
                .orElseThrow(() -> new XtreamBadRequestException("path param `channel` is missing"))
                .shortValue();

        final String queryString = uri.getQuery();
        if (!StringUtils.hasText(queryString)) {
            return new Jt1078VideoStreamSubscriberDto().setSim(sim).setChannel(channel);
        }

        final String[] arrays = queryString.split("&");
        final Map<String, String> params = new HashMap<>();
        for (final String item : arrays) {
            final String[] split = item.split("=");
            if (split.length == 2) {
                params.put(split[0], split[1]);
            }
        }

        return new Jt1078VideoStreamSubscriberDto()
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

    public static H264Jt1078SubscriberCreator toH264Jt1078SubscriberCreator(Jt1078VideoStreamSubscriberDto params, Map<String, Object> metadata) {
        if (metadata == null) {
            metadata = new LinkedHashMap<>();
        }
        metadata.put("source", "Dashboard");
        return H264Jt1078SubscriberCreator.builder()
                .rawSim(params.getSim())
                .channelNumber(params.getChannel())
                .timeout(Duration.ofSeconds(params.getTimeout()))
                .h264Meta(new H264Jt1078SubscriberCreator.H264Meta(params.getNaluDecoderRingBufferSize()))
                .hasAudio(params.isHasAudio())
                .hasVideo(params.isHasVideo())
                .metadata(metadata)
                .desc("Created By Dashboard Api")
                .build();
    }

}
