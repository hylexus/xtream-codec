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
          @input="onUpdate(localValue)"
          @change="onUpdate(localValue)"
          class="inline-input ignore-click"
      >
        <template #prepend>
          charset: {{ modelValue.charset }}
        </template>
      </el-input>
    </template>

    <template #editor-value="{ onUpdate }">
      <el-form-item label="值">
        <el-input
            v-model="localValue"
            @change="onUpdate(localValue)"
        >
          <template #prepend>
            {{ modelValue.charset }}
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
import {ref, watch} from 'vue';
import BaseField from '../BaseField.vue';
import {StringDataField, useTypedFieldEmit} from "@/types/data-fields.ts";

interface StringLike extends StringDataField {
  type: 'string';
  value: string;
}

const props = defineProps<{
  modelValue: StringLike;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: StringLike): void;
}>();
const safeEmit = useTypedFieldEmit<'string', StringLike>('string', emit);

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
