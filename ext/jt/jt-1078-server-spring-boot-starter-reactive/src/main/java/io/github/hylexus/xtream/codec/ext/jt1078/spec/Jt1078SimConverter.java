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

package io.github.hylexus.xtream.codec.ext.jt1078.spec;

import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.PadLeadingZerosJt1078SimConverter;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.impl.RemoveLeadingZerosJt1078SimConverter;

/**
 * 1078 的 SIM 为 {@code BCD[6]} 或 {@code BCD[10]}
 * <p>
 * 内置了两个实现类:
 * <li>{@link RemoveLeadingZerosJt1078SimConverter 移除前导零的实现}（默认）</li>
 * <li>{@link PadLeadingZerosJt1078SimConverter 填充前导零的实现}</li>
 *
 * @see Jt1078RequestHeader#convertedSim()
 * @see RemoveLeadingZerosJt1078SimConverter
 * @see PadLeadingZerosJt1078SimConverter
 */
public interface Jt1078SimConverter {

    Jt1078SimConverter DEFAULT = new RemoveLeadingZerosJt1078SimConverter();

    String convert(String original);

}
