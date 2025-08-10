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

package io.github.hylexus.xtream.codec.base.web.domain.values;

import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpErrorDetails;

import java.net.URI;
import java.util.List;

public class ExceptionDelegateXtreamApiProblemDetails extends RuntimeException implements XtreamApiProblemDetails {
    private final XtreamApiProblemDetails delegate;

    public ExceptionDelegateXtreamApiProblemDetails(XtreamApiProblemDetails delegate, Throwable cause) {
        super(delegate.getDetail(), cause);
        this.delegate = delegate;
    }

    @Override
    public URI getType() {
        return delegate.getType();
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public int getStatus() {
        return delegate.getStatus();
    }

    @Override
    public String getDetail() {
        return delegate.getDetail();
    }

    @Override
    public URI getInstance() {
        return delegate.getInstance();
    }

    @Override
    public Throwable getCause() {
        return delegate.getCause();
    }

    @Override
    public String getErrorCode() {
        return delegate.getErrorCode();
    }

    @Override
    public List<? extends XtreamHttpErrorDetails> getErrorDetails() {
        return delegate.getErrorDetails();
    }
}
