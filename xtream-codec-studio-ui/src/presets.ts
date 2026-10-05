import type { Draft, Mode, Schema, Values } from './types';
import { pretty } from './model';

export interface Preset {
  id: string;
  title: string;
  description: string;
  schema: Schema;
  values: Values;
  hex: string;
  version?: number;
}
export const PRESETS: Preset[] = [
  {
    id: 'nested',
    title: '位字段与嵌套集合',
    description: '3 + 5 bit · 长度回填 · struct · array',
    schema: {
      schemaVersion: 1,
      fields: [
        { id: 'version', name: '协议版本', type: 'u3' },
        { id: 'flags', name: '状态标志', type: 'u5' },
        { id: 'length', name: '负载长度', type: 'u8', computedLengthOf: 'payload' },
        {
          id: 'payload',
          name: '消息负载',
          type: 'struct',
          length: { ref: 'length', unit: 'byte' },
          fields: [
            { id: 'count', name: '元素数量', type: 'u8' },
            {
              id: 'items',
              name: '数据项',
              type: 'array',
              length: { ref: 'count', unit: 'item' },
              item: {
                id: 'item',
                type: 'struct',
                fields: [
                  { id: 'code', name: '数据标识', type: 'u8' },
                  { id: 'value', name: '数值', type: 'u16' },
                ],
              },
            },
          ],
        },
      ],
    },
    values: {
      version: 5,
      flags: 17,
      payload: {
        count: 2,
        items: [
          { code: 1, value: 4660 },
          { code: 2, value: 22136 },
        ],
      },
    },
    hex: 'b10702011234025678',
  },
  {
    id: 'text',
    title: '文本与字节序',
    description: 'UTF-8 · BCD · HEX · 小端整数',
    schema: {
      schemaVersion: 1,
      fields: [
        { id: 'sequence', name: '流水号', type: 'u16', endian: 'little' },
        {
          id: 'title',
          name: '文本',
          type: 'string',
          charset: 'UTF-8',
          length: { prefixBits: 8, unit: 'byte' },
        },
        { id: 'terminal', name: 'BCD 标识', type: 'bcd', length: { fixed: 6, unit: 'byte' } },
        { id: 'raw', name: '原始字节', type: 'hex', length: { prefixBits: 8, unit: 'byte' } },
      ],
    },
    values: { sequence: 4660, title: 'Xtream', terminal: '139123456789', raw: 'aabbcc' },
    hex: '34120658747265616d13912345678903aabbcc',
  },
  {
    id: 'checksum',
    title: '自动生成与校验和',
    description: '固定边界 · 自动计数 · XOR8',
    schema: {
      schemaVersion: 1,
      fields: [
        { id: 'start', name: '起始标记', type: 'u8', generated: { constant: 126 } },
        { id: 'count', name: '自动计数', type: 'u8', generated: { countOf: 'items' } },
        {
          id: 'items',
          name: '数据项',
          type: 'array',
          length: { ref: 'count', unit: 'item' },
          item: { id: 'item', type: 'u8' },
        },
        {
          id: 'checksum',
          name: '校验和',
          type: 'u8',
          generated: { checksum: { algorithm: 'xor8', range: 'count..items' } },
        },
        { id: 'end', name: '结束标记', type: 'u8', generated: { constant: 126 } },
      ],
    },
    values: { items: [16, 32, 48] },
    hex: '7e03102030037e',
  },
  {
    id: 'jt808-0200',
    title: '808 · 0200 位置汇报',
    description: '2013 / 2019 · BCD 时间 · TLV 附加项 · XOR8',
    version: 2019,
    schema: {
      schemaVersion: 1,
      fields: [
        { id: 'start', name: '起始标记 0x7e', type: 'u8', generated: { constant: 126 } },
        { id: 'messageId', name: '消息 ID 0x0200', type: 'u16', generated: { constant: 512 } },
        { id: 'reserved', name: '保留位', type: 'u1', generated: { constant: 0 } },
        {
          id: 'versionFlag',
          name: '版本标识',
          type: 'u1',
          versions: [2013],
          generated: { constant: 0 },
        },
        {
          id: 'versionFlag',
          name: '版本标识',
          type: 'u1',
          versions: [2019],
          generated: { constant: 1 },
        },
        { id: 'hasSubpackage', name: '分包标识', type: 'u1' },
        { id: 'encryption', name: '加密方式', type: 'u3' },
        { id: 'bodyLength', name: '消息体长度', type: 'u10', computedLengthOf: 'body' },
        {
          id: 'protocolVersion',
          name: '协议版本号',
          type: 'u8',
          versions: [2019],
          generated: { constant: 1 },
        },
        {
          id: 'terminalId',
          name: '终端手机号',
          type: 'bcd',
          versions: [2013],
          length: { fixed: 6, unit: 'byte' },
        },
        {
          id: 'terminalId',
          name: '终端 ID',
          type: 'bcd',
          versions: [2019],
          length: { fixed: 10, unit: 'byte' },
          options: { padByte: 0, padSide: 'left' },
        },
        { id: 'serialNo', name: '消息流水号', type: 'u16' },
        {
          id: 'subpackage',
          name: '分包信息',
          type: 'struct',
          when: { ref: 'hasSubpackage', op: 'eq', value: '1' },
          fields: [
            { id: 'total', name: '分包总数', type: 'u16' },
            { id: 'index', name: '包序号', type: 'u16' },
          ],
        },
        {
          id: 'body',
          name: '位置信息汇报',
          type: 'struct',
          length: { ref: 'bodyLength', unit: 'byte' },
          fields: [
            { id: 'alarmFlag', name: '报警标志', type: 'u32' },
            { id: 'status', name: '状态', type: 'u32' },
            { id: 'latitude', name: '纬度 · 10⁻⁶ 度', type: 'u32' },
            { id: 'longitude', name: '经度 · 10⁻⁶ 度', type: 'u32' },
            { id: 'altitude', name: '高程 · 米', type: 'u16' },
            { id: 'speed', name: '速度 · 0.1 km/h', type: 'u16' },
            { id: 'direction', name: '方向 · 度', type: 'u16' },
            {
              id: 'time',
              name: '时间 · yyMMddHHmmss',
              type: 'bcd',
              length: { fixed: 6, unit: 'byte' },
            },
            {
              id: 'extraItems',
              name: '附加信息',
              type: 'tlv',
              length: { remaining: true, unit: 'byte' },
              options: { keyBits: 8, lengthBits: 8 },
              variants: {
                '1': { id: 'mileage', name: '里程 · 0.1 km', type: 'u32' },
                '2': { id: 'fuel', name: '油量 · 0.1 L', type: 'u16' },
              },
            },
          ],
        },
        {
          id: 'checksum',
          name: '校验码',
          type: 'u8',
          generated: { checksum: { algorithm: 'xor8', range: 'messageId..body' } },
        },
        { id: 'end', name: '结束标记 0x7e', type: 'u8', generated: { constant: 126 } },
      ],
    },
    values: {
      hasSubpackage: 0,
      encryption: 0,
      terminalId: '013912344329',
      serialNo: 0,
      body: {
        alarmFlag: 111,
        status: 22,
        latitude: 31234567,
        longitude: 121987654,
        altitude: 123,
        speed: 66,
        direction: 3,
        time: '250129223344',
        extraItems: [
          { key: 2, value: 100 },
          { key: 1, value: 12 },
        ],
      },
    },
    hex: '7e02004026010000000001391234432900000000006f0000001601dc9a0707456246007b004200032501292233440202006401040000000c417e',
  },
];

export function draftFromPreset(preset: Preset): Draft {
  return {
    schema: structuredClone(preset.schema),
    values: pretty(preset.values),
    hex: preset.hex,
    version: preset.version === undefined ? '' : String(preset.version),
    entityId: '',
  };
}

export function initialDrafts(): Record<Mode, Draft> {
  return {
    dynamic: draftFromPreset(PRESETS[0]),
    entity: {
      schema: { schemaVersion: 1, fields: [] },
      values: '{"sequence":4660,"flags":5}',
      hex: '123405',
      version: '',
      entityId: 'sample',
    },
    field: {
      schema: { schemaVersion: 1, fields: [{ id: 'value', name: '试验字段', type: 'u64' }] },
      values: '{"value":"18446744073709551615"}',
      hex: 'ffffffffffffffff',
      version: '',
      entityId: '',
    },
  };
}
