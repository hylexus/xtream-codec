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

package io.github.hylexus.xtream.codec.server.reactive.spec.common;

public final class XtreamServerConstants {
    private XtreamServerConstants() {
    }

    public static final String BEAN_NAME_REQUEST_DISPATCHER_SCHEDULER = "xtreamRequestDispatcherScheduler";
    public static final String BEAN_NAME_HANDLER_ADAPTER_NON_BLOCKING_SCHEDULER = "xtreamHandlerAdapterNonBlockingScheduler";
    public static final String BEAN_NAME_HANDLER_ADAPTER_BLOCKING_SCHEDULER = "xtreamHandlerAdapterBlockingScheduler";
    public static final String BEAN_NAME_EVENT_PUBLISHER_SCHEDULER = "xtreamEventPublisherScheduler";

}
