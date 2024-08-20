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

package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.common.exception.NotYetImplementedException;
import io.netty.buffer.ByteBuf;
import org.springframework.expression.EvaluationContext;

public interface FieldCodec<T> {

    T deserialize(DeserializeContext context, ByteBuf input, int length);

    void serialize(SerializeContext context, ByteBuf output, T value);

    default Class<?> underlyingJavaType() {
        throw new NotYetImplementedException();
    }

    interface CodecContext {

        Object containerInstance();

        EvaluationContext evaluationContext();

    }

    interface DeserializeContext extends CodecContext {
        EntityDecoder entityDecoder();
    }

    interface SerializeContext extends CodecContext {

        EntityEncoder entityEncoder();

    }

    class Placeholder implements FieldCodec<Object> {

        @Override
        public Object deserialize(DeserializeContext context, ByteBuf input, int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(SerializeContext context, ByteBuf output, Object value) {
            throw new UnsupportedOperationException();
        }
    }
}
