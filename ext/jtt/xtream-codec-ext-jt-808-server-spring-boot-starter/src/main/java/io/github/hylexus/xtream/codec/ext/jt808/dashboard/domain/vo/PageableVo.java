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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.vo;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class PageableVo<T> {
    private long total;
    private List<T> data;

    public PageableVo(long total, List<T> data) {
        this.total = total;
        this.data = data;
    }

    public static <T> PageableVo<T> of(long total, List<T> data) {
        return new PageableVo<T>(total, data);
    }

    public static <T> PageableVo<T> empty() {
        return new PageableVo<T>(0, Collections.emptyList());
    }

    public long getTotal() {
        return total;
    }

    public PageableVo<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    public List<T> getData() {
        return data;
    }

    public PageableVo<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PageableVo.class.getSimpleName() + "[", "]")
                .add("total=" + total)
                .add("data=" + data)
                .toString();
    }
}
