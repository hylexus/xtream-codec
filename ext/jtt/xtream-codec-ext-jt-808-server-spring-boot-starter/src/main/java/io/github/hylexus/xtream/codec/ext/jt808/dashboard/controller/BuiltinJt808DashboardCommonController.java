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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.controller;

import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hylexus
 */
@RestController
@RequestMapping("/dashboard-api/v1")
public class BuiltinJt808DashboardCommonController {

    private final XtreamJt808ServerProperties serverProperties;

    public BuiltinJt808DashboardCommonController(XtreamJt808ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @GetMapping("/config")
    public XtreamJt808ServerProperties config() {
        return this.serverProperties;
    }
}
