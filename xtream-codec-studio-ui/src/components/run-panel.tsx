import { Button, Tooltip } from '@heroui/react';
import { useRef } from 'react';
import { Maximize2, Minimize2, Minus, Terminal, X } from 'lucide-react';
import { TraceViewer } from './trace-viewer';
import { isStale } from '../workspace';
import type { DocumentTab } from '../workspace';
import { PanelResizer } from './panel-resizer';
import { DEFAULT_LAYOUT } from '../layout';

export function RunPanel({
  tabs,
  activeId,
  theme,
  height,
  maxHeight,
  maximized,
  pending,
  onHeight,
  onMaximize,
  onClose,
  onNotice,
  onReuse,
}: {
  tabs: DocumentTab[];
  activeId: string;
  theme: 'dark' | 'light';
  height: number;
  maxHeight: number;
  maximized: boolean;
  pending: boolean;
  onHeight: (height: number) => void;
  onMaximize: () => void;
  onClose: () => void;
  onNotice: (message: string) => void;
  onReuse: (id: string) => void;
}) {
  const panel = useRef<HTMLElement>(null);
  const active = tabs.find((tab) => tab.id === activeId);
  return (
    <section
      ref={panel}
      className={`run-dock ${maximized ? 'maximized' : ''}`}
      style={maximized ? undefined : { height }}
      aria-label="运行面板"
    >
      {!maximized && (
        <PanelResizer
          className="run-resize"
          label="调整运行面板高度"
          direction="vertical"
          value={height}
          min={180}
          max={maxHeight}
          step={20}
          reverse
          onPreview={(value) => panel.current?.style.setProperty('height', `${value}px`)}
          onChange={onHeight}
          onReset={() => onHeight(DEFAULT_LAYOUT.run)}
        />
      )}
      <div className="run-dock-heading">
        <div className="run-window-controls" role="group" aria-label="运行面板控制">
          <Tooltip>
            <Button
              className="run-window-button run-window-close"
              size="sm"
              variant="ghost"
              isIconOnly
              aria-label="关闭运行面板"
              onPress={onClose}
            >
              <span className="run-window-dot">
                <X size={10} strokeWidth={2.5} />
              </span>
            </Button>
            <Tooltip.Content>关闭运行面板</Tooltip.Content>
          </Tooltip>
          <Tooltip>
            <Button
              className="run-window-button run-window-collapse"
              size="sm"
              variant="ghost"
              isIconOnly
              aria-label="折叠运行面板"
              onPress={onClose}
            >
              <span className="run-window-dot">
                <Minus size={10} strokeWidth={2.5} />
              </span>
            </Button>
            <Tooltip.Content>折叠运行面板</Tooltip.Content>
          </Tooltip>
          <Tooltip>
            <Button
              className="run-window-button run-window-maximize"
              size="sm"
              variant="ghost"
              isIconOnly
              aria-label={maximized ? '还原运行面板' : '最大化运行面板'}
              onPress={onMaximize}
            >
              <span className="run-window-dot">
                {maximized ? <Minimize2 size={9} /> : <Maximize2 size={9} />}
              </span>
            </Button>
            <Tooltip.Content>{maximized ? '还原运行面板' : '最大化运行面板'}</Tooltip.Content>
          </Tooltip>
        </div>
        <div className="heading-title">
          <Terminal size={16} />
          <strong>运行</strong>
          <span className="muted">{active?.name}</span>
          {pending && <span className="run-status">正在执行…</span>}
          {active?.data.run && isStale(active.data) && (
            <span className="run-stale">输入已变化 · 结果来自上次运行</span>
          )}
        </div>
      </div>
      <div className="run-dock-content">
        {tabs.map((tab) => (
          <div key={tab.id} hidden={tab.id !== activeId}>
            <TraceViewer
              key={tab.data.run?.executedAt || 'empty'}
              result={tab.data.run?.result || null}
              theme={theme}
              onNotice={onNotice}
              onReuse={() => onReuse(tab.id)}
            />
          </div>
        ))}
      </div>
    </section>
  );
}
