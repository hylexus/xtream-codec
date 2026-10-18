import { migrateLegacy, restoreSession } from './workspace';
import type { Session } from './workspace';
import { randomUuid } from './uuid';

const PAGE_KEY = 'xtream-codec-studio-session-id';
const LATEST_KEY = 'meta:latest-workspace';
let initialization: Promise<Session> | undefined;
let currentKey: string;
let writes: Promise<void> = Promise.resolve();

function open(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open('xtream-codec-studio', 1);
    request.onupgradeneeded = () => request.result.createObjectStore('session');
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
    request.onblocked = () => reject(new Error('草稿数据库被其他窗口阻塞，请关闭旧窗口后重试'));
  });
}

async function transact<T>(key: string, value?: Session): Promise<T> {
  const db = await open();
  try {
    return await new Promise<T>((resolve, reject) => {
      const transaction = db.transaction('session', value ? 'readwrite' : 'readonly');
      const store = transaction.objectStore('session');
      let request: IDBRequest;
      try {
        request = value ? store.put(value, key) : store.get(key);
        if (value) store.put(key, LATEST_KEY);
      } catch (cause) {
        transaction.abort();
        reject(cause);
        return;
      }
      transaction.oncomplete = () => resolve(request.result as T);
      transaction.onerror = () => reject(transaction.error);
      transaction.onabort = () => reject(transaction.error || new Error('草稿事务被取消'));
    });
  } finally {
    db.close();
  }
}

async function initialize(): Promise<Session> {
  const source =
    sessionStorage.getItem(PAGE_KEY) ?? (await transact<string | undefined>(LATEST_KEY));
  if (source !== undefined && (typeof source !== 'string' || !source.startsWith('workspace:'))) {
    throw new Error('无效的页面草稿记录，原始数据已保留');
  }
  const saved = await transact<Session | undefined>(source ?? 'current');
  if (source !== undefined && !saved) throw new Error('本页草稿记录不存在，原始数据已保留');
  const session = saved
    ? restoreSession(saved)
    : migrateLegacy(localStorage.getItem('xtream-codec-workbench-v1'));
  // 浏览器复制标签页会复制 sessionStorage，只把原记录当作来源，不继续共写。
  const key = `workspace:${randomUuid()}`;
  await transact<void>(key, session);
  sessionStorage.setItem(PAGE_KEY, key);
  currentKey = key;
  return session;
}

export const loadSession = (): Promise<Session> => (initialization ??= initialize());

export function saveSession(session: Session): Promise<void> {
  const snapshot = structuredClone(session);
  const write = writes
    .catch(() => {})
    .then(async () => {
      await loadSession();
      await transact<void>(currentKey, snapshot);
    });
  // 单次失败不阻止后续备份，定时与后台备份仍按发起顺序提交。
  writes = write;
  return write;
}
