<template>
  <view class="page mine-page">
    <view class="profile-card">
      <view class="avatar" @tap="chooseAvatar">
        <image v-if="profile.avatar" :src="profile.avatar" mode="aspectFill" />
        <text v-else>{{ profile.nickname ? profile.nickname.slice(0, 1) : '饭' }}</text>
      </view>
      <view class="profile-info">
        <text class="nickname">{{ profile.nickname || '未登录同学' }}</text>
        <text class="profile-sub">收藏、想吃和抽签记录都在这里</text>
      </view>
      <button v-if="!loggedIn" class="login-btn" :loading="isLoggingIn" :disabled="isLoggingIn" @tap="login">登录</button>
      <view v-else class="profile-actions">
        <text class="login-state" @tap="openProfileEdit">编辑</text>
        <text class="login-state" @tap="logoutNow">登出</text>
      </view>
    </view>

    <view v-if="!loggedIn" class="login-empty">
      <text class="empty-title">先登录，再同步你的收藏和抽签记录</text>
      <text class="empty-sub">点击上方登录后，会自动刷新这里的数据。</text>
    </view>

    <view v-if="loggedIn" class="preference-card">
      <view class="section-head">
        <text class="section-title">我的偏好</text>
        <text class="section-link" @tap="openPreferenceEdit">编辑</text>
      </view>
      <view class="pref-grid">
        <view class="pref-item">
          <text>预算</text>
          <text>¥{{ preferences.minPrice }}-{{ preferences.maxPrice }}</text>
        </view>
        <view class="pref-item">
          <text>口味</text>
          <text>{{ preferences.tastePreference }}</text>
        </view>
        <view class="pref-item">
          <text>忌口</text>
          <text>{{ preferences.avoidTags || '暂无' }}</text>
        </view>
      </view>
    </view>

    <view v-if="loggedIn && editingProfile" class="edit-card">
      <view class="form-row">
        <text class="label">昵称</text>
        <input v-model="profileForm.nickname" placeholder="怎么称呼你" />
      </view>
      <view class="form-row">
        <text class="label">手机</text>
        <input v-model="profileForm.phone" type="number" placeholder="选填" />
      </view>
      <view class="edit-actions">
        <button class="ghost-btn" @tap="editingProfile = false">取消</button>
        <button class="primary-btn" @tap="saveProfile">保存</button>
      </view>
    </view>

    <view v-if="loggedIn && editingPreferences" class="edit-card">
      <view class="form-row">
        <text class="label">最低价</text>
        <input v-model="preferenceForm.minPrice" type="digit" placeholder="0" />
      </view>
      <view class="form-row">
        <text class="label">最高价</text>
        <input v-model="preferenceForm.maxPrice" type="digit" placeholder="30" />
      </view>
      <view class="form-row">
        <text class="label">口味</text>
        <input v-model="preferenceForm.tastePreference" placeholder="如 清淡 / 微辣" />
      </view>
      <view class="form-row">
        <text class="label">忌口</text>
        <input v-model="preferenceForm.avoidTags" placeholder="如 香菜, 海鲜" />
      </view>
      <view class="edit-actions">
        <button class="ghost-btn" @tap="editingPreferences = false">取消</button>
        <button class="primary-btn" @tap="savePreferences">保存</button>
      </view>
    </view>

    <view class="merchant-entry" @tap="goMerchant">
      <view>
        <text class="merchant-title">卖家工作台</text>
        <text class="merchant-sub">管理菜品、发布推荐、处理学生反馈</text>
      </view>
      <text class="merchant-arrow">进入</text>
    </view>

    <view v-if="loggedIn" class="tabs">
      <view v-for="tab in tabs" :key="tab.value" :class="['tab', activeTab === tab.value ? 'active' : '']" @tap="activeTab = tab.value">
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <view v-if="loggedIn && activeTab === 'favorite'" class="list">
      <view v-for="item in favorites" :key="item.id" class="mini-card" @tap="goDetail(item.targetId || item.dish.id)">
        <view class="food-art mini-art"><text>{{ shortName(item.dish.name) }}</text></view>
        <view class="mini-info">
          <text class="mini-title">{{ item.dish.name }}</text>
          <text class="mini-sub">{{ item.dish.canteenName }} · ¥{{ item.dish.price }}</text>
        </view>
        <button class="small-action danger" @tap.stop="removeFavorite(item.id)">取消</button>
      </view>
    </view>

    <view v-if="loggedIn && activeTab === 'eatList'" class="list">
      <view v-for="item in eatList" :key="item.id" class="mini-card" @tap="goDetail(item.dish.id)">
        <view class="food-art mini-art"><text>{{ shortName(item.dish.name) }}</text></view>
        <view class="mini-info">
          <text class="mini-title">{{ item.dish.name }}</text>
          <text class="mini-sub">想吃 · {{ item.dish.merchantName }}</text>
        </view>
        <view class="item-actions">
          <button class="small-action" @tap.stop="markEaten(item.id)">已吃</button>
          <button class="small-action danger" @tap.stop="removeEatList(item.id)">移除</button>
        </view>
      </view>
    </view>

    <view v-if="loggedIn && activeTab === 'history'" class="list">
      <view v-for="item in history" :key="item.id" class="history-card">
        <view>
          <text class="mini-title">{{ item.title }}</text>
          <text class="mini-sub">{{ item.createdAt }} · {{ actionText(item.resultAction) }}</text>
        </view>
        <text class="history-price">¥{{ item.price }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { deleteEatList, deleteFavorite, fetchEatList, fetchFavorites, fetchLotteryRecords, fetchPreferences, fetchProfile, logout, markEatListEaten, studentLogin, updatePreferences, updateProfile, uploadStudentImage } from '../../services/student.js'
import { hasToken } from '../../utils/request.js'

export default {
  data() {
    return {
      profile: {},
      preferences: {},
      profileForm: {
        nickname: '',
        phone: '',
        avatar: ''
      },
      preferenceForm: {
        minPrice: '',
        maxPrice: '',
        tastePreference: '',
        avoidTags: ''
      },
      loggedIn: false,
      isLoggingIn: false,
      editingProfile: false,
      editingPreferences: false,
      activeTab: 'favorite',
      tabs: [
        { label: '收藏', value: 'favorite' },
        { label: '想吃', value: 'eatList' },
        { label: '抽签', value: 'history' }
      ],
      favorites: [],
      eatList: [],
      history: []
    }
  },
  onShow() {
    this.refresh()
  },
  methods: {
    refresh() {
      this.loggedIn = hasToken()
      if (!this.loggedIn) {
        this.profile = {}
        this.preferences = {}
        this.editingProfile = false
        this.editingPreferences = false
        this.favorites = []
        this.eatList = []
        this.history = []
        return
      }
      this.load()
    },
    async load() {
      try {
        const [profile, preferences] = await Promise.all([fetchProfile(), fetchPreferences()])
        this.profile = profile || {}
        this.preferences = preferences || {}
        this.syncForms()
        await this.loadLists()
        return true
      } catch (error) {
        this.loggedIn = false
        this.profile = {}
        this.preferences = {}
        this.editingProfile = false
        this.editingPreferences = false
        this.favorites = []
        this.eatList = []
        this.history = []
        uni.showToast({ title: error.message || '加载失败', icon: 'none' })
        return false
      }
    },
    async loadLists() {
      if (!this.loggedIn) return
      const [favorites, eatList, history] = await Promise.all([fetchFavorites(), fetchEatList(), fetchLotteryRecords()])
      this.favorites = favorites.records || []
      this.eatList = eatList.records || []
      this.history = history.records || []
    },
    async login() {
      if (this.isLoggingIn) return
      this.isLoggingIn = true
      try {
        await studentLogin()
        this.loggedIn = true
        const loaded = await this.load()
        if (loaded) {
          uni.showToast({ title: '登录成功', icon: 'success' })
        }
      } catch (error) {
        uni.showModal({
          title: '登录失败',
          content: error.message || '请稍后再试',
          showCancel: false
        })
      } finally {
        this.isLoggingIn = false
      }
    },
    syncForms() {
      this.profileForm = {
        nickname: this.profile.nickname || '',
        phone: this.profile.phone || '',
        avatar: this.profile.avatar || ''
      }
      this.preferenceForm = {
        minPrice: this.preferences.minPrice === undefined || this.preferences.minPrice === null ? '' : String(this.preferences.minPrice),
        maxPrice: this.preferences.maxPrice === undefined || this.preferences.maxPrice === null ? '' : String(this.preferences.maxPrice),
        tastePreference: this.preferences.tastePreference || '',
        avoidTags: this.preferences.avoidTags || ''
      }
    },
    openProfileEdit() {
      this.syncForms()
      this.editingProfile = true
    },
    openPreferenceEdit() {
      this.syncForms()
      this.editingPreferences = true
    },
    async saveProfile() {
      const profile = await updateProfile({
        nickname: this.profileForm.nickname,
        phone: this.profileForm.phone,
        avatar: this.profileForm.avatar
      })
      this.profile = profile || {}
      this.editingProfile = false
      uni.showToast({ title: '资料已更新', icon: 'success' })
    },
    async savePreferences() {
      const minPrice = this.preferenceForm.minPrice === '' ? null : Number(this.preferenceForm.minPrice)
      const maxPrice = this.preferenceForm.maxPrice === '' ? null : Number(this.preferenceForm.maxPrice)
      if (minPrice !== null && maxPrice !== null && minPrice > maxPrice) {
        uni.showToast({ title: '最低价不能高于最高价', icon: 'none' })
        return
      }
      const preferences = await updatePreferences({
        minPrice,
        maxPrice,
        tastePreference: this.preferenceForm.tastePreference,
        avoidTags: this.preferenceForm.avoidTags
      })
      this.preferences = preferences || {}
      this.editingPreferences = false
      uni.showToast({ title: '偏好已保存', icon: 'success' })
    },
    chooseAvatar() {
      if (!this.loggedIn) return
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (result) => {
          try {
            uni.showLoading({ title: '上传中' })
            const data = await uploadStudentImage(result.tempFilePaths[0], 'avatar')
            this.profileForm.avatar = data.url
            const profile = await updateProfile({
              nickname: this.profile.nickname,
              phone: this.profile.phone,
              avatar: data.url
            })
            this.profile = profile || {}
            this.syncForms()
            uni.hideLoading()
            uni.showToast({ title: '头像已更新', icon: 'success' })
          } catch (error) {
            uni.hideLoading()
            uni.showToast({ title: error.message || '上传失败', icon: 'none' })
          }
        }
      })
    },
    async logoutNow() {
      await logout()
      this.refresh()
      uni.showToast({ title: '已登出', icon: 'success' })
    },
    async removeFavorite(id) {
      await deleteFavorite(id)
      this.favorites = this.favorites.filter((item) => item.id !== id)
      uni.showToast({ title: '已取消收藏', icon: 'success' })
    },
    async markEaten(id) {
      await markEatListEaten(id)
      this.eatList = this.eatList.filter((item) => item.id !== id)
      uni.showToast({ title: '已标记已吃', icon: 'success' })
    },
    async removeEatList(id) {
      await deleteEatList(id)
      this.eatList = this.eatList.filter((item) => item.id !== id)
      uni.showToast({ title: '已移除', icon: 'success' })
    },
    shortName(name) {
      return name ? name.slice(0, 2) : '饭'
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/dish-detail/index?id=${id}` })
    },
    goMerchant() {
      uni.navigateTo({ url: '/pages/merchant/index/index' })
    },
    actionText(action) {
      const map = { ACCEPT: '就吃它', FAVORITE: '已收藏', SKIP: '跳过' }
      return map[action] || '看过'
    }
  }
}
</script>

<style>
.profile-card {
  display: flex;
  align-items: center;
  min-height: 156rpx;
  padding: 26rpx;
  border-radius: 32rpx;
  background: #2b2118;
  color: #fffdf7;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 92rpx;
  height: 92rpx;
  border-radius: 30rpx;
  background: linear-gradient(135deg, #ffd64e, #e94b35);
  font-size: 38rpx;
  font-weight: 900;
  overflow: hidden;
}

.avatar image {
  width: 100%;
  height: 100%;
}

.profile-info {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.nickname {
  display: block;
  font-size: 32rpx;
  font-weight: 900;
}

.profile-sub {
  display: block;
  margin-top: 8rpx;
  color: rgba(255, 253, 247, 0.68);
  font-size: 23rpx;
}

.login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 106rpx;
  height: 62rpx;
  border-radius: 999rpx;
  background: #fffdf7;
  color: #e94b35;
  font-size: 24rpx;
  font-weight: 900;
}

.login-state {
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 253, 247, 0.16);
  color: #fffdf7;
  font-size: 23rpx;
  font-weight: 900;
}

.profile-actions {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.login-empty {
  margin-top: 22rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.empty-title {
  display: block;
  color: #241811;
  font-size: 29rpx;
  font-weight: 900;
}

.empty-sub {
  display: block;
  margin-top: 10rpx;
  color: #8a7a68;
  font-size: 24rpx;
  line-height: 1.4;
}

.preference-card {
  margin-top: 22rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.edit-card {
  margin-top: 18rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.form-row {
  display: flex;
  align-items: center;
  min-height: 82rpx;
  border-bottom: 2rpx solid rgba(43, 33, 24, 0.06);
}

.label {
  width: 116rpx;
  color: #7b6046;
  font-size: 25rpx;
  font-weight: 900;
}

.form-row input {
  flex: 1;
  height: 80rpx;
  color: #241811;
  font-size: 26rpx;
}

.edit-actions {
  display: flex;
  gap: 14rpx;
  margin-top: 22rpx;
}

.edit-actions button {
  flex: 1;
}

.merchant-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 22rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background:
    radial-gradient(circle at 18% 20%, rgba(255, 214, 78, 0.4), transparent 120rpx),
    linear-gradient(135deg, #fff1d1 0%, #ff9f43 72%, #e94b35 100%);
}

.merchant-title {
  display: block;
  color: #241811;
  font-size: 30rpx;
  font-weight: 900;
}

.merchant-sub {
  display: block;
  margin-top: 8rpx;
  color: rgba(43, 33, 24, 0.68);
  font-size: 23rpx;
  font-weight: 700;
}

.merchant-arrow {
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: #fffdf7;
  color: #e94b35;
  font-size: 23rpx;
  font-weight: 900;
}

.pref-grid {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.pref-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 58rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  background: #fff7ea;
}

.pref-item text:first-child {
  color: #8a7a68;
  font-size: 23rpx;
  font-weight: 800;
}

.pref-item text:last-child {
  color: #241811;
  font-size: 24rpx;
  font-weight: 900;
}

.tabs {
  display: flex;
  gap: 14rpx;
  margin-top: 24rpx;
}

.tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 68rpx;
  border-radius: 999rpx;
  background: #fffdf7;
  color: #8a7a68;
  font-size: 25rpx;
  font-weight: 900;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.tab.active {
  background: #e94b35;
  color: #fffdf7;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 22rpx;
}

.mini-card,
.history-card {
  display: flex;
  align-items: center;
  padding: 20rpx;
  border-radius: 26rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.mini-info {
  flex: 1;
  min-width: 0;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.small-action {
  width: 96rpx;
  height: 50rpx;
  border-radius: 999rpx;
  background: #fff1d1;
  color: #7b6046;
  font-size: 21rpx;
  font-weight: 900;
  line-height: 50rpx;
}

.small-action.danger {
  background: #ffe9e4;
  color: #e94b35;
}

.mini-art {
  display: flex;
  align-items: flex-end;
  flex: 0 0 104rpx;
  width: 104rpx;
  height: 104rpx;
  margin-right: 18rpx;
  padding: 14rpx;
  color: #fffdf7;
  font-size: 26rpx;
  font-weight: 900;
}

.mini-title {
  display: block;
  color: #241811;
  font-size: 28rpx;
  font-weight: 900;
}

.mini-sub {
  display: block;
  margin-top: 8rpx;
  color: #9d8466;
  font-size: 23rpx;
}

.history-card {
  justify-content: space-between;
}

.history-price {
  color: #e94b35;
  font-size: 29rpx;
  font-weight: 900;
}
</style>
