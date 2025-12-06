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
          :maxlength="isFixed ? maxDigits : undefined"
          :placeholder="placeholder"
      >
        <template #prepend>
          BCD[{{ byteCount }}]
        </template>
        <template #prefix v-if="props.showPrefix">
          <span>{{ prefix }}</span>
        </template>
      </el-input>
    </template>

    <template #editor-value>
      <el-form-item label="值">
        <el-input
            v-model="localValue"
            @input="handleInput"
            @blur="handleInput"
            :maxlength="isFixed ? maxDigits : undefined"
            :placeholder="placeholder"
        >
          <template #prepend>
            BCD [ {{ byteCount }} ]
          </template>
          <template #prefix v-if="props.showPrefix">
            <span>{{ prefix }}</span>
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
import {Bcd8421StringLike, useTypedFieldEmit} from '@/types/data-fields.ts';


const props = withDefaults(defineProps<{
  modelValue: Bcd8421StringLike;
  showPrefix?: boolean;
}>(), {
  showPrefix: true
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: Bcd8421StringLike): void;
}>();

const safeEmit = useTypedFieldEmit<'bcd_8421_string', Bcd8421StringLike>('bcd_8421_string', emit);

const isFixed = computed(() => (props.modelValue.fixedLength ?? 0) > 0);
const maxDigits = computed(() => isFixed.value ? props.modelValue.fixedLength! * 2 : Infinity);

const placeholder = computed(() =>
    isFixed.value ? `最多 ${maxDigits.value} 位数字` : '请输入数字'
);

const localValue = ref(props.modelValue.value);

// 字节数（用于显示 BCD[N]）
const byteCount = computed(() => {
  if (isFixed.value) return props.modelValue.fixedLength!;
  return Math.ceil((localValue.value.length || 0) / 2);
});

const prefix = computed(() => {
  if (!isFixed.value) {
    return '';
  }
  const diff = props.modelValue.fixedLength! * 2 - Math.ceil((localValue.value.length || 0))
  if (diff <= 0) {
    return '';
  }
  return ''.padStart(diff, '0');
});


// 同步外部变化
watch(
    () => props.modelValue.value,
    (newVal) => {
      // 只同步干净的数字（防止外部传非法值）
      const clean = newVal.replace(/\D/g, '');
      if (clean !== localValue.value) {
        localValue.value = clean;
      }
    },
    {immediate: true}
);

const handleInput = () => {
  let val = localValue.value;

  val = val.replace(/\D/g, '');

  if (isFixed.value && val.length > maxDigits.value) {
    val = val.slice(-maxDigits.value); // 保留最后 N 位
  }

  if (val !== localValue.value) {
    localValue.value = val;
  }

  onValueChange();
};


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
