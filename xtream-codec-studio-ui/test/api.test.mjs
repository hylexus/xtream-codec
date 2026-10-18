import { test } from 'node:test';
import assert from 'node:assert/strict';
import { ApiError, codecApi, workspaceApi } from '../src/api.ts';
import { initialDrafts } from '../src/presets.ts';

test('工作区失败保留 HTTP 状态和服务器文案，版本更新携带预期版本', async (t) => {
  let status = 409;
  const requests = [];
  t.mock.method(globalThis, 'fetch', async (path, options) => {
    requests.push({ path, options });
    return new Response(JSON.stringify({ error: `错误 ${status}` }), { status });
  });
  for (status of [400, 404, 409, 500]) {
    await assert.rejects(
      workspaceApi.update({ id: 'entry', revision: 2, document: null }),
      (error) => {
        assert.ok(error instanceof ApiError);
        assert.equal(error.status, status);
        assert.equal(error.message, `错误 ${status}`);
        return true;
      },
    );
  }
  assert.equal(requests[0].path, '/api/v1/workspace/entries/entry');
  assert.equal(requests[0].options.method, 'PUT');
  assert.equal(JSON.parse(requests[0].options.body).expectedRevision, 2);
});

test('编解码的 422 结果保留 Trace，网络失败不伪装成保存冲突', async (t) => {
  const result = { success: false, hex: '', trace: { nodes: [] } };
  t.mock.method(
    globalThis,
    'fetch',
    async () => new Response(JSON.stringify(result), { status: 422 }),
  );
  assert.deepEqual(await codecApi.execute('dynamic', 'encode', initialDrafts().dynamic), result);
  globalThis.fetch = async () => {
    throw new TypeError('网络不可用');
  };
  await assert.rejects(workspaceApi.get('entry'), (error) => {
    assert.equal(error instanceof ApiError, false);
    assert.match(error.message, /网络不可用/);
    return true;
  });
});
