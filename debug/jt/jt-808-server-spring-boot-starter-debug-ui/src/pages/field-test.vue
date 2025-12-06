<script setup lang="ts">

import {ref} from "vue";
import {DataField, U8Like,} from "@/types/data-fields.ts";

import {ProtocolEditor, U8Field,} from "@/components/fields";


const allInOne = ref<DataField[]>([
  {
    type: 'dict',
    name: 'dict1',
    keyType: 'u16',
    valueLengthType: 'u8',
    value: {
      0: {type: 'f64', value: 64.0},
      1: {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
      2: {
        type: 'seq', value: [
          {type: 'f64', value: 64.0},
          {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
          {
            type: 'seq', value: [
              {type: 'f64', value: 64.0},
              {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
              {type: 'string', value: "普通字符串", prependLengthFieldType: 'u32'},
            ]
          },
          {type: 'string', value: "普通字符串", prependLengthFieldType: 'u32'},
        ]
      },
      3: {
        type: 'dict', keyType: 'u16', valueLengthType: 'u8', value: {
          0: {type: 'f64', value: 64.0},
          1: {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
        }
      }
    }
  },
  {
    type: 'struct', value: [
      {type: 'f64', value: 64.0},
      {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
      {
        type: 'seq', value: [
          {type: 'f64', value: 64.0},
          {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
          {
            type: 'seq', value: [
              {type: 'f64', value: 64.0},
              {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
              {type: 'string', value: "普通字符串", prependLengthFieldType: 'u32'},
            ]
          },
          {type: 'string', value: "普通字符串", prependLengthFieldType: 'u32'},
        ]
      }
    ]
  },
  {
    type: 'seq', value: [
      {type: 'f64', value: 64.0},
      {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
      {
        type: 'seq', value: [
          {type: 'f64', value: 64.0},
          {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
          {type: 'string', value: "普通字符串", prependLengthFieldType: 'u32'},
        ]
      },
      {type: 'string', value: "普通字符串", prependLengthFieldType: 'u32'},
    ]
  },
  {type: 'bcd_8421_string', value: "13919192222", prependLengthFieldType: 'u32'},
  {type: 'hex_string', value: "112233", prependLengthFieldType: 'u32'},
  {type: 'string', value: "普通字符串", prependLengthFieldType: 'u32'},
  {type: 'gb2312_string', value: "GB2312字符串", prependLengthFieldType: 'u32'},
  {type: 'utf8_string', value: "UTF-8字符串"},
  {type: 'gbk_string', value: "GBK字符串"},
  {type: 'byte_seq', value: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]},
  {type: 'f64', value: 64.0},
  {type: 'f32', value: 32.0},
  {type: 'i64', value: -64},
  {type: 'i32', value: -32},
  {type: 'u32', value: 32},
  {type: 'i16', value: -16},
  {type: 'u16', value: 16},
  {type: 'i8', value: -1},
  {type: 'u8', value: 8},
])
const u8Value = ref<U8Like>({type: 'u8', value: 8})
</script>

<template>
  <div>

    {{ u8Value }}
    <U8Field v-model="u8Value"/>
    <hr/>
    <div style="display: flex;">
      <ProtocolEditor v-model="allInOne" :show-title="false" title="标题"/>
      <div>
        <pre>{{ allInOne }}</pre>
      </div>
    </div>

  </div>
</template>

<style scoped lang="scss">

</style>
