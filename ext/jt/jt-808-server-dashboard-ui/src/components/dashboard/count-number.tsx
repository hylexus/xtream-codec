import { useCountUp } from "react-countup";
import { useEffect, useRef, type RefObject } from "react";

export const CountNumber = ({ end }: { end: number }) => {
  const countUpRef = useRef<HTMLSpanElement>(null);
  const { update } = useCountUp({
    ref: countUpRef as unknown as RefObject<HTMLElement>,
    start: 0,
    end,
  });

  useEffect(() => {
    update(end);
  }, [end, update]);

  return <span ref={countUpRef} />;
};
