<template>
  <view class="page canteen-page">
    <view class="head">
      <text class="title">去哪儿吃</text>
      <text class="subtitle">先选食堂或独立店铺，再找喜欢的菜。</text>
    </view>

    <view class="type-tabs">
      <view v-for="item in types" :key="item.value" :class="['type-tab', activeType === item.value ? 'active' : '']" @tap="selectType(item.value)">
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view class="canteen-list">
      <view v-for="canteen in filteredCanteens" :key="canteen.id" class="canteen-card" @tap="selectCanteen(canteen)">
        <view class="canteen-main">
          <view class="canteen-badge">
            <text>{{ canteen.name.slice(0, 1) }}</text>
          </view>
          <view class="canteen-info">
            <view class="canteen-title-row">
              <text class="canteen-name">{{ canteen.name }}</text>
              <text class="status">{{ canteen.status === 'OPEN' ? '营业中' : '休息中' }}</text>
            </view>
            <text class="canteen-desc">{{ canteen.description }}</text>
            <view class="canteen-meta">
              <text>{{ canteen.location }}</text>
              <text>{{ canteen.openingHours }}</text>
              <text>{{ canteen.averageScore }} 分</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="section" v-if="currentCanteen">
      <view class="section-head">
        <text class="section-title">{{ currentSectionTitle }}</text>
        <text class="section-link" @tap="goDishes(currentCanteen.id)">看全部菜</text>
      </view>

      <view v-if="currentCanteen.type === 'CANTEEN'" class="stall-list">
        <view v-for="stall in stalls" :key="stall.id" class="stall-card" @tap="goDishes(currentCanteen.id, stall.id)">
          <view>
            <text class="stall-name">{{ stall.name }}</text>
            <text class="stall-sub">{{ stall.merchantName }} · {{ stall.location }}</text>
          </view>
          <view class="stall-score">
            <text>{{ stall.averageScore || 4.5 }}</text>
            <text>分</text>
          </view>
        </view>
      </view>
      <view v-else class="independent-card" @tap="goDishes(currentCanteen.id)">
        <view>
          <text class="stall-name">{{ currentCanteen.name }}</text>
          <text class="stall-sub">{{ currentCanteen.type === 'CAMPUS_SHOP' ? '校内独立店铺' : '校外独立店铺' }} · {{ currentCanteen.location || '位置待补充' }}</text>
        </view>
        <text class="section-link">进店</text>
      </view>
    </view>
  </view>
</template>

<script>
import { fetchCanteens, fetchStalls } from '../../services/student.js'

export default {
  data() {
    return {
      types: [
        { label: '全部', value: 'ALL' },
        { label: '食堂', value: 'CANTEEN' },
        { label: '校内店', value: 'CAMPUS_SHOP' },
        { label: '校外店', value: 'PERIPHERY_SHOP' }
      ],
      activeType: 'ALL',
      canteens: [],
      currentCanteen: null,
      stalls: []
    }
  },
  computed: {
    filteredCanteens() {
      if (this.activeType === 'ALL') return this.canteens
      return this.canteens.filter((item) => item.type === this.activeType)
    },
    currentSectionTitle() {
      if (!this.currentCanteen) return ''
      const suffix = this.currentCanteen.type === 'CANTEEN' ? '窗口' : '菜品'
      return `${this.currentCanteen.name}${suffix}`
    }
  },
  onLoad() {
    this.load()
  },
  methods: {
    async load() {
      this.canteens = await fetchCanteens()
      if (this.canteens.length) {
        this.selectCanteen(this.canteens[0])
      }
    },
    selectType(type) {
      this.activeType = type
      const first = this.filteredCanteens[0]
      if (first) this.selectCanteen(first)
    },
    async selectCanteen(canteen) {
      this.currentCanteen = canteen
      this.stalls = canteen.type === 'CANTEEN' ? await fetchStalls(canteen.id) : []
    },
    goDishes(canteenId, stallId) {
      const query = [`canteenId=${canteenId}`]
      if (stallId) query.push(`stallId=${stallId}`)
      uni.navigateTo({ url: `/pages/dishes/index?${query.join('&')}` })
    }
  }
}
</script>

<style>
.head {
  padding-top: 16rpx;
}

.title {
  display: block;
  color: #241811;
  font-size: 48rpx;
  font-weight: 900;
}

.subtitle {
  display: block;
  margin-top: 10rpx;
  color: #8a7a68;
  font-size: 26rpx;
}

.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 28rpx;
}

.type-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64rpx;
  padding: 0 28rpx;
  border-radius: 999rpx;
  background: #fffdf7;
  color: #8a7a68;
  font-size: 25rpx;
  font-weight: 800;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.type-tab.active {
  background: #2b2118;
  color: #fffdf7;
}

.canteen-list {
  margin-top: 24rpx;
}

.canteen-card {
  padding: 24rpx;
  margin-bottom: 18rpx;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.canteen-main {
  display: flex;
}

.canteen-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 82rpx;
  width: 82rpx;
  height: 82rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ffd64e, #e94b35);
  color: #fffdf7;
  font-size: 34rpx;
  font-weight: 900;
}

.canteen-info {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.canteen-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.canteen-name {
  color: #241811;
  font-size: 31rpx;
  font-weight: 900;
}

.status {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #e9f8cf;
  color: #4f8f20;
  font-size: 21rpx;
  font-weight: 800;
}

.canteen-desc {
  display: block;
  margin-top: 10rpx;
  color: #6f5e4e;
  font-size: 24rpx;
  line-height: 1.4;
}

.canteen-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 14rpx;
  color: #9d8466;
  font-size: 22rpx;
}

.stall-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.stall-card,
.independent-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 116rpx;
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.independent-card {
  margin-top: 4rpx;
}

.stall-name {
  display: block;
  color: #241811;
  font-size: 29rpx;
  font-weight: 900;
}

.stall-sub {
  display: block;
  margin-top: 8rpx;
  color: #9d8466;
  font-size: 23rpx;
}

.stall-score {
  display: flex;
  align-items: baseline;
  color: #e94b35;
  font-weight: 900;
}

.stall-score text:first-child {
  font-size: 34rpx;
}

.stall-score text:last-child {
  margin-left: 4rpx;
  font-size: 21rpx;
}
</style>
