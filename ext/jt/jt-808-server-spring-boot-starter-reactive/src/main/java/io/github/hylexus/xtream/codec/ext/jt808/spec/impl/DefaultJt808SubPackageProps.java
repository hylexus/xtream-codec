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

package io.github.hylexus.xtream.codec.ext.jt808.spec.impl;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;

/**
 * @author hylexus
 */
public class DefaultJt808SubPackageProps implements Jt808RequestHeader.Jt808SubPackageProps {
    private int totalSubPackageCount;
    private int currentPackageNo;

    public DefaultJt808SubPackageProps() {
    }

    public DefaultJt808SubPackageProps(int totalSubPackageCount, int currentPackageNo) {
        this.totalSubPackageCount = totalSubPackageCount;
        this.currentPackageNo = currentPackageNo;
    }

    @Override
    public int totalSubPackageCount() {
        return this.totalSubPackageCount;
    }

    public DefaultJt808SubPackageProps totalSubPackageCount(int totalSubPackageCount) {
        this.totalSubPackageCount = totalSubPackageCount;
        return this;
    }

    @Override
    public int currentPackageNo() {
        return this.currentPackageNo;
    }

    public DefaultJt808SubPackageProps currentPackageNo(int currentPackageNo) {
        this.currentPackageNo = currentPackageNo;
        return this;
    }

}
