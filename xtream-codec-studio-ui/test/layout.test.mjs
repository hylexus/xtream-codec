import assert from 'node:assert/strict';
import test from 'node:test';
import { DEFAULT_LAYOUT, restoreLayout } from '../src/layout.tsx';

test('没有布局偏好或偏好格式损坏时保留可用默认值', () => {
  for (const value of [
    null,
    undefined,
    'broken',
    [],
    { editor: '60', input: NaN, run: Infinity },
  ]) {
    assert.deepEqual(restoreLayout(value), DEFAULT_LAYOUT);
  }
  assert.deepEqual(restoreLayout({ editor: 63, unknown: true }), { ...DEFAULT_LAYOUT, editor: 63 });
});

test('恢复旧窗口的极端尺寸时限制边界，不改变存储输入', () => {
  const saved = { sidebar: 900, editor: -10, input: 100, run: -200, trace: 51 };
  assert.deepEqual(restoreLayout(saved), {
    sidebar: 420,
    editor: 20,
    input: 85,
    run: 180,
    trace: 51,
  });
  assert.equal(saved.sidebar, 900);
  assert.equal(DEFAULT_LAYOUT.sidebar, 242);
});
