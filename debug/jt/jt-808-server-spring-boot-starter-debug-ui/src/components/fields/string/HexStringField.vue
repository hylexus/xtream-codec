<template>
  <BaseField
      :model-value="modelValue"
      :type-label="modelValue.type"
      @update:model-value="safeEmit"
  >
    <template #inline-value>
      <el-input
          v-model="localValue"
          @input="handleInput"
          @blur="handleInput"
          size="small"
          class="inline-input ignore-click"
          placeholder="如: a1b2c3"
      >
        <template #prepend>
          {{ byteCount }} 字节
        </template>
      </el-input>
    </template>

    <template #editor-value>
      <el-form-item label="值">
        <el-input
            v-model="localValue"
            @input="handleInput"
            @blur="handleInput"
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
import {HexStringLike, useTypedFieldEmit} from '@/types/data-fields.ts';


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

function handleInput() {
  let val = localValue.value;

  val = val.replace(/[^0-9a-fA-F]/g, '').toUpperCase();

  if (val !== localValue.value) {
    localValue.value = val;
  }

  // 通知父组件
  onValueChange();
}

// 值变化时 emit 整个对象
const onValueChange = () => {
  if (localValue.value !== props.modelValue.value) {
    emit('update:modelValue', {
      ...props.modelValue,
      value: localValue.value,
    });
  }
};
</script>
