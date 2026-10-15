import { useRef, useState } from 'react';
import {
  Alert,
  Button,
  Description,
  Dropdown,
  Input,
  Label,
  ListBox,
  SearchField,
  Select,
  Tabs,
} from '@heroui/react';
import {
  ArrowDownToLine,
  ArrowLeftRight,
  ArrowRight,
  Binary,
  Braces,
  ChevronDown,
  Code2,
  Download,
  FlaskConical,
  Layers3,
  Plus,
  Upload,
  X,
} from 'lucide-react';
import { codecApi } from '../api';
import {
  addField,
  createField,
  duplicateField,
  getAt,
  moveField,
  parseSchema,
  parseValues,
  pathKey,
  pretty,
  removeField,
  replaceField,
  setAt,
  uniqueId,
} from '../model';
import { PRESETS } from '../presets';
import type { Draft, Entity, Field, JsonValue, Path } from '../types';
import { FieldDialog } from './field-dialog';
import { CodeEditor } from './code-editor';
import { SchemaEditor } from './schema-editor';
import { ResizableSplit } from './resizable-split';

import type { DocumentData } from '../workspace';
import type { Preset } from '../presets';
interface Props {
  data: DocumentData;
  theme: 'dark' | 'light';
  entities: Entity[];
  extensions: string[];
  onChange: (update: (data: DocumentData) => DocumentData) => void;
  onRun: (direction: 'encode' | 'decode', submitted: DocumentData) => Promise<void>;
  onPreset: (preset: Preset) => void;
  notify: (message: string) => void;
}
export function DocumentEditor({
  data,
  theme,
  entities,
  extensions,
  onChange,
  onRun,
  onPreset,
  notify,
}: Props) {
  const { mode, draft, schemaText, schemaTab } = data;
  const [entitySearch, setEntitySearch] = useState('');
  const [pending, setPending] = useState<'encode' | 'decode' | null>(null);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState<{ path: Path; field: Field; isNew: boolean } | null>(null);
  const [showPresets, setShowPresets] = useState(false);
  const fileInput = useRef<HTMLInputElement>(null);
  const setSchemaText = (schemaText: string) => onChange((old) => ({ ...old, schemaText }));
  const setSchemaTab = (schemaTab: string) =>
    onChange((old) => ({ ...old, schemaTab: schemaTab as 'visual' | 'json' }));
  const update = (changes: Partial<Draft>) =>
    onChange((old) => ({ ...old, draft: { ...old.draft, ...changes } }));
  let values = {};
  let valuesError = '';
  try {
    values = parseValues(draft.values);
  } catch (cause) {
    valuesError = (cause as Error).message;
  }
  const updateSchema = (schema: Draft['schema'], nextValues?: JsonValue) => {
    parseSchema(pretty(schema));
    update({ schema, ...(nextValues ? { values: pretty(nextValues) } : {}) });
    setSchemaText(pretty(schema));
    setError('');
  };
  const report = (cause: unknown) =>
    setError(cause instanceof Error ? cause.message : String(cause));
  const safely = (action: () => void) => {
    try {
      action();
    } catch (cause) {
      report(cause);
    }
  };
  const switchSchemaTab = (next: string) =>
    safely(() => {
      if (next === 'visual' && schemaTab === 'json') updateSchema(parseSchema(schemaText));
      setSchemaTab(next);
    });
  const execute = async (direction: 'encode' | 'decode') => {
    setError('');
    try {
      const submitted =
        schemaTab === 'json' && mode !== 'entity'
          ? { ...data, draft: { ...draft, schema: parseSchema(schemaText) } }
          : data;
      setPending(direction);
      await onRun(direction, submitted);
    } catch (cause) {
      report(cause);
    } finally {
      setPending(null);
    }
  };
  const saveField = (field: Field) => {
    if (!editing) return;
    const currentValues = parseValues(draft.values);
    if (editing.isNew) {
      const parent = editing.path;
      const fields = getAt<Field[]>(draft.schema, parent) || [];
      if (
        fields.some((old) => old.id === field.id && pretty(old.versions) === pretty(field.versions))
      )
        throw new Error('同一作用域内字段标识重复');
      const next = addField(draft.schema, currentValues, parent, field);
      updateSchema(next.schema, next.values);
    } else {
      const parent = getAt<Field[]>(draft.schema, editing.path.slice(0, -1));
      if (
        Array.isArray(parent) &&
        parent.some(
          (old, index) =>
            index !== editing.path.at(-1) &&
            old.id === field.id &&
            pretty(old.versions) === pretty(field.versions),
        )
      )
        throw new Error('同一作用域内字段标识重复');
      const next = replaceField(draft.schema, currentValues, editing.path, field);
      updateSchema(next.schema, next.values);
    }
    setEditing(null);
  };
  const exportSchema = () =>
    safely(() => {
      const schema = schemaTab === 'json' ? parseSchema(schemaText) : draft.schema;
      const url = URL.createObjectURL(new Blob([pretty(schema)], { type: 'application/json' }));
      const link = document.createElement('a');
      link.href = url;
      link.download = 'codec-schema.json';
      link.click();
      URL.revokeObjectURL(url);
      notify('已导出结构定义');
    });
  const CurrentIcon = mode === 'entity' ? Braces : mode === 'field' ? FlaskConical : Layers3;
  return (
    <>
      {error && (
        <Alert status="danger" className="error-banner dismissible" role="alert">
          <Alert.Content>
            <Alert.Description>{error}</Alert.Description>
          </Alert.Content>
          <Button
            size="sm"
            isIconOnly
            variant="ghost"
            aria-label="关闭错误提示"
            onPress={() => setError('')}
          >
            <X size={15} />
          </Button>
        </Alert>
      )}
      <ResizableSplit
        name="editor"
        label="调整结构与输入宽度"
        minSize={340}
        className="editor-grid"
      >
        <section
          className={`panel structure-panel ${schemaTab === 'json' && mode !== 'entity' ? 'structure-json' : ''}`}
        >
          <div className="panel-heading">
            <div className="heading-title">
              <span className="section-icon violet">
                <CurrentIcon size={17} />
              </span>
              <h2>
                {mode === 'entity' ? '实体目录' : mode === 'field' ? '试验结构' : '结构与字段值'}
              </h2>
              {mode !== 'entity' && (
                <span className="count-badge">{draft.schema.fields.length}</span>
              )}
            </div>
            {mode !== 'entity' && (
              <div className="actions">
                <Button
                  size="sm"
                  variant="ghost"
                  isIconOnly
                  aria-label="导入结构 JSON"
                  onPress={() => fileInput.current?.click()}
                >
                  <Upload size={15} />
                </Button>
                <Button
                  size="sm"
                  variant="ghost"
                  isIconOnly
                  aria-label="导出结构 JSON"
                  onPress={exportSchema}
                >
                  <Download size={15} />
                </Button>
                <Button
                  size="sm"
                  variant="secondary"
                  isDisabled={schemaTab === 'json'}
                  onPress={() => {
                    const fields = draft.schema.fields;
                    setEditing({
                      path: ['fields'],
                      field: createField('u8', uniqueId(fields)),
                      isNew: true,
                    });
                  }}
                >
                  <Plus size={14} />
                  添加字段
                </Button>
              </div>
            )}
          </div>
          {mode === 'entity' ? (
            <div className="entity-panel">
              <SearchField value={entitySearch} onChange={setEntitySearch}>
                <Label>搜索实体</Label>
                <SearchField.Group>
                  <SearchField.SearchIcon />
                  <SearchField.Input placeholder="名称、目录标识或类名" />
                  <SearchField.ClearButton aria-label="清除实体搜索" />
                </SearchField.Group>
              </SearchField>
              <Select
                className="field-select"
                fullWidth
                placeholder="请选择实体"
                value={draft.entityId || null}
                onChange={(key) => update({ entityId: key === null ? '' : String(key) })}
              >
                <Label>已注册实体</Label>
                <Select.Trigger>
                  <Select.Value>
                    {({ defaultChildren }) =>
                      draft.entityId
                        ? `${entities.find((entity) => entity.id === draft.entityId)?.name || draft.entityId} · ${draft.entityId}`
                        : defaultChildren
                    }
                  </Select.Value>
                  <Select.ClearButton aria-label="清除实体选择" />
                  <Select.Indicator />
                </Select.Trigger>
                <Select.Popover className="field-options">
                  <ListBox
                    items={entities.filter((entity) =>
                      `${entity.id} ${entity.className} ${entity.name || ''}`
                        .toLowerCase()
                        .includes(entitySearch.toLowerCase()),
                    )}
                    renderEmptyState={() => (
                      <p className="field-options-empty">
                        {entities.length ? '没有匹配的实体' : '暂无已注册实体'}
                      </p>
                    )}
                  >
                    {(entity) => (
                      <ListBox.Item
                        id={entity.id}
                        textValue={`${entity.name || entity.id} · ${entity.id}`}
                      >
                        <Label>
                          {entity.name || entity.id} · {entity.id}
                        </Label>
                        <ListBox.ItemIndicator />
                      </ListBox.Item>
                    )}
                  </ListBox>
                </Select.Popover>
              </Select>
              <Button
                variant="secondary"
                isDisabled={!draft.entityId || !!pending}
                onPress={async () => {
                  try {
                    update({ values: pretty(await codecApi.template(draft.entityId)) });
                    notify('已载入实体初始值');
                  } catch (cause) {
                    report(cause);
                  }
                }}
              >
                <ArrowDownToLine size={15} />
                载入初始值
              </Button>
              <div className="entity-summary">
                <Braces size={25} />
                <strong>{draft.entityId || '选择一个实体'}</strong>
                <code>{entities.find((entity) => entity.id === draft.entityId)?.className}</code>
                <p>使用实体注解定义的布局，支持目标版本和字段跟踪。</p>
              </div>
            </div>
          ) : (
            <>
              <div className="structure-toolbar">
                <Tabs
                  variant="secondary"
                  selectedKey={schemaTab}
                  onSelectionChange={(key) => switchSchemaTab(String(key))}
                >
                  <Tabs.ListContainer>
                    <Tabs.List aria-label="结构编辑方式">
                      <Tabs.Tab id="visual">
                        <Layers3 size={14} />
                        可视化
                        <Tabs.Indicator />
                      </Tabs.Tab>
                      <Tabs.Tab id="json">
                        <Code2 size={14} />
                        DSL JSON
                        <Tabs.Indicator />
                      </Tabs.Tab>
                    </Tabs.List>
                  </Tabs.ListContainer>
                </Tabs>
                <Dropdown isOpen={showPresets} onOpenChange={setShowPresets}>
                  <Dropdown.Trigger
                    className="button button--sm button--ghost preset-trigger"
                    isDisabled={!!pending}
                  >
                    示例
                    <ChevronDown size={13} />
                  </Dropdown.Trigger>
                  <Dropdown.Popover className="preset-popover" placement="bottom end">
                    <Dropdown.Menu
                      aria-label="示例协议"
                      onAction={(key) => {
                        const preset = PRESETS.find((item) => item.id === key);
                        if (!preset) return;
                        onPreset(preset);
                        setShowPresets(false);
                        setError('');
                      }}
                    >
                      {PRESETS.map((preset) => (
                        <Dropdown.Item id={preset.id} key={preset.id} textValue={preset.title}>
                          <Label>{preset.title}</Label>
                          <Description>{preset.description}</Description>
                        </Dropdown.Item>
                      ))}
                    </Dropdown.Menu>
                  </Dropdown.Popover>
                </Dropdown>
              </div>
              {schemaTab === 'json' ? (
                <div className="schema-json-panel">
                  <CodeEditor
                    value={schemaText}
                    onChange={setSchemaText}
                    schema
                    theme={theme}
                    label="结构定义 JSON"
                    height="var(--studio-schema-height, 480px)"
                  />
                  <div className="json-footer">
                    <span>Ctrl + Space 补全属性与引用</span>
                    <Button
                      size="sm"
                      variant="secondary"
                      onPress={() => safely(() => updateSchema(parseSchema(schemaText)))}
                    >
                      应用定义
                    </Button>
                  </div>
                </div>
              ) : (
                <>
                  {valuesError && (
                    <Alert status="warning" className="warning-banner" role="alert">
                      <Alert.Content>
                        <Alert.Description>
                          编码值 JSON 尚未完成，修正后可继续编辑字段值。
                        </Alert.Description>
                      </Alert.Content>
                    </Alert>
                  )}
                  <SchemaEditor
                    schema={draft.schema}
                    values={values}
                    onValue={(path, value) =>
                      safely(() =>
                        update({
                          values: pretty(setAt(parseValues(draft.values), path, value)),
                        }),
                      )
                    }
                    onEdit={(path) =>
                      setEditing({
                        path,
                        field: getAt<Field>(draft.schema, path)!,
                        isNew: false,
                      })
                    }
                    onAdd={(path) =>
                      setEditing({
                        path,
                        field: createField(
                          'u8',
                          uniqueId(getAt<Field[]>(draft.schema, path) || []),
                        ),
                        isNew: true,
                      })
                    }
                    onMove={(path, index) =>
                      safely(() => updateSchema(moveField(draft.schema, path, index)))
                    }
                    onRemove={(path) =>
                      safely(() => {
                        const next = removeField(draft.schema, parseValues(draft.values), path);
                        updateSchema(next.schema, next.values);
                      })
                    }
                    onDuplicate={(path) =>
                      safely(() => {
                        const next = duplicateField(draft.schema, parseValues(draft.values), path);
                        updateSchema(next.schema, next.values);
                      })
                    }
                  />
                </>
              )}
            </>
          )}
        </section>
        <ResizableSplit
          name="input"
          label="调整编码与解码高度"
          direction="vertical"
          minSize={160}
          className="execution-column"
        >
          <section className="panel values-panel">
            <div className="panel-heading">
              <div className="heading-title">
                <span className="section-icon green">
                  <Braces size={17} />
                </span>
                <h2>编码值</h2>
              </div>
              <span className="quiet-label">JSON INPUT</span>
            </div>
            <CodeEditor
              value={draft.values}
              onChange={(value) => update({ values: value })}
              theme={theme}
              label="编码值 JSON"
              height="var(--studio-values-height, 260px)"
            />
            <div className="execution-footer">
              <span>按字段标识传值</span>
              <Button
                size="sm"
                className="encode-button"
                isDisabled={!!pending}
                isPending={pending === 'encode'}
                onPress={() => void execute('encode')}
              >
                <ArrowRight size={16} />
                编码为 HEX
              </Button>
            </div>
          </section>
          <section className="panel hex-input-panel">
            <div className="panel-heading">
              <div className="heading-title">
                <span className="section-icon amber">
                  <Binary size={17} />
                </span>
                <h2>解码字节</h2>
              </div>
              <span className="quiet-label">HEX INPUT</span>
            </div>
            <CodeEditor
              language="hex"
              value={draft.hex}
              onChange={(hex) => update({ hex })}
              theme={theme}
              label="解码 HEX"
              height="100%"
              placeholder="输入原始 HEX，允许空白与换行"
            />
            <div className="execution-footer">
              <span>{draft.hex.replace(/\s/g, '').length / 2} 字节 · 原始报文</span>
              <Button
                size="sm"
                className="decode-button"
                variant="secondary"
                isDisabled={!!pending}
                isPending={pending === 'decode'}
                onPress={() => void execute('decode')}
              >
                <ArrowLeftRight size={16} />
                解码为字段值
              </Button>
            </div>
          </section>
        </ResizableSplit>
      </ResizableSplit>
      <Input
        hidden
        className="schema-file-input"
        type="file"
        accept=".json,application/json"
        ref={fileInput}
        onChange={async (event) => {
          const file = event.target.files?.[0];
          if (!file) return;
          try {
            updateSchema(parseSchema(await file.text()));
            setSchemaTab('visual');
            notify(`已导入 ${file.name}`);
          } catch (cause) {
            report(cause);
          } finally {
            event.target.value = '';
          }
        }}
      />
      {editing && (
        <FieldDialog
          key={`${mode}-${pathKey(editing.path)}`}
          {...editing}
          extensions={extensions}
          theme={theme}
          onSave={saveField}
          onClose={() => setEditing(null)}
        />
      )}
    </>
  );
}
