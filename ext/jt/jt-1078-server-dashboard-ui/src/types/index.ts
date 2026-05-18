import { SVGProps } from "react";

export type IconSvgProps = SVGProps<SVGSVGElement> & {
  size?: number;
};

export interface Jt1078Session {
  id: string;
  simLength: number;
  convertedSim: string;
  rawSim: string;
  protocolType: string;
  audioType?: string | null;
  videoType?: string | null;
  creationTime: string;
  lastCommunicateTime: string;
}

export interface Jt1078Subscriber {
  id: string;
  convertedSim: string;
  rawSim: string;
  channel: number;
  rawAudioType?: string | null;
  rawVideoType?: string | null;
  convertedAudioType?: string | null;
  createdAt: string;
  desc?: string;
  metadata?: Record<string, unknown>;
}

export interface PageableVo<T> {
  total: number;
  data: T[];
}

export interface Dic {
  [key: string]: unknown;
}
