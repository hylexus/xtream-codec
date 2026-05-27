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

package io.github.hylexus.xtream.codec.base.web.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.StringJoiner;


public class PageableDto {

    @Min(value = 1, message = "pageNumber must be greater than or equal to 1")
    protected Integer pageNumber = 1;

    @Min(value = 1, message = "pageSize must be greater than or equal to 1")
    @Max(value = 200, message = "pageSize must be less than or equal to 200")
    protected Integer pageSize = 10;

    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public PageableDto setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public PageableDto setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PageableDto.class.getSimpleName() + "[", "]")
                .add("page=" + pageNumber)
                .add("size=" + pageSize)
                .toString();
    }
}
