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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import static io.github.hylexus.xtream.codec.studio.workspace.WorkspaceData.*;

/**
 * SQLite 工作区存储，使用事务同时提交整体版本与条目。
 * 单实例连接串行使用，数据库版本比较还保护其他进程的提交。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public final class SqliteWorkspaceStore implements WorkspaceStore {
    private final Connection connection;
    private final ObjectMapper mapper = new ObjectMapper();

    public SqliteWorkspaceStore(Path path) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
            try (var statement = connection.createStatement()) {
                statement.execute("PRAGMA busy_timeout=5000");
                statement.execute("CREATE TABLE IF NOT EXISTS workspace_meta (id INTEGER PRIMARY KEY CHECK(id=1), revision INTEGER NOT NULL, format_version INTEGER NOT NULL)");
                statement.execute("INSERT OR IGNORE INTO workspace_meta VALUES (1,0,1)");
                statement.execute("CREATE TABLE IF NOT EXISTS workspace_entries (id TEXT PRIMARY KEY, content TEXT NOT NULL)");
                try (var result = statement.executeQuery("SELECT format_version FROM workspace_meta WHERE id=1")) {
                    if (!result.next() || result.getInt(1) != 1) {
                        throw new SQLException("不支持的 SQLite 工作区版本");
                    }
                }
            } catch (SQLException exception) {
                connection.close();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IOException("初始化 SQLite 工作区失败", exception);
        }
    }

    @Override
    public synchronized Snapshot read() throws IOException {
        try {
            connection.setAutoCommit(false);
            long revision;
            final var entries = new ArrayList<Entry>();
            try (var statement = connection.createStatement()) {
                try (var result = statement.executeQuery("SELECT revision FROM workspace_meta WHERE id=1")) {
                    result.next();
                    revision = result.getLong(1);
                }
                try (var result = statement.executeQuery("SELECT content FROM workspace_entries ORDER BY rowid")) {
                    while (result.next()) {
                        entries.add(mapper.readValue(result.getString(1), Entry.class));
                    }
                }
            }
            connection.commit();
            return new Snapshot(revision, entries);
        } catch (SQLException exception) {
            throw new IOException("读取 SQLite 工作区失败", exception);
        } finally {
            resetTransaction();
        }
    }

    @Override
    public synchronized Snapshot commit(long expectedRevision, List<Entry> entries) throws IOException {
        try {
            connection.setAutoCommit(false);
            // 先取得写事务并比较版本，避免读取后升级事务时遗漏并发提交。
            try (var update = connection.prepareStatement("UPDATE workspace_meta SET revision=revision+1 WHERE id=1 AND revision=?")) {
                update.setLong(1, expectedRevision);
                if (update.executeUpdate() != 1) {
                    throw new WorkspaceException(409, "工作区已变化，请刷新后重试");
                }
            }
            final var retained = new HashSet<String>();
            entries.forEach(entry -> retained.add(entry.id()));
            try (var statement = connection.createStatement();
                    var result = statement.executeQuery("SELECT id FROM workspace_entries");
                    var delete = connection.prepareStatement("DELETE FROM workspace_entries WHERE id=?")) {
                while (result.next()) {
                    if (!retained.contains(result.getString(1))) {
                        delete.setString(1, result.getString(1));
                        delete.addBatch();
                    }
                }
                delete.executeBatch();
            }
            // 未改变的记录不重写，保存一个文档不会重新写入其他文档的大型 Trace。
            try (var insert = connection.prepareStatement("INSERT INTO workspace_entries(id,content) VALUES (?,?) ON CONFLICT(id) DO UPDATE SET content=excluded.content WHERE workspace_entries.content<>excluded.content")) {
                for (final Entry entry : entries) {
                    insert.setString(1, entry.id());
                    insert.setString(2, mapper.writeValueAsString(entry));
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            connection.commit();
            return new Snapshot(expectedRevision + 1, entries);
        } catch (SQLException exception) {
            throw new IOException("保存 SQLite 工作区失败", exception);
        } finally {
            resetTransaction();
        }
    }

    private void resetTransaction() throws IOException {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException exception) {
            throw new IOException("结束 SQLite 工作区事务失败", exception);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException exception) {
            throw new IOException(exception);
        }
    }
}
