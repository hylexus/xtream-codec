import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';

export const DEFAULT_LAYOUT = { sidebar: 242, editor: 55, input: 62, run: 360, trace: 50 };
export type LayoutKey = keyof typeof DEFAULT_LAYOUT;
const STORAGE_KEY = 'xtream-codec-studio-layout-v1';
const LIMITS: Record<LayoutKey, [number, number]> = {
  sidebar: [184, 420],
  editor: [20, 80],
  input: [15, 85],
  run: [180, 1600],
  trace: [20, 80],
};

export function restoreLayout(value: unknown): typeof DEFAULT_LAYOUT {
  const layout = { ...DEFAULT_LAYOUT };
  if (!value || typeof value !== 'object') return layout;
  for (const key of Object.keys(layout) as LayoutKey[]) {
    const size = (value as Record<string, unknown>)[key];
    if (typeof size === 'number' && Number.isFinite(size)) {
      const [min, max] = LIMITS[key];
      layout[key] = Math.max(min, Math.min(max, size));
    }
  }
  return layout;
}

const LayoutContext = createContext<{
  layout: typeof DEFAULT_LAYOUT;
  setSize: (key: LayoutKey, value: number) => void;
} | null>(null);

export function LayoutProvider({ children }: { children: ReactNode }) {
  const [layout, setLayout] = useState(() => {
    try {
      return restoreLayout(JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null'));
    } catch {
      return { ...DEFAULT_LAYOUT };
    }
  });
  useEffect(() => {
    // 松手或键盘提交尺寸后合并存储写入，拖动预览不进入全局状态。
    const timer = window.setTimeout(() => {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(layout));
      } catch {
        // 浏览器禁用存储时仍允许本次会话调整布局。
      }
    }, 200);
    return () => clearTimeout(timer);
  }, [layout]);
  return (
    <LayoutContext.Provider
      value={{
        layout,
        setSize: (key, value) =>
          setLayout((old) => {
            const next = restoreLayout({ ...old, [key]: value });
            return old[key] === next[key] ? old : next;
          }),
      }}
    >
      {children}
    </LayoutContext.Provider>
  );
}

export function useLayout() {
  return useContext(LayoutContext)!;
}

export function useViewportSize() {
  const [size, setSize] = useState({ width: window.innerWidth, height: window.innerHeight });
  useEffect(() => {
    const update = () => setSize({ width: window.innerWidth, height: window.innerHeight });
    window.addEventListener('resize', update);
    return () => window.removeEventListener('resize', update);
  }, []);
  return size;
}
