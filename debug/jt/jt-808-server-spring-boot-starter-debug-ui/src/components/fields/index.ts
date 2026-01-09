import {defineAsyncComponent} from 'vue';
import {FieldType} from "@/types/data-fields.ts";

export {default as StringField} from './string/StringField.vue';
export {default as Utf8StringField} from './string/Utf8StringField.vue';
export {default as GbkStringField} from './string/GbkStringField.vue';
export {default as Gb2312StringField} from './string/Gb2312StringField.vue';
export {default as Bcd8421StringField} from './string/Bcd8421StringField.vue';
export {default as HexStringField} from './string/HexStringField.vue';

export {default as U8Field} from './integral/U8Field.vue';
export {default as I8Field} from './integral/I8Field.vue';
export {default as U16Field} from './integral/U16Field.vue';
export {default as I16Field} from './integral/I16Field.vue';
export {default as I32Field} from './integral/I32Field.vue';
export {default as U32Field} from './integral/U32Field.vue';
export {default as I64Field} from './integral/I64Field.vue';

export {default as F32Field} from './float/F32Field.vue';
export {default as F64Field} from './float/F64Field.vue';

export {default as StructField} from './struct/StructField.vue';

export {default as SequenceField} from './seq/SequenceField.vue';
export {default as ByteSequenceField} from './seq/ByteSequenceField.vue';
export {default as DictField} from './dict/DictField.vue';
export {default as ProtocolEditor} from './ProtocolEditor.vue';

export const FIELD_COMPONENT_MAP: Record<FieldType, any> = {
    i8: defineAsyncComponent(() => import('./integral/I8Field.vue')),
    u8: defineAsyncComponent(() => import('./integral/U8Field.vue')),
    i16: defineAsyncComponent(() => import('./integral/I16Field.vue')),
    u16: defineAsyncComponent(() => import('./integral/U16Field.vue')),
    i32: defineAsyncComponent(() => import('./integral/I32Field.vue')),
    u32: defineAsyncComponent(() => import('./integral/U32Field.vue')),
    i64: defineAsyncComponent(() => import('./integral/I64Field.vue')),
    f32: defineAsyncComponent(() => import('./float/F32Field.vue')),
    f64: defineAsyncComponent(() => import('./float/F64Field.vue')),
    string: defineAsyncComponent(() => import('./string/StringField.vue')),
    utf8_string: defineAsyncComponent(() => import('./string/Utf8StringField.vue')),
    gbk_string: defineAsyncComponent(() => import('./string/GbkStringField.vue')),
    gb2312_string: defineAsyncComponent(() => import('./string/Gb2312StringField.vue')),
    bcd_8421_string: defineAsyncComponent(() => import('./string/Bcd8421StringField.vue')),
    hex_string: defineAsyncComponent(() => import('./string/HexStringField.vue')),
    // 递归支持
    seq: defineAsyncComponent(() => import('./seq/SequenceField.vue')),
    byte_seq: defineAsyncComponent(() => import('./seq/ByteSequenceField.vue')),
    struct: defineAsyncComponent(() => import('./struct/StructField.vue')),
    dict: defineAsyncComponent(() => import('./dict/DictField.vue')),
} as const;

export const availableTypes = Object.keys(FIELD_COMPONENT_MAP) as FieldType[];
export const fileTypeGroupOptions: {
    label: string,
    options: {
        label: string,
        value: FieldType,
    }[]
}[] = [
    {
        label: '整数',
        options: [
            {value: 'u8', label: 'u8 / BYTE',},
            {value: 'u16', label: 'u16 / WORD',},
            {value: 'u32', label: 'u32 / DWORD',},
            {value: 'i8', label: 'i8',},
            {value: 'i16', label: 'i16',},
            {value: 'i32', label: 'i32',},
            {value: 'i64', label: 'i64',},
        ],
    },
    {
        label: '浮点数',
        options: [
            {value: 'f32', label: 'f32 / Float',},
            {value: 'f64', label: 'f64 / Double',},
        ],
    },
    {
        label: '字符串',
        options: [
            {value: 'string', label: '自定义编码',},
            {value: 'utf8_string', label: 'utf8_string',},
            {value: 'gbk_string', label: 'gbk_string',},
            {value: 'gb2312_string', label: 'gb2312_string',},
            {value: 'bcd_8421_string', label: 'bcd_8421_string',},
            {value: 'hex_string', label: 'hex_string',},
        ]
    },
    {
        label: '容器类',
        options: [
            {value: 'seq', label: 'seq',},
            {value: 'seq', label: 'seq',},
            {value: 'struct', label: 'struct',},
            {value: 'dict', label: 'dict',},
        ]
    }
]
