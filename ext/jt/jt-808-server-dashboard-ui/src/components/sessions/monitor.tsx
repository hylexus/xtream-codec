import {
  Avatar,
  Button,
  Card,
  Drawer,
  Dropdown,
  Input,
  Label,
  ScrollShadow,
  type Selection,
  TextField,
  useOverlayState,
} from "@heroui/react";
import {
  Dispatch,
  FC,
  SetStateAction,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import {
  EventSourceMessage,
  fetchEventSource,
} from "@microsoft/fetch-event-source";
import clsx from "clsx";

import {
  FaChevronDownIcon,
  FaDesktopIcon,
  FaRobotIcon,
  FaServerIcon,
} from "@/components/icons.tsx";
import { subtitle } from "@/components/primitives.ts";
import { Event, EventType, Session } from "@/types";
const SESSION_MAX_LENGTH = "100";

interface MessageProps {
  item: Event;
  className?: string;
}

const eventKeyDesc = (key: string): string => {
  switch (key) {
    case "messageId":
      return "消息ID";
    case "hexString":
      return "报文";
    case "eventTime":
      return "时间";
    default:
      return key;
  }
};
const Message: FC<MessageProps> = ({ item, className }) => {
  const rowDisplay = useMemo(() => {
    return [
      EventType.AFTER_REQUEST_RECEIVED,
      EventType.AFTER_SUB_REQUEST_MERGED,
    ].includes(Number(item.type))
      ? {
          name: "C",
          avatar: <FaServerIcon className="animate-pulse text-success" />,
          bg: "bg-background-tertiary",
        }
      : {
          name: "S",
          avatar: <FaDesktopIcon className="animate-pulse text-accent" />,
          bg: "bg-surface",
        };
  }, [item]);

  return [
    EventType.AFTER_SESSION_CREATED,
    EventType.BEFORE_SESSION_CLOSED,
  ].includes(Number(item.type)) ? (
    <div className="flex gap-3">
      <div className="w-12">
        <Avatar>
          <Avatar.Fallback>
            <FaRobotIcon className="text-primary" />
          </Avatar.Fallback>
        </Avatar>
      </div>
      <Card className="w-full flex-grow-0">
        <Card.Content className="text-center">
          <p className="text-primary line-clamp-2 text-sm">{`Session${EventType.BEFORE_SESSION_CLOSED ? " closed" : " opened"} at: ${item.eventTime} remoteAddress: ${item.remoteAddress} reason: ${item.reason}`}</p>
        </Card.Content>
      </Card>
      <div className="w-12" />
    </div>
  ) : (
    <div className={clsx(className, "flex gap-3")}>
      <div className="w-12">
        {rowDisplay.name === "C" && (
          <Avatar>
            <Avatar.Fallback>{rowDisplay.avatar}</Avatar.Fallback>
          </Avatar>
        )}
      </div>
      <Card
        className={clsx(
          "w-full flex-grow-0 border border-border",
          rowDisplay.bg,
        )}
      >
        <Card.Content>
          {Object.keys(item)
            .filter((k) => ["messageId", "hexString", "eventTime"].includes(k))
            .map((e, i) => {
              const messageDesc = item["messageDesc"];
              const messageDescText = messageDesc ? `(${messageDesc})` : "";

              return (
                <p
                  key={i}
                  style={{ wordBreak: "break-all" }}
                >{`${eventKeyDesc(e)}: ${e === "messageId" ? "0x" + item[e as keyof Event].toString(16).padStart(4, "0") + messageDescText : item[e as keyof Event]}`}</p>
              );
            })}
        </Card.Content>
      </Card>
      <div className="w-12">
        {rowDisplay.name === "S" && (
          <Avatar>
            <Avatar.Fallback>{rowDisplay.avatar}</Avatar.Fallback>
          </Avatar>
        )}
      </div>
    </div>
  );
};

export interface SessionMonitorProps {
  row: Session | null;
  isOpen: boolean;
  setIsOpen: Dispatch<SetStateAction<boolean>>;
}
export const SessionMonitor: FC<SessionMonitorProps> = ({
  row,
  isOpen,
  setIsOpen,
}) => {
  const [max, setMax] = useState(SESSION_MAX_LENGTH);
  const [selected, setSelected] = useState<Selection>("all");
  const [linkData, setLinkData] = useState<Event[]>([]);
  const drawerState = useOverlayState({
    isOpen,
    onOpenChange: setIsOpen,
  });

  useEffect(() => {
    if (!isOpen || !row?.terminalId) {
      return;
    }
    const ctrl = new AbortController();

    fetchEventSource(
      `${import.meta.env.VITE_API_DASHBOARD_V1}event/link-data?terminalId=${row.terminalId}`,
      {
        method: "GET",
        signal: ctrl.signal,
        onmessage: (event: EventSourceMessage) => {
          const data: any = JSON.parse(event.data);

          data.type = event.event;
          setLinkData((pre) => {
            if (pre.length <= Number(max)) {
              return pre.concat([data]);
            } else {
              return pre
                .concat([data])
                .filter((_e, i) => pre.length - i < Number(max));
            }
          });
        },
      },
    ).then(() => {
      // TODO
    });

    return () => {
      ctrl.abort();
    };
  }, [isOpen, row?.terminalId, max]);

  useEffect(() => {
    setLinkData((pre) => {
      if (pre.length <= Number(max)) {
        return pre;
      } else {
        return pre.filter((_e, i) => i < Number(max));
      }
    });
  }, [max]);
  const listBottomRef = useRef<HTMLDivElement>(null);
  const scrollToIndex = () => {
    const listNode = listBottomRef.current;

    listNode?.scrollIntoView({
      behavior: "smooth",
      block: "start",
      inline: "center",
    });
  };
  const eventList = [];

  for (const key in EventType) {
    if (!["-1", "ALL"].includes(key) && !isNaN(Number(key))) {
      eventList.push({ key, name: key });
    }
  }
  const filteredLinkData = useMemo(() => {
    if (selected === "all") {
      return linkData;
    }
    const keys = selected as Set<React.Key>;

    if (keys.size === eventList.length) {
      return linkData;
    }

    return linkData.filter((e) => keys.has(String(e.type))) || [];
  }, [linkData, selected, eventList.length]);

  useEffect(() => {
    scrollToIndex();
  }, [filteredLinkData, isOpen]);

  return (
    <Drawer state={drawerState}>
      <Drawer.Backdrop isDismissable variant="blur" />
      <Drawer.Content className="max-w-[min(100vw,56rem)]" placement="right">
        <Drawer.Dialog>
          <Drawer.Header className="flex justify-between gap-1">
            <div className={subtitle()}>terminalId: {row?.terminalId}</div>
          </Drawer.Header>
          <Drawer.Body>
            <div className="flex items-center gap-4">
              <TextField
                className="w-1/6 min-w-32"
                value={max}
                onChange={setMax}
              >
                <Label>Max</Label>
                <Input type="number" />
              </TextField>
              <Dropdown>
                <Dropdown.Trigger className="hidden sm:flex">
                  <Button variant="secondary">
                    <span className="flex items-center gap-2">
                      Event
                      <FaChevronDownIcon className="text-small" />
                    </span>
                  </Button>
                </Dropdown.Trigger>
                <Dropdown.Popover>
                  <Dropdown.Menu
                    disallowEmptySelection
                    aria-label="Event"
                    selectedKeys={selected}
                    selectionMode="multiple"
                    onSelectionChange={setSelected}
                  >
                    {eventList.map((event) => (
                      <Dropdown.Item
                        key={event.key}
                        className="capitalize"
                        id={event.key}
                        textValue={event.name}
                      >
                        {event.name}
                      </Dropdown.Item>
                    ))}
                  </Dropdown.Menu>
                </Dropdown.Popover>
              </Dropdown>
            </div>
            <ScrollShadow
              hideScrollBar
              className="mt-4 flex flex-col gap-4 px-1"
            >
              {filteredLinkData?.map((item: any, index: number) => (
                <Message key={index} className="message-card" item={item} />
              ))}
              <div ref={listBottomRef} />
            </ScrollShadow>
          </Drawer.Body>
        </Drawer.Dialog>
      </Drawer.Content>
    </Drawer>
  );
};
