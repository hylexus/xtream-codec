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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.boot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;


@SuppressWarnings("NullAway")
@ConfigurationProperties(prefix = "jt1078-server.features.dashboard")
public class XtreamJt808ServerDashboardProperties {

    private boolean enabled = true;
    // 暂时不支持动态配置
    private final String basePath = "/dashboard-ui-1078/";
    /**
     * 将 `/` 307 到 `/dashboard-ui-1078/`
     */
    private boolean redirectRootToBasePath = true;
    /**
     * 将 `base-path` 开头的 404 请求转发到 `index.html`
     * <p>
     * 如果不开启这个配置，刷新 dashboard 页面时会出现404(也就是说只能刷新 `base-path` 根路由，其他子路由刷新会 404)
     */
    private boolean forwardNotFoundToIndex = true;

    @NestedConfigurationProperty
    private Jt808DashboardProxy jt808DashboardProxy = new Jt808DashboardProxy();

    @NestedConfigurationProperty
    private CorsProperties dashboardApiCorsFilter = new CorsProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public XtreamJt808ServerDashboardProperties setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getBasePath() {
        return basePath;
    }

    public boolean isRedirectRootToBasePath() {
        return redirectRootToBasePath;
    }

    public XtreamJt808ServerDashboardProperties setRedirectRootToBasePath(boolean redirectRootToBasePath) {
        this.redirectRootToBasePath = redirectRootToBasePath;
        return this;
    }

    public boolean isForwardNotFoundToIndex() {
        return forwardNotFoundToIndex;
    }

    public XtreamJt808ServerDashboardProperties setForwardNotFoundToIndex(boolean forwardNotFoundToIndex) {
        this.forwardNotFoundToIndex = forwardNotFoundToIndex;
        return this;
    }

    public Jt808DashboardProxy getJt808DashboardProxy() {
        return jt808DashboardProxy;
    }

    public XtreamJt808ServerDashboardProperties setJt808DashboardProxy(Jt808DashboardProxy jt808DashboardProxy) {
        this.jt808DashboardProxy = jt808DashboardProxy;
        return this;
    }

    public CorsProperties getDashboardApiCorsFilter() {
        return dashboardApiCorsFilter;
    }

    public XtreamJt808ServerDashboardProperties setDashboardApiCorsFilter(CorsProperties dashboardApiCorsFilter) {
        this.dashboardApiCorsFilter = dashboardApiCorsFilter;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", XtreamJt808ServerDashboardProperties.class.getSimpleName() + "[", "]")
                .add("enabled=" + enabled)
                .add("basePath='" + basePath + "'")
                .add("redirectRootToBasePath=" + redirectRootToBasePath)
                .add("forwardNotFoundToIndex=" + forwardNotFoundToIndex)
                .add("jt808DashboardProxy=" + jt808DashboardProxy)
                .add("dashboardApiCorsFilter=" + dashboardApiCorsFilter)
                .toString();
    }

    public static class Jt808DashboardProxy {
        private String baseUrl = "http://localhost:8888";

        public String getBaseUrl() {
            return baseUrl;
        }

        public Jt808DashboardProxy setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Jt808DashboardProxy.class.getSimpleName() + "[", "]")
                    .add("baseUrl='" + baseUrl + "'")
                    .toString();
        }
    }


    public static class CorsProperties {
        private boolean enabled = false;
        private String pathPattern = "/dashboard-api/jt1078/v1/stream-data/**";
        private Boolean allowCredentials = false;
        private List<String> allowedOrigins;
        private List<String> allowedOriginPatterns;
        private List<String> allowedHeaders;
        private List<String> exposedHeaders;
        private List<String> allowedMethods;
        private Duration maxAge;

        public boolean isEnabled() {
            return enabled;
        }

        public CorsProperties setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public String getPathPattern() {
            return pathPattern;
        }

        public CorsProperties setPathPattern(String pathPattern) {
            this.pathPattern = pathPattern;
            return this;
        }

        public Boolean getAllowCredentials() {
            return allowCredentials;
        }

        public CorsProperties setAllowCredentials(Boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
            return this;
        }

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public CorsProperties setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
            return this;
        }

        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public CorsProperties setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
            return this;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public CorsProperties setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
            return this;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public CorsProperties setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
            return this;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public CorsProperties setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
            return this;
        }

        public Duration getMaxAge() {
            return maxAge;
        }

        public CorsProperties setMaxAge(Duration maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", CorsProperties.class.getSimpleName() + "[", "]")
                    .add("enabled=" + enabled)
                    .add("pathPattern='" + pathPattern + "'")
                    .add("allowCredentials=" + allowCredentials)
                    .add("allowedOrigins=" + allowedOrigins)
                    .add("allowedOriginPatterns=" + allowedOriginPatterns)
                    .add("allowedHeaders=" + allowedHeaders)
                    .add("exposedHeaders=" + exposedHeaders)
                    .add("allowedMethods=" + allowedMethods)
                    .add("maxAge=" + maxAge)
                    .toString();
        }
    }

    public String getFormatedBasePath() {
        return formatBasePath(this.getBasePath());
    }

    public static String formatBasePath(String input) {
        if (!StringUtils.hasText(input)) {
            return "/dashboard-ui-1078/";
        }
        input = input.trim();
        if (!input.startsWith("/")) {
            input = "/" + input;
        }
        if (!input.endsWith("/")) {
            input += "/";
        }
        return input;
    }
}
