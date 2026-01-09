import {ConcreteDataField, FieldType} from "@/types/data-fields.ts";

export const generateFieldId = () => 'field_' + Math.random().toString(36).slice(2) + Date.now()

export const createDefaultField = (type: FieldType, name?: string, charset?: string): ConcreteDataField => {
    return {
        id: generateFieldId(),
        name: name || undefined,
        type,
        value: getDefaultFieldValue(type),
        charset,
    } as ConcreteDataField;
}

export const getDefaultFieldValue = (type: FieldType): any => {
    switch (type) {
        case 'i8':
            return 0;
        case 'u8':
            return 0;
        case 'i16':
            return 0;
        case 'u16':
            return 0;
        case 'i32':
            return 0;
        case 'u32':
            return 0;
        case 'i64':
            return 0;
        case 'f32':
            return 0.0;
        case 'f64':
            return 0.0;
        case 'string':
        case 'utf8_string':
        case 'gbk_string':
        case 'gb2312_string':
        case 'hex_string':
        case 'bcd_8421_string':
            return '';
        case 'byte_seq':
            return [];
        case 'struct':
            return [];
        case 'seq':
            return [];
        case 'dict':
            return {};
        default:
            return null;
    }
}

export const fieldTypeColorMap: Record<FieldType, string> = {
    'i8': '#13c2c2',
    'u8': '#13c2c2',
    'i16': '#13c2c2',
    'u16': '#13c2c2',
    'i32': '#13c2c2',
    'u32': '#13c2c2',
    'i64': '#13c2c2',
    'f32': '#722ed1',
    'f64': '#722ed1',
    'string': '#52c41b',
    'utf8_string': '#52c41b',
    'gbk_string': '#52c41b',
    'gb2312_string': '#52c41b',
    'hex_string': '#f56a0a',
    'bcd_8421_string': '#f56a0a',
    'byte_seq': '#f56a0a',
    'struct': '#95d65f',
    'seq': '#95d65f',
    'dict': '#95d65f',
};

export const getTypeColor = (type: FieldType) => {
    return fieldTypeColorMap[type] || '#1890ff'
}
