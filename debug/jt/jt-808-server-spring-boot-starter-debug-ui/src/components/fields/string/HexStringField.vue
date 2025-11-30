<template>
  <BaseField
      :model-value="modelValue"
      :type-label="modelValue.type"
      @update:model-value="safeEmit"
  >
    <template #inline-value="{ onUpdate }">
      <el-input
          v-model="localValue"
          @input="handleInput(onUpdate)"
          @blur="handleBlur(onUpdate)"
          size="small"
          class="inline-input ignore-click"
          placeholder="如: a1b2c3"
      >
        <template #prepend>
          {{ byteCount }} 字节
        </template>
      </el-input>
    </template>

    <template #editor-value="{ onUpdate }">
      <el-form-item label="值">
        <el-input
            v-model="localValue"
            @input="handleInput(onUpdate)"
            @blur="handleBlur(onUpdate)"
            placeholder="请输入十六进制字符串（0-9, a-f）"
        >
          <template #append>
            {{ byteCount }} 字节
          </template>
        </el-input>
      </el-form-item>
    </template>

    <!-- 透传 actions 插槽 -->
    <template #actions>
      <slot name="actions"/>
    </template>

  </BaseField>
</template>

<script setup lang="ts">
import {computed, ref, watch} from 'vue';
import BaseField from '../BaseField.vue';
import {StringDataField, useTypedFieldEmit} from '@/types/data-fields.ts';

interface HexStringLike extends StringDataField {
  type: 'hex_string';
  charset: 'hex';
  value: string;
}

const props = defineProps<{
  modelValue: HexStringLike;
}>();
const byteCount = computed(() => {
  return props.modelValue.value?.length || 0;
})
const emit = defineEmits<{
  (e: 'update:modelValue', value: HexStringLike): void;
}>();
const safeEmit = useTypedFieldEmit<'hex_string', HexStringLike>('hex_string', emit);

const localValue = ref(props.modelValue.value);

watch(
    () => props.modelValue.value,
    (newVal) => {
      const clean = newVal.replace(/[^0-9a-fA-F]/g, '').toUpperCase();
      if (clean !== localValue.value) {
        localValue.value = clean;
      }
    },
    {immediate: true}
);

function handleInput(onUpdate: (value: string) => void) {
  let val = localValue.value;

  val = val.replace(/[^0-9a-fA-F]/g, '').toUpperCase();

  if (val !== localValue.value) {
    localValue.value = val;
  }

  // 通知父组件
  onUpdate(val);
}

function handleBlur(onUpdate: (value: string) => void) {
  handleInput(onUpdate);
}
</script>
