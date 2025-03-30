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

package io.github.hylexus.xtream.codec.ext.jt1078.pubsub;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jt1078SubscriberCreator {
    private String sim;
    private short channelNumber;
    private Duration timeout;
    private String desc;
    private Map<String, Object> metadata;
}
