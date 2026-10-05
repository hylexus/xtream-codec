import { useRef } from 'react';
import { Dropdown, Label } from '@heroui/react';
import { dropdownVariants } from '@heroui/styles';
import { ArrowLeft, ArrowRight, Copy, Download, Pencil, Pin, PinOff, Save, X } from 'lucide-react';

const styles = dropdownVariants();

export interface TabContext {
  id: string;
  x: number;
  y: number;
}

export type TabAction =
  | 'pin'
  | 'move-left'
  | 'move-right'
  | 'save'
  | 'rename'
  | 'duplicate'
  | 'export'
  | 'close'
  | 'close-others'
  | 'close-right'
  | 'close-unpinned'
  | 'close-all';

export function TabContextMenu({
  context,
  isSaving,
  isPinned,
  canMoveLeft,
  canMoveRight,
  canCloseOthers,
  canCloseRight,
  canCloseUnpinned,
  canCloseAll,
  onAction,
  onClose,
}: {
  context: TabContext;
  isSaving: boolean;
  isPinned: boolean;
  canMoveLeft: boolean;
  canMoveRight: boolean;
  canCloseOthers: boolean;
  canCloseRight: boolean;
  canCloseUnpinned: boolean;
  canCloseAll: boolean;
  onAction: (action: TabAction) => void;
  onClose: () => void;
}) {
  const anchor = useRef<HTMLSpanElement>(null);
  return (
    <>
      {/* 使用鼠标位置作为布局锚点；焦点保留给真实的标签和菜单项。 */}
      <span
        ref={anchor}
        aria-hidden="true"
        className="tab-context-anchor"
        style={{ left: context.x, top: context.y }}
      />
      <Dropdown.Popover
        isOpen
        onOpenChange={(open) => !open && onClose()}
        triggerRef={anchor}
        placement="bottom start"
        offset={0}
        className={styles.popover({ class: 'tab-context-menu' })}
      >
        <Dropdown.Menu
          aria-label="标签右键菜单"
          autoFocus="first"
          className={styles.menu()}
          onClose={onClose}
          onAction={(key) => onAction(key as TabAction)}
        >
          <Dropdown.Section aria-label="文档操作">
            <Dropdown.Item id="save" isDisabled={isSaving}>
              <Save size={15} />
              <Label>{isSaving ? '保存中…' : '保存'}</Label>
            </Dropdown.Item>
            <Dropdown.Item id="rename" isDisabled={isSaving}>
              <Pencil size={15} />
              <Label>重命名</Label>
            </Dropdown.Item>
            <Dropdown.Item id="duplicate">
              <Copy size={15} />
              <Label>复制为新标签</Label>
            </Dropdown.Item>
            <Dropdown.Item id="export">
              <Download size={15} />
              <Label>导出完整文档</Label>
            </Dropdown.Item>
          </Dropdown.Section>
          <Dropdown.Section aria-label="排列标签" className="tab-context-close-actions">
            <Dropdown.Item id="pin">
              {isPinned ? <PinOff size={15} /> : <Pin size={15} />}
              <Label>{isPinned ? '取消固定标签' : '固定标签'}</Label>
            </Dropdown.Item>
            <Dropdown.Item id="move-left" isDisabled={!canMoveLeft}>
              <ArrowLeft size={15} />
              <Label>向左移动</Label>
            </Dropdown.Item>
            <Dropdown.Item id="move-right" isDisabled={!canMoveRight}>
              <ArrowRight size={15} />
              <Label>向右移动</Label>
            </Dropdown.Item>
          </Dropdown.Section>
          <Dropdown.Section aria-label="关闭标签" className="tab-context-close-actions">
            <Dropdown.Item id="close">
              <X size={15} />
              <Label>关闭当前标签</Label>
            </Dropdown.Item>
            <Dropdown.Item id="close-others" isDisabled={!canCloseOthers}>
              <X size={15} />
              <Label>关闭其他标签</Label>
            </Dropdown.Item>
            <Dropdown.Item id="close-right" isDisabled={!canCloseRight}>
              <X size={15} />
              <Label>关闭右侧标签</Label>
            </Dropdown.Item>
            <Dropdown.Item id="close-unpinned" isDisabled={!canCloseUnpinned}>
              <X size={15} />
              <Label>关闭未固定标签</Label>
            </Dropdown.Item>
            <Dropdown.Item id="close-all" isDisabled={!canCloseAll}>
              <X size={15} />
              <Label>关闭全部标签（含固定）</Label>
            </Dropdown.Item>
          </Dropdown.Section>
        </Dropdown.Menu>
      </Dropdown.Popover>
    </>
  );
}
