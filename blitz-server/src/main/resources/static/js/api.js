/**
 * Blitz API Client v2.0 — REST API wrapper
 * Token from AUTH.getToken() (common.js), auto-injected.
 */
const API_BASE = '';

const api = {
  async request(method, path, body) {
    const headers = {};
    if (body && !(body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }
    const token = typeof AUTH !== 'undefined' ? AUTH.getToken() : null;
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const opts = { method, headers };
    if (body) {
      opts.body = body instanceof FormData ? body : JSON.stringify(body);
    }

    const res = await fetch(API_BASE + path, opts);
    const data = await res.json().catch(() => ({}));

    if (data.code !== 0 && data.code !== undefined) {
      if (data.code === 401 || data.code === 403) {
        if (typeof AUTH !== 'undefined') AUTH.clearToken();
      }
      throw new Error(data.msg || 'Request failed');
    }
    return data;
  },

  get(path)          { return this.request('GET', path); },
  post(path, body)   { return this.request('POST', path, body); },
  put(path, body)    { return this.request('PUT', path, body); },
  del(path)          { return this.request('DELETE', path); },

  /* ========= User ========= */
  login(username, password) {
    return this.post('/user/login', { username, password });
  },
  register(username, password, email, phone, inviteCode) {
    return this.post('/user/register', { username, password, email, phone, inviteCode });
  },
  getCurrentUser() {
    return this.get('/user/current');
  },

  /* ========= Product ========= */
  getProducts(params = {}) {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v != null && v !== '') qs.set(k, v); });
    const s = qs.toString();
    return this.get('/product/list' + (s ? '?' + s : ''));
  },
  getProduct(id) {
    return this.get('/product/' + id);
  },
  searchProducts(keyword, params = {}) {
    const qs = new URLSearchParams({ keyword, ...params });
    return this.get('/product/search?' + qs.toString());
  },
  getCategories() {
    return this.get('/product/category/tree');
  },

  /* ========= Order ========= */
  createOrder(orderData) {
    return this.post('/order', orderData);
  },
  getOrders(params = {}) {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v != null && v !== '') qs.set(k, v); });
    const s = qs.toString();
    return this.get('/order/list' + (s ? '?' + s : ''));
  },
  getOrder(id) {
    return this.get('/order/' + id);
  },
  payOrder(orderId) {
    return this.post('/order/pay', { orderId });
  },
  cancelOrder(orderId) {
    return this.post('/order/' + orderId + '/cancel');
  },
  shipOrder(orderId) {
    return this.post('/order/' + orderId + '/ship');
  },

  /* ========= Seckill ========= */
  getSeckillPath(seckillProductId) {
    return this.get('/seckill/path?seckillProductId=' + seckillProductId);
  },
  executeSeckill(seckillProductId, path) {
    return this.post('/seckill/execute', { seckillProductId, path });
  },
  getSeckillResult(seckillProductId) {
    return this.get('/seckill/result?seckillProductId=' + seckillProductId);
  },
  getSeckillList() {
    return this.get('/seckill/list');
  },
  getSeckillActivities() {
    return this.get('/seckill/activity');
  },
  warmupSeckill(activityId) {
    return this.post('/seckill/admin/warmup/' + activityId);
  },

  /* ========= Address ========= */
  getAddresses() {
    return this.get('/user/address');
  },
  addAddress(addr) {
    return this.post('/user/address', addr);
  },
  updateAddress(id, addr) {
    return this.put('/user/address/' + id, addr);
  },
  deleteAddress(id) {
    return this.del('/user/address/' + id);
  },

  /* ========= Refund ========= */
  requestRefund(orderId, reason) {
    return this.post('/refund/request/' + orderId, { reason: reason || '用户申请退款' });
  },

  /* ========= Review / Comment ========= */
  getReviews(productId, params = {}) {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v != null && v !== '') qs.set(k, v); });
    const s = qs.toString();
    return this.get('/review/list/' + productId + (s ? '?' + s : ''));
  },
  submitReview(review) {
    return this.post('/review', review);
  },
  likeReview(reviewId) {
    return this.post('/review/' + reviewId + '/like');
  },
  replyReview(reviewId, reply) {
    return this.post('/review/' + reviewId + '/reply', { reply: reply });
  },
  getReviewStats(productId) {
    return this.get('/review/stats/' + productId);
  },

  /* ========= Shop ========= */
  getShopProducts(shopId, params = {}) {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v != null && v !== '') qs.set(k, v); });
    return this.get('/shop/' + shopId + '/products' + (qs.toString() ? '?' + qs.toString() : ''));
  },
  getShopInfo(shopId) {
    return this.get('/shop/' + shopId + '/info');
  },

  /* ========= Merchant ========= */
  getMerchantDashboard() {
    return this.get('/merchant/dashboard');
  },
  getMerchantProducts() {
    return this.get('/merchant/products');
  },
  createMerchantProduct(data) {
    return this.post('/merchant/products', data);
  },
  updateMerchantProduct(id, data) {
    return this.put('/merchant/products/' + id, data);
  },
  deleteMerchantProduct(id) {
    return this.del('/merchant/products/' + id);
  },
  toggleMerchantProduct(id) {
    return this.post('/merchant/products/' + id + '/toggle');
  },
  getMerchantOrders(params = {}) {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v != null && v !== '') qs.set(k, v); });
    return this.get('/merchant/orders' + (qs.toString() ? '?' + qs.toString() : ''));
  },
  getMerchantShop() {
    return this.get('/merchant/shop');
  },
  updateMerchantShop(data) {
    return this.put('/merchant/shop', data);
  },
  createShop(data) {
    return this.post('/merchant/shop', data);
  },

  /* ========= Admin ========= */
  getAdminStats() {
    return this.get('/admin/stats');
  },
  getAdminUsers(params = {}) {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v != null && v !== '') qs.set(k, v); });
    return this.get('/admin/users' + (qs.toString() ? '?' + qs.toString() : ''));
  },
  updateUserRole(userId, roleId) {
    return this.put('/admin/users/' + userId + '/role', { roleId });
  },
  banUser(userId) {
    return this.post('/admin/users/' + userId + '/ban');
  },
  getAdminOrders(params = {}) {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v != null && v !== '') qs.set(k, v); });
    return this.get('/admin/orders' + (qs.toString() ? '?' + qs.toString() : ''));
  },
  refundOrder(orderId) {
    return this.post('/admin/orders/' + orderId + '/refund');
  },
  getAdminSeckillActivities() {
    return this.get('/seckill/admin/activities');
  },
  createSeckillActivity(data) {
    return this.post('/seckill/admin/activity', data);
  },

  /* ========= Coupon ========= */
  getOfficialCoupons() { return this.get('/coupon/official'); },
  getShopCoupons(shopId) { return this.get('/coupon/shop/' + shopId); },
  claimCoupon(couponId) { return this.post('/coupon/claim/' + couponId); },
  getMyCoupons() { return this.get('/coupon/my'); },
  getAvailableCoupons(amount) { return this.get('/coupon/available?amount=' + (amount||0)); },
  useCoupon(userCouponId) { return this.post('/coupon/use/' + userCouponId); },

  /* ========= Cart (localStorage-based) ========= */
  // Cart operations are local — see CART object in common.js

  /* ========= Health ========= */
  health() {
    return fetch(API_BASE + '/api/health').then(r => r.json().catch(() => ({})));
  }
};
