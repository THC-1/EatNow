<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  BarChart3,
  Building2,
  ChevronLeft,
  ClipboardCheck,
  LogOut,
  Menu,
  MessageSquareText,
  Store,
  Utensils,
  Users,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(false)
const mobileOpen = ref(false)

const navItems = [
  { name: 'dashboard', label: '运营总览', icon: BarChart3 },
  { name: 'users', label: '用户管理', icon: Users },
  { name: 'applications', label: '入驻审核', icon: ClipboardCheck },
  { name: 'canteens', label: '食堂与独立店铺', icon: Building2 },
  { name: 'stalls', label: '窗口管理', icon: Store },
  { name: 'dishes', label: '菜品管理', icon: Utensils },
  { name: 'reviews', label: '评价管理', icon: MessageSquareText },
]

const title = computed(() => route.meta.title || 'EatNow 管理端')

async function logout() {
  await auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="admin-shell" :class="{ 'is-collapsed': collapsed }">
    <aside class="sidebar" :class="{ 'is-open': mobileOpen }">
      <div class="brand">
        <div class="brand-mark">EN</div>
        <div class="brand-copy">
          <strong>EatNow</strong>
          <span>校园餐饮运营台</span>
        </div>
      </div>

      <nav class="side-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          :to="{ name: item.name }"
          class="nav-item"
          @click="mobileOpen = false"
        >
          <component :is="item.icon" :size="18" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <button class="collapse-btn icon-btn" type="button" title="收起侧边栏" @click="collapsed = !collapsed">
        <ChevronLeft :size="18" />
      </button>
    </aside>

    <div class="mobile-shade" :class="{ 'is-visible': mobileOpen }" @click="mobileOpen = false"></div>

    <main class="main-panel">
      <header class="topbar">
        <button class="icon-btn mobile-menu" type="button" title="打开导航" @click="mobileOpen = true">
          <Menu :size="19" />
        </button>
        <div>
          <p class="eyebrow">Admin Console</p>
          <h1>{{ title }}</h1>
        </div>
        <div class="topbar-actions">
          <span class="admin-pill">管理员 #{{ auth.adminId || '-' }}</span>
          <button class="ghost-button" type="button" @click="logout">
            <LogOut :size="16" />
            退出
          </button>
        </div>
      </header>

      <section class="content-stage">
        <RouterView />
      </section>
    </main>
  </div>
</template>
