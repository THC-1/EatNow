<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { LockKeyhole, ShieldCheck, UserRound } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({
  username: '',
  password: '',
})
const loading = ref(false)
const error = ref('')
const canSubmit = computed(() => form.username.trim() && form.password)

async function submit() {
  if (!canSubmit.value || loading.value) return
  loading.value = true
  error.value = ''
  try {
    await auth.login({
      username: form.username.trim(),
      password: form.password,
    })
    router.push({ name: 'dashboard' })
  } catch (exception) {
    error.value = exception.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-screen">
    <section class="login-visual">
      <div class="login-brand">
        <span>EatNow</span>
        <strong>校园餐饮运营中枢</strong>
      </div>
      <div class="orbit-board">
        <div class="orbit-ring"></div>
        <div class="metric-chip chip-a">今日活跃</div>
        <div class="metric-chip chip-b">入驻审核</div>
        <div class="metric-chip chip-c">菜品监控</div>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card">
        <div class="login-title">
          <ShieldCheck :size="26" />
          <div>
            <p class="eyebrow">Admin Access</p>
            <h1>管理员登录</h1>
          </div>
        </div>

        <form class="login-form" @submit.prevent="submit">
          <label>
            <span>账号</span>
            <div class="input-shell">
              <UserRound :size="18" />
              <input v-model="form.username" autocomplete="username" placeholder="请输入管理员账号" />
            </div>
          </label>
          <label>
            <span>密码</span>
            <div class="input-shell">
              <LockKeyhole :size="18" />
              <input v-model="form.password" autocomplete="current-password" placeholder="请输入密码" type="password" />
            </div>
          </label>

          <p v-if="error" class="form-error">{{ error }}</p>
          <button class="primary-button full-width" type="submit" :disabled="!canSubmit || loading">
            {{ loading ? '登录中...' : '进入管理端' }}
          </button>
        </form>
      </div>
    </section>
  </main>
</template>
