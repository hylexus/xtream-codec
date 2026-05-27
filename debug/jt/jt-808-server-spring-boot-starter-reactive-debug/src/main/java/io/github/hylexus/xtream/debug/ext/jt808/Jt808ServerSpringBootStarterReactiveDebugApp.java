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

package io.github.hylexus.xtream.debug.ext.jt808;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Jt808ServerSpringBootStarterReactiveDebugApp {

    /**
     * 测试报文:
     * <p>
     * 7e02004086010000000001893094655200E4000000000000000101D907F2073D336C000000000000211124114808010400000026030200003001153101002504000000001404000000011504000000FA160400000000170200001803000000EA10FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF02020000EF0400000000F31B017118000000000000000000000000000000000000000000000000567e
     */
    public static void main(String[] args) {
        // FIXME 如果你不了解 ResourceLeakDetector 是做什么的,请务必注释掉下面这行代码!!!
        // io.netty.util.ResourceLeakDetector.setLevel(io.netty.util.ResourceLeakDetector.Level.PARANOID);
        SpringApplication.run(Jt808ServerSpringBootStarterReactiveDebugApp.class, args);
    }
}
