import { Table } from "@heroui/react";
import { FC, useMemo, type ReactNode } from "react";

import { getKeyValue } from "@/utils/get-key-value.ts";

export const MsgMiniTable: FC<{ data: any }> = ({ data }) => {
  const tableData = useMemo(() => {
    if (!data || typeof data !== "object") {
      return [];
    }

    return Object.keys(data).map((key) => {
      const cellValue = getKeyValue(data, key);

      return {
        id: key,
        ...(typeof cellValue === "object" && cellValue !== null
          ? (cellValue as object)
          : { value: cellValue }),
      };
    });
  }, [data]);

  const columns = [
    { id: "messageIdAsHexString", name: "消息ID" },
    { id: "desc", name: "消息描述" },
    { id: "count", name: "总数" },
  ];

  return (
    <Table.Root className="max-h-[520px] min-h-[100px] w-full overflow-auto shadow-none">
      <Table.ScrollContainer>
        <Table.Content aria-label="Detail">
          <Table.Header>
            {columns.map((col) => (
              <Table.Column
                key={col.id}
                isRowHeader={col.id === "messageIdAsHexString"}
              >
                {col.name}
              </Table.Column>
            ))}
          </Table.Header>
          <Table.Body items={tableData}>
            {(item: any) => (
              <Table.Row id={String(item.id)}>
                {columns.map((col) => (
                  <Table.Cell key={col.id}>
                    {getKeyValue(item, col.id) as ReactNode}
                  </Table.Cell>
                ))}
              </Table.Row>
            )}
          </Table.Body>
        </Table.Content>
      </Table.ScrollContainer>
    </Table.Root>
  );
};
