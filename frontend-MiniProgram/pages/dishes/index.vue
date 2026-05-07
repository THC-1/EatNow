<template>
  <view class="page dishes-page">
    <view class="search-panel">
      <view class="search-box">
        <view class="search-icon"></view>
        <input v-model="keyword" confirm-type="search" placeholder="搜索菜品、窗口或口味" @confirm="load" />
      </view>
      <button class="search-btn" @tap="load">搜索</button>
    </view>

    <view class="filters">
      <view class="filter-group">
        <text class="filter-label">评分</text>
        <view class="filter-row">
          <view v-for="item in scoreFilters" :key="item.value" :class="['filter', minScore === item.value ? 'active' : '']" @tap="setScore(item.value)">
            <text>{{ item.label }}</text>
          </view>
        </view>
      </view>
      <view class="filter-group">
        <text class="filter-label">价格</text>
        <view class="filter-row">
          <view v-for="item in priceFilters" :key="item.value" :class="['filter', priceRange === item.value ? 'active' : '']" @tap="setPrice(item.value)">
            <text>{{ item.label }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="rankingTitle" class="ranking-strip">
      <text>{{ rankingTitle }}</text>
      <text @tap="clearRanking">查看普通列表</text>
    </view>

    <view class="dish-list">
      <view v-for="dish in dishes" :key="dish.id" class="dish-item" @tap="goDetail(dish.id)">
        <view class="food-art dish-art">
          <image v-if="dishImage(dish)" :src="dishImage(dish)" mode="aspectFill" />
          <text v-else>{{ shortName(dish.name) }}</text>
        </view>
        <view class="dish-info">
          <view class="dish-title-row">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-price">¥{{ dish.price }}</text>
          </view>
          <text class="dish-desc">{{ dish.description }}</text>
          <view class="tag-row">
            <text v-for="tag in dish.tags" :key="tag.id || tag" class="tag">{{ tag.name || tag }}</text>
          </view>
          <view class="dish-bottom">
            <text>{{ dish.canteenName }} · {{ dish.stallName || dish.merchantName }}</text>
            <text>{{ dish.score }} 分 · {{ dish.favoriteCount || 0 }} 收藏</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="!dishes.length" class="empty">暂时没有匹配的菜品</view>
  </view>
</template>

<script>
import { fetchDishes, fetchRanking } from '../../services/student.js'
import { assetUrl } from '../../utils/request.js'

const rankingNames = {
  'top-rated': '好评榜',
  popular: '热门榜',
  'most-favorited': '收藏榜',
  'best-value': '性价比榜',
  'new-dishes': '新品榜',
  'most-feedback': '待改进榜'
}

export default {
  data() {
    return {
      keyword: '',
      canteenId: '',
      stallId: '',
      minScore: '',
      priceRange: '',
      rankingType: '',
      dishes: [],
      scoreFilters: [
        { label: '全部评分', value: '' },
        { label: '4.5 分以上', value: '4.5' },
        { label: '4.0 分以上', value: '4.0' }
      ],
      priceFilters: [
        { label: '全部价格', value: '' },
        { label: '15 元内', value: '0-15' },
        { label: '15-20 元', value: '15-20' }
      ]
    }
  },
  computed: {
    rankingTitle() {
      return this.rankingType ? rankingNames[this.rankingType] || '排行榜' : ''
    }
  },
  onLoad(query) {
    this.canteenId = query.canteenId || ''
    this.stallId = query.stallId || ''
    this.rankingType = query.ranking || ''
    this.load()
  },
  methods: {
    async load() {
      if (this.rankingType) {
        const ranks = await fetchRanking(this.rankingType)
        this.dishes = ranks.map((item) => ({
          id: item.dishId,
          name: item.dishName,
          price: item.price,
          score: item.score,
          coverImageUrl: item.coverImageUrl,
          canteenName: item.canteenName,
          merchantName: item.merchantName,
          description: `${item.favoriteCount || 0} 人收藏，榜单第 ${item.rank} 名`,
          tags: [{ id: 'rank', name: this.rankingTitle }]
        }))
        return
      }

      const params = {
        keyword: this.keyword,
        canteenId: this.canteenId,
        stallId: this.stallId,
        minScore: this.minScore,
        page: 1,
        size: 20,
        status: 'ON_SALE'
      }
      if (this.priceRange) {
        const parts = this.priceRange.split('-')
        params.minPrice = parts[0]
        params.maxPrice = parts[1]
      }
      const page = await fetchDishes(params)
      this.dishes = page.records || []
    },
    setScore(score) {
      this.rankingType = ''
      this.minScore = score
      this.load()
    },
    setPrice(range) {
      this.rankingType = ''
      this.priceRange = range
      this.load()
    },
    clearRanking() {
      this.rankingType = ''
      this.load()
    },
    shortName(name) {
      return name.slice(0, 2)
    },
    dishImage(dish) {
      const image = dish && dish.images && dish.images.length ? dish.images[0] : dish.coverImageUrl
      return assetUrl(image)
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/dish-detail/index?id=${id}` })
    }
  }
}
</script>

<style>
.search-panel {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding-top: 8rpx;
}

.search-box {
  display: flex;
  align-items: center;
  flex: 1;
  height: 80rpx;
  padding: 0 22rpx;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.search-icon {
  margin-right: 12rpx;
  font-size: 0;
}

.search-box input {
  flex: 1;
  height: 80rpx;
  color: #241811;
  font-size: 27rpx;
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 114rpx;
  height: 80rpx;
  border-radius: 28rpx;
  background: #2b2118;
  color: #fffdf7;
  font-size: 26rpx;
  font-weight: 900;
}

.filters {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 22rpx;
  padding: 20rpx;
  border-radius: 28rpx;
  background: rgba(255, 253, 247, 0.72);
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.filter-group {
  display: flex;
  align-items: flex-start;
  gap: 14rpx;
}

.filter-label {
  flex: 0 0 64rpx;
  padding-top: 14rpx;
  color: #7b6046;
  font-size: 23rpx;
  font-weight: 900;
}

.filter-row {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: 12rpx;
  min-width: 0;
}

.filter {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 128rpx;
  height: 58rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  background: #fffdf7;
  color: #8a7a68;
  font-size: 23rpx;
  font-weight: 800;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.filter.active {
  background: #e9f8cf;
  color: #4f8f20;
  border-color: rgba(127, 189, 65, 0.35);
}

.ranking-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20rpx;
  padding: 18rpx 22rpx;
  border-radius: 22rpx;
  background: #fff1d1;
  color: #7b6046;
  font-size: 24rpx;
  font-weight: 800;
}

.ranking-strip text:last-child {
  color: #e94b35;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 24rpx;
}

.dish-item {
  display: flex;
  padding: 20rpx;
  border-radius: 30rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.dish-art {
  display: flex;
  align-items: flex-end;
  flex: 0 0 164rpx;
  width: 164rpx;
  height: 164rpx;
  padding: 18rpx;
  color: #fffdf7;
  font-size: 36rpx;
  font-weight: 900;
}

.dish-art image {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 0;
  width: 100%;
  height: 100%;
}

.dish-art text {
  position: relative;
  z-index: 1;
}

.dish-info {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.dish-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dish-name {
  flex: 1;
  color: #241811;
  font-size: 30rpx;
  font-weight: 900;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.dish-price {
  margin-left: 16rpx;
  color: #e94b35;
  font-size: 29rpx;
  font-weight: 900;
}

.dish-desc {
  display: -webkit-box;
  margin-top: 8rpx;
  color: #6f5e4e;
  font-size: 24rpx;
  line-height: 1.35;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}

.tag {
  padding: 7rpx 12rpx;
  border-radius: 999rpx;
  background: #e9f8cf;
  color: #4f8f20;
  font-size: 20rpx;
  font-weight: 800;
}

.dish-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14rpx;
  color: #9d8466;
  font-size: 21rpx;
}
</style>
