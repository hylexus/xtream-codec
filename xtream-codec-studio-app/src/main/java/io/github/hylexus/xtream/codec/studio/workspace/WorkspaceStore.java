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

import java.io.IOException;
import java.util.List;

/**
 * 工作区的原子存储边界。实现必须支持并发读取和带版本比较的完整提交。
 * 冲突抛出状态为 409 的 WorkspaceException，失败时保留原快照；成功后整体版本加一。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public interface WorkspaceStore extends AutoCloseable {
    WorkspaceData.Snapshot read() throws IOException;

    WorkspaceData.Snapshot commit(long expectedRevision, List<WorkspaceData.Entry> entries) throws IOException;

    @Override
    void close() throws IOException;
}
