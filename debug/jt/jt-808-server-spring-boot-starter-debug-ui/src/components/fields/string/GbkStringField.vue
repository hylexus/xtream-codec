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

interface GbkStringLike extends StringDataField {
  type: 'gbk_string';
  charset: 'gbk';
  value: string;
}

const props = defineProps<{
  modelValue: GbkStringLike;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: GbkStringLike): void;
}>();
const safeEmit = useTypedFieldEmit<'gbk_string', GbkStringLike>('gbk_string', emit);

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
