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
          @input="onValueChange"
          @change="onValueChange"
          class="inline-input ignore-click"
      >
        <template #prepend>
          charset: {{ modelValue.charset }}
        </template>
      </el-input>
    </template>

    <template #editor-value>
      <el-form-item label="编码">
        <template #label>
          <div class="form-item-label-with-icon">
            编码
            <el-tooltip content="字符集: gbk, utf-8, ..." placement="top">
              <el-icon>
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-input
            v-model="localCharset"
            @input="onValueChange"
            @change="onValueChange"
        />
      </el-form-item>
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
import {StringLike, useTypedFieldEmit} from "@/types/data-fields.ts";
import {InfoFilled} from "@element-plus/icons-vue";


const props = defineProps<{
  modelValue: StringLike;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: StringLike): void;
}>();
const safeEmit = useTypedFieldEmit<'string', StringLike>('string', emit);

const localValue = ref(props.modelValue.value);
const localCharset = ref(props.modelValue.charset ?? 'utf-8');

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
  if (localValue.value !== props.modelValue.value || localCharset.value !== props.modelValue.charset) {
    emit('update:modelValue', {
      ...props.modelValue,
      value: localValue.value,
      charset: localCharset.value,
    });
  }
};

if (!props.modelValue.charset) {
  localCharset.value = 'utf-8';
  onValueChange();
}
</script>
