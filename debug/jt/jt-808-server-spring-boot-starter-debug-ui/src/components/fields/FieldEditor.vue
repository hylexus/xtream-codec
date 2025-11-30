<template>
  <div>
    <el-form size="small" label-width="auto" label-position="right">
      <el-form-item label="前置长度字段">
        <el-radio-group v-model="model.prependLengthFieldType">
          <el-radio-button label="none" value="none"/>
          <el-radio-button label="u8" value="u8"/>
          <el-radio-button label="u16" value="u16"/>
          <el-radio-button label="u32" value="u32"/>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="字段名称">
        <el-input v-model="model.name" placeholder="字段名称" style="width: 150px"/>
      </el-form-item>
      <el-form-item label="类型">
        <FieldTypeRadioGroup v-model="model.type"/>
      </el-form-item>
      <el-form-item v-if="model.type ==='string'" label="编码">
        <el-input v-model="model.charset" placeholder="uft-8" style="width: 150px"/>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import {DataField} from '@/types/data-fields';
import FieldTypeRadioGroup from "@/components/fields/FieldTypeRadioGroup.vue";

const model = defineModel<DataField>({
  required: true,
  default: {prependLengthFieldType: 'none', type: 'u8'}
})

{
  if (!model.value.prependLengthFieldType) {
    model.value.prependLengthFieldType = 'none'
  }
  if (model.value.type === 'string' && !model.value.charset) {
    model.value.charset = 'utf8'
  }
}
</script>

<style scoped lang="scss">
</style>
