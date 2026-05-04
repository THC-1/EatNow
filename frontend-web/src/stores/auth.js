import { defineStore } from 'pinia'
import { api } from '@/services/api'

const STORAGE_KEY = 'eatnow_admin_auth'

function loadStoredAuth() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {}
  } catch {
    return {}
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    ...loadStoredAuth(),
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    persist(payload) {
      Object.assign(this, payload)
      localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
    },
    async login(credentials) {
      const data = await api.login(credentials)
      this.persist(data)
      return data
    },
    async logout() {
      const refreshToken = this.refreshToken
      if (refreshToken) {
        try {
          await api.logout(refreshToken)
        } catch {
          // Local logout should still complete when the token is already invalid.
        }
      }
      this.clearAuth()
    },
    clearAuth() {
      this.$patch({
        token: null,
        refreshToken: null,
        tokenExpiresAt: null,
        refreshTokenExpiresAt: null,
        adminId: null,
      })
      localStorage.removeItem(STORAGE_KEY)
    },
  },
})
