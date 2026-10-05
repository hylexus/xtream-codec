import { test } from 'node:test';
import assert from 'node:assert/strict';
import { inspectTraceBits } from '../src/components/trace-viewer.tsx';

test('808 消息体长度跨字节展示完整 10 位，排除相邻字段的位', () => {
  const node = { bitStart: 30, bitEnd: 40, attributes: { bitOrder: 'msb0' } };
  const bytes = ['7e', '02', '00', '40', '26', '01'];
  for (const selectedByte of [3, 4]) {
    const { rows } = inspectTraceBits(node, bytes, selectedByte);
    assert.deepEqual(
      rows.map((row) => row.offset),
      [3, 4],
    );
    assert.deepEqual(
      rows[0].bits.filter((cell) => cell.active).map((cell) => cell.bit),
      [1, 0],
    );
    assert.equal(rows[1].bits.filter((cell) => cell.active).length, 8);
    const fieldBits = rows.flatMap((row) => row.bits.filter((cell) => cell.active));
    assert.equal(fieldBits.map((cell) => cell.value).join(''), '0000100110');
    assert.equal(parseInt(fieldBits.map((cell) => cell.value).join(''), 2), 38);
  }
});

test('位视图保留字节边界，lsb0 高亮对应的物理低位', () => {
  const bytes = ['a5', '7e'];
  const msb = inspectTraceBits({ bitStart: 0, bitEnd: 3, attributes: {} }, bytes, 0);
  const lsb = inspectTraceBits(
    { bitStart: 0, bitEnd: 3, attributes: { bitOrder: 'lsb0' } },
    bytes,
    0,
  );
  assert.equal(msb.rows.length, 1);
  assert.deepEqual(
    msb.rows[0].bits.filter((cell) => cell.active).map((cell) => cell.bit),
    [7, 6, 5],
  );
  assert.deepEqual(
    lsb.rows[0].bits.filter((cell) => cell.active).map((cell) => cell.bit),
    [2, 1, 0],
  );
  const byteRange = inspectTraceBits({ byteStart: 0, byteEnd: 1, attributes: {} }, bytes, 0);
  assert.equal(byteRange.rows.length, 1);
  assert.ok(byteRange.rows[0].bits.every((cell) => cell.active));
});

test('长字段分段展示，跨 HEX 分页的末段仍保留不足一字节的边界', () => {
  const bytes = Array(270).fill('ff');
  const node = { bitStart: 254 * 8 + 6, bitEnd: 265 * 8 + 2, attributes: {} };
  const first = inspectTraceBits(node, bytes, 254);
  assert.equal(first.rows.length, 8);
  assert.equal(first.windowStart, 254);
  assert.equal(first.windowEnd, 262);
  const last = inspectTraceBits(node, bytes, 264);
  assert.deepEqual(
    last.rows.map((row) => row.offset),
    [262, 263, 264, 265],
  );
  assert.equal(last.rows.at(-1).bits.filter((cell) => cell.active).length, 2);
  assert.equal(last.windowEnd, last.end);
  assert.equal(
    [...first.rows, ...last.rows].flatMap((row) => row.bits.filter((cell) => cell.active)).length,
    node.bitEnd - node.bitStart,
  );
});

test('无字段或零宽字段只查看当前字节，失败跟踪不展示尚未生成的字节', () => {
  const bytes = ['aa', 'bb'];
  const unselected = inspectTraceBits(undefined, bytes, 1);
  assert.deepEqual(
    unselected.rows.map((row) => row.offset),
    [1],
  );
  assert.ok(unselected.rows[0].bits.every((cell) => !cell.active));
  const empty = inspectTraceBits({ bitStart: 8, bitEnd: 8, attributes: {} }, bytes, 1);
  assert.equal(empty.rows.length, 1);
  assert.ok(empty.rows[0].bits.every((cell) => !cell.active));
  const incomplete = inspectTraceBits({ bitStart: 8, bitEnd: 32, attributes: {} }, bytes, 1);
  assert.equal(incomplete.rows.length, 1);
});
