import { clearAuth, getRefreshToken, request, setAuthTokens, uploadImage } from '../utils/request.js'

function wxLoginCode() {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success(result) {
        if (result.code) {
          resolve(result.code)
          return
        }
        reject(new Error('微信登录未返回 code'))
      },
      fail(error) {
        reject(error)
      }
    })
  })
}

export async function studentLogin() {
  const code = await wxLoginCode()
  const data = await request({ url: '/api/v1/auth/student-login', method: 'POST', data: { code } })
  const token = data.token || data.accessToken
  if (!token) {
    throw new Error('学生登录接口未返回 token')
  }
  setAuthTokens(data)
  uni.setStorageSync('eatnow_role', 'STUDENT')
  return data
}

export function fetchProfile() {
  return request({ url: '/api/v1/users/me', auth: true })
}

export function fetchPreferences() {
  return request({ url: '/api/v1/users/preferences', auth: true })
}

export function updateProfile(payload) {
  return request({ url: '/api/v1/users/me', method: 'PUT', auth: true, data: payload })
}

export function updatePreferences(payload) {
  return request({ url: '/api/v1/users/preferences', method: 'PUT', auth: true, data: payload })
}

export function fetchCanteens(params = {}) {
  return request({ url: '/api/v1/canteens', params })
}

export function fetchStalls(canteenId) {
  return request({ url: '/api/v1/stalls', params: { canteenId } })
}

export function fetchDishes(params = {}) {
  return request({ url: '/api/v1/dishes', params })
}

export function fetchDishDetail(id) {
  return request({ url: `/api/v1/dishes/${id}` })
}

export function fetchReviews(targetId) {
  return request({ url: '/api/v1/reviews', params: { targetType: 'DISH', targetId, page: 1, size: 10 } })
}

export function createFavorite(targetId) {
  return request({ url: '/api/v1/favorites', method: 'POST', auth: true, data: { targetType: 'DISH', targetId } })
}

export function deleteFavorite(id) {
  return request({ url: `/api/v1/favorites/${id}`, method: 'DELETE', auth: true })
}

export function checkFavorite(targetId) {
  return request({ url: '/api/v1/favorites/check', auth: true, params: { targetType: 'DISH', targetId } })
}

export function createEatList(dishId, sourceLotteryRecordId = null) {
  return request({ url: '/api/v1/eat-list', method: 'POST', auth: true, data: { dishId, sourceLotteryRecordId, note: '' } })
}

export function markEatListEaten(id, note = '') {
  return request({ url: `/api/v1/eat-list/${id}/eaten`, method: 'PATCH', auth: true, data: { note } })
}

export function deleteEatList(id) {
  return request({ url: `/api/v1/eat-list/${id}`, method: 'DELETE', auth: true })
}

export function createReview(payload) {
  return request({ url: '/api/v1/reviews', method: 'POST', auth: true, data: payload })
}

export function createFeedback(payload) {
  return request({ url: '/api/v1/feedbacks', method: 'POST', auth: true, data: payload })
}

export function drawLottery(mode = 'RANDOM', condition = {}) {
  const map = {
    RANDOM: '/api/v1/lottery/draw',
    CONDITION: '/api/v1/lottery/draw-with-condition',
    FAVORITE: '/api/v1/lottery/draw-from-favorites'
  }
  return request({ url: map[mode], method: 'POST', auth: true, data: { drawMode: mode, ...condition } })
}

export function recordLotteryAction(recordId, resultAction) {
  return request({ url: `/api/v1/lottery/records/${recordId}/action`, method: 'POST', auth: true, data: { resultAction } })
}

export function fetchLotteryRecords() {
  return request({ url: '/api/v1/lottery/records', auth: true, params: { page: 1, size: 10 } })
}

export function fetchFavorites(params = {}) {
  return request({ url: '/api/v1/favorites', auth: true, params: { targetType: 'DISH', page: 1, size: 10, ...params } })
}

export function fetchEatList(params = {}) {
  return request({ url: '/api/v1/eat-list', auth: true, params: { status: 'WANT_TO_EAT', page: 1, size: 10, ...params } })
}

export function fetchRanking(type = 'top-rated') {
  return request({ url: `/api/v1/rankings/${type}`, params: { limit: 6 } })
}

export function uploadStudentImage(filePath, type = 'review') {
  return uploadImage(filePath, type)
}

export async function logout() {
  const refreshToken = getRefreshToken()
  try {
    await request({ url: '/api/v1/auth/logout', method: 'POST', auth: true, data: { refreshToken } })
  } finally {
    clearAuth()
  }
}
