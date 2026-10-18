import { normalizeHex, parseJson, parseSchema, parseValues, pretty } from './model';
import type { CodecResult, Draft, Entity, Mode } from './types';
import type { DocumentData, WorkspaceEntry, WorkspaceIndex } from './workspace';

const API_BASE = (import.meta.env?.VITE_CODEC_API_BASE || '/api/v1').replace(/\/$/, '');

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

async function request<T>(
  path: string,
  body?: unknown,
  method = body === undefined ? 'GET' : 'POST',
): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(20_000),
  });
  const data = parseJson(await response.text()) as unknown as T & {
    success?: boolean;
    error?: string;
  };
  // 422 响应包含已经完成的 Trace，必须保留为执行结果供用户检查。
  if (!response.ok && !(response.status === 422 && typeof data.success === 'boolean')) {
    throw new ApiError(
      response.status,
      typeof data.error === 'string' ? data.error : `请求失败 (${response.status})`,
    );
  }
  return data;
}

export const workspaceApi = {
  index: () => request<WorkspaceIndex>('/workspace'),
  get: (id: string) => request<WorkspaceEntry>(`/workspace/entries/${id}`),
  create: (
    parentId: string | null,
    kind: 'folder' | 'document',
    name: string,
    document: DocumentData | null,
  ) =>
    request<WorkspaceEntry>('/workspace/entries', {
      parentId,
      kind,
      name,
      document,
      expectedRevision: 0,
    }),
  update: (entry: WorkspaceEntry) =>
    request<WorkspaceEntry>(
      `/workspace/entries/${entry.id}`,
      {
        parentId: entry.parentId,
        kind: entry.kind,
        name: entry.name,
        document: entry.document,
        expectedRevision: entry.revision,
      },
      'PUT',
    ),
  remove: (entry: WorkspaceEntry, workspaceRevision: number) =>
    request<WorkspaceIndex>(
      `/workspace/entries/${entry.id}`,
      { expectedRevision: entry.revision, workspaceRevision },
      'DELETE',
    ),
  export: () => request<unknown>('/workspace/export'),
  import: (backup: unknown) => request<WorkspaceIndex>('/workspace/import', backup),
};

export const codecApi = {
  entities: () => request<Entity[]>('/entities'),
  extensions: () => request<string[]>('/extensions'),
  template: (id: string) =>
    request<Record<string, unknown>>(`/entities/${encodeURIComponent(id)}/template`),
  execute: (mode: Mode, direction: 'encode' | 'decode', draft: Draft) => {
    const version = draft.version.trim() ? Number(draft.version) : null;
    if (
      version !== null &&
      (!Number.isInteger(version) || version < -2147483648 || version > 2147483647)
    ) {
      throw new Error('目标协议版本必须是 32 位整数');
    }
    if (mode === 'entity' && !draft.entityId) throw new Error('请先选择实体');
    return request<CodecResult>('/codec', {
      mode: mode === 'entity' ? 'entity' : 'dynamic',
      direction,
      version,
      entityId: mode === 'entity' ? draft.entityId : null,
      schema: mode === 'entity' ? null : parseSchema(pretty(draft.schema)),
      values: direction === 'encode' ? parseValues(draft.values) : null,
      hex: direction === 'decode' ? normalizeHex(draft.hex) : null,
    });
  },
};
