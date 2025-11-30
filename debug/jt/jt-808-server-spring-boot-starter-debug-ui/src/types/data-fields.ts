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

export enum DicKeyType {
    i8 = "i8",
    u8 = "u8",
    i16 = "i16",
    u16 = "u16",
    i32 = "i32",
    u32 = "u32",
    i64 = "i64"
}

export enum ValueLengthType {
    i8 = "i8",
    u8 = "u8",
    i16 = "i16",
    u16 = "u16",
    i32 = "i32",
    u32 = "u32",
    i64 = "i64"
}

// export enum PrependLengthFieldType {
//     none = "none",
//     u8 = "u8",
//     u16 = "u16",
//     u32 = "u32",
// }
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

export class GbkString implements StringDataField {
    readonly type = "gbk_string" as const;
    readonly charset = "gbk" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: string,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }
}

export class Gb2312String implements StringDataField {
    readonly type = "gb2312_string" as const;
    readonly charset = "gb2312" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: string,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }
}

export class Utf8String implements StringDataField {
    readonly type = "utf8_string" as const;
    readonly charset = "utf-8" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: string,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }
}

export class Bcd8421String implements StringDataField {
    readonly type = "bcd_8421_string" as const;
    readonly charset = "bcd_8421" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: string,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }
}

export class HexString implements StringDataField {
    readonly type = "hex_string" as const;
    readonly charset = "hex" as const;
    prependLengthFieldType: PrependLengthFieldType = 'none';
    name?: string;

    [key: string]: unknown;

    constructor(
        public value: string,
        name?: string,
        prependLengthFieldType?: PrependLengthFieldType,
        props?: Record<string, any>
    ) {
        this.name = name || `${this.type}Field`;
        this.prependLengthFieldType = prependLengthFieldType || 'none';
        if (props) {
            Object.assign(this, props);
        }
    }
}

export class GenericString implements StringDataField {
    readonly type = "string" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: string,
        public charset: string,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.charset = charset;
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }
}

export interface IntegralDataField extends DataField {
    value: number;
}

export interface DictKey extends IntegralDataField {
    type: "i8" | "u8" | "i16" | "u16" | "i32" | "u32" | "i64";
}

export class I8 implements DictKey {
    readonly type = "i8" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.validateValue(value);
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }

    private validateValue(value: number): void {

        if (!Number.isInteger(value)) {
            throw new Error(`I8 value must be an integer, got ${value}`);
        }
        if (value < -128 || value > 127) {
            throw new Error(`I8 value must be between -128 and 127, got ${value}`);
        }
    }
}

export class U8 implements DictKey {
    readonly type = "u8" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.validateValue(value);
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }

    private validateValue(value: number): void {

        if (!Number.isInteger(value)) {
            throw new Error(`I16 value must be an integer, got ${value}`);
        }
        if (value < 0 && value > 255) {
            throw new Error(`I16 value must be between 0 and 255, got ${value}`);
        }
    }

}

export class I16 implements DictKey {
    readonly type = "i16" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.validateValue(value);
        this.name = name || `${this.type}Field`;

        Object.assign(this, props);
    }

    private validateValue(value: number): void {

        if (!Number.isInteger(value)) {
            throw new Error(`I16 value must be an integer, got ${value}`);
        }
        if (value < -32768 || value > 32767) {
            throw new Error(`I16 value must be between -32768 and 32767, got ${value}`);
        }
    }
}

export class U16 implements DictKey {
    readonly type = "u16" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.validateValue(value);
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }

    private validateValue(value: number): void {

        if (!Number.isInteger(value)) {
            throw new Error(`U16 value must be an integer, got ${value}`);
        }
        if (value < 0 || value > 65535) {
            throw new Error(`U16 value must be between 0 and 65535, got ${value}`);
        }
    }
}

export class I32 implements DictKey {
    readonly type = "i32" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.validateValue(value);
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }

    private validateValue(value: number): void {

        if (!Number.isInteger(value)) {
            throw new Error(`I32 value must be an integer, got ${value}`);
        }
        if (value < -2147483648 || value > 2147483647) {
            throw new Error(`I32 value must be between -2147483648 and 2147483647, got ${value}`);
        }
    }
}

export class U32 implements DictKey {
    readonly type = "u32" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.validateValue(value);
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }

    private validateValue(value: number): void {

        if (!Number.isInteger(value)) {
            throw new Error(`U32 value must be an integer, got ${value}`);
        }
        if (value < 0 || value > 4294967295) {
            throw new Error(`U32 value must be between 0 and 4294967295, got ${value}`);
        }
    }
}

export class I64 implements DictKey {
    readonly type = "i64" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }
}

export interface FloatDataField extends DataField {
}

export class F32 implements FloatDataField {
    readonly type = "f32" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.validateValue(value);
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }

    private validateValue(value: number): void {
        if (!isFinite(value)) {
            throw new Error(`F32 value must be a finite number, got ${value}`);
        }
        if (value < -3.402823466e+38 || value > 3.402823466e+38) {
            throw new Error(`F32 value out of range, got ${value}`);
        }
    }
}

export class F64 implements FloatDataField {
    readonly type = "f64" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.validateValue(value);
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }

    private validateValue(value: number): void {
        if (!isFinite(value)) {
            throw new Error(`F64 value must be a finite number, got ${value}`);
        }
    }
}

export class Struct implements DataField {
    readonly type = "struct" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: DataField[],
        name?: string,
        props?: Record<string, any>
    ) {
        this.name = name || `${this.type}Field`;
        if (props) {
            Object.assign(this, props);
        }
    }
}

export class Sequence implements DataField {
    readonly type = "seq" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: DataField[],
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.name = name || `${this.type}Field`;
        Object.assign(this, props);
    }
}

export class ByteSequence implements DataField {
    readonly type = "byte_seq" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';

    [key: string]: unknown;

    constructor(
        public value: number[],
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.name = name || 'byteSeqField';
        Object.assign(this, props);
    }
}

export class Dict implements DataField {
    readonly type = "dict" as const;
    name?: string;
    prependLengthFieldType?: PrependLengthFieldType = 'none';
    keyType: DicKeyType;
    valueLengthType: ValueLengthType;

    [key: string]: unknown;

    constructor(
        public value: Record<number, DataField>,
        keyType: DicKeyType,
        valueLengthType: ValueLengthType,
        name?: string,
        props: Record<string, any> = {}
    ) {
        this.name = name || `${this.type}Field`;
        this.keyType = keyType;
        this.valueLengthType = valueLengthType;
        Object.assign(this, props);
    }
}

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

        // 安全断言：因为已校验 type
        emit('update:modelValue', rawValue as Payload);
    };
}


//
// const gbkString1: DataFieldType = new GbkString("123", "name11111", {a: 111});
// console.log(gbkString1)
// gbkString1.a = 2
// console.log(gbkString1)
// console.log(JSON.stringify(gbkString1))
// console.log(gbkString1.type)
// console.log(new U8(255))
// console.log(new F32(111, 'xx'))
// console.log(new F32(111, undefined))
// console.log(JSON.stringify(new F32(111, undefined, {a: 111, b: 222})))
//
// const genericString = new GenericString("123", "utf-8", "name11111", {a: 111});
// console.log(genericString)
