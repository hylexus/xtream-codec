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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.hylexus.xtream.codec.base.web.exception.XtreamHttpErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author hylexus
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC-7807</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface XtreamApiProblemDetails {
    URI BLANK_TYPE = URI.create("about:blank");

    // region RFC-7807 标准字段
    default URI getType() {
        return BLANK_TYPE;
    }

    String getTitle();

    int getStatus();

    String getDetail();

    /**
     * 目前暂时用不到这个属性(不会输出到客户端)
     */
    default URI getInstance() {
        return null;
    }
    // endregion RFC-7807 标准字段

    // region RFC-7807 的基础上扩展的字段
    default Throwable getCause() {
        return null;
    }

    String getErrorCode();

    List<? extends XtreamHttpErrorDetails> getErrorDetails();
    // endregion RFC-7807 的基础上扩展的字段

    @JsonValue
    default Map<String, Object> jsonValue() {
        return jsonValue(true, true);
    }

    default Map<String, Object> jsonValue(boolean withErrorDetails, boolean withTimestamp) {
        final Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("type", this.getType());
        resp.put("title", this.getTitle());
        resp.put("status", this.getStatus());
        resp.put("detail", this.getDetail());
        if (this.getInstance() != null) {
            resp.put("instance", this.getInstance());
        }
        if (withTimestamp) {
            resp.put("timestamp", Instant.now());
        }
        resp.put("errorCode", this.getErrorCode());
        if (withErrorDetails && this.getErrorDetails() != null) {
            resp.put("errorDetails", this.getErrorDetails());
        }
        return resp;
    }

    static XtreamApiProblemDetails badRequest(List<? extends XtreamHttpErrorDetails> details) {
        return badRequest("Param Error", details);
    }

    static XtreamApiProblemDetails badRequest(String message, List<? extends XtreamHttpErrorDetails> details) {
        return new DefaultXtreamApiProblemDetails(XtreamApiErrorCode.BAD_REQUEST, message, details);
    }

    static XtreamApiProblemDetails of(HttpStatusCode httpStatusCode, String code, String message, List<? extends XtreamHttpErrorDetails> details) {
        return new DefaultXtreamApiProblemDetails(
                httpStatusCode.value(),
                httpStatusCode.toString(),
                code,
                message,
                details
        );
    }

    static XtreamApiProblemDetails of(XtreamApiErrorCode errorCode, String message, List<? extends XtreamHttpErrorDetails> details) {
        return new DefaultXtreamApiProblemDetails(errorCode, message, details);
    }

    default ResponseEntity<XtreamApiProblemDetails> toResponseEntity() {
        return ResponseEntity
                .status(this.getStatus())
                .body(this);
    }

    default Mono<ServerResponse> toServerResponse() {
        return ServerResponse.status(this.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(this.jsonValue()));
    }

    static XtreamApiProblemDetailsBuilder newBuilder() {
        return new DefaultXtreamApiProblemDetails();
    }

    static XtreamApiProblemDetailsBuilder newBuilderFrom(XtreamApiErrorCode errorCode) {
        return new DefaultXtreamApiProblemDetails().errorCode(errorCode.getErrorCode())
                .title(errorCode.getTitle())
                .status(errorCode.getStatus())
                .detail(errorCode.getDetail())
                .errorCode(errorCode.getErrorCode());
    }

    interface XtreamApiProblemDetailsBuilder {
        XtreamApiProblemDetailsBuilder type(URI type);

        XtreamApiProblemDetailsBuilder title(String title);

        XtreamApiProblemDetailsBuilder status(int status);

        default XtreamApiProblemDetailsBuilder status(HttpStatusCode status) {
            return this.status(status.value())
                    .title(parseHttpStatusTitle(status));
        }

        XtreamApiProblemDetailsBuilder detail(String detail);

        XtreamApiProblemDetailsBuilder instance(URI instance);

        XtreamApiProblemDetailsBuilder cause(Throwable cause);

        XtreamApiProblemDetailsBuilder errorCode(String errorCode);

        XtreamApiProblemDetailsBuilder errorDetails(List<? extends XtreamHttpErrorDetails> errorDetails);

        XtreamApiProblemDetails build();

        ExceptionDelegateXtreamApiProblemDetails buildAsException();
    }

    static String parseHttpStatusTitle(HttpStatusCode statusCode) {
        if (statusCode instanceof HttpStatus httpStatus) {
            return httpStatus.getReasonPhrase();
        }
        final HttpStatus resolved = HttpStatus.resolve(statusCode.value());
        if (resolved != null) {
            return resolved.getReasonPhrase();
        }
        return statusCode.toString();
    }

}
