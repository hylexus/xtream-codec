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

package io.github.hylexus.xtream.codec.common.exception;

/**
 * 这个类主要用来包装那些 不得不处理但又不知道怎么处理的 "受检异常"。
 * <p>
 * 比如文件读写异常，实现者可能不知道怎么处理，只能将异常抛出，但是这种 "受检异常" 要强制在方法签名上声明。
 * <p>
 * 为了避免这种强制策略，所以将其包装为 "非受检异常" 再抛出。
 *
 * @author hylexus
 * @see "io.github.hylexus.xtream.codec.server.reactive.spec.impl.ExceptionHandlingXtreamHandler#handle(io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange)"
 * @see "io.github.hylexus.xtream.codec.server.reactive.spec.impl.DispatcherXtreamHandler#handleResultMono(io.github.hylexus.xtream.codec.server.reactive.spec.XtreamExchange, reactor.core.publisher.Mono)"
 */
public class XtreamWrappedRuntimeException extends RuntimeException {

    public static Throwable unwrapIfNecessary(Throwable throwable) {
        if (throwable instanceof XtreamWrappedRuntimeException wrappedRuntimeException) {
            return wrappedRuntimeException.cause;
        }
        return throwable;
    }

    private final Throwable cause;

    public XtreamWrappedRuntimeException(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

}
