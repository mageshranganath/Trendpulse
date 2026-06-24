from PIL import Image, ImageDraw, ImageFont

W, H = 320, 3840
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


def draw_chart(draw, y, points, color):
    x0, x1 = 24, W - 24
    y0, y1 = y, y + 220
    draw.rectangle((x0, y0, x1, y1), fill=(247, 251, 255))
    for i in range(5):
        gy = y0 + int((y1 - y0) * i / 4)
        draw.line((x0, gy, x1, gy), fill=(225, 234, 244), width=1)
    scaled = []
    for i, p in enumerate(points):
        xx = x0 + int((x1 - x0) * i / max(1, len(points) - 1))
        yy = y1 - int((y1 - y0) * p)
        scaled.append((xx, yy))
    draw.line(scaled, fill=color, width=4)
    for xx, yy in scaled:
        draw.ellipse((xx - 3, yy - 3, xx + 3, yy + 3), fill=color)


def draw_screen(index: int):
    palettes = [
        ((232, 245, 255), (247, 250, 255), (23, 98, 188)),
        ((235, 255, 248), (248, 255, 252), (16, 130, 86)),
        ((255, 241, 234), (255, 250, 245), (200, 76, 28)),
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

    # Header
    for y in range(115):
        t = y / 114
        r = int(accent[0] * (1 - t) + 20 * t)
        g = int(accent[1] * (1 - t) + 70 * t)
        b = int(accent[2] * (1 - t) + 120 * t)
        draw.line((0, y, W, y), fill=(r, g, b))

    draw.text((18, 20), "CTrend Tablet", font=get_font(20, bold=True), fill=(255, 255, 255))
    draw.text((18, 52), f"Showcase {index + 1} of {COUNT}", font=get_font(12), fill=(225, 240, 255))

    y = 136
    rounded_rect(draw, (12, y, W - 12, y + 240), radius=18, fill=(255, 255, 255), outline=(220, 228, 240), width=2)
    draw.text((24, y + 16), "Market Snapshot", font=get_font(14, bold=True), fill=(20, 40, 70))
    draw.text((24, y + 56), "CAD/INR", font=get_font(12), fill=(100, 116, 139))
    draw.text((24, y + 82), f"66.{70 + index}", font=get_font(34, bold=True), fill=accent)
    change = f"+{1.6 + index * 0.2:.1f}%"
    draw.text((24, y + 132), f"24h {change}", font=get_font(13, bold=True), fill=(21, 128, 61))

    y += 270
    rounded_rect(draw, (12, y, W - 12, y + 430), radius=18, fill=(255, 255, 255), outline=(220, 228, 240), width=2)
    draw.text((24, y + 16), "Trend Chart", font=get_font(14, bold=True), fill=(20, 40, 70))
    points = [0.16 + 0.02 * index, 0.20, 0.24, 0.21, 0.29, 0.35, 0.33, 0.41]
    draw_chart(draw, y + 52, points, accent)
    draw.text((24, y + 286), "30-day volatility: low", font=get_font(12), fill=(71, 85, 105))

    y += 460
    rounded_rect(draw, (12, y, W - 12, y + 720), radius=18, fill=(255, 255, 255), outline=(220, 228, 240), width=2)
    draw.text((24, y + 16), "Recent Entries", font=get_font(14, bold=True), fill=(20, 40, 70))
    row_y = y + 56
    for i in range(9):
        bg = (247, 250, 255) if i % 2 == 0 else (255, 255, 255)
        rounded_rect(draw, (20, row_y, W - 20, row_y + 62), radius=10, fill=bg, outline=(228, 235, 245), width=1)
        draw.text((30, row_y + 21), f"2026-06-{23 - i:02d}", font=get_font(11), fill=(71, 85, 105))
        draw.text((W - 86, row_y + 19), f"66.{70 - i + index}", font=get_font(13, bold=True), fill=accent)
        row_y += 70

    y += 750
    rounded_rect(draw, (12, y, W - 12, H - 20), radius=18, fill=(255, 255, 255), outline=(220, 228, 240), width=2)
    draw.text((24, y + 16), "Insights", font=get_font(14, bold=True), fill=(20, 40, 70))

    paragraph = (
        "Currency Trend provides quick visual analytics for exchange movement. "
        "Users can switch base and target currencies, choose time windows, and "
        "inspect trend behavior for planning and monitoring."
    )
    text_font = get_font(12)
    yy = y + 50
    while yy < H - 40:
        draw.text((24, yy), paragraph, font=text_font, fill=(71, 85, 105))
        yy += 95

    out = f"tablet-screenshot-{index + 1}-320x3840.png"
    img.save(out, "PNG")
    return out


def main():
    files = []
    for i in range(COUNT):
        files.append(draw_screen(i))
    for f in files:
        print(f"Saved {f} ({W}x{H})")


if __name__ == "__main__":
    main()
