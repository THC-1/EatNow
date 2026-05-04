import router from '@/router'

const API_BASE = '/api/v1'

class ApiError extends Error {
  constructor(message, status, payload) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.payload = payload
  }
}

async function request(path, options = {}) {
  const { useAuthStore } = await import('@/stores/auth')
  const auth = useAuthStore()
  const headers = new Headers(options.headers || {})

  if (!(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }

  if (auth.token) {
    headers.set('Authorization', `Bearer ${auth.token}`)
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  })

  const text = await response.text()
  const payload = text ? JSON.parse(text) : null

  if (response.status === 401) {
    auth.clearAuth()
    if (router.currentRoute.value.name !== 'login') {
      router.push({ name: 'login' })
    }
  }

  if (!response.ok || (payload && payload.code && payload.code !== 200)) {
    throw new ApiError(payload?.message || '请求失败', response.status, payload)
  }

  return payload?.data ?? null
}

export const api = {
  login: (data) => request('/auth/admin-login', { method: 'POST', body: JSON.stringify(data) }),
  logout: (refreshToken) => request('/auth/logout', { method: 'POST', body: JSON.stringify({ refreshToken }) }),
  overview: () => request('/admin/statistics/overview'),
  popularDishes: (limit = 8) => request(`/admin/statistics/popular-dishes?limit=${limit}`),
  popularMerchants: (limit = 8) => request(`/admin/statistics/popular-merchants?limit=${limit}`),
  userActivity: (params) => request(withQuery('/admin/statistics/user-activity', params)),
  canteenScores: () => request('/admin/statistics/canteen-scores'),
  users: (params) => request(withQuery('/admin/users', params)),
  userDetail: (id) => request(`/admin/users/${id}`),
  disableUser: (id) => request(`/admin/users/${id}/disable`, { method: 'PATCH' }),
  enableUser: (id) => request(`/admin/users/${id}/enable`, { method: 'PATCH' }),
  merchantApplications: (params) => request(withQuery('/admin/merchant-applications', params)),
  approveApplication: (id) => request(`/admin/merchant-applications/${id}/approve`, { method: 'PATCH' }),
  rejectApplication: (id, rejectReason) =>
    request(`/admin/merchant-applications/${id}/reject`, {
      method: 'PATCH',
      body: JSON.stringify({ rejectReason }),
    }),
  canteens: (params) => request(withQuery('/canteens', params)),
  createCanteen: (data) => request('/admin/canteens', { method: 'POST', body: JSON.stringify(data) }),
  updateCanteen: (id, data) => request(`/admin/canteens/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteCanteen: (id) => request(`/admin/canteens/${id}`, { method: 'DELETE' }),
  stalls: (params) => request(withQuery('/stalls', params)),
  createStall: (data) => request('/admin/stalls', { method: 'POST', body: JSON.stringify(data) }),
  updateStall: (id, data) => request(`/admin/stalls/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteStall: (id) => request(`/admin/stalls/${id}`, { method: 'DELETE' }),
  dishes: (params) => request(withQuery('/admin/dishes', params)),
  dishDetail: (id) => request(`/admin/dishes/${id}`),
  updateDishStatus: (id, status) =>
    request(`/admin/dishes/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
  reviews: (params) => request(withQuery('/admin/reviews', params)),
  deleteReview: (id) => request(`/admin/reviews/${id}`, { method: 'DELETE' }),
}

export function withQuery(path, params = {}) {
  const query = new URLSearchParams()
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, value)
    }
  })
  const suffix = query.toString()
  return suffix ? `${path}?${suffix}` : path
}

export { ApiError }
