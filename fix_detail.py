#!/usr/bin/env python3
"""Fix detail.html: add seckill mode when ?seckill=1"""

filepath = '/home/ubuntu/blitz/blitz-server/src/main/resources/static/detail.html'

with open(filepath, 'r') as f:
    content = f.read()

# Add seckill banner and modify "立即购买" to "立即秒杀" when ?seckill=1
# Find the buyBtn section and add seckill support

# Fix 1: Add seckill hero section before detailContent
old_hero = '<div id="detailContent" class="hidden">'
new_hero = '''<div id="seckillBanner" class="hidden" style="background:linear-gradient(135deg,#ff4d4f,#ff7875);color:#fff;padding:16px 20px;border-radius:8px;margin-bottom:20px;display:flex;align-items:center;justify-content:space-between">
  <div><strong style="font-size:1.2rem">⚡ 限时秒杀</strong><span style="margin-left:12px;font-size:0.9rem;opacity:0.9" id="seckillCountdown"></span></div>
  <div style="font-size:0.85rem">手慢无！</div>
</div>
<div id="detailContent" class="hidden">'''
content = content.replace(old_hero, new_hero)
print("Fix 1: seckill banner added")

# Fix 2: Change buy button text and behavior for seckill mode
# In handleBuyNow, check if seckill mode
old_buy = "function handleBuyNow() {\n    /* Require SKU selection if product has SKUs */"
new_buy = '''function handleBuyNow() {
    var isSeckill = getQueryParam('seckill') === '1';
    /* Require SKU selection if product has SKUs */'''
content = content.replace(old_buy, new_buy)
print("Fix 2: seckill check added to buy handler")

# Fix 3: Show seckill banner and modify button in loadProduct
old_load = "product = r.data;\n      renderDetail();"
new_load = '''product = r.data;
      var isSeckill = getQueryParam('seckill') === '1';
      if (isSeckill) {
        document.getElementById('seckillBanner').classList.remove('hidden');
        document.getElementById('buyBtn').textContent = '⚡ 立即秒杀';
        document.getElementById('buyBtn').style.background = 'linear-gradient(135deg,#ff4d4f,#ff7875)';
        document.getElementById('buyBtn').style.border = 'none';
        document.getElementById('buyBtn').style.color = '#fff';
        // Start countdown to midnight
        var now = new Date();
        var midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59);
        if (midnight <= now) midnight.setDate(midnight.getDate() + 1);
        var timer = setInterval(function() {
          var diff = Math.max(0, Math.floor((midnight - Date.now()) / 1000));
          var h = Math.floor(diff / 3600), m = Math.floor((diff % 3600) / 60), s = diff % 60;
          var el = document.getElementById('seckillCountdown');
          if (el) el.textContent = '倒计时 ' + String(h).padStart(2,'0') + ':' + String(m).padStart(2,'0') + ':' + String(s).padStart(2,'0');
          if (diff <= 0) { clearInterval(timer); if (el) el.textContent = '已结束'; }
        }, 1000);
        // Fetch seckill price from API
        api.get('/seckill/list').then(function(sr) {
          if (sr.data) {
            var sp = sr.data.find(function(s) { return s.productId === product.id; });
            if (sp && sp.seckillPrice) {
              document.getElementById('currentPrice').textContent = formatPrice(sp.seckillPrice);
              var origEl = document.getElementById('originalPrice');
              origEl.textContent = formatPrice(product.minPrice || product.price);
              origEl.classList.remove('hidden');
            }
          }
        }).catch(function(){});
      }
      renderDetail();'''
content = content.replace(old_load, new_load)
print("Fix 3: seckill mode added to product load")

# Fix 4: In seckill mode, clicking buy goes to seckill page
old_buy2 = "if (!AUTH.isLoggedIn()) {\n      location.href = '/login.html?redirect=' + encodeURIComponent(location.pathname + location.search);\n      return;\n    }\n\n    var buyBtn = document.getElementById('buyBtn');\n    buyBtn.disabled = true;\n    buyBtn.textContent = '下单中...';\n\n    api.createOrder({"
new_buy2 = '''if (!AUTH.isLoggedIn()) {
      location.href = '/login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
      return;
    }

    if (isSeckill) {
      // Redirect to seckill page for seckill flow
      location.href = '/seckill.html';
      return;
    }

    var buyBtn = document.getElementById('buyBtn');
    buyBtn.disabled = true;
    buyBtn.textContent = '下单中...';

    api.createOrder({'''
content = content.replace(old_buy2, new_buy2)
print("Fix 4: seckill buy redirect to seckill page")

with open(filepath, 'w') as f:
    f.write(content)
print('Done')
