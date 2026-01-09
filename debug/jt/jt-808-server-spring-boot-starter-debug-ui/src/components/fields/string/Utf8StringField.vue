<template>
  <BaseField
      :model-value="modelValue"
      :type-label="modelValue.type"
      @update:model-value="safeEmit"
  >
    <template #inline-value>
      <el-input
          v-model="localValue"
          size="small"
          class="inline-input ignore-click"
          @input="onValueChange"
          @change="onValueChange"
      />
    </template>

    <template #editor-value>
      <el-form-item label="值">
        <el-input
            v-model="localValue"
            @input="onValueChange"
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
import {useTypedFieldEmit, Utf8StringLike} from "@/types/data-fields.ts";


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
