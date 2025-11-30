<template>
  <BaseField
      :model-value="modelValue"
      :type-label="`${modelValue.type} / BYTE`"
      @update:model-value="safeEmit"
  >
    <template #inline-value="{ onUpdate }">
      <el-input-number
          v-model="localValue"
          :min="0"
          :max="255"
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
            :min="0"
            :max="255"
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
import {IntegralDataField, useTypedFieldEmit} from "@/types/data-fields.ts";

interface U8Like extends IntegralDataField {
  type: "u8";
  value: number;
}

const props = defineProps<{ modelValue: U8Like }>();
const emit = defineEmits<{ (e: 'update:modelValue', value: U8Like): void }>();
const safeEmit = useTypedFieldEmit<'u8', U8Like>('u8', emit);
const localValue = ref(props.modelValue.value);

watch(() => props.modelValue.value, (newVal) => {
  localValue.value = newVal;
});
</script>
