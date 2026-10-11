import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  addField,
  createField,
  defaultValue,
  duplicateField,
  fieldDropIndex,
  getAt,
  moveField,
  normalizeHex,
  parseJson,
  parseScalar,
  parseSchema,
  pretty,
  removeField,
  replaceField,
  setAt,
} from '../src/model.ts';
import { draftFromPreset, initialDrafts, PRESETS } from '../src/presets.ts';

test('大整数 JSON、数值输入与 HEX 规范化保持精度及边界', () => {
  assert.deepEqual(
    parseJson('{"n":18446744073709551615,"safe":123,"nested":[-9223372036854775808,null]}'),
    { n: '18446744073709551615', safe: 123, nested: ['-9223372036854775808', null] },
  );
  assert.equal(parseScalar({ id: 'n', type: 'u64' }, '0xffffffffffffffff'), '18446744073709551615');
  assert.equal(parseScalar({ id: 'n', type: 'u16' }, '0x1234'), 4660);
  assert.equal(parseJson('1.5'), 1.5);
  assert.throws(() => parseJson('1.8446744073709551615e19'), /精度/);
  assert.throws(() => parseJson('1e400'), /精度/);
  assert.throws(() => parseScalar({ id: 'n', type: 'u8' }, '1.5'));
  assert.equal(normalizeHex('AA bb\n01'), 'aabb01');
  assert.throws(() => normalizeHex('abc'));
  assert.throws(() => normalizeHex('7x'));
});

test('重排与复制结构不修改原对象，复制值保持独立', () => {
  const schema = {
    schemaVersion: 1,
    fields: [
      { id: 'a', type: 'u8' },
      { id: 'b', type: 'struct', fields: [{ id: 'child', type: 'u8' }] },
    ],
  };
  const values = { a: 5, b: { child: 17 } };
  const moved = moveField(schema, ['fields', 0], 1);
  assert.deepEqual(
    moved.fields.map((f) => f.id),
    ['b', 'a'],
  );
  assert.deepEqual(
    schema.fields.map((f) => f.id),
    ['a', 'b'],
  );
  const duplicated = duplicateField(schema, values, ['fields', 1]);
  assert.equal(duplicated.schema.fields[2].id, 'b_copy');
  assert.deepEqual(duplicated.values.b_copy, { child: 17 });
  const changed = setAt(duplicated.values, ['b_copy', 'child'], 99);
  assert.equal(changed.b.child, 17);
  assert.equal(values.b.child, 17);
});

test('字段拖放的上下落点与最终顺序一致，相邻原位置不产生修改', () => {
  const schema = {
    schemaVersion: 1,
    fields: ['a', 'b', 'c', 'd'].map((id) => ({ id, type: 'u8' })),
  };
  for (const [source, target, position, expected] of [
    [0, 2, 'before', ['b', 'a', 'c', 'd']],
    [0, 2, 'after', ['b', 'c', 'a', 'd']],
    [3, 1, 'before', ['a', 'd', 'b', 'c']],
    [3, 1, 'after', ['a', 'b', 'd', 'c']],
    [0, 3, 'after', ['b', 'c', 'd', 'a']],
    [3, 0, 'before', ['d', 'a', 'b', 'c']],
  ]) {
    const path = ['fields', source];
    const index = fieldDropIndex(path, ['fields', target], position);
    assert.notEqual(index, null);
    assert.deepEqual(
      moveField(schema, path, index).fields.map((field) => field.id),
      expected,
    );
  }
  for (const [source, target, position] of [
    [1, 1, 'before'],
    [1, 1, 'after'],
    [1, 2, 'before'],
    [2, 1, 'after'],
  ])
    assert.equal(fieldDropIndex(['fields', source], ['fields', target], position), null);
  assert.deepEqual(
    schema.fields.map((field) => field.id),
    ['a', 'b', 'c', 'd'],
  );
});

test('嵌套字段只在自己的同级列表中排序，不能拖到容器或分支上', () => {
  const schema = {
    schemaVersion: 1,
    fields: [
      { id: 'count', type: 'u8' },
      {
        id: 'items',
        type: 'array',
        item: {
          id: 'item',
          type: 'struct',
          fields: [
            { id: 'code', type: 'u8' },
            { id: 'value', type: 'u16' },
          ],
        },
      },
    ],
  };
  const source = ['fields', 1, 'item', 'fields', 0];
  const index = fieldDropIndex(source, ['fields', 1, 'item', 'fields', 1], 'after');
  const moved = moveField(schema, source, index);
  assert.deepEqual(
    moved.fields[1].item.fields.map((field) => field.id),
    ['value', 'code'],
  );
  assert.deepEqual(
    schema.fields[1].item.fields.map((field) => field.id),
    ['code', 'value'],
  );
  for (const target of [
    ['fields', 0],
    ['fields', 1, 'item'],
    ['fields', 1, 'fields', 1],
    ['fields', 1, 'variants', '1'],
    ['fields', 1, 'variants', '1', 'fields', 1],
  ])
    assert.equal(fieldDropIndex(source, target, 'before'), null);
});

test('数组元素中的子字段重命名、复制、删除及新增同步每个元素', () => {
  const schema = {
    schemaVersion: 1,
    fields: [
      {
        id: 'items',
        type: 'array',
        length: { fixed: 2, unit: 'item' },
        item: {
          id: 'item',
          type: 'struct',
          fields: [{ id: 'value', type: 'u64', name: '数值' }],
        },
      },
    ],
  };
  const values = { items: [{ value: '18446744073709551615' }, { value: 2 }] };
  const path = ['fields', 0, 'item', 'fields', 0];
  const renamed = replaceField(schema, values, path, { id: 'number', type: 'u64' });
  assert.deepEqual(renamed.values.items, [{ number: '18446744073709551615' }, { number: 2 }]);
  const copied = duplicateField(renamed.schema, renamed.values, path);
  assert.deepEqual(copied.values.items, [
    { number: '18446744073709551615', number_copy: '18446744073709551615' },
    { number: 2, number_copy: 2 },
  ]);
  const added = addField(
    copied.schema,
    copied.values,
    path.slice(0, -1),
    createField('u8', 'status'),
  );
  assert.equal(added.values.items[0].status, 0);
  assert.equal(added.values.items[1].status, 0);
  const removed = removeField(added.schema, added.values, path);
  assert.equal(getAt(removed.values, ['items', 0, 'number']), undefined);
  assert.equal(values.items[0].value, '18446744073709551615');
  const specialId = replaceField(schema, values, path, { id: '__proto__', type: 'u64' });
  assert.equal(Object.hasOwn(specialId.values.items[0], '__proto__'), true);
  assert.equal(specialId.values.items[0].__proto__, '18446744073709551615');
});

test('导入导出保留高级属性，并拒绝无效 Schema', () => {
  for (const preset of PRESETS) assert.deepEqual(parseSchema(pretty(preset.schema)), preset.schema);
  const schema = {
    schemaVersion: 1,
    fields: [
      { id: 'kind', type: 'u8', generated: { constant: 1 } },
      {
        id: 'value',
        type: 'choice',
        ref: 'kind',
        variants: { 1: { id: 'one', type: 'u8' } },
        defaultVariant: { id: 'raw', type: 'hex', length: { fixed: 1, unit: 'byte' } },
      },
    ],
  };
  assert.deepEqual(parseSchema(pretty(schema)), schema);
  assert.equal(
    parseSchema(
      '{"schemaVersion":1,"fields":[{"id":"n","type":"u64","generated":{"constant":18446744073709551615}}]}',
    ).fields[0].generated.constant,
    '18446744073709551615',
  );
  assert.throws(() => parseSchema('{"schemaVersion":1,"fields":[{"id":"n","type":"u65"}]}'));
  assert.throws(() =>
    parseSchema('{"schemaVersion":1,"fields":[{"id":"n","type":"u8","bogus":1}]}'),
  );
});

test('默认类型可导出为合法 Schema，各模式草稿独立', () => {
  for (const type of [
    'u8',
    'u64',
    'float',
    'bits',
    'string',
    'bytes',
    'struct',
    'array',
    'tlv',
    'map',
    'pair',
    'derived',
    'padding',
    'align',
  ]) {
    const field = createField(type, 'field');
    assert.doesNotThrow(() => parseSchema(pretty({ schemaVersion: 1, fields: [field] })), type);
    assert.notEqual(defaultValue(field), undefined);
  }
  const drafts = initialDrafts();
  drafts.field.hex = '00';
  assert.equal(drafts.entity.hex, '123405');
  assert.equal(drafts.dynamic.hex, PRESETS[0].hex);
  const locationPreset = PRESETS.find((preset) => preset.id === 'jt808-0200');
  const locationDraft = draftFromPreset(locationPreset);
  assert.equal(locationDraft.version, '2019');
  const locationValues = parseJson(locationDraft.values);
  assert.equal(locationValues.terminalId, '013912344329');
  assert.equal(locationValues.body.latitude, 31234567);
  assert.deepEqual(locationValues.body.extraItems, [
    { key: 2, value: 100 },
    { key: 1, value: 12 },
  ]);
  for (const version of [2013, 2019]) {
    const fields = locationDraft.schema.fields.filter(
      (field) => !field.versions || field.versions.includes(version),
    );
    assert.equal(fields.filter((field) => field.id === 'versionFlag').length, 1);
    assert.equal(fields.filter((field) => field.id === 'terminalId').length, 1);
    assert.equal(
      fields.find((field) => field.id === 'terminalId').length.fixed,
      version === 2013 ? 6 : 10,
    );
    assert.equal(
      fields.some((field) => field.id === 'protocolVersion'),
      version === 2019,
    );
  }
  locationDraft.schema.fields.at(-1).id = 'changed';
  assert.equal(locationPreset.schema.fields.at(-1).id, 'end');
});
