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

package io.github.hylexus.xtream.codec.ext.jt1078.spec;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamSession;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public interface Jt1078Session extends XtreamSession {

    @Override
    Jt1078Session lastCommunicateTime(Instant current);

    int simLength();

    String rawSim();

    String convertedSim();

    short channelNumber();

    @Nullable Jt1078PayloadType audioType();

    Jt1078Session audioType(Jt1078PayloadType type);

    @Nullable Jt1078PayloadType videoType();

    Jt1078Session videoType(Jt1078PayloadType type);

}
