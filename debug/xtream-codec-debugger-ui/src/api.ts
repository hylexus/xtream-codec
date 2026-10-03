import { normalizeHex, parseJson, parseSchema, parseValues, pretty } from './model';
import type { CodecResult, Draft, Entity, Mode } from './types';

const API_BASE = (import.meta.env.VITE_CODEC_API_BASE || '/api/v1').replace(/\/$/, '');

async function request<T>(path: string, body?: unknown): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    method: body === undefined ? 'GET' : 'POST',
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
    throw new Error(typeof data.error === 'string' ? data.error : `请求失败 (${response.status})`);
  }
  return data;
}

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
