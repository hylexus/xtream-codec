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

@Getter
@Setter
@ToString
public class AttachmentServerProps {
    /**
     * 附件服务器的IP，应该是一个公网IP(设备能访问的IP)
     */
    private String serverIp;
    /**
     * 本地文件临时存储路径
     */
    private String temporaryPath;
    /**
     * 远程文件存 bucketName
     */
    private String remoteStorageBucketName = "jt-attachment";
    /**
     * 将本地文件上传到远程存储后，是否保留本地文件
     */
    private boolean retainLocalTemporaryFile = true;
    private String filePreviewBaseUrl;
}
