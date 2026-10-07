#!/usr/bin/env python3
"""
Generate dedicated launcher icons for PersoPhoto AI
Adaptive vector + raster mipmaps for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi
"""
import os
from PIL import Image, ImageDraw, ImageFilter

def create_icon(size):
    # Create RGBA canvas
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Base rounded container (dark studio obsidian #0D1117)
    radius = int(size * 0.22)
    margin = int(size * 0.04)
    draw.rounded_rectangle(
        [margin, margin, size - margin, size - margin],
        radius=radius,
        fill=(13, 17, 23, 255),
        outline=(0, 210, 180, 200),
        width=max(1, int(size * 0.02))
    )

    # Pure White ID Photo Card inside
    card_w = int(size * 0.52)
    card_h = int(size * 0.68)
    card_x0 = (size - card_w) // 2
    card_y0 = (size - card_h) // 2 + int(size * 0.02)
    card_x1 = card_x0 + card_w
    card_y1 = card_y0 + card_h
    card_r = int(size * 0.06)

    # Card shadow
    draw.rounded_rectangle(
        [card_x0 + 2, card_y0 + 4, card_x1 + 2, card_y1 + 4],
        radius=card_r,
        fill=(0, 0, 0, 120)
    )
    # White Card background (Biometric pure white)
    draw.rounded_rectangle(
        [card_x0, card_y0, card_x1, card_y1],
        radius=card_r,
        fill=(255, 255, 255, 255),
        outline=(220, 225, 230, 255),
        width=max(1, int(size * 0.015))
    )

    # Portrait Silhouette inside the card
    head_cx = size // 2
    head_r = int(size * 0.11)
    head_cy = card_y0 + int(card_h * 0.32)

    # Head circle
    draw.ellipse(
        [head_cx - head_r, head_cy - head_r, head_cx + head_r, head_cy + head_r],
        fill=(30, 41, 59, 255)
    )

    # Shoulders / Bust arc
    bust_w = int(card_w * 0.72)
    bust_h = int(card_h * 0.40)
    bust_x0 = head_cx - bust_w // 2
    bust_y0 = head_cy + int(head_r * 0.8)
    bust_x1 = head_cx + bust_w // 2
    bust_y1 = card_y1 - 2

    draw.chord(
        [bust_x0, bust_y0, bust_x1, bust_y1 + int(size * 0.08)],
        start=180, end=360,
        fill=(30, 41, 59, 255)
    )

    # Biometric Alignment Corner Brackets (Cyan #00D2B4)
    bracket_color = (0, 210, 180, 255)
    bw = max(2, int(size * 0.025))
    blen = int(size * 0.09)
    bx0, by0 = card_x0 - int(size * 0.05), card_y0 - int(size * 0.05)
    bx1, by1 = card_x1 + int(size * 0.05), card_y1 + int(size * 0.05)

    # Top-Left
    draw.line([(bx0, by0), (bx0 + blen, by0)], fill=bracket_color, width=bw)
    draw.line([(bx0, by0), (bx0, by0 + blen)], fill=bracket_color, width=bw)
    # Top-Right
    draw.line([(bx1, by0), (bx1 - blen, by0)], fill=bracket_color, width=bw)
    draw.line([(bx1, by0), (bx1, by0 + blen)], fill=bracket_color, width=bw)
    # Bottom-Left
    draw.line([(bx0, by1), (bx0 + blen, by1)], fill=bracket_color, width=bw)
    draw.line([(bx0, by1), (bx0, by1 - blen)], fill=bracket_color, width=bw)
    # Bottom-Right
    draw.line([(bx1, by1), (bx1 - blen, by1)], fill=bracket_color, width=bw)
    draw.line([(bx1, by1), (bx1, by1 - blen)], fill=bracket_color, width=bw)

    # Studio Camera Lens Aperture / Flash Badge at bottom right (Terracotta #FF6B4A)
    badge_r = int(size * 0.12)
    badge_cx = card_x1 + int(size * 0.04)
    badge_cy = card_y1 + int(size * 0.02)
    draw.ellipse(
        [badge_cx - badge_r, badge_cy - badge_r, badge_cx + badge_r, badge_cy + badge_r],
        fill=(255, 107, 74, 255),
        outline=(255, 255, 255, 255),
        width=max(1, int(size * 0.02))
    )
    # Camera Flash / Spark icon inside badge
    spark_color = (255, 255, 255, 255)
    sw = max(1, int(size * 0.015))
    draw.line([(badge_cx, badge_cy - int(badge_r * 0.55)), (badge_cx, badge_cy + int(badge_r * 0.55))], fill=spark_color, width=sw)
    draw.line([(badge_cx - int(badge_r * 0.55), badge_cy), (badge_cx + int(badge_r * 0.55), badge_cy)], fill=spark_color, width=sw)
    draw.ellipse(
        [badge_cx - int(badge_r * 0.2), badge_cy - int(badge_r * 0.2), badge_cx + int(badge_r * 0.2), badge_cy + int(badge_r * 0.2)],
        fill=spark_color
    )

    return img

def main():
    base_res = "/var/minis/workspace/persophoto-ai/app/src/main/res"
    sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    for folder, size in sizes.items():
        out_dir = os.path.join(base_res, folder)
        os.makedirs(out_dir, exist_ok=True)
        icon = create_icon(size)
        icon.save(os.path.join(out_dir, "ic_launcher.png"))
        icon.save(os.path.join(out_dir, "ic_launcher_round.png"))
        print(f"Generated {folder}/ic_launcher.png ({size}x{size})")

    # High-res 512x512 preview for repo / docs
    preview = create_icon(512)
    os.makedirs("/var/minis/workspace/persophoto-ai/docs", exist_ok=True)
    preview.save("/var/minis/workspace/persophoto-ai/docs/app_icon_512.png")
    preview.save("/var/minis/attachments/persophoto_icon.png")
    print("Generated 512x512 preview icon")

if __name__ == "__main__":
    main()
