import { migrateLegacy, restoreSession } from './workspace';
import type { Session } from './workspace';

function open(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open('xtream-codec-studio', 1);
    request.onupgradeneeded = () => request.result.createObjectStore('session');
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
    request.onblocked = () => reject(new Error('草稿数据库被其他窗口阻塞，请关闭旧窗口后重试'));
  });
}

async function transact<T>(write: boolean, value?: Session): Promise<T> {
  const db = await open();
  try {
    return await new Promise<T>((resolve, reject) => {
      const transaction = db.transaction('session', write ? 'readwrite' : 'readonly');
      const store = transaction.objectStore('session');
      const request = write ? store.put(value, 'current') : store.get('current');
      transaction.oncomplete = () => resolve(request.result as T);
      transaction.onerror = () => reject(transaction.error);
      transaction.onabort = () => reject(transaction.error || new Error('草稿事务被取消'));
    });
  } finally {
    db.close();
  }
}

export const saveSession = (session: Session) => transact<void>(true, session);
export async function loadSession(): Promise<Session> {
  const session = await transact<Session | undefined>(false);
  if (!session) {
    const migrated = migrateLegacy(localStorage.getItem('xtream-codec-workbench-v1'));
    await saveSession(migrated);
    // 迁移完成后保留旧键作为备份，不再依赖它继续写入草稿。
    return migrated;
  }
  return restoreSession(session);
}
