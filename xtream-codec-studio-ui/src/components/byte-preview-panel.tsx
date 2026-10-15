import { Button } from '@heroui/react';
import { ArrowLeft, ArrowRight, Binary } from 'lucide-react';
import { previewBytes, type previewFieldValue } from '../byte-preview';
import type { inspectTraceBits } from './trace-viewer';

export function BytePreviewPanel({
  inspection,
  fieldPath,
  fieldPreview,
  range,
  onInspectByte,
  children,
}: {
  inspection: ReturnType<typeof inspectTraceBits>;
  fieldPath: string;
  fieldPreview: ReturnType<typeof previewFieldValue>;
  range: [number, number] | null;
  onInspectByte: (offset: number) => void;
  children: React.ReactNode;
}) {
  const previews = previewBytes(inspection.rows.map((row) => row.hex));
  const partialByte = range && (range[0] % 8 !== 0 || range[1] % 8 !== 0);
  const byteRange = `[${inspection.windowStart}, ${inspection.windowEnd}) · ${inspection.rows.length} B`;
  return (
    <section className="byte-preview" aria-label="字节预览">
      <div className="byte-preview-heading">
        <Binary size={15} />
        <span>字节预览</span>
        <code className="byte-preview-path" title={fieldPath}>
          {fieldPath}
        </code>
        <span className="byte-preview-range">{byteRange}</span>
      </div>
      <div className="byte-preview-toolbar">
        <span>
          {range ? `${range[1] - range[0]} bit · [${range[0]}, ${range[1]})` : '原始字节'}
          {inspection.contentOnly && <small>内容预览 · 不含长度前缀</small>}
          {partialByte && <small>原始字节预览包含完整边界字节</small>}
        </span>
        {inspection.end - inspection.start > 8 && (
          <div className="bit-pagination">
            <Button
              size="sm"
              variant="ghost"
              isIconOnly
              aria-label="查看前一段字段位"
              isDisabled={inspection.windowStart === inspection.start}
              onPress={() => onInspectByte(inspection.windowStart - 8)}
            >
              <ArrowLeft size={14} />
            </Button>
            <span>当前片段</span>
            <Button
              size="sm"
              variant="ghost"
              isIconOnly
              aria-label="查看后一段字段位"
              isDisabled={inspection.windowEnd === inspection.end}
              onPress={() => onInspectByte(inspection.windowEnd)}
            >
              <ArrowRight size={14} />
            </Button>
          </div>
        )}
      </div>
      <div className="byte-preview-grid">
        <div className="byte-preview-card byte-preview-bits">
          <div className="pane-caption">
            <span>Bit 预览</span>
            <small>高位 → 低位</small>
          </div>
          <div className="bit-byte-list">
            {!inspection.rows.length && <p className="muted">内容为空。</p>}
            {inspection.rows.map((row) => (
              <div className="bit-byte-row" key={row.offset}>
                <code title={`BYTE ${row.offset} · ${row.hex}`}>
                  {row.offset} · {row.hex}
                </code>
                <div className="bit-cells">
                  {row.bits.map((cell) => (
                    <span
                      key={cell.bit}
                      className={cell.active ? 'active' : ''}
                      title={`BYTE ${row.offset} · bit ${cell.bit} = ${cell.value}`}
                    >
                      <small>{cell.bit}</small>
                      {cell.value}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
        {(
          [
            ['编码预览', previews.encodings],
            ['数值预览', previews.numbers],
          ] as const
        ).map(([title, rows]) => (
          <div className="byte-preview-card byte-preview-values" key={title}>
            <div className="pane-caption">
              <span>{title}</span>
              {title === '编码预览' && <small>原始字节</small>}
            </div>
            {title === '数值预览' && (
              <>
                {fieldPreview && (
                  <div className="field-value-preview" aria-label="字段值预览">
                    <div className="pane-caption">
                      <span>字段值</span>
                      <small>
                        {fieldPreview.dataType}
                        {fieldPreview.bits !== null && ` · ${fieldPreview.bits} bit`}
                      </small>
                    </div>
                    <strong className="field-value-main">{fieldPreview.value}</strong>
                    {fieldPreview.hex !== null && (
                      <dl>
                        <div>
                          <dt>HEX</dt>
                          <dd>{fieldPreview.hex}</dd>
                        </div>
                        <div>
                          <dt>BIN</dt>
                          <dd>{fieldPreview.binary}</dd>
                        </div>
                      </dl>
                    )}
                  </div>
                )}
                <div className="pane-caption">
                  <span>原始字节数值</span>
                </div>
              </>
            )}
            <div className="byte-preview-scope">{byteRange}</div>
            <dl>
              {rows.map(([label, value]) => (
                <div key={label}>
                  <dt>{label}</dt>
                  <dd>{value}</dd>
                </div>
              ))}
            </dl>
          </div>
        ))}
      </div>
      {children}
    </section>
  );
}
