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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.controller;

import io.github.hylexus.xtream.codec.base.web.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.base.web.exception.XtreamBadRequestException;
import io.github.hylexus.xtream.codec.ext.jt808.boot.properties.XtreamJt808ServerProperties;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.dto.Jt808AlarmAttachmentInfoDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.dto.Jt808TraceLogDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808AlarmAttachmentInfoVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.domain.vo.Jt808TraceLogVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.AttachmentInfoService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.TraceLogService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.clickhouse.AttachmentInfoServiceClickhouseImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.clickhouse.TraceLogServiceClickhouseImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.mysql.AttachmentInfoServiceMysqlImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.mysql.TraceLogServiceMysqlImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.postgres.AttachmentInfoServicePostgresImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service.impl.postgres.TraceLogServicePostgresImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.utils.DemoNetUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jt-808-quick-start-with-storage/v1/data-query")
public class DemoQueryController implements InitializingBean {
    private final Map<String, AttachmentInfoService> attachmentInfoServiceRoutes = new HashMap<>();
    private final Map<String, TraceLogService> traceLogServiceRoutes = new HashMap<>();
    private final QuickStartAppProps appProps;
    private final ApplicationContext applicationContext;
    private final int port;
    private final XtreamJt808ServerProperties jt808ServerProperties;
    private final WebApplicationType webApplicationType;
    private final List<String> availableIpAddresses;

    public DemoQueryController(QuickStartAppProps appProps, ApplicationContext applicationContext, @Value("${server.port}") int port, XtreamJt808ServerProperties jt808ServerProperties) {
        this.appProps = appProps;
        this.applicationContext = applicationContext;
        this.port = port;
        this.jt808ServerProperties = jt808ServerProperties;
        this.webApplicationType = applicationContext instanceof ReactiveWebApplicationContext ? WebApplicationType.REACTIVE : WebApplicationType.SERVLET;
        this.availableIpAddresses = DemoNetUtils.getLocalIpAddressesOrEmpty();
    }

    @GetMapping("/server-config")
    public Map<String, Object> serverConfig() {
        final QuickStartAppProps.DatabaseControl databaseControl = appProps.getFeatureControl().getDatabase();
        final List<Map<String, Object>> databaseList = List.of(
                Map.of("label", "Clickhouse", "value", "clickhouse", "enabled", databaseControl.getClickhouse().isEnabled()),
                Map.of("label", "Postgres", "value", "postgres", "enabled", databaseControl.getPostgres().isEnabled()),
                Map.of("label", "Mysql", "value", "mysql", "enabled", databaseControl.getMysql().isEnabled())
        );
        return Map.of(
                "server", Map.of("port", this.port, "type", this.webApplicationType, "availableIpAddresses", this.availableIpAddresses),
                "database", databaseList,
                "oss", List.of(Map.of("label", "Minio", "value", "minio", "enabled", appProps.getFeatureControl().getOss().getMinio().isEnabled())),
                "jt808", jt808ServerProperties
        );
    }

    @GetMapping("/attachment-info")
    public PageableVo<Jt808AlarmAttachmentInfoVo> alarmInfo(@Validated Jt808AlarmAttachmentInfoDto dto) {
        final AttachmentInfoService service = this.attachmentInfoServiceRoutes.get(dto.getSt());
        if (service == null) {
            throw new XtreamBadRequestException("`st` is invalid");
        }
        return service.listAlarmAttachmentInfo(dto);
    }

    @GetMapping("/trace-log")
    public PageableVo<Jt808TraceLogVo> alarmInfo(@Validated Jt808TraceLogDto dto) {
        final TraceLogService service = this.traceLogServiceRoutes.get(dto.getSt());
        if (service == null) {
            throw new XtreamBadRequestException("`st` is invalid");
        }
        return service.listTraceLog(dto);
    }

    @Override
    public void afterPropertiesSet() {
        applicationContext.getBeansOfType(AttachmentInfoService.class).forEach((ignored, service) -> {
            switch (service) {
                case AttachmentInfoServiceClickhouseImpl clickhouse -> this.attachmentInfoServiceRoutes.put("clickhouse", clickhouse);
                case AttachmentInfoServiceMysqlImpl mysql -> this.attachmentInfoServiceRoutes.put("mysql", mysql);
                case AttachmentInfoServicePostgresImpl postgres -> this.attachmentInfoServiceRoutes.put("postgres", postgres);
                default -> {
                    // ignored
                }
            }
        });
        applicationContext.getBeansOfType(TraceLogService.class).forEach((ignored, service) -> {
            switch (service) {
                case TraceLogServiceClickhouseImpl clickhouse -> this.traceLogServiceRoutes.put("clickhouse", clickhouse);
                case TraceLogServiceMysqlImpl mysql -> this.traceLogServiceRoutes.put("mysql", mysql);
                case TraceLogServicePostgresImpl postgres -> this.traceLogServiceRoutes.put("postgres", postgres);
                default -> {
                    // ignored
                }
            }
        });
    }

}
