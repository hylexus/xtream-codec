import { useState } from 'react';
import { Alert, Button, Input, Label, ListBox, Modal, Select, TextField } from '@heroui/react';
import type { WorkspaceEntry } from '../workspace';

export interface DialogAction {
  title: string;
  description?: string;
  name?: string;
  parentId?: string | null;
  folders?: WorkspaceEntry[];
  confirm?: string;
  danger?: boolean;
  discard?: () => void;
  submit: (name: string, parentId: string | null) => Promise<void>;
}

export function WorkspaceDialog({
  action,
  onClose,
}: {
  action: DialogAction;
  onClose: (completed?: boolean) => void;
}) {
  const [name, setName] = useState(action.name || '');
  const [parentId, setParentId] = useState(action.parentId || 'root');
  const [pending, setPending] = useState(false);
  const [error, setError] = useState('');
  const submit = async () => {
    setPending(true);
    setError('');
    try {
      await action.submit(name.trim(), parentId === 'root' ? null : parentId);
      onClose(true);
    } catch (cause) {
      setError((cause as Error).message);
    } finally {
      setPending(false);
    }
  };
  return (
    <Modal
      isOpen
      onOpenChange={(open) => {
        if (!open && !pending) onClose();
      }}
    >
      <Modal.Backdrop variant="blur" isDismissable={!pending}>
        <Modal.Container size="sm">
          <Modal.Dialog className="workspace-dialog">
            <Modal.CloseTrigger isDisabled={pending} />
            <Modal.Header>
              <Modal.Heading>{action.title}</Modal.Heading>
            </Modal.Header>
            <Modal.Body>
              {action.description && <p className="muted">{action.description}</p>}
              {action.name !== undefined && (
                <TextField value={name} onChange={setName} isDisabled={pending} autoFocus>
                  <Label>名称</Label>
                  <Input
                    onKeyDown={(event) => {
                      if (event.key === 'Enter') void submit();
                    }}
                  />
                </TextField>
              )}
              {action.folders && (
                <Select
                  fullWidth
                  value={parentId}
                  onChange={(key) => {
                    if (key !== null) setParentId(String(key));
                  }}
                  isDisabled={pending}
                >
                  <Label>保存目录</Label>
                  <Select.Trigger>
                    <Select.Value />
                    <Select.Indicator />
                  </Select.Trigger>
                  <Select.Popover>
                    <ListBox items={[{ id: 'root', name: '我的报文 / 根目录' }, ...action.folders]}>
                      {(folder) => (
                        <ListBox.Item id={folder.id} textValue={folder.name}>
                          <Label>{folder.name}</Label>
                          <ListBox.ItemIndicator />
                        </ListBox.Item>
                      )}
                    </ListBox>
                  </Select.Popover>
                </Select>
              )}
              {error && (
                <Alert status="danger">
                  <Alert.Content>
                    <Alert.Description>{error}</Alert.Description>
                  </Alert.Content>
                </Alert>
              )}
            </Modal.Body>
            <Modal.Footer>
              <Button variant="ghost" isDisabled={pending} onPress={() => onClose()}>
                取消
              </Button>
              {action.discard && (
                <Button
                  variant="secondary"
                  isDisabled={pending}
                  onPress={() => {
                    action.discard?.();
                    onClose(true);
                  }}
                >
                  放弃并关闭
                </Button>
              )}
              <Button
                variant={action.danger ? 'danger' : 'primary'}
                isPending={pending}
                onPress={() => void submit()}
              >
                {action.confirm || '确定'}
              </Button>
            </Modal.Footer>
          </Modal.Dialog>
        </Modal.Container>
      </Modal.Backdrop>
    </Modal>
  );
}
