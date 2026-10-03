import { useState } from 'react';
import { Button } from '@heroui/react';
import {
  ArrowDown,
  ArrowUp,
  ChevronDown,
  ChevronRight,
  Copy,
  GripVertical,
  Plus,
  Settings2,
  Trash2,
} from 'lucide-react';
import { fieldColor, getAt, isAutomatic, pathKey } from '../model';
import type { Field, JsonValue, Path, Schema, Values } from '../types';
import { ValueEditor } from './value-editor';

interface Props {
  schema: Schema;
  values: Values;
  onValue: (path: Path, value: JsonValue) => void;
  onEdit: (path: Path) => void;
  onAdd: (parent: Path) => void;
  onRemove: (path: Path) => void;
  onDuplicate: (path: Path) => void;
  onMove: (path: Path, index: number) => void;
}

export function SchemaEditor(props: Props) {
  const [collapsed, setCollapsed] = useState<Set<string>>(new Set());
  const [dragged, setDragged] = useState<Path | null>(null);
  const toggle = (path: Path) =>
    setCollapsed((old) => {
      const next = new Set(old),
        key = pathKey(path);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  const render = (
    field: Field,
    path: Path,
    valuePath: Path | null,
    index?: number,
    count?: number,
  ): React.ReactNode => {
    const color = fieldColor(field.type);
    const hasChildren =
      !!field.fields || !!field.item || !!field.variants || !!field.defaultVariant;
    const open = !collapsed.has(pathKey(path));
    return (
      <div
        className={`field-card color-${color}`}
        key={pathKey(path)}
        onDragOver={(event) => {
          if (dragged && pathKey(dragged.slice(0, -1)) === pathKey(path.slice(0, -1)))
            event.preventDefault();
        }}
        onDrop={(event) => {
          event.preventDefault();
          event.stopPropagation();
          if (
            dragged &&
            typeof path.at(-1) === 'number' &&
            pathKey(dragged.slice(0, -1)) === pathKey(path.slice(0, -1))
          )
            props.onMove(dragged, Number(path.at(-1)));
          setDragged(null);
        }}
      >
        <div className="field-line">
          {index !== undefined && (
            <span
              className="drag-handle"
              draggable
              onDragStart={(event) => {
                setDragged(path);
                event.dataTransfer.setData('text/plain', pathKey(path));
              }}
              onDragEnd={() => setDragged(null)}
              title="拖拽调整同级顺序"
            >
              <Button
                variant="ghost"
                size="sm"
                isIconOnly
                aria-label={`${field.id} 拖拽调整同级顺序`}
              >
                <GripVertical size={15} />
              </Button>
            </span>
          )}
          {hasChildren && (
            <Button
              size="sm"
              isIconOnly
              variant="ghost"
              aria-label={`${field.id} ${open ? '折叠' : '展开'}`}
              aria-expanded={open}
              onPress={() => toggle(path)}
            >
              {open ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
            </Button>
          )}
          <span className="type-chip">{field.type}</span>
          <div className="field-title">
            <strong>{field.name || field.id}</strong>
            <code>{field.id}</code>
          </div>
          {isAutomatic(field) && <span className="generated-tag">AUTO</span>}
          <div className="field-actions">
            <Button
              size="sm"
              isIconOnly
              variant="ghost"
              aria-label={`${field.id} 编辑字段`}
              onPress={() => props.onEdit(path)}
            >
              <Settings2 size={14} />
            </Button>
            {index !== undefined && (
              <>
                <Button
                  size="sm"
                  isIconOnly
                  variant="ghost"
                  aria-label={`${field.id} 复制字段`}
                  onPress={() => props.onDuplicate(path)}
                >
                  <Copy size={13} />
                </Button>
                <Button
                  size="sm"
                  isIconOnly
                  variant="ghost"
                  aria-label={`${field.id} 上移`}
                  isDisabled={index === 0}
                  onPress={() => props.onMove(path, index - 1)}
                >
                  <ArrowUp size={13} />
                </Button>
                <Button
                  size="sm"
                  isIconOnly
                  variant="ghost"
                  aria-label={`${field.id} 下移`}
                  isDisabled={index === (count || 0) - 1}
                  onPress={() => props.onMove(path, index + 1)}
                >
                  <ArrowDown size={13} />
                </Button>
                <Button
                  size="sm"
                  isIconOnly
                  variant="ghost"
                  aria-label={`${field.id} 删除字段`}
                  onPress={() => props.onRemove(path)}
                >
                  <Trash2 size={13} />
                </Button>
              </>
            )}
          </div>
        </div>
        <div className="field-meta">
          {field.length && (
            <span>
              长度 ·{' '}
              {field.length.ref ??
                field.length.fixed ??
                (field.length.expression
                  ? '表达式'
                  : field.length.remaining
                    ? 'remaining'
                    : `${field.length.prefixBits} bit 前缀`)}{' '}
              {field.length.unit}
            </span>
          )}
          {field.computedLengthOf && <span>回填 · {field.computedLengthOf}</span>}
          {field.versions && <span>版本 · {field.versions.join(' / ')}</span>}
          {field.endian && <span>{field.endian === 'little' ? '小端' : '大端'}</span>}
          {field.type === 'struct' && <span>{field.fields?.length || 0} 个子字段</span>}
        </div>
        {valuePath && !hasChildren && (
          <div className="inline-value">
            <span>值</span>
            <ValueEditor
              field={field}
              value={getAt<JsonValue>(props.values, valuePath)}
              onChange={(next) => props.onValue(valuePath, next)}
            />
          </div>
        )}
        {hasChildren && open && (
          <div className="field-children">
            {field.fields?.map((child, childIndex) =>
              render(
                child,
                [...path, 'fields', childIndex],
                valuePath && [...valuePath, child.id],
                childIndex,
                field.fields!.length,
              ),
            )}
            {field.fields && (
              <Button
                className="add-child"
                size="sm"
                variant="ghost"
                onPress={() => props.onAdd([...path, 'fields'])}
              >
                <Plus size={13} />
                添加子字段
              </Button>
            )}
            {field.item && (
              <>
                <div className="child-label">元素结构 · 每个元素共用</div>
                {render(field.item, [...path, 'item'], null)}
                {valuePath && (
                  <ValueEditor
                    field={field}
                    value={getAt<JsonValue>(props.values, valuePath)}
                    onChange={(next) => props.onValue(valuePath, next)}
                  />
                )}
              </>
            )}
            {field.variants &&
              Object.entries(field.variants).map(([key, child]) => (
                <div key={key}>
                  <div className="child-label">分支 · {key}</div>
                  {render(child, [...path, 'variants', key], null)}
                </div>
              ))}
            {field.defaultVariant && (
              <>
                <div className="child-label">默认分支</div>
                {render(field.defaultVariant, [...path, 'defaultVariant'], null)}
              </>
            )}
            {valuePath && field.variants && (
              <ValueEditor
                field={field}
                value={getAt<JsonValue>(props.values, valuePath)}
                onChange={(next) => props.onValue(valuePath, next)}
              />
            )}
          </div>
        )}
      </div>
    );
  };
  return (
    <div className="schema-tree">
      {props.schema.fields.map((field, index) =>
        render(field, ['fields', index], [field.id], index, props.schema.fields.length),
      )}
      {!props.schema.fields.length && (
        <div className="empty-state">
          <Plus size={28} />
          <strong>从第一个字段开始</strong>
          <p>添加数值、字符串或容器，组合你的协议结构。</p>
        </div>
      )}
      <Button variant="ghost" className="add-field-bottom" onPress={() => props.onAdd(['fields'])}>
        <Plus size={16} />
        添加字段
      </Button>
    </div>
  );
}
