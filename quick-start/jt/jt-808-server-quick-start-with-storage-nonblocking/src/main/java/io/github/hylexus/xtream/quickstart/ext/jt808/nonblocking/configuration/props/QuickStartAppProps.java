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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author hylexus
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "demo-app-with-storage")
public class QuickStartAppProps {

    @NestedConfigurationProperty
    private MinioProps minio = new MinioProps();

    @NestedConfigurationProperty
    private AttachmentServerProps attachmentServer = new AttachmentServerProps();

    @NestedConfigurationProperty
    private FeatureControl featureControl = new FeatureControl();

    @Getter
    @Setter
    public static class FeatureControl {
        @NestedConfigurationProperty
        private DatabaseControl database = new DatabaseControl();

        @NestedConfigurationProperty
        private OssControl oss = new OssControl();
    }

    @Getter
    @Setter
    public static class OssControl {

        @NestedConfigurationProperty
        private Switchable minio = new Switchable();

        @NestedConfigurationProperty
        private Switchable logging = new Switchable();
    }

    @Getter
    @Setter
    public static class DatabaseControl {
        private Switchable clickhouse = new Switchable();
        private Switchable mysql = new Switchable();
        private Switchable postgres = new Switchable();
        private Switchable logging = new Switchable();
    }

    @Getter
    @Setter
    public static class Switchable {
        private boolean enabled = true;
    }

    public String getAttachmentBasePreviewUrl() {
        final String filePreviewBaseUrl = this.getAttachmentServer().getFilePreviewBaseUrl();
        if (StringUtils.isEmpty(filePreviewBaseUrl)) {
            return this.getMinio().getEndpoint() + "/" + this.getAttachmentServer().getRemoteStorageBucketName();
        }
        return filePreviewBaseUrl;
    }
}
