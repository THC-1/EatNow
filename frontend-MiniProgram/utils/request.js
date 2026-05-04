const BASE_URL = 'http://10.207.122.19:8080'
const OLD_PLACEHOLDER_TOKENS = [`mo${'ck'}-token`, `mo${'ck'}-merchant-token`]
let refreshingPromise = null

function buildUrl(path, params = {}) {
  const query = Object.keys(params)
    .filter((key) => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  return `${BASE_URL}${path}${query ? `?${query}` : ''}`
}

export function getToken() {
  const token = uni.getStorageSync('eatnow_token') || ''
  if (OLD_PLACEHOLDER_TOKENS.includes(token)) {
    clearAuth()
    return ''
  }
  return token
}

export function hasToken() {
  return !!getToken()
}

export function setToken(token) {
  if (token) {
    uni.setStorageSync('eatnow_token', token)
    return
  }
  uni.removeStorageSync('eatnow_token')
}

export function getRefreshToken() {
  return uni.getStorageSync('eatnow_refresh_token') || ''
}

export function setRefreshToken(token) {
  if (token) {
    uni.setStorageSync('eatnow_refresh_token', token)
    return
  }
  uni.removeStorageSync('eatnow_refresh_token')
}

export function setAuthTokens(data = {}) {
  setToken(data.token || data.accessToken)
  setRefreshToken(data.refreshToken)
}

export function clearAuth() {
  uni.removeStorageSync('eatnow_token')
  uni.removeStorageSync('eatnow_refresh_token')
  uni.removeStorageSync('eatnow_role')
}

function parseResponseBody(raw) {
  if (typeof raw !== 'string') {
    return raw || {}
  }
  try {
    return JSON.parse(raw)
  } catch (error) {
    return {}
  }
}

function refreshToken() {
  const refreshTokenValue = getRefreshToken()
  if (!refreshTokenValue) {
    return Promise.reject(new Error('登录已过期，请重新登录'))
  }
  if (!refreshingPromise) {
    refreshingPromise = request({
      url: '/api/v1/auth/refresh',
      method: 'POST',
      data: { refreshToken: refreshTokenValue },
      skipRefresh: true
    })
      .then((data) => {
        setAuthTokens(data)
        return data
      })
      .finally(() => {
        refreshingPromise = null
      })
  }
  return refreshingPromise
}

export function request({ url, method = 'GET', data = {}, params = {}, auth = false, skipRefresh = false }) {
  const header = {
    'content-type': 'application/json'
  }
  const token = getToken()
  if (auth && token) {
    header.Authorization = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: buildUrl(url, params),
      method,
      data,
      header,
      success(res) {
        const body = res.data || {}
        if (res.statusCode >= 200 && res.statusCode < 300 && (body.code === 200 || body.code === undefined)) {
          resolve(body.data === undefined ? body : body.data)
          return
        }
        if (res.statusCode === 401 && auth && !skipRefresh) {
          refreshToken()
            .then(() => request({ url, method, data, params, auth, skipRefresh: true }))
            .then(resolve)
            .catch((error) => {
              clearAuth()
              reject(error)
            })
          return
        }
        if (res.statusCode === 401) {
          clearAuth()
        }
        reject(new Error(`${method} ${url} ${res.statusCode}: ${body.message || '接口请求失败'}`))
      },
      fail(error) {
        reject(new Error(`${method} ${url}: ${error.errMsg || error.message || '网络请求失败'}`))
      }
    })
  })
}

export function uploadImage(filePath, type = 'dish', skipRefresh = false) {
  const header = {}
  const token = getToken()
  if (token) {
    header.Authorization = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: buildUrl('/api/v1/files/upload-image', { type }),
      filePath,
      name: 'file',
      header,
      success(res) {
        const body = parseResponseBody(res.data)
        if (res.statusCode >= 200 && res.statusCode < 300 && (body.code === 200 || body.code === undefined)) {
          resolve(body.data === undefined ? body : body.data)
          return
        }
        if (res.statusCode === 401 && !skipRefresh) {
          refreshToken()
            .then(() => uploadImage(filePath, type, true))
            .then(resolve)
            .catch((error) => {
              clearAuth()
              reject(error)
            })
          return
        }
        if (res.statusCode === 401) {
          clearAuth()
        }
        reject(new Error(`POST /api/v1/files/upload-image ${res.statusCode}: ${body.message || '接口请求失败'}`))
      },
      fail(error) {
        reject(new Error(`POST /api/v1/files/upload-image: ${error.errMsg || error.message || '网络请求失败'}`))
      }
    })
  })
}
