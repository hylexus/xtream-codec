import { Chip } from "@heroui/react";

export function SessionStatBadges({
  serverRole,
  protocolType,
}: {
  serverRole: string;
  protocolType: string;
}) {
  const isAttachment = serverRole === "附件服务器";

  return (
    <>
      <Chip
        color={isAttachment ? "warning" : "success"}
        size="sm"
        variant="soft"
      >
        {isAttachment ? "附件" : "指令"}
      </Chip>
      <Chip
        color={protocolType === "TCP" ? "accent" : "default"}
        size="sm"
        variant="soft"
      >
        {protocolType}
      </Chip>
    </>
  );
}
