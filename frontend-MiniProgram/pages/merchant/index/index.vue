<template>
  <view class="page merchant-page">
    <view class="merchant-hero">
      <view class="hero-main">
        <text class="eyebrow">Merchant Studio</text>
        <text class="hero-title">{{ profile.name || '卖家工作台' }}</text>
        <text class="hero-sub">{{ merchantLocationText }} · {{ statusText(profile.status) }}</text>
      </view>
      <view class="hero-tools">
        <button class="login-chip" @tap="login">{{ loggedIn ? '刷新' : '卖家登录' }}</button>
        <text class="apply-link" @tap="goApply">入驻资料</text>
      </view>
    </view>

    <view v-if="profile.applyStatus && profile.applyStatus !== 'APPROVED'" class="audit-card" @tap="goApply">
      <text class="audit-title">{{ applyText(profile.applyStatus) }}</text>
      <text class="audit-sub">完善入驻资料，等待管理员审核后即可管理菜品。</text>
    </view>

    <view class="metric-grid">
      <view v-for="item in metrics" :key="item.label" class="metric-card">
        <text class="metric-value">{{ item.value }}</text>
        <text class="metric-label">{{ item.label }}</text>
      </view>
    </view>

    <view class="quick-panel">
      <view class="quick-action hot" @tap="goDishes">
        <text class="quick-mark">菜</text>
        <text class="quick-title">菜品管理</text>
        <text class="quick-sub">新增、编辑、售罄</text>
      </view>
      <view class="quick-action green" @tap="goRecommendations">
        <text class="quick-mark">推</text>
        <text class="quick-title">今日推荐</text>
        <text class="quick-sub">主推、新品、招牌</text>
      </view>
      <view class="quick-action ink" @tap="goFeedback">
        <text class="quick-mark">信</text>
        <text class="quick-title">留言反馈</text>
        <text class="quick-sub">{{ overview.pendingFeedbackCount || 0 }} 条待处理</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">运营提醒</text>
        <text class="section-link" @tap="goFeedback">处理反馈</text>
      </view>
      <view class="notice-card">
        <view>
          <text class="notice-title">今日推荐点击 {{ overview.todayRecommendClickCount || 0 }} 次</text>
          <text class="notice-sub">把高评分菜品保持上架，能让抽签和首页曝光更稳。</text>
        </view>
        <text class="notice-badge">{{ overview.averageScore || '-' }} 分</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">受欢迎菜品</text>
        <text class="section-link" @tap="goDishes">全部</text>
      </view>
      <view class="rank-list">
        <view v-for="(dish, index) in popularDishes" :key="dish.dishId" class="rank-item">
          <text class="rank-no">{{ index + 1 }}</text>
          <view class="rank-info">
            <text class="rank-name">{{ dish.dishName }}</text>
            <text class="rank-sub">{{ dish.viewCount || 0 }} 浏览 · {{ dish.favoriteCount || 0 }} 收藏</text>
          </view>
          <text class="rank-score">{{ dish.averageScore || '-' }}</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">反馈较多</text>
        <text class="section-link" @tap="goFeedback">查看</text>
      </view>
      <view class="feedback-strip">
        <view v-for="dish in feedbackDishes" :key="dish.dishId" class="feedback-chip">
          <text>{{ dish.dishName }}</text>
          <text>{{ dish.pendingFeedbackCount || 0 }} 待处理</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import {
  fetchMerchantFeedbackDishes,
  fetchMerchantOverview,
  fetchMerchantPopularDishes,
  fetchMerchantProfile,
  merchantLogin
} from '../../../services/merchant.js'

export default {
  data() {
    return {
      loggedIn: !!uni.getStorageSync('eatnow_token'),
      profile: {},
      overview: {},
      popularDishes: [],
      feedbackDishes: []
    }
  },
  computed: {
    merchantLocationText() {
      if (!this.profile.id) {
        return '店铺资料'
      }
      if (this.profile.canteenType === 'CANTEEN') {
        return `${this.profile.canteenName || '食堂'} · ${this.profile.stallName || '窗口'}`
      }
      const typeText = this.profile.canteenType === 'PERIPHERY_SHOP' ? '校外独立店' : '校内独立店'
      return `${typeText} · ${this.profile.canteenName || this.profile.name || '店铺'}`
    },
    metrics() {
      return [
        { label: '菜品', value: this.overview.dishCount || 0 },
        { label: '浏览', value: this.shortNumber(this.overview.viewCount) },
        { label: '收藏', value: this.shortNumber(this.overview.favoriteCount) },
        { label: '评价', value: this.shortNumber(this.overview.reviewCount) }
      ]
    }
  },
  onLoad() {
    this.load()
  },
  onShow() {
    this.load()
  },
  methods: {
    async load() {
      const [profile, overview, popularDishes, feedbackDishes] = await Promise.all([
        fetchMerchantProfile(),
        fetchMerchantOverview(),
        fetchMerchantPopularDishes(4),
        fetchMerchantFeedbackDishes(3)
      ])
      this.profile = profile || {}
      this.overview = overview || {}
      this.popularDishes = popularDishes || []
      this.feedbackDishes = feedbackDishes || []
    },
    async login() {
      const data = await merchantLogin()
      this.loggedIn = true
      await this.load()
      uni.showToast({ title: data.applyStatus === 'APPROVED' ? '登录成功' : '已提交审核', icon: 'success' })
    },
    shortNumber(value) {
      const num = Number(value || 0)
      return num >= 1000 ? `${(num / 1000).toFixed(1)}k` : num
    },
    statusText(status) {
      const map = { OPEN: '营业中', CLOSED: '已休息', PENDING: '待审核' }
      return map[status] || '营业中'
    },
    applyText(status) {
      const map = { PENDING: '入驻审核中', REJECTED: '入驻被驳回，去修改资料' }
      return map[status] || '去提交入驻申请'
    },
    goApply() {
      uni.navigateTo({ url: '/pages/merchant/apply/index' })
    },
    goDishes() {
      uni.navigateTo({ url: '/pages/merchant/dishes/index' })
    },
    goRecommendations() {
      uni.navigateTo({ url: '/pages/merchant/recommendations/index' })
    },
    goFeedback() {
      uni.navigateTo({ url: '/pages/merchant/feedback/index' })
    }
  }
}
</script>

<style>
.merchant-page {
  padding-top: 24rpx;
}

.merchant-hero {
  display: flex;
  justify-content: space-between;
  min-height: 250rpx;
  padding: 32rpx;
  border-radius: 34rpx;
  background:
    radial-gradient(circle at 18% 14%, rgba(255, 214, 78, 0.72), transparent 160rpx),
    linear-gradient(135deg, #fff0b8 0%, #ff9657 58%, #e94b35 100%);
  box-shadow: 0 22rpx 42rpx rgba(233, 75, 53, 0.2);
}

.hero-main {
  flex: 1;
  min-width: 0;
}

.eyebrow {
  display: block;
  color: #4f8f20;
  font-size: 23rpx;
  font-weight: 900;
}

.hero-title {
  display: block;
  margin-top: 10rpx;
  color: #241811;
  font-size: 48rpx;
  font-weight: 900;
  line-height: 1.08;
}

.hero-sub {
  display: block;
  margin-top: 18rpx;
  color: rgba(43, 33, 24, 0.7);
  font-size: 25rpx;
  font-weight: 700;
}

.login-chip {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 136rpx;
  height: 62rpx;
  border-radius: 999rpx;
  background: #2b2118;
  color: #fffdf7;
  font-size: 24rpx;
  font-weight: 900;
}

.hero-tools {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.apply-link {
  display: block;
  margin-top: 18rpx;
  color: #2b2118;
  font-size: 23rpx;
  font-weight: 900;
}

.audit-card {
  margin-top: 22rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(233, 75, 53, 0.16);
}

.audit-title,
.audit-sub {
  display: block;
}

.audit-title {
  color: #e94b35;
  font-size: 29rpx;
  font-weight: 900;
}

.audit-sub {
  margin-top: 8rpx;
  color: #7b6046;
  font-size: 24rpx;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14rpx;
  margin-top: 22rpx;
}

.metric-card {
  min-height: 126rpx;
  padding: 20rpx 8rpx;
  border-radius: 24rpx;
  background: #fffdf7;
  text-align: center;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.metric-value {
  display: block;
  color: #241811;
  font-size: 34rpx;
  font-weight: 900;
}

.metric-label {
  display: block;
  margin-top: 10rpx;
  color: #8a7a68;
  font-size: 22rpx;
  font-weight: 800;
}

.quick-panel {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  margin-top: 22rpx;
}

.quick-action {
  min-height: 174rpx;
  padding: 22rpx;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.quick-action.hot .quick-mark {
  background: #ffe2d0;
  color: #e94b35;
}

.quick-action.green .quick-mark {
  background: #e9f8cf;
  color: #4f8f20;
}

.quick-action.ink .quick-mark {
  background: #2b2118;
  color: #fffdf7;
}

.quick-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52rpx;
  height: 52rpx;
  border-radius: 18rpx;
  font-size: 25rpx;
  font-weight: 900;
}

.quick-title {
  display: block;
  margin-top: 18rpx;
  color: #241811;
  font-size: 26rpx;
  font-weight: 900;
}

.quick-sub {
  display: block;
  margin-top: 8rpx;
  color: #9d8466;
  font-size: 21rpx;
}

.notice-card,
.rank-list {
  overflow: hidden;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.notice-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26rpx;
}

.notice-title {
  display: block;
  color: #241811;
  font-size: 29rpx;
  font-weight: 900;
}

.notice-sub {
  display: block;
  margin-top: 8rpx;
  color: #7b6046;
  font-size: 23rpx;
  line-height: 1.4;
}

.notice-badge {
  flex: 0 0 92rpx;
  height: 92rpx;
  border-radius: 30rpx;
  background: #e9f8cf;
  color: #4f8f20;
  text-align: center;
  line-height: 92rpx;
  font-size: 30rpx;
  font-weight: 900;
}

.rank-item {
  display: flex;
  align-items: center;
  min-height: 106rpx;
  padding: 18rpx 22rpx;
  border-bottom: 2rpx solid rgba(43, 33, 24, 0.06);
}

.rank-item:last-child {
  border-bottom: 0;
}

.rank-no {
  width: 48rpx;
  height: 48rpx;
  border-radius: 16rpx;
  background: #ffd64e;
  color: #241811;
  text-align: center;
  line-height: 48rpx;
  font-size: 25rpx;
  font-weight: 900;
}

.rank-info {
  flex: 1;
  min-width: 0;
  margin-left: 18rpx;
}

.rank-name,
.rank-sub {
  display: block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.rank-name {
  color: #241811;
  font-size: 28rpx;
  font-weight: 900;
}

.rank-sub {
  margin-top: 6rpx;
  color: #9d8466;
  font-size: 22rpx;
}

.rank-score {
  color: #e94b35;
  font-size: 30rpx;
  font-weight: 900;
}

.feedback-strip {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.feedback-chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 74rpx;
  padding: 0 22rpx;
  border-radius: 22rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.feedback-chip text:first-child {
  color: #241811;
  font-size: 25rpx;
  font-weight: 900;
}

.feedback-chip text:last-child {
  color: #e94b35;
  font-size: 23rpx;
  font-weight: 900;
}
</style>
