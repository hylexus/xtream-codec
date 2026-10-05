import { useState } from 'react';
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

export function WorkspaceTree({
  entries,
  activeId,
  onOpen,
  onAction,
}: {
  entries: WorkspaceEntry[];
  activeId: string | null;
  onOpen: (entry: WorkspaceEntry) => void;
  onAction: (entry: WorkspaceEntry, action: string) => void;
}) {
  const [collapsed, setCollapsed] = useState<Set<string>>(new Set());
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
              className={`workspace-tree-row ${activeId === entry.id ? 'active' : ''}`}
              style={{ paddingLeft: depth * 12 }}
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
      {entries.length ? (
        render(null, 0)
      ) : (
        <p className="workspace-empty">新建报文后点击保存，或导入工作区备份。</p>
      )}
    </div>
  );
}
