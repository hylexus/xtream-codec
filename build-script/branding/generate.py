#!/usr/bin/env python3
# Copyright 2026 the xtream-codec authors. SPDX-License-Identifier: Apache-2.0
"""根据几何参数生成项目彩色标志的主 SVG。"""

from pathlib import Path


def arrow_path(x, y, direction):
    """从箭头尖端和方向生成带圆角的箭头路径。"""

    def point(dx, dy):
        return f"{x + direction * dx} {y + dy}"

    return (
        f"M{point(0, -1)}L{point(12, -11)}Q{point(14, -13)} {point(14, -10)}"
        f"V{y + 10}Q{point(14, 13)} {point(12, 11)}L{point(0, 1)}"
        f"Q{point(-2, 0)} {point(0, -1)}Z"
    )


def draw_mark():
    size = 96
    corner_radius = 24
    background = "#0B1224"
    blue = "#4D7CFE"
    teal = "#22D3C5"
    stroke_width = 10
    crossing_width = stroke_width + 4
    lower_y = 66
    upper_y = size - lower_y
    curve = f"M29 {lower_y}C48 {lower_y} 48 {upper_y} 67 {upper_y}"
    arrows = arrow_path(16, lower_y, 1) + arrow_path(size - 16, upper_y, -1)

    # 上层通道沿水平中线镜像，较宽的背景描边在交叉处留出间隙。
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}" fill="none" role="img" aria-label="xtream-codec">
  <rect width="{size}" height="{size}" rx="{corner_radius}" fill="{background}"/>
  <g>
    <path d="{curve}" stroke="{blue}" stroke-width="{stroke_width}"/>
    <path d="{arrows}" fill="{blue}"/>
  </g>
  <g transform="translate(0 {size}) scale(1 -1)">
    <path d="{curve}" stroke="{background}" stroke-width="{crossing_width}"/>
    <path d="{curve}" stroke="{teal}" stroke-width="{stroke_width}"/>
    <path d="{arrows}" fill="{teal}"/>
  </g>
</svg>
'''


if __name__ == "__main__":
    root = Path(__file__).resolve().parents[2]
    (root / "assets/branding/mark.svg").write_bytes(draw_mark().encode("utf-8"))
