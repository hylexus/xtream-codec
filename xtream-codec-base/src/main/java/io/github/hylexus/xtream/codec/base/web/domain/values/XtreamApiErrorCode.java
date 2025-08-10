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

/**
 * @author hylexus
 */
public enum XtreamApiErrorCode {
    SERVER_ERROR(500, "Internal Server Error", "000:500", "Server error"),
    BAD_REQUEST(400, "Bad Request", "000:400", "Bad Request"),
    NOT_FOUND(404, "Not Found", "000:404", "Not Found"),
    REQUEST_TIMEOUT(408, "Request Timeout", "000:408", "Request timeout"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout", "000:504", "Gateway timeout"),
    PROXY_ERROR(502, "Bad Gateway", "001:502", "Proxy error"),
    ;
    private final String title;
    private final int status;
    private final String detail;
    private final String errorCode;

    XtreamApiErrorCode(int status, String title, String errorCode, String detail) {
        this.status = status;
        this.title = title;
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDetail() {
        return detail;
    }

}
