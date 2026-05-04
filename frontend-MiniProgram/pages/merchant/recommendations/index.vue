<template>
  <view class="page recommend-page">
    <view class="top-panel">
      <text class="eyebrow">Spotlight</text>
      <text class="page-title">推荐管理</text>
      <text class="page-sub">把今天最值得吃的菜推到学生首页和抽签结果里。</text>
    </view>

    <view class="form-card">
      <view class="form-head">
        <text>{{ form.id ? '编辑推荐' : '发布今日推荐' }}</text>
        <text v-if="form.id" @tap="resetForm">取消编辑</text>
      </view>
      <view class="form-row">
        <text class="label">推荐菜品</text>
        <picker :range="dishes" range-key="name" @change="chooseDish">
          <view class="picker-value">{{ currentDishName }}</view>
        </picker>
      </view>
      <view class="form-row">
        <text class="label">推荐类型</text>
        <picker :range="typeOptions" range-key="label" @change="chooseType">
          <view class="picker-value">{{ currentTypeLabel }}</view>
        </picker>
      </view>
      <view class="form-row">
        <text class="label">标题</text>
        <input v-model="form.title" placeholder="午餐主推：黑椒鸡排饭" />
      </view>
      <view class="form-row block">
        <text class="label">推荐理由</text>
        <textarea v-model="form.recommendReason" maxlength="500" placeholder="现做、分量、口味或适合场景" />
      </view>
      <view class="time-grid">
        <view>
          <text>开始时间</text>
          <input v-model="form.startTime" placeholder="2026-04-26T10:00:00" />
        </view>
        <view>
          <text>结束时间</text>
          <input v-model="form.endTime" placeholder="2026-04-26T20:30:00" />
        </view>
      </view>
      <view class="top-row">
        <text>置顶展示</text>
        <switch :checked="form.isTop" color="#E94B35" @change="form.isTop = $event.detail.value" />
      </view>
      <button class="primary-btn" @tap="submit">{{ form.id ? '保存推荐' : '发布推荐' }}</button>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">推荐记录</text>
        <text class="section-link" @tap="loadRecommendations">刷新</text>
      </view>
      <view class="record-list">
        <view v-for="item in recommendations" :key="item.id" class="record-card">
          <view class="record-top">
            <view>
              <text class="record-title">{{ item.title }}</text>
              <text class="record-sub">{{ item.dishName }} · {{ typeText(item.recommendType) }}</text>
            </view>
            <text class="record-status">{{ statusText(item.status) }}</text>
          </view>
          <text class="record-reason">{{ item.recommendReason }}</text>
          <view class="record-foot">
            <text>{{ item.clickCount || 0 }} 点击</text>
            <view>
              <text @tap="edit(item)">编辑</text>
              <text @tap="remove(item.id)">删除</text>
            </view>
          </view>
        </view>
      </view>
      <view v-if="!recommendations.length" class="empty">还没有推荐记录</view>
    </view>
  </view>
</template>

<script>
import {
  createRecommendation,
  deleteRecommendation,
  fetchMerchantDishes,
  fetchMerchantRecommendations,
  updateRecommendation
} from '../../../services/merchant.js'

function pad(value) {
  return String(value).padStart(2, '0')
}

function dayTime(hour, minute) {
  const now = new Date()
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(hour)}:${pad(minute)}:00`
}

function emptyForm() {
  return {
    id: '',
    dishId: '',
    title: '',
    recommendReason: '',
    recommendType: 'TODAY',
    startTime: dayTime(10, 0),
    endTime: dayTime(20, 30),
    isTop: true
  }
}

export default {
  data() {
    return {
      dishes: [],
      recommendations: [],
      form: emptyForm(),
      typeOptions: [
        { label: '今日主推', value: 'TODAY' },
        { label: '新品', value: 'NEW' },
        { label: '特色', value: 'SPECIAL' },
        { label: '高性价比', value: 'VALUE' },
        { label: '招牌', value: 'SIGNATURE' }
      ]
    }
  },
  computed: {
    currentDishName() {
      const dish = this.dishes.find((item) => String(item.id) === String(this.form.dishId))
      return dish ? dish.name : '选择菜品'
    },
    currentTypeLabel() {
      const type = this.typeOptions.find((item) => item.value === this.form.recommendType)
      return type ? type.label : '今日主推'
    }
  },
  onLoad() {
    this.load()
  },
  methods: {
    async load() {
      const page = await fetchMerchantDishes({ page: 1, size: 30, status: 'ON_SALE' })
      this.dishes = page.records || []
      if (!this.form.dishId && this.dishes.length) {
        this.form.dishId = this.dishes[0].id
        this.form.title = `今日主推：${this.dishes[0].name}`
      }
      this.loadRecommendations()
    },
    async loadRecommendations() {
      const page = await fetchMerchantRecommendations({ page: 1, size: 30 })
      this.recommendations = page.records || []
    },
    chooseDish(event) {
      const dish = this.dishes[Number(event.detail.value)]
      this.form.dishId = dish ? dish.id : ''
      if (dish && !this.form.id) {
        this.form.title = `${this.currentTypeLabel}：${dish.name}`
      }
    },
    chooseType(event) {
      const option = this.typeOptions[Number(event.detail.value)]
      this.form.recommendType = option ? option.value : 'TODAY'
      const dish = this.dishes.find((item) => String(item.id) === String(this.form.dishId))
      if (dish && !this.form.id) {
        this.form.title = `${option ? option.label : '今日主推'}：${dish.name}`
      }
    },
    async submit() {
      if (!this.form.dishId || !this.form.title) {
        uni.showToast({ title: '请选择菜品并填写标题', icon: 'none' })
        return
      }
      const payload = {
        dishId: Number(this.form.dishId),
        title: this.form.title,
        recommendReason: this.form.recommendReason,
        recommendType: this.form.recommendType,
        startTime: this.form.startTime,
        endTime: this.form.endTime,
        isTop: this.form.isTop
      }
      if (this.form.id) {
        await updateRecommendation(this.form.id, payload)
      } else {
        await createRecommendation(payload)
      }
      uni.showToast({ title: '已保存推荐', icon: 'success' })
      this.resetForm()
      this.loadRecommendations()
    },
    edit(item) {
      this.form = {
        id: item.id,
        dishId: item.dishId,
        title: item.title,
        recommendReason: item.recommendReason || '',
        recommendType: item.recommendType || 'TODAY',
        startTime: item.startTime || dayTime(10, 0),
        endTime: item.endTime || dayTime(20, 30),
        isTop: item.isTop !== false
      }
    },
    async remove(id) {
      await deleteRecommendation(id)
      uni.showToast({ title: '已删除', icon: 'success' })
      this.loadRecommendations()
    },
    resetForm() {
      const next = emptyForm()
      next.dishId = this.dishes.length ? this.dishes[0].id : ''
      next.title = this.dishes[0] ? `今日主推：${this.dishes[0].name}` : ''
      this.form = next
    },
    typeText(type) {
      const target = this.typeOptions.find((item) => item.value === type)
      return target ? target.label : type
    },
    statusText(status) {
      const map = { ACTIVE: '展示中', EXPIRED: '已过期', CANCELLED: '已取消' }
      return map[status] || '展示中'
    }
  }
}
</script>

<style>
.recommend-page {
  padding-top: 24rpx;
}

.top-panel {
  padding: 32rpx;
  border-radius: 34rpx;
  background:
    radial-gradient(circle at 82% 20%, rgba(127, 189, 65, 0.42), transparent 150rpx),
    linear-gradient(135deg, #fff4c2 0%, #ffd64e 42%, #ff8b4a 100%);
}

.eyebrow,
.page-title,
.page-sub {
  display: block;
}

.eyebrow {
  color: #4f8f20;
  font-size: 23rpx;
  font-weight: 900;
}

.page-title {
  margin-top: 8rpx;
  color: #241811;
  font-size: 46rpx;
  font-weight: 900;
}

.page-sub {
  margin-top: 14rpx;
  color: #6f5e4e;
  font-size: 25rpx;
  line-height: 1.45;
}

.form-card,
.record-card {
  margin-top: 22rpx;
  padding: 24rpx;
  border-radius: 30rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.form-head,
.form-row,
.top-row,
.record-top,
.record-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-head text:first-child {
  color: #241811;
  font-size: 31rpx;
  font-weight: 900;
}

.form-head text:last-child {
  color: #e94b35;
  font-size: 24rpx;
  font-weight: 900;
}

.form-row {
  min-height: 86rpx;
  border-bottom: 2rpx solid rgba(43, 33, 24, 0.06);
}

.form-row.block {
  display: block;
  padding: 20rpx 0;
}

.label {
  width: 150rpx;
  color: #7b6046;
  font-size: 25rpx;
  font-weight: 900;
}

.form-row input,
.picker-value {
  flex: 1;
  height: 82rpx;
  color: #241811;
  font-size: 27rpx;
  line-height: 82rpx;
}

.form-row picker {
  flex: 1;
}

.form-row textarea {
  width: 100%;
  height: 150rpx;
  margin-top: 14rpx;
  padding: 18rpx;
  border-radius: 22rpx;
  background: #fff7ea;
  color: #241811;
  font-size: 26rpx;
  line-height: 1.45;
}

.time-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14rpx;
  margin-top: 18rpx;
}

.time-grid view {
  padding: 18rpx;
  border-radius: 22rpx;
  background: #fff7ea;
}

.time-grid text {
  display: block;
  color: #7b6046;
  font-size: 22rpx;
  font-weight: 900;
}

.time-grid input {
  height: 58rpx;
  margin-top: 6rpx;
  color: #241811;
  font-size: 25rpx;
}

.top-row {
  min-height: 88rpx;
  color: #241811;
  font-size: 26rpx;
  font-weight: 900;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.record-title,
.record-sub,
.record-reason {
  display: block;
}

.record-title {
  color: #241811;
  font-size: 29rpx;
  font-weight: 900;
}

.record-sub,
.record-reason,
.record-foot {
  color: #8a7a68;
  font-size: 23rpx;
}

.record-sub {
  margin-top: 8rpx;
}

.record-status {
  padding: 10rpx 14rpx;
  border-radius: 999rpx;
  background: #e9f8cf;
  color: #4f8f20;
  font-size: 21rpx;
  font-weight: 900;
}

.record-reason {
  margin-top: 18rpx;
  line-height: 1.45;
}

.record-foot {
  margin-top: 18rpx;
  font-weight: 900;
}

.record-foot view text {
  margin-left: 24rpx;
  color: #e94b35;
}
</style>
