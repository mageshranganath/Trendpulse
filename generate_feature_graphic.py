from PIL import Image, ImageDraw, ImageFont, ImageFilter

W, H = 1024, 500
OUT = "feature-graphic-1024x500.png"


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


def main() -> None:
    img = Image.new("RGBA", (W, H), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)

    # Background gradient.
    for y in range(H):
        t = y / max(1, H - 1)
        r = int(16 + (9 - 16) * t)
        g = int(148 + (88 - 148) * t)
        b = int(196 + (165 - 196) * t)
        draw.line([(0, y), (W, y)], fill=(r, g, b, 255))

    # Soft glow blobs for depth.
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse((-180, -80, 340, 420), fill=(255, 255, 255, 38))
    gd.ellipse((760, 80, 1160, 500), fill=(255, 255, 255, 28))
    gd.ellipse((450, -120, 920, 260), fill=(255, 255, 255, 18))
    glow = glow.filter(ImageFilter.GaussianBlur(12))
    img = Image.alpha_composite(img, glow)
    draw = ImageDraw.Draw(img)

    # Background motif dollar signs.
    dollar_font = get_font(180, bold=True)
    draw.text((720, 40), "$", font=dollar_font, fill=(255, 255, 255, 70))
    draw.text((820, 210), "$", font=dollar_font, fill=(255, 255, 255, 55))

    # Main title and subtitle.
    title_font = get_font(84, bold=True)
    subtitle_font = get_font(34, bold=False)

    title = "CTrend"
    subtitle = "Currency Trends at a Glance"

    try:
        tb = draw.textbbox((0, 0), title, font=title_font)
        tw = tb[2] - tb[0]
        th = tb[3] - tb[1]
    except Exception:
        tw, th = draw.textsize(title, font=title_font)

    x = 70
    y = 150

    draw.text((x + 2, y + 2), title, font=title_font, fill=(0, 0, 0, 110))
    draw.text((x, y), title, font=title_font, fill=(255, 255, 255, 255))

    draw.text((x + 2, y + th + 18 + 2), subtitle, font=subtitle_font, fill=(0, 0, 0, 90))
    draw.text((x, y + th + 18), subtitle, font=subtitle_font, fill=(235, 249, 255, 245))

    # Decorative trend line.
    points = [(80, 390), (220, 360), (350, 375), (500, 330), (650, 345), (800, 295), (940, 255)]
    draw.line(points, fill=(255, 255, 255, 220), width=7, joint="curve")
    for px, py in points:
        draw.ellipse((px - 8, py - 8, px + 8, py + 8), fill=(255, 255, 255, 235))

    # Export RGB PNG.
    img.convert("RGB").save(OUT, format="PNG")
    print(f"Saved {OUT} ({W}x{H})")


if __name__ == "__main__":
    main()
