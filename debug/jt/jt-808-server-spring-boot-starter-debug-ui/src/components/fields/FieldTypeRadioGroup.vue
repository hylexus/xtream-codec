<template>
  <div>
    <el-form label-width="auto" size="small" label-position="left">
      <el-form-item label="整数" class="!mb-1">
        <el-radio-group v-model="model" @change="handleChange">
          <el-radio-button label="U8" value="u8"/>
          <el-radio-button label="U16" value="u16"/>
          <el-radio-button label="U32" value="u32"/>
          <el-radio-button label="I8" value="i8"/>
          <el-radio-button label="I16" value="i16"/>
          <el-radio-button label="I32" value="i32"/>
          <el-radio-button label="I64" value="i64"/>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-model="model" label="浮点数" class="!mb-1">
        <el-radio-group v-model="model" @change="handleChange">
          <el-radio-button label="F32" value="f32"/>
          <el-radio-button label="F64" value="f64"/>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="字符串" class="!mb-1">
        <el-radio-group v-model="model" @change="handleChange">
          <el-radio-button label="GBK" value="gbk_string"/>
          <el-radio-button label="UTF8" value="utf8_string"/>
          <el-radio-button label="GB2312" value="gb2312_string"/>
          <el-radio-button label="HEX" value="hex_string"/>
          <el-radio-button label="BCD_8421" value="bcd_8421_string"/>
          <el-radio-button label="自定义编码" value="string"/>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="容器类" style="margin-bottom: 0;">
        <el-radio-group v-model="model" @change="handleChange">
          <el-radio-button label="Struct" value="struct"/>
          <el-radio-button label="Seq" value="seq"/>
          <el-radio-button label="Dict" value="dict"/>
          <el-radio-button label="ByteSeq" value="byte_seq"/>
        </el-radio-group>
      </el-form-item>
    </el-form>

  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue';
import {FIELD_TYPES, FieldType} from '@/types/data-fields.ts';

const props = defineProps<{
  modelValue: FieldType;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', val: FieldType): void;
  (e: 'change', val: FieldType): void;
}>();

const model = computed({
  get: () => props.modelValue,
  set: (val: string) => {
    if (FIELD_TYPES.includes(val as FieldType)) {
      emit('update:modelValue', val as FieldType);
    }
  },
});

const handleChange = (val: string) => {
  emit('change', val as FieldType);
};
</script>

<style scoped lang="scss">

</style>
