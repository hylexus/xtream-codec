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

package io.github.hylexus.xtream.codec.core.dynamic.engine;

import io.github.hylexus.xtream.codec.core.tracker.CodecTraceNode;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * 单次作用域的值与回填状态。集合元素使用新帧，外层字段通过稳定地址访问。
 * 根作用域在执行开始时建立帧；数组元素和各类分支按需建立独立帧，普通结构的子字段沿用当前帧。
 * 每个帧的槽位数量由 {@link DynamicLayout#scopeSizes()} 决定，帧及其中的状态只属于本次执行。
 * 值槽位除业务值外还有三种状态：{@link #MISSING} 表示没有保留值，
 * 包括尚未执行、被条件跳过和编码时无需保留的字段；{@code null} 表示已存入空值，
 * {@link #PENDING} 表示长度字段等待回填。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
final class DynamicFrame {
    /**
     * 槽位中没有保留值；尚未执行、条件跳过或编码时无需保留的字段均使用此状态。
     */
    static final Object MISSING = new Object();
    /**
     * 编码已写入长度占位位段，但区间尚未结束。
     */
    static final Object PENDING = new Object();
    /**
     * 与布局中的作用域编号对应，不表示当前帧在父帧链中的深度。
     */
    final int scope;
    /**
     * 外层活动帧；集合元素和分支退出后即丢弃当前帧。
     */
    final @Nullable DynamicFrame parent;
    /**
     * 当前作用域的值槽位，初始值均为 {@link #MISSING}。
     */
    private final Object[] values;
    /**
     * 待回填字段的写入位置，首次预留时才分配。
     */
    private long @Nullable [] positions;
    /**
     * 待回填区间的实际起点，与写入位置分别记录。
     */
    private long @Nullable [] regionStarts;
    // 校验和的预期值与普通字段值分开，后置校验字段尚未读取时不暴露为可引用的字段值。
    private @Nullable Long @Nullable [] checksums;
    /**
     * 跟踪模式下各节点执行前的 bit 位置；未启用跟踪时不分配。
     */
    private final long @Nullable [] starts;
    /**
     * 跟踪模式下各节点执行后的 bit 位置；未启用跟踪时不分配。
     */
    private final long @Nullable [] ends;
    /**
     * 跟踪模式下各节点对应的跟踪对象；未启用跟踪时不分配。
     */
    private final @Nullable CodecTraceNode @Nullable [] traces;

    /**
     * 为一个活动作用域创建独立状态，不复用其他集合元素或请求的槽位。
     *
     * @param scope   编译布局中的作用域编号
     * @param size    该作用域的槽位数量
     * @param parent  外层活动帧，根帧为空
     * @param tracked 是否为节点范围和跟踪对象分配数组
     */
    DynamicFrame(int scope, int size, @Nullable DynamicFrame parent, boolean tracked) {
        this.scope = scope;
        this.parent = parent;
        values = new Object[size];
        Arrays.fill(values, MISSING);
        starts = tracked ? new long[size] : null;
        ends = tracked ? new long[size] : null;
        traces = tracked ? new CodecTraceNode[size] : null;
    }

    /**
     * 沿活动帧链查找地址所属的作用域，允许子节点引用外层已解析字段。
     */
    private DynamicFrame owner(DynamicLayout.Address address) {
        for (DynamicFrame frame = this; frame != null; frame = frame.parent) {
            if (frame.scope == address.scope()) {
                return frame;
            }
        }
        throw new IllegalStateException("执行引用不在活动作用域中");
    }

    /**
     * 读取地址中的原始状态，包括 {@link #MISSING}、{@link #PENDING} 和 {@code null}。
     */
    @Nullable Object get(DynamicLayout.Address address) {
        return owner(address).values[address.slot()];
    }

    /**
     * 写入地址所属的帧；完成回填时用实际长度替换 {@link #PENDING}。
     */
    void put(DynamicLayout.Address address, @Nullable Object value) {
        owner(address).values[address.slot()] = value;
    }

    /**
     * 要求引用字段已就绪；缺失或待回填时以使用该引用的字段位置报告错误。
     */
    @Nullable Object require(DynamicLayout.Address address, String path, long offset) {
        final Object value = get(address);
        if (value == MISSING || value == PENDING) {
            throw new DynamicCodecException(path, offset, "引用字段不可用");
        }
        return value;
    }

    /**
     * 记录长度占位位段的写入位置，并将目标值标为待回填。
     */
    void reserve(DynamicLayout.Address address, long offset) {
        final DynamicFrame frame = owner(address);
        if (frame.positions == null) {
            frame.positions = new long[frame.values.length];
        }
        frame.positions[address.slot()] = offset;
        put(address, PENDING);
    }

    /**
     * 区间起点节点执行前记录实际位置，长度与校验和都使用实际游标。
     * 后置校验字段尚未执行时也需记录，独立分配区间数组以免创建无用的占位位置数组。
     */
    void beginRegion(DynamicLayout.Address address, long offset) {
        final DynamicFrame frame = owner(address);
        if (frame.regionStarts == null) {
            frame.regionStarts = new long[frame.values.length];
        }
        frame.regionStarts[address.slot()] = offset;
    }

    void checksum(DynamicLayout.Address address, long value) {
        final DynamicFrame frame = owner(address);
        if (frame.checksums == null) {
            frame.checksums = new Long[frame.values.length];
        }
        frame.checksums[address.slot()] = value;
    }

    @Nullable Long checksum(DynamicLayout.Address address) {
        final DynamicFrame frame = owner(address);
        return frame.checksums == null ? null : frame.checksums[address.slot()];
    }

    /**
     * 返回长度占位位段的位置，供编码器原地回填。
     */
    long position(DynamicLayout.Address address) {
        return requireNonNull(owner(address).positions)[address.slot()];
    }

    /**
     * 返回当前区间起点，供编码器按实际写入的 bit 数结算长度。
     */
    long regionStart(DynamicLayout.Address address) {
        return requireNonNull(owner(address).regionStarts)[address.slot()];
    }

    /**
     * 跟踪模式下保存节点实际执行范围与跟踪对象；生产路径不写入这些数组。
     */
    void recordTrace(DynamicLayout.Address address, long start, long end, @Nullable CodecTraceNode node) {
        final DynamicFrame frame = owner(address);
        if (frame.starts != null) {
            frame.starts[address.slot()] = start;
            requireNonNull(frame.ends)[address.slot()] = end;
            requireNonNull(frame.traces)[address.slot()] = node;
        }
    }

    /**
     * 返回源节点的实际起点，用于派生位段的跟踪范围定位。
     */
    long start(DynamicLayout.Address address) {
        return requireNonNull(owner(address).starts)[address.slot()];
    }

    /**
     * 返回源节点的实际终点，用于派生位段的跟踪范围定位。
     */
    long end(DynamicLayout.Address address) {
        return requireNonNull(owner(address).ends)[address.slot()];
    }

    /**
     * 查找已保存的跟踪节点；未启用跟踪时返回 {@code null}。
     */
    @Nullable CodecTraceNode trace(DynamicLayout.Address address) {
        final @Nullable CodecTraceNode[] nodes = owner(address).traces;
        return nodes == null ? null : nodes[address.slot()];
    }
}
