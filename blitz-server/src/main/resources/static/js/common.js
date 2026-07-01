/**
 * Blitz 智能商城 v2.0 — Shared Utilities
 * Include before page scripts: <script src="/js/common.js"></script>
 * Also requires: /js/api.js
 */

/* ================================================================
   Auth — JWT + localStorage
   ================================================================ */
const AUTH = {
  getToken() {
    return localStorage.getItem('blitz_token');
  },
  setToken(t) {
    localStorage.setItem('blitz_token', t);
  },
  clearToken() {
    localStorage.removeItem('blitz_token');
    localStorage.removeItem('blitz_user');
  },
  getUser() {
    try { return JSON.parse(localStorage.getItem('blitz_user') || 'null'); }
    catch { return null; }
  },
  setUser(u) {
    localStorage.setItem('blitz_user', JSON.stringify(u));
  },
  isLoggedIn() {
    return !!this.getToken();
  },
  getRole() {
    const u = this.getUser();
    if (!u) return null;
    if (u.role === 'ADMIN' || u.roleId === 1) return 'ADMIN';
    if (u.role === 'MERCHANT' || u.roleId === 3) return 'MERCHANT';
    return 'USER';
  },
  isAdmin() {
    return this.getRole() === 'ADMIN';
  },
  isMerchant() {
    return this.getRole() === 'MERCHANT';
  }
};

/* Global Auth Guard — redirect to login for protected pages */
(function() {
  var path = (location.pathname || '').replace(/^\/+|\/+$/g, '') || '/';
  var PUBLIC = ['', '/', 'index.html', 'login.html', 'products.html', 'detail.html',
    'seckill.html', 'shop.html', 'shops.html', 'coupons.html', 'brand.html'];
  var needsAuth = ['user.html', 'cart.html', 'checkout.html', 'payment.html',
    'merchant.html', 'admin.html'];
  var page = path.split('/').pop() || 'index.html';
  if (needsAuth.indexOf(page) !== -1 && !AUTH.isLoggedIn()) {
    location.href = '/login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
  }
})();

/* ================================================================
   Cart — localStorage-based
   ================================================================ */
const CART = {
  _key: 'blitz_cart',
  getItems() {
    try { return JSON.parse(localStorage.getItem(this._key) || '[]'); }
    catch { return []; }
  },
  saveItems(items) {
    localStorage.setItem(this._key, JSON.stringify(items));
  },
  add(product, skuId, quantity) {
    const items = this.getItems();
    const key = product.id + '_' + (skuId || 'default');
    const existing = items.find(i => i.key === key);
    if (existing) {
      existing.quantity += (quantity || 1);
    } else {
      const sku = product.skus ? product.skus.find(s => String(s.id) === String(skuId)) : null;
      items.push({
        key: key,
        productId: product.id,
        skuId: skuId || null,
        name: product.name,
        image: product.image || product.mainImage || '',
        price: sku ? sku.price : (product.price || product.minPrice || 0),
        skuName: sku ? sku.name : '默认',
        shopId: product.shopId || null,
        shopName: product.shopName || '',
        quantity: quantity || 1,
        checked: true
      });
    }
    this.saveItems(items);
  },
  remove(key) {
    this.saveItems(this.getItems().filter(i => i.key !== key));
  },
  updateQty(key, qty) {
    const items = this.getItems();
    const item = items.find(i => i.key === key);
    if (item) { item.quantity = Math.max(1, qty); }
    this.saveItems(items);
  },
  toggleCheck(key) {
    const items = this.getItems();
    const item = items.find(i => i.key === key);
    if (item) { item.checked = !item.checked; }
    this.saveItems(items);
  },
  toggleAll(checked) {
    const items = this.getItems();
    items.forEach(i => { i.checked = checked; });
    this.saveItems(items);
  },
  removeChecked() {
    this.saveItems(this.getItems().filter(i => !i.checked));
  },
  getCount() {
    return this.getItems().reduce((sum, i) => sum + i.quantity, 0);
  },
  getCheckedTotal() {
    return this.getItems()
      .filter(i => i.checked)
      .reduce((sum, i) => sum + i.price * i.quantity, 0);
  },
  getCheckedItems() {
    return this.getItems().filter(i => i.checked);
  },
  clear() {
    localStorage.removeItem(this._key);
  }
};

/* ================================================================
   Navbar — role-aware rendering
   ================================================================ */
function renderNavbar(currentPage) {
  const container = document.getElementById('blitz-navbar');
  if (!container) return;

  const user = AUTH.getUser();
  const role = AUTH.getRole();
  const cartCount = CART.getCount();

  const brandHTML = '<a href="/" class="nav-brand"><span class="brand-icon">⚡</span>Blitz</a>';

  const searchHTML = `
    <div class="nav-search">
      <input type="text" placeholder="搜索商品、品牌、分类..." id="navSearchInput" value="${getQueryParam('keyword') || ''}">
      <button id="navSearchBtn">🔍</button>
    </div>`;

  let userHTML;
  if (user) {
    userHTML = `
      <a href="/cart.html" class="cart-badge" style="position:relative">
        🛒 购物车${cartCount > 0 ? `<span class="badge-count">${cartCount > 99 ? '99+' : cartCount}</span>` : ''}
      </a>
      <div class="nav-user-info">
        <span class="user-avatar">${(user.username || 'U').charAt(0).toUpperCase()}</span>
        <span>${user.username || '用户'}</span>
      </div>
      <a href="#" onclick="event.preventDefault();doLogout()">退出</a>
    `;
  } else {
    userHTML = `
      <a href="/cart.html">🛒 购物车</a>
      <a href="/login.html">登录</a>
    `;
  }

  container.innerHTML = `
    <div class="nav-inner">
      ${brandHTML}
      ${searchHTML}
      <div class="nav-icons">
        <a href="/" class="${currentPage === 'home' ? 'active' : ''}">🏠 首页</a>
        <a href="/products.html" class="${currentPage === 'products' ? 'active' : ''}">📦 全部商品</a>
        <a href="/seckill.html" class="${currentPage === 'seckill' ? 'active' : ''}">⚡ 秒杀</a>
        ${user ? `<a href="/user.html" class="${currentPage === 'user' ? 'active' : ''}">📋 我的订单</a>` : ''}
        ${role === 'MERCHANT' ? `<a href="/merchant.html" class="${currentPage === 'merchant' ? 'active' : ''}">🏪 商家中心</a>` : ''}
        ${role === 'ADMIN' ? `<a href="/admin.html" class="${currentPage === 'admin' ? 'active' : ''}">⚙️ 管理后台</a>` : ''}
        ${userHTML}
      </div>
    </div>`;

  // Bind search
  setTimeout(() => {
    const si = document.getElementById('navSearchInput');
    const sb = document.getElementById('navSearchBtn');
    if (si && sb) {
      const doSearch = () => {
        const kw = si.value.trim();
        if (kw) location.href = '/products.html?keyword=' + encodeURIComponent(kw);
        else location.href = '/products.html';
      };
      sb.onclick = doSearch;
      si.addEventListener('keydown', e => { if (e.key === 'Enter') doSearch(); });
    }
  }, 0);
}

function doLogout() {
  AUTH.clearToken();
  CART.clear();
  location.href = '/';
}

/* ================================================================
   Mobile Navigation
   ================================================================ */
var _mobileInjected = false;

function initMobileNav(currentPage) {
  if (_mobileInjected) return;
  _mobileInjected = true;
  currentPage = currentPage || 'home';

  const body = document.body;
  const user = AUTH.getUser();
  const role = AUTH.getRole();

  /* Hamburger */
  const navInner = document.querySelector('.nav-inner');
  if (navInner) {
    const hamburger = document.createElement('button');
    hamburger.className = 'hamburger-btn';
    hamburger.id = 'hamburgerBtn';
    hamburger.setAttribute('aria-label', '菜单');
    hamburger.innerHTML = '☰';
    navInner.insertBefore(hamburger, navInner.firstChild);
  }

  /* Drawer */
  const drawer = document.createElement('div');
  drawer.className = 'mobile-nav-drawer';
  drawer.id = 'mobileNavDrawer';
  drawer.innerHTML =
    '<div class="drawer-header">' +
      '<span class="drawer-brand">⚡ Blitz</span>' +
      '<button class="drawer-close" id="drawerCloseBtn" aria-label="关闭">✕</button>' +
    '</div>' +
    '<div class="drawer-section">' +
      '<div class="drawer-section-title">导航</div>' +
      '<a href="/" class="drawer-link' + (currentPage === 'home' ? ' active' : '') + '"><span class="drawer-icon">🏠</span>首页</a>' +
      '<a href="/products.html" class="drawer-link' + (currentPage === 'products' ? ' active' : '') + '"><span class="drawer-icon">📦</span>全部商品</a>' +
      '<a href="/seckill.html" class="drawer-link' + (currentPage === 'seckill' ? ' active' : '') + '"><span class="drawer-icon">⚡</span>秒杀专区</a>' +
      '<a href="#" class="drawer-link" id="drawerCatBtn"><span class="drawer-icon">📂</span>商品分类</a>' +
      '<a href="/cart.html" class="drawer-link' + (currentPage === 'cart' ? ' active' : '') + '"><span class="drawer-icon">🛒</span>购物车' + (CART.getCount() > 0 ? ' (' + CART.getCount() + ')' : '') + '</a>' +
    '</div>' +
    '<div class="drawer-section">' +
      '<div class="drawer-section-title">账户</div>' +
      (user
        ? '<a href="/user.html" class="drawer-link' + (currentPage === 'user' ? ' active' : '') + '"><span class="drawer-icon">📋</span>我的订单</a>' +
          (role === 'MERCHANT' ? '<a href="/merchant.html" class="drawer-link' + (currentPage === 'merchant' ? ' active' : '') + '"><span class="drawer-icon">🏪</span>商家中心</a>' : '') +
          (role === 'ADMIN' ? '<a href="/admin.html" class="drawer-link' + (currentPage === 'admin' ? ' active' : '') + '"><span class="drawer-icon">⚙️</span>管理后台</a>' : '') +
          '<a href="#" class="drawer-link" id="drawerLogoutBtn"><span class="drawer-icon">🚪</span>退出登录</a>'
        : '<a href="/login.html" class="drawer-link"><span class="drawer-icon">👤</span>登录 / 注册</a>'
      ) +
    '</div>';
  body.appendChild(drawer);

  /* Overlay */
  const overlay = document.createElement('div');
  overlay.className = 'mobile-nav-overlay';
  overlay.id = 'mobileNavOverlay';
  body.appendChild(overlay);

  /* Category Sheet */
  const catSheet = document.createElement('div');
  catSheet.className = 'mobile-cat-sheet';
  catSheet.id = 'mobileCatSheet';
  catSheet.innerHTML =
    '<div class="sheet-handle"></div>' +
    '<div class="sheet-title">商品分类</div>' +
    '<div id="mobileCatList"></div>';
  body.appendChild(catSheet);

  const catOverlay = document.createElement('div');
  catOverlay.className = 'mobile-cat-overlay';
  catOverlay.id = 'mobileCatOverlay';
  body.appendChild(catOverlay);

  /* Tab Bar */
  const tabBar = document.createElement('nav');
  tabBar.className = 'mobile-tab-bar';
  tabBar.id = 'mobileTabBar';
  tabBar.innerHTML =
    '<div class="tab-inner">' +
      '<a href="/" class="tab-item' + (currentPage === 'home' ? ' active' : '') + '"><span class="tab-icon">🏠</span>首页</a>' +
      '<a href="#" class="tab-item" id="tabCatBtn"><span class="tab-icon">📂</span>分类</a>' +
      '<a href="/seckill.html" class="tab-item' + (currentPage === 'seckill' ? ' active' : '') + '"><span class="tab-icon">⚡</span>秒杀</a>' +
      '<a href="/cart.html" class="tab-item' + (currentPage === 'cart' ? ' active' : '') + '"><span class="tab-icon">🛒</span>购物车</a>' +
      '<a href="/user.html" class="tab-item' + (currentPage === 'user' ? ' active' : '') + '"><span class="tab-icon">👤</span>我的</a>' +
    '</div>';
  body.appendChild(tabBar);

  bindMobileEvents();
}

function bindMobileEvents() {
  const drawer = document.getElementById('mobileNavDrawer');
  const overlay = document.getElementById('mobileNavOverlay');

  function openDrawer() {
    if (!drawer || !overlay) return;
    drawer.classList.add('open');
    overlay.classList.add('show');
    document.body.style.overflow = 'hidden';
  }
  function closeDrawer() {
    if (!drawer || !overlay) return;
    drawer.classList.remove('open');
    overlay.classList.remove('show');
    document.body.style.overflow = '';
  }

  const hamburger = document.getElementById('hamburgerBtn');
  if (hamburger) {
    hamburger.addEventListener('click', e => {
      e.stopPropagation();
      drawer && drawer.classList.contains('open') ? closeDrawer() : openDrawer();
    });
  }

  const closeBtn = document.getElementById('drawerCloseBtn');
  if (closeBtn) closeBtn.addEventListener('click', closeDrawer);
  if (overlay) overlay.addEventListener('click', closeDrawer);

  // Category sheet
  const catSheet = document.getElementById('mobileCatSheet');
  const catOverlay = document.getElementById('mobileCatOverlay');

  function openCatSheet() {
    if (!catSheet || !catOverlay) return;
    closeDrawer();
    loadMobileCategories();
    catSheet.classList.add('open');
    catOverlay.classList.add('show');
    document.body.style.overflow = 'hidden';
  }
  function closeCatSheet() {
    if (!catSheet || !catOverlay) return;
    catSheet.classList.remove('open');
    catOverlay.classList.remove('show');
    document.body.style.overflow = '';
  }

  if (catOverlay) catOverlay.addEventListener('click', closeCatSheet);

  const drawerCatBtn = document.getElementById('drawerCatBtn');
  const tabCatBtn = document.getElementById('tabCatBtn');
  if (drawerCatBtn) drawerCatBtn.addEventListener('click', e => { e.preventDefault(); openCatSheet(); });
  if (tabCatBtn) tabCatBtn.addEventListener('click', e => { e.preventDefault(); openCatSheet(); });

  const drawerLogoutBtn = document.getElementById('drawerLogoutBtn');
  if (drawerLogoutBtn) drawerLogoutBtn.addEventListener('click', e => { e.preventDefault(); doLogout(); });

  // Swipe to close drawer
  if (drawer) {
    let tx = 0;
    drawer.addEventListener('touchstart', e => { tx = e.touches[0].clientX; }, { passive: true });
    drawer.addEventListener('touchmove', e => {
      if (e.touches[0].clientX - tx < -50) closeDrawer();
    }, { passive: true });
  }
  // Swipe down to close sheet
  if (catSheet) {
    let ty = 0;
    catSheet.addEventListener('touchstart', e => { ty = e.touches[0].clientY; }, { passive: true });
    catSheet.addEventListener('touchmove', e => {
      if (e.touches[0].clientY - ty > 80) closeCatSheet();
    }, { passive: true });
  }
}

var _mobileCatsLoaded = false;
function loadMobileCategories() {
  if (_mobileCatsLoaded) return;
  _mobileCatsLoaded = true;
  const container = document.getElementById('mobileCatList');
  if (!container) return;
  container.innerHTML = '<div style="text-align:center;padding:24px;color:var(--text-muted)">加载中...</div>';

  if (typeof api !== 'undefined' && api.getCategories) {
    api.getCategories().then(r => {
      const cats = r.data || [];
      let html = '';
      function walk(list) {
        if (!list) return;
        list.forEach(node => {
          html += `<div class="sheet-cat-item" onclick="location.href='/products.html?categoryId=${node.id}'">${node.icon || '📂'} ${node.name}</div>`;
          if (node.children && node.children.length) walk(node.children);
        });
      }
      walk(cats);
      container.innerHTML = html || '<div style="text-align:center;padding:24px;color:var(--text-muted)">暂无分类</div>';
    }).catch(() => {
      container.innerHTML = '<div style="text-align:center;padding:24px;color:var(--text-muted)">加载失败</div>';
    });
  }
}

/* ================================================================
   Toast Notifications
   ================================================================ */
let _toastTimer = null;
function showToast(msg, type) {
  type = type || 'info';
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const el = document.createElement('div');
  el.className = 'toast toast-' + type;
  el.textContent = msg;
  container.appendChild(el);
  clearTimeout(_toastTimer);
  _toastTimer = setTimeout(() => { el.remove(); }, 3000);
}

/* ================================================================
   Modal System
   ================================================================ */
function openModal(id) {
  const el = document.getElementById(id);
  if (el) { el.classList.add('active'); document.body.style.overflow = 'hidden'; }
}
function closeModal(id) {
  const el = document.getElementById(id);
  if (el) { el.classList.remove('active'); document.body.style.overflow = ''; }
}
// Close modal on overlay click
document.addEventListener('click', function(e) {
  if (e.target.classList.contains('modal-overlay') && e.target.classList.contains('active')) {
    closeModal(e.target.id);
  }
});

/* ================================================================
   Formatting Helpers
   ================================================================ */
function formatPrice(n) {
  if (n == null || isNaN(n)) return '-';
  return '¥' + Number(n).toFixed(2);
}

function formatTime(t) {
  if (!t) return '-';
  const d = new Date(t);
  if (isNaN(d.getTime())) return String(t);
  const pad = n => String(n).padStart(2, '0');
  const now = new Date();
  const diff = now - d;
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  if (diff < 172800000) return '昨天 ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
}

function formatDate(t) {
  if (!t) return '-';
  const d = new Date(t);
  if (isNaN(d.getTime())) return String(t);
  const pad = n => String(n).padStart(2, '0');
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
}

function formatNumber(n) {
  if (n == null) return '0';
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k';
  return String(n);
}

function getQueryParam(name) {
  return new URLSearchParams(location.search).get(name);
}

/* ================================================================
   Star Rating Component
   ================================================================ */
function renderStars(rating, size) {
  size = size || 'small';
  const full = Math.floor(rating);
  const half = rating - full >= 0.5 ? 1 : 0;
  const empty = 5 - full - half;
  let html = '';
  for (let i = 0; i < full; i++) html += '<span style="color:var(--warning)">★</span>';
  if (half) html += '<span style="color:var(--warning)">★</span>'; // simplified
  for (let i = 0; i < empty; i++) html += '<span style="color:var(--border)">★</span>';
  return html;
}

function createStarInput(containerId, onChange) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.className = 'star-rating interactive';
  container.innerHTML = '';
  for (let i = 1; i <= 5; i++) {
    const star = document.createElement('span');
    star.className = 'star';
    star.textContent = '★';
    star.dataset.value = i;
    star.addEventListener('click', () => {
      container.querySelectorAll('.star').forEach((s, idx) => {
        s.classList.toggle('active', idx < i);
      });
      if (onChange) onChange(i);
    });
    star.addEventListener('mouseenter', () => {
      container.querySelectorAll('.star').forEach((s, idx) => {
        s.classList.toggle('active', idx < i);
      });
    });
    container.appendChild(star);
  }
  container.addEventListener('mouseleave', () => {
    const active = container.dataset.value || 0;
    container.querySelectorAll('.star').forEach((s, idx) => {
      s.classList.toggle('active', idx < active);
    });
  });
}

/* ================================================================
   Image Upload Preview (base64)
   ================================================================ */
function createImageUpload(containerId, maxImages) {
  maxImages = maxImages || 6;
  const container = document.getElementById(containerId);
  if (!container) return { getImages: () => [] };

  let images = [];
  const previewContainer = document.createElement('div');
  previewContainer.className = 'upload-previews';
  container.appendChild(previewContainer);

  function render() {
    previewContainer.innerHTML = images.map((img, i) =>
      `<div class="upload-preview">
        <img src="${img}" alt="preview">
        <span class="remove-btn" data-index="${i}">✕</span>
      </div>`
    ).join('');
    if (images.length < maxImages) {
      previewContainer.innerHTML += `<label class="upload-add">+<input type="file" accept="image/*" style="display:none" id="${containerId}_input"></label>`;
    }
    // Bind remove
    previewContainer.querySelectorAll('.remove-btn').forEach(btn => {
      btn.addEventListener('click', e => {
        e.stopPropagation();
        images.splice(parseInt(btn.dataset.index), 1);
        render();
      });
    });
    // Bind add
    const input = document.getElementById(containerId + '_input');
    if (input) {
      input.addEventListener('change', e => {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = ev => {
          images.push(ev.target.result);
          render();
        };
        reader.readAsDataURL(file);
      });
    }
  }

  render();
  return { getImages: () => images, reset: () => { images = []; render(); } };
}

/* ================================================================
   Table Cardify (mobile responsive)
   ================================================================ */
function cardifyTable(tbodyId, labels) {
  const tbody = document.getElementById(tbodyId);
  if (!tbody) return;
  tbody.querySelectorAll('tr').forEach(row => {
    const cells = row.querySelectorAll('td');
    cells.forEach((cell, j) => {
      if (j < labels.length) cell.setAttribute('data-label', labels[j]);
    });
  });
}

/* ================================================================
   Auth Guards
   ================================================================ */
function requireAuth() {
  if (!AUTH.isLoggedIn()) {
    location.href = '/login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
    return false;
  }
  return true;
}

function requireRole(role) {
  if (!requireAuth()) return false;
  if (AUTH.getRole() !== role) {
    showToast('无' + (role === 'ADMIN' ? '管理员' : role === 'MERCHANT' ? '商家' : '') + '权限', 'error');
    setTimeout(() => { location.href = '/'; }, 1500);
    return false;
  }
  return true;
}

function requireAdmin() { return requireRole('ADMIN'); }
function requireMerchant() { return requireRole('MERCHANT'); }

async function fetchUser() {
  if (!AUTH.isLoggedIn()) return null;
  try {
    const r = await api.getCurrentUser();
    if (r.data) {
      AUTH.setUser(r.data);
      return r.data;
    }
  } catch (e) {
    // Token might be expired
    AUTH.clearToken();
  }
  return null;
}

/* ================================================================
   Pagination Helper
   ================================================================ */
function getPageNumbers(current, total) {
  if (total <= 7) {
    const result = [];
    for (let i = 1; i <= total; i++) result.push(i);
    return result;
  }
  if (current <= 4) return [1, 2, 3, 4, 5, '...', total];
  if (current >= total - 3) return [1, '...', total - 4, total - 3, total - 2, total - 1, total];
  return [1, '...', current - 1, current, current + 1, '...', total];
}

function renderPagination(containerId, page, totalPages, onPageChange) {
  const container = document.getElementById(containerId);
  if (!container) return;
  if (totalPages <= 1) { container.innerHTML = ''; return; }

  const pageNums = getPageNumbers(page, totalPages);
  let html = '';
  html += `<button data-page="${page - 1}"${page <= 1 ? ' disabled' : ''}>上一页</button>`;
  pageNums.forEach(p => {
    if (p === '...') {
      html += '<button disabled>...</button>';
    } else {
      html += `<button class="${p === page ? 'active' : ''}" data-page="${p}">${p}</button>`;
    }
  });
  html += `<button data-page="${page + 1}"${page >= totalPages ? ' disabled' : ''}>下一页</button>`;

  container.innerHTML = html;
  container.onclick = function(e) {
    const btn = e.target.closest('button');
    if (!btn || btn.disabled) return;
    const p = parseInt(btn.getAttribute('data-page'), 10);
    if (p >= 1 && p <= totalPages && p !== page && onPageChange) onPageChange(p);
  };
}

/* ================================================================
   Status Labels
   ================================================================ */
function statusLabel(s) {
  const map = {
    PENDING: '待支付', PENDING_PAYMENT: '待支付',
    PAID: '已支付', SHIPPED: '已发货',
    RECEIVED: '已收货', COMPLETED: '已完成',
    CANCELLED: '已取消', REFUNDED: '已退款'
  };
  return map[s] || s || '未知';
}

/* ================================================================
   AI Chat Widget — injected on all pages
   Uses /ai/chat/stream (SSE) and /ai/session
   ================================================================ */
var AI_CHAT = {
  sessionId: null,
  isOpen: false,
  isStreaming: false,

  init: function() {
    if (document.getElementById('ai-chat-fab')) return;
    if (!AUTH.isLoggedIn()) return; // Only for logged-in users

    // FAB button
    var fab = document.createElement('button');
    fab.id = 'ai-chat-fab';
    fab.className = 'ai-chat-fab';
    fab.innerHTML = '💬<span class="fab-badge"></span>';
    fab.title = 'AI 智能客服';
    document.body.appendChild(fab);

    // Chat panel
    var panel = document.createElement('div');
    panel.id = 'ai-chat-panel';
    panel.className = 'ai-chat-panel';
    panel.innerHTML =
      '<div class="chat-header">' +
        '<h4>🤖 AI 智能客服</h4>' +
        '<div>' +
          '<span class="chat-status" id="chatStatus">在线</span>' +
          '<button class="chat-close" id="chatCloseBtn">✕</button>' +
        '</div>' +
      '</div>' +
      '<div class="chat-messages" id="chatMessages">' +
        '<div class="chat-msg ai">' +
          '你好！我是 Blitz 智能客服 🤖<br>可以问我商品咨询、订单问题、售后帮助等问题～' +
        '</div>' +
      '</div>' +
      '<div class="chat-quick-qs" id="chatQuickQs">' +
        '<button class="chat-quick-q" data-q="如何下单购买商品？">如何下单？</button>' +
        '<button class="chat-quick-q" data-q="秒杀活动怎么参与？">秒杀怎么玩？</button>' +
        '<button class="chat-quick-q" data-q="如何申请退款退货？">如何退款？</button>' +
        '<button class="chat-quick-q" data-q="发货后多久能收到？">多久到货？</button>' +
      '</div>' +
      '<div class="chat-input-wrap">' +
        '<input type="text" id="chatInput" placeholder="输入你的问题..." maxlength="500">' +
        '<button id="chatSendBtn">➤</button>' +
      '</div>';
    document.body.appendChild(panel);

    fab.addEventListener('click', function() { AI_CHAT.toggle(); });
    document.getElementById('chatCloseBtn').addEventListener('click', function(e) {
      e.stopPropagation(); AI_CHAT.close();
    });

    var input = document.getElementById('chatInput');
    var sendBtn = document.getElementById('chatSendBtn');
    var send = function() { AI_CHAT.send(input.value.trim()); };
    sendBtn.addEventListener('click', send);
    input.addEventListener('keydown', function(e) {
      if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send(); }
    });

    // Quick questions
    document.getElementById('chatQuickQs').addEventListener('click', function(e) {
      var btn = e.target.closest('.chat-quick-q');
      if (!btn) return;
      var q = btn.getAttribute('data-q');
      AI_CHAT.send(q);
    });

    // Load saved sessionId
    this.sessionId = localStorage.getItem('ai_chat_session');
  },

  toggle: function() {
    this.isOpen ? this.close() : this.open();
  },

  open: function() {
    var panel = document.getElementById('ai-chat-panel');
    if (!panel) return;
    panel.classList.add('open');
    this.isOpen = true;
    document.getElementById('chatInput').focus();
  },

  close: function() {
    var panel = document.getElementById('ai-chat-panel');
    if (!panel) return;
    panel.classList.remove('open');
    this.isOpen = false;
  },

  send: function(msg) {
    if (!msg || this.isStreaming) return;
    this.isStreaming = true;

    var input = document.getElementById('chatInput');
    var sendBtn = document.getElementById('chatSendBtn');
    input.value = '';
    input.disabled = true;
    sendBtn.disabled = true;

    // Add user message
    this._addMsg('user', msg);

    // Add AI placeholder with typing indicator
    var aiEl = this._addMsg('ai', '<div class="msg-typing"><span></span><span></span><span></span></div>');
    aiEl.classList.add('streaming');
    var aiContent = '';
    var that = this;

    // Build request body
    var body = { message: msg };
    if (this.sessionId) body.sessionId = parseInt(this.sessionId, 10);

    // Send SSE request
    fetch('/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + AUTH.getToken()
      },
      body: JSON.stringify(body)
    }).then(function(response) {
      if (!response.ok) throw new Error('AI service unavailable');
      var reader = response.body.getReader();
      var decoder = new TextDecoder();
      var buffer = '';

      function pump() {
        return reader.read().then(function(result) {
          if (result.done) {
            that._finishStream(aiEl, aiContent);
            return;
          }
          buffer += decoder.decode(result.value, { stream: true });
          // Parse SSE data lines
          var lines = buffer.split('\n');
          buffer = lines.pop() || ''; // Keep incomplete line in buffer
          for (var i = 0; i < lines.length; i++) {
            var line = lines[i].trim();
            if (!line || !line.startsWith('data:')) continue;
            var content = line.substring(5).trim();
            if (!content) continue;
            // Try to parse as JSON (Spring AI SseEmitter wraps content)
            try {
              var json = JSON.parse(content);
              if (json.content !== undefined) content = json.content;
              if (json.sessionId && !that.sessionId) {
                that.sessionId = json.sessionId;
                localStorage.setItem('ai_chat_session', json.sessionId);
              }
            } catch(e) {
              // Plain text content — use directly
            }
            if (typeof content === 'string' && content.length > 0) {
              aiContent += content;
              aiEl.innerHTML = that._escapeHtml(aiContent);
              that._scrollBottom();
            }
            // Check for [DONE] marker
            if (content === '[DONE]' || content.indexOf('[DONE]') !== -1) {
              that._finishStream(aiEl, aiContent.replace('[DONE]', ''));
              return;
            }
          }
          return pump();
        }).catch(function() {
          that._finishStream(aiEl, aiContent || '抱歉，回复中断了，请重试。');
        });
      }
      return pump();
    }).catch(function(err) {
      aiEl.innerHTML = '抱歉，AI 服务暂时不可用 😢<br><small>' + that._escapeHtml(err.message) + '</small>';
      that._finishStream(aiEl, null);
    });
  },

  _addMsg: function(type, html) {
    var container = document.getElementById('chatMessages');
    var el = document.createElement('div');
    el.className = 'chat-msg ' + type;
    el.innerHTML = html;
    container.appendChild(el);
    this._scrollBottom();
    return el;
  },

  _finishStream: function(el, content) {
    el.classList.remove('streaming');
    if (content && content.trim()) {
      el.innerHTML = this._escapeHtml(content).replace(/\n/g, '<br>');
    }
    this.isStreaming = false;
    var input = document.getElementById('chatInput');
    var sendBtn = document.getElementById('chatSendBtn');
    if (input) { input.disabled = false; input.focus(); }
    if (sendBtn) sendBtn.disabled = false;
  },

  _scrollBottom: function() {
    var container = document.getElementById('chatMessages');
    if (container) container.scrollTop = container.scrollHeight;
  },

  _escapeHtml: function(str) {
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
  }
};

// Auto-init AI chat on page load
document.addEventListener('DOMContentLoaded', function() {
  setTimeout(function() { AI_CHAT.init(); }, 500);
  setTimeout(function() { SEARCH_AC.init(); }, 600);
});

/* Coupon System — localStorage demo */
var COUPONS = {
  _key: 'blitz_coupons',
  // Pre-seeded demo coupons
  _seed: [
    {id:'c1',name:'新人专享券',amount:20, minSpend:99, expire:'2026-12-31',used:false},
    {id:'c2',name:'满减优惠券',amount:15, minSpend:149, expire:'2026-12-31',used:false},
    {id:'c3',name:'618大促券',amount:50, minSpend:299, expire:'2026-07-15',used:false},
    {id:'c4',name:'运费券',amount:10, minSpend:0, expire:'2026-12-31',used:false},
  ],
  init: function() {
    var stored = localStorage.getItem(this._key);
    if (!stored) { this.save(this._seed); return; }
    try { var arr = JSON.parse(stored); if (arr.length === 0) this.save(this._seed); }
    catch { this.save(this._seed); }
  },
  getAll: function() {
    try { return JSON.parse(localStorage.getItem(this._key) || '[]'); }
    catch { return this._seed; }
  },
  getAvailable: function(totalAmount) {
    return this.getAll().filter(function(c) { return !c.used && totalAmount >= c.minSpend; });
  },
  use: function(couponId) {
    var coupons = this.getAll();
    var c = coupons.find(function(x) { return x.id === couponId; });
    if (c) { c.used = true; this.save(coupons); }
  },
  save: function(coupons) { localStorage.setItem(this._key, JSON.stringify(coupons)); },
  count: function() { return this.getAll().filter(function(c){return !c.used;}).length; }
};
COUPONS.init();

/* ================================================================
   Favorites / Wishlist — localStorage
   ================================================================ */
var FAVS = {
  _key: 'blitz_favs',
  getAll: function() {
    try { return JSON.parse(localStorage.getItem(this._key) || '[]'); }
    catch { return []; }
  },
  add: function(productId) {
    var ids = this.getAll();
    if (ids.indexOf(productId) === -1) { ids.push(productId); }
    localStorage.setItem(this._key, JSON.stringify(ids));
  },
  remove: function(productId) {
    var ids = this.getAll().filter(function(id) { return id !== productId; });
    localStorage.setItem(this._key, JSON.stringify(ids));
  },
  toggle: function(productId) {
    this.isFaved(productId) ? this.remove(productId) : this.add(productId);
  },
  isFaved: function(productId) {
    return this.getAll().indexOf(productId) !== -1;
  },
  count: function() {
    return this.getAll().length;
  }
};

/* ================================================================
   Browsing History — localStorage (max 20 product IDs)
   (Defined below with extended features — this section is now unified)
   ================================================================ */

/* Back to Top */
(function() {
  var btn = document.createElement('button');
  btn.className = 'back-to-top';
  btn.id = 'backToTop';
  btn.innerHTML = '↑';
  btn.title = '回到顶部';
  btn.onclick = function() { window.scrollTo({top:0, behavior:'smooth'}); };
  document.body.appendChild(btn);
  window.addEventListener('scroll', function() {
    btn.classList.toggle('visible', window.scrollY > 400);
  });
})();

/* Search Autocomplete */
var SEARCH_AC = {
  _timer: null,
  _dropdown: null,
  init: function() {
    var searchWrap = document.querySelector('.nav-search');
    if (!searchWrap) return;
    var input = document.getElementById('navSearchInput');
    if (!input) return;

    // Create dropdown
    var dd = document.createElement('div');
    dd.className = 'search-dropdown';
    dd.id = 'searchDropdown';
    searchWrap.appendChild(dd);
    this._dropdown = dd;

    var that = this;
    input.addEventListener('input', function() {
      clearTimeout(that._timer);
      var kw = this.value.trim();
      if (kw.length < 1) { that.hide(); return; }
      that._timer = setTimeout(function() { that.search(kw); }, 250);
    });
    input.addEventListener('focus', function() {
      if (this.value.trim().length >= 1) that.search(this.value.trim());
    });
    document.addEventListener('click', function(e) {
      if (!searchWrap.contains(e.target)) that.hide();
    });
    // Enter key navigates to search results
    input.addEventListener('keydown', function(e) {
      if (e.key === 'Escape') that.hide();
    });
  },
  search: function(kw) {
    var that = this;
    if (typeof api === 'undefined' || !api.searchProducts) return;
    api.searchProducts(kw, {size: 8}).then(function(r) {
      var products = (r.data && r.data.records) ? r.data.records : (Array.isArray(r.data) ? r.data : []);
      that.render(products, kw);
    }).catch(function() { that.hide(); });
  },
  render: function(products, kw) {
    var dd = this._dropdown;
    if (!dd) return;
    if (!products || products.length === 0) {
      dd.innerHTML = '<div class="sd-empty">未找到相关商品</div>';
      dd.classList.add('show');
      return;
    }
    var html = '';
    products.forEach(function(p) {
      var price = p.price != null ? p.price : p.minPrice;
      var img = p.image || p.mainImage || '';
      html += '<div class="sd-item" onclick="location.href=\'/detail.html?id=' + p.id + '\'">' +
        '<div class="sd-img">' + (img ? '<img src="' + img + '" alt="">' : '📦') + '</div>' +
        '<div class="sd-info"><div class="sd-name">' + p.name + '</div>' +
        '<div class="sd-price">' + (typeof formatPrice !== 'undefined' ? formatPrice(price) : '¥' + Number(price).toFixed(2)) + '</div></div>' +
      '</div>';
    });
    html += '<div class="sd-item" style="justify-content:center;color:var(--primary);font-weight:600" onclick="location.href=\'/products.html?keyword=' + encodeURIComponent(kw) + '\'">查看全部结果 →</div>';
    dd.innerHTML = html;
    dd.classList.add('show');
  },
  hide: function() {
    if (this._dropdown) this._dropdown.classList.remove('show');
  }
};

/* Browsing History — localStorage, used for personalized recommendations */
var BROWSING_HISTORY = {
  _key: 'blitz_history',
  _max: 30,
  add: function(productId) {
    if (!productId) return;
    var ids = this.getAll();
    var idx = ids.indexOf(productId);
    if (idx !== -1) ids.splice(idx, 1);
    ids.unshift(productId);
    if (ids.length > this._max) ids = ids.slice(0, this._max);
    localStorage.setItem(this._key, JSON.stringify(ids));
  },
  getAll: function() {
    try { return JSON.parse(localStorage.getItem(this._key) || '[]'); }
    catch { return []; }
  },
  getRecent: function(n) {
    return this.getAll().slice(0, n || 10);
  },
  clear: function() {
    localStorage.removeItem(this._key);
  },
  getCategoryIds: function() {
    // Extract unique category IDs from stored metadata
    try {
      var meta = JSON.parse(localStorage.getItem('blitz_history_meta') || '{}');
      var cats = [];
      Object.values(meta).forEach(function(m) { if (m && m.categoryId && cats.indexOf(m.categoryId)===-1) cats.push(m.categoryId); });
      return cats;
    } catch { return []; }
  },
  addMeta: function(productId, categoryId) {
    try {
      var meta = JSON.parse(localStorage.getItem('blitz_history_meta') || '{}');
      meta[productId] = { categoryId: categoryId, time: Date.now() };
      localStorage.setItem('blitz_history_meta', JSON.stringify(meta));
    } catch {}
  }
};

/* ================================================================
   Confetti Celebration
   ================================================================ */
function showConfetti() {
  var colors = [
    '#FF6B6B', '#FFE66D', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
    '#DDA0DD', '#FF8C00', '#00CED1', '#FF1493', '#7B68EE', '#00FA9A'
  ];
  var container = document.createElement('div');
  container.id = 'confetti-container';
  container.style.cssText =
    'position:fixed;top:0;left:0;width:100%;height:100%;' +
    'pointer-events:none;z-index:99999;overflow:hidden;';
  document.body.appendChild(container);

  for (var i = 0; i < 60; i++) {
    var particle = document.createElement('div');
    var color = colors[Math.floor(Math.random() * colors.length)];
    var left = Math.random() * 100;
    var size = 6 + Math.random() * 8;
    var delay = Math.random() * 0.8;
    var duration = 2 + Math.random() * 3;
    var rotation = Math.random() * 720 - 360;

    particle.style.cssText =
      'position:absolute;' +
      'left:' + left + '%;' +
      'top:-' + size + 'px;' +
      'width:' + size + 'px;' +
      'height:' + size + 'px;' +
      'background:' + color + ';' +
      'border-radius:' + (Math.random() > 0.5 ? '50%' : '2px') + ';' +
      'opacity:0.9;' +
      'transition: transform ' + duration + 's ease-in ' + delay + 's, ' +
                  'opacity ' + duration + 's ease-in ' + delay + 's;';

    container.appendChild(particle);

    (function(p, d, r) {
      setTimeout(function() {
        p.style.transform = 'translateY(110vh) rotate(' + r + 'deg)';
        p.style.opacity = '0';
      }, 10);
    })(particle, duration, rotation);
  }

  setTimeout(function() {
    if (container.parentNode) container.parentNode.removeChild(container);
  }, 5500);
}

/* ================================================================
   Cart Badge Update
   ================================================================ */
function updateCartBadge() {
  var count = CART.getCount();
  var badge = document.querySelector('.cart-badge');
  if (!badge) return;
  var existingBadge = badge.querySelector('.badge-count');
  if (count > 0) {
    var text = count > 99 ? '99+' : String(count);
    if (existingBadge) {
      existingBadge.textContent = text;
    } else {
      var span = document.createElement('span');
      span.className = 'badge-count';
      span.textContent = text;
      badge.appendChild(span);
    }
  } else {
    if (existingBadge) existingBadge.remove();
  }
}

/* ================================================================
   Fly-to-Cart Animation
   ================================================================ */
var _flyToCartStyleInjected = false;

function _ensureFlyToCartStyle() {
  if (_flyToCartStyleInjected) return;
  _flyToCartStyleInjected = true;
  var style = document.createElement('style');
  style.textContent =
    '@keyframes cartBounce {' +
      '0%, 100% { transform: scale(1); }' +
      '25% { transform: scale(1.25); }' +
      '50% { transform: scale(0.9); }' +
      '75% { transform: scale(1.1); }' +
    '}' +
    '.cart-bounce { animation: cartBounce 0.5s ease; }' +
    '.fly-to-cart-img {' +
      'position:fixed;z-index:9999;width:50px;height:50px;object-fit:cover;' +
      'border-radius:8px;pointer-events:none;box-shadow:0 4px 14px rgba(0,0,0,0.25);' +
    '}';
  document.head.appendChild(style);
}

/**
 * Animate a product image flying from startEl to the cart icon.
 * @param {string} imgSrc - URL of the product image
 * @param {Element} startEl - DOM element to start the animation from
 */
function flyToCart(imgSrc, startEl) {
  if (!imgSrc || !startEl) return;

  _ensureFlyToCartStyle();

  var startRect = startEl.getBoundingClientRect();
  var cartEl = document.querySelector('.cart-badge') || document.querySelector('a[href="/cart.html"]');
  if (!cartEl) return;
  var cartRect = cartEl.getBoundingClientRect();

  var size = 50;
  var flyImg = document.createElement('img');
  flyImg.src = imgSrc;
  flyImg.className = 'fly-to-cart-img';
  flyImg.style.left = (startRect.left + startRect.width / 2 - size / 2) + 'px';
  flyImg.style.top = (startRect.top + startRect.height / 2 - size / 2) + 'px';
  flyImg.style.transition = 'all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
  document.body.appendChild(flyImg);

  // Force reflow then animate to cart position
  flyImg.offsetHeight;
  flyImg.style.left = (cartRect.left + cartRect.width / 2 - size / 2) + 'px';
  flyImg.style.top = (cartRect.top + cartRect.height / 2 - size / 2) + 'px';
  flyImg.style.transform = 'scale(0.15)';
  flyImg.style.opacity = '0.35';

  // Remove after animation completes
  setTimeout(function() {
    flyImg.remove();
  }, 620);

  // Bounce the cart icon
  cartEl.classList.add('cart-bounce');
  setTimeout(function() {
    cartEl.classList.remove('cart-bounce');
  }, 520);
}
