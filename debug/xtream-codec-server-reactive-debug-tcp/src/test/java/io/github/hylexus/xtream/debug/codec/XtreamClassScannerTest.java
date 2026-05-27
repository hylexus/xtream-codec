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

package io.github.hylexus.xtream.debug.codec;

import io.github.hylexus.xtream.codec.common.utils.DefaultXtreamClassScanner;
import io.github.hylexus.xtream.codec.common.utils.XtreamClassScanner;
import io.github.hylexus.xtream.codec.server.reactive.spec.common.XtreamRequestHandlerMapping;
import io.github.hylexus.xtream.debug.codec.server.reactive.tcp.handler.DemoTcpXtreamHandler2;
import io.github.hylexus.xtream.debug.codec.server.reactive.tcp.handler.MyTcpRequestRouter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class XtreamClassScannerTest {

    @Test
    void test() {
        final String pkg = "io.github.hylexus.xtream.debug.codec.server.reactive.tcp";
        // final Class<XtreamField> annotation = XtreamField.class;
        final Class<XtreamRequestHandlerMapping> annotation = XtreamRequestHandlerMapping.class;
        final XtreamClassScanner scanner = new DefaultXtreamClassScanner();
        final Set<Class<?>> result = scanner.scan(
                new String[]{pkg},
                Set.of(
                        XtreamClassScanner.ScanMode.CLASS_ANNOTATION,
                        XtreamClassScanner.ScanMode.METHOD_ANNOTATION,
                        XtreamClassScanner.ScanMode.FIELD_ANNOTATION
                ),
                annotation, classInfo -> {
                    // ...
                    return classInfo.getName().startsWith(pkg) && !classInfo.isInnerClass();
                });
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.contains(DemoTcpXtreamHandler2.class));
        Assertions.assertTrue(result.contains(MyTcpRequestRouter.class));
    }
}
