<template>
  <view class="page merchant-dishes">
    <view class="top-panel">
      <view>
        <text class="eyebrow">Menu Control</text>
        <text class="page-title">菜品管理</text>
      </view>
      <button class="add-btn" @tap="startCreate">新增</button>
    </view>

    <view class="tabs">
      <view v-for="item in statusFilters" :key="item.value" :class="['tab', status === item.value ? 'active' : '']" @tap="setStatus(item.value)">
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view v-if="editing" class="form-card">
      <view class="form-head">
        <text>{{ form.id ? '编辑菜品' : '新增菜品' }}</text>
        <text @tap="cancelEdit">收起</text>
      </view>
      <view class="form-row">
        <text class="label">名称</text>
        <input v-model="form.name" placeholder="例如：黑椒鸡排饭" />
      </view>
      <view class="form-row">
        <text class="label">价格</text>
        <input v-model="form.price" type="digit" placeholder="15" />
      </view>
      <view class="form-row">
        <text class="label">分类</text>
        <picker :range="categories" range-key="name" @change="chooseCategory">
          <view class="picker-value">{{ currentCategoryName }}</view>
        </picker>
      </view>
      <view class="form-row block">
        <text class="label">描述</text>
        <textarea v-model="form.description" maxlength="500" placeholder="一句话讲清楚菜品特色" />
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
            <image :src="imageUrl(image)" mode="aspectFill" />
            <text>图{{ index + 1 }}</text>
            <text @tap="removeImage(index)">删除</text>
          </view>
          <button class="ghost-btn image-btn" @tap="chooseDishImage">上传图片</button>
        </view>
      </view>
      <view class="lottery-row">
        <text>参与抽签推荐</text>
        <switch :checked="form.isJoinLottery" color="#E94B35" @change="form.isJoinLottery = $event.detail.value" />
      </view>
      <button class="primary-btn" @tap="submitDish">{{ form.id ? '保存修改' : '发布菜品' }}</button>
    </view>

    <view class="dish-list">
      <view v-for="dish in dishes" :key="dish.id" class="dish-card">
        <view class="food-art dish-art">
          <image v-if="dishImage(dish)" :src="dishImage(dish)" mode="aspectFill" />
          <text v-else>{{ shortName(dish.name) }}</text>
        </view>
        <view class="dish-body">
          <view class="dish-head">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-price">¥{{ dish.price }}</text>
          </view>
          <text class="dish-desc">{{ dish.description }}</text>
          <view class="dish-meta">
            <text>{{ statusText(dish.status) }}</text>
            <text>{{ dish.score || 0 }} 分 · {{ dish.favoriteCount || 0 }} 收藏</text>
          </view>
          <view class="actions">
            <button @tap="editDish(dish)">编辑</button>
            <button @tap="changeStatus(dish, 'ON_SALE')">上架</button>
            <button @tap="changeStatus(dish, 'SOLD_OUT')">售罄</button>
            <button @tap="changeStatus(dish, 'OFF_SHELF')">下架</button>
            <button class="danger-btn" @tap="removeDish(dish)">删除</button>
          </view>
        </view>
      </view>
    </view>

    <view v-if="!dishes.length" class="empty">还没有符合条件的菜品</view>
  </view>
</template>

<script>
import { createDish, deleteDish, fetchCategories, fetchMerchantDishes, fetchTags, updateDish, updateDishStatus, uploadMerchantImage } from '../../../services/merchant.js'
import { assetUrl } from '../../../utils/request.js'

function emptyForm() {
  return {
    id: '',
    name: '',
    description: '',
    price: '',
    categoryId: '',
    tagIds: [],
    images: [],
    isJoinLottery: true
  }
}

export default {
  data() {
    return {
      status: '',
      dishes: [],
      categories: [],
      tags: [],
      editing: false,
      form: emptyForm(),
      statusFilters: [
        { label: '全部', value: '' },
        { label: '待审核', value: 'PENDING' },
        { label: '上架', value: 'ON_SALE' },
        { label: '售罄', value: 'SOLD_OUT' },
        { label: '下架', value: 'OFF_SHELF' }
      ]
    }
  },
  computed: {
    currentCategoryName() {
      const target = this.categories.find((item) => String(item.id) === String(this.form.categoryId))
      return target ? target.name : '选择分类'
    }
  },
  onLoad() {
    this.loadBase()
    this.loadDishes()
  },
  methods: {
    async loadBase() {
      const [categories, tags] = await Promise.all([fetchCategories(), fetchTags()])
      this.categories = categories || []
      this.tags = tags || []
      if (!this.form.categoryId && this.categories.length) {
        this.form.categoryId = this.categories[0].id
      }
    },
    async loadDishes() {
      const page = await fetchMerchantDishes({ page: 1, size: 30, status: this.status })
      this.dishes = page.records || []
    },
    setStatus(status) {
      this.status = status
      this.loadDishes()
    },
    startCreate() {
      this.form = emptyForm()
      this.form.categoryId = this.categories.length ? this.categories[0].id : ''
      this.editing = true
    },
    editDish(dish) {
      this.form = {
        id: dish.id,
        name: dish.name,
        description: dish.description || '',
        price: dish.price,
        categoryId: dish.categoryId || (this.categories.length ? this.categories[0].id : ''),
        tagIds: (dish.tags || []).map((tag) => tag.id || tag),
        images: dish.images || [],
        isJoinLottery: dish.isJoinLottery !== false
      }
      this.editing = true
    },
    cancelEdit() {
      this.editing = false
      this.form = emptyForm()
    },
    chooseCategory(event) {
      const index = Number(event.detail.value)
      this.form.categoryId = this.categories[index] ? this.categories[index].id : ''
    },
    toggleTag(id) {
      const index = this.form.tagIds.findIndex((item) => String(item) === String(id))
      if (index >= 0) {
        this.form.tagIds.splice(index, 1)
      } else {
        this.form.tagIds.push(id)
      }
    },
    chooseDishImage() {
      if (this.form.images.length >= 5) {
        uni.showToast({ title: '最多上传 5 张', icon: 'none' })
        return
      }
      uni.chooseImage({
        count: 5 - this.form.images.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (result) => {
          try {
            uni.showLoading({ title: '上传中' })
            for (const filePath of result.tempFilePaths) {
              const data = await uploadMerchantImage(filePath, 'dish')
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
    async submitDish() {
      if (!this.form.name || !this.form.price) {
        uni.showToast({ title: '请填写名称和价格', icon: 'none' })
        return
      }
      const payload = {
        name: this.form.name,
        description: this.form.description,
        price: Number(this.form.price),
        categoryId: this.form.categoryId ? Number(this.form.categoryId) : null,
        tagIds: this.form.tagIds,
        images: this.form.images,
        isJoinLottery: this.form.isJoinLottery
      }
      if (this.form.id) {
        await updateDish(this.form.id, payload)
      } else {
        await createDish(payload)
      }
      uni.showToast({ title: '已保存', icon: 'success' })
      this.cancelEdit()
      this.loadDishes()
    },
    async changeStatus(dish, status) {
      if (dish.status === status) {
        return
      }
      await updateDishStatus(dish.id, status)
      uni.showToast({ title: status === 'ON_SALE' ? '已上架' : '状态已更新', icon: 'success' })
      this.loadDishes()
    },
    removeDish(dish) {
      uni.showModal({
        title: '删除菜品',
        content: `确认删除“${dish.name}”吗？`,
        confirmText: '删除',
        confirmColor: '#E94B35',
        success: async (result) => {
          if (!result.confirm) {
            return
          }
          try {
            await deleteDish(dish.id)
            if (String(this.form.id) === String(dish.id)) {
              this.cancelEdit()
            }
            uni.showToast({ title: '已删除', icon: 'success' })
            this.loadDishes()
          } catch (error) {
            uni.showToast({ title: error.message || '删除失败', icon: 'none' })
          }
        }
      })
    },
    shortName(name) {
      return name ? name.slice(0, 2) : '菜'
    },
    imageUrl(image) {
      return assetUrl(image)
    },
    dishImage(dish) {
      const image = dish && dish.images && dish.images.length ? dish.images[0] : dish.coverImageUrl
      return assetUrl(image)
    },
    statusText(status) {
      const map = { ON_SALE: '上架中', SOLD_OUT: '已售罄', OFF_SHELF: '已下架', PENDING: '待审核' }
      return map[status] || '上架中'
    }
  }
}
</script>

<style>
.merchant-dishes {
  padding-top: 24rpx;
}

.top-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx;
  border-radius: 30rpx;
  background: #2b2118;
  color: #fffdf7;
}

.eyebrow,
.page-title {
  display: block;
}

.eyebrow {
  color: #ffd64e;
  font-size: 22rpx;
  font-weight: 900;
}

.page-title {
  margin-top: 8rpx;
  font-size: 42rpx;
  font-weight: 900;
}

.add-btn {
  width: 112rpx;
  height: 62rpx;
  border-radius: 999rpx;
  background: #ffd64e;
  color: #241811;
  font-size: 25rpx;
  font-weight: 900;
}

.tabs {
  display: flex;
  gap: 12rpx;
  margin-top: 22rpx;
}

.tab {
  flex: 1;
  height: 64rpx;
  border-radius: 999rpx;
  background: #fffdf7;
  color: #8a7a68;
  text-align: center;
  line-height: 64rpx;
  font-size: 24rpx;
  font-weight: 900;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.tab.active {
  background: #e94b35;
  color: #fffdf7;
}

.form-card {
  margin-top: 22rpx;
  padding: 24rpx;
  border-radius: 30rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.form-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
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
  display: flex;
  align-items: center;
  min-height: 86rpx;
  border-bottom: 2rpx solid rgba(43, 33, 24, 0.06);
}

.form-row.block {
  display: block;
  padding: 20rpx 0;
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

.form-row textarea {
  width: 100%;
  height: 156rpx;
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

.image-chip image {
  width: 44rpx;
  height: 44rpx;
  border-radius: 12rpx;
  background: #f1e2cb;
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
  min-height: 88rpx;
  color: #241811;
  font-size: 26rpx;
  font-weight: 900;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 22rpx;
}

.dish-card {
  display: flex;
  padding: 20rpx;
  border-radius: 30rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.dish-art {
  display: flex;
  align-items: flex-end;
  flex: 0 0 154rpx;
  width: 154rpx;
  height: 154rpx;
  padding: 18rpx;
  color: #fffdf7;
  font-size: 34rpx;
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

.dish-body {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.dish-head,
.dish-meta,
.actions {
  display: flex;
  align-items: center;
}

.dish-head,
.dish-meta {
  justify-content: space-between;
}

.dish-name {
  flex: 1;
  color: #241811;
  font-size: 29rpx;
  font-weight: 900;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.dish-price {
  margin-left: 12rpx;
  color: #e94b35;
  font-size: 29rpx;
  font-weight: 900;
}

.dish-desc {
  display: -webkit-box;
  margin-top: 8rpx;
  color: #6f5e4e;
  font-size: 23rpx;
  line-height: 1.35;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.dish-meta {
  margin-top: 12rpx;
  color: #9d8466;
  font-size: 21rpx;
}

.actions {
  gap: 10rpx;
  margin-top: 14rpx;
  flex-wrap: wrap;
}

.actions button {
  flex: 1 0 104rpx;
  height: 54rpx;
  border-radius: 999rpx;
  background: #fff1d1;
  color: #7b6046;
  font-size: 22rpx;
  font-weight: 900;
}

.actions button:first-child {
  background: #2b2118;
  color: #fffdf7;
}

.actions button.danger-btn {
  background: #ffe7e1;
  color: #e94b35;
}
</style>
