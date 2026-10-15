import { test } from 'node:test';
import assert from 'node:assert/strict';
import { randomUuid } from '../src/uuid.ts';

test('原生 randomUUID 可用时优先调用并保持返回值', (t) => {
  const expected = '01234567-89ab-4cde-8f01-23456789abcd';
  const native = t.mock.method(crypto, 'randomUUID', function () {
    assert.equal(this, crypto);
    return expected;
  });
  assert.equal(randomUuid(), expected);
  assert.equal(native.mock.callCount(), 1);
});

test('缺少 randomUUID 时使用随机字节并设置 UUID v4 版本和变体位', (t) => {
  const descriptor = Object.getOwnPropertyDescriptor(globalThis, 'crypto');
  let fill = 0;
  const fallback = {
    getRandomValues(bytes) {
      assert.equal(this, fallback);
      assert.ok(bytes instanceof Uint8Array);
      assert.equal(bytes.length, 16);
      return bytes.fill(fill);
    },
  };
  Object.defineProperty(globalThis, 'crypto', { configurable: true, value: fallback });
  t.after(() => Object.defineProperty(globalThis, 'crypto', descriptor));
  assert.equal(randomUuid(), '00000000-0000-4000-8000-000000000000');
  fill = 0xff;
  assert.equal(randomUuid(), 'ffffffff-ffff-4fff-bfff-ffffffffffff');
});
