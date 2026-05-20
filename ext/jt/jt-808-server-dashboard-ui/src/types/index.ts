import { SVGProps } from "react";

export type IconSvgProps = SVGProps<SVGSVGElement> & {
  size?: number;
};

export interface Session {
  id: string;
  terminalId: string;
  serverType: string;
  protocolVersion: string;
  protocolType: string;
  creationTime: string;
  lastCommunicateTime: string;
}

export type SessionType = "instruction" | "attachment";

export interface Event {
  requestId: string;
  traceId: string;
  version: string;
  isSubPackage: string;
  messageId: string;
  messageDesc: string;
  hexString: string;
  rawHexString: string;
  escapedHexString: string;
  type: EventType;
  eventTime: string;
  remoteAddress: string;
  reason: string;
}

export enum EventType {
  ALL = -1, // 所有事件
  BEFORE_COMMAND_SEND = -103, // 指令下发
  BEFORE_RESPONSE_SEND, // 发送响应
  AFTER_SUB_REQUEST_MERGED, // 合并请求
  AFTER_REQUEST_RECEIVED, // 收到请求
  AFTER_SESSION_CREATED, // Session创建
  BEFORE_SESSION_CLOSED, // Session关闭
}

// interface SessionCount {
//   max: number;
//   current: number;
// }
// interface SessionRequest {
//   total: number;
//   details: any;
// }

export interface Metrics {
  [key: string]: any;
}

export interface Thread {
  time: string;
  name?: string;
  remark?: string;
  metadata?: {
    [key: string]: any;
  };
  value: {
    [key: string]: number;
  };
}

export interface JavaRuntime {
  name: String;
  version: String;
}

export interface JavaVendor {
  name: String;
  version: String;
}

export interface JvmInfo {
  name: String;
  vendor: String;
  version: String;
}

export interface JavaInfo {
  version: String;
  jvm: JvmInfo;
  runtime: JavaRuntime;
  vendor: JavaVendor;
}

export interface OsInfo {
  name: String;
  arch: string;
  version: string;
}

export interface DependencyInfo {
  name: string;
  version: string;
}

export interface Dependencies {
  spring: DependencyInfo;
  springBoot: DependencyInfo;
  xtreamCodec: DependencyInfo;
}

export interface TcpServerProperties {
  enabled: boolean;
  // 省略其他属性
}

export interface UdpServerProperties {
  enabled: boolean;
  // 省略其他属性
}

export interface ServerInfo {
  dependencies: Dependencies;
  // 服务启动时间
  serverStartupTime: string;
  // 服务配置(application.yaml#jt808-server.*)
  jt808ServerConfig: {
    instructionServer?: {
      tcpServer: TcpServerProperties;
      udpServer: UdpServerProperties;
    };
    attachmentServer?: {
      tcpServer: TcpServerProperties;
      udpServer: UdpServerProperties;
    };
  };
  java: JavaInfo;
  os: OsInfo;
}

export interface Dic {
  [key: string]: any;
}
