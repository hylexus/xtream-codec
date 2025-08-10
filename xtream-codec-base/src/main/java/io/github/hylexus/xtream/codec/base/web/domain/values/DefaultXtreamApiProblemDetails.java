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
import java.util.Objects;

public class DefaultXtreamApiProblemDetails implements XtreamApiProblemDetails, XtreamApiProblemDetails.XtreamApiProblemDetailsBuilder {
    private URI type;
    private String title;
    private int status;
    private String detail;
    private URI instance;
    protected Throwable cause;
    private String errorCode;
    private List<? extends XtreamHttpErrorDetails> errorDetails;

    public DefaultXtreamApiProblemDetails() {
        this.type = BLANK_TYPE;
    }

    public DefaultXtreamApiProblemDetails(XtreamApiErrorCode errorCode, String detail, List<? extends XtreamHttpErrorDetails> errorDetails) {
        this(errorCode.getStatus(), errorCode.getTitle(), errorCode.getErrorCode(), detail, errorDetails);
    }

    public DefaultXtreamApiProblemDetails(int status, String title, String errorCode, String detail, List<? extends XtreamHttpErrorDetails> errorDetails) {
        this.type = BLANK_TYPE;
        this.status = status;
        this.title = title;
        this.errorCode = errorCode;
        this.detail = detail;
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
        return this.detail;
    }

    @Override
    public URI getInstance() {
        return this.instance;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public List<? extends XtreamHttpErrorDetails> getErrorDetails() {
        return this.errorDetails;
    }

    @Override
    public XtreamApiProblemDetailsBuilder type(URI type) {
        this.type = type;
        return this;
    }

    @Override
    public XtreamApiProblemDetailsBuilder title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public XtreamApiProblemDetailsBuilder status(int status) {
        this.status = status;
        return this;
    }

    @Override
    public XtreamApiProblemDetailsBuilder detail(String detail) {
        this.detail = detail;
        return this;
    }

    @Override
    public XtreamApiProblemDetailsBuilder instance(URI instance) {
        this.instance = instance;
        return this;
    }

    @Override
    public XtreamApiProblemDetailsBuilder cause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public XtreamApiProblemDetailsBuilder errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    @Override
    public XtreamApiProblemDetailsBuilder errorDetails(List<? extends XtreamHttpErrorDetails> errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }

    @Override
    public XtreamApiProblemDetails build() {
        Objects.requireNonNull(this.title);
        // Objects.requireNonNull(this.detail);
        // Objects.requireNonNull(this.errorCode);
        return this;
    }

    @Override
    public ExceptionDelegateXtreamApiProblemDetails buildAsException() {
        Objects.requireNonNull(this.title);
        // Objects.requireNonNull(this.detail);
        // Objects.requireNonNull(this.errorCode);
        // Objects.requireNonNull(this.cause);
        return new ExceptionDelegateXtreamApiProblemDetails(this, this.cause);
    }

}
