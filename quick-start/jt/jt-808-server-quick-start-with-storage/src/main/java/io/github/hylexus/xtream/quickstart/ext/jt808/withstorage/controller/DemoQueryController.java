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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.controller;

import io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo.PageableVo;
import io.github.hylexus.xtream.codec.ext.jt808.exception.BadRequestException;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.domain.dto.Jt808AlarmAttachmentInfoDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.domain.dto.Jt808TraceLogDto;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.domain.vo.Jt808AlarmAttachmentInfoVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.domain.vo.Jt808TraceLogVo;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.AttachmentInfoService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.TraceLogService;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.clickhouse.AttachmentInfoServiceClickhouseImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.clickhouse.TraceLogServiceClickhouseImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.mysql.AttachmentInfoServiceMysqlImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.mysql.TraceLogServiceMysqlImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.postgres.AttachmentInfoServicePostgresImpl;
import io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.service.impl.postgres.TraceLogServicePostgresImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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

    public DemoQueryController(QuickStartAppProps appProps, ApplicationContext applicationContext) {
        this.appProps = appProps;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/server-config")
    public Map<String, Object> serverConfig() {
        final QuickStartAppProps.DatabaseControl databaseControl = appProps.getFeatureControl().getDatabase();
        final List<Map<String, Object>> databaseList = List.of(
                Map.of("label", "Clickhouse", "value", "clickhouse", "enabled", databaseControl.getClickhouse().isEnabled()),
                Map.of("label", "Postgres", "value", "postgres", "enabled", databaseControl.getPostgres().isEnabled()),
                Map.of("label", "Mysql", "value", "mysql", "enabled", databaseControl.getMysql().isEnabled())
        );
        return Map.of("database", databaseList);
    }

    @GetMapping("/attachment-info")
    public Mono<PageableVo<Jt808AlarmAttachmentInfoVo>> alarmInfo(@Validated Jt808AlarmAttachmentInfoDto dto) {
        final AttachmentInfoService service = this.attachmentInfoServiceRoutes.get(dto.getSt());
        if (service == null) {
            return Mono.error(new BadRequestException("`st` is invalid"));
        }
        return service.listAlarmAttachmentInfo(dto);
    }

    @GetMapping("/trace-log")
    public Mono<PageableVo<Jt808TraceLogVo>> alarmInfo(@Validated Jt808TraceLogDto dto) {
        final TraceLogService service = this.traceLogServiceRoutes.get(dto.getSt());
        if (service == null) {
            return Mono.error(new BadRequestException("`st` is invalid"));
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