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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.configuration;

import cn.mybatis.mp.core.mybatis.configuration.MybatisConfiguration;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.values.Jt808NetType;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.values.Jt808Version;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.adapter.Jt808NetTypeHandlerAdapter;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.mapper.adapter.Jt808VersionHandlerAdapter;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.stereotype.Component;

@Component
public class MybatisConfigurationCustomizer implements ConfigurationCustomizer {

    @Override
    public void customize(MybatisConfiguration configuration) {
        final TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        typeHandlerRegistry.register(Jt808NetType.class, Jt808NetTypeHandlerAdapter.class);
        typeHandlerRegistry.register(Jt808Version.class, Jt808VersionHandlerAdapter.class);
    }

}
