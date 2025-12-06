<template>
  <div>
    <BaseField
        :model-value="modelValue"
        type-label="seq"
        @update:model-value="safeEmit"
    >
      <!-- Inline 显示 -->
      <template #inline-value>
        <el-tag type="info" size="small">
          {{ localValue.length }} 个子项
        </el-tag>
        <el-tooltip
            effect="dark"
            content="添加子项"
            placement="top"
        >
          <el-button color="#626aef" size="small" class="ml-2" :icon="Plus" circle
                     @click="e => openPopover(-1,'add','添加子项', e)"
                     @click.stop
          />
        </el-tooltip>
      </template>

      <!-- 编辑器 -->
      <template #editor-value>
        <el-form-item>
          <template #label>
            <div class="form-item-label-with-icon">
              元素
              <el-tooltip content="列表的元素" placement="top">
                <el-icon>
                  <InfoFilled/>
                </el-icon>
              </el-tooltip>
            </div>
          </template>
          <el-tooltip
              v-if="!localValue || localValue.length === 0"
              effect="dark"
              content="添加子项"
              placement="top"
          >
            <el-button color="#626aef" size="small" class="ml-2" :icon="Plus" circle
                       @click="e => openPopover(-1,'add','添加子项', e)"
                       @click.stop
            />
          </el-tooltip>
          <div v-else class="sequence-editor">
            <!-- 字段列表 -->
            <div ref="fieldListRef" class="field-list">
              <div
                  v-for="(field, index) in localValue"
                  :key="field.id || `${field.type}_${index}`"
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
                      <el-tooltip
                          effect="dark"
                          content="编辑"
                          placement="top"
                      >
                        <el-button size="small"
                                   type="default"
                                   :icon="Edit"
                                   circle
                                   @click="e => openPopover(index,'edit','编辑字段', e)"
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
                            @click="e => openPopover(index,'add','添加同级字段', e)"
                        />
                      </el-tooltip>
                      <el-tooltip
                          effect="dark"
                          content="删除"
                          placement="top"
                      >
                        <el-button size="small" type="danger" :icon="Delete" circle
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

    <FieldEditorPopover v-model="popoverProps" @on-confirm="onPopoverConfirm"/>
  </div>
</template>

<script setup lang="ts">
import {nextTick, onMounted, onUnmounted, ref, watch} from 'vue';
import BaseField from '../BaseField.vue';
import {ConcreteDataField, FieldType, SequenceLike, useTypedFieldEmit} from '@/types/data-fields';
import {FIELD_COMPONENT_MAP} from '@/components/fields';
import Sortable from 'sortablejs';
import {createDefaultField, generateFieldId, getDefaultFieldValue} from '@/utils/field-utils';
import {Delete, Edit, InfoFilled, Plus} from "@element-plus/icons-vue";
import {ElButton} from "element-plus";
import FieldEditorPopover, {
  FieldPopoverActionType,
  FieldPopoverProps
} from "@/components/fields/FieldEditorPopover.vue";


const props = defineProps<{
  modelValue: SequenceLike;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: SequenceLike): void;
}>();

const safeEmit = useTypedFieldEmit<'seq', SequenceLike>('seq', emit);

// 本地副本
const localValue = ref<ConcreteDataField[]>(
    props.modelValue.value.map(f => ({
      ...f,
      id: f.id ?? generateFieldId(),
    }))
);

// 同步外部 modelValue 变化（deep）
watch(
    () => props.modelValue.value,
    (newVal) => {
      localValue.value = newVal.map(f => ({
        ...f,
        id: f.id ?? generateFieldId(),
      }));
    },
    {deep: true}
);

const popoverProps = ref<FieldPopoverProps>({
  visible: false,
  title: '',
  actionType: 'add',
  virtualRef: null,
  dataField: createDefaultField('u8', 'field_1')
})
const popoverVirtualRefButtonIndex = ref(-1)
const oldValue = ref<ConcreteDataField | null>(null)
const openPopover = async (index: number, action: FieldPopoverActionType, title: string, e: any) => {
  popoverProps.value.title = title
  popoverProps.value.virtualRef = e.currentTarget
  popoverProps.value.actionType = action
  if (action == 'edit' && index >= 0) {
    popoverProps.value.dataField = JSON.parse(JSON.stringify(localValue.value[index]))
    oldValue.value = JSON.parse(JSON.stringify(localValue.value[index]))
  } else {
    oldValue.value = null
  }
  popoverVirtualRefButtonIndex.value = index
  await nextTick()
  popoverProps.value.visible = true
}
const closePopover = async () => {
  popoverProps.value.visible = false
  await nextTick()
}
const onPopoverConfirm = async (e: { action: FieldPopoverActionType, data: ConcreteDataField }) => {
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
    const index = popoverVirtualRefButtonIndex.value
    if (index === -1) {
      localValue.value = [field, ...localValue.value];
    } else if (index === -2) {
      localValue.value = [...localValue.value, field];
    } else {
      localValue.value.splice(index + 1, 0, field);
    }
  } else if (action == 'edit') {
    const index = popoverVirtualRefButtonIndex.value
    if (index < 0) {
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
    localValue.value[index] = data
  }
  emitSequenceUpdate(localValue.value);
}

// DOM 引用
const fieldListRef = ref<HTMLDivElement | null>(null);
const sortable = ref<Sortable | null>(null);

// 获取组件
const getComponentForType = (type: FieldType) => {
  return FIELD_COMPONENT_MAP[type] || 'div';
};

// 更新子字段
const onChildUpdate = (index: number, updatedField: ConcreteDataField) => {
  const newValue = [...localValue.value];
  newValue[index] = updatedField;
  emitSequenceUpdate(newValue);
};

// 删除字段
const removeField = (index: number) => {
  const newValue = [...localValue.value];
  newValue.splice(index, 1);
  emitSequenceUpdate(newValue);
};

// 触发更新
const emitSequenceUpdate = (newValue: ConcreteDataField[]) => {
  const updated: SequenceLike = {
    ...props.modelValue,
    value: newValue,
  };
  emit('update:modelValue', updated);
};

// 初始化拖拽
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
            oldIndex == null ||
            newIndex == null ||
            oldIndex === newIndex
        ) {
          return;
        }

        const newValue = [...localValue.value];
        const [movedItem] = newValue.splice(oldIndex, 1);
        newValue.splice(newIndex, 0, movedItem);

        localValue.value = newValue;
        emitSequenceUpdate(newValue);
      },
    });
  }
});

// 销毁拖拽实例
onUnmounted(() => {
  if (sortable.value) {
    sortable.value.destroy();
    sortable.value = null;
  }
});
</script>

<style scoped>
.sequence-editor {
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  padding: 6px;
  background-color: #fafafa;
  margin-bottom: 5px;
}

.field-list {
  margin-bottom: 0;
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
