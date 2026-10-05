import Ajv2020 from 'ajv/dist/2020.js';
import { parse, toSafeNumberOrThrow } from 'lossless-json';
import schemaDocument from '../../xtream-codec-core/src/main/resources/io/github/hylexus/xtream/codec/core/dynamic/schema/dynamic-codec-schema.json';
import type { Field, JsonValue, Path, Schema, Values } from './types';

export { schemaDocument };
export const FIELD_TYPES = [
  ...new Set([
    'u8',
    'u16',
    'u32',
    'u64',
    'i8',
    'i16',
    'i32',
    'i64',
    'u3',
    'u5',
    'float',
    'string',
    'bcd',
    'hex',
    'bytes',
    'bits',
    'struct',
    'array',
    'tlv',
    'map',
    'pair',
    'choice',
    'derived',
    'padding',
    'align',
    'extension',
    ...Array.from({ length: 64 }, (_, index) => `u${index + 1}`),
    ...Array.from({ length: 64 }, (_, index) => `i${index + 1}`),
  ]),
];
const validate = new Ajv2020({ allErrors: true, strict: false }).compile(schemaDocument);

// 大整数在页面中使用十进制字符串，避免 JSON 导入和响应读取先丢失精度。
export function parseJson(text: string): JsonValue {
  return parse(text, undefined, {
    parseNumber: (source: string) => {
      if (/^-?\d+$/.test(source) && !Number.isSafeInteger(Number(source))) return source;
      const value = Number(source);
      if (!Number.isFinite(value) || (Number.isInteger(value) && !Number.isSafeInteger(value))) {
        throw new Error('数值超出 JavaScript 的精度范围，请使用完整的十进制整数字符串');
      }
      try {
        return toSafeNumberOrThrow(source, { approx: true });
      } catch {
        throw new Error('数值超出 JavaScript 的精度范围，请使用完整的十进制整数字符串');
      }
    },
  }) as JsonValue;
}

export function parseValues(text: string): Values {
  const value = parseJson(text);
  if (!value || typeof value !== 'object' || Array.isArray(value))
    throw new Error('编码值必须是 JSON 对象');
  return value;
}

export function parseSchema(text: string): Schema {
  const schema = parseJson(text);
  if (!validate(schema)) {
    throw new Error(
      validate.errors
        ?.map((error) => `${error.instancePath || '$'} ${error.message}`)
        .slice(0, 4)
        .join('；'),
    );
  }
  return schema as unknown as Schema;
}

export const pretty = (value: unknown) => JSON.stringify(value, null, 2);
export const pathKey = (path: Path) => JSON.stringify(path);

export function getAt<T = unknown>(root: unknown, path: Path): T | undefined {
  let value = root;
  for (const key of path) {
    if (!value || typeof value !== 'object' || !Object.hasOwn(value, key)) return undefined;
    value = (value as Record<string | number, unknown>)[key];
  }
  return value as T;
}

export function setAt<T>(root: T, path: Path, value: unknown): T {
  if (!path.length) return value as T;
  const [key, ...rest] = path;
  const child = setAt(getAt(root, [key]), rest, value);
  if (Array.isArray(root)) {
    const copy = [...root];
    copy[Number(key)] = child;
    return copy as T;
  }
  return { ...(root as object), [key]: child } as T;
}

export function fieldColor(type: string): string {
  if (/^[ui]\d+$/.test(type) || type === 'float') return 'blue';
  if (['struct', 'array', 'tlv', 'map', 'pair', 'choice'].includes(type)) return 'violet';
  if (['derived', 'padding', 'align'].includes(type)) return 'amber';
  return 'green';
}

export function createField(type = 'u8', id = 'field'): Field {
  const field: Field = { id, type };
  if (type === 'struct') field.fields = [];
  if (type === 'array') {
    field.length = { prefixBits: 8, unit: 'item' };
    field.item = { id: 'item', type: 'u8' };
  }
  if (['string', 'hex', 'bcd', 'bytes'].includes(type))
    field.length = { prefixBits: 8, unit: 'byte' };
  if (type === 'float') field.bits = 32;
  if (type === 'bits') field.bits = 8;
  if (type === 'padding') field.length = { fixed: 1, unit: 'byte' };
  if (type === 'align') field.alignTo = 8;
  if (type === 'derived') field.expression = { value: 0 };
  if (type === 'choice') {
    field.ref = 'selector';
    field.variants = { '1': { id: 'selected', type: 'u8' } };
  }
  if (['tlv', 'map', 'pair'].includes(type)) {
    field.length = { prefixBits: 8, unit: 'item' };
    field.options = type === 'pair' ? { keyBits: 8 } : { keyBits: 8, lengthBits: 8 };
    field.variants = { '1': { id: 'value', type: 'u8' } };
  }
  return field;
}

export function defaultValue(field: Field): JsonValue {
  if (field.type === 'struct')
    return Object.fromEntries(
      (field.fields || []).filter((f) => !isAutomatic(f)).map((f) => [f.id, defaultValue(f)]),
    );
  if (['array', 'tlv', 'map', 'pair'].includes(field.type)) return [];
  if (field.type === 'choice') return field.defaultVariant ? defaultValue(field.defaultVariant) : 0;
  if (field.type === 'bits') return '0'.repeat(field.bits || 8);
  if (['string', 'bcd', 'hex'].includes(field.type)) return '';
  if (field.type === 'bytes') return [];
  return 0;
}

export function isAutomatic(field: Field) {
  return (
    !!field.generated ||
    !!field.computedLengthOf ||
    ['derived', 'padding', 'align'].includes(field.type)
  );
}

export function uniqueId(fields: Field[], base = 'field') {
  const ids = new Set(fields.map((field) => field.id));
  let id = base;
  for (let suffix = 2; ids.has(id); suffix++) id = `${base}_${suffix}`;
  return id;
}

export function moveField(schema: Schema, path: Path, targetIndex: number): Schema {
  const parent = path.slice(0, -1);
  const list = getAt<Field[]>(schema, parent);
  const index = Number(path.at(-1));
  if (!list || targetIndex < 0 || targetIndex >= list.length) return schema;
  const copy = [...list];
  const [field] = copy.splice(index, 1);
  copy.splice(targetIndex, 0, field);
  return setAt(schema, parent, copy);
}

// 结构定义的数组元素只出现一次，实际值可能有多个；重命名和删除需更新每个元素。
function valuePaths(schema: Schema, values: Values, target: Path): Path[] {
  const found: Path[] = [];
  const walk = (field: Field, schemaPath: Path, valuePath: Path) => {
    if (pathKey(schemaPath) === pathKey(target)) found.push(valuePath);
    field.fields?.forEach((child, index) =>
      walk(child, [...schemaPath, 'fields', index], [...valuePath, child.id]),
    );
    if (field.item) {
      const list = getAt<JsonValue[]>(values, valuePath);
      if (Array.isArray(list))
        list.forEach((_value, index) =>
          walk(field.item!, [...schemaPath, 'item'], [...valuePath, index]),
        );
    }
  };
  schema.fields.forEach((field, index) => walk(field, ['fields', index], [field.id]));
  return found;
}

export function replaceField(schema: Schema, values: Values, path: Path, field: Field) {
  const old = getAt<Field>(schema, path)!;
  let nextValues = values;
  if (old.id !== field.id && path.at(-1) !== 'item') {
    for (const valuePath of valuePaths(schema, values, path)) {
      const parentPath = valuePath.slice(0, -1);
      const parent = { ...getAt<Values>(nextValues, parentPath) };
      if (Object.hasOwn(parent, old.id)) {
        const value = parent[old.id];
        delete parent[old.id];
        nextValues = setAt(nextValues, parentPath, { ...parent, [field.id]: value });
      }
    }
  }
  return { schema: setAt(schema, path, field), values: nextValues };
}

export function addField(schema: Schema, values: Values, parentPath: Path, field: Field) {
  const fields = getAt<Field[]>(schema, parentPath) || [];
  let nextValues = values;
  if (!isAutomatic(field)) {
    const parents =
      parentPath.length === 1 ? [[]] : valuePaths(schema, values, parentPath.slice(0, -1));
    for (const parent of parents)
      nextValues = setAt(nextValues, [...parent, field.id], defaultValue(field));
  }
  return { schema: setAt(schema, parentPath, [...fields, field]), values: nextValues };
}

export function removeField(schema: Schema, values: Values, path: Path) {
  const parentPath = path.slice(0, -1);
  const fields = [...getAt<Field[]>(schema, parentPath)!];
  fields.splice(Number(path.at(-1)), 1);
  let nextValues = values;
  for (const valuePath of valuePaths(schema, values, path)) {
    const valueParent = valuePath.slice(0, -1);
    const parent = { ...getAt<Values>(nextValues, valueParent) };
    delete parent[String(valuePath.at(-1))];
    nextValues = setAt(nextValues, valueParent, parent);
  }
  return { schema: setAt(schema, parentPath, fields), values: nextValues };
}

export function duplicateField(schema: Schema, values: Values, path: Path) {
  const parentPath = path.slice(0, -1);
  const fields = [...getAt<Field[]>(schema, parentPath)!];
  const index = Number(path.at(-1));
  const copy = structuredClone(fields[index]);
  copy.id = uniqueId(fields, `${copy.id}_copy`);
  fields.splice(index + 1, 0, copy);
  let nextValues = values;
  for (const valuePath of valuePaths(schema, values, path)) {
    const value = getAt<JsonValue>(values, valuePath);
    if (value !== undefined)
      nextValues = setAt(nextValues, [...valuePath.slice(0, -1), copy.id], structuredClone(value));
  }
  return { schema: setAt(schema, parentPath, fields), values: nextValues };
}

export function parseScalar(field: Field, text: string): JsonValue {
  if (!/^[ui]\d+$/.test(field.type) && field.type !== 'float') return text;
  if (!text.trim()) throw new Error('请输入数值');
  if (field.type === 'float') {
    const value = Number(text);
    if (!Number.isFinite(value)) throw new Error('请输入有限浮点数');
    return value;
  }
  const integer = BigInt(text.trim());
  const number = Number(integer);
  return Number.isSafeInteger(number) ? number : integer.toString();
}

export function normalizeHex(text: string): string {
  const hex = text.replace(/\s+/g, '').toLowerCase();
  if (!/^(?:[\da-f]{2})*$/.test(hex)) throw new Error('HEX 需为偶数个十六进制字符，可包含空白');
  return hex;
}
