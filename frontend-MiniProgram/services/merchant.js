import { request, setAuthTokens, uploadImage } from '../utils/request.js'

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

export async function merchantLogin() {
  const code = await wxLoginCode()
  const data = await request({ url: '/api/v1/auth/merchant-login', method: 'POST', data: { code } })
  const token = data.token || data.accessToken
  if (!token) {
    throw new Error('卖家登录接口未返回 token')
  }
  setAuthTokens(data)
  uni.setStorageSync('eatnow_role', 'MERCHANT')
  return data
}

export function fetchMerchantProfile() {
  return request({ url: '/api/v1/merchants/me', auth: true })
}

export function fetchCanteensForApply() {
  return request({ url: '/api/v1/canteens' })
}

export function fetchStallsForApply(canteenId) {
  return request({ url: '/api/v1/stalls', params: { canteenId } })
}

export function applyMerchant(payload) {
  return request({ url: '/api/v1/merchants/apply', method: 'POST', auth: true, data: payload })
}

export function updateMerchantProfile(payload) {
  return request({ url: '/api/v1/merchants/me', method: 'PUT', auth: true, data: payload })
}

export function fetchMerchantOverview() {
  return request({ url: '/api/v1/merchant/statistics/overview', auth: true })
}

export function fetchMerchantPopularDishes(limit = 5) {
  return request({ url: '/api/v1/merchant/statistics/popular-dishes', auth: true, params: { limit } })
}

export function fetchMerchantFeedbackDishes(limit = 5) {
  return request({ url: '/api/v1/merchant/statistics/most-feedback-dishes', auth: true, params: { limit } })
}

export async function fetchMerchantDishes(params = {}) {
  const finalParams = { ...params }
  if (!finalParams.merchantId) {
    const profile = await fetchMerchantProfile()
    finalParams.merchantId = profile.id
  }
  return request({ url: '/api/v1/dishes', auth: true, params: finalParams })
}

export function createDish(payload) {
  return request({ url: '/api/v1/dishes', method: 'POST', auth: true, data: payload })
}

export function updateDish(id, payload) {
  return request({ url: `/api/v1/dishes/${id}`, method: 'PUT', auth: true, data: payload })
}

export function updateDishStatus(id, status) {
  return request({ url: `/api/v1/dishes/${id}/status`, method: 'PATCH', auth: true, data: { status } })
}

export function deleteDish(id) {
  return request({ url: `/api/v1/dishes/${id}`, method: 'DELETE', auth: true })
}

export function fetchCategories() {
  return request({ url: '/api/v1/categories', params: { type: 'DISH' } })
}

export function fetchTags() {
  return request({ url: '/api/v1/tags', params: { type: 'TASTE' } })
}

export function fetchMerchantRecommendations(params = {}) {
  return request({ url: '/api/v1/merchant-recommendations', auth: true, params })
}

export function createRecommendation(payload) {
  return request({ url: '/api/v1/merchant-recommendations', method: 'POST', auth: true, data: payload })
}

export function updateRecommendation(id, payload) {
  return request({ url: `/api/v1/merchant-recommendations/${id}`, method: 'PUT', auth: true, data: payload })
}

export function deleteRecommendation(id) {
  return request({ url: `/api/v1/merchant-recommendations/${id}`, method: 'DELETE', auth: true })
}

export function fetchMerchantFeedback(params = {}) {
  return request({ url: '/api/v1/feedbacks', auth: true, params })
}

export function replyFeedback(id, replyContent) {
  return request({ url: `/api/v1/feedbacks/${id}/reply`, method: 'POST', auth: true, data: { replyContent } })
}

export function updateFeedbackStatus(id, status) {
  return request({ url: `/api/v1/feedbacks/${id}/status`, method: 'PATCH', auth: true, data: { status } })
}

export function fetchImprovementRecords(params = {}) {
  return request({ url: '/api/v1/merchant/improvement-records', auth: true, params })
}

export function createImprovementRecord(payload) {
  return request({ url: '/api/v1/merchant/improvement-records', method: 'POST', auth: true, data: payload })
}

export function uploadMerchantImage(filePath, type = 'dish') {
  return uploadImage(filePath, type)
}
