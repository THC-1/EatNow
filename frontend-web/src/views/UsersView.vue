<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Eye, RotateCcw, Search } from 'lucide-vue-next'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LoadingState from '@/components/LoadingState.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import PageShell from '@/components/PageShell.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api } from '@/services/api'
import { formatDateTime } from '@/utils/format'

const filters = reactive({
  role: '',
  status: '',
  keyword: '',
})
const page = ref(1)
const size = 10
const total = ref(0)
const records = ref([])
const loading = ref(false)
const error = ref('')
const detailOpen = ref(false)
const detail = ref(null)
const confirm = reactive({
  open: false,
  user: null,
  action: '',
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await api.users({ ...filters, page: page.value, size })
    records.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function reset() {
  filters.role = ''
  filters.status = ''
  filters.keyword = ''
  search()
}

async function openDetail(user) {
  detail.value = await api.userDetail(user.id)
  detailOpen.value = true
}

function askStatus(user, action) {
  confirm.open = true
  confirm.user = user
  confirm.action = action
}

async function confirmStatus() {
  if (confirm.action === 'disable') {
    await api.disableUser(confirm.user.id)
  } else {
    await api.enableUser(confirm.user.id)
  }
  confirm.open = false
  await load()
}

onMounted(load)
</script>

<template>
  <PageShell title="用户管理" description="检索学生与商家账号，查看档案并处理违规账号。">
    <form class="filter-bar" @submit.prevent="search">
      <select v-model="filters.role">
        <option value="">全部角色</option>
        <option value="STUDENT">学生</option>
        <option value="MERCHANT">商家</option>
      </select>
      <select v-model="filters.status">
        <option value="">全部状态</option>
        <option value="ACTIVE">正常</option>
        <option value="DISABLED">禁用</option>
      </select>
      <input v-model="filters.keyword" placeholder="搜索用户名或昵称" />
      <button class="primary-button" type="submit"><Search :size="16" />搜索</button>
      <button class="secondary-button" type="button" @click="reset"><RotateCcw :size="16" />重置</button>
    </form>

    <LoadingState :loading="loading" :error="error" :empty="!records.length">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>用户</th>
              <th>角色</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.id">
              <td>
                <div class="identity-cell">
                  <img :src="item.avatar || '/favicon.ico'" alt="" />
                  <div>
                    <strong>{{ item.nickname || item.username || `用户 ${item.id}` }}</strong>
                    <span>{{ item.username || '-' }}</span>
                  </div>
                </div>
              </td>
              <td>{{ item.role === 'MERCHANT' ? '商家' : '学生' }}</td>
              <td><StatusBadge group="user" :value="item.status" /></td>
              <td>{{ formatDateTime(item.createdAt) }}</td>
              <td>
                <div class="row-actions">
                  <button class="text-button" type="button" @click="openDetail(item)"><Eye :size="15" />详情</button>
                  <button
                    v-if="item.status !== 'DISABLED'"
                    class="text-danger"
                    type="button"
                    @click="askStatus(item, 'disable')"
                  >
                    禁用
                  </button>
                  <button v-else class="text-button" type="button" @click="askStatus(item, 'enable')">恢复</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar v-model:page="page" :size="size" :total="total" @update:page="load" />
    </LoadingState>

    <ModalDialog :open="detailOpen" title="用户详情" wide @close="detailOpen = false">
      <div v-if="detail" class="detail-grid">
        <div><span>ID</span><strong>{{ detail.id }}</strong></div>
        <div><span>用户名</span><strong>{{ detail.username || '-' }}</strong></div>
        <div><span>昵称</span><strong>{{ detail.nickname || '-' }}</strong></div>
        <div><span>手机号</span><strong>{{ detail.phone || '-' }}</strong></div>
        <div><span>状态</span><StatusBadge group="user" :value="detail.status" /></div>
        <div><span>评价数</span><strong>{{ detail.reviewCount || 0 }}</strong></div>
        <div><span>角色</span><strong>{{ (detail.roles || []).join(' / ') || '-' }}</strong></div>
        <div><span>最近登录</span><strong>{{ formatDateTime(detail.lastLoginAt) }}</strong></div>
      </div>
      <div v-if="detail?.studentProfile" class="sub-panel">
        <h4>学生档案</h4>
        <p>学号：{{ detail.studentProfile.studentNo || '-' }} · 专业：{{ detail.studentProfile.major || '-' }}</p>
      </div>
      <div v-if="detail?.merchantProfile" class="sub-panel">
        <h4>商家档案</h4>
        <p>
          {{ detail.merchantProfile.merchantName || '-' }} · {{ detail.merchantProfile.canteenName || '-' }} /
          {{ detail.merchantProfile.stallName || '-' }}
        </p>
      </div>
    </ModalDialog>

    <ConfirmDialog
      :open="confirm.open"
      :danger="confirm.action === 'disable'"
      :title="confirm.action === 'disable' ? '禁用用户' : '恢复用户'"
      :message="`确认${confirm.action === 'disable' ? '禁用' : '恢复'} ${confirm.user?.nickname || confirm.user?.username || ''}？`"
      @close="confirm.open = false"
      @confirm="confirmStatus"
    />
  </PageShell>
</template>
