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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.audio.impl.g711;

/**
 * 当前类是从 <a href="https://gitee.com/mazcpnt/maz-g711/blob/master/maz_cpnt_g711.c">maz_cpnt_g711.c</a> 复制过来修改的。
 * <p>
 * The current class is derived from and modified based on <a href="https://gitee.com/mazcpnt/maz-g711/blob/master/maz_cpnt_g711.c">maz_cpnt_g711.c</a>.
 *
 * @author hylexus
 */
public class G711MuLawJt1078AudioCodec extends AbstractG711AudioCodec {

    @Override
    protected short decodeOne(byte input) {
        input = (byte) (input ^ 0xff);
        // G.711 符号位 unsigned byte
        final short s = (short) ((input >> 7) & 0x1);
        // G.711 强度位 unsigned byte
        final short eee = (short) ((input >> 4) & 0x7);
        // G.711 样本位 unsigned byte
        final short abcd = (short) (input & 0xf);
        // PCM 绝对值
        final short pcmNoS = (short) (1 << (eee + 5) | 1 << eee | abcd << (eee + 1));
        // PCM 有效值
        final short pcm14bit = (s == 0) ? pcmNoS : (short) ~pcmNoS;
        // PCM 值
        final short pcm16bit = (short) (pcm14bit << 2);

        return (short) (pcm16bit & 0xffff);
    }

}
