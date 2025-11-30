<template>
  <BaseField
      :model-value="modelValue"
      :type-label="modelValue.type"
      @update:model-value="safeEmit"
  >
    <template #inline-value="{ onUpdate }">
      <el-input
          v-model="localValue"
          @input="onUpdate(localValue)"
          @change="onUpdate(localValue)"
          size="small"
          class="inline-input ignore-click"
      />
    </template>

    <template #editor-value="{ onUpdate }">
      <el-form-item label="值">
        <el-input
            v-model="localValue"
            @change="onUpdate(localValue)"
        />
      </el-form-item>
    </template>

    <!-- 透传 actions 插槽 -->
    <template #actions>
      <slot name="actions"/>
    </template>

  </BaseField>
</template>

<script setup lang="ts">
import {ref, watch} from 'vue';
import BaseField from '../BaseField.vue';
import {StringDataField, useTypedFieldEmit} from "@/types/data-fields.ts";

interface Gb2312StringLike extends StringDataField {
  type: 'gb2312_string';
  charset: 'gb2312';
  value: string;
}

const props = defineProps<{
  modelValue: Gb2312StringLike;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: Gb2312StringLike): void;
}>();
const safeEmit = useTypedFieldEmit<'gb2312_string', Gb2312StringLike>('gb2312_string', emit);

const localValue = ref(props.modelValue.value);

watch(
    () => props.modelValue.value,
    (newVal) => {
      if (newVal !== localValue.value) {
        localValue.value = newVal;
      }
    }
);
</script>
