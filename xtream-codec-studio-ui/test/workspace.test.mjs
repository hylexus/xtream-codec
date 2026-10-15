import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  acceptSave,
  canMoveEntry,
  completeRun,
  descendants,
  fingerprint,
  inputSignature,
  isDirty,
  isStale,
  migrateLegacy,
  moveTab,
  newTab,
  parseDocument,
  pinTab,
  removeDocumentTab,
  restoreSession,
} from '../src/workspace.ts';
import { initialDrafts } from '../src/presets.ts';

const response = {
  success: true,
  hex: '01',
  values: { x: 1 },
  consumedBits: 8,
  consumedBytes: 1,
  remainingHex: '',
  checksums: [],
  trace: {
    nodes: [],
    root: {
      id: 'root',
      name: 'root',
      status: 'SUCCESS',
      children: [],
      diagnostics: [],
      attributes: {},
    },
    nodeIdsByByteOffset: {},
  },
};

test('HTTP 环境缺少 randomUUID 时仍能初始化、迁移和新建草稿', (t) => {
  const descriptor = Object.getOwnPropertyDescriptor(crypto, 'randomUUID');
  Object.defineProperty(crypto, 'randomUUID', { configurable: true, value: undefined });
  t.after(() => {
    if (descriptor) Object.defineProperty(crypto, 'randomUUID', descriptor);
    else delete crypto.randomUUID;
  });
  const initial = migrateLegacy(null);
  const migrated = migrateLegacy(JSON.stringify({ drafts: initialDrafts() }));
  const tabs = [...initial.tabs, ...migrated.tabs, newTab()];
  assert.equal(tabs.length, 5);
  assert.equal(new Set(tabs.map((tab) => tab.id)).size, tabs.length);
  for (const tab of tabs) {
    assert.match(tab.id, /^[\da-f]{8}-[\da-f]{4}-4[\da-f]{3}-[89ab][\da-f]{3}-[\da-f]{12}$/);
  }
  assert.deepEqual(restoreSession(initial), initial);
});

test('标签和复制的文档值独立，切换时保留未完成 JSON', () => {
  const first = newTab();
  const second = newTab();
  first.data.schemaText = '{"fields":';
  first.data.schemaTab = 'json';
  first.data.draft.values = '{';
  assert.notEqual(first.id, second.id);
  assert.notEqual(first.data.draft.values, second.data.draft.values);
  assert.equal(parseDocument(first.data).schemaText, '{"fields":');
  const copy = structuredClone(first.data);
  copy.draft.schema.fields[0].id = 'changed';
  assert.notEqual(copy.draft.schema.fields[0].id, first.data.draft.schema.fields[0].id);
});

test('固定标签按分组排列，不改变激活状态、输入或保存基线', () => {
  const tabs = ['a', 'b', 'c'].map((name) => newTab('dynamic', name));
  tabs[1].saved = fingerprint(tabs[1].data);
  const session = { formatVersion: 1, tabs, activeId: tabs[0].id, sidebarCollapsed: false };
  const pinned = pinTab(session, tabs[1].id, true);
  assert.deepEqual(
    pinned.tabs.map((tab) => tab.name),
    ['b', 'a', 'c'],
  );
  assert.equal(pinned.activeId, session.activeId);
  assert.equal(pinned.tabs[0].data, tabs[1].data);
  assert.equal(isDirty(pinned.tabs[0]), false);
  assert.equal(session.tabs[1].pinned, false);
  const twoPinned = pinTab(pinned, tabs[2].id, true);
  assert.deepEqual(
    twoPinned.tabs.map((tab) => tab.name),
    ['b', 'c', 'a'],
  );
  const unpinned = pinTab(twoPinned, tabs[1].id, false);
  assert.deepEqual(
    unpinned.tabs.map((tab) => tab.name),
    ['c', 'b', 'a'],
  );
});

test('标签双向排序限定在固定或普通分组，内容与激活标签保持独立', () => {
  const tabs = ['a', 'b', 'c', 'd'].map((name) => newTab('dynamic', name));
  tabs[0].pinned = tabs[1].pinned = true;
  tabs[2].data.schemaText = '{"unfinished":';
  const session = { formatVersion: 1, tabs, activeId: tabs[2].id, sidebarCollapsed: false };
  const moved = moveTab(session, tabs[3].id, 0);
  assert.deepEqual(
    moved.tabs.map((tab) => tab.name),
    ['a', 'b', 'd', 'c'],
  );
  assert.equal(moved.activeId, tabs[2].id);
  assert.equal(moved.tabs[3].data.schemaText, '{"unfinished":');
  assert.equal(moved.tabs[3], tabs[2]);
  const fixedMove = moveTab(moved, tabs[0].id, 99);
  assert.deepEqual(
    fixedMove.tabs.map((tab) => tab.name),
    ['b', 'a', 'd', 'c'],
  );
  assert.deepEqual(
    moveTab(fixedMove, tabs[2].id, 2).tabs.map((tab) => tab.name),
    ['b', 'a', 'c', 'd'],
  );
  assert.equal(moveTab(session, 'missing', 0), session);
  assert.deepEqual(
    session.tabs.map((tab) => tab.name),
    ['a', 'b', 'c', 'd'],
  );
});

test('关闭最后一个标签保持空会话，恢复和迟到回包不会新建报文', () => {
  const tab = newTab();
  tab.pinned = true;
  const session = { formatVersion: 1, tabs: [tab], activeId: tab.id, sidebarCollapsed: true };
  const closed = removeDocumentTab(session, tab.id);
  assert.deepEqual(closed.tabs, []);
  assert.equal(closed.activeId, '');
  assert.deepEqual(restoreSession(JSON.parse(JSON.stringify(closed))), closed);
  const afterResponse = closed.tabs.map((item) =>
    item.id === tab.id ? completeRun(item, tab.data, 'encode', response) : item,
  );
  assert.deepEqual(afterResponse, []);
  assert.equal(removeDocumentTab(closed, tab.id), closed);
});

test('恢复旧会话默认未固定，并恢复固定分组、顺序和有效激活标签', () => {
  const tabs = ['a', 'b', 'c'].map((name) => newTab('dynamic', name));
  delete tabs[0].pinned;
  tabs[1].pinned = true;
  const session = { formatVersion: 1, tabs, activeId: tabs[2].id, sidebarCollapsed: false };
  const restored = restoreSession(JSON.parse(JSON.stringify(session)));
  assert.deepEqual(
    restored.tabs.map((tab) => tab.name),
    ['b', 'a', 'c'],
  );
  assert.equal(restored.tabs[1].pinned, false);
  assert.equal(restored.activeId, tabs[2].id);
  const removed = removeDocumentTab(restored, tabs[0].id);
  assert.equal(removed.activeId, tabs[2].id);
  assert.equal(removeDocumentTab(removed, tabs[2].id).activeId, tabs[1].id);
  assert.equal(restoreSession({ ...session, activeId: 'missing' }).activeId, tabs[1].id);
});

test('跨标签回包只更新发起者，并保留执行期间的编辑', () => {
  const first = newTab();
  const second = newTab();
  const submitted = structuredClone(first.data);
  first.data.draft.values = '{"new":2}';
  first.data.draft.hex = 'ff';
  const tabs = [first, second].map((tab) =>
    tab.id === first.id ? completeRun(tab, submitted, 'encode', response) : tab,
  );
  assert.equal(tabs[0].data.run.result.hex, '01');
  assert.equal(tabs[0].data.draft.hex, 'ff');
  assert.equal(tabs[0].data.draft.values, '{"new":2}');
  assert.equal(tabs[1].data.run, null);
  assert.ok(isStale(tabs[0].data));
  const fresh = completeRun(newTab(), submitted, 'encode', response);
  assert.equal(fresh.data.draft.hex, '01');
  assert.equal(isStale(fresh.data), false);
});

test('保存期间继续编辑不丢失，确认基线保留未保存状态', () => {
  const tab = newTab();
  const submitted = structuredClone(tab.data);
  tab.data.schemaText = '{"unfinished":';
  const saved = acceptSave(
    tab,
    { id: 'server', parentId: 'folder', name: '0200', revision: 2 },
    submitted,
  );
  assert.equal(saved.revision, 2);
  assert.equal(saved.saved, fingerprint(submitted));
  assert.equal(saved.data.schemaText, '{"unfinished":');
  assert.ok(isDirty(saved));
  assert.equal(
    isDirty(
      acceptSave(
        saved,
        { id: 'server', parentId: 'folder', name: '0200', revision: 3 },
        saved.data,
      ),
    ),
    false,
  );
});

test('切换结构编辑方式属于视图操作，不产生未保存标记', () => {
  const tab = newTab();
  tab.saved = fingerprint(tab.data);
  tab.data.schemaTab = 'json';
  assert.equal(isDirty(tab), false);
});

test('旧草稿逐个迁移，单个损坏草稿不影响其他模式恢复', () => {
  const old = initialDrafts();
  old.field.schema = null;
  old.entity.values = '{"unfinished":';
  const session = migrateLegacy(
    JSON.stringify({ mode: 'entity', drafts: old, sidebarCollapsed: true }),
  );
  assert.equal(session.tabs.length, 2);
  assert.equal(session.tabs.find((tab) => tab.id === session.activeId).data.mode, 'entity');
  assert.equal(session.tabs[1].data.draft.values, '{"unfinished":');
  assert.ok(session.sidebarCollapsed);
  assert.equal(migrateLegacy('invalid').tabs.length, 1);
});

test('完整文档往返包含快照，大整数输入原文和未完成 DSL', () => {
  const tab = newTab();
  tab.data = completeRun(tab, tab.data, 'encode', response).data;
  tab.data.draft.values = '{"big":18446744073709551615}';
  tab.data.schemaText = '{';
  const restored = parseDocument(JSON.parse(JSON.stringify(tab.data)));
  assert.deepEqual(restored, tab.data);
  assert.throws(() => parseDocument({ ...restored, formatVersion: 9 }), /无效/);
  assert.throws(() => parseDocument({ ...restored, run: { ...restored.run, result: {} } }), /快照/);
});

test('输入变化标记按方向比较，目录后代集合用于移动和递归删除', () => {
  const data = newTab().data;
  assert.notEqual(
    inputSignature(data, 'encode'),
    inputSignature({ ...data, draft: { ...data.draft, values: '{}' } }, 'encode'),
  );
  assert.equal(
    inputSignature(data, 'encode'),
    inputSignature({ ...data, draft: { ...data.draft, hex: 'ff' } }, 'encode'),
  );
  const entries = [
    { id: 'a', parentId: null },
    { id: 'b', parentId: 'a' },
    { id: 'c', parentId: 'b' },
    { id: 'd', parentId: null },
  ];
  assert.deepEqual([...descendants(entries, 'a')], ['a', 'b', 'c']);
});

test('文件树移动只接受新的目录归属，拒绝自身、后代及非目录目标', () => {
  const entries = [
    { id: 'a', parentId: null, kind: 'folder' },
    { id: 'b', parentId: 'a', kind: 'folder' },
    { id: 'c', parentId: 'b', kind: 'folder' },
    { id: 'file', parentId: 'a', kind: 'document' },
    { id: 'other', parentId: null, kind: 'folder' },
  ];
  assert.equal(canMoveEntry(entries, 'file', 'b'), true);
  assert.equal(canMoveEntry(entries, 'a', 'other'), true);
  assert.equal(canMoveEntry(entries, 'b', null), true);
  for (const target of ['a', 'b', 'c', 'file', 'missing', null]) {
    assert.equal(canMoveEntry(entries, 'a', target), false, `根目录 a 的无效落点 ${target}`);
  }
  assert.equal(canMoveEntry(entries, 'file', 'a'), false);
  assert.equal(canMoveEntry(entries, 'file', 'file'), false);
  assert.equal(canMoveEntry(entries, 'missing', 'other'), false);
});
