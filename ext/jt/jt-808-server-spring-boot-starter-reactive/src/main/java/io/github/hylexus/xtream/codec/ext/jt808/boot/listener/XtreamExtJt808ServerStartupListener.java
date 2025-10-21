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

package io.github.hylexus.xtream.codec.ext.jt808.boot.listener;

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.tcp.TcpXtreamServer;
import io.github.hylexus.xtream.codec.server.reactive.spec.impl.udp.UdpXtreamServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class XtreamExtJt808ServerStartupListener implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(XtreamExtJt808ServerStartupListener.class);
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final XtreamJt808ServerProperties serverProps;
    @SuppressWarnings("NullAway.Init")
    private ApplicationContext applicationContext;

    public XtreamExtJt808ServerStartupListener(XtreamJt808ServerProperties serverProps) {
        this.serverProps = serverProps;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final boolean tcpServerEnabled = serverProps.getInstructionServer().getTcpServer().isEnabled() || serverProps.getAttachmentServer().getTcpServer().isEnabled();
        final boolean udpServerEnabled = serverProps.getInstructionServer().getUdpServer().isEnabled() || serverProps.getAttachmentServer().getUdpServer().isEnabled();

        if (!tcpServerEnabled && !udpServerEnabled) {
            log.error("Both tcpServer and udpServer are disabled, please enable one of them.");
            return;
        }

        if (initialized.compareAndSet(false, true) && event.getApplicationContext().getParent() == null) {
            if (tcpServerEnabled) {
                final Map<String, TcpXtreamServer> servers = applicationContext.getBeansOfType(TcpXtreamServer.class);
                servers.forEach((name, tcpServer) -> {
                    // ...
                    tcpServer.start();
                });
            }

            if (udpServerEnabled) {
                final Map<String, UdpXtreamServer> servers = applicationContext.getBeansOfType(UdpXtreamServer.class);
                servers.forEach((name, udpServer) -> {
                    // ...
                    udpServer.start();
                });
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
