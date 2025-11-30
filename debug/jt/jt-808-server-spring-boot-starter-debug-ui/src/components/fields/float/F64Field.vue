<template>
  <BaseField
      :model-value="modelValue"
      :type-label="modelValue.type"
      @update:model-value="safeEmit"
  >
    <template #inline-value="{ onUpdate }">
      <el-input-number
          v-model="localValue"
          controls-position="right"
          size="small"
          class="inline-input ignore-click"
          @input="onUpdate(localValue)"
          @change="onUpdate(localValue)"
      />
    </template>

    <template #editor-value="{ onUpdate }">
      <el-form-item label="Value">
        <el-input-number
            v-model="localValue"
            controls-position="right"
            @change="onUpdate"
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
import {FloatDataField, useTypedFieldEmit} from "@/types/data-fields.ts";

interface F64Like extends FloatDataField {
  type: "f64";
  value: number;
}

const props = defineProps<{ modelValue: F64Like }>();
const emit = defineEmits<{ (e: 'update:modelValue', value: F64Like): void }>();
const safeEmit = useTypedFieldEmit<'f64', F64Like>('f64', emit);
const localValue = ref(props.modelValue.value);

watch(() => props.modelValue.value, (newVal) => {
  localValue.value = newVal;
});
</script>
