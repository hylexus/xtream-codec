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

package io.github.hylexus.xtream.codec.ext.jt808.spec;

import io.github.hylexus.xtream.codec.ext.jt808.spec.impl.DefaultJt808FlowIdGenerator;

/**
 * @author hylexus
 */
@SuppressWarnings("ClassInitializationDeadlock")
public interface Jt808FlowIdGenerator {

    Jt808FlowIdGenerator DEFAULT = new DefaultJt808FlowIdGenerator();

    int MAX_FLOW_ID = 0xffff;

    /**
     * @param increment 一次性(连续不间断)递增 {@code increment} 个序列号
     * @return 当前流水号
     */
    int flowId(int increment);

    /**
     * @param count 流水号个数
     * @return 一批连续递增的流水号
     */
    default int[] flowIds(int count) {
        int last = this.flowId(count) - 1;
        final int[] ids = new int[count];
        for (int i = count - 1; i >= 0; i--) {
            ids[i] = last--;
        }
        return ids;
    }

    /**
     * @return 当前流水号(不会自增)
     */
    default int currentFlowId() {
        return flowId(0);
    }

    /**
     * @return 下一个流水号
     * @see #flowId(int)
     */
    default int nextFlowId() {
        return flowId(1);
    }
}
