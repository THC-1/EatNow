import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AdminLayout from '@/layouts/AdminLayout.vue'
import ApplicationsView from '@/views/ApplicationsView.vue'
import CanteensView from '@/views/CanteensView.vue'
import DashboardView from '@/views/DashboardView.vue'
import DishesView from '@/views/DishesView.vue'
import LoginView from '@/views/LoginView.vue'
import ReviewsView from '@/views/ReviewsView.vue'
import StallsView from '@/views/StallsView.vue'
import UsersView from '@/views/UsersView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true },
    },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { title: '运营总览' } },
        { path: 'users', name: 'users', component: UsersView, meta: { title: '用户管理' } },
        { path: 'applications', name: 'applications', component: ApplicationsView, meta: { title: '入驻审核' } },
        { path: 'canteens', name: 'canteens', component: CanteensView, meta: { title: '食堂与独立店铺管理' } },
        { path: 'stalls', name: 'stalls', component: StallsView, meta: { title: '窗口管理' } },
        { path: 'dishes', name: 'dishes', component: DishesView, meta: { title: '菜品管理' } },
        { path: 'reviews', name: 'reviews', component: ReviewsView, meta: { title: '评价管理' } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isAuthenticated) {
    return { name: 'login' }
  }
  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
