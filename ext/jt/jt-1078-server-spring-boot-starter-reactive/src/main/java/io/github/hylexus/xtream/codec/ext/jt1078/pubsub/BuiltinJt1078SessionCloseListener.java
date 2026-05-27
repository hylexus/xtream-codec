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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub;

import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078Session;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionDestroyException;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuiltinJt1078SessionCloseListener implements Jt1078SessionEventListener {

    private static final Logger log = LoggerFactory.getLogger(BuiltinJt1078SessionCloseListener.class);
    private final Jt1078RequestPublisher publisher;

    public BuiltinJt1078SessionCloseListener(Jt1078RequestPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void beforeSessionClose(Jt1078Session session, SessionCloseReason reason) {
        log.info("Unsubscribe [{}/{}], because [{}]", session.convertedSim(), session.channelNumber(), reason);
        this.publisher.unsubscribeWithSimAndChannelNumber(session.convertedSim(), session.channelNumber(), new Jt1078SessionDestroyException("session closed"));
    }

}
