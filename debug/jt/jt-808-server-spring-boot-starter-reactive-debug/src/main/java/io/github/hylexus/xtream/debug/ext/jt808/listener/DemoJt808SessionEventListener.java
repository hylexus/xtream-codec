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

package io.github.hylexus.xtream.debug.ext.jt808.listener;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808SessionEventListener;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@NullMarked
@Component
public class DemoJt808SessionEventListener implements Jt808SessionEventListener {
    private static final Logger log = LoggerFactory.getLogger(DemoJt808SessionEventListener.class);

    @Override
    public void afterSessionCreate(Jt808Session session) {
        log.info("Session 创建: {}", session);
    }

    @Override
    public void beforeSessionClose(Jt808Session session, SessionCloseReason reason) {
        log.info("Session 销毁. 原因: {}; Session: {}", reason, session);
    }
}
