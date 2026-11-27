const BASE = ''

async function request(url, options = {}) {
  const response = await fetch(BASE + url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  const body = await response.json().catch(() => null)
  if (!body || body.code !== 200) {
    throw new Error((body && body.message) || `请求失败（${response.status}）`)
  }
  return body.data
}

function post(url, data) {
  return request(url, { method: 'POST', body: JSON.stringify(data || {}) })
}

function put(url, data) {
  return request(url, { method: 'PUT', body: JSON.stringify(data || {}) })
}

function del(url) {
  return request(url, { method: 'DELETE' })
}

export const api = {
  login: (username, password) => post('/api/auth/login', { username, password }),
  register: (payload) => post('/api/auth/register', payload),

  users: () => request('/api/users'),

  windows: () => request('/api/windows'),
  windowsByMerchant: (merchantId) => request(`/api/windows/merchant?merchantId=${merchantId}`),
  createWindow: (payload) => post('/api/windows', payload),
  updateWindow: (id, payload) => put(`/api/windows/${id}`, payload),
  deleteWindow: (id) => del(`/api/windows/${id}`),

  categories: () => request('/api/categories'),
  createCategory: (name) => post('/api/categories', { name }),
  deleteCategory: (id) => del(`/api/categories/${id}`),

  dishes: (params = {}) => {
    const query = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined) query.set(key, value)
    })
    return request(`/api/dishes?${query.toString()}`)
  },
  dish: (id) => request(`/api/dishes/${id}`),
  createDish: (payload) => post('/api/dishes', payload),
  updateDish: (id, payload) => put(`/api/dishes/${id}`, payload),
  deleteDish: (id) => del(`/api/dishes/${id}`),
  changeStock: (id, delta) => post(`/api/dishes/${id}/stock?delta=${delta}`),
  topSales: (limit = 10) => request(`/api/dishes/rank/top?limit=${limit}`),

  cart: (userId) => request(`/api/cart?userId=${userId}`),
  addToCart: (userId, dishId, quantity = 1) => post('/api/cart', { userId, dishId, quantity }),
  updateCart: (id, quantity) => put(`/api/cart/${id}?quantity=${quantity}`),
  removeCart: (id) => del(`/api/cart/${id}`),
  clearCart: (userId) => del(`/api/cart?userId=${userId}`),
  cartTotal: (userId) => request(`/api/cart/total?userId=${userId}`),

  createOrder: (userId, pickupTime, remark) => post('/api/orders', { userId, pickupTime, remark }),
  payOrder: (id) => post(`/api/orders/${id}/pay`),
  cancelOrder: (id) => post(`/api/orders/${id}/cancel`),
  verifyOrder: (id, pickupCode) => post(`/api/orders/${id}/verify`, { pickupCode }),
  myOrders: (userId) => request(`/api/orders/my?userId=${userId}`),
  orders: (role, userId) => {
    const query = new URLSearchParams()
    if (role) query.set('role', role)
    if (userId) query.set('userId', userId)
    return request(`/api/orders?${query.toString()}`)
  },
  orderDetail: (id) => request(`/api/orders/${id}`),

  stats: () => request('/api/stats/overview'),
  exportCsv: () => request('/api/stats/export'),
}

export function statusText(status) {
  return {
    PENDING_PAY: '待支付',
    PAID: '待取餐',
    PICKED: '已完成',
    CANCELLED: '已取消',
  }[status] || status
}
