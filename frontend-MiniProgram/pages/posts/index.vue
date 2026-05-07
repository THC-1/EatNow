<template>
  <view class="page posts-page">
    <view class="share-hero">
      <view>
        <text class="eyebrow">Campus Bites</text>
        <text class="hero-title">同学今天吃到了什么</text>
        <text class="hero-sub">真实的一口饭，比菜单更会说话。</text>
      </view>
      <button class="post-btn" @tap="goEditor">发布</button>
    </view>

    <view class="search-panel">
      <view class="search-box">
        <view class="search-icon"></view>
        <input v-model="keyword" confirm-type="search" placeholder="搜食物、店铺或分享内容" @confirm="reload" />
      </view>
      <button class="search-btn" @tap="reload">搜索</button>
    </view>

    <view class="sort-row">
      <view v-for="item in sorts" :key="item.value" :class="['sort-chip', sortBy === item.value ? 'active' : '']" @tap="setSort(item.value)">
        <text>{{ item.label }}</text>
      </view>
    </view>

    <scroll-view scroll-x class="filters" show-scrollbar="false">
      <view class="filter-row">
        <view :class="['filter', !canteenId ? 'active' : '']" @tap="setCanteen('')">全部地点</view>
        <view v-for="item in canteens" :key="item.id" :class="['filter', String(canteenId) === String(item.id) ? 'active' : '']" @tap="setCanteen(item.id)">
          <text>{{ item.name }}</text>
        </view>
      </view>
    </scroll-view>

    <scroll-view scroll-x class="filters compact" show-scrollbar="false">
      <view class="filter-row">
        <view :class="['filter', !categoryId ? 'active' : '']" @tap="setCategory('')">全部分类</view>
        <view v-for="item in categories" :key="item.id" :class="['filter', String(categoryId) === String(item.id) ? 'active' : '']" @tap="setCategory(item.id)">
          <text>{{ item.name }}</text>
        </view>
      </view>
    </scroll-view>

    <scroll-view scroll-x class="filters compact" show-scrollbar="false">
      <view class="filter-row">
        <view :class="['filter', !tagId ? 'active' : '']" @tap="setTag('')">全部口味</view>
        <view v-for="item in tags" :key="item.id" :class="['filter', String(tagId) === String(item.id) ? 'active' : '']" @tap="setTag(item.id)">
          <text>{{ item.name }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="post-list">
      <view v-for="post in posts" :key="post.id" :class="['post-card', { 'no-image': !firstImage(post) }]" @tap="goDetail(post.id)">
        <view class="post-head">
          <view class="avatar">
            <image v-if="post.userAvatar" :src="imageUrl(post.userAvatar)" mode="aspectFill" />
            <text v-else>{{ shortName(post.userNickname || '同学') }}</text>
          </view>
          <view class="post-author">
            <text class="author-name">{{ post.userNickname || '同学' }}</text>
            <text class="post-time">{{ formatTime(post.createdAt) }}</text>
          </view>
          <text v-if="post.isJoinLottery" class="lottery-mark">进抽签池</text>
        </view>

        <view class="post-cover" v-if="firstImage(post)">
          <image :src="firstImage(post)" mode="aspectFit" />
        </view>

        <text class="post-title">{{ post.title || post.foodName }}</text>
        <text class="post-content">{{ post.content }}</text>

        <view class="tag-row">
          <text v-for="tag in normalizedTags(post.tags)" :key="tag.id || tag.name" class="tag">{{ tag.name }}</text>
        </view>

        <view class="post-meta">
          <text>{{ post.shopName || post.stallName || post.canteenName }}</text>
          <text>{{ priceText(post.price) }} · {{ post.score || 0 }} 分</text>
        </view>

        <view class="post-actions">
          <text>{{ post.viewCount || 0 }} 浏览</text>
          <text :class="['tap-action', post.isLiked ? 'active' : '']" @tap.stop="toggleLike(post)">{{ post.isLiked ? '已赞' : '点赞' }} {{ post.likeCount || 0 }}</text>
          <text>{{ post.favoriteCount || 0 }} 收藏</text>
          <text>{{ post.commentCount || 0 }} 评论</text>
        </view>
      </view>
    </view>

    <view v-if="!posts.length && !loading" class="empty">还没有同学分享，来发布第一口。</view>
    <view v-if="loading" class="empty">加载中...</view>
    <view v-if="posts.length && !hasMore" class="end-text">已经到底啦</view>
  </view>
</template>

<script>
import { fetchCanteens, fetchCategories, fetchPosts, fetchTags, likePost, unlikePost } from '../../services/student.js'
import { assetUrl, hasToken } from '../../utils/request.js'

export default {
  data() {
    return {
      keyword: '',
      sortBy: 'latest',
      canteenId: '',
      categoryId: '',
      tagId: '',
      page: 1,
      size: 10,
      total: 0,
      loading: false,
      posts: [],
      canteens: [],
      categories: [],
      tags: [],
      sorts: [
        { label: '最新', value: 'latest' },
        { label: '热门', value: 'popular' }
      ]
    }
  },
  computed: {
    hasMore() {
      return this.posts.length < this.total
    }
  },
  onLoad() {
    this.loadBase()
    this.reload()
  },
  onPullDownRefresh() {
    this.reload().finally(() => uni.stopPullDownRefresh())
  },
  onReachBottom() {
    if (!this.loading && this.hasMore) {
      this.page += 1
      this.loadPosts(true)
    }
  },
  methods: {
    async loadBase() {
      try {
        const [canteens, categories, tags] = await Promise.all([fetchCanteens(), fetchCategories(), fetchTags()])
        this.canteens = canteens || []
        this.categories = categories || []
        this.tags = tags || []
      } catch (error) {
        uni.showToast({ title: error.message || '筛选加载失败', icon: 'none' })
      }
    },
    reload() {
      this.page = 1
      this.total = 0
      return this.loadPosts(false)
    },
    async loadPosts(append) {
      this.loading = true
      try {
        const page = await fetchPosts({
          keyword: this.keyword,
          sortBy: this.sortBy,
          canteenId: this.canteenId,
          categoryId: this.categoryId,
          tagId: this.tagId,
          page: this.page,
          size: this.size
        })
        const records = page.records || []
        this.posts = append ? this.posts.concat(records) : records
        this.total = page.total || this.posts.length
      } catch (error) {
        uni.showToast({ title: error.message || '帖子加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    requireLogin() {
      if (hasToken()) return true
      uni.showToast({ title: '请先在我的页面登录', icon: 'none' })
      setTimeout(() => uni.switchTab({ url: '/pages/mine/index' }), 500)
      return false
    },
    setSort(value) {
      this.sortBy = value
      this.reload()
    },
    setCanteen(id) {
      this.canteenId = id
      this.reload()
    },
    setCategory(id) {
      this.categoryId = id
      this.reload()
    },
    setTag(id) {
      this.tagId = id
      this.reload()
    },
    async toggleLike(post) {
      if (!this.requireLogin()) return
      try {
        const data = post.isLiked ? await unlikePost(post.id) : await likePost(post.id)
        post.isLiked = !post.isLiked
        post.likeCount = data.likeCount === undefined ? Math.max(0, (post.likeCount || 0) + (post.isLiked ? 1 : -1)) : data.likeCount
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' })
      }
    },
    goEditor() {
      if (!this.requireLogin()) return
      uni.navigateTo({ url: '/pages/post-editor/index' })
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/post-detail/index?id=${id}` })
    },
    firstImage(post) {
      return post.images && post.images.length ? assetUrl(post.images[0]) : ''
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
      return price === undefined || price === null || price === '' ? '未写价格' : `¥${price}`
    },
    formatTime(value) {
      return value ? value.replace('T', ' ').slice(0, 16) : '刚刚'
    }
  }
}
</script>

<style>
.posts-page {
  padding-top: 24rpx;
}

.share-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 30rpx;
  border-radius: 34rpx;
  background:
    radial-gradient(circle at 82% 18%, rgba(255, 214, 78, 0.58), transparent 180rpx),
    linear-gradient(135deg, #2b2118 0%, #6f472a 100%);
  color: #fffdf7;
}

.eyebrow {
  display: block;
  color: #ffd64e;
  font-size: 22rpx;
  font-weight: 900;
}

.hero-title {
  display: block;
  margin-top: 10rpx;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.15;
}

.hero-sub {
  display: block;
  margin-top: 12rpx;
  color: rgba(255, 253, 247, 0.72);
  font-size: 24rpx;
}

.post-btn {
  width: 112rpx;
  height: 62rpx;
  border-radius: 999rpx;
  background: #ffd64e;
  color: #241811;
  font-size: 25rpx;
  font-weight: 900;
}

.search-panel {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-top: 22rpx;
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
  width: 114rpx;
  height: 80rpx;
  border-radius: 28rpx;
  background: #2b2118;
  color: #fffdf7;
  font-size: 26rpx;
  font-weight: 900;
}

.sort-row {
  display: flex;
  gap: 14rpx;
  margin-top: 20rpx;
}

.sort-chip {
  flex: 1;
  height: 64rpx;
  border-radius: 999rpx;
  background: #fffdf7;
  color: #8a7a68;
  font-size: 25rpx;
  font-weight: 900;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.sort-chip.active {
  background: #e94b35;
  color: #fffdf7;
}

.filters {
  width: 100%;
  margin-top: 18rpx;
  white-space: nowrap;
}

.filters.compact {
  margin-top: 12rpx;
}

.filter-row {
  display: inline-flex;
  gap: 12rpx;
  padding-right: 28rpx;
}

.filter {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 58rpx;
  padding: 0 20rpx;
  border-radius: 999rpx;
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

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-top: 22rpx;
}

.post-card {
  padding: 24rpx;
  border-radius: 30rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.post-head {
  display: flex;
  align-items: center;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  border-radius: 22rpx;
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

.post-author {
  flex: 1;
  min-width: 0;
  margin-left: 16rpx;
}

.author-name {
  display: block;
  color: #241811;
  font-size: 27rpx;
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

.post-cover {
  margin-top: 20rpx;
  height: 520rpx;
  border-radius: 26rpx;
  overflow: hidden;
}

.post-cover image {
  width: 100%;
  height: 100%;
}

.post-title {
  display: block;
  margin-top: 20rpx;
  color: #241811;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.22;
}

.post-content {
  display: -webkit-box;
  margin-top: 12rpx;
  color: #5d4a3b;
  font-size: 26rpx;
  line-height: 1.45;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-card.no-image .post-title {
  margin-top: 24rpx;
}

.post-card.no-image .post-content {
  -webkit-line-clamp: 3;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
}

.tag {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #e9f8cf;
  color: #4f8f20;
  font-size: 21rpx;
  font-weight: 800;
}

.post-meta,
.post-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  margin-top: 18rpx;
  color: #9d8466;
  font-size: 22rpx;
}

.post-meta text,
.post-actions text {
  min-width: 0;
}

.post-actions {
  flex-wrap: wrap;
}

.tap-action {
  color: #7b6046;
  font-weight: 900;
}

.tap-action.active {
  color: #e94b35;
}

.end-text {
  padding: 28rpx;
  color: #9d8466;
  text-align: center;
  font-size: 23rpx;
}
</style>
