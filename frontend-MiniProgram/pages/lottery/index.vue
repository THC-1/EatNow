<template>
  <view class="page lottery-page">
    <view class="lottery-hero">
      <text class="kicker">饭点盲盒</text>
      <text class="title">把选择困难拆开吃掉</text>
      <text class="subtitle">全平台随缘抽、按预算口味抽，或者只从收藏里抽。</text>
    </view>

    <view class="mode-row">
      <view v-for="item in modes" :key="item.value" :class="['mode-card', mode === item.value ? 'active' : '']" @tap="mode = item.value">
        <text class="mode-title">{{ item.label }}</text>
        <text class="mode-desc">{{ item.desc }}</text>
      </view>
    </view>

    <view v-if="mode === 'CONDITION'" class="condition-card">
      <view class="condition-row">
        <text>预算上限</text>
        <view class="stepper">
          <text @tap="changeMaxPrice(-2)">-</text>
          <text>¥{{ condition.maxPrice }}</text>
          <text @tap="changeMaxPrice(2)">+</text>
        </view>
      </view>
      <view class="condition-row">
        <text>最低评分</text>
        <view class="stepper">
          <text @tap="changeMinScore(-0.5)">-</text>
          <text>{{ condition.minScore }}</text>
          <text @tap="changeMinScore(0.5)">+</text>
        </view>
      </view>
    </view>

    <button class="draw-btn" :class="{ spinning }" @tap="draw">
      <text>{{ spinning ? '正在翻菜卡' : '开抽' }}</text>
    </button>

    <view v-if="result" class="result-card">
      <view class="food-art result-art" @tap="goResultDetail">
        <image v-if="resultImage(result)" :src="resultImage(result)" mode="aspectFill" />
        <text v-else>{{ shortName(result.title) }}</text>
      </view>
      <view class="result-info">
        <text class="result-label">今天可以吃</text>
        <text class="result-title" @tap="goResultDetail">{{ result.title }}</text>
        <text class="result-place">{{ result.canteenName }} · {{ result.merchantName }}</text>
        <text class="result-reason">{{ result.recommendReason }}</text>
        <view class="tag-row">
          <text v-for="tag in result.tags" :key="tag" class="tag">{{ tag }}</text>
        </view>
        <view class="result-meta">
          <text>¥{{ result.price }}</text>
          <text>{{ result.score }} 分</text>
        </view>
      </view>
      <view class="action-row">
        <button class="ghost-btn" @tap="skip">再抽一次</button>
        <button class="ghost-btn" @tap="favorite">{{ resultFavorite.isFavorite ? '取消收藏' : '收藏' }}</button>
        <button class="primary-btn" @tap="accept">{{ result.sourceType === 'POST' ? '查看分享' : '就吃它' }}</button>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">抽签历史</text>
        <text class="section-link">最近</text>
      </view>
      <view class="history-list">
        <view v-for="item in history" :key="item.id" class="history-item" @tap="goDetail(item.sourceId, item.sourceType)">
          <view>
            <text class="history-title">{{ item.title }}</text>
            <text class="history-sub">{{ item.createdAt }} · {{ actionText(item.resultAction) }}</text>
          </view>
          <text class="history-score">{{ item.score }}</text>
        </view>
      </view>
      <view v-if="!history.length" class="empty">登录后可以看到最近抽过什么。</view>
    </view>
  </view>
</template>

<script>
import { checkFavorite, createEatList, createFavorite, deleteFavorite, drawLottery, fetchFavorites, fetchLotteryRecords, recordLotteryAction } from '../../services/student.js'
import { assetUrl, hasToken } from '../../utils/request.js'

export default {
  data() {
    return {
      mode: 'RANDOM',
      spinning: false,
      result: null,
      resultFavorite: {
        isFavorite: false,
        id: null
      },
      history: [],
      condition: {
        maxPrice: 20,
        minScore: 4
      },
      modes: [
        { label: '随缘抽', value: 'RANDOM', desc: '全平台随机' },
        { label: '条件抽', value: 'CONDITION', desc: '预算评分筛选' },
        { label: '收藏抽', value: 'FAVORITE', desc: '只抽收藏' }
      ]
    }
  },
  onShow() {
    this.loadHistory()
  },
  methods: {
    requireLogin() {
      if (hasToken()) return true
      uni.showToast({ title: '请先在我的页面登录', icon: 'none' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/mine/index' })
      }, 500)
      return false
    },
    async loadHistory() {
      if (!hasToken()) {
        this.history = []
        return
      }
      try {
        const page = await fetchLotteryRecords()
        this.history = page.records || []
      } catch (error) {
        this.history = []
      }
    },
    async draw() {
      if (!this.requireLogin()) return
      this.spinning = true
      this.resultFavorite = { isFavorite: false, id: null }
      const condition = this.mode === 'CONDITION' ? this.condition : {}
      try {
        const result = await drawLottery(this.mode, condition)
        setTimeout(() => {
          this.result = result
          this.loadResultFavorite()
          this.spinning = false
        }, 520)
      } catch (error) {
        this.spinning = false
        uni.showToast({ title: error.message || '抽签失败', icon: 'none' })
      }
    },
    changeMaxPrice(delta) {
      this.condition.maxPrice = Math.max(8, this.condition.maxPrice + delta)
    },
    changeMinScore(delta) {
      this.condition.minScore = Math.min(5, Math.max(3, this.condition.minScore + delta))
    },
    shortName(name) {
      return name ? name.slice(0, 2) : '饭'
    },
    resultImage(result) {
      const image = result && result.images && result.images.length ? result.images[0] : result.coverImageUrl
      return assetUrl(image)
    },
    async skip() {
      if (!this.requireLogin()) return
      try {
        if (this.result) await recordLotteryAction(this.result.recordId, 'SKIP')
        this.draw()
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' })
      }
    },
    async favorite() {
      if (!this.requireLogin()) return
      try {
        if (this.resultFavorite.isFavorite) {
          if (!this.resultFavorite.id) {
            await this.loadResultFavorite()
          }
          if (!this.resultFavorite.id) {
            uni.showToast({ title: '未找到收藏记录', icon: 'none' })
            return
          }
          await deleteFavorite(this.resultFavorite.id)
          this.resultFavorite = { isFavorite: false, id: null }
          uni.showToast({ title: '已取消收藏', icon: 'success' })
          return
        }
        const data = await createFavorite(this.result.sourceId, this.targetType(this.result))
        this.resultFavorite = { isFavorite: true, id: data.id }
        if (this.result.recordId) {
          await recordLotteryAction(this.result.recordId, 'FAVORITE')
        }
        uni.showToast({ title: '已收藏', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' })
      }
    },
    async loadResultFavorite() {
      if (!this.result || !this.result.sourceId || !hasToken()) {
        this.resultFavorite = { isFavorite: false, id: null }
        return
      }
      try {
        const targetType = this.targetType(this.result)
        const state = await checkFavorite(this.result.sourceId, targetType)
        let favoriteId = state.favoriteId || null
        if (state.isFavorite && !favoriteId) {
          const page = await fetchFavorites({ targetType, size: 50 })
          const matched = (page.records || []).find((item) => String(item.targetId) === String(this.result.sourceId))
          favoriteId = matched ? matched.id : null
        }
        this.resultFavorite = { isFavorite: !!state.isFavorite, id: favoriteId }
      } catch (error) {
        this.resultFavorite = { isFavorite: false, id: null }
      }
    },
    async accept() {
      if (!this.requireLogin()) return
      try {
        if (this.result.sourceType === 'POST') {
          await recordLotteryAction(this.result.recordId, 'ACCEPT')
          this.goResultDetail()
          return
        }
        await createEatList(this.result.sourceId, this.result.recordId)
        await recordLotteryAction(this.result.recordId, 'ACCEPT')
        uni.showToast({ title: '已加入想吃', icon: 'success' })
        this.loadHistory()
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' })
      }
    },
    targetType(item) {
      return item && item.sourceType === 'POST' ? 'POST' : 'DISH'
    },
    goResultDetail() {
      if (!this.result) return
      this.goDetail(this.result.sourceId, this.result.sourceType)
    },
    goDetail(id, sourceType = 'DISH') {
      if (!id) return
      if (sourceType === 'POST') {
        uni.navigateTo({ url: `/pages/post-detail/index?id=${id}` })
        return
      }
      uni.navigateTo({ url: `/pages/dish-detail/index?id=${id}` })
    },
    actionText(action) {
      const map = { ACCEPT: '就吃它', FAVORITE: '已收藏', SKIP: '跳过' }
      return map[action] || '看过'
    }
  }
}
</script>

<style>
.lottery-hero {
  padding: 34rpx;
  border-radius: 36rpx;
  background:
    radial-gradient(circle at 80% 12%, rgba(127, 189, 65, 0.55), transparent 180rpx),
    linear-gradient(135deg, #2b2118 0%, #604229 100%);
  color: #fffdf7;
}

.kicker {
  display: block;
  color: #ffd64e;
  font-size: 24rpx;
  font-weight: 900;
}

.title {
  display: block;
  margin-top: 12rpx;
  font-size: 44rpx;
  font-weight: 900;
  line-height: 1.12;
}

.subtitle {
  display: block;
  width: 520rpx;
  margin-top: 14rpx;
  color: rgba(255, 253, 247, 0.75);
  font-size: 25rpx;
  line-height: 1.45;
}

.mode-row {
  display: flex;
  gap: 16rpx;
  margin-top: 22rpx;
}

.mode-card {
  flex: 1;
  min-height: 132rpx;
  padding: 22rpx 14rpx;
  border-radius: 26rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.mode-card.active {
  background: #fff1d1;
  border-color: rgba(233, 75, 53, 0.32);
}

.mode-title {
  display: block;
  color: #241811;
  font-size: 27rpx;
  font-weight: 900;
  text-align: center;
}

.mode-desc {
  display: block;
  margin-top: 10rpx;
  color: #8a7a68;
  font-size: 21rpx;
  text-align: center;
}

.condition-card {
  margin-top: 18rpx;
  padding: 22rpx;
  border-radius: 26rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.condition-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 70rpx;
  color: #5d4a3b;
  font-size: 26rpx;
  font-weight: 800;
}

.stepper {
  display: flex;
  align-items: center;
  height: 58rpx;
  overflow: hidden;
  border-radius: 999rpx;
  background: #fff7ea;
}

.stepper text {
  min-width: 70rpx;
  text-align: center;
  color: #e94b35;
  font-size: 27rpx;
  font-weight: 900;
}

.draw-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 220rpx;
  height: 220rpx;
  margin: 34rpx auto 0;
  border-radius: 50%;
  background: radial-gradient(circle at 34% 28%, #fffdf7 0 18rpx, #ffd64e 19rpx, #e94b35 100%);
  color: #fffdf7;
  font-size: 42rpx;
  font-weight: 900;
  box-shadow: 0 24rpx 44rpx rgba(233, 75, 53, 0.28);
}

.draw-btn.spinning {
  transform: scale(0.96);
  opacity: 0.86;
}

.result-card {
  margin-top: 30rpx;
  padding: 24rpx;
  border-radius: 32rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.result-art {
  display: flex;
  align-items: flex-end;
  height: 260rpx;
  padding: 28rpx;
  color: #fffdf7;
  font-size: 58rpx;
  font-weight: 900;
}

.result-art image {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 0;
  width: 100%;
  height: 100%;
}

.result-art text {
  position: relative;
  z-index: 1;
}

.result-info {
  padding: 22rpx 4rpx 4rpx;
}

.result-label {
  color: #7fbd41;
  font-size: 23rpx;
  font-weight: 900;
}

.result-title {
  display: block;
  margin-top: 6rpx;
  color: #241811;
  font-size: 40rpx;
  font-weight: 900;
}

.result-place,
.result-reason {
  display: block;
  margin-top: 10rpx;
  color: #6f5e4e;
  font-size: 25rpx;
  line-height: 1.42;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 18rpx;
}

.tag {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #e9f8cf;
  color: #4f8f20;
  font-size: 21rpx;
  font-weight: 800;
}

.result-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 20rpx;
  color: #e94b35;
  font-size: 30rpx;
  font-weight: 900;
}

.action-row {
  display: flex;
  gap: 14rpx;
  margin-top: 24rpx;
}

.action-row button {
  flex: 1;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.history-title {
  display: block;
  color: #241811;
  font-size: 27rpx;
  font-weight: 900;
}

.history-sub {
  display: block;
  margin-top: 8rpx;
  color: #9d8466;
  font-size: 22rpx;
}

.history-score {
  color: #e94b35;
  font-size: 30rpx;
  font-weight: 900;
}
</style>
