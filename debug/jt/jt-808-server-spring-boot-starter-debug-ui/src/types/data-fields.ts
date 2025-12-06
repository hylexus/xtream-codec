import {warn} from "vue";

export const FIELD_TYPES = [
    "gbk_string",
    "gb2312_string",
    "utf8_string",
    "bcd_8421_string",
    "hex_string",
    "string",
    "i8",
    "u8",
    "i16",
    "u16",
    "i32",
    "u32",
    "i64",
    "f32",
    "f64",
    "struct",
    "seq",
    "byte_seq",
    "dict",
] as const;

export type FieldType = typeof FIELD_TYPES[number];

export type DicKeyType = 'i8' | 'u8' | 'i16' | 'u16' | 'i32' | 'u32' | 'i64';

export type ValueLengthType = 'i8' | 'u8' | 'i16' | 'u16' | 'i32' | 'u32' | 'i64';

export type PrependLengthFieldType = 'none' | 'u8' | 'u16' | 'u32';

export interface DataField {
    type: FieldType;
    value: unknown;
    name?: string;
    id?: string;
    prependLengthFieldType?: PrependLengthFieldType;

    [key: string]: unknown;
}

export type TypedDataField<T extends FieldType> = DataField & { type: T };

export interface StringDataField extends DataField {
    charset: string;
}

export interface Bcd8421StringLike extends StringDataField {
    type: 'bcd_8421_string';
    charset: 'bcd_8421';
    value: string;
    fixedLength?: number;
}

export interface Gb2312StringLike extends StringDataField {
    type: 'gb2312_string';
    charset: 'gb2312';
    value: string;
}

export interface GbkStringLike extends StringDataField {
    type: 'gbk_string';
    charset: 'gbk';
    value: string;
}

export interface HexStringLike extends StringDataField {
    type: 'hex_string';
    charset: 'hex';
    value: string;
}

export interface Utf8StringLike extends StringDataField {
    type: 'utf8_string';
    charset: 'utf-8';
    value: string;
}

export interface StringLike extends StringDataField {
    type: 'string';
    value: string;
    charset: string;
}

export interface IntegralDataField extends DataField {
    value: number;
}

export interface I8Like extends IntegralDataField {
    type: "i8";
    value: number;
}

export interface U8Like extends IntegralDataField {
    type: "u8";
    value: number;
}

export interface I16Like extends IntegralDataField {
    type: "i16";
    value: number;
}

export interface U16Like extends IntegralDataField {
    type: "u16";
    value: number;
}

export interface I32Like extends IntegralDataField {
    type: "i32";
    value: number;
}

export interface U32Like extends IntegralDataField {
    type: "u32";
    value: number;
}

export interface I64Like extends IntegralDataField {
    type: "i64";
    value: number;
}

export interface FloatDataField extends DataField {
    value: number;
}

export interface F32Like extends FloatDataField {
    type: "f32";
    value: number;
}

export interface F64Like extends FloatDataField {
    type: 'f64';
    value: number;
}

export interface ByteSequenceLike extends DataField {
    type: 'byte_seq';
    value: number[];
}

export interface StructLike extends DataField {
    type: 'struct';
    value: ConcreteDataField[];
}

export interface SequenceLike extends DataField {
    type: 'seq';
    value: ConcreteDataField[];
}

export interface DictLike extends DataField {
    type: 'dict';
    keyType: DicKeyType;
    valueLengthType: ValueLengthType;
    value: Record<number, ConcreteDataField>;
}

export type ConcreteDataField =
    | Bcd8421StringLike
    | Gb2312StringLike
    | GbkStringLike
    | HexStringLike
    | Utf8StringLike
    | StringLike
    | I8Like
    | U8Like
    | I16Like
    | U16Like
    | I32Like
    | U32Like
    | I64Like
    | F32Like
    | F64Like
    | ByteSequenceLike
    | StructLike
    | SequenceLike
    | DictLike;

export function useTypedFieldEmit<
    T extends FieldType,
    Payload extends TypedDataField<T>
>(
    expectedType: T,
    emit: (event: 'update:modelValue', value: Payload) => void
) {
    return (rawValue: DataField) => {
        if (rawValue.type !== expectedType) {
            warn(
                `[useTypedFieldEmit] Type mismatch! Expected "${expectedType}", got "${rawValue.type}".`,
                rawValue
            );
            return;
        }

        emit('update:modelValue', rawValue as Payload);
    };
}
