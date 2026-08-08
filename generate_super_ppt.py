import sys
import subprocess
import os
import urllib.request

try:
    from pptx import Presentation
    from pptx.util import Inches, Pt
    from pptx.dml.color import RGBColor
    from pptx.enum.text import PP_ALIGN
    from pptx.enum.shapes import MSO_SHAPE
except ImportError:
    print("python-pptx not found. Installing...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "python-pptx"])
    from pptx import Presentation
    from pptx.util import Inches, Pt
    from pptx.dml.color import RGBColor
    from pptx.enum.text import PP_ALIGN
    from pptx.enum.shapes import MSO_SHAPE

# --- 1. Image Downloader ---
cache_dir = os.path.join(os.path.dirname(__file__), "assets")
os.makedirs(cache_dir, exist_ok=True)

image_library = {
    "intro": "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?q=80&w=800&auto=format&fit=crop", # Mobile app screen
    "architecture": "https://images.unsplash.com/photo-1551288049-bebda4e38f71?q=80&w=800&auto=format&fit=crop", # Data & Code
    "claims": "https://images.unsplash.com/photo-1517457373958-b7bdd4587205?q=80&w=800&auto=format&fit=crop", # People interacting / trust
    "future": "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800&auto=format&fit=crop" # Globe / Next Level tech
}

def get_image(name):
    url = image_library.get(name)
    if not url: return None
    path = os.path.join(cache_dir, f"{name}.jpg")
    if not os.path.exists(path):
        print(f"Downloading image '{name}'...")
        try:
            req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
            with urllib.request.urlopen(req) as resp, open(path, 'wb') as out:
                out.write(resp.read())
        except Exception as e:
            print(f"Failed {name}: {e}")
            return None
    return path

# --- 2. Presentation Setup & Theme ---
prs = Presentation()

# Standard layout (10 x 7.5 inches)
prs.slide_width = Inches(10)
prs.slide_height = Inches(7.5)

# Dark Mode Theme Colors
BG_COLOR = RGBColor(22, 24, 35)       # Very dark blue-gray
ACCENT_COLOR = RGBColor(0, 212, 255)  # Neon bright blue (Cyberpunk-esque accent)
TEXT_LIGHT = RGBColor(240, 245, 255)
TEXT_MUTED = RGBColor(170, 180, 200)

def apply_background(slide):
    # PPTX workaround to set solid background on blank slides robustly
    bg = slide.background
    fill = bg.fill
    fill.solid()
    fill.fore_color.rgb = BG_COLOR
    
    # Add neon accent line at the very top (a sleek design touch)
    line = slide.shapes.add_shape(
        MSO_SHAPE.RECTANGLE, 
        Inches(0), Inches(0), Inches(10), Inches(0.15)
    )
    line.fill.solid()
    line.fill.fore_color.rgb = ACCENT_COLOR
    line.line.color.rgb = ACCENT_COLOR

# --- 3. Custom Slide Generators ---
def create_title_slide(title_text, subtitle_text):
    slide = prs.slides.add_slide(prs.slide_layouts[6]) # blank
    apply_background(slide)
    
    # Title Box
    tb_title = slide.shapes.add_textbox(Inches(1), Inches(2.5), Inches(8), Inches(1.5))
    tf = tb_title.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = title_text
    p.font.size = Pt(54)
    p.font.bold = True
    p.font.color.rgb = TEXT_LIGHT
    p.alignment = PP_ALIGN.CENTER
    
    # Subtitle Box
    tb_sub = slide.shapes.add_textbox(Inches(1), Inches(4.2), Inches(8), Inches(1))
    tf_sub = tb_sub.text_frame
    tf_sub.word_wrap = True
    p_sub = tf_sub.paragraphs[0]
    p_sub.text = subtitle_text
    p_sub.font.size = Pt(24)
    p_sub.font.color.rgb = TEXT_MUTED
    p_sub.alignment = PP_ALIGN.CENTER

def add_content_slide(title_text, bullet_points, image_name=None):
    slide = prs.slides.add_slide(prs.slide_layouts[6])
    apply_background(slide)
    
    # Slide Title
    tb_title = slide.shapes.add_textbox(Inches(0.5), Inches(0.5), Inches(9), Inches(1))
    tf_title = tb_title.text_frame
    p_title = tf_title.paragraphs[0]
    p_title.text = title_text
    p_title.font.size = Pt(36)
    p_title.font.bold = True
    p_title.font.color.rgb = TEXT_LIGHT
    
    # Layout logic: if there is an image, split screen
    content_width = Inches(8.5)
    if image_name:
        content_width = Inches(5.0)
        img_path = get_image(image_name)
        if img_path:
            # Add image to right side
            # We constrain height or width to fit the 4x5 inch slot on the right
            try:
                # Add picture centered on right side
                pic = slide.shapes.add_picture(img_path, Inches(5.5), Inches(2.0), width=Inches(4.0))
            except Exception as e:
                print("Could not place image:", e)

    # Add Content Body (Bullets)
    tb_body = slide.shapes.add_textbox(Inches(0.5), Inches(1.8), content_width, Inches(5))
    tf_body = tb_body.text_frame
    tf_body.word_wrap = True
    
    # We iterate over bullet points
    if bullet_points:
        p0 = tf_body.paragraphs[0]
        p0.text = bullet_points[0]
        p0.font.size = Pt(22)
        p0.font.color.rgb = TEXT_MUTED
        # Add visual "dot" spacing instead of raw PPT bullets for a cleaner look
        
        for pt in bullet_points[1:]:
            p = tf_body.add_paragraph()
            p.text = pt
            p.font.size = Pt(22)
            p.font.color.rgb = TEXT_MUTED
            p.space_before = Pt(14)
            p.level = 0

# --- 4. Build Presentation Data ---
print("Building Super Presentation...")

create_title_slide(
    "FindIt", 
    "The Next-Generation Lost and Found Platform"
)

add_content_slide(
    "Introduction & Overview",
    [
        "Target: Revolutionize how people recover lost items using a modern digital interface.",
        "Problem: Currently, finding a lost item relies on luck, disorganized physical bins, or scattered social media posts.",
        "Solution: A centralized, elegant Android Application that intelligently bridges the gap between those who lost items and those who found them.",
        "Core Stack: Native Android powered by Firebase realtime architecture and Cloudinary."
    ],
    "intro"
)

add_content_slide(
    "System Architecture",
    [
        "Frontend: Crafted with Java/Kotlin focusing on fluid UX and Material Design principles.",
        "Backend/DB: Firebase Firestore integration for low-latency, real-time data syncing across all users.",
        "Media Layer: Cloudinary handles high-res image uploads securely without bogging down the database.",
        "Operations: Fully asynchronous architecture preventing UI locks and providing instantly responsive feedback."
    ],
    "architecture"
)

add_content_slide(
    "Part 1: User & Item Hub",
    [
        "Authentication: Frictionless and secure user registration and login.",
        "Comprehensive Profiling: Keep user data and contact paths cleanly organized.",
        "Rich Item Reporting: When reporting an item, users add high-res photos, location tags, and detailed descriptors.",
        "Status Tracking: Users can follow their reported item through a beautiful, timeline-style tracking UI."
    ]
)

add_content_slide(
    "Part 2: Discovery & Search",
    [
        "Global Feed: A unified, infinitely scrolling feed of all active items in the ecosystem.",
        "Item Deep-Dive: Tapping an item yields a full-screen, detailed card with zoomable photos.",
        "Smart Search: Instantly filter items by keywords (e.g., 'Keys', 'Backpack').",
        "Categorization: Rapidly drill down by specific status (Lost/Found) or semantic category."
    ]
)

add_content_slide(
    "Part 3: Claims & Notification",
    [
        "Direct Claiming: Found your item? Initiate a direct claim seamlessly from the item's view.",
        "Ownership Proof: Securely upload private verification images that only admins can see.",
        "Real-time Alerts: Instant notifications when a claim state changes (Pending -> Verified).",
        "Secure Channels: The platform mediates communication, ensuring privacy and trust for both parties."
    ],
    "claims"
)

add_content_slide(
    "Part 4: Admin Authority",
    [
        "Command Center: A dedicated dashboard giving administrators a 10,000-foot view of the platform.",
        "Verification Hub: Admins can side-by-side compare the original listing with the claimant's proof.",
        "One-Tap Closure: Securely finalize a case by marking the item 'RECOVERED', hiding it from the public feed.",
        "Analytics: Exportable insights on app performance and success rates."
    ]
)

add_content_slide(
    "Future Roadmap",
    [
        "Push Notifications: Implement FCM for instant matching alerts and pings.",
        "Geolocation: Integrate Google Maps API to let users browse items on an interactive map.",
        "Encrypted Chat: A secure in-app messaging feature unlocking once an admin approves a match."
    ],
    "future"
)

create_title_slide(
    "Q&A",
    "Thank You. Questions are welcomed."
)

output_file = os.path.join(os.path.dirname(os.path.abspath(__file__)), "FindIt_Super_Presentation.pptx")
prs.save(output_file)
print(f"\nSUCCESS! Super Presentation generated at:\n{output_file}")
