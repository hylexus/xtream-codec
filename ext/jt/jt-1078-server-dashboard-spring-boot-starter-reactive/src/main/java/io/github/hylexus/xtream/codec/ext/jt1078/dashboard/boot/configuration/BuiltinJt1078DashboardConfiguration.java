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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.configuration;

import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.BuiltinJt1078DashboardSessionController;
import io.github.hylexus.xtream.codec.ext.jt1078.dashboard.controller.BuiltinJt1078DashboardSubscribeController;
import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078SubscriberManager;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SessionManager;
import org.springframework.context.annotation.Bean;

public class BuiltinJt1078DashboardConfiguration {

    @Bean
    BuiltinJt1078DashboardSessionController builtinJt1078DashboardSessionController(Jt1078SessionManager sessionManager) {
        return new BuiltinJt1078DashboardSessionController(sessionManager);
    }

    @Bean
    BuiltinJt1078DashboardSubscribeController builtinJt1078DashboardSubscribeController(Jt1078SubscriberManager subscriberManager) {
        return new BuiltinJt1078DashboardSubscribeController(subscriberManager);
    }

}
