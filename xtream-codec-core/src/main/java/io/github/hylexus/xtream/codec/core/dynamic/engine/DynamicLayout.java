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

import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicLengthUnit;
import io.github.hylexus.xtream.codec.core.dynamic.schema.DynamicChecksumAlgorithm;
import io.github.hylexus.xtream.codec.core.dynamic.spi.DynamicExtension;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * 编译完成的执行布局。所有配置与引用均已解析，不持有原始 DSL 字段。
 * {@link DynamicLayoutCompiler} 将编译期字段树转换为此布局，供编码和解码共同使用。
 * 布局保存跨调用复用的配置；字段值、游标、边界和回填位置属于单次执行的
 * {@link DynamicFrame} 与 {@link DynamicRuntime}。
 *
 * @param nodes      按报文顺序排列的根节点；子结构由各节点的操作保存
 * @param scopeSizes 各作用域所需的槽位数量，数组下标为作用域编号；编号 0 表示根作用域
 * @author Codex (AI)
 * @since 0.11.0
 */
record DynamicLayout(List<Node> nodes, int[] scopeSizes) {
    DynamicLayout {
        nodes = List.copyOf(nodes);
        scopeSizes = scopeSizes.clone();
    }

    /**
     * 编译后字段值的稳定地址，运行时无需通过路径字符串查找值。
     * 执行帧沿父帧链找到相同作用域编号，再按槽位索引访问值。
     * 作用域编号标识布局中的词法作用域，不表示父帧层数；不同集合元素可使用同一编号的新帧。
     *
     * @param scope 编译时分配的作用域编号
     * @param slot  字段在所属作用域中的槽位索引
     * @author Codex (AI)
     */
    record Address(int scope, int slot) {
    }

    /**
     * 普通字段、数组元素和分支共用的执行节点。
     * 节点入口统一处理作用域、条件、错误定位和可选跟踪，具体内容由 {@link Operation} 决定。
     * 回填区间的起止动作在编码字段序列中围绕节点执行，条件跳过区间端点时仍按实际位置结算。
     *
     * @param id        业务值对象中的字段键；集合元素和分支也保留定义中的标识
     * @param name      跟踪展示名称，未声明名称时使用字段标识
     * @param path      编译后完整字段路径，用于错误定位
     * @param address   当前节点的值槽位地址
     * @param retained  后续引用或回填是否需要保留该值；编码时据此决定是否计算规范化值
     * @param condition 编译后的出现条件，未声明条件时恒为真
     * @param generated 编码时的即时值生成规则；区间校验和使用回填动作，普通输入字段为空
     * @param operation 节点具体的编解码操作与配置
     * @param starts    编码进入当前字段前，记录区间起点的回填动作
     * @param ends      编码完成当前字段后，结算区间长度的回填动作
     * @author Codex (AI)
     */
    record Node(
            String id,
            String name,
            String path,
            Address address,
            boolean retained,
            ValueExpression condition,
            @Nullable Generation generated,
            Operation operation,
            List<Backfill> starts,
            List<Backfill> ends
    ) {
        Node {
            starts = List.copyOf(starts);
            ends = List.copyOf(ends);
        }
    }

    /**
     * 编译后的求值入口，供条件、派生值和长度表达式复用。
     * 受控表达式直接访问帧中的已解析值；引擎表达式已绑定宿主提供的引擎实例与字段地址。
     *
     * @author Codex (AI)
     */
    @FunctionalInterface
    interface ValueExpression {
        /**
         * 根据本次执行的帧求值；调用位置负责检查结果是否满足条件、长度或派生值的用途约束。
         *
         * @param frame  当前活动执行帧，外层值通过父帧访问
         * @param offset 当前游标的 bit 偏移，用于求值失败时的定位
         * @return 求值结果，允许为空；不隐式转换为布尔值或长度
         */
        @Nullable Object evaluate(DynamicFrame frame, long offset);
    }

    /**
     * 编码生成值的两种即时来源；区间校验和由回填动作处理。
     *
     * @author Codex (AI)
     */
    sealed interface Generation permits GeneratedExpression, GeneratedCount {
    }

    record GeneratedExpression(ValueExpression expression) implements Generation {
    }

    record GeneratedCount(String id) implements Generation {
    }

    /**
     * 互斥的节点操作类型，各类型只保存自身需要的配置。
     * 编码与解码分别按此类型分派，条件和跟踪由外层节点入口处理。
     *
     * @author Codex (AI)
     */
    sealed interface Operation
            permits IntegerOp, FloatOp, BytesOp, BitsOp, AlignOp, StructOp, ArrayOp, EntriesOp, ChoiceOp, DerivedOp {
    }

    /**
     * 1..64 bit 整数操作，读写时以 long 保存原始位模式。
     * 编码源整数值缺省时，可由同级派生位段合成；已提供源值时，派生位段负责检查一致性。
     *
     * @param bits     整数位宽
     * @param signed   是否按有符号整数解释位模式
     * @param little   是否使用小端字节序，仅适用于完整字节位宽
     * @param lsb      是否使用字节内低位优先的位序
     * @param backfill 长度或校验和区间动作；前置字段先写占位，后置校验字段使用已计算值
     * @param reverse  引用当前整数的同级派生位段，用于反向合成编码值
     * @author Codex (AI)
     */
    record IntegerOp(
            int bits, boolean signed, boolean little, boolean lsb,
            @Nullable Backfill backfill, List<BitView> reverse) implements Operation {
        IntegerOp {
            reverse = List.copyOf(reverse);
        }
    }

    /**
     * 32 或 64 bit 浮点数操作，起点要求字节对齐，编解码均拒绝非有限值。
     *
     * @param bits   浮点数位宽
     * @param little 是否使用小端字节序
     * @param lsb    是否使用字节内低位优先的位序
     * @author Codex (AI)
     */
    record FloatOp(int bits, boolean little, boolean lsb) implements Operation {
    }

    /**
     * 字节字段预解析后的转换与填充配置，避免执行时重复读取内置选项。
     *
     * @param type           string、bcd、hex、bytes、padding 或 extension 类型名称
     * @param charset        字符串使用的字符集
     * @param padByte        填充字节值
     * @param paddingAllowed 是否允许用填充字节补足目标长度
     * @param left           是否在值之前填充，否则在值之后填充
     * @param strip          解码时是否移除填充字节
     * @param extension      编译时绑定的扩展实例，内置字节类型为空
     * @param config         已冻结的类型选项，传给扩展的校验与编解码入口
     * @author Codex (AI)
     */
    record ByteFormat(
            String type, Charset charset, int padByte, boolean paddingAllowed,
            boolean left, boolean strip, @Nullable DynamicExtension extension,
            Map<String, Object> config) {
    }

    /**
     * 按完整字节读写的字段操作，转换规则与长度来源分别保存。
     *
     * @param format 字节与业务值的转换配置
     * @param length 字段长度边界，bit 单位的实际长度也必须为 8 的倍数
     * @author Codex (AI)
     */
    record BytesOp(ByteFormat format, Length length) implements Operation {
    }

    /**
     * 固定位数的原始位串操作，业务值使用由 0 和 1 组成的字符串，不作整数解释。
     *
     * @param bits 位串长度
     * @param lsb  是否使用字节内低位优先的位序
     * @author Codex (AI)
     */
    record BitsOp(int bits, boolean lsb) implements Operation {
    }

    /**
     * 根据当前 bit 位置补齐至指定边界，不产生业务值。
     * 编码写入填充位，解码检查被跳过的每一位是否符合填充值。
     *
     * @param alignment 以 bit 计的对齐边界，取不超过 64 的二次幂
     * @param fill      填充位，取 0 或 1
     * @param lsb       是否使用字节内低位优先的位序
     * @author Codex (AI)
     */
    record AlignOp(int alignment, int fill, boolean lsb) implements Operation {
    }

    /**
     * 按顺序执行子字段的结构操作，普通结构子字段与结构节点处于同一执行作用域。
     *
     * @param fields 子字段节点
     * @param length 可选的结构长度；省略时继承父容器的边界
     * @author Codex (AI)
     */
    record StructOp(List<Node> fields, @Nullable Length length) implements Operation {
        StructOp {
            fields = List.copyOf(fields);
        }
    }

    /**
     * 重复执行同一元素布局的数组操作，每次进入元素都使用新的局部帧。
     *
     * @param item   元素节点，保留自身的条件和操作
     * @param length item 单位表示元素个数，bit 或 byte 单位表示数组内容的总边界
     * @author Codex (AI)
     */
    record ArrayOp(Node item, Length length) implements Operation {
    }

    /**
     * TLV、map 和 pair 共用的有序条目操作，各条目通过键选择自己的值分支。
     * TLV 和 map 带有字节长度前缀，未知键可保留原始字节；pair 没有条目长度，必须匹配已知分支。
     *
     * @param variants   非负十进制键字符串到值节点的映射，各分支拥有独立作用域
     * @param length     item 单位表示条目个数，bit 或 byte 单位表示整个条目集合的边界
     * @param keyBits    条目键的位宽
     * @param lengthBits 条目内容的字节长度前缀位宽；pair 不使用该前缀
     * @param pair       是否按无条目长度前缀的 pair 布局执行
     * @author Codex (AI)
     */
    record EntriesOp(
            Map<String, Node> variants,
            Length length, int keyBits, int lengthBits, boolean pair) implements Operation {
        EntriesOp {
            variants = Map.copyOf(variants);
        }
    }

    /**
     * 根据已解析整数判别值选择一个分支，分支仍经过统一节点入口检查自身条件。
     *
     * @param discriminator  判别字段的值地址
     * @param variants       非负十进制判别值字符串到分支节点的映射，各分支拥有独立作用域
     * @param defaultVariant 未命中显式键时使用的可选分支，拥有独立作用域
     * @author Codex (AI)
     */
    record ChoiceOp(Address discriminator, Map<String, Node> variants, @Nullable Node defaultVariant) implements Operation {
        ChoiceOp {
            variants = Map.copyOf(variants);
        }
    }

    /**
     * 不消费报文空间的派生值操作，表达式与源整数位段视图在编译时保证二选一。
     *
     * @param expression 编译后的派生表达式，采用位段视图时为空
     * @param view       源整数的位段视图，采用表达式时为空
     * @author Codex (AI)
     */
    record DerivedOp(@Nullable ValueExpression expression, @Nullable BitView view) implements Operation {
    }

    /**
     * 源整数的无符号位段视图，同时用于派生求值、反向合成与跟踪范围定位。
     * 位段偏移从数值最低有效位起算，与报文中的字节序和字节内位序无关；后两者用于映射跟踪范围。
     *
     * @param id          派生字段标识，反向合成时用于读取输入值
     * @param path        派生字段路径，用于错误定位
     * @param source      源整数的值地址
     * @param offset      从源整数最低有效位起算的 bit 偏移
     * @param width       位段宽度
     * @param sourceWidth 源整数位宽
     * @param little      源整数是否使用小端字节序
     * @param lsb         源整数是否使用字节内低位优先的位序
     * @author Codex (AI)
     */
    record BitView(
            String id, String path, Address source, int offset, int width,
            int sourceWidth, boolean little, boolean lsb) {
    }

    /**
     * 长度回填或字节校验和的区间配置，目标占位位置和区间起点由本次执行的帧保存。
     * 同一个动作被整数操作及区间起止节点引用；解码仅执行校验和动作，长度仍直接读取。
     *
     * @param target   回填长度字段的值地址
     * @param path     目标字段路径，区间核对失败时定位到生成字段
     * @param bits     目标整数字段位宽
     * @param little   目标整数是否使用小端字节序
     * @param lsb      目标整数是否使用字节内低位优先的位序
     * @param unit     区间长度的计量单位，取 bit 或 byte
     * @param checksum 非空时按线上字节计算校验和，否则计算区间长度
     * @param deferred 目标位于区间之前时为 true，编码先预留再回填；后置校验字段为 false
     * @author Codex (AI)
     */
    record Backfill(
            Address target, String path, int bits, boolean little, boolean lsb, DynamicLengthUnit unit,
            @Nullable DynamicChecksumAlgorithm checksum, boolean deferred) {
    }

    /**
     * 编译后的长度声明，计量单位与互斥的长度来源分别表达。
     * 执行器根据节点类型将长度用于内容边界或集合计数。
     *
     * @param unit   bit、byte 或 item 计量单位
     * @param source 唯一的长度来源
     * @author Codex (AI)
     */
    record Length(DynamicLengthUnit unit, LengthSource source) {
    }

    /**
     * 互斥的长度来源。固定值、引用和表达式通过公共求值入口获得长度，
     * 前缀和剩余量由各方向的执行器结合游标处理。
     *
     * @author Codex (AI)
     */
    sealed interface LengthSource permits Fixed, Referenced, Prefix, Remaining, Evaluated {
    }

    /**
     * 固定长度或个数，具体含义由 {@link Length#unit()} 决定。
     *
     * @param units 以声明单位计量的非负长度
     * @author Codex (AI)
     */
    record Fixed(long units) implements LengthSource {
    }

    /**
     * 引用已分配槽位的长度来源。
     * 编码时若引用的是待回填长度，使用内容的实际长度并在区间结束后回填；解码使用已读取的线上值。
     *
     * @param target   长度字段的值地址
     * @param deferred 目标是否为回填字段；只有其值尚处于待回填状态时才延迟确定编码长度
     * @author Codex (AI)
     */
    record Referenced(Address target, boolean deferred) implements LengthSource {
    }

    /**
     * 内容之前的无符号长度前缀，前缀值使用 {@link Length#unit()} 声明的单位。
     * 编码按内容确定或回填前缀，解码先读取前缀再建立边界或计数。
     *
     * @param bits 前缀位宽，取 8、16 或 32；前缀按大端字节序和高位优先位序读写
     * @author Codex (AI)
     */
    record Prefix(int bits) implements LengthSource {
    }

    /**
     * 剩余内容长度。解码使用当前父边界的剩余量，编码使用提供内容的实际长度并受父边界约束。
     *
     * @author Codex (AI)
     */
    record Remaining() implements LengthSource {
    }

    /**
     * 表达式计算的长度或个数，执行时检查结果必须为可表示为 long 的非负精确整数。
     *
     * @param expression 已编译的长度求值函数
     * @author Codex (AI)
     */
    record Evaluated(ValueExpression expression) implements LengthSource {
    }
}
