import assert from 'node:assert/strict';
import { test } from 'node:test';

const target = process.env.STUDIO_TEMP_API_URL;
const base = process.env.STUDIO_UI_URL || 'http://127.0.0.1:5173';

test('临时真实后端：双页面保存、双向覆盖、再次冲突、刷新和删除', { skip: !target }, async () => {
  assert.equal(new URL(target).hostname, '127.0.0.1');
  assert.notEqual(new URL(target).port, '8765');
  const { chromium } = await import(process.env.STUDIO_PLAYWRIGHT_MODULE || 'playwright');
  const browser = await chromium.launch({ channel: 'chrome', headless: true });
  const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
  context.setDefaultTimeout(8000);
  const errors = [];
  const requests = [];
  let advanceBeforePut = false;
  const api = async (path, method = 'GET', body) => {
    const response = await context.request.fetch(`${target}/api/v1${path}`, {
      method,
      data: body,
    });
    assert.ok(response.ok(), `${method} ${path}: ${response.status()} ${await response.text()}`);
    return response.json();
  };
  const put = (entry) =>
    api(`/workspace/entries/${entry.id}`, 'PUT', {
      parentId: entry.parentId,
      kind: entry.kind,
      name: entry.name,
      document: entry.document,
      expectedRevision: entry.revision,
    });
  const host = (page) => page.locator('.document-host:not([hidden])');
  const hex = (page) => host(page).locator('.hex-input-panel .cm-content');
  const dialog = (page) => page.getByRole('dialog', { name: '报文保存冲突' });
  const save = (page) => page.getByRole('button', { name: '保存', exact: true }).click();
  const waitFor = async (check) => {
    const end = Date.now() + 8000;
    while (!(await check())) {
      assert.ok(Date.now() < end, '等待真实后端状态超时');
      await new Promise((resolve) => setTimeout(resolve, 30));
    }
  };
  context.on('page', (page) => {
    page.on('pageerror', (error) => errors.push(error.message));
    page.on('dialog', (event) => event.accept());
  });
  try {
    assert.equal((await api('/workspace')).entries.length, 0, '仅允许使用空的临时后端');
    await context.route('**/api/v1/**', async (route) => {
      const request = route.request();
      const path = new URL(request.url()).pathname;
      if (advanceBeforePut && request.method() === 'PUT') {
        advanceBeforePut = false;
        const entry = await api(path.replace('/api/v1', ''));
        await put({ ...entry, name: '第三方再次保存' });
      }
      const response = await route.fetch({ url: `${target}${path}` });
      requests.push({
        method: request.method(),
        status: response.status(),
        body: request.postData() ? request.postDataJSON() : null,
      });
      await route.fulfill({ response });
    });
    await context.route('**/live-seed', (route) =>
      route.fulfill({
        contentType: 'text/html',
        body: '<!doctype html><title>临时后端验收</title>',
      }),
    );
    const a = await context.newPage();
    await a.goto(`${base}/live-seed`);
    const tab = await a.evaluate(async () => {
      const { newTab } = await import('/src/workspace.ts');
      return newTab('dynamic', '双页面报文');
    });
    const entry = await api('/workspace/entries', 'POST', {
      parentId: null,
      kind: 'document',
      name: tab.name,
      expectedRevision: 0,
      document: tab.data,
    });
    await a.evaluate(
      async ({ tab, entry }) => {
        const { acceptSave } = await import('/src/workspace.ts');
        const saved = acceptSave(tab, entry, tab.data);
        const db = await new Promise((resolve, reject) => {
          const open = indexedDB.open('xtream-codec-studio', 1);
          open.onupgradeneeded = () => open.result.createObjectStore('session');
          open.onsuccess = () => resolve(open.result);
          open.onerror = () => reject(open.error);
        });
        await new Promise((resolve, reject) => {
          const tx = db.transaction('session', 'readwrite');
          tx.objectStore('session').put(
            { formatVersion: 1, tabs: [saved], activeId: saved.id, sidebarCollapsed: false },
            'current',
          );
          tx.oncomplete = resolve;
          tx.onabort = () => reject(tx.error);
        });
        db.close();
      },
      { tab, entry },
    );
    const path = `/workspace/entries/${entry.id}`;
    const backedUp = (page, revision) =>
      page.evaluate(async (revision) => {
        const db = await new Promise((resolve) => {
          const open = indexedDB.open('xtream-codec-studio', 1);
          open.onsuccess = () => resolve(open.result);
        });
        const saved = await new Promise((resolve) => {
          const tx = db.transaction('session');
          const get = tx
            .objectStore('session')
            .get(sessionStorage.getItem('xtream-codec-studio-session-id'));
          tx.oncomplete = () => resolve(get.result);
        });
        db.close();
        return saved.tabs[0].revision === revision;
      }, revision);
    await a.goto(base);
    await host(a).waitFor();
    const b = await context.newPage();
    await b.goto(base);
    await host(b).waitFor();
    await hex(a).fill('aa');
    await hex(b).fill('bb');
    await save(a);
    await waitFor(async () => (await api(path)).revision === 2);
    await save(b);
    await dialog(b).waitFor();
    const writes = requests.filter((r) => r.method === 'PUT').length;
    await dialog(b).getByRole('button', { name: '使用服务器版本', exact: true }).click();
    await dialog(b).waitFor({ state: 'hidden' });
    await waitFor(async () => (await hex(b).innerText()) === 'aa');
    assert.equal(requests.filter((r) => r.method === 'PUT').length, writes);
    await hex(a).fill('cc');
    await hex(b).fill('dd');
    await save(a);
    await waitFor(async () => (await api(path)).revision === 3);
    await save(b);
    await dialog(b).waitFor();
    advanceBeforePut = true;
    await dialog(b).getByRole('button', { name: '用本地版本覆盖服务器', exact: true }).click();
    await dialog(b)
      .getByText('服务器再次发生变化，已加载最新版本，请重新选择', { exact: true })
      .waitFor();
    assert.equal((await api(path)).revision, 4);
    assert.equal((await api(path)).document.draft.hex, 'cc');
    await dialog(b).getByRole('button', { name: '用本地版本覆盖服务器', exact: true }).click();
    await dialog(b).waitFor({ state: 'hidden' });
    assert.equal((await api(path)).document.draft.hex, 'dd');
    assert.equal((await api(path)).revision, 5);
    await waitFor(() => backedUp(a, 3));
    await waitFor(() => backedUp(b, 5));
    await a.reload();
    await b.reload();
    await host(a).waitFor();
    await host(b).waitFor();
    assert.equal(await hex(a).innerText(), 'cc');
    assert.equal(await hex(b).innerText(), 'dd');
    await hex(a).fill('ee');
    await save(a);
    await dialog(a).waitFor();
    const latest = await api(path);
    await api(path, 'DELETE', {
      expectedRevision: latest.revision,
      workspaceRevision: (await api('/workspace')).revision,
    });
    await dialog(a).getByRole('button', { name: '用本地版本覆盖服务器', exact: true }).click();
    await dialog(a).getByText('文档或目录不存在', { exact: true }).waitFor();
    await dialog(a).getByRole('button', { name: '取消', exact: true }).click();
    await dialog(a).waitFor({ state: 'hidden' });
    assert.equal(await hex(a).innerText(), 'ee');
    assert.equal((await api('/workspace')).entries.length, 0);
    assert.equal(
      requests.some((r) => r.method === 'POST'),
      false,
    );
    assert.deepEqual(
      requests.filter((r) => r.method === 'PUT').map((r) => r.status),
      [200, 409, 200, 409, 409, 200, 409, 404],
    );
    assert.deepEqual(errors, []);
  } finally {
    await context.close();
    await browser.close();
  }
});
