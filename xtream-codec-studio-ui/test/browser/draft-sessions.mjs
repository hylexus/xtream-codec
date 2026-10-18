import assert from 'node:assert/strict';
import { after, before, test } from 'node:test';

const { chromium } = await import(process.env.STUDIO_PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.STUDIO_UI_URL || 'http://127.0.0.1:5173';
let browser;

before(async () => {
  browser = await chromium.launch({ channel: 'chrome', headless: true });
});
after(async () => browser?.close());

const pageKey = (page) =>
  page.evaluate(() => sessionStorage.getItem('xtream-codec-studio-session-id'));

async function records(page, entries) {
  return page.evaluate(async (values) => {
    const db = await new Promise((resolve, reject) => {
      const request = indexedDB.open('xtream-codec-studio', 1);
      request.onupgradeneeded = () => request.result.createObjectStore('session');
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
    try {
      return await new Promise((resolve, reject) => {
        const transaction = db.transaction('session', values ? 'readwrite' : 'readonly');
        const store = transaction.objectStore('session');
        for (const [key, value] of values || []) store.put(value, key);
        const keys = store.getAllKeys();
        const rows = store.getAll();
        transaction.oncomplete = () =>
          resolve(Object.fromEntries(keys.result.map((key, index) => [key, rows.result[index]])));
        transaction.onabort = () => reject(transaction.error);
      });
    } finally {
      db.close();
    }
  }, entries);
}

async function fixture() {
  const context = await browser.newContext();
  await context.route('**/draft-store-fixture', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<!doctype html><title>草稿测试</title>' }),
  );
  return context;
}

async function pageIn(context) {
  const page = await context.newPage();
  await page.goto(`${base}/draft-store-fixture`);
  return page;
}

const load = (page) =>
  page.evaluate(async () => (await import('/src/draft-store.ts')).loadSession());
const save = (page, session) =>
  page.evaluate(async (value) => (await import('/src/draft-store.ts')).saveSession(value), session);

async function draft(page, marker) {
  return page.evaluate(async (name) => {
    const { newTab, fingerprint } = await import('/src/workspace.ts');
    const first = newTab('dynamic', name);
    first.pinned = true;
    first.entryId = 'same-server-document';
    first.revision = 1;
    first.saved = fingerprint(first.data);
    first.data.schemaText = '{"fields":';
    first.data.schemaTab = 'json';
    first.data.draft.values = `{"${name}":`;
    first.data.draft.hex = `aB \t 01\n${name}`;
    first.data.run = {
      direction: 'decode',
      signature: name,
      executedAt: 1,
      result: {
        success: true,
        hex: '01',
        values: { marker: name },
        remainingHex: '',
        consumedBits: 8,
        consumedBytes: 1,
        checksums: [],
        trace: {
          root: {
            id: 'root',
            name: 'root',
            status: 'SUCCESS',
            children: [],
            diagnostics: [],
            attributes: {},
          },
          nodes: [],
          nodeIdsByByteOffset: {},
        },
      },
    };
    const second = newTab('field', `${name}-second`);
    return { formatVersion: 1, tabs: [first, second], activeId: second.id, sidebarCollapsed: true };
  }, marker);
}

test('同一浏览器的两页交替备份和刷新，各自恢复完整工作区', async () => {
  const context = await fixture();
  try {
    const a = await pageIn(context);
    const b = await pageIn(context);
    await load(a);
    await load(b);
    const ownA = await draft(a, 'A');
    const ownB = await draft(b, 'B');
    await save(a, ownA);
    await save(b, ownB);
    await a.reload();
    assert.deepEqual(await load(a), ownA, 'B 的后台备份不得覆盖 A 的恢复内容');
    await b.reload();
    assert.deepEqual(await load(b), ownB);
  } finally {
    await context.close();
  }
});

test('初始化幂等，浏览器复制继承来源后独立保存，并发新页面互不共写', async () => {
  const context = await fixture();
  try {
    const a = await pageIn(context);
    assert.equal(
      await a.evaluate(async () => {
        const store = await import('/src/draft-store.ts');
        return store.loadSession() === store.loadSession();
      }),
      true,
    );
    await load(a);
    const ownA = await draft(a, 'source');
    await save(a, ownA);
    const originalKey = await pageKey(a);
    const opened = context.waitForEvent('page');
    await a.evaluate((url) => window.open(url), `${base}/draft-store-fixture`);
    const copy = await opened;
    await copy.waitForLoadState();
    assert.equal(await pageKey(copy), originalKey);
    assert.deepEqual(await load(copy), ownA);
    assert.notEqual(await pageKey(copy), originalKey);
    const ownCopy = await draft(copy, 'copy');
    await save(copy, ownCopy);
    const fresh = await Promise.all([pageIn(context), pageIn(context), pageIn(context)]);
    const restored = await Promise.all(fresh.map(load));
    restored.forEach((session) => assert.deepEqual(session, ownCopy));
    const keys = await Promise.all([a, copy, ...fresh].map(pageKey));
    assert.equal(new Set(keys).size, keys.length);
    await a.reload();
    assert.deepEqual(await load(a), ownA);
    await copy.reload();
    assert.deepEqual(await load(copy), ownCopy);
  } finally {
    await context.close();
  }
});

test('旧共享草稿迁移保留来源，后续页面复制新备份而不再写旧键', async () => {
  const context = await fixture();
  try {
    const a = await pageIn(context);
    const legacy = await draft(a, 'legacy');
    await records(a, [['current', legacy]]);
    assert.deepEqual(await load(a), legacy);
    const changed = await draft(a, 'new');
    await save(a, changed);
    const b = await pageIn(context);
    assert.deepEqual(await load(b), changed);
    assert.deepEqual((await records(a)).current, legacy);
    assert.notEqual(await pageKey(a), await pageKey(b));
  } finally {
    await context.close();
  }
});

test('旧模式草稿兼容迁移且保留 localStorage 备份', async () => {
  const context = await fixture();
  try {
    const page = await pageIn(context);
    const raw = await page.evaluate(async () => {
      const { initialDrafts } = await import('/src/presets.ts');
      const raw = JSON.stringify({ mode: 'field', drafts: initialDrafts() });
      localStorage.setItem('xtream-codec-workbench-v1', raw);
      return raw;
    });
    const restored = await load(page);
    assert.equal(restored.tabs.length, 3);
    assert.equal(restored.tabs.find((tab) => tab.id === restored.activeId).data.mode, 'field');
    assert.equal(await page.evaluate(() => localStorage.getItem('xtream-codec-workbench-v1')), raw);
  } finally {
    await context.close();
  }
});

test('备份按发起顺序提交，事务失败不部分写入且后续仍可备份', async () => {
  const context = await fixture();
  try {
    const page = await pageIn(context);
    await load(page);
    const first = await draft(page, 'first');
    const last = await draft(page, 'last');
    await page.evaluate(
      async ({ first, last }) => {
        const { saveSession } = await import('/src/draft-store.ts');
        await Promise.all([saveSession(first), saveSession(last)]);
      },
      { first, last },
    );
    const key = await pageKey(page);
    assert.deepEqual((await records(page))[key], last);
    await page.evaluate(() => {
      const original = IDBObjectStore.prototype.put;
      IDBObjectStore.prototype.put = function (value, key) {
        if (key === 'meta:latest-workspace') {
          IDBObjectStore.prototype.put = original;
          this.transaction.abort();
        }
        return original.call(this, value, key);
      };
    });
    await assert.rejects(save(page, first), /InvalidStateError|草稿事务被取消/);
    assert.deepEqual((await records(page))[key], last);
    await save(page, first);
    assert.deepEqual((await records(page))[key], first);
  } finally {
    await context.close();
  }
});

test('迁移写入配额失败回滚新记录，旧草稿与页面指针保持原样', async () => {
  const context = await fixture();
  try {
    const page = await pageIn(context);
    const legacy = await draft(page, 'legacy');
    await records(page, [['current', legacy]]);
    await page.evaluate(() => {
      const original = IDBObjectStore.prototype.put;
      IDBObjectStore.prototype.put = function (value, key) {
        if (key === 'meta:latest-workspace')
          throw new DOMException('草稿容量不足', 'QuotaExceededError');
        return original.call(this, value, key);
      };
    });
    await assert.rejects(load(page), /草稿容量不足/);
    assert.equal(await pageKey(page), null);
    assert.deepEqual(await records(page), { current: legacy });
    await page.reload();
    assert.deepEqual(await load(page), legacy);
  } finally {
    await context.close();
  }
});

test('本页恢复记录缺失或损坏时不静默加载另一页草稿', async () => {
  const context = await fixture();
  try {
    const a = await pageIn(context);
    await load(a);
    const valid = await draft(a, 'valid');
    await save(a, valid);
    for (const [key, value] of [
      ['workspace:missing', undefined],
      ['workspace:broken', { formatVersion: 99 }],
    ]) {
      const page = await pageIn(context);
      if (value) await records(page, [[key, value]]);
      await page.evaluate(
        (key) => sessionStorage.setItem('xtream-codec-studio-session-id', key),
        key,
      );
      await assert.rejects(load(page), /草稿记录不存在|不支持的本地草稿版本/);
      assert.equal(await pageKey(page), key);
      assert.deepEqual((await records(a))[await pageKey(a)], valid);
    }
  } finally {
    await context.close();
  }
});

test('sessionStorage 或 IndexedDB 不可用时不写共享回退', async () => {
  const context = await fixture();
  try {
    for (const unavailable of ['sessionStorage', 'indexedDB']) {
      const page = await pageIn(context);
      await page.evaluate((name) => {
        Object.defineProperty(window, name, {
          get() {
            throw new DOMException('浏览器禁用存储', 'SecurityError');
          },
        });
      }, unavailable);
      await assert.rejects(load(page), /浏览器禁用存储/);
    }
    const check = await pageIn(context);
    assert.deepEqual(await records(check), {});
  } finally {
    await context.close();
  }
});

test('缺少 randomUUID 时页面备份仍使用独立 UUID v4 记录', async () => {
  const context = await fixture();
  try {
    const page = await pageIn(context);
    await page.evaluate(() => Object.defineProperty(crypto, 'randomUUID', { value: undefined }));
    await load(page);
    assert.match(
      await pageKey(page),
      /^workspace:[\da-f]{8}-[\da-f]{4}-4[\da-f]{3}-[89ab][\da-f]{3}-[\da-f]{12}$/,
    );
  } finally {
    await context.close();
  }
});

test('页面存储不可用时提示失败，仍能在内存中新建和编辑报文', async () => {
  const context = await fixture();
  try {
    await context.route('**/api/v1/**', (route) =>
      route.fulfill({
        json: route.request().url().endsWith('/workspace') ? { revision: 0, entries: [] } : [],
      }),
    );
    const page = await context.newPage();
    await page.addInitScript(() => {
      Object.defineProperty(window, 'sessionStorage', {
        get() {
          throw new DOMException('浏览器禁用存储', 'SecurityError');
        },
      });
    });
    await page.goto(base);
    await page.getByText('本地草稿不可用', { exact: true }).waitFor();
    await page.getByRole('button', { name: '新建自由组合', exact: true }).first().click();
    const editor = page.locator('.document-host:not([hidden])');
    await editor.waitFor();
    const hex = editor.locator('.hex-input-panel .cm-content');
    await hex.fill('aB 01');
    assert.equal(await hex.innerText(), 'aB 01');
    assert.deepEqual(await records(page), {});
  } finally {
    await context.close();
  }
});
