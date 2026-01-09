<template>
  <div>
    <BaseField
        :model-value="modelValue"
        type-label="struct"
        @update:model-value="safeEmit"
    >
      <!-- Inline 显示 -->
      <template #inline-value>
        <el-tag type="success" size="small">
          {{ modelValue.value.length }} 个子项
        </el-tag>
        <el-tooltip effect="dark" content="添加子项" placement="top">
          <el-button
              color="#626aef"
              size="small"
              class="ml-2"
              :icon="Plus"
              circle
              @click="(e:any) => openPopover(-1, 'add', '添加子项', e)"
              @click.stop
          />
        </el-tooltip>
      </template>

      <!-- 编辑器 -->
      <template #editor-value>
        <el-form-item>
          <template #label>
            <div class="form-item-label-with-icon">
              字段
              <el-tooltip content="结构体字段" placement="top">
                <el-icon>
                  <InfoFilled/>
                </el-icon>
              </el-tooltip>
            </div>
          </template>
          <el-tooltip v-if="!localValue || localValue.length === 0" effect="dark" content="添加子项" placement="top">
            <el-button
                color="#626aef"
                size="small"
                class="ml-2"
                :icon="Plus"
                circle
                @click="(e:any) => openPopover(-1, 'add', '添加子项', e)"
                @click.stop
            />
          </el-tooltip>
          <div v-else class="struct-editor">
            <!-- 字段列表 -->
            <div ref="fieldListRef" class="field-list">
              <div
                  v-for="(field, index) in localValue"
                  :key="field.id!"
                  class="field-item"
              >
                <!-- 拖拽手柄 -->
                <div class="drag-handle" title="拖拽排序">
                  <el-icon size="24">
                    <SvgIcon icon-class="icon-svg-drag"/>
                  </el-icon>
                </div>

                <!-- 子字段编辑器 -->
                <div style="flex: 1; min-width: 0">
                  <component
                      :is="getComponentForType(field.type)"
                      :model-value="field"
                      @update:model-value="onChildUpdate(index, $event)"
                  >
                    <template #actions>
                      <el-tooltip effect="dark" content="编辑" placement="top">
                        <el-button
                            size="small"
                            type="default"
                            :icon="Edit"
                            circle
                            @click="(e:any) => openPopover(index, 'edit', '编辑字段', e)"
                        />
                      </el-tooltip>
                      <el-tooltip effect="dark" content="添加同级字段" placement="top">
                        <el-button
                            size="small"
                            type="primary"
                            :icon="Plus"
                            circle
                            @click="(e:any) => openPopover(index, 'add', '添加同级字段', e)"
                        />
                      </el-tooltip>
                      <el-tooltip effect="dark" content="删除" placement="top">
                        <el-button
                            size="small"
                            type="danger"
                            :icon="Delete"
                            circle
                            @click="removeField(index)"
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

      <!-- 透传 actions 插槽 -->
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
import {nextTick, onMounted, onUnmounted, ref, watch} from 'vue';
import BaseField from '../BaseField.vue';
import {ConcreteDataField, FieldType, StructLike, useTypedFieldEmit,} from '@/types/data-fields';
import {FIELD_COMPONENT_MAP} from '@/components/fields/index.ts';
import Sortable from 'sortablejs';
import {createDefaultField, generateFieldId, getDefaultFieldValue,} from '@/utils/field-utils.ts';
import {Delete, Edit, InfoFilled, Plus} from '@element-plus/icons-vue';
import FieldEditorPopover, {
  FieldPopoverActionType,
  FieldPopoverProps,
} from '@/components/fields/FieldEditorPopover.vue';


const props = defineProps<{
  modelValue: StructLike;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: StructLike): void;
}>();

const safeEmit = useTypedFieldEmit<'struct', StructLike>('struct', emit);

// 本地副本：用于 UI 操作（如拖拽、新增），但不用于 emit 判断
const localValue = ref<ConcreteDataField[]>(
    props.modelValue.value.map((f) => ({
      ...f,
      id: f.id ?? generateFieldId(),
    }))
);

watch(
    () => props.modelValue,
    (newVal) => {
      localValue.value = newVal.value.map((f) => ({
        ...f,
        id: f.id ?? generateFieldId(),
      }));
    },
    {deep: true}
);

// DOM 引用
const fieldListRef = ref<HTMLDivElement | null>(null);

// 获取组件
const getComponentForType = (type: FieldType) => {
  return FIELD_COMPONENT_MAP[type] || 'div';
};

// 更新子字段
const onChildUpdate = (index: number, updatedField: ConcreteDataField) => {
  const newValue = [...localValue.value];
  newValue[index] = updatedField;
  localValue.value = newValue;

  emitStructUpdate(newValue);
};

// 删除字段
const removeField = (index: number) => {
  const newValue = [...localValue.value];
  newValue.splice(index, 1);
  localValue.value = newValue;
  emitStructUpdate(newValue);
};

const emitStructUpdate = (newValue: ConcreteDataField[]) => {
  const oldValue = props.modelValue.value;

  // 长度或 id 序列不同 → 肯定变了
  if (
      newValue.length !== oldValue.length ||
      newValue.some((f, i) => f.id !== oldValue[i]?.id)
  ) {
    const updated: StructLike = {
      ...props.modelValue,
      value: newValue,
    };
    emit('update:modelValue', updated);
    return;
  }

  const updated: ConcreteDataField = {
    ...props.modelValue,
    value: newValue,
  };
  emit('update:modelValue', updated);
};

const sortable = ref<Sortable>();
onMounted(() => {
  if (fieldListRef.value) {
    sortable.value = new Sortable(fieldListRef.value, {
      animation: 150,
      handle: '.drag-handle',
      ghostClass: 'sortable-ghost',
      chosenClass: 'sortable-chosen',
      onUpdate: (evt) => {
        const {oldIndex, newIndex} = evt;
        if (
            oldIndex === undefined ||
            newIndex === undefined ||
            oldIndex === newIndex
        ) {
          return;
        }
        const newValue = [...localValue.value];
        const [movedItem] = newValue.splice(oldIndex, 1);
        newValue.splice(newIndex, 0, movedItem);

        localValue.value = newValue;
        emitStructUpdate(newValue);
      },
    });
  }
});

onUnmounted(() => {
  if (sortable.value) {
    sortable.value.destroy();
  }
});

// Popover 相关
const popoverProps = ref<FieldPopoverProps>({
  visible: false,
  title: '',
  actionType: 'add',
  virtualRef: null,
  dataField: createDefaultField('u8', 'field_1'),
});
const popoverVirtualRefButtonIndex = ref(-1);
const oldValue = ref<ConcreteDataField | null>(null);

const openPopover = async (
    index: number,
    action: FieldPopoverActionType,
    title: string,
    e: any
) => {
  popoverProps.value.title = title;
  popoverProps.value.virtualRef = e.currentTarget;
  popoverProps.value.actionType = action;

  if (action === 'edit' && index >= 0) {
    popoverProps.value.dataField = JSON.parse(JSON.stringify(localValue.value[index]));
    oldValue.value = JSON.parse(JSON.stringify(localValue.value[index]));
  } else {
    oldValue.value = null;
  }

  popoverVirtualRefButtonIndex.value = index;
  await nextTick();
  popoverProps.value.visible = true;
};

const closePopover = async () => {
  popoverProps.value.visible = false;
  await nextTick();
};

const onPopoverConfirm = async (e: { action: FieldPopoverActionType; data: ConcreteDataField }) => {
  const {action, data} = e;
  const clonedData = JSON.parse(JSON.stringify(data));
  await closePopover();

  if (action === 'add') {
    const field = {
      ...clonedData,
      value: getDefaultFieldValue(clonedData.type),
      id: generateFieldId(),
    };
    const index = popoverVirtualRefButtonIndex.value;
    if (index === -1) {
      localValue.value = [field, ...localValue.value];
    } else if (index === -2) {
      localValue.value = [...localValue.value, field];
    } else {
      localValue.value.splice(index + 1, 0, field);
    }
  } else if (action === 'edit') {
    const index = popoverVirtualRefButtonIndex.value;
    if (index < 0) return;

    let finalValue = clonedData.value;
    if (oldValue.value) {
      if (oldValue.value.type !== clonedData.type) {
        // 类型变了，重置 value
        finalValue = getDefaultFieldValue(clonedData.type);
      } else {
        // 类型没变，保留原 value
        finalValue = oldValue.value.value;
      }
    }
    localValue.value[index] = {
      ...clonedData,
      value: finalValue,
    };
    oldValue.value = null;
  }

  emitStructUpdate(localValue.value);
};
</script>

<style scoped>
.struct-editor {
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  padding: 5px;
  background-color: #fafafa;
}

.field-list {
  margin-bottom: 5px;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;

  &:not(:last-child) {
    border-bottom: 1px dashed #eee;
  }

}

.drag-handle {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: move;
  color: #999;
  transition: color 0.2s;
  user-select: none;
}

.drag-handle:hover {
  color: #666;
}

.sortable-ghost {
  background-color: #e6f7ff !important;
  opacity: 0.8;
  border: 2px dashed #1890ff;
}

.sortable-chosen {
  background-color: #f0f9eb;
  box-shadow: 0 0 0 2px var(--el-color-primary-light-5);
}
</style>
