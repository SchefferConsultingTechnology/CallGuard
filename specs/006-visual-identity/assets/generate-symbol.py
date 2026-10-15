"""Generates the CallGuard symbol (outer hexagonal C + inner G) as SVG, plus previews.

Usage: venv/bin/python symbol.py <out_dir> <board_png>
"""
import io
import math
import sys

import cairosvg
from PIL import Image, ImageDraw

OUT, BOARD = sys.argv[1], sys.argv[2]

C = 54.0            # adaptive icon canvas 108, centre 54
W = 9.0             # ribbon width (~15% of the symbol, as on the board)
NAVY, BLUE, SKY, CYAN = "#0B1F3B", "#2563EB", "#38BDF8", "#22D3EE"

# Centre lines measured on the board (crop 780x810), scaled so the symbol is 62 units tall.
SCALE = 62 / 750
OUT_C, OUT_R = (385, 424), 329     # outer C
IN_C, IN_R = (468, 425), 196       # inner G, shifted right: its right side meets the C's
MID = (385, 415)                   # symbol centre on the board
BAR_DROP = 5.0                     # bar below the G centre, leaving the opening above it


def to_icon(p):
    return (C + (p[0] - MID[0]) * SCALE, C + (p[1] - MID[1]) * SCALE)


def ring(centre, r):
    names = ["T", "UR", "LR", "B", "LL", "UL"]
    pts = {}
    for k, name in enumerate(names):
        a = math.radians(-90 + 60 * k)
        pts[name] = to_icon((centre[0] + r * math.cos(a), centre[1] + r * math.sin(a)))
    return pts


def lerp(p, q, t):
    return (p[0] + (q[0] - p[0]) * t, p[1] + (q[1] - p[1]) * t)


def at_x(p, q, x):
    """Point on line p→q (extended) with the given x."""
    t = (x - p[0]) / (q[0] - p[0])
    return lerp(p, q, t)


o = ring(OUT_C, OUT_R)
right_x = o["LR"][0]

# Inner G shares the C's right side (one band, as on the board): place it from the right edge.
r_in = IN_R * SCALE
centre_in = (right_x - r_in * math.cos(math.radians(30)), to_icon(IN_C)[1])
i = {}
for k, name in enumerate(["T", "UR", "LR", "B", "LL", "UL"]):
    ang = math.radians(-90 + 60 * k)
    i[name] = (centre_in[0] + r_in * math.cos(ang), centre_in[1] + r_in * math.sin(ang))

bar_y = centre_in[1] + BAR_DROP

# Outer C: cut on the upper-right edge (the G's top arm fills the gap), down the left, up the right edge to the bar.
outer = [lerp(o["T"], o["UR"], 0.4), o["T"], o["UL"], o["LL"], o["B"], o["LR"], (right_x, bar_y)]
# Inner G: top arm runs out to the right edge, then around; the opening sits between the arm and the bar.
# The bar continues the same stroke, so the corner where it meets the right side is rounded.
inner = [at_x(i["T"], i["UR"], right_x), i["T"], i["UL"], i["LL"], i["B"], i["LR"], (right_x, bar_y),
         (centre_in[0] - 1.0, bar_y)]


def d(points):
    return "M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in points)


def svg(background=None, mono=None, size=108):
    bg = f'<rect width="108" height="108" fill="{background}"/>' if background else ""
    paths = "".join(
        f'<path d="{d(p)}" fill="none" stroke="{mono or grad}" stroke-width="{W}" '
        f'stroke-linejoin="round" stroke-linecap="butt"/>'
        for p, grad in ((outer, "url(#go)"), (inner, "url(#gi)"))
    )
    return f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="{size}" height="{size}">
<defs><linearGradient id="go" gradientUnits="userSpaceOnUse" x1="30" y1="86" x2="80" y2="22">
<stop offset="0" stop-color="{BLUE}"/><stop offset="1" stop-color="{SKY}"/></linearGradient>
<linearGradient id="gi" gradientUnits="userSpaceOnUse" x1="40" y1="76" x2="80" y2="36">
<stop offset="0" stop-color="{SKY}"/><stop offset="1" stop-color="{CYAN}"/></linearGradient></defs>
{bg}{paths}</svg>'''


def render(markup, px):
    return Image.open(io.BytesIO(cairosvg.svg2png(bytestring=markup.encode(), output_width=px, output_height=px))).convert("RGBA")


open(f"{OUT}/callguard-symbol.svg", "w").write(svg())
open(f"{OUT}/callguard-symbol-mono.svg", "w").write(svg(mono="#000000"))
print("outer", d(outer)); print("inner", d(inner))

# Masks: circle, rounded square (squircle-ish), teardrop — visible area of a 108dp adaptive icon is 72dp.
icon = render(svg(background=NAVY), 432)


def masked(kind):
    m = Image.new("L", (432, 432), 0)
    dr = ImageDraw.Draw(m)
    inset = 432 * 18 / 108   # 72dp visible window
    box = (inset, inset, 432 - inset, 432 - inset)
    if kind == "circle":
        dr.ellipse(box, fill=255)
    elif kind == "squircle":
        dr.rounded_rectangle(box, radius=70, fill=255)
    else:  # teardrop: rounded except bottom-right corner
        dr.ellipse(box, fill=255)
        dr.rounded_rectangle((216, 216, box[2], box[3]), radius=30, fill=255)
    out = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
    out.paste(icon, (0, 0), m)
    return out.crop((int(inset), int(inset), int(432 - inset), int(432 - inset)))


board = Image.open(BOARD).convert("RGBA").crop((100, 80, 360, 350)).resize((288, 299))
sheet = Image.new("RGBA", (288 * 5 + 60, 340), (245, 247, 251, 255))
sheet.paste(board, (10, 20))
for n, kind in enumerate(["circle", "squircle", "teardrop"]):
    sheet.paste(masked(kind), (10 + (n + 1) * 298, 26))
symbol_on_light = render(svg(), 288)
light = Image.new("RGBA", (288, 288), (245, 247, 251, 255)); light.alpha_composite(symbol_on_light)
sheet.paste(light, (10 + 4 * 298, 26))
sheet.save(f"{OUT}/symbol-preview.png")
render(svg(background=NAVY), 512).convert("RGB").save(f"{OUT}/play-icon-512.png")
render(svg(mono="#000000"), 288).save(f"{OUT}/mono-preview.png")


# ---- Android vector drawables (same geometry) ----
def vd_path(points, stroke):
    return (f'    <path\n        android:pathData="{d(points)}"\n        android:strokeWidth="{W}"\n'
            f'        android:strokeLineJoin="round"\n        android:strokeLineCap="butt"\n'
            f'        android:fillColor="#00000000"{stroke}')


def gradient_stroke(x1, y1, x2, y2, c1, c2):
    return (f'>\n        <aapt:attr name="android:strokeColor">\n            <gradient\n'
            f'                android:type="linear"\n                android:startX="{x1}"\n                android:startY="{y1}"\n'
            f'                android:endX="{x2}"\n                android:endY="{y2}"\n'
            f'                android:startColor="#FF{c1[1:]}"\n                android:endColor="#FF{c2[1:]}" />\n'
            f'        </aapt:attr>\n    </path>')


def vector(width_dp, viewport, body, translate=(0, 0), note=''):
    tx, ty = translate
    group_open = f'    <group android:translateX="{tx:.2f}" android:translateY="{ty:.2f}">\n' if (tx or ty) else ''
    group_close = '    </group>\n' if (tx or ty) else ''
    inner = body if not group_open else '\n'.join('    ' + l if l else l for l in body.split('\n'))
    return (f'<?xml version="1.0" encoding="utf-8"?>\n<!-- Generated by specs/006-visual-identity/assets/generate-symbol.py{note} -->\n'
            f'<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            f'    xmlns:aapt="http://schemas.android.com/aapt"\n'
            f'    android:width="{width_dp}dp"\n    android:height="{width_dp}dp"\n'
            f'    android:viewportWidth="{viewport}"\n    android:viewportHeight="{viewport}">\n'
            f'{group_open}{inner}\n{group_close}</vector>\n')


colour_body = (vd_path(outer, gradient_stroke(30, 86, 80, 22, BLUE, SKY)) + '\n' +
               vd_path(inner, gradient_stroke(40, 76, 80, 36, SKY, CYAN)))
mono_body = (vd_path(outer, '\n        android:strokeColor="#FF000000" />') + '\n' +
             vd_path(inner, '\n        android:strokeColor="#FF000000" />'))

open(f"{OUT}/ic_launcher_foreground.xml", "w").write(vector(108, 108, colour_body, note=' (adaptive icon, 66dp safe zone)'))
open(f"{OUT}/ic_launcher_monochrome.xml", "w").write(vector(108, 108, mono_body, note=' (themed icons)'))

# Symbol without the adaptive-icon margin, for use inside the app.
pts = outer + inner
xs = [p[0] for p in pts]; ys = [p[1] for p in pts]
half = W / 2
x0, x1 = min(xs) - half, max(xs) + half
y0, y1 = min(ys) - half, max(ys) + half
side = max(x1 - x0, y1 - y0)
tx = -x0 + (side - (x1 - x0)) / 2
ty = -y0 + (side - (y1 - y0)) / 2
open(f"{OUT}/ic_brand_symbol.xml", "w").write(
    vector(48, round(side, 2), colour_body, translate=(tx, ty), note=' (symbol only, no margin)'))
print('vectors ok; symbol box', round(side, 2))
