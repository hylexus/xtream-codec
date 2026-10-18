import { useState } from 'react';
import { Alert, Button, Modal } from '@heroui/react';
import type { WorkspaceEntry } from '../workspace';

export interface SaveConflictAction {
  tabId: string;
  server: WorkspaceEntry;
  resolve: (source: 'server' | 'local') => Promise<void>;
  cancel: () => void;
}

export function SaveConflictDialog({ action }: { action: SaveConflictAction }) {
  const [pending, setPending] = useState<'server' | 'local' | null>(null);
  const [error, setError] = useState('');
  const choose = async (source: 'server' | 'local') => {
    if (pending) return;
    setPending(source);
    setError('');
    try {
      await action.resolve(source);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause));
    } finally {
      setPending(null);
    }
  };
  return (
    <Modal
      isOpen
      onOpenChange={(open) => {
        if (!open && !pending) action.cancel();
      }}
    >
      <Modal.Backdrop variant="blur" isDismissable={!pending}>
        <Modal.Container size="md">
          <Modal.Dialog className="workspace-dialog">
            <Modal.CloseTrigger isDisabled={!!pending} />
            <Modal.Header>
              <Modal.Heading>报文保存冲突</Modal.Heading>
            </Modal.Header>
            <Modal.Body>
              <p>“{action.server.name}”已被修改，本地编辑尚未保存。</p>
              <p className="muted">
                服务器版本 v{action.server.revision} · 更新时间{' '}
                {new Date(action.server.updatedAt).toLocaleString()}
              </p>
              <p>
                使用服务器版本将放弃本地未保存的修改；用本地版本覆盖服务器将替换服务器上的报文内容和运行结果，保留服务器名称和目录。
              </p>
              {error && (
                <Alert status="danger">
                  <Alert.Content>
                    <Alert.Description>{error}</Alert.Description>
                  </Alert.Content>
                </Alert>
              )}
            </Modal.Body>
            <Modal.Footer className="flex-wrap">
              <Button variant="ghost" isDisabled={!!pending} onPress={action.cancel} autoFocus>
                取消
              </Button>
              <Button
                variant="secondary"
                isDisabled={!!pending}
                isPending={pending === 'server'}
                onPress={() => void choose('server')}
              >
                使用服务器版本
              </Button>
              <Button
                variant="danger"
                isDisabled={!!pending}
                isPending={pending === 'local'}
                onPress={() => void choose('local')}
              >
                用本地版本覆盖服务器
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
