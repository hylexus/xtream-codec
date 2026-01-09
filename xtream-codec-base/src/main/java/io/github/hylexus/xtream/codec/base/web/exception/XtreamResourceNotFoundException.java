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
import org.jspecify.annotations.Nullable;

public class XtreamResourceNotFoundException extends XtreamHttpException {

    public XtreamResourceNotFoundException(@Nullable String message) {
        super(null, XtreamApiErrorCode.NOT_FOUND, message, null);
    }

}
