from PIL import Image, ImageDraw, ImageFont

W, H = 2560, 1440  # 10-inch style, 16:9
COUNT = 8


def get_font(size: int, bold: bool = False):
    candidates = [
        "arialbd.ttf" if bold else "arial.ttf",
        "segoeuib.ttf" if bold else "segoeui.ttf",
        "DejaVuSans-Bold.ttf" if bold else "DejaVuSans.ttf",
    ]
    for name in candidates:
        try:
            return ImageFont.truetype(name, size)
        except Exception:
            continue
    return ImageFont.load_default()


def rounded_rect(draw, xy, radius, fill, outline=None, width=1):
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def gradient_background(img, top, bottom):
    draw = ImageDraw.Draw(img)
    for y in range(H):
        t = y / max(1, H - 1)
        r = int(top[0] + (bottom[0] - top[0]) * t)
        g = int(top[1] + (bottom[1] - top[1]) * t)
        b = int(top[2] + (bottom[2] - top[2]) * t)
        draw.line((0, y, W, y), fill=(r, g, b))


def draw_chart(draw, x0, y0, x1, y1, points, color):
    draw.rectangle((x0, y0, x1, y1), fill=(247, 251, 255))
    for i in range(5):
        gy = y0 + int((y1 - y0) * i / 4)
        draw.line((x0, gy, x1, gy), fill=(225, 234, 244), width=3)
    scaled = []
    for i, p in enumerate(points):
        xx = x0 + int((x1 - x0) * i / max(1, len(points) - 1))
        yy = y1 - int((y1 - y0) * p)
        scaled.append((xx, yy))
    draw.line(scaled, fill=color, width=10)
    for xx, yy in scaled:
        draw.ellipse((xx - 10, yy - 10, xx + 10, yy + 10), fill=color)


def draw_screen(index: int) -> str:
    palettes = [
        ((230, 243, 255), (246, 250, 255), (23, 98, 188)),
        ((233, 253, 244), (247, 255, 252), (16, 130, 86)),
        ((255, 241, 234), (255, 251, 246), (200, 76, 28)),
        ((244, 240, 255), (252, 249, 255), (124, 58, 237)),
        ((232, 250, 252), (246, 254, 255), (6, 148, 162)),
        ((255, 247, 230), (255, 252, 242), (202, 138, 4)),
        ((239, 246, 255), (248, 251, 255), (37, 99, 235)),
        ((240, 253, 244), (249, 255, 251), (21, 128, 61)),
    ]
    top, bottom, accent = palettes[index]

    img = Image.new("RGB", (W, H), (245, 250, 255))
    gradient_background(img, top, bottom)
    draw = ImageDraw.Draw(img)

    # Header bar
    for y in range(150):
        t = y / 149
        r = int(accent[0] * (1 - t) + 15 * t)
        g = int(accent[1] * (1 - t) + 56 * t)
        b = int(accent[2] * (1 - t) + 105 * t)
        draw.line((0, y, W, y), fill=(r, g, b))

    draw.text((56, 36), "CTrend", font=get_font(58, bold=True), fill=(255, 255, 255))
    draw.text((56, 98), f"10-inch Tablet Showcase {index + 1}/8", font=get_font(30), fill=(219, 235, 255))

    # Left panel
    left_x0, left_x1 = 48, 940
    y = 188
    rounded_rect(draw, (left_x0, y, left_x1, y + 340), 28, fill=(255, 255, 255), outline=(220, 228, 240), width=4)
    draw.text((left_x0 + 30, y + 24), "Market Snapshot", font=get_font(40, bold=True), fill=(20, 40, 70))
    draw.text((left_x0 + 30, y + 96), "CAD -> INR", font=get_font(30), fill=(100, 116, 139))
    draw.text((left_x0 + 30, y + 138), f"66.{72 + index}", font=get_font(82, bold=True), fill=accent)
    draw.text((left_x0 + 30, y + 248), f"24h: +{1.6 + 0.2 * index:.1f}%", font=get_font(30, bold=True), fill=(21, 128, 61))

    y += 376
    rounded_rect(draw, (left_x0, y, left_x1, y + 500), 28, fill=(255, 255, 255), outline=(220, 228, 240), width=4)
    draw.text((left_x0 + 30, y + 22), "Recent Rates", font=get_font(38, bold=True), fill=(20, 40, 70))
    row_y = y + 92
    for i in range(5):
        bg = (247, 250, 255) if i % 2 == 0 else (255, 255, 255)
        rounded_rect(draw, (left_x0 + 22, row_y, left_x1 - 22, row_y + 76), 14, fill=bg, outline=(228, 235, 245), width=2)
        draw.text((left_x0 + 36, row_y + 24), f"2026-06-{23 - i:02d}", font=get_font(24), fill=(71, 85, 105))
        draw.text((left_x1 - 160, row_y + 19), f"66.{73 - i + index}", font=get_font(30, bold=True), fill=accent)
        row_y += 88

    # Right panel
    right_x0, right_x1 = 980, W - 48
    y = 188
    rounded_rect(draw, (right_x0, y, right_x1, y + 700), 28, fill=(255, 255, 255), outline=(220, 228, 240), width=4)
    draw.text((right_x0 + 30, y + 24), "Trend Chart", font=get_font(40, bold=True), fill=(20, 40, 70))
    pts = [0.15 + 0.02 * index, 0.20, 0.23, 0.19, 0.28, 0.36, 0.33, 0.41, 0.45]
    draw_chart(draw, right_x0 + 30, y + 96, right_x1 - 30, y + 580, pts, accent)
    draw.text((right_x0 + 30, y + 602), "30-day trend: moderate upward movement", font=get_font(30), fill=(71, 85, 105))

    y += 736
    rounded_rect(draw, (right_x0, y, right_x1, y + 468), 28, fill=(255, 255, 255), outline=(220, 228, 240), width=4)
    draw.text((right_x0 + 30, y + 24), "Insights", font=get_font(40, bold=True), fill=(20, 40, 70))
    para = (
        "Compare currency pairs, switch periods, and inspect trend behavior in real time. "
        "CTrend is optimized for large-screen readability and decision support."
    )
    draw.text((right_x0 + 30, y + 98), para, font=get_font(30), fill=(71, 85, 105))

    out = f"tablet-10inch-screenshot-{index + 1}-16x9-2560x1440.png"
    img.save(out, "PNG")
    return out


def main():
    files = [draw_screen(i) for i in range(COUNT)]
    for f in files:
        print(f"Saved {f} ({W}x{H})")


if __name__ == "__main__":
    main()
