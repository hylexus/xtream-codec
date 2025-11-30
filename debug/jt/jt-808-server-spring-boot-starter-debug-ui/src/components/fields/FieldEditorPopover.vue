<template>
  <div>
    <el-popover
        placement="bottom"
        :title="model.title"
        :width="560"
        trigger="contextmenu"
        :teleported="false"
        :virtual-triggering="true"
        :virtual-ref="model.virtualRef"
        :visible="model.visible"
    >
      <template #default>
        <div v-click-outside="closePopover" style="border: 1px solid var(--el-border-color); padding: 5px;">
          <FieldEditor v-model="model.dataField"/>
          <div class="flex flex-justify-center">
            <el-button :icon="Close" circle type="default" size="small"
                       @click="closePopover"/>
            <el-button :icon="Select" circle type="success" size="small"
                       @click="onPopoverConfirm"/>
          </div>
        </div>
      </template>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import {DataField} from '@/types/data-fields';
import {Close, Select} from "@element-plus/icons-vue";
import {ClickOutside as vClickOutside, ElButton} from "element-plus";
import FieldEditor from "@/components/fields/FieldEditor.vue";
import {nextTick} from "vue";

export type FieldPopoverActionType = 'add' | 'edit';

export interface FieldPopoverProps {
  actionType: FieldPopoverActionType;
  visible: boolean;
  title: string;
  dataField: DataField;
  virtualRef: any;
}

const model = defineModel<FieldPopoverProps>({
  required: true,
  default: {visible: false, dataField: {prependLengthFieldType: 'none', type: 'u8'}}
})

{
  if (!model.value.dataField.prependLengthFieldType) {
    model.value.dataField.prependLengthFieldType = 'none'
  }
  if (model.value.dataField.type === 'string' && !model.value.dataField.charset) {
    model.value.dataField.charset = 'utf8'
  }
}

export interface FieldPopoverConfirmEvent {
  action: FieldPopoverActionType;
  data: DataField;
}
const emit = defineEmits<{
  // (e: 'update:modelValue', val: FieldType): void;
  (e: 'onConfirm', val: FieldPopoverConfirmEvent): void;
  (e: 'onCancel'): void;
}>();

const closePopover = async () => {
  emit('onCancel')
  await nextTick()
  model.value.visible = false
}
const onPopoverConfirm = async () => {
  emit('onConfirm', {action: model.value.actionType, data: model.value.dataField})
}
</script>

<style scoped lang="scss">
</style>
