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

package io.github.hylexus.xtream.quickstart.ext.jt808.attachment.nonblocking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Jt808AttachmentServerQuickStartNonblockingApp {

    public static void main(String[] args) {
        // FIXME 如果你不了解 ResourceLeakDetector 是做什么的,请务必注释掉下面这行代码!!!
        // io.netty.util.ResourceLeakDetector.setLevel(io.netty.util.ResourceLeakDetector.Level.PARANOID);
        SpringApplication.run(Jt808AttachmentServerQuickStartNonblockingApp.class, args);
    }

}
