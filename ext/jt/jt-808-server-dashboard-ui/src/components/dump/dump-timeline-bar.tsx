import { animated, useSpring } from "@react-spring/web";
import { FC, KeyboardEvent, useEffect, useState } from "react";

import type { BarGeometry } from "./timeline-scale.ts";

const SPRING_CONFIG = { tension: 280, friction: 26 };

export interface DumpTimelineBarProps {
  geom: BarGeometry;
  barY: number;
  barHeight: number;
  fill: string;
  opacity: number;
  /** 该行最右侧 segment 首次出现时播放宽度展开 */
  animateEnter?: boolean;
  emphasize?: boolean;
  ariaLabel: string;
  highlighted: boolean;
  onClick: () => void;
  onHover: (active: boolean) => void;
  onKeyDown: (e: KeyboardEvent<SVGRectElement>) => void;
}

type BarRectProps = Pick<
  DumpTimelineBarProps,
  | "geom"
  | "barY"
  | "barHeight"
  | "fill"
  | "opacity"
  | "ariaLabel"
  | "highlighted"
  | "onClick"
  | "onHover"
  | "onKeyDown"
> & {
  width: number;
};

const BarRect: FC<BarRectProps> = ({
  geom,
  barY,
  barHeight,
  fill,
  opacity,
  width,
  ariaLabel,
  highlighted,
  onClick,
  onHover,
  onKeyDown,
}) => (
  <rect
    aria-label={ariaLabel}
    className="outline-none"
    fill={fill}
    height={barHeight}
    opacity={opacity}
    role="button"
    shapeRendering="crispEdges"
    style={{
      cursor: "pointer",
      filter: highlighted ? "brightness(1.2)" : undefined,
    }}
    tabIndex={0}
    width={width}
    x={geom.x}
    y={barY}
    onBlur={() => onHover(false)}
    onClick={onClick}
    onFocus={() => onHover(true)}
    onKeyDown={onKeyDown}
    onMouseEnter={() => onHover(true)}
    onMouseLeave={() => onHover(false)}
  />
);

const EnteringBar: FC<
  DumpTimelineBarProps & { onEntered: () => void }
> = ({
  geom,
  barY,
  barHeight,
  fill,
  opacity,
  emphasize,
  ariaLabel,
  highlighted,
  onClick,
  onHover,
  onKeyDown,
  onEntered,
}) => {
  const [{ width }, api] = useSpring(() => ({
    width: 0,
    config: SPRING_CONFIG,
  }));

  useEffect(() => {
    if (geom.width <= 0) {
      onEntered();

      return;
    }

    let finished = false;

    api.start({
      to: { width: geom.width },
      onRest: () => {
        if (!finished) {
          finished = true;
          onEntered();
        }
      },
    });
  }, [api, geom.width, onEntered]);

  return (
    <>
      {emphasize && (
        <animated.rect
          fill="rgba(255,255,255,0.2)"
          height={barHeight}
          pointerEvents="none"
          shapeRendering="crispEdges"
          width={width}
          x={geom.x}
          y={barY}
        />
      )}
      <animated.rect
        aria-label={ariaLabel}
        className="outline-none"
        fill={fill}
        height={barHeight}
        opacity={opacity}
        role="button"
        shapeRendering="crispEdges"
        style={{
          cursor: "pointer",
          filter: highlighted ? "brightness(1.2)" : undefined,
        }}
        tabIndex={0}
        width={width}
        x={geom.x}
        y={barY}
        onBlur={() => onHover(false)}
        onClick={onClick}
        onFocus={() => onHover(true)}
        onKeyDown={onKeyDown}
        onMouseEnter={() => onHover(true)}
        onMouseLeave={() => onHover(false)}
      />
    </>
  );
};

export const DumpTimelineBar: FC<DumpTimelineBarProps> = (props) => {
  const { animateEnter = false, geom, emphasize, barY, barHeight } = props;
  const [entered, setEntered] = useState(() => !animateEnter);

  if (entered) {
    return (
      <>
        {emphasize && (
          <rect
            fill="rgba(255,255,255,0.2)"
            height={barHeight}
            pointerEvents="none"
            shapeRendering="crispEdges"
            width={geom.width}
            x={geom.x}
            y={barY}
          />
        )}
        <BarRect {...props} width={geom.width} />
      </>
    );
  }

  return <EnteringBar {...props} onEntered={() => setEntered(true)} />;
};
