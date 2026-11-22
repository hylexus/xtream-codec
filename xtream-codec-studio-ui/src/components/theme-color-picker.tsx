import { useEffect, useState } from 'react';
import { Button, ColorSwatchPicker, Popover } from '@heroui/react';
import { Palette } from 'lucide-react';

const COLORS = [
  { id: 'violet', name: '紫罗兰', hex: '#7c3aed' },
  { id: 'blue', name: '湛蓝', hex: '#006fee' },
  { id: 'cyan', name: '青色', hex: '#087f92' },
  { id: 'green', name: '翡翠绿', hex: '#047857' },
  { id: 'yellow', name: '金黄', hex: '#a16207' },
  { id: 'orange', name: '琥珀橙', hex: '#c2410c' },
  { id: 'red', name: '红色', hex: '#dc2626' },
  { id: 'rose', name: '玫红', hex: '#be185d' },
  { id: 'magenta', name: '洋红', hex: '#a21caf' },
];

export function ThemeColorPicker() {
  const [color, setColor] = useState(
    () =>
      COLORS.find((item) => item.id === document.documentElement.dataset.themeColor)?.id || 'blue',
  );
  const selectedColor = COLORS.find((item) => item.id === color) || COLORS[0];
  useEffect(() => {
    document.documentElement.dataset.themeColor = color;
    try {
      localStorage.setItem('xtream-codec-theme-color', color);
    } catch {
      // 浏览器禁用存储时仍允许切换当前页面的主题色。
    }
  }, [color]);
  return (
    <Popover>
      <Button
        size="sm"
        variant="ghost"
        isIconOnly
        className="theme-color-trigger"
        aria-label={`选择主题色，当前为${selectedColor.name}`}
      >
        <Palette size={18} />
      </Button>
      <Popover.Content className="theme-color-popover" placement="bottom end">
        <Popover.Dialog aria-label="选择主题色">
          <ColorSwatchPicker
            aria-label="主题色"
            className="theme-color-grid"
            variant="square"
            value={selectedColor.hex}
            onChange={(value) => {
              const nextColor = COLORS.find(
                (item) => item.hex === value.toString('hex').toLowerCase(),
              );
              if (nextColor) setColor(nextColor.id);
            }}
          >
            {COLORS.map((item) => (
              <ColorSwatchPicker.Item key={item.id} color={item.hex} aria-label={item.name}>
                <ColorSwatchPicker.Swatch />
                <ColorSwatchPicker.Indicator />
              </ColorSwatchPicker.Item>
            ))}
          </ColorSwatchPicker>
        </Popover.Dialog>
      </Popover.Content>
    </Popover>
  );
}
