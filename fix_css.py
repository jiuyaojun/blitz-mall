#!/usr/bin/env python3
"""Add responsive CSS to fix layout issues"""

filepath = '/home/ubuntu/blitz/blitz-server/src/main/resources/static/css/common.css'

with open(filepath, 'r') as f:
    content = f.read()

# Add responsive styles before the existing mobile section
responsive_css = '''
/* ============================================================
   Responsive Layout Fixes (2026-06-20)
   ============================================================ */

/* Hide sidebar on screens < 900px, stack layout vertically */
@media (max-width: 900px) {
  .main-content .flex.gap-16 {
    flex-direction: column !important;
    gap: 0 !important;
  }
  .category-sidebar {
    display: none !important;
  }
  .banner-carousel {
    height: 200px !important;
  }
  .banner-slide h2 { font-size: 1.2rem !important; }
  .banner-slide p { font-size: 0.85rem !important; }
  .scene-row {
    grid-template-columns: repeat(2, 1fr) !important;
    gap: 10px !important;
  }
  .section-header h2 { font-size: 1rem !important; }
}

/* On medium screens, reduce sidebar width */
@media (min-width: 901px) and (max-width: 1100px) {
  .category-sidebar {
    width: 160px !important;
    min-width: 160px !important;
    font-size: 0.82rem !important;
  }
}

/* Fix product card image display */
.product-card .card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
}

/* Make card-img have proper aspect ratio */
.product-card .card-img {
  position: relative;
  overflow: hidden;
  aspect-ratio: 1 / 1;
  background: var(--bg);
}

/* Scene row grid - ensure responsive */
.scene-row, .product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

/* Banner carousel responsive */
.banner-carousel {
  position: relative;
  height: 280px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 24px;
}

.banner-slide {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.6s;
  display: flex;
  align-items: center;
  padding: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.banner-slide.active { opacity: 1; }

.banner-text h2 { font-size: 1.8rem; margin-bottom: 8px; }
.banner-text p { font-size: 1rem; opacity: 0.9; margin-bottom: 16px; }

.btn-white {
  display: inline-block;
  padding: 10px 24px;
  background: #fff;
  color: #667eea;
  border-radius: 24px;
  font-weight: 600;
  text-decoration: none;
}

.banner-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 2;
}

.dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  background: rgba(255,255,255,0.4);
  cursor: pointer;
}

.dot.active { background: #fff; }
'''

# Find where to insert - before the existing mobile section or at end
insert_marker = '/* ============================================================\n   Mobile Bottom TabBar'
# If not found, append at end
if insert_marker in content:
    content = content.replace(insert_marker, responsive_css + '\n' + insert_marker)
    print('CSS inserted before mobile tabbar section')
else:
    # Append before closing
    content += '\n' + responsive_css
    print('CSS appended at end')

with open(filepath, 'w') as f:
    f.write(content)
print('Done')
