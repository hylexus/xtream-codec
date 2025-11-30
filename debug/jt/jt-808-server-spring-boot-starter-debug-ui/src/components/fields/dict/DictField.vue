<template>
  <div>
    <BaseField
        :model-value="modelValue"
        type-label="dict"
        @update:model-value="safeEmit"
    >
      <!-- Inline 显示 -->
      <template #inline-value>
        <el-tag type="warning" size="small">
          {{ Object.keys(localValue).length }} 个键值对
        </el-tag>
        <el-tooltip
            effect="dark"
            content="添加键值对"
            placement="top"
        >
          <el-button color="#626aef" size="small" class="ml-2" :icon="Plus" circle
                     @click="e => openPopover('-1','add','添加键值对', e)"
                     @click.stop
          />
        </el-tooltip>
      </template>

      <!-- 编辑器 -->
      <template #editor-value>
        <el-form-item class="dict-editor" label="键值对">
          <!-- 字段列表 -->
          <div class="field-list">
            <div
                v-for="(field, keyStr) in localValue"
                :key="keyStr"
                class="field-item"
            >
              <!-- 键输入 -->
              <div class="key-input">
                <el-input-number
                    style="width: 100px;"
                    v-model.number="editableKeys[keyStr]"
                    :min="getKeyMin()"
                    :max="getKeyMax()"
                    size="small"
                    :controls="false"
                    @change="onKeyChange(keyStr, $event)"
                >
                  <template #prefix>Key</template>
                </el-input-number>
              </div>

              <!-- 值编辑器 -->
              <div style="flex: 1; min-width: 0;">
                <component
                    :is="getComponentForType(field.type)"
                    :model-value="field"
                    @update:model-value="onValueUpdate(keyStr, $event)"
                >
                  <template #actions>
                    <el-tooltip
                        effect="dark"
                        content="编辑"
                        placement="top"
                    >
                      <el-button
                          size="small"
                          type="default"
                          :icon="Edit"
                          circle
                          @click="e => openPopover(keyStr,'edit','编辑字段', e)"
                      />
                    </el-tooltip>
                    <el-tooltip
                        effect="dark"
                        content="添加同级字段"
                        placement="top"
                    >
                      <el-button
                          size="small"
                          type="primary"
                          :icon="Plus"
                          circle
                          @click="e => openPopover(keyStr,'add','添加同级字段', e)"
                      />
                    </el-tooltip>
                    <el-tooltip
                        effect="dark"
                        content="删除"
                        placement="top"
                    >
                      <el-button size="small" type="danger" :icon="Delete" circle
                                 @click="removeEntry(keyStr)"
                      />
                    </el-tooltip>
                  </template>
                </component>
              </div>
            </div>
          </div>
        </el-form-item>
      </template>

      <!-- 透传 actions 插槽 -->
      <template #actions>
        <slot name="actions"/>
      </template>

    </BaseField>
    <FieldEditorPopover v-model="popoverProps" @on-confirm="onPopoverConfirm"/>
  </div>
</template>

<script setup lang="ts">
import {nextTick, ref, watch} from 'vue';
import BaseField from "@/components/fields/BaseField.vue";
import {DataField, DicKeyType, FieldType, useTypedFieldEmit,} from '@/types/data-fields';
import {FIELD_COMPONENT_MAP} from '@/components/fields';
import {createDefaultField, generateFieldId, getDefaultFieldValue} from '@/utils/field-utils';
import {Delete, Edit, Plus} from "@element-plus/icons-vue";
import {ElButton} from "element-plus";
import FieldEditorPopover, {
  FieldPopoverActionType,
  FieldPopoverProps
} from "@/components/fields/FieldEditorPopover.vue";

interface DictLike extends DataField {
  type: 'dict';
  value: Record<number, DataField>;
}

const props = defineProps<{
  modelValue: DictLike;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: DictLike): void;
}>();

const safeEmit = useTypedFieldEmit<'dict', DictLike>('dict', emit);

// 本地状态：使用 string key 便于 Vue 响应式处理
type LocalDict = Record<string, DataField>;
const localValue = ref<LocalDict>({});
const editableKeys = ref<Record<string, number>>({});

// 初始化本地状态
function initLocalState() {
  const newVal: LocalDict = {};
  const newKeys: Record<string, number> = {};
  for (const [keyNum, field] of Object.entries(props.modelValue.value)) {
    const keyStr = keyNum;
    newVal[keyStr] = {
      ...field,
      id: field.id ?? generateFieldId(),
    };
    newKeys[keyStr] = parseInt(keyNum, 10);
  }
  localValue.value = newVal;
  editableKeys.value = newKeys;
}

initLocalState();

// 同步外部 modelValue 变化
watch(
    () => props.modelValue.value,
    () => {
      initLocalState();
    },
    {deep: true}
);

const newValueType = ref<FieldType | undefined>(undefined);
const newValueName = ref<string>('');

// 获取组件
const getComponentForType = (type: FieldType) => {
  return FIELD_COMPONENT_MAP[type] || 'div';
};

// 键取值范围（根据 keyType）
const getKeyMin = (): number => 0;
const getKeyMax = (): number => {
  const keyType = props.modelValue.keyType as DicKeyType;
  switch (keyType) {
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

// 更新值
const onValueUpdate = (keyStr: string, updatedField: DataField) => {
  const newValue = {...localValue.value};
  newValue[keyStr] = updatedField;
  localValue.value = newValue;
  emitDictUpdate();
};

// 更新键（带去重）
const onKeyChange = (oldKeyStr: string, newKeyNum: number | null) => {
  if (newKeyNum === null || isNaN(newKeyNum)) return;

  const newKeyStr = String(newKeyNum);

  // 防止键冲突
  if (newKeyStr !== oldKeyStr && localValue.value[newKeyStr]) {
    // 回滚
    editableKeys.value[oldKeyStr] = parseInt(oldKeyStr, 10);
    return;
  }

  // 重命名键
  const newValue = {...localValue.value};
  const newKeys = {...editableKeys.value};

  const field = newValue[oldKeyStr];
  delete newValue[oldKeyStr];
  delete newKeys[oldKeyStr];

  newValue[newKeyStr] = field;
  newKeys[newKeyStr] = newKeyNum;

  localValue.value = newValue;
  editableKeys.value = newKeys;
  emitDictUpdate();
};

// 删除条目
const removeEntry = (keyStr: string) => {
  const newValue = {...localValue.value};
  const newKeys = {...editableKeys.value};
  delete newValue[keyStr];
  delete newKeys[keyStr];
  localValue.value = newValue;
  editableKeys.value = newKeys;
  emitDictUpdate();
};

// 添加条目
const addEntry = (data: DataField) => {

  const newField = createDefaultField(data.type, data.name, data.charset as string);

  // 生成不冲突的默认键（从 0 开始）
  let newKeyNum = 0;
  const existingKeys = Object.keys(localValue.value).map(Number);
  while (existingKeys.includes(newKeyNum)) {
    newKeyNum++;
  }

  const newKeyStr = String(newKeyNum);

  const newValue = {...localValue.value};
  const newKeys = {...editableKeys.value};

  newValue[newKeyStr] = newField;
  newKeys[newKeyStr] = newKeyNum;

  localValue.value = newValue;
  editableKeys.value = newKeys;
  emitDictUpdate();

  newValueType.value = undefined;
  newValueName.value = '';
};

// 触发更新
const emitDictUpdate = () => {
  const outputValue: Record<number, DataField> = {};
  for (const [keyStr, field] of Object.entries(localValue.value)) {
    outputValue[parseInt(keyStr, 10)] = field;
  }

  const updated: DictLike = {
    ...props.modelValue,
    value: outputValue,
  };
  emit('update:modelValue', updated);
};

const popoverProps = ref<FieldPopoverProps>({
  visible: false,
  title: '',
  actionType: 'add',
  virtualRef: null,
  dataField: createDefaultField('u8', 'field_1')
})
const ADD_ENTRY_BUTTON_INDEX_TOP = 'top' as const;
const popoverVirtualRefButtonIndex = ref<string>(ADD_ENTRY_BUTTON_INDEX_TOP)
const oldValue = ref<DataField | null>(null)
const openPopover = async (index: string, action: FieldPopoverActionType, title: string, e: any) => {
  popoverProps.value.title = title
  popoverProps.value.virtualRef = e.currentTarget
  popoverProps.value.actionType = action
  if (action == 'edit' && index !== ADD_ENTRY_BUTTON_INDEX_TOP) {
    popoverProps.value.dataField = JSON.parse(JSON.stringify(localValue.value[index]))
    oldValue.value = JSON.parse(JSON.stringify(localValue.value[index]))
  } else {
    oldValue.value = null;
  }
  popoverVirtualRefButtonIndex.value = index
  await nextTick()
  popoverProps.value.visible = true
}

const closePopover = async () => {
  popoverProps.value.visible = false
  await nextTick()
}

const onPopoverConfirm = async (e: { action: FieldPopoverActionType, data: DataField }) => {
  const action = e.action;
  const data = JSON.parse(JSON.stringify(e.data))
  await closePopover()
  if (action == 'add') {
    const field = {
      ...data,
      ...{
        value: getDefaultFieldValue(data.type),
        id: generateFieldId()
      }
    }
    addEntry(field)
  } else if (action == 'edit') {
    const index = popoverVirtualRefButtonIndex.value
    if (index === ADD_ENTRY_BUTTON_INDEX_TOP) {
      return
    }
    if (oldValue.value) {
      if (oldValue.value.type !== data.type) {
        data.value = getDefaultFieldValue(data.type)
      } else {
        data.value = oldValue.value.value
      }
    }
    oldValue.value = null
    onValueUpdate(index, data)
  }
}
</script>

<style scoped>
.dict-editor {
  border-radius: 4px;
  background-color: #fafafa;
}

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
  border-bottom: 1px dashed #eee;
}

.key-input {
  width: 100px;
  margin-right: 5px;
}

</style>
