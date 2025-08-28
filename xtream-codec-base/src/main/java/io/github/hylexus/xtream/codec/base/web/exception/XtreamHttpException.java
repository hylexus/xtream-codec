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

package io.github.hylexus.xtream.codec.base.web.exception;


import io.github.hylexus.xtream.codec.base.web.domain.values.XtreamApiErrorCode;
import io.github.hylexus.xtream.codec.base.web.domain.values.XtreamApiProblemDetails;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.List;

public class XtreamHttpException extends RuntimeException implements XtreamApiProblemDetails {

    protected URI type = BLANK_TYPE;
    protected String title;
    protected int status;

    protected @Nullable URI instance;
    protected String errorCode;
    protected @Nullable List<? extends XtreamHttpErrorDetails> errorDetails;

    public XtreamHttpException(@Nullable Throwable cause, XtreamApiErrorCode apiErrorCode, @Nullable String detail, @Nullable List<? extends XtreamHttpErrorDetails> errorDetails) {
        this(cause, BLANK_TYPE, apiErrorCode.getTitle(), apiErrorCode.getStatus(), detail, null, apiErrorCode.getErrorCode(), errorDetails);
    }

    public XtreamHttpException(@Nullable Throwable cause, URI type, String title, int status, @Nullable String detail, @Nullable URI instance, String errorCode, @Nullable List<? extends XtreamHttpErrorDetails> errorDetails) {
        super(detail, cause);
        this.type = type;
        this.title = title;
        this.status = status;
        this.instance = instance;
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    @Override
    public URI getType() {
        return this.type;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getDetail() {
        return this.getMessage();
    }

    @Override
    @Nullable
    public URI getInstance() {
        return this.instance;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    @Nullable
    public List<? extends XtreamHttpErrorDetails> getErrorDetails() {
        return this.errorDetails;
    }

    public static XtreamHttpException notFound(@Nullable String error) {
        return new XtreamResourceNotFoundException(error);
    }

    public static XtreamHttpException timeout(@Nullable String error) {
        return new XtreamHttpGatewayTimeoutException(error);
    }

    public static XtreamHttpException badRequest(@Nullable String error) {
        return new XtreamBadRequestException(error);
    }

}
