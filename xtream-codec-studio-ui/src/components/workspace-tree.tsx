import { useState, type DragEvent } from 'react';
import { Button, Dropdown, Label } from '@heroui/react';
import {
  ChevronDown,
  ChevronRight,
  FileCode2,
  Folder,
  FolderOpen,
  MoreHorizontal,
} from 'lucide-react';
import type { WorkspaceEntry } from '../workspace';
import { canMoveEntry } from '../workspace';

const ENTRY_DRAG_TYPE = 'application/x-xtream-studio-entry';

export function WorkspaceTree({
  entries,
  activeId,
  onOpen,
  onAction,
  onMove,
  isDisabled,
}: {
  entries: WorkspaceEntry[];
  activeId: string | null;
  onOpen: (entry: WorkspaceEntry) => void;
  onAction: (entry: WorkspaceEntry, action: string) => void;
  onMove: (entry: WorkspaceEntry, parentId: string | null) => void;
  isDisabled: boolean;
}) {
  const [collapsed, setCollapsed] = useState<Set<string>>(new Set());
  const [draggedId, setDraggedId] = useState<string | null>(null);
  // undefined 表示没有落点，null 表示根目录。
  const [dropTarget, setDropTarget] = useState<string | null>();
  const clearDrag = () => {
    setDraggedId(null);
    setDropTarget(undefined);
  };
  const dragOver = (event: DragEvent<HTMLDivElement>, parentId: string | null) => {
    event.stopPropagation();
    if (!draggedId) return;
    if (isDisabled || !canMoveEntry(entries, draggedId, parentId)) {
      event.dataTransfer.dropEffect = 'none';
      setDropTarget(undefined);
      return;
    }
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
    setDropTarget(parentId);
  };
  const dragLeave = (event: DragEvent<HTMLDivElement>) => {
    if (!event.currentTarget.contains(event.relatedTarget as Node | null)) setDropTarget(undefined);
  };
  const drop = (event: DragEvent<HTMLDivElement>, parentId: string | null) => {
    event.stopPropagation();
    const entry = entries.find((item) => item.id === draggedId);
    clearDrag();
    // 只接受本文件树发起的拖动，标签拖动和外部文件不会触发目录写入。
    if (
      isDisabled ||
      !entry ||
      event.dataTransfer.getData(ENTRY_DRAG_TYPE) !== entry.id ||
      !canMoveEntry(entries, entry.id, parentId)
    )
      return;
    event.preventDefault();
    if (parentId !== null)
      setCollapsed((old) => {
        const next = new Set(old);
        next.delete(parentId);
        return next;
      });
    onMove(entry, parentId);
  };
  const render = (parentId: string | null, depth: number) =>
    entries
      .filter((entry) => entry.parentId === parentId)
      .sort((a, b) =>
        a.kind === b.kind ? a.name.localeCompare(b.name, 'zh-CN') : a.kind === 'folder' ? -1 : 1,
      )
      .map((entry) => {
        const folder = entry.kind === 'folder';
        const expanded = !collapsed.has(entry.id);
        const Icon = folder ? (expanded ? FolderOpen : Folder) : FileCode2;
        return (
          <div key={entry.id}>
            <div
              className={`workspace-tree-row ${activeId === entry.id ? 'active' : ''} ${draggedId === entry.id ? 'tree-dragging' : ''} ${dropTarget === entry.id ? 'tree-drop-target' : ''}`}
              style={{ paddingLeft: depth * 12 }}
              draggable={!isDisabled}
              onDragStart={(event) => {
                if (isDisabled || (event.target as Element).closest('.tree-menu')) {
                  event.preventDefault();
                  return;
                }
                event.dataTransfer.effectAllowed = 'move';
                event.dataTransfer.setData(ENTRY_DRAG_TYPE, entry.id);
                setDraggedId(entry.id);
                setDropTarget(undefined);
              }}
              onDragOver={(event) => dragOver(event, entry.id)}
              onDragLeave={dragLeave}
              onDrop={(event) => drop(event, entry.id)}
              onDragEnd={clearDrag}
            >
              <Button
                variant="ghost"
                size="sm"
                className="workspace-tree-label"
                aria-expanded={folder ? expanded : undefined}
                onPress={() =>
                  folder
                    ? setCollapsed((old) => {
                        const next = new Set(old);
                        if (next.has(entry.id)) next.delete(entry.id);
                        else next.add(entry.id);
                        return next;
                      })
                    : onOpen(entry)
                }
              >
                {folder ? (
                  expanded ? (
                    <ChevronDown size={12} />
                  ) : (
                    <ChevronRight size={12} />
                  )
                ) : (
                  <span className="tree-indent" />
                )}
                <Icon size={15} />
                <span title={entry.name}>{entry.name}</span>
              </Button>
              <Dropdown>
                <Dropdown.Trigger
                  className="button button--sm button--ghost tree-menu"
                  aria-label={`${entry.name} 操作`}
                >
                  <MoreHorizontal size={15} />
                </Dropdown.Trigger>
                <Dropdown.Popover>
                  <Dropdown.Menu
                    aria-label="文档和目录操作"
                    onAction={(key) => onAction(entry, String(key))}
                  >
                    {folder && (
                      <Dropdown.Item id="new">
                        <Label>新建报文</Label>
                      </Dropdown.Item>
                    )}
                    {folder && (
                      <Dropdown.Item id="folder">
                        <Label>新建子目录</Label>
                      </Dropdown.Item>
                    )}
                    <Dropdown.Item id="rename">
                      <Label>重命名</Label>
                    </Dropdown.Item>
                    <Dropdown.Item id="move">
                      <Label>移动到…</Label>
                    </Dropdown.Item>
                    <Dropdown.Item id="delete" variant="danger">
                      <Label>删除</Label>
                    </Dropdown.Item>
                  </Dropdown.Menu>
                </Dropdown.Popover>
              </Dropdown>
            </div>
            {folder && expanded && render(entry.id, depth + 1)}
          </div>
        );
      });
  return (
    <div className="workspace-tree" aria-label="我的报文目录">
      {draggedId && (
        <div
          className={`workspace-tree-root-drop ${dropTarget === null ? 'tree-drop-target' : ''}`}
          aria-label="移动到根目录"
          onDragOver={(event) => dragOver(event, null)}
          onDragLeave={dragLeave}
          onDrop={(event) => drop(event, null)}
        >
          <FolderOpen size={14} />
          <span>移到根目录</span>
        </div>
      )}
      {entries.length ? (
        render(null, 0)
      ) : (
        <p className="workspace-empty">新建报文后点击保存，或导入工作区备份。</p>
      )}
    </div>
  );
}
