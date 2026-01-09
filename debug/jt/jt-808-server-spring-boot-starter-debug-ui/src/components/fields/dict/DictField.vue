<template>
  <div>
    <BaseField
        :model-value="modelValue"
        type-label="dict"
        @update:model-value="safeEmit"
    >
      <template #inline-value>
        <el-tag type="warning" size="small">
          {{ numericEntries.length }} 个键值
        </el-tag>
        <el-tooltip effect="dark" content="添加键值对" placement="top">
          <el-button
              color="#626aef"
              size="small"
              class="ml-2"
              :icon="Plus"
              circle
              @click="(e:any)=> openPopover(null, 'add', '添加键值对', e)"
              @click.stop
          />
        </el-tooltip>
      </template>

      <template #editor-value>
        <el-form-item label="Key类型">
          <template #label>
            <div class="form-item-label-with-icon">
              键类型
              <el-tooltip content="字典中 Key 的类型" placement="top">
                <el-icon>
                  <InfoFilled/>
                </el-icon>
              </el-tooltip>
            </div>
          </template>
          <el-radio-group v-model="internalKeyType" @change="onKeyTypeChange">
            <el-radio-button label="u8"/>
            <el-radio-button label="u16"/>
            <el-radio-button label="u32"/>
            <el-radio-button label="i8"/>
            <el-radio-button label="i16"/>
            <el-radio-button label="i32"/>
            <el-radio-button label="i64"/>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="Value长度类型">
          <template #label>
            <div class="form-item-label-with-icon">
              值长度
              <el-tooltip content="描述字典值长度的数据类型" placement="top">
                <el-icon>
                  <InfoFilled/>
                </el-icon>
              </el-tooltip>
            </div>
          </template>
          <el-radio-group v-model="internalValueLengthType" @change="onValueLengthTypeChange">
            <el-radio-button label="u8"/>
            <el-radio-button label="u16"/>
            <el-radio-button label="u32"/>
            <el-radio-button label="i8"/>
            <el-radio-button label="i16"/>
            <el-radio-button label="i32"/>
            <el-radio-button label="i64"/>
          </el-radio-group>
        </el-form-item>

        <el-form-item class="dict-editor" label="键值对">
          <template #label>
            <div class="form-item-label-with-icon">
              键值对
              <el-tooltip content="字典数据条目" placement="top">
                <el-icon>
                  <InfoFilled/>
                </el-icon>
              </el-tooltip>
            </div>
          </template>
          <el-tooltip v-if="!numericEntries || numericEntries.length == 0" effect="dark" content="添加键值对"
                      placement="top">
            <el-button
                color="#626aef"
                size="small"
                class="ml-2"
                :icon="Plus"
                circle
                @click="(e:any)=> openPopover(null, 'add', '添加键值对', e)"
                @click.stop
            />
          </el-tooltip>
          <div v-else class="dict-editor" style="width: 100%;">
            <div class="field-list">
              <div
                  v-for="[keyNum, field] in numericEntries"
                  :key="keyNum"
                  class="field-item"
              >
                <div class="key-input">
                  <el-input-number
                      :model-value="keyNum"
                      :min="getKeyMin()"
                      :max="getKeyMax()"
                      size="small"
                      :controls="false"
                      @update:model-value="(newVal: number) => onKeyChange(keyNum, newVal)"
                      style="width: 120px; height: 36px;"
                  >
                    <template #prefix>{{ internalKeyType }}</template>
                    <template #suffix>{{ toHexString(keyNum) }}</template>
                  </el-input-number>
                </div>

                <div style="flex: 1; min-width: 0">
                  <component
                      :is="getComponentForType(field.type)"
                      :model-value="field"
                      @update:model-value="(updated: ConcreteDataField) => onValueUpdate(keyNum, updated)"
                  >
                    <template #actions>
                      <el-tooltip effect="dark" content="编辑" placement="top">
                        <el-button
                            size="small"
                            type="default"
                            :icon="Edit"
                            circle
                            @click="(e:any) => openPopover(keyNum, 'edit', '编辑字段', e)"
                        />
                      </el-tooltip>
                      <el-tooltip effect="dark" content="添加同级字段" placement="top">
                        <el-button
                            size="small"
                            type="primary"
                            :icon="Plus"
                            circle
                            @click="(e :any)=> openPopover(keyNum, 'add', '添加同级字段', e)"
                        />
                      </el-tooltip>
                      <el-tooltip effect="dark" content="删除" placement="top">
                        <el-button
                            size="small"
                            type="danger"
                            :icon="Delete"
                            circle
                            @click="() => removeEntry(keyNum)"
                        />
                      </el-tooltip>
                    </template>
                  </component>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
      </template>

      <template #actions>
        <slot name="actions"/>
      </template>
    </BaseField>

    <FieldEditorPopover
        v-model="popoverProps"
        @on-confirm="onPopoverConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, ref, watch} from 'vue';
import BaseField from '@/components/fields/BaseField.vue';
import {
  ConcreteDataField,
  DicKeyType,
  DictLike,
  FieldType,
  useTypedFieldEmit,
  ValueLengthType,
} from '@/types/data-fields';
import {FIELD_COMPONENT_MAP} from '@/components/fields';
import {createDefaultField, generateFieldId, getDefaultFieldValue} from '@/utils/field-utils';
import {Delete, Edit, InfoFilled, Plus} from '@element-plus/icons-vue';
import FieldEditorPopover, {
  FieldPopoverActionType,
  FieldPopoverProps,
} from '@/components/fields/FieldEditorPopover.vue';
import {toHexString} from "@/utils/codec-utils.ts";


const props = defineProps<{
  modelValue: DictLike; // value: Record<number, ConcreteDataField>
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: DictLike): void;
}>();

const safeEmit = useTypedFieldEmit<'dict', DictLike>('dict', emit);

// 内部状态
const internalKeyType = ref<DicKeyType>(props.modelValue.keyType);
const internalValueLengthType = ref<ValueLengthType>(props.modelValue.valueLengthType);

watch(
    () => props.modelValue,
    (newVal) => {
      internalKeyType.value = newVal.keyType;
      internalValueLengthType.value = newVal.valueLengthType;
    },
    {deep: true}
);

// 将 Record<number, T> 转为 [number, T][] 用于安全遍历
const numericEntries = computed<[number, ConcreteDataField][]>(() => {
  return Object.entries(props.modelValue.value).map(([k, v]) => [
    Number(k),
    v,
  ]);
});

const getComponentForType = (type: FieldType) => {
  return FIELD_COMPONENT_MAP[type] || 'div';
};

const getKeyMin = (): number => {
  return 0;
};

const getKeyMax = (): number => {
  switch (internalKeyType.value) {
    case 'u8':
      return 255;
    case 'u16':
      return 65535;
    case 'u32':
      return 4294967295;
    default:
      return 255;
  }
};

// 更新 key
const onKeyChange = (oldKey: number, newKeyNum: number | null) => {
  if (newKeyNum === null || isNaN(newKeyNum)) return;

  const currentValue = props.modelValue.value;

  // 检查冲突（用 number key）
  if (newKeyNum !== oldKey && currentValue[newKeyNum] !== undefined) {
    return;
  }

  // 构造新对象：先删旧 key，再加新 key
  const {[oldKey]: fieldToMove, ...rest} = currentValue;
  const newValue = {...rest, [newKeyNum]: fieldToMove} as Record<number, ConcreteDataField>;

  emit('update:modelValue', {
    ...props.modelValue,
    value: newValue,
  });
};

// 更新值
const onValueUpdate = (key: number, updatedField: ConcreteDataField) => {
  emit('update:modelValue', {
    ...props.modelValue,
    value: {
      ...props.modelValue.value,
      [key]: updatedField,
    },
  });
};

// 删除条目
const removeEntry = (key: number) => {
  const {[key]: _, ...newValue} = props.modelValue.value;
  emit('update:modelValue', {
    ...props.modelValue,
    value: newValue as Record<number, ConcreteDataField>,
  });
};

// 添加条目
const addEntry = (data: ConcreteDataField) => {
  const currentKeys = Object.keys(props.modelValue.value).map(Number);
  let newKey = 0;
  while (currentKeys.includes(newKey)) {
    newKey++;
  }

  emit('update:modelValue', {
    ...props.modelValue,
    value: {
      ...props.modelValue.value,
      [newKey]: {
        ...data,
        value: getDefaultFieldValue(data.type),
        id: generateFieldId(),
      },
    },
  });
};

// KeyType 变更
const onKeyTypeChange = (val: DicKeyType) => {
  emit('update:modelValue', {...props.modelValue, keyType: val});
};

const onValueLengthTypeChange = (val: ValueLengthType) => {
  emit('update:modelValue', {...props.modelValue, valueLengthType: val});
};

// Popover
const popoverProps = ref<FieldPopoverProps>({
  visible: false,
  title: '',
  actionType: 'add',
  virtualRef: null,
  dataField: createDefaultField('u8', 'field_1'),
});
const popoverTargetKey = ref<number | null>(null);
const oldValue = ref<ConcreteDataField | null>(null);

const openPopover = async (
    key: number | null,
    action: FieldPopoverActionType,
    title: string,
    e: any
) => {
  popoverProps.value.title = title;
  popoverProps.value.virtualRef = e.currentTarget;
  popoverProps.value.actionType = action;
  popoverTargetKey.value = key;

  if (action === 'edit' && key !== null) {
    popoverProps.value.dataField = JSON.parse(JSON.stringify(props.modelValue.value[key]));
    oldValue.value = JSON.parse(JSON.stringify(props.modelValue.value[key]));
  } else {
    oldValue.value = null;
  }

  await nextTick();
  popoverProps.value.visible = true;
};

const closePopover = async () => {
  popoverProps.value.visible = false;
  await nextTick();
};

const onPopoverConfirm = async (e: { action: FieldPopoverActionType; data: ConcreteDataField }) => {
  const {action, data} = e;
  const cloned = JSON.parse(JSON.stringify(data));
  await closePopover();

  if (action === 'add') {
    addEntry(cloned);
  } else if (action === 'edit') {
    const key = popoverTargetKey.value;
    if (key === null) return;

    let finalValue = cloned.value;
    if (oldValue.value?.type !== cloned.type) {
      finalValue = getDefaultFieldValue(cloned.type);
    }

    onValueUpdate(key, {...cloned, value: finalValue});
    oldValue.value = null;
  }
};

</script>

<style scoped lang="scss">
.dict-editor {
  border-radius: 4px;
  background-color: #fafafa;
  margin-bottom: 5px;

  .field-list {
    border: 1px solid var(--el-border-color-light);
    padding: 5px 10px;
  }

  .field-item {
    display: flex;
    align-items: center;
    justify-content: start;
    gap: 8px;
    padding: 8px 0;

    &:not(:last-child) {
      border-bottom: 1px dashed #eee;
    }
  }

  .key-input {
    width: 120px;
    margin-right: 5px;
  }
}

</style>
