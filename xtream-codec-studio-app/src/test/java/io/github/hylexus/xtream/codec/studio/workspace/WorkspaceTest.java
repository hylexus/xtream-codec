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
import io.github.hylexus.xtream.codec.core.debugger.CodecDebuggerService;
import io.github.hylexus.xtream.codec.core.debugger.DebugEntityCatalog;
import io.github.hylexus.xtream.codec.studio.app.CodecStudioServer;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.github.hylexus.xtream.codec.studio.workspace.WorkspaceData.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 两种存储共用的目录、冲突、持久化与 HTTP 合同测试。
 *
 * @author Codex (AI)
 * @since 0.11.0
 */
public class WorkspaceTest {
    @TempDir
    Path directory;
    private final ObjectMapper mapper = new ObjectMapper();

    private WorkspaceStore open(String type) throws IOException {
        return "file".equals(type) ? new FileWorkspaceStore(directory) : new SqliteWorkspaceStore(directory.resolve("workspace.db"));
    }

    @Test
    void storageSelectionAndCustomProviderHaveNoSilentFallback() throws Exception {
        final String oldType = System.getProperty("xtream.studio.storage");
        final String oldDirectory = System.getProperty("xtream.studio.data-dir");
        try {
            System.setProperty("xtream.studio.data-dir", directory.toString());
            for (final String type : List.of("file", "sqlite", "test-provider")) {
                System.setProperty("xtream.studio.storage", type);
                try (var store = WorkspaceStores.configured()) {
                    assertEquals(0, store.read().revision());
                }
            }
            System.setProperty("xtream.studio.storage", "missing-provider");
            assertThrows(IllegalArgumentException.class, WorkspaceStores::configured);
        } finally {
            if (oldType == null) {
                System.clearProperty("xtream.studio.storage");
            } else {
                System.setProperty("xtream.studio.storage", oldType);
            }
            if (oldDirectory == null) {
                System.clearProperty("xtream.studio.data-dir");
            } else {
                System.setProperty("xtream.studio.data-dir", oldDirectory);
            }
        }
    }

    public static final class TestProvider implements WorkspaceStoreProvider {
        @Override
        public String type() {
            return "test-provider";
        }

        @Override
        public WorkspaceStore open(Path dataDirectory) throws IOException {
            return new FileWorkspaceStore(dataDirectory.resolve("custom"));
        }
    }

    @Test
    void malformedDocumentPayloadsAreRejectedBeforeStorage() {
        assertThrows(IllegalArgumentException.class, () -> new Document(1, "dynamic", document().draft(), "{", "json", Map.of()));
        final var invalid = new java.util.HashMap<>(document().draft());
        invalid.put("schema", Map.of("schemaVersion", 1, "fields", List.of(Map.of("id", "bad", "type", "unknown"))));
        assertThrows(IllegalArgumentException.class, () -> new Document(1, "dynamic", invalid, "{", "json", null));
    }

    @Test
    void failedFileWriteKeepsPublishedManifest() throws Exception {
        final String id = UUID.randomUUID().toString();
        try (var store = new FileWorkspaceStore(directory)) {
            final Snapshot before = store.commit(0, List.of(new Entry(id, null, "folder", "old", 1, 0, null)));
            final Path blocked = directory.resolve("records").resolve(id + ".2.json");
            Files.createDirectory(blocked);
            Files.writeString(blocked.resolve("block"), "占用目标路径以模拟写入失败");
            assertThrows(IOException.class, () -> store.commit(1, List.of(new Entry(id, null, "folder", "new", 2, 0, null))));
            assertEquals(before, store.read());
            Files.delete(blocked.resolve("block"));
            Files.delete(blocked);
        }
        try (var store = new FileWorkspaceStore(directory)) {
            assertEquals("old", store.read().entries().getFirst().name());
        }
    }

    @Test
    void sqliteClientsCompareWorkspaceRevisionInTheSameTransaction() throws Exception {
        final Path path = directory.resolve("shared.db");
        try (var first = new SqliteWorkspaceStore(path); var second = new SqliteWorkspaceStore(path)) {
            final Snapshot stale = second.read();
            final Snapshot committed = first.commit(0, List.of(new Entry(UUID.randomUUID().toString(), null, "folder", "first", 1, 0, null)));
            assertEquals(409, assertThrows(WorkspaceException.class, () -> second.commit(stale.revision(), List.of())).status());
            assertEquals(committed, second.read());
        }
    }

    @Test
    void workspaceHttpRequestLimitRejectsLargeBackup() throws Exception {
        try (var service = new WorkspaceService(new FileWorkspaceStore(directory));
                var server = new CodecStudioServer(new InetSocketAddress("127.0.0.1", 0), new CodecDebuggerService(new DebugEntityCatalog(Map.of())), 1024, service)) {
            server.start();
            assertEquals(413, call(server, "POST", "/import", "x".repeat(20 * 1024 * 1024)).statusCode());
            assertEquals(0, service.read().entries().size());
        }
    }

    private static Document document() {
        return new Document(1, "dynamic", Map.of("schema", Map.of("schemaVersion", 1, "fields", List.of()),
                "values", "{\"unfinished\":", "hex", "7e", "version", "2019", "entityId", ""), "{\"fields\":", "json", null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "sqlite"})
    void restartKeepsIncompleteInputsAndStableIdentities(String type) throws Exception {
        Entry saved;
        try (var service = new WorkspaceService(open(type))) {
            final Entry folder = service.create(new Edit(null, "folder", "808", 0, null));
            saved = service.create(new Edit(folder.id(), "document", "0200", 0, document()));
            assertEquals(2, service.read().revision());
        }
        try (var service = new WorkspaceService(open(type))) {
            assertEquals(saved, service.get(saved.id()));
            assertEquals("{\"fields\":", service.get(saved.id()).document().schemaText());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "sqlite"})
    void invalidMovesConflictsAndImportsNeverPartiallyCommit(String type) throws Exception {
        try (var service = new WorkspaceService(open(type))) {
            final Entry root = service.create(new Edit(null, "folder", "808", 0, null));
            final Entry child = service.create(new Edit(root.id(), "folder", "2019", 0, null));
            final Entry doc = service.create(new Edit(child.id(), "document", "0200", 0, document()));
            final Snapshot before = service.read();
            assertThrows(IllegalArgumentException.class, () -> service.update(root.id(), new Edit(child.id(), "folder", "808", 1, null)));
            assertThrows(IllegalArgumentException.class, () -> service.create(new Edit(root.id(), "folder", "2019", 0, null)));
            assertThrows(IllegalArgumentException.class, () -> service.update(child.id(), new Edit(doc.id(), "folder", "2019", 1, null)));
            assertThrows(IllegalArgumentException.class, () -> service.importBackup(new Backup("studio-workspace", 1,
                    List.of(new Entry(UUID.randomUUID().toString(), "missing", "document", "bad", 1, 0, document())))));
            assertThrows(IllegalArgumentException.class, () -> service.importBackup(new Backup("studio-workspace", 1,
                    List.of(new Entry("1-1-1-1-1", null, "folder", "bad UUID", 1, 0, null)))));
            assertEquals(before, service.read());
            final Entry updated = service.update(doc.id(), new Edit(root.id(), "document", "0200 新", 1, document()));
            assertEquals(2, updated.revision());
            assertEquals(409, assertThrows(WorkspaceException.class,
                    () -> service.update(doc.id(), new Edit(null, "document", "旧客户端", 1, document()))).status());
            assertEquals(409, assertThrows(WorkspaceException.class,
                    () -> service.delete(root.id(), root.revision(), before.revision())).status());
            final Snapshot latest = service.read();
            assertEquals(0, service.delete(root.id(), root.revision(), latest.revision()).entries().size());
            assertEquals(404, assertThrows(WorkspaceException.class, () -> service.get(doc.id())).status());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "sqlite"})
    void backupRemapsIdentitiesAndPreservesTree(String type) throws Exception {
        try (var service = new WorkspaceService(open(type))) {
            final Entry folder = service.create(new Edit(null, "folder", "808", 0, null));
            final Entry doc = service.create(new Edit(folder.id(), "document", "0200", 0, document()));
            final Snapshot imported = service.importBackup(service.export());
            assertEquals(5, imported.entries().size());
            final Entry copy = imported.entries().stream().filter(entry -> entry.kind().equals("document") && !entry.id().equals(doc.id())).findFirst().orElseThrow();
            assertNotEquals(doc.parentId(), copy.parentId());
            assertEquals(doc.document(), copy.document());
            final Entry copiedFolder = service.get(copy.parentId());
            assertEquals("808", copiedFolder.name());
            assertNotNull(copiedFolder.parentId());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "sqlite"})
    void storeCommitRejectsStaleWorkspaceAndRollsBack(String type) throws Exception {
        try (var store = open(type)) {
            store.commit(0, List.of(new Entry(UUID.randomUUID().toString(), null, "folder", "first", 1, 0, null)));
            assertEquals(409, assertThrows(WorkspaceException.class, () -> store.commit(0, List.of())).status());
            assertEquals(1, store.read().entries().size());
        }
        if ("file".equals(type)) {
            Files.writeString(directory.resolve("records").resolve(UUID.randomUUID() + ".1.json"), "orphan");
        }
        try (var store = open(type)) {
            assertEquals(1, store.read().revision());
            assertEquals(1, store.read().entries().size());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "sqlite"})
    void httpSaveIndexConflictBackupAndRestart(String type) throws Exception {
        String id;
        try (var service = new WorkspaceService(open(type));
                var server = new CodecStudioServer(new InetSocketAddress("127.0.0.1", 0), new CodecDebuggerService(new DebugEntityCatalog(Map.of())), 1024, service)) {
            server.start();
            final var created = call(server, "POST", "/entries", new Edit(null, "document", "0200", 0, document()));
            assertEquals(201, created.statusCode());
            id = mapper.readTree(created.body()).get("id").asText();
            final var index = mapper.readTree(call(server, "GET", "", null).body());
            assertTrue(index.get("entries").get(0).get("document").isNull());
            assertEquals(200, call(server, "PUT", "/entries/" + id, new Edit(null, "document", "0200", 1, document())).statusCode());
            assertEquals(409, call(server, "PUT", "/entries/" + id, new Edit(null, "document", "0200", 1, document())).statusCode());
            assertEquals(400, call(server, "POST", "/import", Map.of("format", "studio-workspace", "formatVersion", 2, "entries", List.of())).statusCode());
            assertEquals(1, mapper.readTree(call(server, "GET", "", null).body()).get("entries").size());
            final Backup backup = mapper.readValue(call(server, "GET", "/export", null).body(), Backup.class);
            assertEquals(201, call(server, "POST", "/import", backup).statusCode());
        }
        try (var service = new WorkspaceService(open(type));
                var server = new CodecStudioServer(new InetSocketAddress("127.0.0.1", 0), new CodecDebuggerService(new DebugEntityCatalog(Map.of())), 1024, service)) {
            server.start();
            final Entry entry = mapper.readValue(call(server, "GET", "/entries/" + id, null).body(), Entry.class);
            assertEquals(2, entry.revision());
            assertEquals(document(), entry.document());
        }
    }

    private HttpResponse<String> call(CodecStudioServer server, String method, String suffix, Object body) throws Exception {
        final var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + "/api/v1/workspace" + suffix));
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        }
        try (var client = HttpClient.newHttpClient()) {
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
