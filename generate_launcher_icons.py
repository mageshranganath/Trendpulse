from PIL import Image, ImageDraw
from pathlib import Path

ROOT = Path(__file__).resolve().parent
RES = ROOT / "app" / "src" / "main" / "res"

SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

SOURCE_ICON_NAME = "TrendPulse - Top.png"


def _fit_on_canvas(source: Image.Image, size: int, margin_ratio: float = 0.10) -> Image.Image:
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    max_w = int(size * (1.0 - 2.0 * margin_ratio))
    max_h = int(size * (1.0 - 2.0 * margin_ratio))
    src = source.copy()
    src.thumbnail((max_w, max_h), Image.Resampling.LANCZOS)

    x = (size - src.width) // 2
    y = (size - src.height) // 2
    canvas.alpha_composite(src, (x, y))
    return canvas


def make_icon(source: Image.Image, size: int, round_icon: bool = False) -> Image.Image:
    base = _fit_on_canvas(source, size=size, margin_ratio=0.10)

    mask = Image.new("L", (size, size), 0)
    md = ImageDraw.Draw(mask)
    if round_icon:
        md.ellipse((0, 0, size - 1, size - 1), fill=255)
    else:
        radius = max(2, int(size * 0.22))
        md.rounded_rectangle((0, 0, size - 1, size - 1), radius=radius, fill=255)

    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(base, (0, 0), mask)
    return out


def make_play_store_icon(source: Image.Image, size: int = 512) -> Image.Image:
    # Play Store listing icon must be square 512x512 and should avoid transparency.
    icon = _fit_on_canvas(source, size=size, margin_ratio=0.07)
    square = Image.new("RGBA", (size, size), (255, 255, 255, 255))
    square.alpha_composite(icon)
    return square.convert("RGB")


def main() -> None:
    source_path = ROOT / SOURCE_ICON_NAME
    if not source_path.exists():
        raise FileNotFoundError(f"Source icon not found: {source_path}")

    source = Image.open(source_path).convert("RGBA")

    for folder, size in SIZES.items():
        target = RES / folder
        target.mkdir(parents=True, exist_ok=True)

        normal_icon = make_icon(source, size=size, round_icon=False)
        normal_icon.save(target / "ic_launcher.png", format="PNG")

        round_icon = make_icon(source, size=size, round_icon=True)
        round_icon.save(target / "ic_launcher_round.png", format="PNG")

    play_icon = make_play_store_icon(source, 512)
    play_icon.save(ROOT / "playstore-icon-512.png", format="PNG")

    print(f"Launcher icons generated from: {source_path.name}")


if __name__ == "__main__":
    main()
