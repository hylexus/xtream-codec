<template>
  <BaseField
      :model-value="modelValue"
      :type-label="`${modelValue.type} / BYTE`"
      @update:model-value="safeEmit"
  >
    <template #inline-value>
      <el-input-number
          v-model="localValue"
          :min="0"
          :max="255"
          controls-position="right"
          size="small"
          class="inline-input ignore-click"
          @change="onValueChange"
      />
    </template>

    <template #editor-value>
      <el-form-item label="Value">
        <el-input-number
            v-model="localValue"
            :min="0"
            :max="255"
            controls-position="right"
            @change="onValueChange"
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
import {U8Like, useTypedFieldEmit} from "@/types/data-fields.ts";


const props = defineProps<{ modelValue: U8Like }>();
const emit = defineEmits<{ (e: 'update:modelValue', value: U8Like): void }>();
const safeEmit = useTypedFieldEmit<'u8', U8Like>('u8', emit);
const localValue = ref<number>(props.modelValue.value);

watch(() => props.modelValue.value, (newVal) => {
  if (newVal !== localValue.value) {
    localValue.value = newVal;
  }
});

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
