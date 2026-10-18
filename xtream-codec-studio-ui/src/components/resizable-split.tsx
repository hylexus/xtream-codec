import { useEffect, useRef, useState, type CSSProperties, type ReactNode } from 'react';
import { DEFAULT_LAYOUT, useLayout } from '../layout';
import { PanelResizer } from './panel-resizer';

export function ResizableSplit({
  name,
  label,
  direction = 'horizontal',
  minSize = 280,
  className = '',
  children,
}: {
  name: 'editor' | 'input' | 'trace';
  label: string;
  direction?: 'horizontal' | 'vertical';
  minSize?: number;
  className?: string;
  children: [ReactNode, ReactNode];
}) {
  const { layout, setSize } = useLayout();
  const container = useRef<HTMLDivElement>(null);
  const [size, setSizeInPixels] = useState(0);
  useEffect(() => {
    const observer = new ResizeObserver(([entry]) => {
      const next = direction === 'horizontal' ? entry.contentRect.width : entry.contentRect.height;
      setSizeInPixels(next);
    });
    observer.observe(container.current!);
    return () => observer.disconnect();
  }, [direction]);
  const available = Math.max(1, size - 8);
  const min = Math.max(name === 'input' ? 15 : 20, Math.min(50, (minSize / available) * 100));
  const ratio = Math.max(min, Math.min(100 - min, layout[name]));
  return (
    <div
      ref={container}
      className={`resizable-split split-${direction} ${className}`}
      style={
        { '--split-first': `${ratio}fr`, '--split-second': `${100 - ratio}fr` } as CSSProperties
      }
    >
      {children[0]}
      <PanelResizer
        label={label}
        direction={direction}
        value={ratio}
        min={min}
        max={100 - min}
        pixelsPerUnit={available / 100}
        step={1}
        onPreview={(value) => {
          container.current?.style.setProperty('--split-first', `${value}fr`);
          container.current?.style.setProperty('--split-second', `${100 - value}fr`);
        }}
        onChange={(value) => setSize(name, value)}
        onReset={() => setSize(name, DEFAULT_LAYOUT[name])}
      />
      {children[1]}
    </div>
  );
}
