<script setup lang="ts">
import {computed, reactive, ref, watch} from 'vue';
import {ArrowDown, ArrowRight, InfoFilled} from '@element-plus/icons-vue';
import {ElInput} from 'element-plus';
import {DataField} from '@/types/data-fields.ts';
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
const isEditingExtra = ref(false);

const local = reactive({
  value: props.modelValue.value,
  name: props.modelValue.name ?? '',
  prependLengthFieldType: props.modelValue.prependLengthFieldType ?? 'none',
});

// 两个状态：原始输入 + 上次有效 extra
const rawExtraInput = ref('');
const lastValidExtra = ref<Record<string, unknown>>({});

// 初始化：从 prop 加载初始值
{
  const {type, value, name, prependLengthFieldType, ...rest} = props.modelValue;
  rawExtraInput.value = JSON.stringify(rest, null, 2) || '';
  lastValidExtra.value = {...rest};
}

// 监听外部变化（仅同步结构字段）
watch(
    () => props.modelValue,
    (newVal) => {
      local.value = newVal.value;
      local.name = newVal.name ?? '';
      local.prependLengthFieldType = newVal.prependLengthFieldType ?? 'none';
      // 注意：extra 不自动同步，避免打断用户编辑
    },
    {deep: true}
);

// 合并值：使用 lastValidExtra，确保渲染稳定
const mergedValue = computed<DataField>(() => ({
  type: props.modelValue.type,
  value: local.value,
  ...(local.name ? {name: local.name} : {}),
  ...(local.prependLengthFieldType ? {prependLengthFieldType: local.prependLengthFieldType} : {}),
  ...lastValidExtra.value,
} as DataField));

// 自动 emit（非编辑态）
watch([local, rawExtraInput], () => {
  if (!isEditingExtra.value) {
    emit('update:modelValue', mergedValue.value);
  }
}, {deep: true});

// Extra 编辑控制
const onExtraFocus = () => {
  isEditingExtra.value = true;
};

const onExtraBlur = () => {
  isEditingExtra.value = false;

  try {
    if (rawExtraInput.value.trim()) {
      const parsed = JSON.parse(rawExtraInput.value);
      lastValidExtra.value = parsed;
      rawExtraInput.value = JSON.stringify(parsed, null, 2);
    } else {
      lastValidExtra.value = {};
    }
  } catch (e) {
    // 解析失败：保留上次有效值，不更新 UI
  }

  emit('update:modelValue', mergedValue.value);
};

const nameInput = ref<InstanceType<typeof ElInput> | null>(null);

const finishEditName = () => {
  if (props.modelValue.name) {
    local.name = props.modelValue.name;
  }
};

const toggleExpand = (e: Event) => {
  const target = e.target as HTMLElement;
  if (target.closest('.ignore-click')) {
    return;
  }
  isExpanded.value = !isExpanded.value;
};

const typeColor = computed(() => {
  return fieldTypeColorMap[props.modelValue.type] || '#1890ff'
})

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
          <span class="keyword">const</span>&nbsp;
          <el-input
              v-model="local.name"
              size="small"
              ref="nameInput"
              placeholder="字段名（调试用）"
              @blur="finishEditName"
              @keyup.enter="finishEditName"
              @keyup.esc="finishEditName"
              @click.stop
              class="inline-input"
          />&nbsp;:&nbsp;
          <el-tag
              style="width: 120px;"
              :style="{
                  backgroundColor: typeColor,
                  color: 'white',
                  border: 'none',
                  borderRadius: '5px'
              }"
          >
            {{ props.typeLabel }}
          </el-tag>
        </code>
      </div>

      <!-- 等号 -->
      <code class="equals-sign">=</code>

      <!-- 值区域 -->
      <div class="field-value">
        <slot
            name="inline-value"
            :value="local.value"
            :on-update="(val: any) => local.value = val"
        />
      </div>

      <!-- 前置长度标签（右对齐） -->
      <div
          v-if="local.prependLengthFieldType && local.prependLengthFieldType !== 'none'"
          class="prepend-tag"
      >
        <el-tag type="warning" size="small" class="length-tag">
          前置: {{ local.prependLengthFieldType }}
        </el-tag>
      </div>

      <div class="field-actions" @click.stop>
        <slot name="actions"/>
      </div>

    </div>
    <!-- 编辑器面板 -->
    <div v-show="isExpanded" class="editor">
      <el-form label-width="auto" size="small">
        <el-form-item label="前置">
          <template #label>
            前置
            <el-tooltip content="前置长度字段: 编码时，前置一个用来表示当前字段长度的字段" placement="top">
              <el-icon
                  style="margin-left: 4px; margin-top: 5px;"
                  size="14"
              >
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </template>
          <el-radio-group v-model="local.prependLengthFieldType">
            <el-radio-button label="none" value="none"/>
            <el-radio-button label="u8" value="u8"/>
            <el-radio-button label="u16" value="u16"/>
            <el-radio-button label="u32" value="u32"/>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="名称">
          <template #label>
            名称
            <el-tooltip content="字段名称: 仅仅调试用到(可以随便写)" placement="top">
              <el-icon
                  style="margin-left: 4px; margin-top: 4px;"
                  size="14"
              >
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </template>
          <el-input v-model="local.name" placeholder="用于调试或文档" style="width: 200px"/>
        </el-form-item>

        <slot
            name="editor-value"
            :value="local.value"
            :on-update="(val: any) => local.value = val"
        />

        <el-form-item label="元数据">
          <template #label>
            元数据
            <el-tooltip content="元数据: 调试信息" placement="top">
              <el-icon
                  style="margin-left: 4px; margin-top: 4px;"
                  size="14"
              >
                <InfoFilled/>
              </el-icon>
            </el-tooltip>
          </template>
          <el-input
              v-model="rawExtraInput"
              type="textarea"
              :rows="4"
              placeholder='{ "description": "...", "ui": { "tooltip": "..." } }'
              class="extra-textarea"
              @focus="onExtraFocus"
              @blur="onExtraBlur"
          />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

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
    padding: 8px 10px;
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
          margin: 0 4px;

          :deep(.el-input__wrapper) {
            padding: 0;
          }

          :deep(.el-input__inner) {
            padding: 0 6px !important;
            height: 22px !important;
            line-height: 20px !important;
            font-family: Consolas, Monaco, 'Courier New', monospace;
            //background-color: #f5f5f5 !important;
            //border: 1px solid #dcdfe6 !important;
            border-radius: 4px !important;
            box-sizing: border-box !important;
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
    padding: 12px;
    background-color: #fafafa;
    border-top: 1px solid #eee;
    border-radius: 0 0 6px 6px;

    .extra-textarea {
      font-family: Consolas, Monaco, 'Courier New', monospace;
      font-size: 12px;
    }
  }
}
</style>
