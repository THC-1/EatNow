<template>
  <view class="page post-detail-page">
    <view v-if="firstImage(post)" class="cover-image">
      <image :src="firstImage(post)" mode="aspectFit" />
    </view>

    <view :class="['detail-card', { 'no-cover': !firstImage(post) }]">
      <view class="author-row">
        <view class="avatar">
          <image v-if="post.userAvatar" :src="imageUrl(post.userAvatar)" mode="aspectFill" />
          <text v-else>{{ shortName(post.userNickname || '同学') }}</text>
        </view>
        <view class="author-info">
          <text class="author-name">{{ post.userNickname || '同学' }}</text>
          <text class="post-time">{{ formatTime(post.createdAt) }}</text>
        </view>
        <text v-if="post.isJoinLottery" class="lottery-mark">已进抽签池</text>
      </view>

      <text class="post-title">{{ post.title || post.foodName }}</text>
      <text class="food-name">{{ post.foodName }}</text>
      <text class="shop-name">{{ post.shopName || post.stallName || post.canteenName }}</text>

      <view class="score-grid">
        <view class="score-item">
          <text>{{ post.score || 0 }}</text>
          <text>评分</text>
        </view>
        <view class="score-item">
          <text>{{ priceText(post.price) }}</text>
          <text>价格</text>
        </view>
        <view class="score-item">
          <text>{{ post.viewCount || 0 }}</text>
          <text>浏览</text>
        </view>
      </view>

      <text class="content">{{ post.content }}</text>

      <view v-if="post.images && post.images.length > 1" class="image-grid">
        <image v-for="image in post.images.slice(1)" :key="image" :src="imageUrl(image)" mode="aspectFit" />
      </view>

      <view class="tag-row">
        <text v-for="tag in normalizedTags(post.tags)" :key="tag.id || tag.name" class="tag">{{ tag.name }}</text>
      </view>

      <view class="action-row">
        <button class="ghost-btn" @tap="toggleLike">{{ post.isLiked ? '取消点赞' : '点赞' }} {{ post.likeCount || 0 }}</button>
        <button class="ghost-btn" @tap="toggleFavorite">{{ favoriteState.isFavorite ? '取消收藏' : '收藏' }}</button>
        <button v-if="fromMine" class="ghost-btn danger" @tap="removePost">删除</button>
      </view>
    </view>
  </view>
</template>

<script>
import { checkFavorite, createFavorite, deleteFavorite, deletePost, fetchFavorites, fetchPostDetail, likePost, unlikePost } from '../../services/student.js'
import { assetUrl, hasToken } from '../../utils/request.js'

export default {
  data() {
    return {
      id: '',
      fromMine: false,
      post: {},
      favoriteState: {
        isFavorite: false,
        id: null
      }
    }
  },
  onLoad(query) {
    this.id = query.id
    this.fromMine = query.from === 'mine'
    this.load()
  },
  methods: {
    async load() {
      try {
        this.post = await fetchPostDetail(this.id) || {}
        this.loadFavoriteState()
      } catch (error) {
        uni.showToast({ title: error.message || '帖子加载失败', icon: 'none' })
      }
    },
    requireLogin() {
      if (hasToken()) return true
      uni.showToast({ title: '请先在我的页面登录', icon: 'none' })
      setTimeout(() => uni.switchTab({ url: '/pages/mine/index' }), 500)
      return false
    },
    async loadFavoriteState() {
      if (!hasToken() || !this.id) {
        this.favoriteState = { isFavorite: false, id: null }
        return
      }
      try {
        const state = await checkFavorite(this.id, 'POST')
        let favoriteId = state.favoriteId || null
        if (state.isFavorite && !favoriteId) {
          const page = await fetchFavorites({ targetType: 'POST', size: 50 })
          const matched = (page.records || []).find((item) => String(item.targetId) === String(this.id))
          favoriteId = matched ? matched.id : null
        }
        this.favoriteState = { isFavorite: !!state.isFavorite, id: favoriteId }
      } catch (error) {
        this.favoriteState = { isFavorite: false, id: null }
      }
    },
    async toggleLike() {
      if (!this.requireLogin()) return
      try {
        const data = this.post.isLiked ? await unlikePost(this.post.id) : await likePost(this.post.id)
        this.post.isLiked = !this.post.isLiked
        this.post.likeCount = data.likeCount === undefined ? Math.max(0, (this.post.likeCount || 0) + (this.post.isLiked ? 1 : -1)) : data.likeCount
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' })
      }
    },
    async toggleFavorite() {
      if (!this.requireLogin()) return
      try {
        if (this.favoriteState.isFavorite) {
          if (!this.favoriteState.id) {
            await this.loadFavoriteState()
          }
          if (!this.favoriteState.id) {
            uni.showToast({ title: '未找到收藏记录', icon: 'none' })
            return
          }
          await deleteFavorite(this.favoriteState.id)
          this.favoriteState = { isFavorite: false, id: null }
          this.post.favoriteCount = Math.max(0, (this.post.favoriteCount || 0) - 1)
          uni.showToast({ title: '已取消收藏', icon: 'success' })
          return
        }
        const data = await createFavorite(this.post.id, 'POST')
        this.favoriteState = { isFavorite: true, id: data.id }
        this.post.favoriteCount = (this.post.favoriteCount || 0) + 1
        uni.showToast({ title: '已收藏', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' })
      }
    },
    removePost() {
      if (!this.requireLogin()) return
      uni.showModal({
        title: '删除分享',
        content: '删除后这条分享将不再展示，确定删除吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await deletePost(this.post.id)
            uni.showToast({ title: '已删除', icon: 'success' })
            setTimeout(() => {
              const pages = getCurrentPages()
              if (pages.length > 1) {
                uni.navigateBack()
              } else {
                uni.switchTab({ url: '/pages/posts/index' })
              }
            }, 450)
          } catch (error) {
            uni.showToast({ title: error.message || '删除失败', icon: 'none' })
          }
        }
      })
    },
    firstImage(post) {
      return post.images && post.images.length ? this.imageUrl(post.images[0]) : ''
    },
    imageUrl(image) {
      return assetUrl(image)
    },
    normalizedTags(tags = []) {
      return tags.map((tag) => ({ id: tag.tagId || tag.id || tag.name || tag, name: tag.tagName || tag.name || tag }))
    },
    shortName(name) {
      return name ? name.slice(0, 2) : '饭'
    },
    priceText(price) {
      return price === undefined || price === null || price === '' ? '-' : `¥${price}`
    },
    formatTime(value) {
      return value ? value.replace('T', ' ').slice(0, 16) : '刚刚'
    }
  }
}
</script>

<style>
.cover,
.cover-image {
  display: flex;
  align-items: flex-end;
  min-height: 620rpx;
  border-radius: 38rpx;
  overflow: hidden;
}

.cover-image {
  background: #fff7ea;
}

.cover {
  padding: 36rpx;
  color: #fffdf7;
  font-size: 70rpx;
  font-weight: 900;
}

.cover-image image {
  width: 100%;
  height: 100%;
}

.detail-card {
  margin-top: -34rpx;
  padding: 30rpx;
  border-radius: 32rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.detail-card.no-cover {
  margin-top: 0;
}

.author-row {
  display: flex;
  align-items: center;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76rpx;
  height: 76rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ffd64e, #e94b35);
  color: #fffdf7;
  font-size: 24rpx;
  font-weight: 900;
  overflow: hidden;
}

.avatar image {
  width: 100%;
  height: 100%;
}

.author-info {
  flex: 1;
  min-width: 0;
  margin-left: 16rpx;
}

.author-name {
  display: block;
  color: #241811;
  font-size: 28rpx;
  font-weight: 900;
}

.post-time {
  display: block;
  margin-top: 6rpx;
  color: #9d8466;
  font-size: 21rpx;
}

.lottery-mark {
  padding: 9rpx 14rpx;
  border-radius: 999rpx;
  background: #fff1d1;
  color: #e94b35;
  font-size: 21rpx;
  font-weight: 900;
}

.post-title {
  display: block;
  margin-top: 26rpx;
  color: #241811;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.18;
}

.food-name {
  display: block;
  margin-top: 14rpx;
  color: #e94b35;
  font-size: 31rpx;
  font-weight: 900;
}

.shop-name {
  display: block;
  margin-top: 8rpx;
  color: #8a7a68;
  font-size: 25rpx;
}

.score-grid {
  display: flex;
  margin-top: 26rpx;
  border-radius: 26rpx;
  background: #fff7ea;
  overflow: hidden;
}

.score-item {
  flex: 1;
  padding: 20rpx 8rpx;
  text-align: center;
  border-right: 2rpx solid rgba(43, 33, 24, 0.06);
}

.score-item:last-child {
  border-right: 0;
}

.score-item text:first-child {
  display: block;
  color: #e94b35;
  font-size: 30rpx;
  font-weight: 900;
}

.score-item text:last-child {
  display: block;
  margin-top: 6rpx;
  color: #8a7a68;
  font-size: 21rpx;
  font-weight: 700;
}

.content {
  display: block;
  margin-top: 24rpx;
  color: #5d4a3b;
  font-size: 28rpx;
  line-height: 1.6;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 22rpx;
}

.image-grid image {
  width: 48%;
  height: 320rpx;
  border-radius: 22rpx;
  background: #fff7ea;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 20rpx;
}

.tag {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #e9f8cf;
  color: #4f8f20;
  font-size: 22rpx;
  font-weight: 800;
}

.action-row {
  display: flex;
  gap: 14rpx;
  margin-top: 30rpx;
}

.action-row button {
  flex: 1;
}

.danger {
  color: #e94b35;
  border-color: rgba(233, 75, 53, 0.2);
}
</style>
