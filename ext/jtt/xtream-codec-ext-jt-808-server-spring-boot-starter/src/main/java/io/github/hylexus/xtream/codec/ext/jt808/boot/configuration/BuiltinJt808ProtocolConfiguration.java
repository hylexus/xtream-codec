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

package io.github.hylexus.xtream.codec.ext.jt808.boot.configuration;

import io.github.hylexus.xtream.codec.common.utils.BufferFactoryHolder;
import io.github.hylexus.xtream.codec.core.EntityCodec;
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808BytesProcessor;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestCombiner;
import io.github.hylexus.xtream.codec.ext.jt808.codec.Jt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.DefaultJt808BytesProcessor;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.DefaultJt808RequestCombiner;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.DefaultJt808RequestDecoder;
import io.github.hylexus.xtream.codec.ext.jt808.codec.impl.DefaultJt808ResponseEncoder;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808FlowIdGenerator;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808MessageEncryptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class BuiltinJt808ProtocolConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Jt808FlowIdGenerator jt808FlowIdGenerator() {
        return Jt808FlowIdGenerator.DEFAULT;
    }

    @Bean
    @ConditionalOnMissingBean
    Jt808BytesProcessor jt808BytesProcessor(BufferFactoryHolder bufferFactoryHolder) {
        return new DefaultJt808BytesProcessor(bufferFactoryHolder.getAllocator());
    }

    @Bean
    @ConditionalOnMissingBean
    Jt808RequestDecoder jt808RequestDecoder(Jt808BytesProcessor jt808BytesProcessor) {
        return new DefaultJt808RequestDecoder(jt808BytesProcessor);
    }

    @Bean
    @ConditionalOnMissingBean
    Jt808RequestCombiner jt808RequestCombiner(BufferFactoryHolder bufferFactoryHolder, XtreamJt808ServerProperties properties) {
        final XtreamJt808ServerProperties.RequestSubPackageStorage subPackageStorage = properties.getRequestSubPackageStorage();
        return new DefaultJt808RequestCombiner(bufferFactoryHolder.getAllocator(), subPackageStorage.getMaximumSize(), subPackageStorage.getTtl());
    }

    @Bean
    @ConditionalOnMissingBean
    Jt808MessageEncryptionHandler jt808MessageEncryptionHandler() {
        return new Jt808MessageEncryptionHandler.NoOps();
    }

    @Bean
    @ConditionalOnMissingBean
    DefaultJt808ResponseEncoder jt808ResponseEncoder(
            BufferFactoryHolder factoryHolder,
            Jt808FlowIdGenerator flowIdGenerator,
            EntityCodec entityCodec,
            Jt808BytesProcessor jt808BytesProcessor,
            Jt808MessageEncryptionHandler encryptionHandler) {

        return new DefaultJt808ResponseEncoder(
                factoryHolder.getAllocator(),
                flowIdGenerator,
                entityCodec,
                jt808BytesProcessor,
                encryptionHandler
        );
    }
}
