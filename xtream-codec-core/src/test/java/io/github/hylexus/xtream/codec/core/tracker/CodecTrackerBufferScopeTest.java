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

package io.github.hylexus.xtream.codec.core.tracker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * 临时缓冲区游标、子树迁移及坐标作用域恢复测试。
 *
 * @author Codex (AI)
 */
class CodecTrackerBufferScopeTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 10})
    void relocatesNestedTemporarySubtreesWithoutMovingLaterSiblings(int origin) {
        final CodecTracker tracker = new CodecTracker();
        tracker.beginEncode(origin, "Message");
        field(tracker, "head", "07", origin, origin + 1);
        try (final CodecTracker.TraceScope body = tracker.enterScope(CodecTraceNodeKind.NESTED_FIELD, "body", null, null, null, origin + 1)) {
            final CodecTracker.TraceCheckpoint outer = tracker.checkpoint();
            try (final CodecTracker.TemporaryBufferScope ignored = tracker.openTemporaryBuffer()) {
                try (final CodecTracker.TraceScope value = tracker.enterScope(CodecTraceNodeKind.NESTED_FIELD, "value", null, null, null, 0)) {
                    field(tracker, "first", "01", 0, 1);
                    final CodecTracker.TraceCheckpoint inner = tracker.checkpoint();
                    try (final CodecTracker.TemporaryBufferScope ignoredInner = tracker.openTemporaryBuffer()) {
                        try (final CodecTracker.TraceScope nested = tracker.enterScope(CodecTraceNodeKind.NESTED_FIELD, "nested", null, null, null, 0)) {
                            field(tracker, "second", "02", 0, 1);
                            field(tracker, "third", "03", 1, 2);
                            nested.completeWithHex(null, "0203", 2);
                        }
                    }
                    inner.captureNewChildren();
                    field(tracker, "innerLength", "02", 1, 2);
                    inner.captureNewChildren().relocateNewChildren(2);
                    value.completeWithHex(null, "01020203", 4);
                }
            }
            outer.captureNewChildren();
            field(tracker, "outerLength", "04", origin + 1, origin + 2);
            outer.captureNewChildren().relocateNewChildren(origin + 2);
            body.completeWithHex(null, "0401020203", origin + 6);
        }
        field(tracker, "tail", "7e", origin + 6, origin + 7);
        tracker.finishTrace("0704010202037e", origin + 7);

        final CodecTraceNode body = tracker.getTrace().getRoot().getChildren().get(1);
        assertRange(body, 1, 6);
        final CodecTraceNode value = body.getChildren().getFirst();
        assertRange(value, 2, 6);
        assertRange(body.getChildren().get(1), 1, 2);
        assertRange(value.getChildren().get(0), 2, 3);
        assertRange(value.getChildren().get(2), 3, 4);
        final CodecTraceNode nested = value.getChildren().get(1);
        assertRange(nested, 4, 6);
        assertRange(nested.getChildren().get(0), 4, 5);
        assertRange(nested.getChildren().get(1), 5, 6);
        assertRange(tracker.getTrace().getRoot().getChildren().get(2), 6, 7);
        assertThat(tracker.toTraceView().root().children().get(1).hex()).isEqualTo("0401020203");
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
        assertThat(tracker.isNotTracing()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10})
    void restoresEachTemporaryCursorAfterNestedBuffersClose(int origin) {
        final CodecTraceRecorder recorder = new CodecTraceRecorder();
        recorder.begin(CodecTraceDirection.ENCODE, origin, null);
        final CodecTraceNode root = recorder.trace().getRoot();
        recorder.updateLeaf(recorder.enterNode(root, CodecTraceNodeKind.FIELD, "head", origin), null, "0102", origin, origin + 2);
        final int outer = recorder.beginTemporaryBuffer();
        recorder.updateLeaf(recorder.enterNode(root, CodecTraceNodeKind.FIELD, "outer", 0), null, "aabbccdd", 0, 4);
        assertRange(root.getChildren().getLast(), 0, 4);
        final int inner = recorder.beginTemporaryBuffer();
        recorder.updateLeaf(recorder.enterNode(root, CodecTraceNodeKind.FIELD, "inner", 0), null, "ab".repeat(9), 0, 9);
        assertRange(root.getChildren().getLast(), 0, 9);
        recorder.endTemporaryBuffer(inner);

        // 无显式范围的叶节点从当前游标开始，用于观察每层临时缓冲区关闭后的恢复结果。
        final CodecTraceNode afterInner = new CodecTraceNode(CodecTraceNodeKind.FIELD, "afterInner", root);
        root.addChild(afterInner);
        recorder.updateLeaf(afterInner, null, "ee", null, null);
        assertRange(afterInner, 4, 5);
        recorder.endTemporaryBuffer(outer);
        final CodecTraceNode afterOuter = new CodecTraceNode(CodecTraceNodeKind.FIELD, "afterOuter", root);
        root.addChild(afterOuter);
        recorder.updateLeaf(afterOuter, null, "ff", null, null);
        assertRange(afterOuter, 2, 3);
    }

    @Test
    void restoresNestedTemporaryAndCoordinateScopesAfterFailure() {
        final CodecTracker tracker = new CodecTracker();
        final IllegalArgumentException failure = new IllegalArgumentException("临时编码失败");
        tracker.beginEncode(10, "Message");
        field(tracker, "head", "0102", 10, 12);
        assertThatThrownBy(() -> {
            try (final CodecTracker.TraceScope parent = tracker.enterScope(CodecTraceNodeKind.NESTED_FIELD, "body", null, null, null, 12)) {
                try (final CodecTracker.TemporaryBufferScope outer = tracker.openTemporaryBuffer();
                        final CodecTracker.CoordinateScope coordinate = tracker.openCoordinateBase(14)) {
                    try (final CodecTracker.TemporaryBufferScope inner = tracker.openTemporaryBuffer();
                            final CodecTracker.CoordinateScope nestedCoordinate = tracker.openCoordinateBase(3)) {
                        try (final CodecTracker.TraceScope child = tracker.enterScope(CodecTraceNodeKind.FIELD, "failed", null, null, null, 1)) {
                            child.fail(failure, 2);
                            throw failure;
                        }
                    }
                }
            }
        }).isSameAs(failure);

        field(tracker, "after", "7e", 19, 20);
        assertThat(tracker.getTrace().getRoot().getChildren()).extracting("name").containsExactly("head", "body", "after");
        final CodecTraceNode body = tracker.getTrace().getRoot().getChildren().get(1);
        assertThat(body.getStatus()).isEqualTo(CodecTraceStatus.ERROR);
        // 内层临时缓冲区隔离外层坐标，只累加其自身的切片偏移。
        assertRange(body.getChildren().getFirst(), 4, 5);
        assertRange(tracker.getTrace().getRoot().getChildren().get(2), 9, 10);
        tracker.recordFailure(failure, 20);
        assertThat(tracker.getTrace().getDiagnostics()).singleElement().satisfies(diagnostic -> {
            assertThat(diagnostic.byteOffset()).isEqualTo(5);
            assertThat(diagnostic.nodeId()).isEqualTo(body.getChildren().getFirst().getId());
        });
        assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.ERROR);
        assertThat(tracker.isNotTracing()).isTrue();
    }

    @Test
    void translatesNonZeroRootAndNestedSlicesWithoutAffectingOuterSibling() {
        final CodecTracker tracker = new CodecTracker();
        tracker.beginDecode(10, "Message");
        try (final CodecTracker.TraceScope body = tracker.enterScope(CodecTraceNodeKind.NESTED_FIELD, "body", null, null, null, 12)) {
            try (final CodecTracker.CoordinateScope coordinate = tracker.openCoordinateBase(14)) {
                field(tracker, "first", "01", 1, 2);
                try (final CodecTracker.CoordinateScope nested = tracker.openCoordinateBase(3)) {
                    field(tracker, "nested", "02", 1, 2);
                }
                field(tracker, "last", "03", 5, 6);
            }
            body.complete(null, 20);
        }
        field(tracker, "tail", "7e", 20, 21);
        tracker.finishTrace(null, 21);

        final CodecTraceNode body = tracker.getTrace().getRoot().getChildren().getFirst();
        assertRange(body, 2, 10);
        assertRange(body.getChildren().get(0), 5, 6);
        assertRange(body.getChildren().get(1), 8, 9);
        assertRange(body.getChildren().get(2), 9, 10);
        assertRange(tracker.getTrace().getRoot().getChildren().get(1), 10, 11);
        assertRange(tracker.getTrace().getRoot(), 0, 11);
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
        assertThat(tracker.getTrace().getRoot().getStatus()).isEqualTo(CodecTraceStatus.SUCCESS);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void rejectsCheckpointThatDoesNotContainExactlyOneNode(int count) {
        final CodecTracker tracker = new CodecTracker();
        tracker.beginEncode(0, null);
        final CodecTracker.TraceCheckpoint checkpoint = tracker.checkpoint();
        for (int index = 0; index < count; index++) {
            field(tracker, "field" + index, "01", index, index + 1);
        }
        assertThatThrownBy(checkpoint::requireSingleNode).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("实际新增 " + count + " 个");
        field(tracker, "tail", "7e", count, count + 1);
        tracker.finishTrace("01".repeat(count) + "7e", count + 1);
        assertThat(tracker.getTrace().getRoot().getChildren()).hasSize(count + 1);
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
    }

    @Test
    void requiresCheckpointCaptureBeforeRelocationAndAllowsEmptyRange() {
        final CodecTracker tracker = new CodecTracker();
        tracker.beginEncode(0, null);
        final CodecTracker.TraceCheckpoint checkpoint = tracker.checkpoint();
        assertThatThrownBy(() -> checkpoint.relocateNewChildren(5)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("captureNewChildren()");
        checkpoint.captureNewChildren();
        field(tracker, "tail", "7e", 0, 1);
        checkpoint.relocateNewChildren(5);
        tracker.finishTrace("7e", 1);
        assertThat(tracker.getTrace().getRoot().getChildren()).singleElement().satisfies(node -> assertRange(node, 0, 1));
        assertThat(tracker.getTrace().getDiagnostics()).isEmpty();
    }

    private static void field(CodecTracker tracker, String name, String hex, int start, int end) {
        try (final CodecTracker.TraceScope scope = tracker.enterScope(CodecTraceNodeKind.FIELD, name, null, null, null, start)) {
            scope.completeWithHex(null, hex, end);
        }
    }

    private static void assertRange(CodecTraceNode node, int start, int end) {
        assertThat(node.getByteStart()).as("%s 起点", node.getName()).isEqualTo(start);
        assertThat(node.getByteEnd()).as("%s 终点", node.getName()).isEqualTo(end);
    }
}
