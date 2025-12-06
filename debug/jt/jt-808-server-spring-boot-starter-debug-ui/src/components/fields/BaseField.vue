<script setup lang="ts">
import {computed, ref} from 'vue';
import {ArrowDown, ArrowRight, InfoFilled} from "@element-plus/icons-vue";
import {DataField, PrependLengthFieldType} from "@/types/data-fields.ts";
import {fieldTypeColorMap} from "@/utils/field-utils.ts";

interface Props {
  modelValue: DataField;
  typeLabel: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'update:modelValue', value: DataField): void;
}>();

const isExpanded = ref(false);
const toggleExpand = (e: Event) => {
  const target = e.target as HTMLElement;
  if (target.closest('.ignore-click')) return;
  isExpanded.value = !isExpanded.value;
};

const updateName = (newName: string) => {
  if (newName === props.modelValue.name) return;
  emit('update:modelValue', {
    ...props.modelValue,
    name: newName,
  });
};

const updatePrepend = (newType: string) => {
  if (newType === props.modelValue.prependLengthFieldType) return;
  emit('update:modelValue', {
    ...props.modelValue,
    prependLengthFieldType: newType as PrependLengthFieldType,
  });
};

const typeColor = computed(() => {
  return fieldTypeColorMap[props.modelValue.type] || '#1890ff';
});
</script>

<template>
  <div class="base-field" :class="{ expanded: isExpanded }">
    <!-- 摘要行 -->
    <div class="summary" @click="toggleExpand">
      <!-- 展开图标 -->
      <el-icon :size="12" class="expand-icon">
        <ArrowDown v-if="isExpanded"/>
        <ArrowRight v-else/>
      </el-icon>

      <!-- 字段声明：const name : type -->
      <div class="field-declaration">
        <code class="declaration-code">
          <span class="keyword">const</span>
          <el-input
              :model-value="props.modelValue.name ?? ''"
              @update:model-value="updateName"
              size="small"
              placeholder="字段名（调试用）"
              @click.stop
              class="inline-input"
          />:
          <el-tag style="width: 120px;" :style="{ backgroundColor: typeColor, color: 'white' }">
            {{ typeLabel }}
          </el-tag>
        </code>
      </div>

      <!-- 等号 -->
      <code class="equals-sign">=</code>

      <!-- 值区域 -->
      <div class="field-value">
        <slot name="inline-value" :value="props.modelValue.value"/>
      </div>

      <!-- 前置长度标签（右对齐） -->
      <div
          v-if="props.modelValue.prependLengthFieldType && props.modelValue.prependLengthFieldType !== 'none'"
          class="prepend-tag"
      >
        <el-tag type="warning" size="small" class="length-tag">
          前置: {{ props.modelValue.prependLengthFieldType }}
        </el-tag>
      </div>

      <div class="field-actions" @click.stop>
        <slot name="actions"/>
      </div>

    </div>
    <!-- 编辑器面板 -->
    <div v-show="isExpanded" class="editor">
      <el-form label-width="auto" size="small">
        <el-form-item label="前置" style="align-items: center;justify-items: center;">
          <template #label>
            <div class="form-item-label-with-icon">
              前置
              <el-tooltip content="前置长度字段: 编码时，前置一个用来表示当前字段长度的字段" placement="top">
                <el-icon>
                  <InfoFilled/>
                </el-icon>
              </el-tooltip>
            </div>
          </template>
          <el-radio-group
              :model-value="props.modelValue.prependLengthFieldType ?? 'none'"
              @update:model-value="updatePrepend"
          >
            <el-radio-button label="none" value="none"/>
            <el-radio-button label="u8" value="u8"/>
            <el-radio-button label="u16" value="u16"/>
            <el-radio-button label="u32" value="u32"/>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="名称">
          <template #label>
            <div class="form-item-label-with-icon">
              名称
              <el-tooltip content="字段名称: 仅仅调试用到(随便写，可重复，也可空)" placement="top">
                <el-icon>
                  <InfoFilled/>
                </el-icon>
              </el-tooltip>
            </div>
          </template>
          <el-input
              :model-value="props.modelValue.name ?? ''"
              @update:model-value="updateName"
              placeholder="用于调试或文档"
              style="width: 200px"
          />
        </el-form-item>

        <slot name="editor-value" :value="props.modelValue.value"/>

        <!-- 元数据... -->
      </el-form>
    </div>
  </div>
</template>

<style lang="scss">

.form-item-label-with-icon {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>

<style scoped lang="scss">

.keyword {
  color: #409eff;
  font-weight: bold;
  margin-right: 4px;
  font-style: italic;
}

.base-field {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  transition: box-shadow 0.2s ease, background-color 0.2s ease;

  &:hover:not(.expanded) {
    box-shadow: 0 0 0 1px var(--el-color-primary-light-5);
    background-color: #fafafa;
  }

  &.expanded {
    > .summary { /* 直接子选择器 */
      background-color: #f0f9eb;
      border-radius: 6px 6px 0 0;
    }

    .expand-icon {
      transform: rotate(90deg);
    }
  }

  .summary {
    display: flex;
    align-items: center;
    padding: 2px 5px;
    cursor: pointer;
    user-select: none;
    gap: 8px;
    min-height: 32px;

    .expand-icon {
      color: #909399;
      transition: transform 0.2s ease;
      transform: rotate(0deg);
    }

    .field-declaration {
      flex-shrink: 0;
      display: flex;
      align-items: center;

      .declaration-code {
        font-family: Consolas, Monaco, 'Courier New', monospace;
        font-size: 13px;
        color: #303133;

        .keyword {
          color: #409eff;
          font-weight: bold;
          margin-right: 4px;
        }

        .inline-input {
          width: auto;
          min-width: 80px;
          margin: 0 5px;

          :deep(.el-input__wrapper) {
            padding: 0;
          }

          :deep(.el-input__inner) {
            padding: 0 10px !important;
            height: 22px !important;
            line-height: 20px !important;
            font-family: Consolas, Monaco, 'Courier New', monospace;
            //background-color: #f5f5f5 !important;
            //border: 1px solid #dcdfe6 !important;
            border-radius: 4px !important;
            box-sizing: border-box !important;
            color: #6a70e4;
            text-align: left;
          }
        }

        .type-tag {
          border: none;
          font-weight: 500;
          height: 22px;
          line-height: 20px;
          padding: 0 8px;
          border-radius: 4px;
          margin: 0 4px;
          min-width: 60px;
          max-width: 120px;
          text-align: center;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .equals-sign {
      flex-shrink: 0;
      font-family: Consolas, Monaco, 'Courier New', monospace;
      color: #909399;
      font-weight: bold;
    }

    .field-value {
      flex: 1;
      min-width: 0;
      font-family: Consolas, Monaco, 'Courier New', monospace;
      font-size: 13px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      /*&:hover {
        background-color: #f5f5f5;
        border-radius: 4px;
        padding: 0 4px;
      }*/
    }

    .field-actions {
      margin-left: auto;
      display: flex;
      gap: 6px;
      align-items: center;
      flex-shrink: 0;
    }

    .prepend-tag {
      margin-left: auto;
      flex-shrink: 0;

      .length-tag {
        border: none;
        font-weight: 500;
        height: 22px;
        line-height: 20px;
        padding: 0 6px;
        border-radius: 4px;
      }
    }
  }

  .editor {
    padding: 10px;
    background-color: #fafafa;
    border-top: 1px solid #eee;
    border-radius: 0 0 6px 6px;

    .extra-textarea {
      font-family: Consolas, Monaco, 'Courier New', monospace;
      font-size: 12px;
    }

    :deep(.el-form-item) {
      &:not(:last-child) {
        margin-bottom: 6px;
      }

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}
</style>
