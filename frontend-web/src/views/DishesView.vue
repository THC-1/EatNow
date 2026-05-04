<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Eye, RotateCcw, Search } from 'lucide-vue-next'
import LoadingState from '@/components/LoadingState.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import PageShell from '@/components/PageShell.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api } from '@/services/api'
import { formatDateTime, formatMoney, formatScore } from '@/utils/format'

const filters = reactive({
  merchantId: '',
  canteenId: '',
  stallId: '',
  status: '',
  keyword: '',
})
const canteens = ref([])
const stalls = ref([])
const page = ref(1)
const size = 10
const total = ref(0)
const records = ref([])
const loading = ref(false)
const error = ref('')
const detailOpen = ref(false)
const detail = ref(null)

async function loadOptions() {
  canteens.value = await api.canteens({})
}

async function loadStalls() {
  stalls.value = filters.canteenId ? await api.stalls({ canteenId: filters.canteenId }) : []
  if (!stalls.value.some((item) => String(item.id) === String(filters.stallId))) {
    filters.stallId = ''
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await api.dishes({ ...filters, page: page.value, size })
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

async function reset() {
  filters.merchantId = ''
  filters.canteenId = ''
  filters.stallId = ''
  filters.status = ''
  filters.keyword = ''
  stalls.value = []
  search()
}

async function openDetail(item) {
  detail.value = await api.dishDetail(item.id)
  detailOpen.value = true
}

async function changeStatus(item, event) {
  const previous = item.status
  const next = event.target.value
  item.status = next
  try {
    await api.updateDishStatus(item.id, next)
  } catch (exception) {
    item.status = previous
    error.value = exception.message
  }
}

onMounted(async () => {
  await loadOptions()
  await load()
})
</script>

<template>
  <PageShell title="菜品管理" description="从平台视角筛选菜品、查看详情并调整上架状态。">
    <form class="filter-bar" @submit.prevent="search">
      <select v-model="filters.canteenId" @change="loadStalls">
        <option value="">全部食堂/店铺</option>
        <option v-for="item in canteens" :key="item.id" :value="item.id">{{ item.name }}</option>
      </select>
      <select v-model="filters.stallId">
        <option value="">全部店铺</option>
        <option v-for="item in stalls" :key="item.id" :value="item.id">{{ item.name }}</option>
      </select>
      <select v-model="filters.status">
        <option value="">全部状态</option>
        <option value="ON_SALE">上架中</option>
        <option value="SOLD_OUT">已售罄</option>
        <option value="OFF_SHELF">已下架</option>
        <option value="PENDING">待审核</option>
      </select>
      <input v-model="filters.merchantId" inputmode="numeric" placeholder="商家 ID" />
      <input v-model="filters.keyword" placeholder="搜索菜品名称" />
      <button class="primary-button" type="submit"><Search :size="16" />搜索</button>
      <button class="secondary-button" type="button" @click="reset"><RotateCcw :size="16" />重置</button>
    </form>

    <LoadingState :loading="loading" :error="error" :empty="!records.length">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>菜品</th>
              <th>商家 / 位置</th>
              <th>价格</th>
              <th>评分</th>
              <th>状态</th>
              <th>数据</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in records" :key="item.id">
              <td>
                <div class="dish-cell">
                  <img :src="item.coverImageUrl || '/favicon.ico'" alt="" />
                  <div>
                    <strong>{{ item.name }}</strong>
                    <small>{{ item.categoryName || '未分类' }} · {{ formatDateTime(item.createdAt) }}</small>
                  </div>
                </div>
              </td>
              <td>
                <strong>{{ item.merchantName || '-' }}</strong>
                <small>{{ item.canteenName || '-' }} / {{ item.stallName || '-' }}</small>
              </td>
              <td>{{ formatMoney(item.price) }}</td>
              <td>{{ formatScore(item.averageScore) }}</td>
              <td>
                <select class="status-select" :value="item.status" @change="changeStatus(item, $event)">
                  <option value="ON_SALE">上架中</option>
                  <option value="SOLD_OUT">已售罄</option>
                  <option value="OFF_SHELF">已下架</option>
                  <option value="PENDING">待审核</option>
                </select>
              </td>
              <td>{{ item.viewCount || 0 }} 浏览 · {{ item.reviewCount || 0 }} 评</td>
              <td>
                <button class="text-button" type="button" @click="openDetail(item)"><Eye :size="15" />详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <PaginationBar v-model:page="page" :size="size" :total="total" @update:page="load" />
    </LoadingState>

    <ModalDialog :open="detailOpen" title="菜品详情" wide @close="detailOpen = false">
      <div v-if="detail" class="dish-detail">
        <div class="image-strip">
          <img v-for="image in detail.images || []" :key="image" :src="image" alt="" />
          <div v-if="!detail.images?.length" class="image-empty">暂无图片</div>
        </div>
        <div class="detail-grid">
          <div><span>名称</span><strong>{{ detail.name }}</strong></div>
          <div><span>价格</span><strong>{{ formatMoney(detail.price) }}</strong></div>
          <div><span>状态</span><StatusBadge group="dish" :value="detail.status" /></div>
          <div><span>抽签池</span><strong>{{ detail.isJoinLottery ? '参与' : '不参与' }}</strong></div>
          <div><span>口味</span><strong>{{ formatScore(detail.tasteScore) }}</strong></div>
          <div><span>分量</span><strong>{{ formatScore(detail.portionScore) }}</strong></div>
          <div><span>性价比</span><strong>{{ formatScore(detail.valueScore) }}</strong></div>
          <div><span>商家状态</span><StatusBadge group="business" :value="detail.merchantStatus" /></div>
        </div>
        <div class="sub-panel">
          <h4>描述</h4>
          <p>{{ detail.description || '暂无描述' }}</p>
        </div>
        <div class="tag-row">
          <span v-for="tag in detail.tags || []" :key="tag.id">{{ tag.name }}</span>
        </div>
      </div>
    </ModalDialog>
  </PageShell>
</template>
