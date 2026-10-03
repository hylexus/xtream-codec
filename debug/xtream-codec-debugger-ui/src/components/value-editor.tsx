import { useEffect, useState } from 'react';
import { Button, FieldError, Input, TextArea, TextField } from '@heroui/react';
import { Plus, Trash2, ChevronDown, ChevronRight } from 'lucide-react';
import { defaultValue, isAutomatic, parseJson, parseScalar, pretty } from '../model';
import type { Field, JsonValue, Values } from '../types';

interface Props {
  field: Field;
  value: JsonValue | undefined;
  onChange: (value: JsonValue) => void;
}

export function ScalarInput({ field, value, onChange }: Props) {
  const [text, setText] = useState(String(value ?? ''));
  const [error, setError] = useState('');
  useEffect(() => {
    setText(String(value ?? ''));
    setError('');
  }, [value]);
  return (
    <TextField
      className="scalar-input"
      aria-label={`${field.id} 的值`}
      value={text}
      isInvalid={!!error}
      onChange={(next) => {
        setText(next);
        try {
          onChange(parseScalar(field, next));
          setError('');
        } catch {
          setError('请输入有效数值');
        }
      }}
    >
      <Input
        className={error ? 'invalid-input' : ''}
        placeholder={/^[ui]\d+$/.test(field.type) ? '十进制 / 0x…' : '输入字段值'}
        onBlur={() => {
          if (error) setText(String(value ?? ''));
          setError('');
        }}
      />
      <FieldError className="input-error">{error}</FieldError>
    </TextField>
  );
}

export function ValueEditor({ field, value, onChange }: Props) {
  const [expanded, setExpanded] = useState(true);
  const [raw, setRaw] = useState(pretty(value ?? defaultValue(field)));
  const [error, setError] = useState('');
  useEffect(() => setRaw(pretty(value ?? defaultValue(field))), [value, field]);
  if (isAutomatic(field)) return <span className="auto-value">自动生成 · 无需输入</span>;
  if (field.type === 'struct') {
    const object =
      value && typeof value === 'object' && !Array.isArray(value) ? (value as Values) : {};
    return (
      <div className="value-struct">
        {field.fields?.map((child, index) => (
          <div className="value-property" key={`${child.id}-${index}`}>
            <span className="value-label">
              {child.name || child.id}
              <code>{child.id}</code>
            </span>
            <ValueEditor
              field={child}
              value={object[child.id]}
              onChange={(next) => onChange({ ...object, [child.id]: next })}
            />
          </div>
        ))}
      </div>
    );
  }
  if (field.type === 'array' && field.item) {
    const items = Array.isArray(value) ? value : [];
    const item = field.item;
    return (
      <div className="value-array">
        <div className="array-heading">
          <Button
            size="sm"
            variant="ghost"
            onPress={() => setExpanded(!expanded)}
            aria-label={`${field.id} ${expanded ? '折叠' : '展开'}数组值`}
            aria-expanded={expanded}
          >
            {expanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
            <span>{items.length} 个元素</span>
          </Button>
          <Button
            size="sm"
            variant="ghost"
            onPress={() => onChange([...items, defaultValue(item)])}
          >
            <Plus size={14} />
            添加元素
          </Button>
        </div>
        {expanded &&
          items.map((current, index) => (
            <div className="array-element" key={index}>
              <span className="array-index">{String(index).padStart(2, '0')}</span>
              <ValueEditor
                field={item}
                value={current}
                onChange={(next) => onChange(items.map((old, i) => (i === index ? next : old)))}
              />
              <Button
                size="sm"
                variant="ghost"
                isIconOnly
                aria-label={`${field.id} 删除元素 ${index}`}
                onPress={() => onChange(items.filter((_old, i) => i !== index))}
              >
                <Trash2 size={13} />
              </Button>
            </div>
          ))}
      </div>
    );
  }
  if (['bytes', 'tlv', 'map', 'pair', 'choice', 'extension'].includes(field.type)) {
    return (
      <TextField
        className="complex-value"
        aria-label={`${field.id} 的值`}
        value={raw}
        isInvalid={!!error}
        onChange={(next) => {
          setRaw(next);
          try {
            onChange(parseJson(next));
            setError('');
          } catch {
            setError('JSON 尚未完成');
          }
        }}
      >
        <TextArea
          rows={3}
          spellCheck={false}
          onBlur={() => {
            if (error) {
              setRaw(pretty(value ?? defaultValue(field)));
              setError('');
            }
          }}
        />
        <FieldError className="input-error">{error}</FieldError>
      </TextField>
    );
  }
  return <ScalarInput field={field} value={value} onChange={onChange} />;
}
