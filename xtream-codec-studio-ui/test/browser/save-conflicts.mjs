import assert from 'node:assert/strict';
import { after, before, test } from 'node:test';

const { chromium } = await import(process.env.STUDIO_PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.STUDIO_UI_URL || 'http://127.0.0.1:5173';
let browser;
before(async () => {
  browser = await chromium.launch({ channel: 'chrome', headless: true });
});
after(async () => browser?.close());

async function until(check) {
  const deadline = Date.now() + 8000;
  while (!(await check())) {
    assert.ok(Date.now() < deadline, '等待页面状态超时');
    await new Promise((resolve) => setTimeout(resolve, 30));
  }
}

async function snapshot(page) {
  return page.evaluate(async () => {
    const key = sessionStorage.getItem('xtream-codec-studio-session-id');
    const db = await new Promise((resolve) => {
      const request = indexedDB.open('xtream-codec-studio', 1);
      request.onsuccess = () => resolve(request.result);
    });
    try {
      return await new Promise((resolve) => {
        const tx = db.transaction('session');
        const request = tx.objectStore('session').get(key);
        tx.oncomplete = () => resolve(request.result);
      });
    } finally {
      db.close();
    }
  });
}

const host = (page) => page.locator('.document-host:not([hidden])');
const hex = (page) => host(page).locator('.hex-input-panel .cm-content');
const dialog = (page) => page.getByRole('dialog', { name: '报文保存冲突' });
const clickSave = (page) => page.getByRole('button', { name: '保存', exact: true }).click();
const localTab = async (page) => (await snapshot(page)).tabs[0];
const puts = (state) => state.requests.filter((request) => request.method === 'PUT');

async function fixture({ others = 0 } = {}) {
  const context = await browser.newContext({ viewport: { width: 1440, height: 1000 } });
  const errors = [];
  context.on('page', (page) => {
    page.on('pageerror', (error) => errors.push(error.message));
    page.on('dialog', (event) => event.accept());
  });
  const state = {
    entry: null,
    requests: [],
    failGet: 0,
    failPut: 0,
    beforePut: null,
    codecGate: null,
  };
  await context.route('**/api/v1/**', async (route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    const method = request.method();
    const body = request.postData() ? request.postDataJSON() : null;
    state.requests.push({ method, path, body });
    const error = (status) => route.fulfill({ status, json: { error: `测试错误 ${status}` } });
    if (path.endsWith('/codec')) {
      if (state.codecGate) await state.codecGate;
      const root = {
        id: 'root',
        name: 'root',
        path: '$',
        kind: 'ROOT',
        status: 'SUCCESS',
        byteStart: 0,
        byteEnd: 1,
        bitStart: 0,
        bitEnd: 8,
        children: [],
        diagnostics: [],
        attributes: {},
      };
      return route.fulfill({
        json: {
          success: true,
          hex: 'ff',
          values: { value: 255 },
          remainingHex: '',
          consumedBits: 8,
          consumedBytes: 1,
          checksums: [],
          trace: { root, nodes: [], nodeIdsByByteOffset: {}, diagnostics: [] },
        },
      });
    }
    if (path.endsWith('/workspace')) {
      const entries = state.entry ? [{ ...state.entry, document: null }] : [];
      if (state.entry?.parentId)
        entries.unshift({
          id: state.entry.parentId,
          parentId: null,
          name: '新目录',
          kind: 'folder',
          revision: 1,
          document: null,
        });
      return route.fulfill({ json: { revision: state.entry?.revision || 0, entries } });
    }
    if (!path.includes('/entries/')) return route.fulfill({ json: [] });
    if (method === 'GET' && state.failGet) return error(state.failGet);
    if (method === 'PUT') {
      if (state.beforePut) await state.beforePut();
      if (state.failPut) return error(state.failPut);
    }
    if (!state.entry) return error(404);
    if (method === 'PUT') {
      if (body.expectedRevision !== state.entry.revision) return error(409);
      state.entry = {
        ...state.entry,
        ...body,
        revision: state.entry.revision + 1,
        updatedAt: Date.now(),
      };
    }
    return route.fulfill({ json: state.entry });
  });
  await context.route('**/conflict-seed', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<!doctype html><title>冲突测试</title>' }),
  );
  const page = await context.newPage();
  await page.goto(`${base}/conflict-seed`);
  const seed = await page.evaluate(async (others) => {
    const { newTab, fingerprint } = await import('/src/workspace.ts');
    const tab = newTab('dynamic', '原报文');
    tab.entryId = '019aebba-a36e-481c-a9fa-a35b12252f08';
    tab.revision = 1;
    tab.data.draft.schema = { schemaVersion: 1, fields: [{ id: 'value', type: 'u8' }] };
    tab.data.schemaText = JSON.stringify(tab.data.draft.schema);
    tab.data.draft.values = '{"value":1}';
    tab.data.draft.hex = '01';
    tab.saved = fingerprint(tab.data);
    const tabs = [tab, ...Array.from({ length: others }, (_, i) => newTab('dynamic', `其他${i}`))];
    const session = { formatVersion: 1, tabs, activeId: tab.id, sidebarCollapsed: false };
    const db = await new Promise((resolve) => {
      const request = indexedDB.open('xtream-codec-studio', 1);
      request.onupgradeneeded = () => request.result.createObjectStore('session');
      request.onsuccess = () => resolve(request.result);
    });
    await new Promise((resolve) => {
      const tx = db.transaction('session', 'readwrite');
      tx.objectStore('session').put(session, 'current');
      tx.oncomplete = resolve;
    });
    db.close();
    return tab;
  }, others);
  state.entry = {
    id: seed.entryId,
    parentId: null,
    kind: 'document',
    name: seed.name,
    revision: 1,
    updatedAt: Date.now(),
    document: structuredClone(seed.data),
  };
  await page.goto(base);
  await host(page).waitFor();
  return {
    context,
    page,
    state,
    seed,
    errors,
    async close() {
      await context.close();
      assert.deepEqual(errors, []);
    },
  };
}

test('两个页面修改同一报文：取消保留本地，采用服务器完整替换且不写服务器', async () => {
  const f = await fixture();
  try {
    const { page: a, context, state, seed } = f;
    const b = await context.newPage();
    await b.goto(base);
    await host(b).waitFor();
    await hex(a).fill('aa');
    await hex(b).fill('bb');
    await clickSave(a);
    await until(() => state.entry.revision === 2);
    await clickSave(b);
    await dialog(b).waitFor();
    assert.match(await dialog(b).innerText(), /服务器版本 v2/);
    assert.equal(
      await dialog(b)
        .getByRole('button', { name: '取消', exact: true })
        .evaluate((button) => button === document.activeElement),
      true,
    );
    const attempts = puts(state).length;
    await b.keyboard.press('ControlOrMeta+s');
    assert.equal(puts(state).length, attempts);
    await b.setViewportSize({ width: 390, height: 844 });
    for (const name of ['取消', '使用服务器版本', '用本地版本覆盖服务器']) {
      const bounds = await dialog(b).getByRole('button', { name, exact: true }).boundingBox();
      assert.ok(bounds && bounds.x >= 0 && bounds.x + bounds.width <= 390);
    }
    await b.keyboard.press('Escape');
    await dialog(b).waitFor({ state: 'hidden' });
    await b.setViewportSize({ width: 1440, height: 1000 });
    await until(async () => (await localTab(b)).data.draft.hex === 'bb');
    assert.equal((await localTab(b)).revision, 1);
    await clickSave(b);
    await dialog(b).waitFor();
    const count = puts(state).length;
    await dialog(b).getByRole('button', { name: '使用服务器版本', exact: true }).click();
    await dialog(b).waitFor({ state: 'hidden' });
    await until(async () => (await localTab(b)).revision === 2);
    const adopted = await localTab(b);
    assert.equal(adopted.id, seed.id);
    assert.deepEqual(adopted.data, state.entry.document);
    assert.equal(puts(state).length, count);
    assert.equal(await b.locator('.document-tabs .dirty-dot').count(), 0);
    assert.equal(await hex(a).innerText(), 'aa');
    await a.reload();
    assert.equal(await hex(a).innerText(), 'aa');
    await b.reload();
    assert.equal(await hex(b).innerText(), 'aa');
  } finally {
    await f.close();
  }
});

test('本地覆盖保留服务器元数据，再次变化时重新确认且不会自动重试', async () => {
  const f = await fixture();
  try {
    const { page, state } = f;
    await hex(page).fill('bb');
    state.entry = { ...state.entry, revision: 2, name: '服务器新名', parentId: 'folder' };
    await page.keyboard.press('ControlOrMeta+s');
    await dialog(page).waitFor();
    state.entry = { ...state.entry, revision: 3 };
    await dialog(page).getByRole('button', { name: '用本地版本覆盖服务器', exact: true }).click();
    await dialog(page)
      .getByText('服务器再次发生变化，已加载最新版本，请重新选择', { exact: true })
      .waitFor();
    assert.match(await dialog(page).innerText(), /服务器版本 v3/);
    assert.equal(puts(state).length, 2);
    assert.equal((await localTab(page)).revision, 1);
    await dialog(page).getByRole('button', { name: '用本地版本覆盖服务器', exact: true }).click();
    await dialog(page).waitFor({ state: 'hidden' });
    await until(async () => (await localTab(page)).revision === 4);
    assert.equal(state.entry.document.draft.hex, 'bb');
    assert.equal(state.entry.name, '服务器新名');
    assert.equal(state.entry.parentId, 'folder');
    assert.equal(puts(state).length, 3);
    assert.deepEqual(
      puts(state).map((request) => request.body.expectedRevision),
      [1, 2, 3],
    );
    assert.equal(await page.locator('.document-tabs .dirty-dot').count(), 0);
  } finally {
    await f.close();
  }
});

test('普通保存失败、读取失败和报文删除都保留本地与基线', async () => {
  const f = await fixture();
  try {
    const { page, state, seed } = f;
    await hex(page).fill('bb');
    for (const status of [400, 500]) {
      state.failPut = status;
      await clickSave(page);
      await page
        .locator('.workspace-error')
        .getByText(`测试错误 ${status}`, { exact: true })
        .waitFor();
      assert.equal(await dialog(page).count(), 0);
    }
    state.failPut = 0;
    state.entry.revision = 2;
    state.failGet = 404;
    await clickSave(page);
    await page.locator('.workspace-error').getByText('测试错误 404', { exact: true }).waitFor();
    assert.equal(await dialog(page).count(), 0);
    state.failGet = 0;
    await clickSave(page);
    await dialog(page).waitFor();
    state.failGet = 500;
    await dialog(page).getByRole('button', { name: '使用服务器版本', exact: true }).click();
    await dialog(page).getByText('测试错误 500', { exact: true }).waitFor();
    state.failGet = 0;
    state.entry = null;
    await dialog(page).getByRole('button', { name: '用本地版本覆盖服务器', exact: true }).click();
    await dialog(page).getByText('测试错误 404', { exact: true }).waitFor();
    await dialog(page).getByRole('button', { name: '取消', exact: true }).click();
    await dialog(page).waitFor({ state: 'hidden' });
    await until(async () => (await localTab(page)).data.draft.hex === 'bb');
    assert.equal((await localTab(page)).saved, seed.saved);
    assert.equal((await localTab(page)).revision, 1);
    assert.equal(state.entry, null);
    assert.equal(
      state.requests.some((request) => request.method === 'POST'),
      false,
    );
  } finally {
    await f.close();
  }
});

test('采用服务器版本后，原本地运行的迟到响应不改写 HEX 或快照', async () => {
  const f = await fixture();
  let release;
  try {
    const { page, state } = f;
    state.codecGate = new Promise((resolve) => {
      release = resolve;
    });
    await host(page).getByRole('button', { name: '编码为 HEX', exact: true }).click();
    await until(() => state.requests.some((request) => request.path.endsWith('/codec')));
    state.entry = { ...state.entry, revision: 2, document: structuredClone(state.entry.document) };
    state.entry.document.draft.hex = '22';
    await clickSave(page);
    await dialog(page).waitFor();
    await dialog(page).getByRole('button', { name: '使用服务器版本', exact: true }).click();
    await dialog(page).waitFor({ state: 'hidden' });
    release();
    await until(
      async () =>
        !(await host(page).getByRole('button', { name: '编码为 HEX', exact: true }).isDisabled()),
    );
    await until(async () => (await localTab(page)).revision === 2);
    assert.deepEqual((await localTab(page)).data, state.entry.document);
    assert.equal(await hex(page).innerText(), '22');
    assert.equal(await page.locator('.document-tabs .dirty-dot').count(), 0);
  } finally {
    release?.();
    await f.close();
  }
});

test('覆盖期间产生新运行结果时，仅已提交快照被标为保存', async () => {
  const f = await fixture();
  let releaseCodec;
  let releasePut;
  try {
    const { page, state } = f;
    await hex(page).fill('bb');
    state.codecGate = new Promise((resolve) => {
      releaseCodec = resolve;
    });
    await host(page).getByRole('button', { name: '编码为 HEX', exact: true }).click();
    await until(() => state.requests.some((request) => request.path.endsWith('/codec')));
    state.entry.revision = 2;
    await clickSave(page);
    await dialog(page).waitFor();
    const gate = new Promise((resolve) => {
      releasePut = resolve;
    });
    state.beforePut = () => gate;
    await dialog(page).getByRole('button', { name: '用本地版本覆盖服务器', exact: true }).click();
    await until(() => puts(state).length === 2);
    assert.equal(
      await dialog(page).getByRole('button', { name: '取消', exact: true }).isDisabled(),
      true,
    );
    releaseCodec();
    await until(async () => (await localTab(page)).data.run !== null);
    releasePut();
    await dialog(page).waitFor({ state: 'hidden' });
    await until(async () => (await localTab(page)).revision === 3);
    assert.equal(state.entry.document.draft.hex, 'bb');
    assert.equal(state.entry.document.run, null);
    assert.equal((await localTab(page)).data.draft.hex, 'ff');
    assert.equal((await localTab(page)).data.run.result.hex, 'ff');
    assert.equal(await page.locator('.document-tabs .dirty-dot').count(), 1);
  } finally {
    releaseCodec?.();
    releasePut?.();
    await f.close();
  }
});

test('批量保存关闭串行显示冲突，取消停止队列，成功后才继续下一报文', async () => {
  const f = await fixture({ others: 2 });
  try {
    const { page, state } = f;
    await hex(page).fill('bb');
    state.entry.revision = 2;
    const start = async () => {
      await page.locator('.document-tabs [role="tab"]').first().click({ button: 'right' });
      await page.getByRole('menuitem', { name: '关闭未固定标签', exact: true }).click();
      const confirmation = page.getByRole('dialog', { name: '保存后关闭？', exact: true });
      await confirmation.getByRole('button', { name: '保存并关闭', exact: true }).click();
      await dialog(page).waitFor();
      assert.equal(await page.locator('[role="dialog"]:visible').count(), 1);
    };
    await start();
    state.failGet = 500;
    await dialog(page).getByRole('button', { name: '使用服务器版本', exact: true }).click();
    await dialog(page).getByText('测试错误 500', { exact: true }).waitFor();
    await dialog(page).getByRole('button', { name: '取消', exact: true }).click();
    await until(async () => (await page.locator('[role="dialog"]:visible').count()) === 0);
    assert.equal(await page.locator('.document-tabs [role="tab"]').count(), 3);
    state.failGet = 0;
    await start();
    await dialog(page).getByRole('button', { name: '使用服务器版本', exact: true }).click();
    const next = page.getByRole('dialog', { name: '保存后关闭？', exact: true });
    await next.getByText(/“其他0”有未保存/).waitFor();
    assert.equal(await page.locator('.document-tabs [role="tab"]').count(), 2);
    await next.getByRole('button', { name: '取消', exact: true }).click();
    await until(async () => (await page.locator('[role="dialog"]:visible').count()) === 0);
    assert.equal(await page.locator('.document-tabs [role="tab"]').count(), 2);
  } finally {
    await f.close();
  }
});

test('不同报文同时返回冲突时逐个处理，不丢失原保存流程', async () => {
  const f = await fixture({ others: 1 });
  let release;
  try {
    const { page, context, state } = f;
    const session = await snapshot(page);
    session.tabs[1].entryId = 'second-entry';
    session.tabs[1].revision = 1;
    await page.evaluate(async (session) => {
      const { saveSession } = await import('/src/draft-store.ts');
      await saveSession(session);
    }, session);
    let second = { ...state.entry, id: 'second-entry', name: '第二报文', revision: 2 };
    await context.route('**/api/v1/workspace/entries/second-entry', async (route) => {
      const request = route.request();
      if (request.method() === 'PUT') {
        if (request.postDataJSON().expectedRevision !== second.revision)
          return route.fulfill({ status: 409, json: { error: '第二报文版本过期' } });
        second = { ...second, revision: second.revision + 1 };
      }
      await route.fulfill({ json: second });
    });
    await page.reload();
    await host(page).waitFor();
    state.entry.revision = 2;
    const gate = new Promise((resolve) => {
      release = resolve;
    });
    state.beforePut = () => gate;
    await clickSave(page);
    await until(() => puts(state).length === 1);
    await page.locator('.document-tabs [role="tab"]').nth(1).click();
    await clickSave(page);
    await dialog(page)
      .getByText(/“第二报文”已被修改/)
      .waitFor();
    release();
    await dialog(page).getByRole('button', { name: '取消', exact: true }).click();
    await dialog(page)
      .getByText(/“原报文”已被修改/)
      .waitFor();
    await dialog(page).getByRole('button', { name: '使用服务器版本', exact: true }).click();
    await dialog(page).waitFor({ state: 'hidden' });
    await until(async () => (await snapshot(page)).tabs[0].revision === 2);
    assert.equal((await snapshot(page)).tabs[1].revision, 1);
  } finally {
    release?.();
    await f.close();
  }
});
