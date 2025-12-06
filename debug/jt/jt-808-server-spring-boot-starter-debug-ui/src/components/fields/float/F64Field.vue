<script setup lang="ts">
import BaseField from '../BaseField.vue';
import {ref, watch} from 'vue';
import {F64Like, useTypedFieldEmit} from '@/types/data-fields.ts';


const props = defineProps<{ modelValue: F64Like }>();
const emit = defineEmits<{ (e: 'update:modelValue', value: F64Like): void }>();
const safeEmit = useTypedFieldEmit<'f64', F64Like>('f64', emit);

const localValue = ref(props.modelValue.value);

watch(
    () => props.modelValue.value,
    (newVal) => {
      if (newVal !== localValue.value) {
        localValue.value = newVal;
      }
    }
);

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

<template>
  <BaseField
      :model-value="modelValue"
      :type-label="modelValue.type"
      @update:model-value="safeEmit"
  >
    <template #inline-value>
      <el-input-number
          v-model="localValue"
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
