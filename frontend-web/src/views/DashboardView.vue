<script setup>
import { onMounted, ref } from 'vue'
import { RefreshCw } from 'lucide-vue-next'
import LoadingState from '@/components/LoadingState.vue'
import PageShell from '@/components/PageShell.vue'
import StatCard from '@/components/StatCard.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import { api } from '@/services/api'
import { formatNumber, formatScore } from '@/utils/format'

const loading = ref(false)
const error = ref('')
const overview = ref({})
const dishes = ref([])
const merchants = ref([])
const activity = ref([])
const scores = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [overviewData, dishData, merchantData, activityData, scoreData] = await Promise.all([
      api.overview(),
      api.popularDishes(8),
      api.popularMerchants(8),
      api.userActivity({}),
      api.canteenScores(),
    ])
    overview.value = overviewData || {}
    dishes.value = dishData || []
    merchants.value = merchantData || []
    activity.value = activityData || []
    scores.value = scoreData || []
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

function barWidth(value, list, field) {
  const max = Math.max(1, ...list.map((item) => Number(item[field] || 0)))
  return `${Math.max(6, (Number(value || 0) / max) * 100)}%`
}

onMounted(load)
</script>

<template>
  <PageShell title="运营总览" description="平台关键指标、活跃趋势与热门内容集中看板。">
    <template #actions>
      <button class="secondary-button" type="button" @click="load">
        <RefreshCw :size="16" />
        刷新
      </button>
    </template>

    <LoadingState :loading="loading" :error="error">
      <div class="stats-grid">
        <StatCard label="学生数" :value="formatNumber(overview.studentCount)" hint="注册学生用户" />
        <StatCard label="商家数" :value="formatNumber(overview.merchantCount)" hint="平台商家规模" />
        <StatCard label="菜品数" :value="formatNumber(overview.dishCount)" hint="可运营菜品池" />
        <StatCard label="评价数" :value="formatNumber(overview.reviewCount)" hint="累计评价反馈" />
        <StatCard label="今日抽签" :value="formatNumber(overview.todayLotteryCount)" hint="今日推荐互动" />
        <StatCard label="今日活跃" :value="formatNumber(overview.todayActiveUsers)" hint="今日活跃用户" />
      </div>

      <div class="dashboard-grid">
        <section class="panel-card span-2">
          <div class="panel-title">
            <h3>用户活跃趋势</h3>
            <span>近 7 日</span>
          </div>
          <div class="mini-chart">
            <div v-for="item in activity" :key="item.date" class="chart-row">
              <span>{{ item.date }}</span>
              <div class="chart-track">
                <i :style="{ width: barWidth(item.activeUsers, activity, 'activeUsers') }"></i>
              </div>
              <strong>{{ formatNumber(item.activeUsers) }}</strong>
            </div>
          </div>
        </section>

        <section class="panel-card">
          <div class="panel-title">
            <h3>食堂/店铺评分</h3>
            <span>{{ scores.length }} 个区域</span>
          </div>
          <div class="score-list">
            <article v-for="item in scores" :key="item.canteenId">
              <div>
                <strong>{{ item.canteenName }}</strong>
                <StatusBadge group="canteenType" :value="item.canteenType" />
              </div>
              <span>{{ formatScore(item.averageScore) }} 分 · {{ item.reviewCount || 0 }} 评</span>
            </article>
          </div>
        </section>

        <section class="panel-card">
          <div class="panel-title">
            <h3>热门菜品</h3>
            <span>按热度</span>
          </div>
          <div class="rank-list">
            <article v-for="(item, index) in dishes" :key="item.dishId">
              <b>{{ index + 1 }}</b>
              <div>
                <strong>{{ item.dishName }}</strong>
                <span>{{ item.merchantName }} · {{ formatScore(item.averageScore) }} 分</span>
              </div>
              <em>{{ item.viewCount || 0 }}</em>
            </article>
          </div>
        </section>

        <section class="panel-card span-2">
          <div class="panel-title">
            <h3>热门商家</h3>
            <span>曝光与评价综合</span>
          </div>
          <div class="merchant-grid">
            <article v-for="item in merchants" :key="item.merchantId">
              <strong>{{ item.merchantName }}</strong>
              <span>{{ item.canteenName || '未分配食堂/店铺' }}</span>
              <div>
                <small>{{ item.dishCount || 0 }} 菜品</small>
                <small>{{ item.viewCount || 0 }} 浏览</small>
                <small>{{ formatScore(item.averageScore) }} 分</small>
              </div>
            </article>
          </div>
        </section>
      </div>
    </LoadingState>
  </PageShell>
</template>
