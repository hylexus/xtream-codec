import { useState } from 'react';
import {
  Autocomplete,
  Alert,
  Button,
  Input,
  Label,
  ListBox,
  Modal,
  SearchField,
  Select,
  TextField,
  useFilter,
} from '@heroui/react';
import { Check, SlidersHorizontal } from 'lucide-react';
import { createField, FIELD_TYPES, lengthPrefixSize, parseJson, pretty } from '../model';
import type { Field } from '../types';
import { CodeEditor } from './code-editor';

interface Props {
  field: Field;
  theme: 'dark' | 'light';
  extensions: string[];
  onSave: (field: Field) => void;
  onClose: () => void;
  isNew: boolean;
}

function FieldSelect({
  label,
  value,
  options,
  onChange,
}: {
  label: string;
  value: string;
  options: { id: string; label: string }[];
  onChange: (value: string) => void;
}) {
  return (
    <Select
      className="field-select"
      fullWidth
      value={value}
      onChange={(key) => {
        if (key !== null) onChange(String(key));
      }}
    >
      <Label>{label}</Label>
      <Select.Trigger>
        <Select.Value />
        <Select.Indicator />
      </Select.Trigger>
      <Select.Popover className="field-options">
        <ListBox items={options}>
          {(option) => (
            <ListBox.Item id={option.id} textValue={option.label}>
              <Label>{option.label}</Label>
              <ListBox.ItemIndicator />
            </ListBox.Item>
          )}
        </ListBox>
      </Select.Popover>
    </Select>
  );
}

export function FieldDialog({ field, theme, extensions, onSave, onClose, isNew }: Props) {
  const { contains } = useFilter({ sensitivity: 'base' });
  const [draft, setDraft] = useState(structuredClone(field));
  const [typeSearch, setTypeSearch] = useState('');
  const [tab, setTab] = useState<'form' | 'json'>('form');
  const [jsonText, setJsonText] = useState(pretty(field));
  const [error, setError] = useState('');
  const [extra, setExtra] = useState(
    pretty(
      Object.fromEntries(
        Object.entries(field).filter(
          ([key]) =>
            ![
              'id',
              'name',
              'type',
              'bits',
              'endian',
              'bitOrder',
              'charset',
              'length',
              'versions',
              'fields',
              'item',
              'variants',
              'defaultVariant',
            ].includes(key),
        ),
      ),
    ),
  );
  const change = (key: string, value: unknown) =>
    setDraft((old) => {
      const next = { ...old };
      if (value === undefined || value === '') delete next[key];
      else next[key] = value;
      return next;
    });
  const formField = (): Field => {
    const attributes = parseJson(extra);
    if (!attributes || typeof attributes !== 'object' || Array.isArray(attributes))
      throw new Error('附加属性必须是 JSON 对象');
    const common = Object.fromEntries(
      Object.entries(draft).filter(([key]) =>
        [
          'id',
          'name',
          'type',
          'bits',
          'endian',
          'bitOrder',
          'charset',
          'length',
          'versions',
          'fields',
          'item',
          'variants',
          'defaultVariant',
        ].includes(key),
      ),
    );
    return { ...common, ...attributes, id: draft.id, type: draft.type };
  };
  const save = () => {
    try {
      const next = tab === 'json' ? (parseJson(jsonText) as unknown as Field) : formField();
      if (!next || !/^[A-Za-z_][A-Za-z0-9_]*$/.test(next.id))
        throw new Error('标识需以字母或下划线开头，仅包含字母、数字和下划线');
      onSave(next);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause));
    }
  };
  const length = draft.length;
  const kind = length?.expression
    ? 'expression'
    : ['fixed', 'ref', 'prefixBits', 'remaining'].find(
        (key) => length && length[key as keyof typeof length] !== undefined,
      ) || '';
  const prefixExample = length?.prefixBits === 1 ? 1 : 3;
  const setLength = (newKind: string, value?: string) => {
    if (!newKind) {
      change('length', undefined);
      return;
    }
    if (newKind === 'expression') return;
    change('length', {
      unit: length?.unit || (draft.type === 'array' ? 'item' : 'byte'),
      [newKind]:
        newKind === 'remaining'
          ? true
          : newKind === 'ref'
            ? value || ''
            : Number(value || (newKind === 'prefixBits' ? 8 : 1)),
    });
  };
  return (
    <Modal
      isOpen
      onOpenChange={(open) => {
        if (!open) onClose();
      }}
    >
      <Modal.Backdrop variant="blur">
        <Modal.Container size="lg" scroll="inside">
          <Modal.Dialog className="field-dialog">
            <Modal.CloseTrigger />
            <Modal.Header>
              <span className="dialog-symbol">
                <SlidersHorizontal size={22} />
              </span>
              <div>
                <Modal.Heading>{isNew ? '添加字段' : '编辑字段'}</Modal.Heading>
                <p className="muted">定义字段布局，编码与解码共用同一份结构。</p>
              </div>
            </Modal.Header>
            <Modal.Body>
              <div className="segment">
                <Button
                  size="sm"
                  variant={tab === 'form' ? 'secondary' : 'ghost'}
                  onPress={() => {
                    if (tab === 'json') {
                      try {
                        const next = parseJson(jsonText) as unknown as Field;
                        setDraft(next);
                        setExtra(
                          pretty(
                            Object.fromEntries(
                              Object.entries(next).filter(
                                ([key]) =>
                                  ![
                                    'id',
                                    'name',
                                    'type',
                                    'bits',
                                    'endian',
                                    'bitOrder',
                                    'charset',
                                    'length',
                                    'versions',
                                    'fields',
                                    'item',
                                    'variants',
                                    'defaultVariant',
                                  ].includes(key),
                              ),
                            ),
                          ),
                        );
                        setTab('form');
                        setError('');
                      } catch {
                        setError('请先修正完整字段 JSON');
                      }
                    }
                  }}
                >
                  可视化配置
                </Button>
                <Button
                  size="sm"
                  variant={tab === 'json' ? 'secondary' : 'ghost'}
                  onPress={() => {
                    try {
                      setJsonText(pretty(formField()));
                      setTab('json');
                      setError('');
                    } catch (cause) {
                      setError(String(cause));
                    }
                  }}
                >
                  完整字段 JSON
                </Button>
              </div>
              {tab === 'json' ? (
                <>
                  <CodeEditor
                    value={jsonText}
                    onChange={setJsonText}
                    theme={theme}
                    label="完整字段 JSON"
                    height="420px"
                  />
                  <p className="muted">保存时使用完整 JSON。高级属性会原样保留。</p>
                </>
              ) : (
                <div className="dialog-form">
                  <TextField value={draft.id} onChange={(value) => change('id', value)}>
                    <Label>字段标识</Label>
                    <Input />
                  </TextField>
                  <TextField value={draft.name || ''} onChange={(value) => change('name', value)}>
                    <Label>显示名称</Label>
                    <Input placeholder="可选" />
                  </TextField>
                  <Autocomplete
                    className="field-select"
                    fullWidth
                    value={draft.type}
                    onOpenChange={() => setTypeSearch('')}
                    onChange={(key) => {
                      if (key === null || key === draft.type) return;
                      const type = String(key);
                      const defaults = createField(type, draft.id);
                      const next: Field = {
                        ...defaults,
                        type,
                        ...(draft.name ? { name: draft.name } : {}),
                        ...(draft.versions ? { versions: draft.versions } : {}),
                      };
                      setDraft(next);
                      setExtra(
                        pretty(
                          Object.fromEntries(
                            Object.entries(next).filter(
                              ([key]) =>
                                ![
                                  'id',
                                  'name',
                                  'type',
                                  'bits',
                                  'endian',
                                  'bitOrder',
                                  'charset',
                                  'length',
                                  'versions',
                                  'fields',
                                  'item',
                                  'variants',
                                  'defaultVariant',
                                ].includes(key),
                            ),
                          ),
                        ),
                      );
                    }}
                  >
                    <Label>字段类型</Label>
                    <Autocomplete.Trigger>
                      <Autocomplete.Value />
                      <Autocomplete.Indicator />
                    </Autocomplete.Trigger>
                    <Autocomplete.Popover className="field-options">
                      <Autocomplete.Filter
                        filter={contains}
                        inputValue={typeSearch}
                        onInputChange={setTypeSearch}
                      >
                        <SearchField aria-label="搜索字段类型">
                          <SearchField.Group>
                            <SearchField.SearchIcon />
                            <SearchField.Input placeholder="搜索类型，例如 u12、struct" />
                            <SearchField.ClearButton aria-label="清除搜索" />
                          </SearchField.Group>
                        </SearchField>
                        <ListBox
                          renderEmptyState={() => (
                            <p className="field-options-empty">
                              没有匹配的字段类型，整数位宽为 1–64。
                            </p>
                          )}
                        >
                          {FIELD_TYPES.map((type) => (
                            <ListBox.Item key={type} id={type} textValue={type}>
                              <Label>{type}</Label>
                              <ListBox.ItemIndicator />
                            </ListBox.Item>
                          ))}
                        </ListBox>
                      </Autocomplete.Filter>
                    </Autocomplete.Popover>
                  </Autocomplete>
                  <TextField
                    value={draft.versions?.join(', ') || ''}
                    onChange={(value) =>
                      change(
                        'versions',
                        value.trim() ? value.split(/[,，\s]+/).map(Number) : undefined,
                      )
                    }
                  >
                    <Label>适用版本</Label>
                    <Input placeholder="例如 2013, 2019；留空为默认" />
                  </TextField>
                  <FieldSelect
                    label="字节序"
                    value={draft.endian || 'big'}
                    onChange={(value) => change('endian', value)}
                    options={[
                      { id: 'big', label: '大端 · big' },
                      { id: 'little', label: '小端 · little' },
                    ]}
                  />
                  <FieldSelect
                    label="位序"
                    value={draft.bitOrder || 'msb0'}
                    onChange={(value) => change('bitOrder', value)}
                    options={[
                      { id: 'msb0', label: '高位优先 · msb0' },
                      { id: 'lsb0', label: '低位优先 · lsb0' },
                    ]}
                  />
                  {['float', 'bits'].includes(draft.type) && (
                    <TextField
                      value={String(draft.bits || '')}
                      onChange={(value) => change('bits', Number(value))}
                    >
                      <Label>位宽</Label>
                      <Input type="number" min={1} />
                    </TextField>
                  )}
                  {draft.type === 'string' && (
                    <TextField
                      value={draft.charset || 'UTF-8'}
                      onChange={(value) => change('charset', value)}
                    >
                      <Label>字符集</Label>
                      <Input />
                    </TextField>
                  )}
                  <div className="form-subtitle">长度与边界</div>
                  <FieldSelect
                    label="长度来源"
                    value={kind || 'none'}
                    onChange={(value) => setLength(value === 'none' ? '' : value)}
                    options={[
                      { id: 'none', label: '不声明长度' },
                      { id: 'fixed', label: '固定长度' },
                      { id: 'ref', label: '字段引用' },
                      { id: 'prefixBits', label: '前置长度字段' },
                      { id: 'remaining', label: '容器剩余空间' },
                      ...(kind === 'expression'
                        ? [{ id: 'expression', label: '表达式（完整 JSON 中编辑）' }]
                        : []),
                    ]}
                  />
                  <FieldSelect
                    label={kind === 'prefixBits' ? '前缀值表示' : '长度单位'}
                    value={length?.unit || 'byte'}
                    onChange={(value) =>
                      change('length', { ...(length || { fixed: 1 }), unit: value })
                    }
                    options={[
                      {
                        id: 'byte',
                        label: kind === 'prefixBits' ? '内容字节数 · byte' : '字节 · byte',
                      },
                      { id: 'bit', label: kind === 'prefixBits' ? '内容位数 · bit' : '位 · bit' },
                      {
                        id: 'item',
                        label: kind === 'prefixBits' ? '元素数量 · item' : '元素 · item',
                      },
                    ]}
                  />
                  {kind && !['remaining', 'expression'].includes(kind) && (
                    <TextField
                      className="wide"
                      value={String(length?.[kind as keyof typeof length] ?? '')}
                      onChange={(value) => setLength(kind, value)}
                    >
                      <Label>
                        {kind === 'ref'
                          ? '引用字段标识'
                          : kind === 'prefixBits'
                            ? '长度前缀占用位数（bit）'
                            : '固定长度值'}
                      </Label>
                      <Input />
                    </TextField>
                  )}
                  {kind === 'prefixBits' && length?.prefixBits !== undefined && (
                    <p className="muted wide length-prefix-help">
                      前缀占 {lengthPrefixSize(length.prefixBits)}；值为 {prefixExample} 时，
                      {length?.unit === 'item'
                        ? `后续有 ${prefixExample} 个元素。`
                        : `后续内容占 ${prefixExample} ${length?.unit === 'bit' ? '位' : '字节'}。`}
                    </p>
                  )}
                  <div className="form-subtitle">
                    高级属性 <span>条件、生成规则、派生表达式、填充与扩展</span>
                  </div>
                  <div className="wide">
                    <CodeEditor
                      value={extra}
                      onChange={setExtra}
                      theme={theme}
                      label="字段附加属性 JSON"
                      height="160px"
                    />
                  </div>
                  <p className="muted wide">
                    {draft.fields || draft.item
                      ? '子字段与数组元素在字段树中编辑，也可在完整字段 JSON 中修改。'
                      : '例如：generated、when、expression、computedLengthOf、options。'}{' '}
                    可用扩展：{extensions.join('、') || '无'}。
                  </p>
                </div>
              )}
              {error && (
                <Alert status="danger" className="error-banner" role="alert">
                  <Alert.Content>
                    <Alert.Description>{error}</Alert.Description>
                  </Alert.Content>
                </Alert>
              )}
            </Modal.Body>
            <Modal.Footer>
              <Button variant="ghost" onPress={onClose}>
                取消
              </Button>
              <Button onPress={save}>
                <Check size={16} />
                保存字段
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
