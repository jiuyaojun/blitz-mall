#!/usr/bin/env python3
"""Fix seckill.html: image tags, clickable cards, auth check, more products"""

filepath = '/home/ubuntu/blitz/blitz-server/src/main/resources/static/seckill.html'

with open(filepath, 'r') as f:
    content = f.read()

# Fix 1: Image rendering in renderGrid
old1 = "var imgHTML = p.image ? '<img src=\"' + p.image + '\" alt=\"' + p.name + '\">' : '<span class=\"grid-card-icon\">📦</span>';"
# This is correct already - check
if "imgHTML = p.image" in content:
    print("Fix 1: grid images already use <img> tags")
else:
    # Fix the grid image rendering
    old_img = "var imgHTML = p.image"
    idx = content.find(old_img)
    if idx > 0:
        print(f"  Found at position {idx}")
        print(f"  {content[idx:idx+100]}")

# Fix 2: Make grid cards clickable - add onclick to navigate to detail page
# Cards currently use: card.addEventListener('click', ...)
# We need to change them to proper <a> links or update the click handler
# The current click handler selects the product as featured, rather than navigating
# Fix: Change click to navigate to detail page
old_click = "card.addEventListener('click', function() {\n      var productId = Number(card.getAttribute('data-id'));\n      document.getElementById('seckillHero').scrollIntoView({ behavior: 'smooth' });\n      var p = seckillProducts.find(function(sp) { return sp.id === productId; });\n      if (p) { renderFeatured(p); hideResult(); }\n    });"
new_click = "card.addEventListener('click', function() {\n      var productId = Number(card.getAttribute('data-id'));\n      // Navigate to detail page with seckill flag\n      location.href = '/detail.html?id=' + productId + '&seckill=1';\n    });"
if old_click in content:
    content = content.replace(old_click, new_click)
    print("Fix 2: grid cards now link to detail page")
else:
    print("Fix 2: pattern not found")

# Fix 3: Featured product - clicking the seckill button should check auth
old_exec = "async function executeSeckill() {\n  if (isSeckilling) return;\n  if (!featuredProduct) { showToast('没有可秒杀的商品', 'warning'); return; }\n\n  if (!requireAuth()) return;"
new_exec = "async function executeSeckill() {\n  if (isSeckilling) return;\n  if (!featuredProduct) { showToast('没有可秒杀的商品', 'warning'); return; }\n\n  if (!AUTH.isLoggedIn()) { location.href = '/login.html?redirect=/seckill.html'; return; }"
if old_exec in content:
    content = content.replace(old_exec, new_exec)
    print("Fix 3: auth check added to seckill button")
else:
    print("Fix 3: pattern not found, searching...")
    idx = content.find("async function executeSeckill")
    if idx > 0:
        print(f"  Found at position {idx}")
        print(f"  {content[idx:idx+200]}")

# Fix 4: Also make featured product image use img tag
old_feat_img = "$sfImg.innerHTML = product.image ? '<img src=\"' + product.image + '\" alt=\"' + product.name + '\">' : '⚡';"
# This might already be correct, let the renderFeatured function handle it

# Fix 5: Load more products to fill the grid if seckill count < 4
old_load = "seckillProducts = products.slice(0, count).map(function(p, idx) {"
new_load = """    // If fewer than 4 real seckill products, fetch regular products as filler
    if (products.length < 4) {
      try {
        var fillRes = await api.getProducts({ size: 8 });
        var regularProducts = (fillRes.data && fillRes.data.records) ? fillRes.data.records : [];
        // Add regular products not already in seckill list
        var seckillIds = new Set(products.map(function(p) { return p.productId || p.id; }));
        regularProducts.forEach(function(rp) {
          if (!seckillIds.has(rp.id) && products.length < 8) {
            products.push({
              id: rp.id, name: rp.name, description: rp.description || '',
              image: rp.image || null, originalPrice: rp.minPrice || 99,
              seckillPrice: Math.round((rp.minPrice || 99) * 0.8 * 100) / 100,
              stock: 50, seckillStock: 50, isFiller: true
            });
          }
        });
      } catch(e) {}
    }
    var count = Math.min(8, products.length);
    seckillProducts = products.slice(0, count).map(function(p, idx) {"""
if old_load in content:
    content = content.replace(old_load, new_load)
    print("Fix 5: filler products added when seckill < 8")
else:
    print("Fix 5: pattern not found")

with open(filepath, 'w') as f:
    f.write(content)
print('Done')
