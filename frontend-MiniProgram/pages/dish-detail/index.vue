<template>
  <view class="page detail-page">
    <view class="cover food-art">
      <text>{{ shortName(dish.name || '') }}</text>
    </view>

    <view class="detail-card">
      <view class="title-row">
        <view>
          <text class="dish-name">{{ dish.name }}</text>
          <text class="place">{{ dish.canteenName }} · {{ dish.stallName || dish.merchantName }}</text>
        </view>
        <text class="price">¥{{ dish.price }}</text>
      </view>

      <view class="score-grid">
        <view class="score-item">
          <text>{{ dish.score || 0 }}</text>
          <text>综合评分</text>
        </view>
        <view class="score-item">
          <text>{{ dish.tasteScore || 0 }}</text>
          <text>口味</text>
        </view>
        <view class="score-item">
          <text>{{ dish.portionScore || 0 }}</text>
          <text>分量</text>
        </view>
        <view class="score-item">
          <text>{{ dish.valueScore || 0 }}</text>
          <text>性价比</text>
        </view>
      </view>

      <text class="desc">{{ dish.description }}</text>

      <view class="tag-row">
        <text v-for="tag in dish.tags" :key="tag.id || tag" class="tag">{{ tag.name || tag }}</text>
      </view>

      <view class="action-row">
        <button class="ghost-btn" @tap="favorite">{{ favoriteState.isFavorite ? '取消收藏' : '收藏' }}</button>
        <button class="ghost-btn" @tap="wantEat">想吃</button>
        <button class="ghost-btn" @tap="openFeedback">留言</button>
        <button class="primary-btn" @tap="openReview">评价</button>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">同学评价</text>
        <text class="section-link">{{ reviews.length }} 条</text>
      </view>
      <view class="review-list">
        <view v-for="review in reviews" :key="review.id" class="review-card">
          <view class="review-head">
            <text class="review-user">{{ review.userNickname }}</text>
            <text class="review-score">{{ review.overallScore }} 分</text>
          </view>
          <text class="review-content">{{ review.content }}</text>
          <view class="review-foot">
            <text>{{ review.createdAt }}</text>
            <text>{{ review.likeCount || 0 }} 赞</text>
          </view>
        </view>
      </view>
      <view v-if="!reviews.length" class="empty">还没有评价，等你来开第一口。</view>
    </view>

    <view v-if="showReview" class="review-mask">
      <view class="review-panel">
        <view class="review-title-row">
          <text class="review-title">{{ formMode === 'review' ? '写评价' : '给商家留言' }}</text>
          <text class="close" @tap="showReview = false">关闭</text>
        </view>
        <view v-if="formMode === 'review'" class="form-row">
          <text>综合评分</text>
          <slider :value="reviewForm.overallScore" min="1" max="5" step="0.5" activeColor="#e94b35" @change="setScore" />
          <text class="score-value">{{ reviewForm.overallScore }}</text>
        </view>
        <textarea v-model="reviewForm.content" class="textarea" :placeholder="formMode === 'review' ? '味道、分量、排队情况都可以说说' : '比如分量、口味、卫生、服务方面的建议'" maxlength="160" />
        <view v-if="formMode === 'review'" class="image-row">
          <view v-for="(image, index) in reviewForm.images" :key="image" class="image-chip">
            <text>图{{ index + 1 }}</text>
            <text @tap="removeReviewImage(index)">删除</text>
          </view>
          <button class="ghost-btn image-btn" @tap="chooseReviewImage">上传图片</button>
        </view>
        <view v-if="formMode === 'review'" class="anonymous-row" @tap="reviewForm.isAnonymous = !reviewForm.isAnonymous">
          <view :class="['check', reviewForm.isAnonymous ? 'checked' : '']"></view>
          <text>匿名发布</text>
        </view>
        <button class="primary-btn submit" @tap="submitForm">{{ formMode === 'review' ? '发布评价' : '提交留言' }}</button>
      </view>
    </view>
  </view>
</template>

<script>
import { checkFavorite, createEatList, createFavorite, createFeedback, createReview, deleteFavorite, fetchDishDetail, fetchFavorites, fetchReviews, uploadStudentImage } from '../../services/student.js'
import { hasToken } from '../../utils/request.js'

export default {
  data() {
    return {
      id: '',
      dish: { tags: [] },
      reviews: [],
      favoriteState: {
        isFavorite: false,
        id: null
      },
      showReview: false,
      formMode: 'review',
      reviewForm: {
        overallScore: 5,
        content: '',
        images: [],
        isAnonymous: false
      }
    }
  },
  onLoad(query) {
    this.id = query.id
    this.load()
  },
  methods: {
    async load() {
      const [dish, reviewPage] = await Promise.all([fetchDishDetail(this.id), fetchReviews(this.id)])
      this.dish = dish || { tags: [] }
      this.reviews = reviewPage.records || []
      this.loadFavoriteState()
    },
    requireLogin() {
      if (hasToken()) return true
      uni.showToast({ title: '请先在我的页面登录', icon: 'none' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/mine/index' })
      }, 500)
      return false
    },
    async loadFavoriteState() {
      if (!hasToken() || !this.id) {
        this.favoriteState = { isFavorite: false, id: null }
        return
      }
      try {
        const state = await checkFavorite(this.id)
        let favoriteId = state.favoriteId || null
        if (state.isFavorite) {
          if (!favoriteId) {
            const page = await fetchFavorites({ size: 50 })
            const matched = (page.records || []).find((item) => String(item.targetId) === String(this.id))
            favoriteId = matched ? matched.id : null
          }
        }
        this.favoriteState = { isFavorite: !!state.isFavorite, id: favoriteId }
      } catch (error) {
        this.favoriteState = { isFavorite: false, id: null }
      }
    },
    shortName(name) {
      return name ? name.slice(0, 2) : '饭'
    },
    async favorite() {
      if (!this.requireLogin()) return
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
        uni.showToast({ title: '已取消收藏', icon: 'success' })
        return
      }
      const data = await createFavorite(this.dish.id)
      this.favoriteState = { isFavorite: true, id: data.id }
      uni.showToast({ title: '已收藏', icon: 'success' })
    },
    async wantEat() {
      if (!this.requireLogin()) return
      await createEatList(this.dish.id)
      uni.showToast({ title: '已加入想吃', icon: 'success' })
    },
    openReview() {
      this.formMode = 'review'
      this.showReview = true
    },
    openFeedback() {
      this.formMode = 'feedback'
      this.showReview = true
    },
    setScore(event) {
      this.reviewForm.overallScore = event.detail.value
    },
    chooseReviewImage() {
      if (!this.requireLogin()) return
      if (this.reviewForm.images.length >= 3) {
        uni.showToast({ title: '最多上传 3 张', icon: 'none' })
        return
      }
      uni.chooseImage({
        count: 3 - this.reviewForm.images.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (result) => {
          try {
            uni.showLoading({ title: '上传中' })
            for (const filePath of result.tempFilePaths) {
              const data = await uploadStudentImage(filePath, 'review')
              this.reviewForm.images.push(data.url)
            }
            uni.hideLoading()
          } catch (error) {
            uni.hideLoading()
            uni.showToast({ title: error.message || '上传失败', icon: 'none' })
          }
        }
      })
    },
    removeReviewImage(index) {
      this.reviewForm.images.splice(index, 1)
    },
    async submitForm() {
      if (!this.requireLogin()) return
      if (!this.reviewForm.content.trim()) {
        uni.showToast({ title: '写点真实感受吧', icon: 'none' })
        return
      }
      if (this.formMode === 'feedback') {
        await createFeedback({
          targetType: 'DISH',
          targetId: this.dish.id,
          feedbackType: 'OTHER',
          content: this.reviewForm.content
        })
        this.reviewForm.content = ''
        this.showReview = false
        uni.showToast({ title: '留言已提交', icon: 'success' })
        return
      }
      const score = Number(this.reviewForm.overallScore)
      await createReview({
        targetType: 'DISH',
        targetId: this.dish.id,
        overallScore: score,
        tasteScore: score,
        portionScore: score,
        valueScore: score,
        content: this.reviewForm.content,
        images: this.reviewForm.images,
        isAnonymous: this.reviewForm.isAnonymous
      })
      this.reviews.unshift({
        id: Date.now(),
        userNickname: this.reviewForm.isAnonymous ? '匿名同学' : '我',
        overallScore: score,
        content: this.reviewForm.content,
        likeCount: 0,
        createdAt: '刚刚'
      })
      this.reviewForm.content = ''
      this.reviewForm.images = []
      this.showReview = false
      uni.showToast({ title: '发布成功', icon: 'success' })
    }
  }
}
</script>

<style>
.cover {
  display: flex;
  align-items: flex-end;
  min-height: 330rpx;
  padding: 36rpx;
  color: #fffdf7;
  font-size: 70rpx;
  font-weight: 900;
  border-radius: 38rpx;
}

.detail-card {
  margin-top: -34rpx;
  padding: 30rpx;
  border-radius: 32rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.title-row {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}

.dish-name {
  display: block;
  color: #241811;
  font-size: 42rpx;
  font-weight: 900;
}

.place {
  display: block;
  margin-top: 10rpx;
  color: #8a7a68;
  font-size: 25rpx;
}

.price {
  color: #e94b35;
  font-size: 40rpx;
  font-weight: 900;
}

.score-grid {
  display: flex;
  margin-top: 28rpx;
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
  font-size: 31rpx;
  font-weight: 900;
}

.score-item text:last-child {
  display: block;
  margin-top: 6rpx;
  color: #8a7a68;
  font-size: 21rpx;
  font-weight: 700;
}

.desc {
  display: block;
  margin-top: 24rpx;
  color: #5d4a3b;
  font-size: 27rpx;
  line-height: 1.55;
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

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.review-card {
  padding: 24rpx;
  border-radius: 26rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.review-head,
.review-foot,
.review-title-row,
.form-row,
.anonymous-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.review-user {
  color: #241811;
  font-size: 27rpx;
  font-weight: 900;
}

.review-score {
  color: #e94b35;
  font-size: 25rpx;
  font-weight: 900;
}

.review-content {
  display: block;
  margin-top: 14rpx;
  color: #5d4a3b;
  font-size: 25rpx;
  line-height: 1.45;
}

.review-foot {
  margin-top: 16rpx;
  color: #9d8466;
  font-size: 21rpx;
}

.review-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 10;
  display: flex;
  align-items: flex-end;
  background: rgba(43, 33, 24, 0.38);
}

.review-panel {
  width: 100%;
  padding: 32rpx 28rpx 46rpx;
  border-radius: 34rpx 34rpx 0 0;
  background: #fffdf7;
}

.review-title {
  color: #241811;
  font-size: 34rpx;
  font-weight: 900;
}

.close {
  color: #e94b35;
  font-size: 25rpx;
  font-weight: 800;
}

.form-row {
  margin-top: 24rpx;
  color: #5d4a3b;
  font-size: 25rpx;
  font-weight: 800;
}

.form-row slider {
  flex: 1;
}

.score-value {
  width: 54rpx;
  color: #e94b35;
  text-align: right;
}

.textarea {
  width: 100%;
  height: 180rpx;
  margin-top: 20rpx;
  padding: 20rpx;
  border-radius: 24rpx;
  background: #fff7ea;
  color: #241811;
  font-size: 26rpx;
}

.image-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.image-chip {
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-height: 56rpx;
  padding: 0 16rpx;
  border-radius: 18rpx;
  background: #fff7ea;
  color: #7b6046;
  font-size: 22rpx;
  font-weight: 800;
}

.image-chip text:last-child {
  color: #e94b35;
}

.image-btn {
  width: 160rpx;
  height: 56rpx;
  border-radius: 18rpx;
  font-size: 22rpx;
}

.anonymous-row {
  justify-content: flex-start;
  gap: 12rpx;
  margin-top: 20rpx;
  color: #5d4a3b;
  font-size: 25rpx;
}

.check {
  width: 34rpx;
  height: 34rpx;
  border-radius: 10rpx;
  border: 3rpx solid rgba(43, 33, 24, 0.22);
}

.check.checked {
  background: #7fbd41;
  border-color: #7fbd41;
}

.submit {
  margin-top: 26rpx;
}
</style>
