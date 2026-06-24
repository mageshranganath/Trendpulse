from PIL import Image, ImageDraw, ImageFont

W, H = 320, 3840
OUT1 = "screenshot-1-320x3840.png"
OUT2 = "screenshot-2-320x3840.png"


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


def draw_header(draw, title, subtitle):
    title_font = get_font(24, bold=True)
    sub_font = get_font(14, bold=False)
    draw.text((20, 24), title, font=title_font, fill=(255, 255, 255))
    draw.text((20, 56), subtitle, font=sub_font, fill=(220, 240, 255))


def draw_card(draw, y, h, title):
    rounded_rect(draw, (12, y, W - 12, y + h), radius=18, fill=(255, 255, 255), outline=(220, 228, 240), width=2)
    draw.text((24, y + 16), title, font=get_font(14, bold=True), fill=(16, 46, 80))


def draw_line_chart(draw, x0, y0, x1, y1, points, line_color):
    draw.rectangle((x0, y0, x1, y1), fill=(247, 251, 255))
    # grid
    for i in range(5):
        yy = y0 + int((y1 - y0) * i / 4)
        draw.line((x0, yy, x1, yy), fill=(226, 234, 245), width=1)
    # line
    scaled = []
    for i, p in enumerate(points):
        xx = x0 + int((x1 - x0) * i / max(1, len(points) - 1))
        yy = y1 - int((y1 - y0) * p)
        scaled.append((xx, yy))
    draw.line(scaled, fill=line_color, width=4)
    for xx, yy in scaled:
        draw.ellipse((xx - 3, yy - 3, xx + 3, yy + 3), fill=line_color)


def draw_screenshot_one():
    img = Image.new("RGB", (W, H), (236, 245, 255))
    draw = ImageDraw.Draw(img)

    # top gradient
    for y in range(120):
        t = y / 119
        r = int(20 + (12 - 20) * t)
        g = int(110 + (82 - 110) * t)
        b = int(180 + (146 - 180) * t)
        draw.line((0, y, W, y), fill=(r, g, b))

    draw_header(draw, "CTrend", "CAD to INR · 30 days")

    y = 140
    draw_card(draw, y, 220, "Today")
    draw.text((24, y + 52), "Latest", font=get_font(12), fill=(99, 115, 139))
    draw.text((24, y + 74), "66.78", font=get_font(34, bold=True), fill=(22, 93, 184))
    draw.text((24, y + 120), "Change +2.6%", font=get_font(13, bold=True), fill=(23, 130, 70))

    y += 250
    draw_card(draw, y, 440, "Trend")
    draw_line_chart(draw, 24, y + 50, W - 24, y + 350, [0.20, 0.28, 0.24, 0.40, 0.36, 0.52, 0.60], (21, 101, 192))
    draw.text((24, y + 368), "Min 64.90    Max 67.10", font=get_font(12), fill=(71, 85, 105))

    y += 470
    draw_card(draw, y, 620, "Recent Rates")
    row_y = y + 54
    dates = ["2026-06-23", "2026-06-22", "2026-06-21", "2026-06-20", "2026-06-19", "2026-06-18", "2026-06-17", "2026-06-16"]
    rates = [66.78, 66.54, 66.49, 66.20, 66.04, 65.88, 65.70, 65.62]
    for i, (d, r) in enumerate(zip(dates, rates)):
        bg = (247, 250, 255) if i % 2 == 0 else (255, 255, 255)
        rounded_rect(draw, (20, row_y, W - 20, row_y + 62), radius=10, fill=bg, outline=(228, 235, 245), width=1)
        draw.text((30, row_y + 20), d, font=get_font(11), fill=(71, 85, 105))
        draw.text((W - 88, row_y + 18), f"{r:.2f}", font=get_font(13, bold=True), fill=(18, 90, 176))
        row_y += 70

    y += 650
    draw_card(draw, y, 2500, "Insights")
    text_font = get_font(12)
    paragraph = (
        "CAD has shown stable movement against INR over the selected period. "
        "The trend line indicates moderate upward momentum with low volatility. "
        "Use the selector to compare with CHF, USD, EUR, and other currencies."
    )
    yy = y + 54
    for _ in range(22):
        draw.text((24, yy), paragraph, font=text_font, fill=(71, 85, 105))
        yy += 110

    img.save(OUT1, "PNG")


def draw_screenshot_two():
    img = Image.new("RGB", (W, H), (245, 250, 255))
    draw = ImageDraw.Draw(img)

    for y in range(120):
        t = y / 119
        r = int(18 + (8 - 18) * t)
        g = int(138 + (98 - 138) * t)
        b = int(173 + (150 - 173) * t)
        draw.line((0, y, W, y), fill=(r, g, b))

    draw_header(draw, "CTrend", "Compare pairs and period")

    y = 140
    draw_card(draw, y, 330, "Currency Pair")
    draw.text((24, y + 54), "Base", font=get_font(11), fill=(100, 116, 139))
    rounded_rect(draw, (24, y + 76, W - 24, y + 130), radius=12, fill=(243, 248, 255), outline=(210, 222, 238), width=2)
    draw.text((34, y + 94), "CAD", font=get_font(14, bold=True), fill=(29, 78, 216))
    draw.text((24, y + 152), "Target", font=get_font(11), fill=(100, 116, 139))
    rounded_rect(draw, (24, y + 174, W - 24, y + 228), radius=12, fill=(243, 248, 255), outline=(210, 222, 238), width=2)
    draw.text((34, y + 192), "INR", font=get_font(14, bold=True), fill=(29, 78, 216))
    rounded_rect(draw, (24, y + 252, W - 24, y + 304), radius=12, fill=(24, 97, 191))
    draw.text((W // 2 - 34, y + 269), "FETCH", font=get_font(13, bold=True), fill=(255, 255, 255))

    y += 360
    draw_card(draw, y, 460, "Performance")
    rounded_rect(draw, (24, y + 54, W - 24, y + 134), radius=14, fill=(239, 253, 245), outline=(181, 233, 205), width=2)
    draw.text((34, y + 78), "Trend: Positive", font=get_font(13, bold=True), fill=(21, 128, 61))
    rounded_rect(draw, (24, y + 152, W - 24, y + 232), radius=14, fill=(239, 246, 255), outline=(191, 219, 254), width=2)
    draw.text((34, y + 176), "Average: 66.12", font=get_font(13, bold=True), fill=(30, 64, 175))
    rounded_rect(draw, (24, y + 250, W - 24, y + 330), radius=14, fill=(255, 247, 237), outline=(254, 215, 170), width=2)
    draw.text((34, y + 274), "Volatility: Low", font=get_font(13, bold=True), fill=(194, 65, 12))

    y += 490
    draw_card(draw, y, 2550, "Multi-period Charts")
    chart_y = y + 50
    sets = [
        [0.14, 0.18, 0.16, 0.25, 0.21, 0.27, 0.30],
        [0.22, 0.24, 0.20, 0.29, 0.26, 0.32, 0.38],
        [0.17, 0.15, 0.19, 0.22, 0.28, 0.31, 0.34],
        [0.35, 0.33, 0.36, 0.30, 0.34, 0.40, 0.45],
        [0.25, 0.20, 0.22, 0.28, 0.30, 0.33, 0.37],
    ]
    colors = [(29, 78, 216), (22, 163, 74), (194, 65, 12), (147, 51, 234), (6, 182, 212)]
    for i, pts in enumerate(sets):
        draw.text((24, chart_y), f"Window {i+1}", font=get_font(12, bold=True), fill=(51, 65, 85))
        draw_line_chart(draw, 24, chart_y + 24, W - 24, chart_y + 220, pts, colors[i])
        chart_y += 240

    img.save(OUT2, "PNG")


def main() -> None:
    draw_screenshot_one()
    draw_screenshot_two()
    print(f"Saved {OUT1} ({W}x{H})")
    print(f"Saved {OUT2} ({W}x{H})")


if __name__ == "__main__":
    main()
