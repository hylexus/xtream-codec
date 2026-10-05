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

package io.github.hylexus.xtream.codec.studio.workspace;

/**
 * 工作区中可向调用方报告的冲突或不存在错误。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class WorkspaceException extends RuntimeException {
    private final int status;

    public WorkspaceException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
