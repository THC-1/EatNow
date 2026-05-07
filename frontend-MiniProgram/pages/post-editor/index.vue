<template>
  <view class="page post-editor-page">
    <view class="editor-hero">
      <text class="eyebrow">New Bite</text>
      <text class="hero-title">把这一口分享出去</text>
      <text class="hero-sub">写给正在纠结吃什么的同学。</text>
    </view>

    <view class="form-card">
      <view class="form-row">
        <text class="label">食物</text>
        <input v-model="form.foodName" placeholder="例如：麻辣烫" />
      </view>
      <view class="shop-search">
        <view class="form-row shop-row">
          <text class="label">店铺</text>
          <input
            v-model="form.shopName"
            confirm-type="search"
            placeholder="搜索或填写店铺名"
            @input="handleShopInput"
            @focus="openPlaceSuggestions"
            @confirm="searchPlaceNow"
          />
        </view>
        <view v-if="selectedPlaceMeta" class="place-chip-row">
          <text class="place-chip">{{ selectedPlaceMeta }}</text>
          <text class="place-clear" @tap="clearSelectedPlace">解绑</text>
        </view>
        <view v-if="showPlaceSuggestions" class="place-panel">
          <view v-if="searchingPlaces" class="place-status">搜索中</view>
          <view v-for="place in placeSuggestions" :key="place.placeType + '-' + place.id" class="place-option" @tap="selectPlace(place)">
            <view class="place-option-main">
              <text class="place-name">{{ place.name }}</text>
              <text class="place-badge">{{ placeBadge(place) }}</text>
            </view>
            <text class="place-meta">{{ placeMeta(place) }}</text>
          </view>
          <view v-if="!searchingPlaces && placeSearchKeyword && !placeSuggestions.length" class="place-status">
            没有匹配，按填写名称发布
          </view>
        </view>
      </view>
      <view class="form-row">
        <text class="label">标题</text>
        <input v-model="form.title" placeholder="不填则使用食物名" />
      </view>
      <view class="form-row">
        <text class="label">价格</text>
        <input v-model="form.price" type="digit" placeholder="选填" />
      </view>
      <view class="form-row">
        <text class="label">评分</text>
        <slider :value="form.score" min="1" max="5" step="0.5" activeColor="#e94b35" @change="setScore" />
        <text class="score-value">{{ form.score }}</text>
      </view>

      <view class="form-row">
        <text class="label">分类</text>
        <picker :range="categories" range-key="name" @change="chooseCategory">
          <view class="picker-value">{{ currentCategoryName }}</view>
        </picker>
      </view>

      <view class="form-row block">
        <text class="label">内容</text>
        <textarea v-model="form.content" maxlength="500" placeholder="汤底、分量、排队、性价比，都可以说说" />
      </view>

      <view class="form-row block">
        <text class="label">标签</text>
        <view class="tag-picker">
          <view v-for="tag in tags" :key="tag.id" :class="['tag-option', form.tagIds.includes(tag.id) ? 'active' : '']" @tap="toggleTag(tag.id)">
            <text>{{ tag.name }}</text>
          </view>
        </view>
      </view>

      <view class="form-row block">
        <text class="label">图片</text>
        <view class="image-row">
          <view v-for="(image, index) in form.images" :key="image" class="image-chip">
            <text>图{{ index + 1 }}</text>
            <text @tap="removeImage(index)">删除</text>
          </view>
          <button class="ghost-btn image-btn" @tap="chooseImage">上传图片</button>
        </view>
      </view>

      <view class="lottery-row">
        <view>
          <text class="lottery-title">加入抽签池</text>
          <text class="lottery-sub">打开后，其他同学随机抽签时也可能抽到这条分享。</text>
        </view>
        <switch :checked="form.isJoinLottery" color="#E94B35" @change="form.isJoinLottery = $event.detail.value" />
      </view>

      <button class="primary-btn submit" :loading="submitting" :disabled="submitting" @tap="submit">发布分享</button>
    </view>
  </view>
</template>

<script>
import { createPost, fetchCategories, fetchTags, searchPlaces, uploadStudentImage } from '../../services/student.js'
import { hasToken } from '../../utils/request.js'

function emptyForm() {
  return {
    foodName: '',
    shopName: '',
    title: '',
    content: '',
    canteenId: '',
    stallId: '',
    categoryId: '',
    price: '',
    score: 5,
    images: [],
    tagIds: [],
    isJoinLottery: false
  }
}

export default {
  data() {
    return {
      form: emptyForm(),
      categories: [],
      tags: [],
      placeSuggestions: [],
      placeSearchKeyword: '',
      placeSearchTimer: null,
      placeSearchSeq: 0,
      searchingPlaces: false,
      selectedPlaceName: '',
      selectedPlaceMeta: '',
      showPlaceSuggestions: false,
      submitting: false
    }
  },
  computed: {
    currentCategoryName() {
      const target = this.categories.find((item) => String(item.id) === String(this.form.categoryId))
      return target ? target.name : '选择分类'
    }
  },
  onLoad() {
    if (!hasToken()) {
      uni.showToast({ title: '请先在我的页面登录', icon: 'none' })
      setTimeout(() => uni.switchTab({ url: '/pages/mine/index' }), 500)
      return
    }
    this.loadBase()
  },
  onUnload() {
    this.clearPlaceSearchTimer()
  },
  methods: {
    async loadBase() {
      try {
        const [categories, tags] = await Promise.all([fetchCategories(), fetchTags()])
        this.categories = categories || []
        this.tags = tags || []
      } catch (error) {
        uni.showToast({ title: error.message || '基础数据加载失败', icon: 'none' })
      }
    },
    chooseCategory(event) {
      const index = Number(event.detail.value)
      this.form.categoryId = this.categories[index] ? this.categories[index].id : ''
    },
    handleShopInput(event) {
      const value = event.detail.value
      if (this.selectedPlaceName && value.trim() !== this.selectedPlaceName) {
        this.clearPlaceBinding()
      }
      this.queuePlaceSearch(value)
    },
    openPlaceSuggestions() {
      if (this.form.shopName.trim()) {
        this.queuePlaceSearch(this.form.shopName)
      }
    },
    searchPlaceNow(event) {
      if (event && event.detail && event.detail.value !== undefined) {
        this.form.shopName = event.detail.value
      }
      this.clearPlaceSearchTimer()
      this.loadPlaceSuggestions(this.form.shopName)
    },
    queuePlaceSearch(value) {
      const keyword = value.trim()
      this.placeSearchKeyword = keyword
      this.clearPlaceSearchTimer()
      if (!keyword) {
        this.placeSuggestions = []
        this.showPlaceSuggestions = false
        this.searchingPlaces = false
        return
      }
      this.showPlaceSuggestions = true
      this.placeSearchTimer = setTimeout(() => {
        this.loadPlaceSuggestions(keyword)
      }, 260)
    },
    async loadPlaceSuggestions(value) {
      const keyword = value.trim()
      this.placeSearchKeyword = keyword
      if (!keyword) return
      const seq = ++this.placeSearchSeq
      this.searchingPlaces = true
      this.showPlaceSuggestions = true
      try {
        const places = await searchPlaces(keyword, 8)
        if (seq !== this.placeSearchSeq) return
        this.placeSuggestions = places || []
      } catch (error) {
        if (seq !== this.placeSearchSeq) return
        this.placeSuggestions = []
      } finally {
        if (seq === this.placeSearchSeq) {
          this.searchingPlaces = false
        }
      }
    },
    selectPlace(place) {
      this.form.shopName = place.name
      this.form.canteenId = place.canteenId || ''
      this.form.stallId = place.stallId || ''
      this.selectedPlaceName = place.name
      this.selectedPlaceMeta = this.placeMeta(place)
      this.placeSuggestions = []
      this.showPlaceSuggestions = false
      this.clearPlaceSearchTimer()
    },
    clearSelectedPlace() {
      this.clearPlaceBinding()
      if (this.form.shopName.trim()) {
        this.queuePlaceSearch(this.form.shopName)
      }
    },
    clearPlaceBinding() {
      this.form.canteenId = ''
      this.form.stallId = ''
      this.selectedPlaceName = ''
      this.selectedPlaceMeta = ''
    },
    clearPlaceSearchTimer() {
      if (this.placeSearchTimer) {
        clearTimeout(this.placeSearchTimer)
        this.placeSearchTimer = null
      }
    },
    placeBadge(place) {
      if (place.placeType === 'STALL') return '窗口'
      return place.canteenType === 'CANTEEN' ? '地点' : '独立店'
    },
    placeMeta(place) {
      const parts = []
      if (place.placeType === 'STALL' && place.canteenName) parts.push(place.canteenName)
      if (place.location) parts.push(place.location)
      if (place.merchantName) parts.push(place.merchantName)
      return parts.join(' · ') || '位置待补充'
    },
    setScore(event) {
      this.form.score = event.detail.value
    },
    toggleTag(id) {
      const index = this.form.tagIds.findIndex((item) => String(item) === String(id))
      if (index >= 0) {
        this.form.tagIds.splice(index, 1)
      } else {
        this.form.tagIds.push(id)
      }
    },
    chooseImage() {
      if (this.form.images.length >= 6) {
        uni.showToast({ title: '最多上传 6 张', icon: 'none' })
        return
      }
      uni.chooseImage({
        count: 6 - this.form.images.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (result) => {
          try {
            uni.showLoading({ title: '上传中' })
            for (const filePath of result.tempFilePaths) {
              const data = await uploadStudentImage(filePath, 'post')
              this.form.images.push(data.url)
            }
            uni.hideLoading()
          } catch (error) {
            uni.hideLoading()
            uni.showToast({ title: error.message || '上传失败', icon: 'none' })
          }
        }
      })
    },
    removeImage(index) {
      this.form.images.splice(index, 1)
    },
    async submit() {
      if (this.submitting) return
      if (!this.form.foodName.trim() || !this.form.shopName.trim() || !this.form.content.trim()) {
        uni.showToast({ title: '食物、店铺和内容必填', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const payload = {
          foodName: this.form.foodName.trim(),
          shopName: this.form.shopName.trim(),
          content: this.form.content.trim(),
          title: this.form.title.trim(),
          images: this.form.images,
          tagIds: this.form.tagIds,
          isJoinLottery: this.form.isJoinLottery
        }
        if (this.form.canteenId) payload.canteenId = Number(this.form.canteenId)
        if (this.form.stallId) payload.stallId = Number(this.form.stallId)
        if (this.form.categoryId) payload.categoryId = Number(this.form.categoryId)
        if (this.form.price !== '') payload.price = Number(this.form.price)
        if (this.form.score) payload.score = Number(this.form.score)
        const data = await createPost(payload)
        uni.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => {
          uni.redirectTo({ url: `/pages/post-detail/index?id=${data.id}&from=mine` })
        }, 450)
      } catch (error) {
        uni.showToast({ title: error.message || '发布失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style>
.post-editor-page {
  padding-top: 24rpx;
}

.editor-hero {
  padding: 30rpx;
  border-radius: 34rpx;
  background:
    radial-gradient(circle at 84% 20%, rgba(127, 189, 65, 0.48), transparent 160rpx),
    linear-gradient(135deg, #2b2118 0%, #604229 100%);
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
}

.hero-sub {
  display: block;
  margin-top: 12rpx;
  color: rgba(255, 253, 247, 0.72);
  font-size: 24rpx;
}

.form-card {
  margin-top: 22rpx;
  padding: 24rpx;
  border-radius: 30rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.form-row {
  display: flex;
  align-items: center;
  min-height: 86rpx;
  border-bottom: 2rpx solid rgba(43, 33, 24, 0.06);
}

.form-row.block {
  display: block;
  padding: 20rpx 0;
}

.shop-search {
  border-bottom: 2rpx solid rgba(43, 33, 24, 0.06);
}

.shop-row {
  border-bottom: 0;
}

.place-chip-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 0 0 18rpx 120rpx;
}

.place-chip {
  max-width: 440rpx;
  min-height: 48rpx;
  padding: 0 16rpx;
  border-radius: 16rpx;
  background: #e9f8cf;
  color: #4f8f20;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 48rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.place-clear {
  min-height: 48rpx;
  color: #e94b35;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 48rpx;
}

.place-panel {
  margin: 0 0 18rpx 120rpx;
  border-radius: 22rpx;
  background: #fff7ea;
  overflow: hidden;
}

.place-option {
  padding: 18rpx;
  border-bottom: 2rpx solid rgba(43, 33, 24, 0.05);
}

.place-option-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.place-name {
  flex: 1;
  color: #241811;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.3;
}

.place-badge {
  min-width: 72rpx;
  height: 40rpx;
  border-radius: 14rpx;
  background: #241811;
  color: #fffdf7;
  font-size: 20rpx;
  font-weight: 900;
  line-height: 40rpx;
  text-align: center;
}

.place-meta {
  display: block;
  margin-top: 8rpx;
  color: #8a7a68;
  font-size: 22rpx;
  line-height: 1.35;
}

.place-status {
  min-height: 66rpx;
  padding: 0 18rpx;
  color: #8a7a68;
  font-size: 23rpx;
  font-weight: 800;
  line-height: 66rpx;
}

.label {
  width: 120rpx;
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

.form-row slider {
  flex: 1;
}

.score-value {
  width: 60rpx;
  color: #e94b35;
  text-align: right;
  font-size: 26rpx;
  font-weight: 900;
}

.form-row textarea {
  width: 100%;
  height: 210rpx;
  margin-top: 14rpx;
  padding: 18rpx;
  border-radius: 22rpx;
  background: #fff7ea;
  color: #241811;
  font-size: 26rpx;
  line-height: 1.45;
}

.tag-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.tag-option {
  height: 56rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: #fff7ea;
  color: #8a7a68;
  line-height: 56rpx;
  font-size: 23rpx;
  font-weight: 900;
}

.tag-option.active {
  background: #e9f8cf;
  color: #4f8f20;
}

.image-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
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

.lottery-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 108rpx;
}

.lottery-title {
  display: block;
  color: #241811;
  font-size: 27rpx;
  font-weight: 900;
}

.lottery-sub {
  display: block;
  width: 470rpx;
  margin-top: 8rpx;
  color: #8a7a68;
  font-size: 22rpx;
  line-height: 1.35;
}

.submit {
  margin-top: 24rpx;
}
</style>
