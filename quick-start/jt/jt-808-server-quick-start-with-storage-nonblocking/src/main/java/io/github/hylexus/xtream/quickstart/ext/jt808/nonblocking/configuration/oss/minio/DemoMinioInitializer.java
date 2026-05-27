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

package io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.oss.minio;

import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.configuration.props.QuickStartAppProps;
import io.github.hylexus.xtream.quickstart.ext.jt808.nonblocking.service.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

/**
 * 当前类是为了给 Minio 做初始化
 *
 * <ol>
 *     <li>bucket 不存在时
 *     <ol>
 *         <li>创建 bucket</li>
 *         <li>设置 bucket 可以匿名访问(仅仅是为了示例方便，生产环境你不应该这么做！！！)</li>
 *     </ol>
 *     </li>
 *     <li>bucket 存在时，不做其他额外操作</li>
 * </ol>
 *
 * @author hylexus
 */
public class DemoMinioInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoMinioInitializer.class);
    private final QuickStartAppProps quickStartAppProps;
    private final MinioService ossService;

    public DemoMinioInitializer(QuickStartAppProps quickStartAppProps, MinioService ossService) {
        this.quickStartAppProps = quickStartAppProps;
        this.ossService = ossService;
    }

    @Override
    public void run(String... args) {
        // 注意：这里是在项目启动阶段，所以可以调用 .block() 这种[阻塞]方法
        // 运行时，请不要直接或间接调用阻塞方法
        final String attachmentFileBucket = this.quickStartAppProps.getAttachmentServer().getRemoteStorageBucketName();
        if (Boolean.TRUE.equals(this.ossService.bucketExists(attachmentFileBucket).block())) {
            log.info("Bucket [{}] already exists", attachmentFileBucket);
        } else {
            log.info("Bucket [{}] does not exist, creating it ...", attachmentFileBucket);
            this.ossService.makeBucket(attachmentFileBucket).block();
            log.info("Bucket [{}] created", attachmentFileBucket);

            log.info("Setting bucket [{}]] policy(accessible with anonymous for debug purpose) ...", attachmentFileBucket);
            this.ossService.makeBucketAccessibleWithAnonymous(attachmentFileBucket).block();
            log.info("Bucket [{}] accessible with anonymous", attachmentFileBucket);
        }
    }
}
