import { useEffect, useRef, useState, type CSSProperties } from 'react';
import {
  Alert,
  Button,
  Dropdown,
  Input,
  Label,
  Link,
  TextField,
  Toast,
  Tooltip,
  toast,
} from '@heroui/react';
import {
  Binary,
  Braces,
  Check,
  ChevronRight,
  CircleHelp,
  Download,
  FileCode2,
  FlaskConical,
  FolderPlus,
  Layers3,
  Moon,
  MoreHorizontal,
  PanelLeftClose,
  PanelLeftOpen,
  Pin,
  Plus,
  RotateCw,
  Save,
  SlidersHorizontal,
  Sun,
  Terminal,
  Upload,
  X,
} from 'lucide-react';
import { codecApi, workspaceApi } from './api';
import { pretty } from './model';
import { draftFromPreset, PRESETS } from './presets';
import type { Preset } from './presets';
import type { Entity, Mode } from './types';
import {
  acceptSave,
  canMoveEntry,
  completeRun,
  descendants,
  downloadJson,
  fingerprint,
  isDirty,
  moveTab,
  MODE_TITLES,
  newTab,
  parseDocument,
  pinTab,
  removeDocumentTab,
} from './workspace';
import type {
  DocumentData,
  DocumentTab,
  Session,
  WorkspaceEntry,
  WorkspaceIndex,
} from './workspace';
import { loadSession, saveSession } from './draft-store';
import { DocumentEditor } from './components/document-editor';
import { WorkspaceDialog } from './components/workspace-dialog';
import type { DialogAction } from './components/workspace-dialog';
import { WorkspaceTree } from './components/workspace-tree';
import { RunPanel } from './components/run-panel';
import { ThemeColorPicker } from './components/theme-color-picker';
import { TabContextMenu } from './components/tab-context-menu';
import type { TabAction, TabContext } from './components/tab-context-menu';
import { DEFAULT_LAYOUT, useLayout, useViewportSize } from './layout';
import { PanelResizer } from './components/panel-resizer';

const MODE_ICONS = { dynamic: Layers3, entity: Braces, field: FlaskConical };

export function App() {
  const shell = useRef<HTMLDivElement>(null);
  const { layout, setSize } = useLayout();
  const viewport = useViewportSize();
  const sidebarMax = Math.max(184, Math.min(420, viewport.width * 0.3));
  const sidebarWidth = Math.min(layout.sidebar, sidebarMax);
  // 保留顶部工具栏及输入面板的操作空间；查看完整结果可使用最大化。
  const runMax = Math.max(180, viewport.height - 480);
  const [session, setSession] = useState<Session>({
    formatVersion: 1,
    tabs: [],
    activeId: '',
    sidebarCollapsed: false,
  });
  const current = useRef(session);
  const changeSession = (update: (old: Session) => Session) => {
    const next = update(current.current);
    current.current = next;
    setSession(next);
  };
  const changeTab = (id: string, update: (tab: DocumentTab) => DocumentTab) =>
    changeSession((old) => ({
      ...old,
      tabs: old.tabs.map((tab) => (tab.id === id ? update(tab) : tab)),
    }));
  const [ready, setReady] = useState(false);
  const [draftAvailable, setDraftAvailable] = useState(false);
  const [draftStatus, setDraftStatus] = useState('正在恢复草稿…');
  const [workspace, setWorkspace] = useState<WorkspaceIndex>({ revision: 0, entries: [] });
  const [workspaceOnline, setWorkspaceOnline] = useState(false);
  const [entities, setEntities] = useState<Entity[]>([]);
  const [extensions, setExtensions] = useState<string[]>([]);
  const [connection, setConnection] = useState<'connecting' | 'online' | 'offline'>('connecting');
  const [theme, setTheme] = useState<'dark' | 'light'>(() =>
    document.documentElement.dataset.theme === 'light' ? 'light' : 'dark',
  );
  const [error, setError] = useState('');
  const [action, setAction] = useState<DialogAction | null>(null);
  const [tabContext, setTabContext] = useState<TabContext | null>(null);
  const [closeQueue, setCloseQueue] = useState<string[]>([]);
  const draggingTab = useRef<string | null>(null);
  const [tabDrop, setTabDrop] = useState<{ id: string; after: boolean } | null>(null);
  const [saving, setSaving] = useState<Set<string>>(new Set());
  const savingIds = useRef(new Set<string>());
  const [movingEntry, setMovingEntry] = useState(false);
  const [pending, setPending] = useState<Set<string>>(new Set());
  const [runOpen, setRunOpen] = useState(false);
  const [runMaximized, setRunMaximized] = useState(false);
  const fileInput = useRef<HTMLInputElement>(null);
  const [importPending, setImportPending] = useState(false);
  const active = session.tabs.find((tab) => tab.id === session.activeId) || session.tabs[0];
  const report = (cause: unknown) =>
    setError(cause instanceof Error ? cause.message : String(cause));
  const safely = (work: () => Promise<void>) => {
    void work().catch(report);
  };
  const notify = (message: string) => {
    toast(message, { timeout: 3500, indicator: <Check size={15} /> });
  };

  const refresh = async () => {
    try {
      setWorkspace(await workspaceApi.index());
      setWorkspaceOnline(true);
    } catch (cause) {
      setWorkspaceOnline(false);
      throw cause;
    }
  };
  const connect = async () => {
    setConnection('connecting');
    try {
      const [catalog, allowed] = await Promise.all([codecApi.entities(), codecApi.extensions()]);
      setEntities(catalog);
      setExtensions(allowed);
      setConnection('online');
    } catch {
      setConnection('offline');
    }
    await refresh().catch(report);
  };
  useEffect(() => {
    let mounted = true;
    void loadSession()
      .then((saved) => {
        if (mounted) {
          current.current = saved;
          setSession(saved);
          setDraftStatus('草稿已恢复');
          setDraftAvailable(true);
        }
      })
      .catch((cause) => {
        if (mounted) {
          report(cause);
          setDraftStatus('本地草稿不可用');
        }
      })
      .finally(() => {
        if (mounted) setReady(true);
      });
    void connect();
    return () => {
      mounted = false;
    };
  }, []);
  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.documentElement.classList.toggle('dark', theme === 'dark');
    document.documentElement.classList.toggle('light', theme === 'light');
    try {
      localStorage.setItem('xtream-codec-theme', theme);
    } catch {
      /* 存储不可用时保留当前主题。 */
    }
  }, [theme]);
  useEffect(() => {
    if (!ready || !draftAvailable) return;
    const timer = window.setTimeout(() => {
      void saveSession(session)
        .then(() => setDraftStatus('本地草稿已备份'))
        .catch((cause) => {
          setDraftStatus('草稿备份失败');
          report(cause);
        });
    }, 350);
    return () => window.clearTimeout(timer);
  }, [session, ready, draftAvailable]);
  useEffect(() => {
    if (!draftAvailable) return;
    const flush = () => {
      if (document.visibilityState === 'hidden') void saveSession(current.current).catch(report);
    };
    document.addEventListener('visibilitychange', flush);
    return () => document.removeEventListener('visibilitychange', flush);
  }, [draftAvailable]);

  const folders = (excluded = new Set<string>()) =>
    workspace.entries
      .filter((entry) => entry.kind === 'folder' && !excluded.has(entry.id))
      .map((entry) => {
        const names = [entry.name];
        let parentId = entry.parentId;
        while (parentId) {
          const parent = workspace.entries.find((item) => item.id === parentId);
          if (!parent) break;
          names.unshift(parent.name);
          parentId = parent.parentId;
        }
        return { ...entry, name: names.join(' / ') };
      });
  const append = (tab: DocumentTab) =>
    changeSession((old) => ({ ...old, tabs: [...old.tabs, tab], activeId: tab.id }));
  const openPreset = (preset: Preset) =>
    append(newTab('dynamic', preset.title, draftFromPreset(preset)));
  const createDocument = (parentId: string | null = null, mode: Mode = 'dynamic') => {
    const tab = newTab(mode);
    tab.parentId = parentId;
    append(tab);
  };
  const saveDocument = async (id: string, name?: string, parentId?: string | null) => {
    const tab = current.current.tabs.find((item) => item.id === id);
    if (!tab) throw new Error('文档已关闭');
    if (name !== undefined && !name.trim()) throw new Error('请输入报文名称');
    if (savingIds.current.has(id)) throw new Error('文档正在保存，请稍后重试');
    const submitted = structuredClone(tab.data);
    savingIds.current.add(id);
    setSaving(new Set(savingIds.current));
    try {
      const entry = tab.entryId
        ? await workspaceApi.update({
            id: tab.entryId,
            parentId: parentId === undefined ? tab.parentId : parentId,
            kind: 'document',
            name: name || tab.name,
            revision: tab.revision,
            updatedAt: 0,
            document: submitted,
          })
        : await workspaceApi.create(
            parentId === undefined ? tab.parentId : parentId,
            'document',
            name || tab.name,
            submitted,
          );
      changeTab(id, (latest) => acceptSave(latest, entry, submitted));
      notify('文档已保存到服务器');
      await refresh().catch(report);
    } finally {
      savingIds.current.delete(id);
      setSaving(new Set(savingIds.current));
    }
  };
  const requestSave = (id: string) => {
    const tab = current.current.tabs.find((item) => item.id === id);
    if (!tab) return;
    if (tab.entryId) safely(() => saveDocument(id));
    else
      setAction({
        title: '保存报文',
        name: tab.name,
        parentId: tab.parentId,
        folders: folders(),
        confirm: '保存到服务器',
        submit: (name, parentId) => saveDocument(id, name, parentId),
      });
  };
  const removeTab = (id: string) => {
    changeSession((old) => removeDocumentTab(old, id));
    if (!current.current.tabs.length) {
      setRunOpen(false);
      setRunMaximized(false);
    }
  };
  const closeTab = (id: string) => {
    const tab = current.current.tabs.find((item) => item.id === id);
    if (!tab) return;
    if (!isDirty(tab)) {
      removeTab(id);
      return;
    }
    setAction({
      title: '保存后关闭？',
      description: `“${tab.name}”有未保存的编辑或运行结果。关闭标签不会删除服务器文档。`,
      ...(tab.entryId ? {} : { name: tab.name, parentId: tab.parentId, folders: folders() }),
      confirm: '保存并关闭',
      discard: () => removeTab(id),
      submit: async (name, parentId) => {
        await saveDocument(id, name || undefined, tab.entryId ? undefined : parentId);
        const latest = current.current.tabs.find((item) => item.id === id);
        if (latest && isDirty(latest)) throw new Error('保存期间又有新编辑，请再次保存或取消关闭');
        removeTab(id);
      },
    });
  };
  // 逐个处理关闭请求，等待当前确认框完成；取消或保存失败时不继续关闭。
  useEffect(() => {
    if (action || !closeQueue.length) return;
    setCloseQueue(closeQueue.slice(1));
    closeTab(closeQueue[0]);
  }, [action, closeQueue]);
  const openEntry = async (entry: WorkspaceEntry) => {
    const opened = current.current.tabs.find((tab) => tab.entryId === entry.id);
    if (opened) {
      changeSession((old) => ({ ...old, activeId: opened.id }));
      return;
    }
    const loaded = await workspaceApi.get(entry.id);
    const data = parseDocument(loaded.document);
    // 并发点击同一文件时只打开一个标签。
    changeSession((old) => {
      const existing = old.tabs.find((tab) => tab.entryId === entry.id);
      if (existing) return { ...old, activeId: existing.id };
      const tab = {
        id: crypto.randomUUID(),
        entryId: loaded.id,
        parentId: loaded.parentId,
        name: loaded.name,
        revision: loaded.revision,
        saved: fingerprint(data),
        data,
      };
      return { ...old, tabs: [...old.tabs, tab], activeId: tab.id };
    });
  };
  const newFolder = (parentId: string | null = null) =>
    setAction({
      title: '新建目录',
      name: '新建文件夹',
      parentId,
      folders: folders(),
      submit: async (name, parent) => {
        await workspaceApi.create(parent, 'folder', name, null);
        await refresh();
      },
    });
  const updateEntry = async (entry: WorkspaceEntry, name: string, parentId: string | null) => {
    const full = await workspaceApi.get(entry.id);
    const updated = await workspaceApi.update({
      ...full,
      revision: entry.revision,
      name,
      parentId,
    });
    changeSession((old) => ({
      ...old,
      tabs: old.tabs.map((tab) =>
        tab.entryId === entry.id
          ? {
              ...tab,
              name: updated.name,
              parentId: updated.parentId,
              // 只推进与移动前版本一致的标签，旧草稿仍需通过保存冲突检查。
              revision: tab.revision === entry.revision ? updated.revision : tab.revision,
            }
          : tab,
      ),
    }));
    await refresh();
  };
  const moveEntry = async (entry: WorkspaceEntry, parentId: string | null) => {
    if (movingEntry || !canMoveEntry(workspace.entries, entry.id, parentId)) return;
    if (!workspaceOnline) throw new Error('工作区未连接，请连接后移动');
    setMovingEntry(true);
    try {
      await updateEntry(entry, entry.name, parentId);
      notify('已移动到目标目录');
    } finally {
      setMovingEntry(false);
    }
  };
  const treeAction = (entry: WorkspaceEntry, kind: string) => {
    if (kind === 'new') {
      createDocument(entry.id);
      return;
    }
    if (kind === 'folder') {
      newFolder(entry.id);
      return;
    }
    if (kind === 'delete') {
      const removed = descendants(workspace.entries, entry.id);
      setAction({
        title: '删除服务器内容',
        danger: true,
        confirm: '删除',
        description: `将删除“${entry.name}”及其 ${removed.size - 1} 个子项。已打开的标签会作为未保存副本保留。`,
        submit: async () => {
          setWorkspace(await workspaceApi.remove(entry, workspace.revision));
          changeSession((old) => ({
            ...old,
            tabs: old.tabs.map((tab) =>
              tab.entryId && removed.has(tab.entryId)
                ? { ...tab, entryId: null, parentId: null, revision: 0, saved: null }
                : tab.parentId && removed.has(tab.parentId)
                  ? { ...tab, parentId: null }
                  : tab,
            ),
          }));
        },
      });
      return;
    }
    setAction({
      title: kind === 'rename' ? '重命名' : '移动到目录',
      name: entry.name,
      parentId: entry.parentId,
      ...(kind === 'move' ? { folders: folders(descendants(workspace.entries, entry.id)) } : {}),
      submit: (name, parentId) =>
        updateEntry(entry, name, kind === 'move' ? parentId : entry.parentId),
    });
  };
  const renameTab = (tab: DocumentTab) => {
    if (tab.entryId) {
      const entry = workspace.entries.find((item) => item.id === tab.entryId);
      if (entry) {
        treeAction(entry, 'rename');
        return;
      }
      report(new Error('服务器目录已变化，请刷新后重试'));
      return;
    }
    setAction({
      title: '重命名标签',
      name: tab.name,
      submit: async (name) => {
        if (!name) throw new Error('请输入名称');
        changeTab(tab.id, (old) => ({ ...old, name }));
      },
    });
  };
  const duplicate = (tab: DocumentTab) => {
    const copy = newTab(tab.data.mode, `${tab.name} · 副本`);
    copy.data = structuredClone(tab.data);
    copy.parentId = tab.parentId;
    append(copy);
  };
  const exportDocument = (tab: DocumentTab) =>
    downloadJson(`${tab.name}.studio.json`, {
      format: 'studio-document',
      formatVersion: 1,
      name: tab.name,
      document: tab.data,
    });
  const moveTabBy = (id: string, direction: number) => {
    changeSession((old) =>
      moveTab(old, id, old.tabs.findIndex((tab) => tab.id === id) + direction),
    );
    document.getElementById(`tab-${id}`)?.focus();
  };
  const tabAction = (kind: TabAction) => {
    const tabs = current.current.tabs;
    const index = tabs.findIndex((tab) => tab.id === tabContext?.id);
    setTabContext(null);
    if (index < 0) return;
    const tab = tabs[index];
    switch (kind) {
      case 'pin':
        changeSession((old) => pinTab(old, tab.id, !tab.pinned));
        break;
      case 'move-left':
        moveTabBy(tab.id, -1);
        break;
      case 'move-right':
        moveTabBy(tab.id, 1);
        break;
      case 'save':
        requestSave(tab.id);
        break;
      case 'rename':
        renameTab(tab);
        break;
      case 'duplicate':
        duplicate(tab);
        break;
      case 'export':
        exportDocument(tab);
        break;
      case 'close':
        setCloseQueue([tab.id]);
        break;
      case 'close-others':
        setCloseQueue(
          tabs.filter((item) => item.id !== tab.id && !item.pinned).map((item) => item.id),
        );
        break;
      case 'close-right':
        setCloseQueue(
          tabs
            .slice(index + 1)
            .filter((item) => !item.pinned)
            .map((item) => item.id),
        );
        break;
      case 'close-all':
        setCloseQueue(tabs.map((item) => item.id));
        break;
      case 'close-unpinned':
        setCloseQueue(tabs.filter((item) => !item.pinned).map((item) => item.id));
        break;
    }
  };
  const run = async (id: string, direction: 'encode' | 'decode', submitted: DocumentData) => {
    setRunOpen(true);
    setPending((old) => new Set(old).add(id));
    try {
      const result = await codecApi.execute(submitted.mode, direction, submitted.draft);
      changeTab(id, (tab) => completeRun(tab, submitted, direction, result));
      setConnection('online');
    } finally {
      setPending((old) => {
        const next = new Set(old);
        next.delete(id);
        return next;
      });
    }
  };
  useEffect(() => {
    const keydown = (event: KeyboardEvent) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 's') {
        event.preventDefault();
        if (ready && !action) requestSave(current.current.activeId);
      }
    };
    const beforeUnload = (event: BeforeUnloadEvent) => {
      if (current.current.tabs.some(isDirty)) {
        event.preventDefault();
        event.returnValue = '';
      }
    };
    // 先于编辑器和 HeroUI 控件处理保存键，避免其局部键盘处理阻止冒泡。
    window.addEventListener('keydown', keydown, true);
    window.addEventListener('beforeunload', beforeUnload);
    return () => {
      window.removeEventListener('keydown', keydown, true);
      window.removeEventListener('beforeunload', beforeUnload);
    };
  }, [ready, action, workspace]);

  const importFile = async (file: File) => {
    if (file.size > 20 * 1024 * 1024) throw new Error('导入文件超过 20 MiB');
    const value = JSON.parse(await file.text());
    if (value.format === 'studio-workspace') {
      setAction({
        title: '导入工作区',
        description: '备份会导入到新的目录中，已有内容保留。',
        confirm: '导入',
        submit: async () => {
          setWorkspace(await workspaceApi.import(value));
          setWorkspaceOnline(true);
          notify('工作区已导入');
        },
      });
    } else {
      if (
        value.format !== 'studio-document' ||
        value.formatVersion !== 1 ||
        typeof value.name !== 'string' ||
        !value.name.trim()
      )
        throw new Error('请选择完整 Studio 文档或工作区备份；结构 JSON 请在编辑区导入');
      const tab = newTab();
      tab.name = value.name;
      tab.data = parseDocument(value.document);
      append(tab);
      notify('完整文档已作为新标签导入');
    }
  };
  return (
    <div
      ref={shell}
      className={`workbench-shell studio-editor-shell ${session.sidebarCollapsed ? 'sidebar-collapsed' : ''}`}
      style={
        {
          '--sidebar-width': session.sidebarCollapsed ? '72px' : `${sidebarWidth}px`,
        } as CSSProperties
      }
    >
      <aside className="sidebar" id="workbench-sidebar" aria-label="工作区导航">
        <div className="sidebar-header">
          <Button
            className="sidebar-mobile-close"
            variant="ghost"
            size="sm"
            isIconOnly
            aria-label="关闭导航"
            onPress={() => changeSession((old) => ({ ...old, sidebarCollapsed: true }))}
          >
            <X size={16} />
          </Button>
          <Link className="brand" href="/" aria-label="Xtream Codec Studio">
            <span className="brand-symbol">
              <Binary size={23} />
            </span>
            <span className="brand-text">
              xtream<span className="brand-caption">CODEC STUDIO</span>
            </span>
          </Link>
        </div>
        <div className="sidebar-body">
          <div className="sidebar-section-label">新建报文</div>
          <nav aria-label="新建调试文档">
            {(Object.keys(MODE_TITLES) as Mode[]).map((mode) => {
              const Icon = MODE_ICONS[mode];
              return (
                <Tooltip key={mode} isDisabled={!session.sidebarCollapsed} delay={0}>
                  <Button
                    className="nav-item"
                    variant="ghost"
                    aria-label={`新建${MODE_TITLES[mode]}`}
                    isDisabled={!ready}
                    onPress={() => createDocument(null, mode)}
                  >
                    <Icon size={17} />
                    <span className="nav-label">{MODE_TITLES[mode]}</span>
                    <Plus size={13} className="nav-label" />
                  </Button>
                  <Tooltip.Content>新建{MODE_TITLES[mode]}</Tooltip.Content>
                </Tooltip>
              );
            })}
          </nav>
          <div className="workspace-tree-heading">
            <span>我的报文</span>
            <div className="actions">
              <Button
                size="sm"
                variant="ghost"
                isIconOnly
                aria-label="新建报文"
                isDisabled={!ready}
                onPress={() => createDocument()}
              >
                <Plus size={14} />
              </Button>
              <Button
                size="sm"
                variant="ghost"
                isIconOnly
                aria-label="新建目录"
                isDisabled={!workspaceOnline}
                onPress={() => newFolder()}
              >
                <FolderPlus size={14} />
              </Button>
              <Button
                size="sm"
                variant="ghost"
                isIconOnly
                aria-label="刷新目录"
                onPress={() => safely(refresh)}
              >
                <RotateCw size={13} />
              </Button>
            </div>
          </div>
          <div className="workspace-tree-container">
            <WorkspaceTree
              entries={workspace.entries}
              activeId={active?.entryId || null}
              onOpen={(entry) => safely(() => openEntry(entry))}
              onAction={treeAction}
              onMove={(entry, parentId) => safely(() => moveEntry(entry, parentId))}
              isDisabled={!workspaceOnline || movingEntry || saving.size > 0}
            />
            {!workspaceOnline && (
              <p className="workspace-empty">工作区未连接，当前编辑保留在本地草稿。</p>
            )}
          </div>
          <div className="sidebar-section-label examples-label">示例协议 · 在新标签打开</div>
          <div className="sidebar-presets">
            {PRESETS.map((preset, index) => (
              <Tooltip key={preset.id} isDisabled={!session.sidebarCollapsed} delay={0}>
                <Button
                  className="sidebar-preset"
                  variant="ghost"
                  isDisabled={!ready}
                  onPress={() => openPreset(preset)}
                  aria-label={preset.title}
                >
                  <span className={`preset-dot dot-${index}`} />
                  <span className="sidebar-preset-text">
                    <strong>{preset.title}</strong>
                    <small>{preset.description}</small>
                  </span>
                </Button>
                <Tooltip.Content>{preset.title}</Tooltip.Content>
              </Tooltip>
            ))}
          </div>
        </div>
        <div className="sidebar-footer">
          <span>XTREAM CODEC</span>
          <span>0.11</span>
        </div>
        {!session.sidebarCollapsed && (
          <PanelResizer
            className="sidebar-resizer"
            label="调整侧边栏宽度"
            direction="horizontal"
            value={sidebarWidth}
            min={184}
            max={sidebarMax}
            onPreview={(value) => shell.current?.style.setProperty('--sidebar-width', `${value}px`)}
            onChange={(value) => setSize('sidebar', value)}
            onReset={() => setSize('sidebar', DEFAULT_LAYOUT.sidebar)}
          />
        )}
      </aside>
      <div className="workspace studio-workspace">
        <header className="topbar">
          <div className="breadcrumbs">
            <Button
              variant="ghost"
              size="sm"
              isIconOnly
              aria-label={session.sidebarCollapsed ? '展开侧边栏' : '折叠侧边栏'}
              aria-controls="workbench-sidebar"
              aria-expanded={!session.sidebarCollapsed}
              onPress={() =>
                changeSession((old) => ({ ...old, sidebarCollapsed: !old.sidebarCollapsed }))
              }
            >
              {session.sidebarCollapsed ? (
                <PanelLeftOpen size={17} />
              ) : (
                <PanelLeftClose size={17} />
              )}
            </Button>
            <span>Codec Studio</span>
            <ChevronRight size={12} />
            <strong>报文工作区</strong>
          </div>
          <div className="topbar-actions">
            <span className="autosave-label">
              <Check size={12} />
              {draftStatus}
            </span>
            <ThemeColorPicker />
            <Button
              variant="ghost"
              size="sm"
              isIconOnly
              aria-label={theme === 'dark' ? '切换明亮模式' : '切换暗黑模式'}
              onPress={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
            >
              {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
            </Button>
            <Link
              className="help-link"
              href="https://hylexus.github.io/xtream-codec/guide/core/dynamic/"
              target="_blank"
              rel="noreferrer"
              aria-label="动态 DSL 文档"
            >
              <CircleHelp size={18} />
            </Link>
          </div>
        </header>
        <div className="document-tabbar">
          <div className="document-tabs" role="tablist" aria-label="已打开报文">
            {session.tabs.map((tab) => (
              <div
                key={tab.id}
                className={`document-tab ${tab.id === session.activeId ? 'active' : ''} ${tab.pinned ? 'pinned' : ''} ${tabDrop?.id === tab.id ? (tabDrop.after ? 'drop-after' : 'drop-before') : ''}`}
                role="tab"
                draggable
                aria-controls={`editor-${tab.id}`}
                aria-selected={tab.id === session.activeId}
                onDragStart={(event) => {
                  draggingTab.current = tab.id;
                  event.dataTransfer.effectAllowed = 'move';
                  event.dataTransfer.setData('application/x-xtream-studio-tab', tab.id);
                  setTabContext(null);
                }}
                onDragOver={(event) => {
                  if (!draggingTab.current || draggingTab.current === tab.id) return;
                  event.preventDefault();
                  event.dataTransfer.dropEffect = 'move';
                  const bounds = event.currentTarget.getBoundingClientRect();
                  setTabDrop({ id: tab.id, after: event.clientX > bounds.left + bounds.width / 2 });
                }}
                onDragLeave={(event) => {
                  if (!event.currentTarget.contains(event.relatedTarget as Node | null))
                    setTabDrop(null);
                }}
                onDrop={(event) => {
                  if (!draggingTab.current) return;
                  event.preventDefault();
                  const id = draggingTab.current;
                  const bounds = event.currentTarget.getBoundingClientRect();
                  const after = event.clientX > bounds.left + bounds.width / 2;
                  changeSession((old) => {
                    const source = old.tabs.findIndex((item) => item.id === id);
                    const target =
                      old.tabs.findIndex((item) => item.id === tab.id) + (after ? 1 : 0);
                    return moveTab(old, id, target - (source < target ? 1 : 0));
                  });
                  draggingTab.current = null;
                  setTabDrop(null);
                }}
                onDragEnd={() => {
                  draggingTab.current = null;
                  setTabDrop(null);
                }}
                onContextMenu={(event) => {
                  event.preventDefault();
                  document.getElementById(`tab-${tab.id}`)?.focus();
                  setTabContext({ id: tab.id, x: event.clientX, y: event.clientY });
                }}
              >
                <Button
                  variant="ghost"
                  size="sm"
                  className="document-tab-label"
                  id={`tab-${tab.id}`}
                  aria-controls={`editor-${tab.id}`}
                  aria-pressed={tab.id === session.activeId}
                  aria-haspopup="menu"
                  aria-expanded={tabContext?.id === tab.id}
                  onPress={() => changeSession((old) => ({ ...old, activeId: tab.id }))}
                  onKeyDown={(event) => {
                    if (event.key === 'ContextMenu' || (event.shiftKey && event.key === 'F10')) {
                      event.preventDefault();
                      const bounds = event.currentTarget.getBoundingClientRect();
                      setTabContext({ id: tab.id, x: bounds.left, y: bounds.bottom });
                    } else if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
                      event.preventDefault();
                      if (event.altKey && event.shiftKey) {
                        moveTabBy(tab.id, event.key === 'ArrowRight' ? 1 : -1);
                        return;
                      }
                      const index = session.tabs.findIndex((item) => item.id === tab.id);
                      const next =
                        session.tabs[
                          (index + (event.key === 'ArrowRight' ? 1 : -1) + session.tabs.length) %
                            session.tabs.length
                        ];
                      changeSession((old) => ({ ...old, activeId: next.id }));
                      document.getElementById(`tab-${next.id}`)?.focus();
                    }
                  }}
                >
                  <FileCode2 size={14} />
                  <span title={tab.name}>{tab.name}</span>
                  {isDirty(tab) && <span className="dirty-dot" aria-label="未保存" />}
                  {saving.has(tab.id) && <span className="muted">保存中…</span>}
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  isIconOnly
                  className="document-tab-close"
                  aria-label={tab.pinned ? `取消固定 ${tab.name}` : `关闭 ${tab.name}`}
                  onPress={() =>
                    tab.pinned
                      ? changeSession((old) => pinTab(old, tab.id, false))
                      : setCloseQueue([tab.id])
                  }
                >
                  {tab.pinned ? <Pin size={12} /> : <X size={12} />}
                </Button>
              </div>
            ))}
          </div>
          <Button
            size="sm"
            variant="ghost"
            isIconOnly
            aria-label="新建标签"
            isDisabled={!ready}
            onPress={() => createDocument()}
          >
            <Plus size={16} />
          </Button>
        </div>
        {active && (
          <section className="document-toolbar" aria-label="文档工具栏">
            <div className="document-commandbar">
              <div className="heading-title document-info">
                <span className="document-mode">{MODE_TITLES[active.data.mode]}</span>
                <strong title={active.name}>{active.name}</strong>
                {active.entryId && (
                  <span className="document-saved">服务器 · v{active.revision}</span>
                )}
              </div>
              <div className="document-config" role="group" aria-label="编译配置">
                <TextField
                  className="protocol-version-field"
                  value={active.data.draft.version}
                  onChange={(version) =>
                    changeTab(active.id, (tab) => ({
                      ...tab,
                      data: { ...tab.data, draft: { ...tab.data.draft, version } },
                    }))
                  }
                >
                  <Label>
                    <SlidersHorizontal size={14} />
                    协议版本
                  </Label>
                  <Input placeholder="默认" />
                </TextField>
                {active.data.mode !== 'entity' && (
                  <span className="schema-version">
                    SCHEMA <b>v{active.data.draft.schema.schemaVersion}</b>
                  </span>
                )}
              </div>
              <div className="connection-bar">
                <div
                  className={`connection-status ${connection}`}
                  role="status"
                  title={
                    connection === 'offline'
                      ? '启动独立 Java 调试应用后重新连接；离线仍可编辑结构。'
                      : '编解码请求由调试服务执行'
                  }
                >
                  <span />
                  {connection === 'online'
                    ? '服务已连接'
                    : connection === 'connecting'
                      ? '正在连接'
                      : '服务未连接'}
                </div>
                <Button
                  size="sm"
                  variant="ghost"
                  isIconOnly
                  isDisabled={connection === 'connecting'}
                  aria-label="重新连接服务"
                  onPress={() => void connect()}
                >
                  <RotateCw size={14} />
                </Button>
              </div>
              <div className="actions document-actions">
                <Button
                  variant="secondary"
                  size="sm"
                  isDisabled={!ready || saving.has(active.id)}
                  onPress={() => requestSave(active.id)}
                >
                  <Save size={14} />
                  保存
                </Button>
                <Button
                  size="sm"
                  variant="ghost"
                  className="run-panel-button"
                  aria-label="运行面板"
                  aria-pressed={runOpen}
                  onPress={() => {
                    setRunOpen((open) => !open);
                    setRunMaximized(false);
                  }}
                >
                  <Terminal size={14} />
                  <span className="run-panel-label">运行面板</span>
                </Button>
                <Dropdown>
                  <Dropdown.Trigger
                    className="button button--sm button--ghost"
                    aria-label="文档和工作区操作"
                  >
                    <MoreHorizontal size={17} />
                  </Dropdown.Trigger>
                  <Dropdown.Popover>
                    <Dropdown.Menu
                      aria-label="文档操作"
                      onAction={(key) => {
                        if (key === 'rename') renameTab(active);
                        else if (key === 'duplicate') duplicate(active);
                        else if (key === 'export-document') exportDocument(active);
                        else if (key === 'export-workspace')
                          safely(async () =>
                            downloadJson(
                              'codec-studio-workspace.json',
                              await workspaceApi.export(),
                            ),
                          );
                        else if (key === 'import') fileInput.current?.click();
                      }}
                    >
                      <Dropdown.Item id="rename">
                        <Label>重命名</Label>
                      </Dropdown.Item>
                      <Dropdown.Item id="duplicate">
                        <Label>复制为新标签</Label>
                      </Dropdown.Item>
                      <Dropdown.Item id="export-document">
                        <Download size={14} />
                        <Label>导出完整文档</Label>
                      </Dropdown.Item>
                      <Dropdown.Item id="export-workspace">
                        <Download size={14} />
                        <Label>导出服务器工作区</Label>
                      </Dropdown.Item>
                      <Dropdown.Item id="import" isDisabled={importPending}>
                        <Upload size={14} />
                        <Label>导入文档 / 工作区</Label>
                      </Dropdown.Item>
                    </Dropdown.Menu>
                  </Dropdown.Popover>
                </Dropdown>
              </div>
            </div>
          </section>
        )}
        {error && (
          <Alert status="danger" className="error-banner dismissible workspace-error">
            <Alert.Content>
              <Alert.Description>{error}</Alert.Description>
            </Alert.Content>
            <Button
              size="sm"
              isIconOnly
              variant="ghost"
              aria-label="关闭工作区错误"
              onPress={() => setError('')}
            >
              <X size={14} />
            </Button>
          </Alert>
        )}
        <div
          className={`document-editors ${active && runOpen && runMaximized ? 'run-covers-editor' : ''}`}
        >
          {ready && !active && (
            <div className="workspace-welcome">
              <div className="workspace-welcome-icon">
                <Binary size={36} />
              </div>
              <h1>开始一份新的报文</h1>
              <p>新建报文，或从左侧打开已保存的文档与示例。</p>
              <div className="workspace-welcome-actions">
                {(Object.keys(MODE_TITLES) as Mode[]).map((mode) => {
                  const Icon = MODE_ICONS[mode];
                  return (
                    <Button
                      key={mode}
                      variant="secondary"
                      onPress={() => createDocument(null, mode)}
                    >
                      <Icon size={18} />
                      新建{MODE_TITLES[mode]}
                    </Button>
                  );
                })}
                <Button
                  variant="ghost"
                  isDisabled={importPending}
                  onPress={() => fileInput.current?.click()}
                >
                  <Upload size={18} />
                  导入文档 / 工作区
                </Button>
              </div>
            </div>
          )}
          {ready &&
            session.tabs.map((tab) => (
              <div
                key={tab.id}
                id={`editor-${tab.id}`}
                role="tabpanel"
                aria-labelledby={`tab-${tab.id}`}
                hidden={tab.id !== session.activeId}
                className="document-host workbench-main"
              >
                <DocumentEditor
                  data={tab.data}
                  theme={theme}
                  entities={entities}
                  extensions={extensions}
                  onChange={(update) =>
                    changeTab(tab.id, (old) => ({ ...old, data: update(old.data) }))
                  }
                  onRun={(direction, submitted) => run(tab.id, direction, submitted)}
                  onPreset={openPreset}
                  notify={notify}
                />
              </div>
            ))}
        </div>
        <div className="run-dock-wrapper" hidden={!runOpen || !active}>
          <RunPanel
            tabs={session.tabs}
            activeId={session.activeId}
            theme={theme}
            height={Math.min(layout.run, runMax)}
            maxHeight={runMax}
            maximized={runMaximized}
            pending={pending.has(session.activeId)}
            onHeight={(height) => setSize('run', height)}
            onMaximize={() => setRunMaximized((max) => !max)}
            onClose={() => {
              setRunOpen(false);
              setRunMaximized(false);
            }}
            onNotice={notify}
            onReuse={(id) =>
              changeTab(id, (tab) => ({
                ...tab,
                data: {
                  ...tab.data,
                  draft: { ...tab.data.draft, values: pretty(tab.data.run?.result.values || {}) },
                },
              }))
            }
          />
        </div>
        <footer className="studio-statusbar">
          <span>
            {active ? (active.entryId ? '已关联服务器文档' : '未保存报文') : '没有打开的报文'}
            {active && isDirty(active) ? ' · 有未保存更改' : ''}
          </span>
          <span>
            {workspaceOnline ? `${workspace.entries.length} 个工作区条目` : '工作区离线'} · Ctrl / ⌘
            + S 保存
          </span>
        </footer>
      </div>
      <Input
        hidden
        type="file"
        accept=".json,application/json"
        ref={fileInput}
        aria-label="导入 Studio 文件"
        onChange={async (event) => {
          const file = event.target.files?.[0];
          if (!file) return;
          setImportPending(true);
          try {
            await importFile(file);
          } catch (cause) {
            report(cause);
          } finally {
            event.target.value = '';
            setImportPending(false);
          }
        }}
      />
      {tabContext && session.tabs.some((tab) => tab.id === tabContext.id) && (
        <TabContextMenu
          key={`${tabContext.id}-${tabContext.x}-${tabContext.y}`}
          context={tabContext}
          isSaving={saving.has(tabContext.id)}
          isPinned={!!session.tabs.find((tab) => tab.id === tabContext.id)?.pinned}
          canMoveLeft={session.tabs.some(
            (tab, index) =>
              tab.id === tabContext.id &&
              index > 0 &&
              !!session.tabs[index - 1].pinned === !!tab.pinned,
          )}
          canMoveRight={session.tabs.some(
            (tab, index) =>
              tab.id === tabContext.id &&
              index < session.tabs.length - 1 &&
              !!session.tabs[index + 1].pinned === !!tab.pinned,
          )}
          canCloseOthers={session.tabs.some((tab) => tab.id !== tabContext.id && !tab.pinned)}
          canCloseRight={session.tabs
            .slice(session.tabs.findIndex((tab) => tab.id === tabContext.id) + 1)
            .some((tab) => !tab.pinned)}
          canCloseUnpinned={session.tabs.some((tab) => !tab.pinned)}
          canCloseAll={session.tabs.some((tab) => tab.pinned)}
          onAction={tabAction}
          onClose={() => setTabContext(null)}
        />
      )}
      {action && (
        <WorkspaceDialog
          action={action}
          onClose={(completed) => {
            setAction(null);
            if (!completed) setCloseQueue([]);
          }}
        />
      )}
      <Toast.Provider
        className="studio-toasts"
        placement="bottom"
        maxVisibleToasts={1}
        width="min(460px, calc(100vw - 32px))"
      />
    </div>
  );
}
