<script setup lang="ts">

import {ref} from "vue";
import {
  Bcd8421String,
  ByteSequence,
  DataField,
  DicKeyType,
  Dict,
  F32,
  F64,
  FieldType,
  Gb2312String,
  GbkString,
  GenericString,
  HexString,
  I16,
  I32,
  I64,
  I8,
  Sequence,
  Struct,
  U16,
  U32,
  U8,
  Utf8String,
  ValueLengthType,
} from "@/types/data-fields.ts";

import {I8Field, ProtocolEditor,} from "@/components/fields";
import FieldEditor from "@/components/fields/FieldEditor.vue";
import {Close, Plus, Select} from "@element-plus/icons-vue";

const f64Field = ref(new F64(1.1, 'f64_1', {description: '...'}));
const f32Field = ref(new F32(2.1, 'f32_1', {description: '...'}));
const i64Field = ref(new I64(111, 'i64_1', {description: '...'}));
const u32Field = ref(new U32(4294967295, 'u32_1', {description: '...'}));
const i32Field = ref(new I32(2147483647, 'I32_1', {description: '...'}));
const i16Field = ref(new I16(-128, 'I16_1', {description: '...'}));
const i8Field = ref(new I8(-128, 'I8_1', {description: '...'}));
const u8Field = ref(new U8(25, 'U81', {description: '...'}));
const u16Field = ref(new U16(25, 'u16_1', {description: '...'}));
const strField = ref(new GenericString("  字符串 1 ", 'utf-8', 'str1', {description: '...'}));
const utf8StrField = ref(new Utf8String("字符串 2", 'str2', {description: '...'}));
const gbkStrField = ref(new GbkString("字符串 2", 'str2', {description: '...'}));
const gb2312StrField = ref(new Gb2312String("字符串 2", 'str2', {description: '...'}));
const bcd8421StrField = ref(new Bcd8421String("11", 'str3', {description: '...', fixedLength: -6}));
const hexStrField = ref(new HexString("11FF", 'str4', 'u8'));

const struct = ref(new Struct([
  f64Field.value,
  i8Field.value,
  i8Field.value,
  // bcd8421StrField.value,
  {type: 'hex_string', value: 'a1b2', charset: 'hex'},
  {
    type: 'struct',
    value: [
      {type: 'u16', value: 100}
    ],
    name: 'nested'
  }
], 'struct1'));

const seqValue = ref(new Sequence([i8Field.value, f64Field.value, struct.value]));
const bytesSeq = ref(new ByteSequence([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]));
const dictValue = ref(new Dict({1: i8Field.value}, DicKeyType.u8, ValueLengthType.u16))
const allInOne = ref([
  i8Field.value, dictValue.value, seqValue.value, struct.value,
  i16Field.value, i32Field.value, i64Field.value,
  u8Field.value, u16Field.value, u32Field.value,
  f32Field.value,
  f64Field.value,

  bytesSeq.value,
  strField.value, utf8StrField.value, gbkStrField.value, gb2312StrField.value,
  hexStrField.value, bcd8421StrField.value
])
const radio = ref<FieldType | undefined>(undefined);
const field = ref<DataField>({type: 'u8', value: undefined});

</script>

<template>
  <div>
    {{ radio }}
    <I8Field v-model="i8Field"/>
    <br/>

    111

    <!--    todo 优化渲染速度  -->
    <ProtocolEditor v-model="allInOne"/>

    <!--    <U8Field v-model="u8Field"/>-->
    <!--    <I16Field v-model="i16Field"/>-->
    <!--    <U16Field v-model="u16Field"/>-->
    <!--    <I32Field v-model="i32Field"/>-->
    <!--    <U32Field v-model="u32Field"/>-->
    <!--    <I64Field v-model="i64Field"/>-->
    <!--    <F32Field v-model="f32Field"/>-->
    <!--    <F64Field v-model="f64Field"/>-->
    <!--    <StructField v-model="struct"/>-->
    <!--    <SequenceField v-model="seqValue"/>-->
    <!--    <DictField v-model="dictValue"/>-->
    <!--    <ByteSequenceField v-model="bytesSeq"/>-->
    <!--    <HexStringField v-model="hexStrField"/>-->
    <!--    <Bcd8421StringField v-model="bcd8421StrField"/>-->
    <!--    <Gb2312StringField v-model="gb2312StrField"/>-->
    <!--    <GbkStringField v-model="gbkStrField"/>-->
    <!--    <StringField v-model="strField"/>-->
    <!--    <Utf8StringField v-model="utf8StrField"/>-->
    <el-button @click="() => {u8Field.value++}">click</el-button>
  </div>
</template>

<style scoped lang="scss">

</style>
