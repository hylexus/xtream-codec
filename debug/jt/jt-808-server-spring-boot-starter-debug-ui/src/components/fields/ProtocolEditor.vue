<script setup lang="ts">
import {nextTick, onBeforeUnmount, ref, watch} from 'vue';
import Sortable from 'sortablejs';
import {ElButton} from 'element-plus';
import {Delete, Edit, Plus} from '@element-plus/icons-vue';
import {DataField} from '@/types/data-fields';
import {FIELD_COMPONENT_MAP} from '@/components/fields';
import {createDefaultField, generateFieldId, getDefaultFieldValue} from '@/utils/field-utils';
import FieldEditorPopover, {
  FieldPopoverActionType,
  FieldPopoverConfirmEvent,
  FieldPopoverProps
} from "@/components/fields/FieldEditorPopover.vue";

interface Props {
  modelValue: DataField[];
  showTitle?: boolean;
  title?: string;
}

const props = withDefaults(defineProps<Props>(), {
  showTitle: true,
  title: '字段编辑器'
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: DataField[]): void;
}>();

const fields = ref<DataField[]>(
    props.modelValue.map(f => ({...f, id: f.id ?? generateFieldId()}))
);

watch(
    () => props.modelValue,
    (newVal, oldVal) => {
      if (newVal !== oldVal) {
        fields.value = newVal.map(f => ({...f, id: f.id ?? generateFieldId()}));
      }
    },
    {deep: false}
);

const fieldListRef = ref<HTMLDivElement | null>(null);
let sortableInstance: Sortable | null = null;

watch(
    () => fieldListRef.value,
    (el) => {
      if (sortableInstance) sortableInstance.destroy();
      if (el) {
        sortableInstance = new Sortable(el, {
          animation: 150,
          handle: '.drag-handle',
          ghostClass: 'sortable-ghost',
          chosenClass: 'sortable-chosen',
          onUpdate: (evt) => {
            const {oldIndex, newIndex} = evt;
            if (oldIndex == null || newIndex == null || oldIndex === newIndex) return;
            const items = [...fields.value];
            const [moved] = items.splice(oldIndex, 1);
            items.splice(newIndex, 0, moved);
            fields.value = items;
            emit('update:modelValue', items);
          },
        });
      }
    },
    {immediate: true}
);

onBeforeUnmount(() => {
  if (sortableInstance) sortableInstance.destroy();
});

const getFieldComponent = (type: string) => {
  return FIELD_COMPONENT_MAP[type as keyof typeof FIELD_COMPONENT_MAP] || 'div';
};

const onFieldUpdate = (index: number, updated: DataField) => {
  const newFields = [...fields.value];
  // 保留原 id
  newFields[index] = {...updated, id: newFields[index].id!};
  fields.value = newFields;
  emit('update:modelValue', newFields);
};

const removeField = (index: number) => {
  const newFields = [...fields.value];
  newFields.splice(index, 1);
  fields.value = newFields;
  emit('update:modelValue', newFields);
};

const popoverProps = ref<FieldPopoverProps>({
  visible: false,
  title: '',
  actionType: 'add',
  virtualRef: null,
  dataField: createDefaultField('u8', 'field_1')
});
const oldValue = ref<DataField | null>(null);
const popoverVirtualRefButtonIndex = ref(-1);

const openPopover = async (index: number, action: FieldPopoverActionType, title: string, e: any) => {
  popoverProps.value.title = title;
  popoverProps.value.virtualRef = e.currentTarget;
  popoverProps.value.actionType = action;
  if (action === 'edit' && index >= 0) {
    popoverProps.value.dataField = JSON.parse(JSON.stringify(fields.value[index]));
    oldValue.value = JSON.parse(JSON.stringify(fields.value[index]));
  }
  popoverVirtualRefButtonIndex.value = index;
  await nextTick();
  popoverProps.value.visible = true;
};

const closePopover = async () => {
  popoverProps.value.visible = false;
  await nextTick();
};

const onPopoverConfirm = async (e: FieldPopoverConfirmEvent) => {
  const action = e.action;
  const data = JSON.parse(JSON.stringify(e.data));
  await closePopover();

  if (action === 'add') {
    const field = {
      ...data,
      value: getDefaultFieldValue(data.type),
      id: generateFieldId()
    };
    const index = popoverVirtualRefButtonIndex.value;
    if (index === -1) {
      fields.value = [field, ...fields.value];
    } else if (index === -2) {
      fields.value = [...fields.value, field];
    } else {
      fields.value.splice(index + 1, 0, field);
    }
  } else if (action === 'edit') {
    const index = popoverVirtualRefButtonIndex.value;
    if (index < 0) return;
    if (oldValue.value && oldValue.value.type !== data.type) {
      data.value = getDefaultFieldValue(data.type);
    }
    oldValue.value = null;
    const newValues = [...fields.value];
    newValues[index] = data;
    fields.value = newValues;
  }
  emit('update:modelValue', fields.value);
};
</script>

<template>
  <div>
    <div class="protocol-editor">
      <div v-if="props.showTitle" class="summary">
        <div class="summary-left">
          <div>
            共 <span class="color-blue">{{ fields.length }}</span> 个字段
          </div>
          <el-button
              :icon="Plus"
              circle
              type="primary"
              size="small"
              @click="(e) => openPopover(-1,'add','添加字段', e)"/>
        </div>
        <div class="summary-title">{{ props.title }}</div>
      </div>
      <div ref="fieldListRef" class="field-list">
        <div v-for="(field,index) in fields"
             :key="field.id!"
             class="field-row">
          <div class="drag-handle field-row-drag-handle" title="拖拽排序">
            <el-icon size="24">
              <SvgIcon icon-class="icon-svg-drag"/>
            </el-icon>
          </div>
          <div class="field-row-component">
            <component
                :is="getFieldComponent(field.type)"
                :model-value="field"
                @update:model-value="(v: DataField) => onFieldUpdate(index, v)"
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
                             @click="e => openPopover(index, 'edit','编辑', e)"
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
                             @click="removeField(fields.indexOf(field))"
                  />
                </el-tooltip>
              </template>
            </component>
          </div>
        </div>
        <div v-if="fields.length === 0" style="display: flex; align-items: center; justify-content: start;">
          点击
          <el-button
              :icon="Plus"
              class="ml-2 mr-2"
              circle
              type="primary"
              size="small"
              @click="e => openPopover(-2,'add','添加字段', e)">
          </el-button>
          添加字段
        </div>
      </div>
    </div>

    <FieldEditorPopover v-model="popoverProps" @on-confirm="onPopoverConfirm"/>
  </div>
</template>

<style scoped>
.protocol-editor {
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;

  .summary {
    border-bottom: 1px solid var(--el-border-color);
    padding-bottom: 10px;
    margin-bottom: 10px;
    display: flex;
    align-items: center;

    .summary-left {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .summary-title {
      margin: 0 auto;
      font-weight: bold;
      color: var(--el-text-color-primary);
      font-size: 14px;
    }
  }

  .field-list {

    .field-row {
      box-sizing: border-box;
      display: flex;
      align-items: center;

      &:not(:last-child) {
        margin-bottom: 6px;
      }

      .field-row-drag-handle {
        width: 34px;
        flex-shrink: 0;
      }

      .field-row-component {
        flex: 1;
        min-width: 0;
      }

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
    margin-right: 6px;
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

}
</style>
