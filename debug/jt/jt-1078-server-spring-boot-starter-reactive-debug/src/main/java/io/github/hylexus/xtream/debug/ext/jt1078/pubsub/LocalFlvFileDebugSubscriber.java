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

package io.github.hylexus.xtream.debug.ext.jt1078.pubsub;

import io.github.hylexus.xtream.codec.common.utils.XtreamFiles;
import io.github.hylexus.xtream.codec.common.utils.XtreamResources;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078RequestPublisher;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.impl.H264Jt1078SubscriberCreator;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

// @Component
public class LocalFlvFileDebugSubscriber implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(LocalFlvFileDebugSubscriber.class);
    private final Jt1078RequestPublisher publisher;
    private final Map<String, OutputStream> registry = new HashMap<>();
    private final String targetDir;

    public LocalFlvFileDebugSubscriber(Jt1078RequestPublisher publisher) {
        this.publisher = publisher;
        this.targetDir = "/tmp/jtt/";
        XtreamFiles.mkdirs(this.targetDir);
    }

    public void subscribe(Jt1078Session session) {
        new Thread(() -> {
            log.info("start subscribe");
            final String sim = session.rawSim();
            final short channelNumber = session.channelNumber();
            final H264Jt1078SubscriberCreator creator = H264Jt1078SubscriberCreator.builder()
                    .rawSim(sim)
                    .channelNumber(channelNumber)
                    .timeout(Duration.ofMinutes(30))
                    .h264Meta(new H264Jt1078SubscriberCreator.H264Meta(1 << 18))
                    .build();

            final OutputStream outputStream = getOutputStream(sim, channelNumber);
            this.publisher.subscribeH264ToFlvStream(creator)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(subscription -> {
                        // log.info("type: {}", subscription.type());
                        try {
                            outputStream.write((byte[]) subscription.payload());
                            outputStream.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .doOnError(TimeoutException.class, e -> {
                        log.error("取消订阅(超时)");
                    })
                    .doFinally(signalType -> {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .subscribe();
        }).start();
    }

    public void unsubscribe(Jt1078Session session) {
    }

    private synchronized OutputStream getOutputStream(String sim, short channel) {
        final String key = key(sim, channel);
        final OutputStream metadata = registry.get(key);
        if (metadata != null) {
            XtreamResources.close(metadata);
        }

        final OutputStream newOutputStream = this.createNewOutputStream(sim, key);
        this.registry.put(key, newOutputStream);
        return newOutputStream;
    }

    private OutputStream createNewOutputStream(String sim, String key) {
        final String fileName = this.targetDir
                                + File.separator + sim
                                + File.separator + key
                                + File.separator + (DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()))
                                + ".flv";
        try {
            XtreamFiles.mkdirs(new File(fileName).getParent());
            return new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    String key(String sim, short channel) {
        return sim + "-" + channel;
    }

    @Override
    public void destroy() throws Exception {
        this.registry.forEach((k, v) -> {
            // ...
            XtreamResources.close(v);
        });
    }

}
