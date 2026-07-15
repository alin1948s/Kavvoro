from __future__ import annotations

import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).resolve().parents[1]
RES_DIR = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
ART_DIR = ROOT / "art" / "ui"
SIZE = 256
HI = 4
CANVAS = SIZE * HI


CYAN = (29, 232, 200, 255)
PINK = (255, 77, 141, 255)
GOLD = (255, 207, 74, 255)
BLUE = (69, 242, 255, 255)
WHITE = (247, 244, 255, 255)
INK = (7, 9, 15, 255)
VOID = (9, 13, 23, 255)
PURPLE = (193, 92, 255, 255)


def c(color: tuple[int, int, int, int], alpha: int | None = None) -> tuple[int, int, int, int]:
    return color if alpha is None else (color[0], color[1], color[2], alpha)


def p(value: float) -> int:
    return round(value * HI)


def lerp(a: int, b: int, t: float) -> int:
    return round(a + (b - a) * t)


def gradient(size: int, left: tuple[int, int, int, int], right: tuple[int, int, int, int]) -> Image.Image:
    strip = Image.new("RGBA", (size, 1), (0, 0, 0, 0))
    px = strip.load()
    for x in range(size):
        t = x / max(1, size - 1)
        px[x, 0] = tuple(lerp(left[i], right[i], t) for i in range(4))
    return strip.resize((size, size), Image.Resampling.BICUBIC)


def radial(size: int, inner: tuple[int, int, int, int], outer: tuple[int, int, int, int]) -> Image.Image:
    small = max(96, size // 8)
    image = Image.new("RGBA", (small, small), (0, 0, 0, 0))
    px = image.load()
    cx = cy = small / 2
    max_dist = math.hypot(cx, cy)
    for y in range(small):
        for x in range(small):
            t = min(1.0, math.hypot(x - cx, y - cy) / max_dist)
            ease = t * t * (3 - 2 * t)
            px[x, y] = tuple(lerp(inner[i], outer[i], ease) for i in range(4))
    return image.resize((size, size), Image.Resampling.BICUBIC)


def draw_soft_glow(base: Image.Image, mask: Image.Image, color: tuple[int, int, int, int], blur: float) -> None:
    glow = Image.new("RGBA", base.size, c(color, 0))
    glow_draw = ImageDraw.Draw(glow)
    alpha = mask.filter(ImageFilter.GaussianBlur(p(blur)))
    glow_draw.bitmap((0, 0), alpha, fill=color)
    base.alpha_composite(glow)


def alpha_mask(points: list[tuple[float, float]], size: int = CANVAS) -> Image.Image:
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.polygon([(p(x), p(y)) for x, y in points], fill=255)
    return mask


def new_icon() -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    return image, ImageDraw.Draw(image)


def draw_rift_backplate(image: Image.Image, accent_a: tuple[int, int, int, int], accent_b: tuple[int, int, int, int]) -> None:
    draw = ImageDraw.Draw(image)
    plate = Image.new("L", image.size, 0)
    plate_draw = ImageDraw.Draw(plate)
    plate_draw.rounded_rectangle([p(28), p(28), p(228), p(228)], radius=p(42), fill=235)
    draw_soft_glow(image, plate, c(accent_a, 70), 10)

    fill = radial(CANVAS, (24, 31, 45, 238), (5, 8, 15, 225))
    fill.putalpha(plate)
    image.alpha_composite(fill)

    accent = gradient(CANVAS, c(accent_a, 60), c(accent_b, 34))
    accent.putalpha(plate)
    image.alpha_composite(accent)

    draw.rounded_rectangle([p(28), p(28), p(228), p(228)], radius=p(42), outline=c(WHITE, 95), width=p(4))
    draw.rounded_rectangle([p(43), p(42), p(213), p(100)], radius=p(27), fill=(255, 255, 255, 18))
    draw.rounded_rectangle([p(38), p(38), p(50), p(218)], radius=p(6), fill=c(accent_a, 116))


def paste_masked_gradient(image: Image.Image, mask: Image.Image, left: tuple[int, int, int, int], right: tuple[int, int, int, int]) -> None:
    fill = gradient(CANVAS, left, right)
    fill.putalpha(mask)
    image.alpha_composite(fill)


def draw_glyph_shadow(image: Image.Image, mask: Image.Image, color: tuple[int, int, int, int]) -> None:
    draw_soft_glow(image, mask, c(color, 92), 5)
    shadow = Image.new("RGBA", image.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    shadow_draw.bitmap((p(4), p(5)), mask.filter(ImageFilter.GaussianBlur(p(1.0))), fill=(0, 0, 0, 145))
    image.alpha_composite(shadow)


def home_icon() -> Image.Image:
    image, draw = new_icon()
    draw_rift_backplate(image, CYAN, PINK)

    house = [
        (55, 132), (128, 62), (201, 132), (181, 132), (181, 190),
        (148, 190), (148, 151), (108, 151), (108, 190), (75, 190), (75, 132)
    ]
    mask = alpha_mask(house)
    draw_glyph_shadow(image, mask, CYAN)
    paste_masked_gradient(image, mask, c(CYAN, 255), c(PINK, 255))
    draw.line([(p(x), p(y)) for x, y in house + [house[0]]], fill=c(WHITE, 245), width=p(8), joint="curve")
    draw.rounded_rectangle([p(116), p(156), p(140), p(190)], radius=p(6), fill=(6, 9, 16, 225))
    return image


def retry_icon() -> Image.Image:
    image, draw = new_icon()
    draw_rift_backplate(image, PINK, BLUE)

    arc_mask = Image.new("L", image.size, 0)
    arc_draw = ImageDraw.Draw(arc_mask)
    arc_draw.arc([p(56), p(56), p(200), p(200)], start=38, end=330, fill=255, width=p(31))
    arrow = [(180, 53), (212, 111), (150, 105)]
    arc_draw.polygon([(p(x), p(y)) for x, y in arrow], fill=255)
    draw_glyph_shadow(image, arc_mask, PINK)
    paste_masked_gradient(image, arc_mask, c(PINK, 255), c(BLUE, 255))
    draw.arc([p(56), p(56), p(200), p(200)], start=38, end=330, fill=c(WHITE, 230), width=p(7))
    draw.line([(p(x), p(y)) for x, y in arrow + [arrow[0]]], fill=c(WHITE, 230), width=p(5), joint="curve")
    return image


def share_icon() -> Image.Image:
    image, draw = new_icon()
    draw_rift_backplate(image, CYAN, PURPLE)

    points = [(75, 132), (176, 78), (176, 178)]
    line_mask = Image.new("L", image.size, 0)
    line_draw = ImageDraw.Draw(line_mask)
    line_draw.line([(p(points[0][0]), p(points[0][1])), (p(points[1][0]), p(points[1][1]))], fill=255, width=p(25))
    line_draw.line([(p(points[0][0]), p(points[0][1])), (p(points[2][0]), p(points[2][1]))], fill=255, width=p(25))
    draw_glyph_shadow(image, line_mask, CYAN)
    paste_masked_gradient(image, line_mask, c(CYAN, 255), c(PINK, 255))
    draw.line([(p(points[0][0]), p(points[0][1])), (p(points[1][0]), p(points[1][1]))], fill=c(WHITE, 205), width=p(6))
    draw.line([(p(points[0][0]), p(points[0][1])), (p(points[2][0]), p(points[2][1]))], fill=c(WHITE, 205), width=p(6))
    for idx, (x, y) in enumerate(points):
        color = [GOLD, PINK, CYAN][idx]
        node_mask = Image.new("L", image.size, 0)
        node_draw = ImageDraw.Draw(node_mask)
        node_draw.ellipse([p(x - 28), p(y - 28), p(x + 28), p(y + 28)], fill=255)
        draw_soft_glow(image, node_mask, c(color, 105), 6)
        draw.ellipse([p(x - 27), p(y - 27), p(x + 27), p(y + 27)], fill=c(color, 250), outline=c(WHITE, 225), width=p(6))
    return image


def next_icon() -> Image.Image:
    image, draw = new_icon()
    draw_rift_backplate(image, GOLD, PINK)

    arrow = [(66, 58), (66, 198), (178, 128)]
    tail = [(177, 63), (205, 63), (205, 193), (177, 193)]
    mask = alpha_mask(arrow)
    draw = ImageDraw.Draw(mask)
    draw.polygon([(p(x), p(y)) for x, y in tail], fill=255)
    draw_glyph_shadow(image, mask, GOLD)
    paste_masked_gradient(image, mask, c(GOLD, 255), c(PINK, 255))
    outline = ImageDraw.Draw(image)
    outline.line([(p(x), p(y)) for x, y in arrow + [arrow[0]]], fill=c(WHITE, 235), width=p(7), joint="curve")
    outline.rounded_rectangle([p(177), p(63), p(205), p(193)], radius=p(8), outline=c(WHITE, 230), width=p(7))
    return image


def back_icon() -> Image.Image:
    image, draw = new_icon()
    draw_rift_backplate(image, BLUE, PINK)

    arrow = [(190, 58), (190, 198), (78, 128)]
    tail = [(51, 63), (79, 63), (79, 193), (51, 193)]
    mask = alpha_mask(arrow)
    tail_draw = ImageDraw.Draw(mask)
    tail_draw.polygon([(p(x), p(y)) for x, y in tail], fill=255)
    draw_glyph_shadow(image, mask, BLUE)
    paste_masked_gradient(image, mask, c(BLUE, 255), c(PINK, 255))
    draw.line([(p(x), p(y)) for x, y in arrow + [arrow[0]]], fill=c(WHITE, 235), width=p(7), joint="curve")
    draw.rounded_rectangle([p(51), p(63), p(79), p(193)], radius=p(8), outline=c(WHITE, 230), width=p(7))
    return image


def restore_icon() -> Image.Image:
    image, draw = new_icon()
    draw_rift_backplate(image, CYAN, GOLD)

    mask = Image.new("L", image.size, 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.arc([p(55), p(56), p(201), p(202)], start=205, end=548, fill=255, width=p(30))
    mask_draw.polygon([(p(73), p(78)), (p(122), p(77)), (p(97), p(122))], fill=255)
    mask_draw.polygon([(p(184), p(178)), (p(134), p(178)), (p(160), p(133))], fill=255)
    draw_glyph_shadow(image, mask, CYAN)
    paste_masked_gradient(image, mask, c(CYAN, 255), c(GOLD, 255))
    draw.arc([p(55), p(56), p(201), p(202)], start=205, end=548, fill=c(WHITE, 225), width=p(7))
    draw.line([(p(101), p(130)), (p(123), p(153)), (p(160), p(105))], fill=c(WHITE, 240), width=p(12), joint="curve")
    return image


def sound_icon() -> Image.Image:
    image, draw = new_icon()
    draw_rift_backplate(image, BLUE, CYAN)

    mask = Image.new("L", image.size, 0)
    mask_draw = ImageDraw.Draw(mask)
    speaker = [(54, 108), (88, 108), (130, 76), (130, 180), (88, 148), (54, 148)]
    mask_draw.polygon([(p(x), p(y)) for x, y in speaker], fill=255)
    mask_draw.arc([p(112), p(78), p(184), p(178)], start=-42, end=42, fill=255, width=p(23))
    mask_draw.arc([p(130), p(54), p(222), p(202)], start=-43, end=43, fill=255, width=p(18))
    draw_glyph_shadow(image, mask, BLUE)
    paste_masked_gradient(image, mask, c(BLUE, 255), c(CYAN, 255))

    draw.line([(p(x), p(y)) for x, y in speaker + [speaker[0]]], fill=c(WHITE, 225), width=p(6), joint="curve")
    draw.arc([p(112), p(78), p(184), p(178)], start=-42, end=42, fill=c(WHITE, 210), width=p(6))
    draw.arc([p(130), p(54), p(222), p(202)], start=-43, end=43, fill=c(WHITE, 205), width=p(5))
    return image


def music_icon() -> Image.Image:
    image, draw = new_icon()
    draw_rift_backplate(image, GOLD, PINK)

    mask = Image.new("L", image.size, 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle([p(144), p(54), p(168), p(157)], radius=p(11), fill=255)
    mask_draw.polygon([(p(88), p(74)), (p(168), p(54)), (p(168), p(84)), (p(88), p(104))], fill=255)
    mask_draw.rounded_rectangle([p(78), p(94), p(102), p(180)], radius=p(11), fill=255)
    mask_draw.ellipse([p(52), p(156), p(116), p(210)], fill=255)
    mask_draw.ellipse([p(120), p(139), p(184), p(193)], fill=255)
    draw_glyph_shadow(image, mask, GOLD)
    paste_masked_gradient(image, mask, c(GOLD, 255), c(PINK, 255))

    draw.rounded_rectangle([p(144), p(54), p(168), p(157)], radius=p(11), outline=c(WHITE, 220), width=p(5))
    draw.line([(p(88), p(88)), (p(168), p(68))], fill=c(WHITE, 225), width=p(7))
    draw.rounded_rectangle([p(78), p(94), p(102), p(180)], radius=p(11), outline=c(WHITE, 220), width=p(5))
    draw.ellipse([p(52), p(156), p(116), p(210)], outline=c(WHITE, 220), width=p(5))
    draw.ellipse([p(120), p(139), p(184), p(193)], outline=c(WHITE, 220), width=p(5))
    return image


def downsample(image: Image.Image) -> Image.Image:
    return image.resize((SIZE, SIZE), Image.Resampling.LANCZOS)


def save_icon(name: str, image: Image.Image) -> Image.Image:
    output = downsample(image)
    output.save(RES_DIR / f"ui_icon_{name}.png")
    return output


def make_contact_sheet(icons: dict[str, Image.Image]) -> None:
    cell = 196
    label_h = 34
    columns = 4
    rows = math.ceil(len(icons) / columns)
    sheet = Image.new("RGBA", (cell * columns, (cell + label_h) * rows), (6, 8, 14, 255))
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.load_default()
    for index, (name, icon) in enumerate(icons.items()):
        x = (index % columns) * cell
        y = (index // columns) * (cell + label_h)
        draw.rounded_rectangle([x + 14, y + 14, x + cell - 14, y + cell - 14], radius=18, fill=(18, 25, 39, 255), outline=(255, 255, 255, 42), width=2)
        sheet.alpha_composite(icon.resize((142, 142), Image.Resampling.LANCZOS), (x + 27, y + 27))
        draw.text((x + 18, y + cell + 5), name.upper(), fill=(247, 244, 255, 220), font=font)
    ART_DIR.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(ART_DIR / "ui-icons-contact-sheet.png", quality=95)


def main() -> None:
    RES_DIR.mkdir(parents=True, exist_ok=True)
    ART_DIR.mkdir(parents=True, exist_ok=True)
    icons = {
        "home": save_icon("home", home_icon()),
        "retry": save_icon("retry", retry_icon()),
        "share": save_icon("share", share_icon()),
        "next": save_icon("next", next_icon()),
        "back": save_icon("back", back_icon()),
        "restore": save_icon("restore", restore_icon()),
        "sound": save_icon("sound", sound_icon()),
        "music": save_icon("music", music_icon()),
    }
    make_contact_sheet(icons)
    print(f"Generated {len(icons)} UI icons in {RES_DIR}")
    print(f"Preview sheet: {ART_DIR / 'ui-icons-contact-sheet.png'}")


if __name__ == "__main__":
    main()
