import { useEffect, useRef } from 'react';

interface Props {
  label: string;
  direction: 'horizontal' | 'vertical';
  value: number;
  min: number;
  max: number;
  onChange: (value: number) => void;
  onPreview: (value: number) => void;
  onReset: () => void;
  pixelsPerUnit?: number;
  step?: number;
  reverse?: boolean;
  className?: string;
}

export function PanelResizer({
  label,
  direction,
  value,
  min,
  max,
  onChange,
  onPreview,
  onReset,
  pixelsPerUnit = 1,
  step = 10,
  reverse = false,
  className = '',
}: Props) {
  const drag = useRef<{
    node: HTMLDivElement;
    pointerId: number;
    position: number;
    value: number;
    latest: number;
    frame: number | null;
    preview: (value: number) => void;
    commit: (value: number) => void;
  } | null>(null);
  const horizontal = direction === 'horizontal';
  const sign = reverse ? -1 : 1;
  const change = (next: number) => onChange(Math.max(min, Math.min(max, next)));
  const nextValue = (position: number) => {
    const start = drag.current!;
    return Math.max(
      min,
      Math.min(max, start.value + (sign * (position - start.position)) / pixelsPerUnit),
    );
  };
  const finish = (commit: boolean, restorePreview = true) => {
    const current = drag.current;
    if (!current) return;
    drag.current = null;
    if (current.frame !== null) cancelAnimationFrame(current.frame);
    const next = commit ? current.latest : current.value;
    if (restorePreview) current.preview(next);
    current.node.setAttribute('aria-valuenow', String(Math.round(next)));
    delete current.node.dataset.resizing;
    delete document.body.dataset.panelResizing;
    if (current.node.hasPointerCapture(current.pointerId))
      current.node.releasePointerCapture(current.pointerId);
    // 连续位移只改局部样式；结束时才通知全局布局，避免重渲染所有文档。
    if (commit && next !== current.value) current.commit(next);
  };
  useEffect(() => {
    const cancel = () => finish(false);
    window.addEventListener('blur', cancel);
    window.addEventListener('resize', cancel);
    return () => {
      window.removeEventListener('blur', cancel);
      window.removeEventListener('resize', cancel);
      // 卸载时 React 可能已应用折叠或最大化样式，不能用旧尺寸覆盖它。
      finish(false, false);
    };
  }, []);
  return (
    <div
      className={`panel-resizer ${className}`}
      role="separator"
      aria-label={label}
      aria-orientation={horizontal ? 'vertical' : 'horizontal'}
      aria-valuemin={Math.round(min)}
      aria-valuemax={Math.round(max)}
      aria-valuenow={Math.round(value)}
      title={`${label} · 拖动调整，方向键微调，双击恢复默认`}
      tabIndex={0}
      onDoubleClick={onReset}
      onKeyDown={(event) => {
        if (drag.current) {
          if (event.key === 'Escape') {
            event.preventDefault();
            finish(false);
          }
          return;
        }
        const backward = horizontal ? 'ArrowLeft' : 'ArrowUp';
        const forward = horizontal ? 'ArrowRight' : 'ArrowDown';
        if (![backward, forward, 'Home', 'End', 'Enter'].includes(event.key)) return;
        event.preventDefault();
        if (event.key === 'Enter') onReset();
        else if (event.key === 'Home') change(min);
        else if (event.key === 'End') change(max);
        else
          change(value + (event.key === forward ? 1 : -1) * sign * step * (event.shiftKey ? 5 : 1));
      }}
      onPointerDown={(event) => {
        if (event.button !== 0 || !event.isPrimary || drag.current) return;
        event.preventDefault();
        event.currentTarget.focus({ preventScroll: true });
        event.currentTarget.setPointerCapture(event.pointerId);
        event.currentTarget.dataset.resizing = 'true';
        document.body.dataset.panelResizing = direction;
        drag.current = {
          node: event.currentTarget,
          pointerId: event.pointerId,
          position: horizontal ? event.clientX : event.clientY,
          value,
          latest: value,
          frame: null,
          preview: onPreview,
          commit: onChange,
        };
      }}
      onPointerMove={(event) => {
        const current = drag.current;
        if (!current || current.pointerId !== event.pointerId) return;
        current.latest = nextValue(horizontal ? event.clientX : event.clientY);
        // 一帧内合并高频指针事件，只应用最近一次坐标。
        if (current.frame === null)
          current.frame = requestAnimationFrame(() => {
            current.frame = null;
            current.preview(current.latest);
            current.node.setAttribute('aria-valuenow', String(Math.round(current.latest)));
          });
      }}
      onPointerUp={(event) => {
        if (!drag.current || drag.current.pointerId !== event.pointerId) return;
        // 松手可能先于下一帧，必须包含最终坐标而不是最后绘制的尺寸。
        drag.current.latest = nextValue(horizontal ? event.clientX : event.clientY);
        finish(true);
      }}
      onPointerCancel={() => finish(false)}
      onLostPointerCapture={() => finish(false)}
    />
  );
}
