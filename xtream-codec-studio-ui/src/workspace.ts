import { parseSchema, pretty } from './model';
import { initialDrafts } from './presets';
import type { CodecResult, Draft, Mode } from './types';

export interface DocumentData {
  formatVersion: 1;
  mode: Mode;
  draft: Draft;
  schemaText: string;
  schemaTab: 'visual' | 'json';
  run: {
    direction: 'encode' | 'decode';
    signature: string;
    executedAt: number;
    result: CodecResult;
  } | null;
}
export interface DocumentTab {
  id: string;
  pinned?: boolean;
  entryId: string | null;
  parentId: string | null;
  name: string;
  revision: number;
  saved: string | null;
  data: DocumentData;
}
export interface WorkspaceEntry {
  id: string;
  parentId: string | null;
  kind: 'folder' | 'document';
  name: string;
  revision: number;
  updatedAt: number;
  document: DocumentData | null;
}
export interface WorkspaceIndex {
  revision: number;
  entries: WorkspaceEntry[];
}
export interface Session {
  formatVersion: 1;
  tabs: DocumentTab[];
  activeId: string;
  sidebarCollapsed: boolean;
}

export const MODE_TITLES = { dynamic: '自由组合', entity: '实体类调试', field: '单字段试验' };

export function newTab(
  mode: Mode = 'dynamic',
  name = '未命名报文',
  draft = initialDrafts()[mode],
): DocumentTab {
  return {
    id: crypto.randomUUID(),
    pinned: false,
    entryId: null,
    parentId: null,
    name,
    revision: 0,
    saved: null,
    data: {
      formatVersion: 1,
      mode,
      draft: structuredClone(draft),
      schemaText: pretty(draft.schema),
      schemaTab: 'visual',
      run: null,
    },
  };
}

// 固定和顺序属于浏览器工作区状态，不参与文档的保存基线。
export function pinTab(session: Session, id: string, pinned: boolean): Session {
  const tabs = session.tabs.map((tab) => (tab.id === id ? { ...tab, pinned } : tab));
  return {
    ...session,
    tabs: [...tabs.filter((tab) => tab.pinned), ...tabs.filter((tab) => !tab.pinned)],
  };
}

// targetIndex 为移动后的索引；限制在各自分组内，固定标签始终位于左侧。
export function moveTab(session: Session, id: string, targetIndex: number): Session {
  const index = session.tabs.findIndex((tab) => tab.id === id);
  if (index < 0) return session;
  const tab = session.tabs[index];
  const pinnedCount = session.tabs.filter((item) => item.pinned).length;
  const first = tab.pinned ? 0 : pinnedCount;
  const last = tab.pinned ? pinnedCount - 1 : session.tabs.length - 1;
  const target = Math.max(first, Math.min(last, targetIndex));
  if (target === index) return session;
  const tabs = session.tabs.filter((item) => item.id !== id);
  tabs.splice(target, 0, tab);
  return { ...session, tabs };
}

// 调用方先完成未保存确认；最后一个标签关闭后保留空工作区。
export function removeDocumentTab(session: Session, id: string): Session {
  const index = session.tabs.findIndex((tab) => tab.id === id);
  if (index < 0) return session;
  const tabs = session.tabs.filter((tab) => tab.id !== id);
  return {
    ...session,
    tabs,
    activeId: session.activeId === id ? tabs[Math.max(0, index - 1)]?.id || '' : session.activeId,
  };
}

export function restoreSession(session: Session): Session {
  if (session.formatVersion !== 1 || !Array.isArray(session.tabs))
    throw new Error('不支持的本地草稿版本');
  for (const tab of session.tabs) parseDocument(tab.data);
  const tabs = session.tabs.map((tab) => ({ ...tab, pinned: tab.pinned === true }));
  return {
    ...session,
    tabs: [...tabs.filter((tab) => tab.pinned), ...tabs.filter((tab) => !tab.pinned)],
    activeId: tabs.some((tab) => tab.id === session.activeId)
      ? session.activeId
      : tabs.find((tab) => tab.pinned)?.id || tabs[0]?.id || '',
  };
}

export function fingerprint(data: DocumentData): string {
  // 编辑方式是视图状态，单纯切换可视化 / JSON 不产生未保存标记。
  return JSON.stringify({ ...data, schemaTab: undefined });
}
export function isDirty(tab: DocumentTab): boolean {
  return tab.saved !== fingerprint(tab.data);
}

// 编码只比较编码输入，自动写入的 HEX 不会把刚生成的结果标为过期。
export function inputSignature(data: DocumentData, direction: 'encode' | 'decode'): string {
  return JSON.stringify([
    data.mode,
    data.draft.version,
    data.draft.entityId,
    data.mode === 'entity'
      ? null
      : data.schemaTab === 'json'
        ? data.schemaText
        : pretty(data.draft.schema),
    direction === 'encode' ? data.draft.values : data.draft.hex,
  ]);
}
export function isStale(data: DocumentData): boolean {
  return !!data.run && data.run.signature !== inputSignature(data, data.run.direction);
}

// 回包按请求发起时的文档标识归属；编辑中的新 HEX 不被较早的编码回包覆盖。
export function completeRun(
  tab: DocumentTab,
  submitted: DocumentData,
  direction: 'encode' | 'decode',
  result: CodecResult,
): DocumentTab {
  return {
    ...tab,
    data: {
      ...tab.data,
      draft:
        direction === 'encode' && tab.data.draft.hex === submitted.draft.hex
          ? { ...tab.data.draft, hex: result.hex }
          : tab.data.draft,
      run: {
        direction,
        signature: inputSignature(submitted, direction),
        executedAt: Date.now(),
        result,
      },
    },
  };
}

// 保存确认只更新基线，保存期间继续编辑的内容仍标记为未保存。
export function acceptSave(
  tab: DocumentTab,
  entry: WorkspaceEntry,
  submitted: DocumentData,
): DocumentTab {
  return {
    ...tab,
    entryId: entry.id,
    parentId: entry.parentId,
    name: entry.name,
    revision: entry.revision,
    saved: fingerprint(submitted),
  };
}

export function parseDocument(value: unknown): DocumentData {
  const data = value as DocumentData;
  if (
    !data ||
    data.formatVersion !== 1 ||
    !Object.hasOwn(MODE_TITLES, data.mode) ||
    !['visual', 'json'].includes(data.schemaTab) ||
    typeof data.schemaText !== 'string' ||
    !data.draft ||
    ['values', 'hex', 'version', 'entityId'].some(
      (key) => typeof data.draft[key as keyof Draft] !== 'string',
    )
  ) {
    throw new Error('无效的 Studio 文档');
  }
  parseSchema(pretty(data.draft.schema));
  if (
    data.run !== null &&
    (!data.run ||
      !['encode', 'decode'].includes(data.run.direction) ||
      typeof data.run.signature !== 'string' ||
      !Number.isFinite(data.run.executedAt) ||
      !data.run.result ||
      typeof data.run.result.success !== 'boolean' ||
      typeof data.run.result.hex !== 'string' ||
      typeof data.run.result.remainingHex !== 'string' ||
      !Number.isFinite(data.run.result.consumedBits) ||
      !Number.isFinite(data.run.result.consumedBytes) ||
      !Array.isArray(data.run.result.trace?.nodes) ||
      !data.run.result.trace.root ||
      !data.run.result.trace.nodeIdsByByteOffset ||
      !Array.isArray(data.run.result.checksums))
  )
    throw new Error('无效的运行快照');
  if (data.run) {
    const trace = data.run.result.trace;
    const queue = [trace.root, ...trace.nodes];
    for (let index = 0; index < queue.length; index++) {
      const node = queue[index];
      if (
        index > 100000 ||
        !node ||
        typeof node.id !== 'string' ||
        typeof node.name !== 'string' ||
        typeof node.status !== 'string' ||
        !Array.isArray(node.children) ||
        !Array.isArray(node.diagnostics) ||
        !node.attributes ||
        typeof node.attributes !== 'object'
      ) {
        throw new Error('无效的字段跟踪快照');
      }
      queue.push(...node.children);
    }
    if (
      Object.values(trace.nodeIdsByByteOffset).some(
        (ids) => !Array.isArray(ids) || ids.some((id) => typeof id !== 'string'),
      ) ||
      data.run.result.checksums.some(
        (item) => !item || typeof item.fieldPath !== 'string' || typeof item.matched !== 'boolean',
      )
    ) {
      throw new Error('无效的运行快照索引或校验和');
    }
  }
  return structuredClone(data);
}

export function migrateLegacy(raw: string | null): Session {
  const tabs: DocumentTab[] = [];
  let mode: Mode = 'dynamic';
  let sidebarCollapsed = false;
  try {
    const old = JSON.parse(raw || '{}');
    if (Object.hasOwn(MODE_TITLES, old.mode)) mode = old.mode;
    sidebarCollapsed = old.sidebarCollapsed === true;
    for (const key of Object.keys(MODE_TITLES) as Mode[]) {
      if (!old.drafts?.[key]) continue;
      try {
        const tab = newTab(key, `${MODE_TITLES[key]} · 恢复草稿`, {
          ...initialDrafts()[key],
          ...old.drafts[key],
        });
        tab.data = parseDocument(tab.data);
        tabs.push(tab);
      } catch {
        /* 单个旧草稿损坏时保留其他可恢复草稿。 */
      }
    }
  } catch {
    /* 首次使用或没有可恢复的旧草稿。 */
  }
  if (!tabs.length) tabs.push(newTab());
  return {
    formatVersion: 1,
    tabs,
    activeId: (tabs.find((tab) => tab.data.mode === mode) || tabs[0]).id,
    sidebarCollapsed,
  };
}

export function descendants(entries: WorkspaceEntry[], id: string): Set<string> {
  const found = new Set([id]);
  let changed = true;
  while (changed) {
    changed = false;
    for (const entry of entries)
      if (entry.parentId && found.has(entry.parentId) && !found.has(entry.id)) {
        found.add(entry.id);
        changed = true;
      }
  }
  return found;
}

export function canMoveEntry(
  entries: WorkspaceEntry[],
  id: string,
  parentId: string | null,
): boolean {
  const entry = entries.find((item) => item.id === id);
  if (!entry || entry.parentId === parentId) return false;
  if (parentId === null) return true;
  return (
    entries.some((item) => item.id === parentId && item.kind === 'folder') &&
    !descendants(entries, id).has(parentId)
  );
}

export function downloadJson(name: string, data: unknown): void {
  const url = URL.createObjectURL(new Blob([pretty(data)], { type: 'application/json' }));
  const link = document.createElement('a');
  link.href = url;
  link.download = name;
  link.click();
  URL.revokeObjectURL(url);
}
