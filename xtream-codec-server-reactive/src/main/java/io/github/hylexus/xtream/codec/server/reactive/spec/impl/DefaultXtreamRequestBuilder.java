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

package io.github.hylexus.xtream.codec.server.reactive.spec.impl;

import io.github.hylexus.xtream.codec.server.reactive.spec.XtreamRequest;

/**
 * @author hylexus
 */
public class DefaultXtreamRequestBuilder
        extends AbstractXtreamRequestBuilder<XtreamRequest.XtreamRequestBuilder, XtreamRequest> {

    public DefaultXtreamRequestBuilder(XtreamRequest delegate) {
        super(delegate);
        this.remoteAddress = delegate.remoteAddress();
    }

    public XtreamRequest build() {
        return new DefaultXtreamRequest(
                this.delegateRequest.requestId(),
                this.delegateRequest.bufferFactory(),
                this.delegateRequest.underlyingInbound(),
                this.delegateRequest.type(),
                this.payload != null ? this.payload : this.delegateRequest.payload(),
                this.remoteAddress != null ? this.remoteAddress : this.delegateRequest.remoteAddress()
        );
    }

}
