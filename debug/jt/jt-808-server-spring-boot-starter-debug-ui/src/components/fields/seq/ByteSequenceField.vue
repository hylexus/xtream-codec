<template>
  <BaseField
      :model-value="modelValue"
      type-label="byte_seq"
      @update:model-value="safeEmit"
  >
    <template #inline-value>
      <span class="hex-preview">
        {{ formatHex(localValue) }}
      </span>
    </template>

    <template #editor-value>
      <el-form-item label="字节序列（Hex）">
        <el-input
            v-model="hexInput"
            placeholder="例如: 12 34 AB CD 或 1234ABCD"
            @blur="parseHexInput"
            @keyup.enter="parseHexInput"
            clearable
        />
        <div class="byte-hint">
          支持空格分隔或连续十六进制，自动忽略非十六进制字符
        </div>
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
import BaseField from '@/components/fields/BaseField.vue';
import {ByteSequence, DataField, useTypedFieldEmit} from '@/types/data-fields';

interface ByteSequenceLike extends DataField {
  type: 'byte_seq';
  value: number[];
}

const props = defineProps<{
  modelValue: ByteSequenceLike;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: ByteSequenceLike): void;
}>();

const safeEmit = useTypedFieldEmit<'byte_seq', ByteSequenceLike>('byte_seq', emit);

const localValue = ref<number[]>([...props.modelValue.value]);

watch(
    () => props.modelValue.value,
    (newVal) => {
      localValue.value = [...newVal];
      updateHexInput(newVal);
    },
    {deep: true}
);

// Hex 输入框内容
const hexInput = ref('');

updateHexInput(props.modelValue.value);

function updateHexInput(bytes: number[]) {
  hexInput.value = bytes.map(b => b.toString(16).padStart(2, '0')).join(' ').toUpperCase();
}

// 解析 Hex 输入
function parseHexInput() {
  const input = hexInput.value || '';
  // 移除非十六进制字符（保留0-9a-fA-F）
  let hexStr = input.replace(/[^0-9a-fA-F]/g, '');

  // if (hexStr.length % 2 !== 0) {
  //   // 补零（如 "123" → "0123"）
  //   hexStr = '0' + hexStr;
  // }

  const bytes: number[] = [];
  for (let i = 0; i < hexStr.length; i += 2) {
    const byteStr = hexStr.slice(i, i + 2);
    if (byteStr.length === 2) {
      const val = parseInt(byteStr, 16);
      if (!isNaN(val)) bytes.push(val);
    }
  }

  localValue.value = bytes;
  emitUpdate(bytes);
}

function emitUpdate(newValue: number[]) {
  const updated = new ByteSequence(newValue, props.modelValue.name, {
    prependLengthFieldType: props.modelValue.prependLengthFieldType,
    ...Object.fromEntries(
        Object.entries(props.modelValue).filter(
            ([k]) => !['type', 'value', 'name', 'prependLengthFieldType'].includes(k)
        )
    ),
  });
  emit('update:modelValue', updated);
}

function formatHex(bytes: number[]): string {
  if (bytes.length === 0) return '(空)';
  const hex = bytes.map(b => b.toString(16).padStart(2, '0').toUpperCase()).join(' ');
  return bytes.length > 8 ? hex.slice(0, 24) + '...' : hex;
}
</script>

<style scoped>
.hex-preview {
  font-family: monospace;
  font-size: 13px;
  color: #409eff;
  margin: 0 10px;
}

.byte-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.byte-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.byte-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
}

.byte-index {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}
</style>
