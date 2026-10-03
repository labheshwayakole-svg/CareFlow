import os
from PIL import Image, ImageDraw

PROJECT_ROOT = r"c:\Users\sarf0\.antigravity-ide\CareFlow"
RES_DIR = os.path.join(PROJECT_ROOT, "app", "src", "main", "res")
SOURCE_ICON_PATH = os.path.join(PROJECT_ROOT, "app_icon_previews", "icon_3_smart_turn_badge.png")

source_img = Image.open(SOURCE_ICON_PATH).convert("RGBA")
W, H = source_img.size

# 1. Generate circular version for round launcher
def make_round_icon(img):
    w, h = img.size
    mask = Image.new("L", (w, h), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, w, h), fill=255)
    round_img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    round_img.paste(img, (0, 0), mask)
    return round_img

round_source = make_round_icon(source_img)

# 2. Generate Adaptive Foreground (Badge centered in 108x108 safe zone, ~68% scale)
def make_adaptive_foreground(img, target_size):
    badge_size = int(target_size * 0.70)
    resized_badge = img.resize((badge_size, badge_size), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (target_size, target_size), (0, 0, 0, 0))
    offset = (target_size - badge_size) // 2
    canvas.paste(resized_badge, (offset, offset), resized_badge)
    return canvas

# Densities config
DENSITIES = {
    "mipmap-mdpi": (48, 108),
    "mipmap-hdpi": (72, 162),
    "mipmap-xhdpi": (96, 216),
    "mipmap-xxhdpi": (144, 324),
    "mipmap-xxxhdpi": (192, 432),
}

for folder, (launcher_sz, fg_sz) in DENSITIES.items():
    folder_path = os.path.join(RES_DIR, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # Standard launcher
    sq = source_img.resize((launcher_sz, launcher_sz), Image.Resampling.LANCZOS)
    sq.save(os.path.join(folder_path, "ic_launcher.png"), "PNG")
    sq.save(os.path.join(folder_path, "ic_launcher.webp"), "WEBP")
    
    # Round launcher
    rd = round_source.resize((launcher_sz, launcher_sz), Image.Resampling.LANCZOS)
    rd.save(os.path.join(folder_path, "ic_launcher_round.png"), "PNG")
    rd.save(os.path.join(folder_path, "ic_launcher_round.webp"), "WEBP")
    
    # Adaptive foreground
    fg = make_adaptive_foreground(source_img, fg_sz)
    fg.save(os.path.join(folder_path, "ic_launcher_foreground.png"), "PNG")
    fg.save(os.path.join(folder_path, "ic_launcher_foreground.webp"), "WEBP")
    print(f"Generated icons for {folder}: {launcher_sz}x{launcher_sz}")

# In-app main logo drawables
drawable_dir = os.path.join(RES_DIR, "drawable")
os.makedirs(drawable_dir, exist_ok=True)

logo_512 = source_img.resize((512, 512), Image.Resampling.LANCZOS)
logo_512.save(os.path.join(drawable_dir, "ic_app_logo.png"), "PNG")

logo_round_512 = round_source.resize((512, 512), Image.Resampling.LANCZOS)
logo_round_512.save(os.path.join(drawable_dir, "ic_app_logo_round.png"), "PNG")

# Also copy foreground to drawable as fallback
fg_drawable = make_adaptive_foreground(source_img, 432)
fg_drawable.save(os.path.join(drawable_dir, "ic_launcher_foreground.png"), "PNG")

print("Generated ic_app_logo.png (512x512) in res/drawable")
