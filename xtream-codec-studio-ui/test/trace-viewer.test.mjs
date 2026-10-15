import { test } from 'node:test';
import assert from 'node:assert/strict';
import { inspectTraceBits, traceColor, traceDataType } from '../src/components/trace-viewer.tsx';

test('Trace 优先显示声明类型，兼容实体 Java 类型及没有类型的旧结果', () => {
  assert.equal(
    traceDataType({ kind: 'FIELD', attributes: { dataType: 'u10' }, javaType: 'int' }),
    'u10',
  );
  assert.equal(traceDataType({ kind: 'LENGTH_FIELD', attributes: { dataType: 'u8' } }), 'u8');
  assert.equal(traceDataType({ kind: 'COLLECTION', attributes: { dataType: 'array' } }), 'array');
  assert.equal(
    traceDataType({ kind: 'FIELD', attributes: {}, javaType: 'java.lang.String' }),
    'String',
  );
  assert.equal(traceDataType({ kind: 'FIELD', attributes: {}, javaType: 'byte[]' }), 'byte[]');
  assert.equal(
    traceDataType({ kind: 'FIELD', attributes: { dataType: '' }, javaType: 'int' }),
    'int',
  );
  assert.equal(traceDataType({ kind: 'NESTED_FIELD', attributes: {} }), 'NESTED_FIELD');
});

test('Trace 同类型跨节点职责和路径保持同色，颜色不依赖类型出现顺序', () => {
  const original = {
    kind: 'FIELD',
    status: 'SUCCESS',
    path: 'version',
    attributes: { dataType: 'u8' },
  };
  const copy = structuredClone(original);
  const color = traceColor(original);
  assert.equal(color, 'var(--studio-amber)');
  for (const kind of ['LENGTH_FIELD', 'COLLECTION_ITEM', 'MAP_ENTRY', 'NESTED_FIELD']) {
    assert.equal(traceColor({ ...original, kind, path: `body.items[5].${kind}` }), color);
  }
  const nodes = ['u1', 'u16', 'u32', 'u64', 'bcd', 'string', 'struct', 'array', 'tlv', 'entry'].map(
    (type) => ({ ...original, attributes: { dataType: type } }),
  );
  const colors = nodes.map(traceColor);
  assert.deepEqual([...nodes].reverse().map(traceColor).reverse(), colors);
  assert.equal(traceColor(original), color);
  assert.equal(colors[1], 'var(--studio-green)');
  assert.equal(colors[2], 'var(--studio-violet)');
  assert.deepEqual(original, copy);
});

test('Trace 类型颜色沿用显示类型优先级，兼容 Java 类型与旧节点', () => {
  const node = { kind: 'FIELD', status: 'SUCCESS', attributes: { dataType: 'String' } };
  const color = traceColor(node);
  assert.equal(traceColor({ ...node, attributes: { dataType: ' String ' } }), color);
  assert.equal(traceColor({ ...node, attributes: {}, javaType: 'java.lang.String' }), color);
  assert.equal(traceColor({ ...node, javaType: 'java.lang.Integer' }), color);
  assert.equal(
    traceColor({ ...node, kind: 'NESTED_FIELD', attributes: {} }),
    traceColor({ ...node, attributes: { dataType: 'NESTED_FIELD' } }),
  );
});

test('Trace 错误状态覆盖为红色，正常类型色板保留错误红色', () => {
  const node = { kind: 'LENGTH_FIELD', status: 'SUCCESS', attributes: { dataType: 'u8' } };
  for (const status of ['ERROR', 'FAILED']) {
    assert.equal(traceColor({ ...node, status }), 'var(--studio-red)');
  }
  for (let width = 1; width <= 64; width++) {
    for (const prefix of ['u', 'i']) {
      assert.notEqual(
        traceColor({ ...node, attributes: { dataType: `${prefix}${width}` } }),
        'var(--studio-red)',
      );
    }
  }
  assert.equal(traceColor(node), 'var(--studio-amber)');
});

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
