import os
import math
import shutil
from PIL import Image, ImageDraw, ImageFilter

OUTPUT_DIR = r"c:\Users\sarf0\.antigravity-ide\CareFlow\app_icon_previews"
ARTIFACT_DIR = r"C:\Users\sarf0\.gemini\antigravity-ide\brain\340e2c58-d6e1-46f0-8400-8faf14e4470d"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Warm Terracotta Coral Palette
CORAL_DEEP = (195, 80, 55)       # #C35037
CORAL_PRIMARY = (224, 109, 83)   # #E06D53
CORAL_BRIGHT = (244, 138, 114)   # #F48A72
CORAL_PEACH = (252, 162, 140)    # #FCA28C
APRICOT_TINT = (253, 232, 219)   # #FDE8DB
WARM_CREAM = (250, 247, 242)     # #FAF7F2
SAGE_TEAL = (74, 155, 143)       # #4A9B8F
PURE_WHITE = (255, 255, 255)
ESPRESSO = (38, 28, 22)          # #261C16

def create_gradient(size, color1, color2, direction="diag"):
    gw, gh = 128, 128
    base = Image.new("RGBA", (gw, gh))
    c1 = color1 if len(color1) == 4 else color1 + (255,)
    c2 = color2 if len(color2) == 4 else color2 + (255,)
    pix = base.load()
    for y in range(gh):
        for x in range(gw):
            if direction == "diag":
                t = (x + y) / (gw + gh)
            elif direction == "vertical":
                t = y / gh
            elif direction == "horizontal":
                t = x / gw
            elif direction == "radial":
                cx, cy = gw / 2, gh / 2
                dist = math.sqrt((x - cx)**2 + (y - cy)**2)
                t = min(1.0, dist / (gw * 0.7))
            
            r = int(c1[0] * (1 - t) + c2[0] * t)
            g = int(c1[1] * (1 - t) + c2[1] * t)
            b = int(c1[2] * (1 - t) + c2[2] * t)
            a = int(c1[3] * (1 - t) + c2[3] * t)
            pix[x, y] = (r, g, b, a)
    return base.resize(size, Image.Resampling.BILINEAR)

def draw_squircle_mask(size, radius):
    w, h = size
    mask = Image.new("L", size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle((0, 0, w, h), radius=radius, fill=255)
    return mask

# -------------------------------------------------------------
# ICON 1: Flowing Pulse Cross
# -------------------------------------------------------------
def render_icon_1(scale=2):
    s = 1024 * scale
    pad = int(48 * scale)
    card_size = s - 2 * pad
    
    # 1. Base Gradient Squircle
    bg = create_gradient((card_size, card_size), CORAL_BRIGHT, CORAL_DEEP, "diag")
    squircle_mask = draw_squircle_mask((card_size, card_size), int(220 * scale))
    
    # Composite card on transparent canvas
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    
    # Drop shadow
    shadow_mask = Image.new("L", (s, s), 0)
    sdraw = ImageDraw.Draw(shadow_mask)
    sdraw.rounded_rectangle(
        (pad + int(10*scale), pad + int(24*scale), pad + card_size - int(10*scale), pad + card_size + int(24*scale)),
        radius=int(220*scale),
        fill=100
    )
    shadow_layer = Image.new("RGBA", (s, s), (90, 30, 18, 255))
    shadow_layer.putalpha(shadow_mask)
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(int(36 * scale)))
    img = Image.alpha_composite(img, shadow_layer)
    
    # Card surface
    card_surface = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    card_surface.paste(bg, (pad, pad), squircle_mask)
    img = Image.alpha_composite(img, card_surface)
    
    # 2. Modern Cross + ECG Pulse
    symbol_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(symbol_layer)
    cx, cy = s // 2, s // 2
    
    # Vertical cross bar
    vw = int(100 * scale)
    vh = int(440 * scale)
    draw.rounded_rectangle((cx - vw//2, cy - vh//2, cx + vw//2, cy + vh//2), radius=vw//2, fill=(255, 255, 255, 255))
    
    # Left arm
    draw.rounded_rectangle((cx - vh//2, cy - vw//2, cx - vw//2 + int(10*scale), cy + vw//2), radius=vw//2, fill=(255, 255, 255, 255))
    
    # Right arm morphs into pulse wave
    pts = [
        (cx - vw//2, cy),
        (cx + int(45*scale), cy),
        (cx + int(90*scale), cy - int(140*scale)),
        (cx + int(145*scale), cy + int(155*scale)),
        (cx + int(195*scale), cy - int(75*scale)),
        (cx + int(235*scale), cy),
        (cx + int(285*scale), cy)
    ]
    draw.line(pts, fill=(255, 255, 255, 255), width=int(96*scale), joint="curve")
    draw.ellipse((pts[-1][0] - int(48*scale), pts[-1][1] - int(48*scale), pts[-1][0] + int(48*scale), pts[-1][1] + int(48*scale)), fill=(255, 255, 255, 255))
    
    # Symbol subtle drop shadow
    sym_mask = symbol_layer.split()[3]
    sym_shadow = Image.new("RGBA", (s, s), (100, 30, 15, 255))
    sym_shadow.putalpha(sym_mask)
    sym_shadow = sym_shadow.filter(ImageFilter.GaussianBlur(int(16 * scale)))
    # Shift shadow down
    shifted_shadow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    shifted_shadow.paste(sym_shadow, (0, int(10 * scale)))
    
    img = Image.alpha_composite(img, shifted_shadow)
    img = Image.alpha_composite(img, symbol_layer)
    
    # Border highlight
    border_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    bdraw = ImageDraw.Draw(border_layer)
    bdraw.rounded_rectangle(
        (pad + int(3*scale), pad + int(3*scale), pad + card_size - int(3*scale), pad + card_size - int(3*scale)),
        radius=int(217*scale),
        outline=(255, 255, 255, 75),
        width=int(6*scale)
    )
    img = Image.alpha_composite(img, border_layer)
    
    return img.resize((1024, 1024), Image.Resampling.LANCZOS)

# -------------------------------------------------------------
# ICON 2: Care Heart Rhythm (Empathetic Interlocking)
# -------------------------------------------------------------
def render_icon_2(scale=2):
    s = 1024 * scale
    pad = int(48 * scale)
    card_size = s - 2 * pad
    
    # Warm Cream & Apricot Tint base
    bg = create_gradient((card_size, card_size), WARM_CREAM, APRICOT_TINT, "vertical")
    squircle_mask = draw_squircle_mask((card_size, card_size), int(220 * scale))
    
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    
    # Shadow
    shadow_mask = Image.new("L", (s, s), 0)
    sdraw = ImageDraw.Draw(shadow_mask)
    sdraw.rounded_rectangle(
        (pad + int(10*scale), pad + int(24*scale), pad + card_size - int(10*scale), pad + card_size + int(24*scale)),
        radius=int(220*scale),
        fill=85
    )
    shadow_layer = Image.new("RGBA", (s, s), (110, 70, 50, 255))
    shadow_layer.putalpha(shadow_mask)
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(int(36 * scale)))
    img = Image.alpha_composite(img, shadow_layer)
    
    card_surface = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    card_surface.paste(bg, (pad, pad), squircle_mask)
    img = Image.alpha_composite(img, card_surface)
    
    # Symbol: Dual-tone Heart + Medical Cross
    cx, cy = s // 2, s // 2 - int(20*scale)
    symbol_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(symbol_layer)
    
    hr = int(175 * scale)
    # Left lobe (Terracotta Coral)
    draw.ellipse((cx - int(215*scale), cy - int(155*scale), cx - int(215*scale) + 2*hr, cy - int(155*scale) + 2*hr), fill=CORAL_PRIMARY + (255,))
    # Right lobe (Sunset Coral Peach)
    draw.ellipse((cx + int(215*scale) - 2*hr, cy - int(155*scale), cx + int(215*scale), cy - int(155*scale) + 2*hr), fill=CORAL_BRIGHT + (255,))
    # Bottom triangle
    draw.polygon([
        (cx - int(200*scale), cy + int(45*scale)),
        (cx + int(200*scale), cy + int(45*scale)),
        (cx, cy + int(285*scale))
    ], fill=CORAL_PRIMARY + (255,))
    
    # Pure white medical cross in center
    cw = int(72 * scale)
    ch = int(220 * scale)
    draw.rounded_rectangle((cx - cw//2, cy - ch//2 + int(10*scale), cx + cw//2, cy + ch//2 + int(10*scale)), radius=cw//2, fill=(255, 255, 255, 255))
    draw.rounded_rectangle((cx - ch//2, cy - cw//2 + int(10*scale), cx + ch//2, cy + cw//2 + int(10*scale)), radius=cw//2, fill=(255, 255, 255, 255))
    
    # Sage teal center jewel
    draw.ellipse((cx - int(24*scale), cy - int(24*scale) + int(10*scale), cx + int(24*scale), cy + int(24*scale) + int(10*scale)), fill=SAGE_TEAL + (255,))
    
    # Symbol shadow
    sym_mask = symbol_layer.split()[3]
    sym_shadow = Image.new("RGBA", (s, s), (140, 60, 40, 255))
    sym_shadow.putalpha(sym_mask)
    sym_shadow = sym_shadow.filter(ImageFilter.GaussianBlur(int(20 * scale)))
    shifted_shadow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    shifted_shadow.paste(sym_shadow, (0, int(12 * scale)))
    
    img = Image.alpha_composite(img, shifted_shadow)
    img = Image.alpha_composite(img, symbol_layer)
    
    # Border
    border_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    bdraw = ImageDraw.Draw(border_layer)
    bdraw.rounded_rectangle(
        (pad + int(3*scale), pad + int(3*scale), pad + card_size - int(3*scale), pad + card_size - int(3*scale)),
        radius=int(217*scale),
        outline=CORAL_PRIMARY + (70,),
        width=int(6*scale)
    )
    img = Image.alpha_composite(img, border_layer)
    
    return img.resize((1024, 1024), Image.Resampling.LANCZOS)

# -------------------------------------------------------------
# ICON 3: Smart Turn Badge (OPD Queue Chamber)
# -------------------------------------------------------------
def render_icon_3(scale=2):
    s = 1024 * scale
    pad = int(48 * scale)
    card_size = s - 2 * pad
    
    bg = create_gradient((card_size, card_size), CORAL_DEEP, (160, 55, 38), "vertical")
    squircle_mask = draw_squircle_mask((card_size, card_size), int(220 * scale))
    
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    
    # Shadow
    shadow_mask = Image.new("L", (s, s), 0)
    sdraw = ImageDraw.Draw(shadow_mask)
    sdraw.rounded_rectangle(
        (pad + int(10*scale), pad + int(24*scale), pad + card_size - int(10*scale), pad + card_size + int(24*scale)),
        radius=int(220*scale),
        fill=105
    )
    shadow_layer = Image.new("RGBA", (s, s), (70, 20, 10, 255))
    shadow_layer.putalpha(shadow_mask)
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(int(36 * scale)))
    img = Image.alpha_composite(img, shadow_layer)
    
    card_surface = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    card_surface.paste(bg, (pad, pad), squircle_mask)
    img = Image.alpha_composite(img, card_surface)
    
    # Outer queue ring
    cx, cy = s // 2, s // 2
    sym = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(sym)
    
    ring_r = int(270 * scale)
    draw.ellipse((cx - ring_r, cy - ring_r, cx + ring_r, cy + ring_r), outline=CORAL_PEACH + (200,), width=int(14*scale))
    
    # 3 Orbiting dots
    dot_radius = int(22 * scale)
    angles = [-30, 90, 210]
    for idx, ang in enumerate(angles):
        rad = math.radians(ang)
        dx = cx + int(ring_r * math.cos(rad))
        dy = cy + int(ring_r * math.sin(rad))
        dot_color = (255, 255, 255) if idx == 0 else SAGE_TEAL if idx == 1 else CORAL_PEACH
        draw.ellipse((dx - dot_radius, dy - dot_radius, dx + dot_radius, dy + dot_radius), fill=dot_color + (255,))
    
    # Inner circular disc with warm gradient
    inner_r = int(220 * scale)
    inner_disc = create_gradient((inner_r * 2, inner_r * 2), CORAL_PRIMARY, CORAL_BRIGHT, "diag")
    disc_mask = Image.new("L", (inner_r * 2, inner_r * 2), 0)
    ddraw = ImageDraw.Draw(disc_mask)
    ddraw.ellipse((0, 0, inner_r * 2, inner_r * 2), fill=255)
    
    # Center Medical Cross
    cw = int(76 * scale)
    ch = int(240 * scale)
    cross_img = Image.new("RGBA", (inner_r * 2, inner_r * 2), (0, 0, 0, 0))
    cdraw = ImageDraw.Draw(cross_img)
    cdraw.rounded_rectangle((inner_r - cw//2, inner_r - ch//2, inner_r + cw//2, inner_r + ch//2), radius=cw//2, fill=(255, 255, 255, 255))
    cdraw.rounded_rectangle((inner_r - ch//2, inner_r - cw//2, inner_r + ch//2, inner_r + cw//2), radius=cw//2, fill=(255, 255, 255, 255))
    
    inner_disc = Image.alpha_composite(inner_disc, cross_img)
    
    disc_surface = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    disc_surface.paste(inner_disc, (cx - inner_r, cy - inner_r), disc_mask)
    
    img = Image.alpha_composite(img, disc_surface)
    img = Image.alpha_composite(img, sym)
    
    return img.resize((1024, 1024), Image.Resampling.LANCZOS)

# -------------------------------------------------------------
# ICON 4: Monogram "CF" Flowing Ribbon
# -------------------------------------------------------------
def render_icon_4(scale=2):
    s = 1024 * scale
    pad = int(48 * scale)
    card_size = s - 2 * pad
    
    # Rich Terracotta base with warm diagonal glow
    bg = create_gradient((card_size, card_size), CORAL_PRIMARY, CORAL_DEEP, "diag")
    squircle_mask = draw_squircle_mask((card_size, card_size), int(220 * scale))
    
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    
    # Shadow
    shadow_mask = Image.new("L", (s, s), 0)
    sdraw = ImageDraw.Draw(shadow_mask)
    sdraw.rounded_rectangle(
        (pad + int(10*scale), pad + int(24*scale), pad + card_size - int(10*scale), pad + card_size + int(24*scale)),
        radius=int(220*scale),
        fill=100
    )
    shadow_layer = Image.new("RGBA", (s, s), (90, 30, 18, 255))
    shadow_layer.putalpha(shadow_mask)
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(int(36 * scale)))
    img = Image.alpha_composite(img, shadow_layer)
    
    card_surface = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    card_surface.paste(bg, (pad, pad), squircle_mask)
    img = Image.alpha_composite(img, card_surface)
    
    # Symbol: Monogram 'C' and 'F' flowing seamlessly
    cx, cy = s // 2, s // 2
    symbol_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(symbol_layer)
    
    c_radius = int(210 * scale)
    thick = int(84 * scale)
    
    draw.arc(
        (cx - c_radius, cy - c_radius, cx + c_radius, cy + c_radius),
        start=50, end=310,
        fill=(255, 255, 255, 255),
        width=thick
    )
    # Round caps for C
    rad1 = math.radians(50)
    rad2 = math.radians(310)
    x1, y1 = cx + c_radius * math.cos(rad1), cy + c_radius * math.sin(rad1)
    x2, y2 = cx + c_radius * math.cos(rad2), cy + c_radius * math.sin(rad2)
    draw.ellipse((x1 - thick//2, y1 - thick//2, x1 + thick//2, y1 + thick//2), fill=(255, 255, 255, 255))
    draw.ellipse((x2 - thick//2, y2 - thick//2, x2 + thick//2, y2 + thick//2), fill=(255, 255, 255, 255))
    
    # Letter F crossbar extending rightward
    draw.rounded_rectangle(
        (cx - int(110*scale), cy - thick//2, cx + int(190*scale), cy + thick//2),
        radius=thick//2,
        fill=(255, 255, 255, 255)
    )
    
    # Upper F crossbar
    draw.rounded_rectangle(
        (cx - int(30*scale), cy - int(140*scale), cx + int(160*scale), cy - int(140*scale) + thick),
        radius=thick//2,
        fill=(255, 255, 255, 255)
    )
    
    # Floating healing sage pulse dot
    draw.ellipse(
        (cx + int(210*scale) - int(32*scale), cy - thick//2, cx + int(210*scale) + int(32*scale), cy + thick//2),
        fill=SAGE_TEAL + (255,)
    )
    
    # Symbol shadow
    sym_mask = symbol_layer.split()[3]
    sym_shadow = Image.new("RGBA", (s, s), (100, 30, 15, 255))
    sym_shadow.putalpha(sym_mask)
    sym_shadow = sym_shadow.filter(ImageFilter.GaussianBlur(int(18 * scale)))
    shifted_shadow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    shifted_shadow.paste(sym_shadow, (0, int(10 * scale)))
    
    img = Image.alpha_composite(img, shifted_shadow)
    img = Image.alpha_composite(img, symbol_layer)
    
    # Border highlight
    border_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    bdraw = ImageDraw.Draw(border_layer)
    bdraw.rounded_rectangle(
        (pad + int(3*scale), pad + int(3*scale), pad + card_size - int(3*scale), pad + card_size - int(3*scale)),
        radius=int(217*scale),
        outline=(255, 255, 255, 75),
        width=int(6*scale)
    )
    img = Image.alpha_composite(img, border_layer)
    
    return img.resize((1024, 1024), Image.Resampling.LANCZOS)

# -------------------------------------------------------------
# ICON 5: Guardian Shield & Healing Beacon
# -------------------------------------------------------------
def render_icon_5(scale=2):
    s = 1024 * scale
    pad = int(48 * scale)
    card_size = s - 2 * pad
    
    bg = create_gradient((card_size, card_size), CORAL_PRIMARY, CORAL_PEACH, "diag")
    squircle_mask = draw_squircle_mask((card_size, card_size), int(220 * scale))
    
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    
    # Shadow
    shadow_mask = Image.new("L", (s, s), 0)
    sdraw = ImageDraw.Draw(shadow_mask)
    sdraw.rounded_rectangle(
        (pad + int(10*scale), pad + int(24*scale), pad + card_size - int(10*scale), pad + card_size + int(24*scale)),
        radius=int(220*scale),
        fill=100
    )
    shadow_layer = Image.new("RGBA", (s, s), (80, 25, 15, 255))
    shadow_layer.putalpha(shadow_mask)
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(int(36 * scale)))
    img = Image.alpha_composite(img, shadow_layer)
    
    card_surface = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    card_surface.paste(bg, (pad, pad), squircle_mask)
    img = Image.alpha_composite(img, card_surface)
    
    # Symbol: Curved shield containing hospital cross
    cx, cy = s // 2, s // 2 - int(10*scale)
    symbol_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(symbol_layer)
    
    sw = int(320 * scale)
    sh = int(380 * scale)
    shield_pts = [
        (cx - sw, cy - sh//2),
        (cx + sw, cy - sh//2),
        (cx + sw, cy + int(40*scale)),
        (cx, cy + sh//2 + int(80*scale)),
        (cx - sw, cy + int(40*scale))
    ]
    draw.polygon(shield_pts, fill=(255, 255, 255, 45))
    draw.line(shield_pts + [shield_pts[0]], fill=(255, 255, 255, 180), width=int(12*scale), joint="curve")
    
    # Pure White Hospital Cross inside shield
    cw = int(84 * scale)
    ch = int(280 * scale)
    draw.rounded_rectangle((cx - cw//2, cy - ch//2, cx + cw//2, cy + ch//2), radius=cw//2, fill=(255, 255, 255, 255))
    draw.rounded_rectangle((cx - ch//2, cy - cw//2, cx + ch//2, cy + cw//2), radius=cw//2, fill=(255, 255, 255, 255))
    
    # Central jewel: Small Sage Teal round emblem in cross center
    jewel_r = int(26 * scale)
    draw.ellipse((cx - jewel_r, cy - jewel_r, cx + jewel_r, cy + jewel_r), fill=SAGE_TEAL + (255,))
    
    # Shadow
    sym_mask = symbol_layer.split()[3]
    sym_shadow = Image.new("RGBA", (s, s), (110, 30, 15, 255))
    sym_shadow.putalpha(sym_mask)
    sym_shadow = sym_shadow.filter(ImageFilter.GaussianBlur(int(20 * scale)))
    shifted_shadow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    shifted_shadow.paste(sym_shadow, (0, int(10 * scale)))
    
    img = Image.alpha_composite(img, shifted_shadow)
    img = Image.alpha_composite(img, symbol_layer)
    
    return img.resize((1024, 1024), Image.Resampling.LANCZOS)

# Render all 5 icons
print("Generating Icon 1: Flowing Pulse Cross...")
icon1 = render_icon_1()
icon1.save(os.path.join(OUTPUT_DIR, "icon_1_flowing_pulse_cross.png"))
shutil.copy(os.path.join(OUTPUT_DIR, "icon_1_flowing_pulse_cross.png"), os.path.join(ARTIFACT_DIR, "icon_1_flowing_pulse_cross.png"))

print("Generating Icon 2: Care Heart Rhythm...")
icon2 = render_icon_2()
icon2.save(os.path.join(OUTPUT_DIR, "icon_2_care_heart_rhythm.png"))
shutil.copy(os.path.join(OUTPUT_DIR, "icon_2_care_heart_rhythm.png"), os.path.join(ARTIFACT_DIR, "icon_2_care_heart_rhythm.png"))

print("Generating Icon 3: Smart Turn Badge...")
icon3 = render_icon_3()
icon3.save(os.path.join(OUTPUT_DIR, "icon_3_smart_turn_badge.png"))
shutil.copy(os.path.join(OUTPUT_DIR, "icon_3_smart_turn_badge.png"), os.path.join(ARTIFACT_DIR, "icon_3_smart_turn_badge.png"))

print("Generating Icon 4: Monogram CF Flow...")
icon4 = render_icon_4()
icon4.save(os.path.join(OUTPUT_DIR, "icon_4_monogram_cf_flow.png"))
shutil.copy(os.path.join(OUTPUT_DIR, "icon_4_monogram_cf_flow.png"), os.path.join(ARTIFACT_DIR, "icon_4_monogram_cf_flow.png"))

print("Generating Icon 5: Guardian Shield & Beacon...")
icon5 = render_icon_5()
icon5.save(os.path.join(OUTPUT_DIR, "icon_5_guardian_shield_beacon.png"))
shutil.copy(os.path.join(OUTPUT_DIR, "icon_5_guardian_shield_beacon.png"), os.path.join(ARTIFACT_DIR, "icon_5_guardian_shield_beacon.png"))

print("All 5 icons generated successfully at 1024x1024!")
