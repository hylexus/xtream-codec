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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.tag;

import io.netty.buffer.ByteBuf;

import java.util.List;

import static io.github.hylexus.xtream.codec.ext.jt1078.codec.flv.tag.Amf.ofTerminator;

public interface ScriptFlvTag {

    List<Amf> metadata();

    static ScriptFlvTag of(List<Amf> metadataList) {
        return () -> metadataList;
    }

    default int writeTo(ByteBuf buffer) {
        final int writerIndex = buffer.writerIndex();
        for (Amf metadata : this.metadata()) {
            metadata.writeTo(buffer);
        }
        ofTerminator().writeTo(buffer);
        return buffer.writerIndex() - writerIndex;
    }
}
