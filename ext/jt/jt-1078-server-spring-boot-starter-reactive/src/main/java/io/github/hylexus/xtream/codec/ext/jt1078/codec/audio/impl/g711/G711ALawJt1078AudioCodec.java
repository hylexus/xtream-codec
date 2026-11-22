/*
 * MIT License
 *
 * Copyright (c) 2021 mz8023yt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Project modifications:
 * Modifications Copyright 2024-present the xtream-codec authors.
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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.g711;

/**
 * 上游 MIT 全文保留于文件头及 licenses/source/；固定比较快照和改造范围见 THIRD-PARTY-SOURCE-NOTICES.txt。
 * <p>
 * 当前类是从 <a href="https://gitee.com/mazcpnt/maz-g711/blob/master/maz_cpnt_g711.c">maz_cpnt_g711.c</a> 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on <a href="https://gitee.com/mazcpnt/maz-g711/blob/master/maz_cpnt_g711.c">maz_cpnt_g711.c</a>.
 *
 * @author hylexus
 * @author Codex (AI)
 */
public class G711ALawJt1078AudioCodec extends AbstractG711AudioCodec {

    @Override
    protected short decodeOne(byte input) {
        input = (byte) (input ^ 0x55);
        // G.711 符号位 unsigned byte
        final short s = (short) (1 - ((input >> 7) & 0x1));
        // G.711 强度位 unsigned byte
        final short eee = (short) ((input >> 4) & 0x7);
        // G.711 样本位 unsigned byte
        final short abcd = (short) (input & 0xf);
        // PCM 绝对值
        final short pcmNoS;
        if (0 == eee) {
            pcmNoS = (short) (abcd << 1 | 0x1);
        } else {
            pcmNoS = (short) (1 << (eee + 4) | 1 << (eee - 1) | abcd << eee);
        }
        // PCM 有效值
        final short pcm13bit = (s == 0) ? pcmNoS : (short) ~pcmNoS;

        // PCM 值
        final short pcm16bit = (short) (pcm13bit << 3);
        return (short) (pcm16bit & 0xffff);
    }

}
