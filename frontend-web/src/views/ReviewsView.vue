<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Image, RotateCcw, Search, Trash2 } from 'lucide-vue-next'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import LoadingState from '@/components/LoadingState.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import PageShell from '@/components/PageShell.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api } from '@/services/api'
import { formatDateTime, formatScore } from '@/utils/format'

const filters = reactive({
  targetType: '',
  status: '',
  keyword: '',
})
const page = ref(1)
const size = 10
const total = ref(0)
const records = ref([])
const loading = ref(false)
const error = ref('')
const confirm = reactive({ open: false, item: null })
const images = reactive({ open: false, list: [] })

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await api.reviews({ ...filters, page: page.value, size })
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
  filters.targetType = ''
  filters.status = ''
  filters.keyword = ''
  search()
}

async function remove() {
  await api.deleteReview(confirm.item.id)
  confirm.open = false
  await load()
}

function openImages(list) {
  images.list = list || []
  images.open = true
}

onMounted(load)
</script>

<template>
  <PageShell title="评价管理" description="集中巡检平台评价内容，删除违规评价。">
    <form class="filter-bar" @submit.prevent="search">
      <select v-model="filters.targetType">
        <option value="">全部目标</option>
        <option value="DISH">菜品</option>
      </select>
      <select v-model="filters.status">
        <option value="">全部状态</option>
        <option value="VISIBLE">可见</option>
        <option value="HIDDEN">隐藏</option>
        <option value="DELETED">已删除</option>
      </select>
      <input v-model="filters.keyword" placeholder="搜索评价内容 / 菜品" />
      <button class="primary-button" type="submit"><Search :size="16" />搜索</button>
      <button class="secondary-button" type="button" @click="reset"><RotateCcw :size="16" />重置</button>
    </form>

    <LoadingState :loading="loading" :error="error" :empty="!records.length">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>评价</th>
              <th>目标</th>
              <th>评分</th>
              <th>状态</th>
              <th>时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.id">
              <td class="review-content">
                <strong>{{ item.isAnonymous ? '匿名用户' : item.userNickname || `用户 ${item.userId}` }}</strong>
                <p>{{ item.content || '无文字评价' }}</p>
              </td>
              <td>
                <StatusBadge group="target" :value="item.targetType" />
                <small>{{ item.dishName || '-' }} · {{ item.merchantName || '-' }}</small>
              </td>
              <td>{{ formatScore(item.overallScore) }} · {{ item.likeCount || 0 }} 赞</td>
              <td><StatusBadge group="review" :value="item.status" /></td>
              <td>{{ formatDateTime(item.createdAt) }}</td>
              <td>
                <div class="row-actions">
                  <button class="text-button" type="button" :disabled="!item.images?.length" @click="openImages(item.images)">
                    <Image :size="15" />图片
                  </button>
                  <button class="text-danger" type="button" @click="confirm.open = true; confirm.item = item">
                    <Trash2 :size="15" />删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar v-model:page="page" :size="size" :total="total" @update:page="load" />
    </LoadingState>

    <ModalDialog :open="images.open" title="评价图片" wide @close="images.open = false">
      <div class="image-gallery">
        <img v-for="image in images.list" :key="image" :src="image" alt="" />
      </div>
    </ModalDialog>

    <ConfirmDialog
      :open="confirm.open"
      danger
      title="删除评价"
      :message="`确认删除这条来自 ${confirm.item?.userNickname || '用户'} 的评价？`"
      @close="confirm.open = false"
      @confirm="remove"
    />
  </PageShell>
</template>
