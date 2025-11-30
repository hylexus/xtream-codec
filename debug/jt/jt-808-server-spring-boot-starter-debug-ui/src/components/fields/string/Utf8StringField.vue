<template>
  <BaseField
      :model-value="modelValue"
      :type-label="modelValue.type"
      @update:model-value="safeEmit"
  >
    <template #inline-value="{ onUpdate }">
      <el-input
          v-model="localValue"
          size="small"
          class="inline-input ignore-click"
          @input="onUpdate(localValue)"
          @change="onUpdate(localValue)"
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

interface Utf8StringLike extends StringDataField {
  type: 'utf8_string';
  charset: 'utf-8';
  value: string;
}

const props = defineProps<{
  modelValue: Utf8StringLike;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: Utf8StringLike): void;
}>();
const safeEmit = useTypedFieldEmit<'utf8_string', Utf8StringLike>('utf8_string', emit);

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
