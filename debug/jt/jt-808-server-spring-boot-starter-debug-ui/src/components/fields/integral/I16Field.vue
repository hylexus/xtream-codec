<template>
  <BaseField
      :model-value="modelValue"
      :type-label="modelValue.type"
      @update:model-value="safeEmit"
  >
    <template #inline-value="{ onUpdate }">
      <el-input-number
          v-model="localValue"
          :min="-32768"
          :max="32767"
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
            :min="-32768"
            :max="32767"
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

interface I16Like extends IntegralDataField {
  type: "i16";
  value: number;
}

const props = defineProps<{ modelValue: I16Like }>();
const emit = defineEmits<{ (e: 'update:modelValue', value: I16Like): void }>();
const safeEmit = useTypedFieldEmit<'i16', I16Like>('i16', emit);
const localValue = ref(props.modelValue.value);

watch(() => props.modelValue.value, (newVal) => {
  localValue.value = newVal;
});
</script>
