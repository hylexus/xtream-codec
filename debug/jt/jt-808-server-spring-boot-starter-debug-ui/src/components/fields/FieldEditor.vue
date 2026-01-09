<template>
  <div>
    <el-form size="small" label-width="auto" label-position="right">
      <el-form-item label="前置字段">
        <template #label>
          <div class="form-item-label-with-icon">
            前置字段
            <el-tooltip content="前置长度字段: 编码时，前置一个用来表示当前字段长度的字段" placement="top">
              <el-icon>
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-radio-group v-model="model.prependLengthFieldType">
          <el-radio-button label="none" value="none"/>
          <el-radio-button label="u8" value="u8"/>
          <el-radio-button label="u16" value="u16"/>
          <el-radio-button label="u32" value="u32"/>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="字段名称">
        <template #label>
          <div class="form-item-label-with-icon">
            字段名称
            <el-tooltip content="字段名称: 仅仅调试用到(随便写，可重复，也可空)" placement="top">
              <el-icon>
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-input v-model="model.name" placeholder="字段名称" style="width: 150px"/>
      </el-form-item>
      <el-form-item label="字段类型">
        <template #label>
          <div class="form-item-label-with-icon">
            字段类型
            <el-tooltip content="每种类型都代表一种编解码的单元" placement="top">
              <el-icon>
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <FieldTypeRadioGroup v-model="model.type"/>
      </el-form-item>
      <el-form-item v-if="model.type ==='string'" label="编码">
        <template #label>
          <div class="form-item-label-with-icon">
            编码
            <el-tooltip content="字符集: gbk, utf-8, ..." placement="top">
              <el-icon>
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-input v-model="model.charset" placeholder="uft-8" style="width: 150px"/>
      </el-form-item>
      <el-form-item v-if="model.type ==='dict'" label="Key类型">
        <template #label>
          <div class="form-item-label-with-icon">
            键类型
            <el-tooltip content="字典中 Key 的类型" placement="top">
              <el-icon>
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-radio-group v-model="model.keyType">
          <el-radio-button label="u8"/>
          <el-radio-button label="u16"/>
          <el-radio-button label="u32"/>
          <el-radio-button label="i8"/>
          <el-radio-button label="i16"/>
          <el-radio-button label="i32"/>
          <el-radio-button label="i64"/>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="model.type === 'dict'" label="Value长度类型">
        <template #label>
          <div class="form-item-label-with-icon">
            值长度
            <el-tooltip content="描述字典值长度的数据类型" placement="top">
              <el-icon>
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-radio-group v-model="model.valueLengthType">
          <el-radio-button label="u8"/>
          <el-radio-button label="u16"/>
          <el-radio-button label="u32"/>
          <el-radio-button label="i8"/>
          <el-radio-button label="i16"/>
          <el-radio-button label="i32"/>
          <el-radio-button label="i64"/>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import {ConcreteDataField} from '@/types/data-fields';
import FieldTypeRadioGroup from "@/components/fields/FieldTypeRadioGroup.vue";
import {InfoFilled} from "@element-plus/icons-vue";

const model = defineModel<ConcreteDataField>({
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

  if (model.value.type === 'dict') {
    if (!model.value.keyType) {
      model.value.keyType = 'u8'
    }
    if (!model.value.valueLengthType) {
      model.value.valueLengthType = 'u8'
    }
  }
}
</script>

<style scoped lang="scss">
</style>
