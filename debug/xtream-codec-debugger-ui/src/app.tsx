import { useEffect, useRef, useState } from 'react';
import {
  Alert,
  Button,
  Description,
  Dropdown,
  Input,
  Label,
  Link,
  ListBox,
  SearchField,
  Select,
  Tabs,
  TextArea,
  TextField,
  Toast,
  Tooltip,
  toast,
} from '@heroui/react';
import {
  ArrowDownToLine,
  ArrowLeftRight,
  ArrowRight,
  Binary,
  Braces,
  Check,
  ChevronDown,
  CircleHelp,
  Code2,
  Download,
  FlaskConical,
  Layers3,
  MapPin,
  Moon,
  PanelLeftClose,
  PanelLeftOpen,
  Plus,
  Radio,
  RotateCw,
  Search,
  SlidersHorizontal,
  Sparkles,
  Sun,
  Upload,
  X,
} from 'lucide-react';
import { codecApi } from './api';
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
} from './model';
import { draftFromPreset, initialDrafts, PRESETS } from './presets';
import type { CodecResult, Draft, Entity, Field, JsonValue, Mode, Path } from './types';
import { FieldDialog } from './components/field-dialog';
import { JsonEditor } from './components/json-editor';
import { SchemaEditor } from './components/schema-editor';
import { TraceViewer } from './components/trace-viewer';
import { ThemeColorPicker } from './components/theme-color-picker';

const STORAGE = 'xtream-codec-workbench-v1';
const MODE_INFO = {
  dynamic: { title: '自由组合', icon: Layers3 },
  entity: {
    title: '实体类调试',
    icon: Braces,
  },
  field: {
    title: '单字段试验',
    icon: FlaskConical,
  },
};

function loadState() {
  const defaults = { mode: 'dynamic' as Mode, drafts: initialDrafts(), sidebarCollapsed: false };
  try {
    const raw = localStorage.getItem(STORAGE);
    if (!raw) return defaults;
    const saved = JSON.parse(raw);
    if (!Object.hasOwn(MODE_INFO, saved.mode)) return defaults;
    for (const key of Object.keys(defaults.drafts) as Mode[]) {
      const draft = saved.drafts?.[key];
      if (draft && typeof draft.values === 'string' && typeof draft.hex === 'string') {
        parseSchema(pretty(draft.schema));
        defaults.drafts[key] = { ...defaults.drafts[key], ...draft };
      }
    }
    return {
      mode: saved.mode as Mode,
      drafts: defaults.drafts,
      sidebarCollapsed: saved.sidebarCollapsed === true,
    };
  } catch {
    return defaults;
  }
}

export function App() {
  const [initial] = useState(loadState);
  const [mode, setMode] = useState<Mode>(initial.mode);
  const [drafts, setDrafts] = useState(initial.drafts);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(initial.sidebarCollapsed);
  const [theme, setTheme] = useState<'dark' | 'light'>(() =>
    document.documentElement.dataset.theme === 'light' ? 'light' : 'dark',
  );
  const [entities, setEntities] = useState<Entity[]>([]);
  const [extensions, setExtensions] = useState<string[]>([]);
  const [connection, setConnection] = useState<'connecting' | 'online' | 'offline'>('connecting');
  const [entitySearch, setEntitySearch] = useState('');
  const [pending, setPending] = useState<'encode' | 'decode' | null>(null);
  const [result, setResult] = useState<CodecResult | null>(null);
  const [resultMode, setResultMode] = useState<Mode>('dynamic');
  const [error, setError] = useState('');
  const [schemaTab, setSchemaTab] = useState('visual');
  const [schemaText, setSchemaText] = useState(pretty(drafts[mode].schema));
  const [editing, setEditing] = useState<{ path: Path; field: Field; isNew: boolean } | null>(null);
  const [showPresets, setShowPresets] = useState(false);
  const fileInput = useRef<HTMLInputElement>(null);
  const resultRef = useRef<HTMLDivElement>(null);
  const noticeId = useRef<string | null>(null);
  const notify = (message: string) => {
    if (noticeId.current) toast.close(noticeId.current);
    noticeId.current = toast(message, { timeout: 3500, indicator: <Check size={16} /> });
  };
  const draft = drafts[mode];
  let values = {};
  let valuesError = '';
  try {
    values = parseValues(draft.values);
  } catch (cause) {
    valuesError = (cause as Error).message;
  }
  const update = (changes: Partial<Draft>) =>
    setDrafts((old) => ({ ...old, [mode]: { ...old[mode], ...changes } }));
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
  const connect = async () => {
    setConnection('connecting');
    try {
      const [catalog, allowed] = await Promise.all([codecApi.entities(), codecApi.extensions()]);
      setEntities(catalog);
      setExtensions(allowed);
      setConnection('online');
    } catch {
      setConnection('offline');
    }
  };
  useEffect(() => {
    void connect();
  }, []);
  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.documentElement.classList.toggle('dark', theme === 'dark');
    document.documentElement.classList.toggle('light', theme === 'light');
    try {
      localStorage.setItem('xtream-codec-theme', theme);
    } catch {
      /* 浏览器禁用存储时仍允许编辑。 */
    }
  }, [theme]);
  useEffect(() => {
    const timeout = window.setTimeout(() => {
      try {
        localStorage.setItem(STORAGE, JSON.stringify({ mode, drafts, sidebarCollapsed }));
      } catch {
        /* 浏览器禁用存储时保留当前内存草稿。 */
      }
    }, 350);
    return () => window.clearTimeout(timeout);
  }, [mode, drafts, sidebarCollapsed]);
  const switchMode = (next: Mode) => {
    if (pending) return;
    let currentSchema = draft.schema;
    if (schemaTab === 'json' && mode !== 'entity') {
      try {
        currentSchema = parseSchema(schemaText);
        updateSchema(currentSchema);
      } catch (cause) {
        report(cause);
        return;
      }
    }
    setMode(next);
    setSchemaText(pretty(next === mode ? currentSchema : drafts[next].schema));
    setSchemaTab('visual');
    setError('');
    setShowPresets(false);
    setEditing(null);
  };
  const switchSchemaTab = (next: string) =>
    safely(() => {
      if (next === 'visual' && schemaTab === 'json') updateSchema(parseSchema(schemaText));
      setSchemaTab(next);
    });
  const execute = async (direction: 'encode' | 'decode') => {
    setError('');
    try {
      const current =
        schemaTab === 'json' && mode !== 'entity'
          ? { ...draft, schema: parseSchema(schemaText) }
          : draft;
      if (current !== draft) {
        update({ schema: current.schema });
        setSchemaText(pretty(current.schema));
      }
      setPending(direction);
      const response = await codecApi.execute(mode, direction, current);
      setResult(response);
      setResultMode(mode);
      if (direction === 'encode') update({ hex: response.hex });
      setConnection('online');
      resultRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
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
  const currentInfo = MODE_INFO[mode];
  const CurrentIcon = currentInfo.icon;
  return (
    <div className={`workbench-shell ${sidebarCollapsed ? 'sidebar-collapsed' : ''}`}>
      <aside className="sidebar" id="workbench-sidebar" aria-label="工作空间导航">
        <div className="sidebar-header">
          <Link className="brand" href="/" aria-label="Xtream Codec Studio 首页">
            <span className="brand-symbol">
              <Binary size={23} />
            </span>
            <span className="brand-text">
              xtream<span className="brand-caption">CODEC STUDIO</span>
            </span>
          </Link>
        </div>
        <div className="sidebar-body">
          <div className="sidebar-section-label">工作空间</div>
          <nav aria-label="调试模式">
            {(Object.keys(MODE_INFO) as Mode[]).map((key) => {
              const Icon = MODE_INFO[key].icon;
              return (
                <Tooltip key={key} isDisabled={!sidebarCollapsed} delay={0}>
                  <Button
                    className={`nav-item ${mode === key ? 'active' : ''}`}
                    variant="ghost"
                    onPress={() => switchMode(key)}
                    isDisabled={!!pending}
                    aria-label={MODE_INFO[key].title}
                    aria-current={mode === key ? 'page' : undefined}
                  >
                    <Icon size={18} />
                    <span className="nav-label">{MODE_INFO[key].title}</span>
                    {mode === key && <span className="nav-active-dot" />}
                  </Button>
                  <Tooltip.Content placement="right">{MODE_INFO[key].title}</Tooltip.Content>
                </Tooltip>
              );
            })}
          </nav>
          <div className="sidebar-section-label examples-label">示例协议</div>
          <div className="sidebar-presets">
            {PRESETS.map((preset, index) => {
              const Icon = [Binary, Code2, Sparkles, MapPin][index] || Code2;
              return (
                <Tooltip key={preset.id} isDisabled={!sidebarCollapsed} delay={0}>
                  <Button
                    className="sidebar-preset"
                    variant="ghost"
                    isDisabled={!!pending}
                    aria-label={`载入${preset.title}`}
                    onPress={() => {
                      switchMode('dynamic');
                      const next = draftFromPreset(preset);
                      setDrafts((old) => ({ ...old, dynamic: next }));
                      setSchemaText(pretty(next.schema));
                      setResult(null);
                      notify(`已载入 ${preset.title}`);
                    }}
                  >
                    {sidebarCollapsed ? (
                      <Icon size={18} />
                    ) : (
                      <span className={`preset-dot preset-${index}`} />
                    )}
                    <span className="sidebar-preset-label">
                      {preset.title}
                      <small>{preset.description.split(' · ')[0]}</small>
                    </span>
                  </Button>
                  <Tooltip.Content placement="right">{preset.title}</Tooltip.Content>
                </Tooltip>
              );
            })}
          </div>
          <div className="sidebar-bottom">
            <div className="sidebar-note">
              <span className="note-symbol">
                <Sparkles size={17} />
              </span>
              <strong>一次定义，双向执行</strong>
              <p>结构和值分离，让协议调试回到字段本身。</p>
            </div>
            <Tooltip isDisabled={!sidebarCollapsed} delay={0}>
              <Link
                className="schema-link"
                href="/dynamic-codec-schema.json"
                download
                aria-label="下载 JSON Schema"
              >
                <Download size={15} />
                <span className="schema-link-label">下载 JSON Schema</span>
                <ArrowRight size={13} className="schema-link-arrow" />
              </Link>
              <Tooltip.Content placement="right">下载 JSON Schema</Tooltip.Content>
            </Tooltip>
          </div>
        </div>
        <div className="sidebar-version" aria-label="Xtream Codec 0.11">
          <span className="sidebar-version-name">XTREAM CODEC</span> <span>0.11</span>
        </div>
      </aside>
      <div className="workspace">
        <header className="topbar">
          <div className="topbar-start">
            <Tooltip delay={0}>
              <Button
                className="sidebar-toggle"
                size="sm"
                variant="ghost"
                isIconOnly
                aria-label={sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'}
                aria-expanded={!sidebarCollapsed}
                aria-controls="workbench-sidebar"
                onPress={() => setSidebarCollapsed((collapsed) => !collapsed)}
              >
                {sidebarCollapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
              </Button>
              <Tooltip.Content>{sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'}</Tooltip.Content>
            </Tooltip>
            <div className="breadcrumb">
              <span>开发工具</span>
              <ChevronDown size={12} className="breadcrumb-chevron" />
              <strong>二进制编解码工作台</strong>
            </div>
          </div>
          <div className="topbar-actions">
            <span className="autosave">
              <Check size={12} />
              本地草稿
            </span>
            <ThemeColorPicker />
            <Button
              size="sm"
              isIconOnly
              variant="ghost"
              aria-label={theme === 'dark' ? '切换明亮模式' : '切换暗黑模式'}
              onPress={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
            >
              {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
            </Button>
            <Link
              className="help-link"
              href="https://hylexus.github.io/xtream-codec/guide/core/dynamic/"
              target="_blank"
              rel="noreferrer"
              aria-label="动态 DSL 文档"
            >
              <CircleHelp size={18} />
            </Link>
          </div>
        </header>
        <main className="workbench-main">
          <div className="page-heading">
            <h1>
              {currentInfo.title}
              <span className="heading-dot">.</span>
            </h1>
            <Button
              variant="secondary"
              size="sm"
              onPress={() => resultRef.current?.scrollIntoView({ behavior: 'smooth' })}
            >
              <Radio size={15} />
              查看执行结果
            </Button>
          </div>
          <div className="mobile-modes">
            {(Object.keys(MODE_INFO) as Mode[]).map((key) => (
              <Button
                key={key}
                size="sm"
                variant={mode === key ? 'secondary' : 'ghost'}
                onPress={() => switchMode(key)}
              >
                {MODE_INFO[key].title}
              </Button>
            ))}
          </div>
          <section className="config-bar" aria-label="编译配置与服务状态">
            <div className="config-title">
              <SlidersHorizontal size={15} />
              编译配置
            </div>
            <TextField
              aria-label="目标协议版本"
              value={draft.version}
              onChange={(value) => update({ version: value })}
            >
              <Input placeholder="目标协议版本 · 默认" />
            </TextField>
            {mode !== 'entity' && (
              <span className="schema-version">
                SCHEMA <b>v{draft.schema.schemaVersion}</b>
              </span>
            )}
            <div className="connection-bar">
              <div
                className={`connection-status ${connection}`}
                role="status"
                title={
                  connection === 'offline'
                    ? '启动独立 Java 调试应用后重新连接；离线仍可编辑结构。'
                    : '编解码请求由调试服务执行'
                }
              >
                <span />
                {connection === 'online'
                  ? '服务已连接'
                  : connection === 'connecting'
                    ? '正在连接'
                    : '服务未连接'}
              </div>
              <Button
                size="sm"
                variant="ghost"
                isIconOnly
                isDisabled={connection === 'connecting'}
                aria-label="重新连接服务"
                onPress={() => void connect()}
              >
                <RotateCw size={14} />
              </Button>
            </div>
          </section>
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
          <div className="editor-grid">
            <section className="panel structure-panel">
              <div className="panel-heading">
                <div className="heading-title">
                  <span className="section-icon violet">
                    <CurrentIcon size={17} />
                  </span>
                  <h2>
                    {mode === 'entity'
                      ? '实体目录'
                      : mode === 'field'
                        ? '试验结构'
                        : '结构与字段值'}
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
                    <code>
                      {entities.find((entity) => entity.id === draft.entityId)?.className}
                    </code>
                    <p>使用实体注解定义的布局，支持目标版本和字段跟踪。</p>
                  </div>
                </div>
              ) : (
                <>
                  <div className="structure-toolbar">
                    <Tabs
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
                            const next = draftFromPreset(preset);
                            update(next);
                            setSchemaText(pretty(next.schema));
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
                      <JsonEditor
                        value={schemaText}
                        onChange={setSchemaText}
                        schema
                        theme={theme}
                        label="结构定义 JSON"
                        height="480px"
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
                            const next = duplicateField(
                              draft.schema,
                              parseValues(draft.values),
                              path,
                            );
                            updateSchema(next.schema, next.values);
                          })
                        }
                      />
                    </>
                  )}
                </>
              )}
            </section>
            <div className="execution-column">
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
                <JsonEditor
                  value={draft.values}
                  onChange={(value) => update({ values: value })}
                  theme={theme}
                  label="编码值 JSON"
                  height="260px"
                />
                <div className="execution-footer">
                  <span>按字段标识传值</span>
                  <Button
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
                <TextArea
                  className="hex-input"
                  value={draft.hex}
                  onChange={(event) => update({ hex: event.target.value })}
                  aria-label="解码 HEX"
                  spellCheck={false}
                  placeholder="输入原始 HEX，允许空白与换行"
                />
                <div className="execution-footer">
                  <span>{draft.hex.replace(/\s/g, '').length / 2} 字节 · 原始报文</span>
                  <Button
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
              <div className="workflow-hint">
                <Sparkles size={15} />
                <p>
                  编辑字段与值 → 编码 → 用同一结构解码
                  <br />
                  <span>编码结果自动写入 HEX 输入框。</span>
                </p>
              </div>
            </div>
          </div>
          <div ref={resultRef} className="result-anchor">
            <TraceViewer
              key={result ? `${resultMode}-${result.hex}-${result.success}` : 'empty'}
              result={result}
              theme={theme}
              onNotice={notify}
              onReuse={() => {
                if (!result?.values) return;
                setDrafts((old) => ({
                  ...old,
                  [resultMode]: { ...old[resultMode], values: pretty(result.values) },
                }));
                switchMode(resultMode);
                notify('结果已作为编码输入');
              }}
            />
          </div>
          <footer className="page-footer">
            <span>
              <Binary size={13} />
              Xtream Codec Studio
            </span>
            <span>结构、值与执行分离 · 字节级可观察</span>
            <span>
              <Search size={12} />
              字段路径精确定位
            </span>
          </footer>
        </main>
      </div>
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
      <Toast.Provider
        className="studio-toasts"
        placement="bottom"
        maxVisibleToasts={1}
        width="min(460px, calc(100vw - 32px))"
      />
    </div>
  );
}
