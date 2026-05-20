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

package io.github.hylexus.xtream.codec.ext.jt1078.dashboard.domain.dto;

import io.github.hylexus.xtream.codec.base.web.domain.dto.PageableDto;

import java.util.StringJoiner;

public class Jt1078SubscriberQueryDto extends PageableDto {
    private String sim;

    public String getSim() {
        return sim;
    }

    public Jt1078SubscriberQueryDto setSim(String sim) {
        this.sim = sim;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Jt1078SubscriberQueryDto.class.getSimpleName() + "[", "]")
                .add("sim='" + sim + "'")
                .toString();
    }
}
