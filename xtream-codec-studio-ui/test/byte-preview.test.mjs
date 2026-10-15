import { test } from 'node:test';
import assert from 'node:assert/strict';
import { previewBytes, previewFieldValue } from '../src/byte-preview.ts';
import { inspectTraceBits, previewBitRange } from '../src/components/trace-viewer.tsx';

test('位字段的实际值与完整边界字节区分，跨字节字段保留完整位宽', () => {
  const flag = previewFieldValue(
    { status: 'SUCCESS', value: 1, valueSummary: '1' },
    'u1',
    [25, 26],
  );
  assert.deepEqual(flag, { dataType: 'u1', bits: 1, value: '1', hex: '0x1', binary: '1' });
  assert.equal(
    previewFieldValue({ status: 'SUCCESS', value: 1, valueSummary: '64' }, 'u1', [25, 26]).value,
    '1',
  );
  assert.equal(Object.fromEntries(previewBytes(['40']).numbers)['UInt BE'], '64');
  const length = previewFieldValue({ status: 'SUCCESS', value: 38 }, 'u10', [30, 40]);
  assert.equal(length.value, '38');
  assert.equal(length.hex, '0x26');
  assert.equal(length.binary, '0000100110');
  assert.equal(Object.fromEntries(previewBytes(['40', '26']).numbers)['UInt BE'], '16422');
});

test('有符号、小端和 lsb0 字段使用 Trace 实际值，负数基数按字段位宽补码展示', () => {
  const signed = previewFieldValue({ status: 'SUCCESS', value: -2 }, 'i3', [1, 4]);
  assert.equal(signed.value, '-2');
  assert.equal(signed.hex, '0x6');
  assert.equal(signed.binary, '110');
  const little = previewFieldValue({ status: 'SUCCESS', value: 4660 }, 'u16', [0, 16]);
  assert.equal(little.hex, '0x1234');
  assert.equal(little.binary, '0001001000110100');
  assert.equal(Object.fromEntries(previewBytes(['34', '12']).numbers)['UInt BE'], '13330');
  assert.equal(Object.fromEntries(previewBytes(['34', '12']).numbers)['UInt LE'], '4660');
  const lsb = previewFieldValue({ status: 'SUCCESS', value: 3 }, 'u3', [0, 3]);
  assert.equal(lsb.binary, '011');
  const entity = previewFieldValue({ status: 'SUCCESS', value: -129 }, 'Integer', [0, 16]);
  assert.equal(entity.hex, '0xFF7F');
  assert.equal(entity.binary, '1111111101111111');
  assert.equal(
    previewFieldValue({ status: 'SUCCESS', value: 65000 }, 'Integer', [0, 16]).hex,
    '0xFDE8',
  );
});

test('字段预览保留 u64 和 i64 精度，旧结果可使用整数摘要', () => {
  const unsigned = previewFieldValue(
    { status: 'SUCCESS', value: '18446744073709551615', valueSummary: '18446744073709551615' },
    'u64',
    [0, 64],
  );
  assert.equal(unsigned.value, '18446744073709551615');
  assert.equal(unsigned.hex, '0xFFFFFFFFFFFFFFFF');
  assert.equal(unsigned.binary, '1'.repeat(64));
  const signed = previewFieldValue(
    { status: 'SUCCESS', value: '-9223372036854775808' },
    'i64',
    [0, 64],
  );
  assert.equal(signed.value, '-9223372036854775808');
  assert.equal(signed.hex, '0x8000000000000000');
  assert.equal(signed.binary, '1' + '0'.repeat(63));
  const legacy = previewFieldValue({ status: 'SUCCESS', valueSummary: '38' }, 'u10', [30, 40]);
  assert.equal(legacy.value, '38');
  assert.equal(legacy.hex, '0x26');
  assert.equal(previewFieldValue(undefined, '', null), null);
});

test('失败、无值、非整数和越界字段不生成看似有效的整数预览', () => {
  for (const [node, type] of [
    [{ status: 'ERROR', value: 1, valueSummary: '1' }, 'u1'],
    [{ status: 'STARTED', value: 1 }, 'u1'],
    [{ status: 'SUCCESS' }, 'u8'],
    [{ status: 'SUCCESS', value: '123' }, 'string'],
    [{ status: 'SUCCESS', value: 1.5 }, 'f32'],
    [{ status: 'SUCCESS', value: { count: 1 }, valueSummary: '{count=1}' }, 'struct'],
    [{ status: 'SUCCESS', value: 2 }, 'u1'],
    [{ status: 'SUCCESS', value: -1 }, 'u1'],
    [{ status: 'SUCCESS', value: '12...' }, 'u64'],
    [{ status: 'SUCCESS', value: Number.MAX_SAFE_INTEGER + 1 }, 'u64'],
  ]) {
    const field = previewFieldValue(node, type, [0, 8]);
    assert.equal(field.hex, null);
    assert.equal(field.binary, null);
  }
  assert.equal(
    previewFieldValue({ status: 'ERROR', value: 1 }, 'u1', [0, 1]).value,
    '字段执行失败',
  );
  assert.equal(previewFieldValue({ status: 'STARTED' }, 'u1', [0, 1]).value, '字段尚未完成');
  assert.equal(previewFieldValue({ status: 'SUCCESS', value: '' }, 'string', [0, 0]).value, '""');
  assert.equal(previewFieldValue({ status: 'SUCCESS', value: 0 }, 'u8', [0, 8]).value, '0');
});

test('同一字节范围的大小端、有符号和 BCD 预览保留精度与前导零', () => {
  const { numbers, encodings } = previewBytes(['02', '00']);
  assert.deepEqual(Object.fromEntries(numbers), {
    HEX: '02 00',
    BIN: '00000010 00000000',
    'UInt BE': '512',
    'UInt LE': '2',
    'Int BE': '512',
    'Int LE': '2',
  });
  assert.equal(Object.fromEntries(encodings).BCD, '0200');
  const signed = Object.fromEntries(previewBytes(['ff', '7f']).numbers);
  assert.equal(signed['Int BE'], '-129');
  assert.equal(signed['Int LE'], '32767');
  const full = Object.fromEntries(previewBytes(Array(8).fill('ff')).numbers);
  assert.equal(full['UInt BE'], '18446744073709551615');
  assert.equal(full['Int BE'], '-1');
  assert.equal(full['Int LE'], '-1');
});

test('编码预览支持中文并区分控制字符、非法 UTF-8 和非法 BCD', () => {
  const utf8 = Object.fromEntries(previewBytes(['e4', 'b8', 'ad', 'e6', '96', '87']).encodings);
  assert.equal(utf8['UTF-8'], '中文');
  const gbk = Object.fromEntries(previewBytes(['d6', 'd0', 'ce', 'c4']).encodings);
  assert.equal(gbk.GBK, '中文');
  assert.equal(gbk.GB2312, '中文');
  assert.equal(gbk['UTF-8'], '无法解码');
  assert.equal(gbk.BCD, '非 BCD');
  const control = Object.fromEntries(previewBytes(['41', '00', '7f']).encodings);
  assert.equal(control.ASCII, 'A··');
  assert.equal(control['UTF-8'], 'A··');
});

test('长字段各段预览仅使用当前八字节，末段不混入相邻字段', () => {
  const bytes = ['ff', ...Array(8).fill('41'), '42', '43', 'ff'];
  const node = { bitStart: 8, bitEnd: 88, attributes: {} };
  const first = inspectTraceBits(node, bytes, 1);
  const last = inspectTraceBits(node, bytes, 10);
  const firstPreview = previewBytes(first.rows.map((row) => row.hex));
  const lastPreview = previewBytes(last.rows.map((row) => row.hex));
  assert.equal(Object.fromEntries(firstPreview.encodings).ASCII, 'AAAAAAAA');
  assert.equal(Object.fromEntries(lastPreview.encodings).ASCII, 'BC');
  assert.equal(Object.fromEntries(lastPreview.numbers)['UInt BE'], '16963');
  assert.ok([...first.rows, ...last.rows].every((row) => row.bits.every((bit) => bit.active)));
});

test('位字段的数值使用完整边界字节，缺少生成数据时仅预览已有字节', () => {
  const selected = inspectTraceBits({ bitStart: 6, bitEnd: 10, attributes: {} }, ['03', 'c0'], 0);
  assert.equal(selected.rows.flatMap((row) => row.bits).filter((bit) => bit.active).length, 4);
  assert.equal(
    Object.fromEntries(previewBytes(selected.rows.map((row) => row.hex)).numbers).HEX,
    '03 C0',
  );
  const truncated = inspectTraceBits({ bitStart: 0, bitEnd: 24, attributes: {} }, ['ff'], 0);
  assert.equal(
    Object.fromEntries(previewBytes(truncated.rows.map((row) => row.hex)).numbers)['UInt BE'],
    '255',
  );
  assert.ok(previewBytes([]).numbers.every(([, value]) => value === '—'));
  assert.ok(previewBytes([]).encodings.every(([, value]) => value === '—'));
});

test('前置长度字段只预览内容，长度节点仍能单独查看且旧 Trace 保持兼容', () => {
  const bytes = ['03', '73', '73', '73', 'ff'];
  const field = {
    bitStart: 0,
    bitEnd: 32,
    attributes: { contentBitStart: 8, contentBitEnd: 32 },
  };
  assert.deepEqual(previewBitRange(field), [8, 32]);
  const content = inspectTraceBits(field, bytes, 0);
  assert.deepEqual(
    content.rows.map((row) => row.offset),
    [1, 2, 3],
  );
  assert.equal(
    Object.fromEntries(previewBytes(content.rows.map((row) => row.hex)).encodings)['UTF-8'],
    'sss',
  );
  const prefix = inspectTraceBits({ bitStart: 0, bitEnd: 8, attributes: {} }, bytes, 0);
  assert.equal(
    Object.fromEntries(previewBytes(prefix.rows.map((row) => row.hex)).numbers)['UInt BE'],
    '3',
  );
  const legacy = inspectTraceBits({ ...field, attributes: {} }, bytes, 0);
  assert.equal(
    Object.fromEntries(previewBytes(legacy.rows.map((row) => row.hex)).encodings)['UTF-8'],
    '·sss',
  );
});

test('空内容不读取前缀或相邻字段，内容分页不受前缀位宽影响', () => {
  const empty = inspectTraceBits(
    {
      bitStart: 0,
      bitEnd: 8,
      attributes: { contentBitStart: 8, contentBitEnd: 8 },
    },
    ['00', 'ff'],
    0,
  );
  assert.deepEqual(empty.rows, []);
  assert.equal(empty.contentOnly, true);
  const bytes = ['00', '0a', ...Array(10).fill('41'), 'ff'];
  const field = { bitStart: 0, bitEnd: 96, attributes: { contentBitStart: 16, contentBitEnd: 96 } };
  const first = inspectTraceBits(field, bytes, 0);
  const last = inspectTraceBits(field, bytes, 10);
  assert.deepEqual([first.windowStart, first.windowEnd], [2, 10]);
  assert.deepEqual([last.windowStart, last.windowEnd], [10, 12]);
  assert.equal(
    Object.fromEntries(previewBytes(last.rows.map((row) => row.hex)).encodings).ASCII,
    'AA',
  );
});
