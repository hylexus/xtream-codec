export type JsonValue = string | number | boolean | null | JsonValue[] | Values;
export type Values = { [key: string]: JsonValue };
export type Path = (string | number)[];
export type Mode = 'dynamic' | 'entity' | 'field';

export interface Field {
  id: string;
  type: string;
  name?: string;
  bits?: number;
  endian?: string;
  bitOrder?: string;
  charset?: string;
  length?: {
    unit: string;
    fixed?: number;
    ref?: string;
    prefixBits?: number;
    remaining?: boolean;
    expression?: unknown;
  };
  fields?: Field[];
  item?: Field;
  variants?: Record<string, Field>;
  defaultVariant?: Field;
  versions?: number[];
  computedLengthOf?: string;
  generated?: unknown;
  [key: string]: unknown;
}

export interface Schema {
  schemaVersion: 1;
  fields: Field[];
}
export interface Draft {
  schema: Schema;
  values: string;
  hex: string;
  version: string;
  entityId: string;
}
export interface Entity {
  id: string;
  name?: string;
  className: string;
}
export interface Diagnostic {
  level: string;
  message: string;
  fieldPath?: string;
  bitOffset?: number;
}
export interface TraceNode {
  id: string;
  name: string;
  path?: string;
  kind: string;
  status: string;
  byteStart?: number | null;
  byteEnd?: number | null;
  bitStart?: number | null;
  bitEnd?: number | null;
  valueSummary?: string | null;
  hex?: string | null;
  children: TraceNode[];
  diagnostics: Diagnostic[];
  attributes: Record<string, unknown>;
}
export interface CodecResult {
  success: boolean;
  values: Values | null;
  hex: string;
  consumedBits: number;
  consumedBytes: number;
  remainingHex: string;
  error?: { message: string; fieldPath?: string; bitOffset?: number } | null;
  checksums: {
    fieldPath: string;
    actual: JsonValue;
    expected: JsonValue;
    matched: boolean;
    algorithm: string;
  }[];
  trace: {
    nodes: TraceNode[];
    root: TraceNode;
    nodeIdsByByteOffset: Record<string, string[]>;
    diagnostics: Diagnostic[];
  };
}
