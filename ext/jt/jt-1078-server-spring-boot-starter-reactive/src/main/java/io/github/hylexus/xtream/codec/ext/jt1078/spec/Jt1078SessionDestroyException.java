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

package io.github.hylexus.xtream.codec.ext.jt1078.spec;

import io.github.hylexus.xtream.codec.ext.jt1078.pubsub.Jt1078Subscriber;

public class Jt1078SessionDestroyException extends Jt1078Subscriber.Jt1078SubscriberCloseException {
    public Jt1078SessionDestroyException() {
    }

    public Jt1078SessionDestroyException(String message) {
        super(message);
    }

    public Jt1078SessionDestroyException(String message, Throwable cause) {
        super(message, cause);
    }

    public Jt1078SessionDestroyException(Throwable cause) {
        super(cause);
    }

    public Jt1078SessionDestroyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
