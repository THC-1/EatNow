<template>
  <view class="page home-page">
    <view class="hero">
      <view class="hero-top">
        <view>
          <text class="eyebrow">EatNow</text>
          <text class="hero-title">今天吃点啥</text>
        </view>
      </view>

      <view class="search" @tap="goDishes">
        <text class="search-icon">⌕</text>
        <text class="search-placeholder">搜菜名、窗口、口味标签</text>
      </view>

      <view class="hero-card" @tap="goLottery">
        <view class="hero-copy">
          <text class="hero-card-title">饭点盲盒</text>
          <text class="hero-card-subtitle">选择困难时，交给今天的食欲算法。</text>
          <view class="hero-actions">
            <text class="hero-action">马上抽签</text>
            <text class="hero-note">全平台 / 条件 / 收藏</text>
          </view>
        </view>
        <view class="bento">
          <view class="bento-dot bento-one"></view>
          <view class="bento-dot bento-two"></view>
          <view class="bento-dot bento-three"></view>
        </view>
      </view>
    </view>

    <view class="quick-grid">
      <view class="quick-item" @tap="goCanteens">
        <text class="quick-mark">堂</text>
        <text class="quick-title">找食堂</text>
      </view>
      <view class="quick-item" @tap="goDishes">
        <text class="quick-mark">菜</text>
        <text class="quick-title">逛菜品</text>
      </view>
      <view class="quick-item" @tap="goRanking('top-rated')">
        <text class="quick-mark">榜</text>
        <text class="quick-title">看排行</text>
      </view>
      <view class="quick-item merchant" @tap="goMerchant">
        <text class="quick-mark">店</text>
        <text class="quick-title">卖家端</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">今日热门</text>
        <text class="section-link" @tap="goDishes">全部菜品</text>
      </view>
      <scroll-view scroll-x class="dish-scroll" show-scrollbar="false">
        <view class="dish-row">
          <view v-for="dish in hotDishes" :key="dish.id" class="dish-card" @tap="goDetail(dish.id)">
            <view class="food-art dish-art"><text>{{ shortName(dish.name) }}</text></view>
            <view class="dish-body">
              <text class="dish-name">{{ dish.name }}</text>
              <text class="dish-place">{{ dish.canteenName }} · {{ dish.stallName }}</text>
              <view class="dish-meta">
                <text class="price">¥{{ dish.price }}</text>
                <text class="score">{{ dish.score }} 分</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">好评榜</text>
        <text class="section-link" @tap="goRanking('top-rated')">更多</text>
      </view>
      <view class="rank-list">
        <view v-for="item in rankings" :key="item.dishId" class="rank-item" @tap="goDetail(item.dishId)">
          <text class="rank-no">{{ item.rank }}</text>
          <view class="rank-info">
            <text class="rank-name">{{ item.dishName }}</text>
            <text class="rank-sub">{{ item.canteenName }} · {{ item.merchantName }}</text>
          </view>
          <text class="rank-score">{{ item.score }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { fetchDishes, fetchRanking } from '../../services/student.js'

export default {
  data() {
    return {
      hotDishes: [],
      rankings: []
    }
  },
  onLoad() {
    this.load()
  },
  methods: {
    async load() {
      const [dishPage, ranking] = await Promise.all([
        fetchDishes({ page: 1, size: 6, status: 'ON_SALE' }),
        fetchRanking('top-rated')
      ])
      this.hotDishes = dishPage.records || []
      this.rankings = ranking.slice(0, 4)
    },
    shortName(name) {
      return name.slice(0, 2)
    },
    goLottery() {
      uni.switchTab({ url: '/pages/lottery/index' })
    },
    goCanteens() {
      uni.switchTab({ url: '/pages/canteens/index' })
    },
    goDishes() {
      uni.navigateTo({ url: '/pages/dishes/index' })
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/dish-detail/index?id=${id}` })
    },
    goRanking(type) {
      uni.navigateTo({ url: `/pages/dishes/index?ranking=${type}` })
    },
    goMerchant() {
      uni.navigateTo({ url: '/pages/merchant/index/index' })
    }
  }
}
</script>

<style>
.home-page {
  padding-top: 72rpx;
}

.hero {
  position: relative;
}

.hero-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.eyebrow {
  display: block;
  color: #7fbd41;
  font-size: 24rpx;
  font-weight: 900;
}

.hero-title {
  display: block;
  margin-top: 6rpx;
  color: #241811;
  font-size: 56rpx;
  font-weight: 900;
  line-height: 1.05;
}

.search {
  display: flex;
  align-items: center;
  height: 82rpx;
  margin-top: 30rpx;
  padding: 0 24rpx;
  border-radius: 28rpx;
  background: rgba(255, 253, 247, 0.88);
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.search-icon {
  color: #e94b35;
  font-size: 38rpx;
  font-weight: 900;
}

.search-placeholder {
  margin-left: 14rpx;
  color: #9d8466;
  font-size: 27rpx;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  min-height: 292rpx;
  margin-top: 26rpx;
  padding: 34rpx;
  border-radius: 36rpx;
  background:
    radial-gradient(circle at 14% 18%, rgba(255, 214, 78, 0.64), transparent 170rpx),
    linear-gradient(135deg, #ffe09a 0%, #ff8b4a 58%, #e94b35 100%);
  color: #2b2118;
  box-shadow: 0 24rpx 46rpx rgba(233, 75, 53, 0.22);
}

.hero-copy {
  width: 420rpx;
}

.hero-card-title {
  display: block;
  color: #241811;
  font-size: 44rpx;
  font-weight: 900;
}

.hero-card-subtitle {
  display: block;
  width: 360rpx;
  margin-top: 16rpx;
  color: rgba(43, 33, 24, 0.78);
  font-size: 26rpx;
  line-height: 1.45;
}

.hero-actions {
  display: flex;
  align-items: center;
  margin-top: 34rpx;
}

.hero-action {
  padding: 18rpx 24rpx;
  border-radius: 999rpx;
  background: #fffdf7;
  color: #e94b35;
  font-size: 26rpx;
  font-weight: 900;
}

.hero-note {
  margin-left: 16rpx;
  color: rgba(43, 33, 24, 0.62);
  font-size: 22rpx;
  font-weight: 700;
}

.bento {
  position: relative;
  width: 156rpx;
  height: 180rpx;
  margin-top: 32rpx;
}

.bento-dot {
  position: absolute;
  border-radius: 50%;
  background: #fffdf7;
  box-shadow: inset 0 -8rpx 0 rgba(43, 33, 24, 0.08);
}

.bento-one {
  width: 118rpx;
  height: 118rpx;
  right: 6rpx;
  top: 0;
}

.bento-two {
  width: 78rpx;
  height: 78rpx;
  left: 0;
  bottom: 10rpx;
  background: #7fbd41;
}

.bento-three {
  width: 54rpx;
  height: 54rpx;
  right: 0;
  bottom: 0;
  background: #2b2118;
}

.quick-grid {
  display: flex;
  gap: 18rpx;
  margin-top: 24rpx;
}

.quick-item {
  flex: 1;
  min-height: 136rpx;
  padding: 22rpx;
  border-radius: 26rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.quick-item.merchant .quick-mark {
  background: #ffe2d0;
  color: #e94b35;
}

.quick-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52rpx;
  height: 52rpx;
  border-radius: 18rpx;
  background: #e9f8cf;
  color: #4f8f20;
  font-size: 26rpx;
  font-weight: 900;
}

.quick-title {
  display: block;
  margin-top: 18rpx;
  color: #2b2118;
  font-size: 27rpx;
  font-weight: 800;
}

.dish-scroll {
  width: 100%;
  white-space: nowrap;
}

.dish-row {
  display: inline-flex;
  gap: 20rpx;
  padding-right: 28rpx;
}

.dish-card {
  display: inline-block;
  width: 282rpx;
  overflow: hidden;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.dish-art {
  display: flex;
  align-items: flex-end;
  width: 100%;
  height: 158rpx;
  padding: 20rpx;
  color: #fffdf7;
  font-size: 42rpx;
  font-weight: 900;
}

.dish-body {
  padding: 20rpx;
}

.dish-name {
  display: block;
  color: #241811;
  font-size: 29rpx;
  font-weight: 900;
}

.dish-place {
  display: block;
  height: 38rpx;
  margin-top: 8rpx;
  color: #9d8466;
  font-size: 22rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.dish-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}

.price {
  color: #e94b35;
  font-size: 30rpx;
  font-weight: 900;
}

.score {
  color: #4f8f20;
  font-size: 23rpx;
  font-weight: 800;
}

.rank-list {
  overflow: hidden;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.rank-item {
  display: flex;
  align-items: center;
  min-height: 112rpx;
  padding: 20rpx 22rpx;
  border-bottom: 2rpx solid rgba(43, 33, 24, 0.06);
}

.rank-item:last-child {
  border-bottom: 0;
}

.rank-no {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50rpx;
  height: 50rpx;
  border-radius: 16rpx;
  background: #ffd64e;
  color: #2b2118;
  font-size: 25rpx;
  font-weight: 900;
}

.rank-info {
  flex: 1;
  min-width: 0;
  margin-left: 18rpx;
}

.rank-name {
  display: block;
  color: #241811;
  font-size: 28rpx;
  font-weight: 900;
}

.rank-sub {
  display: block;
  margin-top: 6rpx;
  color: #9d8466;
  font-size: 22rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.rank-score {
  color: #e94b35;
  font-size: 28rpx;
  font-weight: 900;
}
</style>
