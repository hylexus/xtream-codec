import { useLayoutEffect, useMemo, useRef, useState, type CSSProperties } from 'react';
import { Alert, Button, Tabs } from '@heroui/react';
import {
  ArrowLeft,
  ArrowRight,
  CheckCheck,
  ChevronDown,
  ChevronRight,
  Copy,
  ScanLine,
} from 'lucide-react';
import type { CodecResult, TraceNode } from '../types';
import { pretty } from '../model';
import { previewFieldValue } from '../byte-preview';
import { CodeEditor } from './code-editor';
import { ResizableSplit } from './resizable-split';
import { BytePreviewPanel } from './byte-preview-panel';

interface Props {
  result: CodecResult | null;
  theme: 'dark' | 'light';
  onReuse: () => void;
  onNotice: (text: string) => void;
}
function bitRange(node: TraceNode): [number, number] | null {
  if (node.bitStart != null && node.bitEnd != null) return [node.bitStart, node.bitEnd];
  return node.byteStart != null && node.byteEnd != null
    ? [node.byteStart * 8, node.byteEnd * 8]
    : null;
}

export function previewBitRange(node: TraceNode): [number, number] | null {
  const { contentBitStart, contentBitEnd } = node.attributes;
  return typeof contentBitStart === 'number' && typeof contentBitEnd === 'number'
    ? [contentBitStart, contentBitEnd]
    : bitRange(node);
}

export function traceDataType(node: TraceNode): string {
  const dataType = node.attributes.dataType;
  if (typeof dataType === 'string' && dataType.trim()) return dataType.trim();
  return node.javaType?.replace(/(?:[\w$]+\.)+/g, '') || node.kind;
}

const TRACE_TYPE_COLORS = ['blue', 'violet', 'green', 'amber', 'cyan'] as const;

export function traceColor(node: TraceNode): string {
  if (node.status === 'ERROR' || node.status === 'FAILED') return 'var(--studio-red)';
  const type = traceDataType(node);
  // FNV-1a 只依赖显示类型，避免节点职责、顺序或当前结果影响颜色。
  let hash = 2166136261;
  for (let i = 0; i < type.length; i++) {
    hash = Math.imul(hash ^ type.charCodeAt(i), 16777619) >>> 0;
  }
  return `var(--studio-${TRACE_TYPE_COLORS[hash % TRACE_TYPE_COLORS.length]})`;
}

export function inspectTraceBits(node: TraceNode | undefined, bytes: string[], byteOffset: number) {
  const range = node ? previewBitRange(node) : null;
  const contentOnly = typeof node?.attributes.contentBitStart === 'number';
  const hasRange = range && (contentOnly || range[0] < range[1]);
  const start = hasRange ? Math.floor(range[0] / 8) : byteOffset;
  const end = Math.min(bytes.length, hasRange ? Math.ceil(range[1] / 8) : byteOffset + 1);
  // 每次最多展示 8 个字节；点击 HEX 中的字节时定位到它所在的分段。
  const current = Math.max(start, Math.min(byteOffset, end - 1));
  const windowStart = start + Math.floor((current - start) / 8) * 8;
  const windowEnd = Math.min(windowStart + 8, end);
  const rows = bytes.slice(windowStart, windowEnd).map((hex, index) => {
    const offset = windowStart + index;
    const value = parseInt(hex, 16);
    return {
      offset,
      hex: hex.toUpperCase(),
      bits: Array.from({ length: 8 }, (_v, i) => {
        const bit = 7 - i;
        // Trace 偏移按位序累计；lsb0 从物理低位开始，界面仍按高位到低位排列。
        const position = offset * 8 + (node?.attributes.bitOrder === 'lsb0' ? bit : i);
        return {
          bit,
          // 将目标位移到最低位，再用 & 1 屏蔽其余位，得到该位的 0 或 1。
          value: (value >> bit) & 1,
          active: !!range && position >= range[0] && position < range[1],
        };
      }),
    };
  });
  return { start, end, windowStart, windowEnd, rows, range, contentOnly };
}

export function TraceViewer({ result, theme, onReuse, onNotice }: Props) {
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [selectedByte, setSelectedByte] = useState<number | null>(null);
  const [page, setPage] = useState(0);
  const [collapsed, setCollapsed] = useState<Set<string>>(new Set());
  const traceTreeRef = useRef<HTMLDivElement>(null);
  const [locateRequest, setLocateRequest] = useState(0);
  useLayoutEffect(() => {
    const tree = traceTreeRef.current;
    const row = tree?.querySelector<HTMLElement>('.trace-row.selected');
    if (!tree?.clientHeight || !row) return;
    // 停止上一次定位，避免新选中行已可见时仍继续向旧目标滚动。
    tree.scrollTo({ top: tree.scrollTop, behavior: 'instant' });
    // 只滚动树的纵向位置，并为固定表头留出空间。
    const viewport = tree.getBoundingClientRect();
    const headingHeight =
      tree.querySelector('.trace-tree-heading')?.getBoundingClientRect().height || 0;
    const top = viewport.top + tree.clientTop + headingHeight;
    const bottom = viewport.top + tree.clientTop + tree.clientHeight;
    if (bottom <= top) return;
    const bounds = row.getBoundingClientRect();
    const delta = bounds.top < top ? bounds.top - top : Math.max(0, bounds.bottom - bottom);
    if (delta) {
      tree.scrollTo({
        top: tree.scrollTop + delta,
        behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches
          ? 'instant'
          : 'smooth',
      });
    }
  }, [locateRequest]);
  const parentIds = useMemo(
    () =>
      new Map(
        result?.trace.nodes.flatMap((node) =>
          node.children.map((child) => [child.id, node.id] as const),
        ),
      ),
    [result],
  );
  if (!result)
    return (
      <section className="panel result-empty">
        <span className="empty-orbit">
          <ScanLine size={26} />
        </span>
        <div>
          <h2>每一个字节，都有迹可循</h2>
          <p>执行编码或解码，在这里查看 HEX、字段边界和逐位跟踪。</p>
        </div>
        <span className="quiet-label">TRACE EXPLORER</span>
      </section>
    );
  const nodes = result.trace?.nodes || [];
  const selected = nodes.find((node) => node.id === selectedId);
  const range = selected ? previewBitRange(selected) : null;
  const fieldPreview = previewFieldValue(selected, selected ? traceDataType(selected) : '', range);
  const bytes = result.hex.match(/.{2}/g) || [];
  const inspection =
    selectedByte !== null || typeof selected?.attributes.contentBitStart === 'number'
      ? inspectTraceBits(selected, bytes, selectedByte ?? Math.floor((range?.[0] || 0) / 8))
      : null;
  const related =
    selectedByte === null
      ? []
      : nodes.filter((node) =>
          result.trace.nodeIdsByByteOffset[String(selectedByte)]?.includes(node.id),
        );
  const toggleNode = (id: string) =>
    setCollapsed((old) => {
      const next = new Set(old);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  const selectTraceNode = (id: string | null) => {
    setSelectedId(id);
    setLocateRequest((old) => old + 1);
    // 从 HEX 定位隐藏的字段时展开其祖先，保留其他分支的折叠状态。
    setCollapsed((old) => {
      const next = new Set(old);
      for (
        let parent = id ? parentIds.get(id) : undefined;
        parent;
        parent = parentIds.get(parent)
      ) {
        next.delete(parent);
      }
      return next;
    });
  };
  const selectNode = (node: TraceNode) => {
    selectTraceNode(node.id);
    const current = previewBitRange(node);
    if (current && current[0] < current[1]) {
      setSelectedByte(Math.floor(current[0] / 8));
      setPage(Math.floor(current[0] / 8 / 256));
    } else setSelectedByte(null);
  };
  const selectByte = (offset: number) => {
    setSelectedByte(offset);
    const ids = result.trace.nodeIdsByByteOffset[String(offset)] || [];
    const candidates = nodes.filter((node) => ids.includes(node.id));
    selectTraceNode(
      (candidates.find((node) => !node.children.length) || candidates.at(-1))?.id || null,
    );
  };
  const inspectByte = (offset: number) => {
    setSelectedByte(offset);
    setPage(Math.floor(offset / 256));
  };
  const renderNode = (node: TraceNode, depth = 0): React.ReactNode => {
    const bits = bitRange(node);
    const hasChildren = node.children.length > 0;
    const open = !collapsed.has(node.id);
    const label = node.path || node.name;
    const fieldDescription = node.attributes.fieldDesc || node.attributes.displayName;
    const description =
      typeof fieldDescription === 'string' &&
      fieldDescription.trim() !== node.name &&
      fieldDescription.trim() !== label
        ? fieldDescription.trim()
        : null;
    return (
      <div key={node.id}>
        <div
          className={`trace-row ${node.id === selectedId ? 'selected' : ''}`}
          data-kind={node.kind}
          style={{ '--trace-color': traceColor(node) } as CSSProperties}
        >
          {hasChildren ? (
            <Button
              className="trace-toggle"
              variant="ghost"
              isIconOnly
              aria-label={`${open ? '折叠' : '展开'} ${node.path || node.name}`}
              aria-expanded={open}
              style={{ left: `${12 + depth * 14}px` }}
              onPress={() => toggleNode(node.id)}
            >
              {open ? <ChevronDown size={13} /> : <ChevronRight size={13} />}
            </Button>
          ) : null}
          <Button
            className="trace-select"
            variant="ghost"
            onPress={() => selectNode(node)}
            aria-label={`跟踪 ${node.path || node.name}`}
            aria-pressed={node.id === selectedId}
          >
            <span className="trace-name-cell" style={{ paddingLeft: `${20 + depth * 14}px` }}>
              <span className="trace-dot" />
              <span className="trace-name">
                <span className="trace-label" title={label}>
                  {label}
                </span>
                {description && (
                  <small className="trace-description" title={description}>
                    {description}
                  </small>
                )}
              </span>
            </span>
            <span
              className="trace-kind"
              title={`${node.kind} · ${node.attributes.dataType || node.javaType || node.kind}`}
            >
              {traceDataType(node)}
            </span>
            <code className="trace-range" title={bits ? `[${bits[0]}, ${bits[1]}) bit` : undefined}>
              {bits ? `${bits[0]}…${bits[1]} bit` : '—'}
            </code>
            <span className="trace-value" title={node.valueSummary || node.status}>
              {node.valueSummary || node.status}
            </span>
          </Button>
        </div>
        {open && node.children.map((child) => renderNode(child, depth + 1))}
      </div>
    );
  };
  return (
    <section className="panel trace-panel">
      <div className="result-toolbar">
        <div className="heading-title">
          <ScanLine size={15} className="cyan" />
          <h2>执行结果</h2>
          <span className={`status-pill ${result.success ? 'success' : 'failure'}`}>
            {result.success ? '执行成功' : '执行失败'}
          </span>
        </div>
        <div className="result-metrics">
          <span>
            <b>{result.consumedBytes}</b> 字节已消费
          </span>
          <span>
            <b>{result.consumedBits}</b> bit
          </span>
          <span>
            <b>{result.remainingHex.length / 2}</b> 字节剩余
          </span>
          <span>
            <b>{nodes.length}</b> 个跟踪节点
          </span>
        </div>
        {result.checksums.length > 0 && (
          <div className="checksum-list" aria-label="校验和结果">
            {result.checksums.map((item) => (
              <span
                className={`checksum ${item.matched ? 'matched' : 'mismatch'}`}
                key={item.fieldPath}
                title={`${item.fieldPath} · ${item.algorithm} · 实际 ${String(item.actual)} / 期望 ${String(item.expected)} · ${item.matched ? '匹配' : '不匹配'}`}
              >
                {item.matched ? '✓' : '!'} {item.fieldPath} · 实际 {String(item.actual)} / 期望{' '}
                {String(item.expected)}
                {!item.matched && ' · 不匹配'}
              </span>
            ))}
          </div>
        )}
        <div className="actions">
          <Button
            size="sm"
            variant="ghost"
            onPress={async () => {
              try {
                await navigator.clipboard.writeText(result.hex);
                onNotice('已复制原始 HEX');
              } catch {
                onNotice('无法访问剪贴板，请从 HEX 面板手动复制');
              }
            }}
          >
            <Copy size={14} />
            复制 HEX
          </Button>
          <Button size="sm" variant="secondary" isDisabled={!result.values} onPress={onReuse}>
            <CheckCheck size={14} />
            结果作为输入
          </Button>
        </div>
      </div>
      {(result.error || result.trace.diagnostics.length > 0) && (
        <div className="result-notices">
          {result.error && (
            <Alert status="danger" className="error-banner" role="alert">
              <Alert.Content>
                <Alert.Title>{result.error.fieldPath || '执行错误'}</Alert.Title>
                <Alert.Description>
                  {result.error.message}
                  {result.error.bitOffset != null && <span> · bit {result.error.bitOffset}</span>}
                </Alert.Description>
              </Alert.Content>
            </Alert>
          )}
          {result.trace.diagnostics.map((item, index) => (
            <Alert
              status={item.level === 'ERROR' ? 'danger' : 'warning'}
              className={item.level === 'ERROR' ? 'error-banner' : 'warning-banner'}
              role="alert"
              key={index}
            >
              <Alert.Content>
                <Alert.Description>
                  {item.level} · {item.message}
                </Alert.Description>
              </Alert.Content>
            </Alert>
          ))}
        </div>
      )}
      <Tabs className="result-tabs" variant="secondary" defaultSelectedKey="trace">
        <Tabs.ListContainer>
          <Tabs.List aria-label="执行结果视图">
            <Tabs.Tab id="trace">
              HEX 与字段跟踪
              <Tabs.Indicator />
            </Tabs.Tab>
            <Tabs.Tab id="values">
              字段值
              <Tabs.Indicator />
            </Tabs.Tab>
            <Tabs.Tab id="response">
              完整响应
              <Tabs.Indicator />
            </Tabs.Tab>
          </Tabs.List>
        </Tabs.ListContainer>
        <Tabs.Panel id="trace">
          <ResizableSplit name="trace" label="调整 HEX 与字段跟踪宽度" className="trace-grid">
            <div className="hex-pane">
              <div className="pane-caption">
                <span>HEX DUMP</span>
                <small>点击字节定位字段</small>
              </div>
              <div
                className="hex-pane-content"
                role="region"
                aria-label="HEX 与字节预览"
                tabIndex={0}
              >
                <div className="hex-dump">
                  <div className="hex-table">
                    <div className="hex-columns">
                      <span className="hex-corner">OFFSET</span>
                      {Array.from({ length: 16 }, (_v, i) => (
                        <span className="hex-coordinate" key={i}>
                          {i.toString(16).toUpperCase().padStart(2, '0')}
                        </span>
                      ))}
                    </div>
                    {Array.from(
                      {
                        length: Math.ceil(
                          Math.min(256, Math.max(0, bytes.length - page * 256)) / 16,
                        ),
                      },
                      (_v, row) => {
                        const start = page * 256 + row * 16;
                        return (
                          <div className="hex-row" key={start}>
                            <span className="hex-offset">
                              {start.toString(16).padStart(4, '0')}
                            </span>
                            {bytes.slice(start, start + 16).map((byte, i) => {
                              const offset = start + i;
                              const highlighted =
                                range && offset * 8 < range[1] && (offset + 1) * 8 > range[0];
                              return (
                                <Button
                                  key={offset}
                                  className={`hex-byte ${highlighted ? 'highlighted' : ''} ${offset === selectedByte ? 'active' : ''}`}
                                  variant="ghost"
                                  aria-label={`字节 ${offset}: ${byte}`}
                                  aria-pressed={offset === selectedByte}
                                  onPress={() => selectByte(offset)}
                                >
                                  {byte.toUpperCase()}
                                </Button>
                              );
                            })}
                          </div>
                        );
                      },
                    )}
                  </div>
                  {!bytes.length && <p className="muted">本次执行尚未产生字节。</p>}
                </div>
                {bytes.length > 256 && (
                  <div className="hex-pagination">
                    <Button
                      size="sm"
                      variant="ghost"
                      isDisabled={!page}
                      onPress={() => setPage(page - 1)}
                    >
                      <ArrowLeft size={14} />
                    </Button>
                    <span>
                      {page + 1} / {Math.ceil(bytes.length / 256)}
                    </span>
                    <Button
                      size="sm"
                      variant="ghost"
                      isDisabled={(page + 1) * 256 >= bytes.length}
                      onPress={() => setPage(page + 1)}
                    >
                      <ArrowRight size={14} />
                    </Button>
                  </div>
                )}
                {inspection && (inspection.rows.length > 0 || inspection.contentOnly) && (
                  <BytePreviewPanel
                    inspection={inspection}
                    fieldPath={selected?.path || selected?.name || '逐位查看'}
                    fieldPreview={fieldPreview}
                    range={inspection.range}
                    onInspectByte={inspectByte}
                  >
                    <div className="related-fields">
                      {related
                        .filter((node) => node.path && node.path !== '$')
                        .map((node) => (
                          <Button
                            size="sm"
                            variant="ghost"
                            key={node.id}
                            onPress={() => selectNode(node)}
                          >
                            {node.path}
                          </Button>
                        ))}
                    </div>
                  </BytePreviewPanel>
                )}
                {result.remainingHex && (
                  <p className="remaining">
                    剩余输入 <code>{result.remainingHex}</code>
                  </p>
                )}
              </div>
            </div>
            <div className="trace-pane">
              <div className="pane-caption">
                <span>FIELD TRACE</span>
                <small>范围为 [起点, 终点)</small>
              </div>
              <div
                className="trace-tree"
                ref={traceTreeRef}
                role="region"
                aria-label="字段跟踪树"
                tabIndex={0}
              >
                <div className="trace-tree-content">
                  <div className="trace-tree-heading">
                    <span>节点</span>
                    <span>类型</span>
                    <span>位范围</span>
                    <span>值</span>
                  </div>
                  {result.trace.root ? (
                    renderNode(result.trace.root)
                  ) : (
                    <p className="muted">暂无跟踪节点。</p>
                  )}
                </div>
                {selected && (
                  <div
                    className="node-detail"
                    data-kind={selected.kind}
                    style={{ '--trace-color': traceColor(selected) } as CSSProperties}
                  >
                    <span
                      className="trace-kind"
                      title={`${selected.kind} · ${selected.attributes.dataType || selected.javaType || selected.kind}`}
                    >
                      {traceDataType(selected)}
                    </span>
                    <strong>{selected.path || selected.name}</strong>
                    <code>{selected.hex}</code>
                    <p>{selected.valueSummary}</p>
                  </div>
                )}
              </div>
            </div>
          </ResizableSplit>
        </Tabs.Panel>
        <Tabs.Panel id="values">
          <CodeEditor
            value={pretty(result.values)}
            theme={theme}
            label="结果字段值"
            readOnly
            height="100%"
          />
        </Tabs.Panel>
        <Tabs.Panel id="response">
          <CodeEditor
            value={pretty(result)}
            theme={theme}
            label="完整执行响应"
            readOnly
            height="100%"
          />
        </Tabs.Panel>
      </Tabs>
    </section>
  );
}
