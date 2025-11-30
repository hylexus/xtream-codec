<template>
  <div>
    <el-select
        v-model="model"
        placeholder="选择字段类型"
        style="min-width: 150px"
        size="small"
        @change="handleChange">
      <template #label="{label, value}">
        <el-tag
            style="width: 120px;"
            :style="{
              backgroundColor: getTypeColor(value),
              color: 'white',
              border: 'none',
              borderRadius: '5px'
          }"
        >
          {{ label }}
        </el-tag>
      </template>
      <el-option-group
          v-for="group in fileTypeGroupOptions"
          :key="group.label"
          :label="group.label"
      >
        <el-option
            v-for="item in group.options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        >
          <template #default>
            <el-tag
                style="width: 120px;"
                :style="{
                  backgroundColor: getTypeColor(item.value),
                  color: 'white',
                  border: 'none',
                  borderRadius: '5px'
              }"
            >
              {{ item.label }}
            </el-tag>
          </template>
        </el-option>
      </el-option-group>
    </el-select>
  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue';
import {FIELD_TYPES, FieldType} from '@/types/data-fields';
import {fileTypeGroupOptions} from "@/components/fields/index.ts";
import {getTypeColor} from "@/utils/field-utils.ts";

const props = defineProps<{
  modelValue: FieldType | undefined;
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
