"""Generates the GlassType launcher icon (SVG sources and PNG renders).

All coordinates use a 512x512 canvas = the 108dp adaptive icon canvas.
Run: python make_icon.py <outdir> [--png] [--android <res dir>]
  --png      renders previews and legacy launcher PNGs (needs Microsoft Edge or Chrome)
  --android  writes the adaptive icon vector drawables into <res dir>/drawable-v26
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = sys.argv[1] if len(sys.argv) > 1 and not sys.argv[1].startswith("--") else os.path.join(HERE, "out")

# Glass pane and keys (foreground)
CARD = dict(x=116, y=151, w=280, h=210, r=40)
KEY_ROWS = [185, 237]
KEY_H = 36
KEY_W = 43
KEY_GAP = 16
KEY_X0 = 146
SPACE = dict(x=186, y=289, w=140, h=36)

BG_DEFS = """
  <linearGradient id="bg" x1="0" y1="0" x2="512" y2="512" gradientUnits="userSpaceOnUse">
    <stop offset="0" stop-color="#12C2E9"/>
    <stop offset="0.55" stop-color="#5B6CF9"/>
    <stop offset="1" stop-color="#B24BF3"/>
  </linearGradient>
  <radialGradient id="blobWarm" cx="372" cy="150" r="170" gradientUnits="userSpaceOnUse">
    <stop offset="0" stop-color="#FFC371" stop-opacity="0.95"/>
    <stop offset="1" stop-color="#FFC371" stop-opacity="0"/>
  </radialGradient>
  <radialGradient id="blobMint" cx="140" cy="380" r="180" gradientUnits="userSpaceOnUse">
    <stop offset="0" stop-color="#3DF5C6" stop-opacity="0.85"/>
    <stop offset="1" stop-color="#3DF5C6" stop-opacity="0"/>
  </radialGradient>
"""

FG_DEFS = """
  <linearGradient id="glass" x1="116" y1="151" x2="396" y2="361" gradientUnits="userSpaceOnUse">
    <stop offset="0" stop-color="#FFFFFF" stop-opacity="0.42"/>
    <stop offset="1" stop-color="#FFFFFF" stop-opacity="0.12"/>
  </linearGradient>
"""


def rrect(x, y, w, h, r):
    """Rounded rectangle as path data (VectorDrawable compatible)."""
    return (f"M{x + r},{y} h{w - 2 * r} a{r},{r} 0 0 1 {r},{r} v{h - 2 * r} "
            f"a{r},{r} 0 0 1 {-r},{r} h{-(w - 2 * r)} a{r},{r} 0 0 1 {-r},{-r} "
            f"v{-(h - 2 * r)} a{r},{r} 0 0 1 {r},{-r} z")


def card_path():
    c = CARD
    return rrect(c["x"], c["y"], c["w"], c["h"], c["r"])


def shadow_path():
    c = CARD
    return rrect(c["x"] + 4, c["y"] + 12, c["w"] - 8, c["h"], c["r"])


def glare_path():
    # diagonal light reflection, clipped to the card
    return "M116,151 L262,151 L170,361 L116,361 z"


def key_paths():
    paths = []
    for y in KEY_ROWS:
        for i in range(4):
            paths.append(rrect(KEY_X0 + i * (KEY_W + KEY_GAP), y, KEY_W, KEY_H, 10))
    s = SPACE
    paths.append(rrect(s["x"], s["y"], s["w"], s["h"], 10))
    return paths


FG_SCALE = 0.9
FG_GROUP = f'<g transform="translate(256 256) scale({FG_SCALE}) translate(-256 -256)">'


def background_svg_body():
    return ('<rect width="512" height="512" fill="url(#bg)"/>'
            '<rect width="512" height="512" fill="url(#blobWarm)"/>'
            '<rect width="512" height="512" fill="url(#blobMint)"/>')


def foreground_svg_body():
    keys = "".join(f'<path d="{p}" fill="#FFFFFF" fill-opacity="0.93"/>' for p in key_paths())
    return (FG_GROUP
            + f'<path d="{shadow_path()}" fill="#1B1464" fill-opacity="0.18"/>'
            f'<path d="{card_path()}" fill="url(#glass)"/>'
            f'<clipPath id="cardClip"><path d="{card_path()}"/></clipPath>'
            f'<path d="{glare_path()}" fill="#FFFFFF" fill-opacity="0.16" clip-path="url(#cardClip)"/>'
            f'<path d="{card_path()}" fill="none" stroke="#FFFFFF" stroke-opacity="0.75" stroke-width="5"/>'
            + keys + "</g>")


def monochrome_svg_body():
    keys = "".join(f'<path d="{p}" fill="#000000"/>' for p in key_paths())
    return FG_GROUP + f'<path d="{card_path()}" fill="none" stroke="#000000" stroke-width="12"/>' + keys + "</g>"


def svg(body, size=512, view="0 0 512 512", mask=None, defs=""):
    clip_open, clip_close = "", ""
    if mask:
        defs += f'<clipPath id="mask">{mask}</clipPath>'
        clip_open, clip_close = '<g clip-path="url(#mask)">', "</g>"
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="{view}">'
            f"<defs>{BG_DEFS}{FG_DEFS}{defs}</defs>{clip_open}{body}{clip_close}</svg>")


# The visible part of an adaptive icon is the central 72dp of 108dp: 85.33..426.67 in canvas units
VIS = 512 * 18 / 108
VIS_VIEW = f"{VIS:.2f} {VIS:.2f} {512 - 2 * VIS:.2f} {512 - 2 * VIS:.2f}"
VIS_SIZE = 512 - 2 * VIS
CX = 256
SQUIRCLE = (f'<rect x="{VIS:.2f}" y="{VIS:.2f}" width="{VIS_SIZE:.2f}" height="{VIS_SIZE:.2f}" '
            f'rx="{VIS_SIZE * 0.3:.2f}"/>')
LEGACY_SQUARE = (f'<rect x="{VIS + 6:.2f}" y="{VIS + 6:.2f}" width="{VIS_SIZE - 12:.2f}" '
                 f'height="{VIS_SIZE - 12:.2f}" rx="{VIS_SIZE * 0.2:.2f}"/>')
CIRCLE = f'<circle cx="{CX}" cy="{CX}" r="{VIS_SIZE / 2:.2f}"/>'
CIRCLE_LEGACY = f'<circle cx="{CX}" cy="{CX}" r="{VIS_SIZE / 2 - 6:.2f}"/>'

FULL = background_svg_body() + foreground_svg_body()

FILES = {
    "background.svg": svg(background_svg_body()),
    "foreground.svg": svg(foreground_svg_body()),
    "monochrome.svg": svg(monochrome_svg_body()),
    "preview_squircle.svg": svg(FULL, 512, VIS_VIEW, SQUIRCLE),
    "preview_circle.svg": svg(FULL, 512, VIS_VIEW, CIRCLE),
    "legacy_square.svg": svg(FULL, 512, VIS_VIEW, LEGACY_SQUARE),
    "legacy_round.svg": svg(FULL, 512, VIS_VIEW, CIRCLE_LEGACY),
    "store_icon.svg": svg(FULL, 512, VIS_VIEW, SQUIRCLE),
}

BROWSERS = [
    r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    r"C:\Program Files\Google\Chrome\Application\chrome.exe",
]


def render(svg_file, png_file, size):
    browser = next(b for b in BROWSERS if os.path.exists(b))
    html = svg_file[:-4] + f"_{size}.html"
    with open(svg_file, encoding="utf-8") as f:
        content = f.read().replace('width="512" height="512"', f'width="{size}" height="{size}"', 1)
    with open(html, "w", encoding="utf-8") as f:
        f.write('<html><body style="margin:0;background:transparent">' + content + "</body></html>")
    try:
        subprocess.run([browser, "--headless=new", "--disable-gpu", "--hide-scrollbars",
                        "--default-background-color=00000000", f"--window-size={size},{size}",
                        f"--screenshot={png_file}", "file:///" + html.replace("\\", "/")],
                       capture_output=True, timeout=30)
    except subprocess.TimeoutExpired:
        pass  # the browser sometimes keeps running after writing the screenshot
    os.remove(html)
    if not os.path.exists(png_file):
        raise RuntimeError(f"rendering {png_file} failed")


VD_HEAD = ('<?xml version="1.0" encoding="utf-8"?>\n'
           '<!-- generated by art/glasstype_icon/make_icon.py -->\n'
           '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
           '    xmlns:aapt="http://schemas.android.com/aapt"\n'
           '    android:width="108dp" android:height="108dp"\n'
           '    android:viewportWidth="512" android:viewportHeight="512">\n')
VD_GROUP = f'    <group android:pivotX="256" android:pivotY="256" android:scaleX="{FG_SCALE}" android:scaleY="{FG_SCALE}">\n'


def vd_gradient(attrs, stops):
    items = "".join(f'                <item android:offset="{o}" android:color="{c}"/>\n' for o, c in stops)
    return ('        <aapt:attr name="android:fillColor">\n'
            f'            <gradient {attrs}>\n{items}            </gradient>\n'
            '        </aapt:attr>\n')


def vd_background():
    full = "M0,0 h512 v512 h-512 z"
    return (VD_HEAD
            + f'    <path android:pathData="{full}">\n'
            + vd_gradient('android:type="linear" android:startX="0" android:startY="0" android:endX="512" android:endY="512"',
                          [("0", "#FF12C2E9"), ("0.55", "#FF5B6CF9"), ("1", "#FFB24BF3")])
            + '    </path>\n'
            + f'    <path android:pathData="{full}">\n'
            + vd_gradient('android:type="radial" android:centerX="372" android:centerY="150" android:gradientRadius="170"',
                          [("0", "#F2FFC371"), ("1", "#00FFC371")])
            + '    </path>\n'
            + f'    <path android:pathData="{full}">\n'
            + vd_gradient('android:type="radial" android:centerX="140" android:centerY="380" android:gradientRadius="180"',
                          [("0", "#D93DF5C6"), ("1", "#003DF5C6")])
            + '    </path>\n</vector>\n')


def vd_foreground():
    keys = "".join(f'        <path android:fillColor="#EDFFFFFF" android:pathData="{p}"/>\n' for p in key_paths())
    return (VD_HEAD + VD_GROUP
            + f'        <path android:fillColor="#2E1B1464" android:pathData="{shadow_path()}"/>\n'
            + f'        <path android:pathData="{card_path()}">\n'
            + vd_gradient('android:type="linear" android:startX="116" android:startY="151" android:endX="396" android:endY="361"',
                          [("0", "#6BFFFFFF"), ("1", "#1FFFFFFF")])
            + '        </path>\n'
            + '        <group>\n'
            + f'            <clip-path android:pathData="{card_path()}"/>\n'
            + f'            <path android:fillColor="#29FFFFFF" android:pathData="{glare_path()}"/>\n'
            + '        </group>\n'
            + f'        <path android:strokeColor="#BFFFFFFF" android:strokeWidth="5" android:pathData="{card_path()}"/>\n'
            + keys + '    </group>\n</vector>\n')


def vd_monochrome():
    keys = "".join(f'        <path android:fillColor="#FF000000" android:pathData="{p}"/>\n' for p in key_paths())
    return (VD_HEAD.replace('    xmlns:aapt="http://schemas.android.com/aapt"\n', "") + VD_GROUP
            + f'        <path android:strokeColor="#FF000000" android:strokeWidth="12" android:pathData="{card_path()}"/>\n'
            + keys + '    </group>\n</vector>\n')


def write_android(res):
    d = os.path.join(res, "drawable-v26")
    os.makedirs(d, exist_ok=True)
    for name, content in (("ic_glasstype_background.xml", vd_background()),
                          ("ic_glasstype_foreground.xml", vd_foreground()),
                          ("ic_glasstype_monochrome.xml", vd_monochrome())):
        with open(os.path.join(d, name), "w", encoding="utf-8", newline="\n") as f:
            f.write(content)


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    if "--android" in sys.argv:
        write_android(sys.argv[sys.argv.index("--android") + 1])
    for name, content in FILES.items():
        with open(os.path.join(OUT, name), "w", encoding="utf-8") as f:
            f.write(content)
    if "--png" in sys.argv:
        for name in ("preview_squircle", "preview_circle", "monochrome"):
            render(os.path.join(OUT, name + ".svg"), os.path.join(OUT, name + ".png"), 512)
        for dpi, size in (("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)):
            render(os.path.join(OUT, "legacy_square.svg"), os.path.join(OUT, f"ic_launcher_{dpi}.png"), size)
            render(os.path.join(OUT, "legacy_round.svg"), os.path.join(OUT, f"ic_launcher_round_{dpi}.png"), size)
        render(os.path.join(OUT, "store_icon.svg"), os.path.join(OUT, "store_icon.png"), 512)
    print("done:", OUT)
