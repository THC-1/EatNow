<template>
  <view class="page apply-page">
    <view class="apply-hero">
      <text class="eyebrow">Merchant Apply</text>
      <text class="hero-title">商家入驻申请</text>
      <text class="hero-sub">食堂商家选择窗口，独立店铺只需选择校内或校外，审核通过后即可管理菜品和学生反馈。</text>
    </view>

    <view v-if="loading" class="state-card">
      <text>正在读取入驻资料...</text>
    </view>

    <view v-else-if="loadError" class="state-card error">
      <text>{{ loadError }}</text>
      <button class="retry-btn" @tap="load">重新加载</button>
    </view>

    <view v-else>
      <view v-if="applyStatus" :class="['audit-card', statusClass]">
        <text class="audit-title">{{ statusText(applyStatus) }}</text>
        <text class="audit-sub">
          {{ applyStatus === 'REJECTED' ? rejectReason || '请修改资料后重新提交。' : statusHint }}
        </text>
      </view>

      <view class="form-card">
        <view class="form-row">
          <text class="label">商家名称</text>
          <input v-model="form.merchantName" placeholder="例如：张记盖饭" />
        </view>

        <view class="apply-type-tabs">
          <view
            v-for="item in storeTypes"
            :key="item.value"
            :class="['apply-type-tab', form.storeType === item.value || (item.value === 'INDEPENDENT' && isIndependentApplication) ? 'active' : '']"
            @tap="selectStoreType(item.value)"
          >
            <text>{{ item.label }}</text>
          </view>
        </view>

        <view v-if="isCanteenApplication" class="form-row picker-row">
          <text class="label">所属食堂</text>
          <picker :range="canteenNames" :value="canteenPickerIndex" :disabled="!canteenOptions.length" @change="onCanteenChange">
            <view class="picker-value">
              <text>{{ selectedCanteen ? selectedCanteen.name : '暂无可选食堂' }}</text>
              <text class="picker-arrow">切换</text>
            </view>
          </picker>
        </view>

        <view v-if="isCanteenApplication" class="form-row picker-row">
          <text class="label">窗口</text>
          <picker :range="stallNames" :value="stallPickerIndex" :disabled="!availableStalls.length" @change="onStallChange">
            <view class="picker-value">
              <text>{{ selectedStall ? selectedStall.name : '暂无可选窗口' }}</text>
              <text class="picker-arrow">选择</text>
            </view>
          </picker>
        </view>

        <view v-if="isIndependentApplication" class="form-row picker-row">
          <text class="label">店铺位置</text>
          <picker :range="independentTypeNames" :value="independentTypeIndex" @change="onIndependentTypeChange">
            <view class="picker-value">
              <text>{{ selectedIndependentType.label }}</text>
              <text class="picker-arrow">选择</text>
            </view>
          </picker>
        </view>

        <view v-if="isCanteenApplication && selectedStall" class="stall-preview">
          <view>
            <text class="stall-title">{{ selectedStall.name }}</text>
            <text class="stall-meta">{{ selectedCanteen.name }} · {{ selectedStall.location || '未填写位置' }}</text>
          </view>
          <text v-if="selectedStall.merchantName" class="stall-owner">当前申请</text>
        </view>

        <view v-else-if="isCanteenApplication" class="empty-stall">
          <text>{{ selectedCanteen ? '当前食堂暂无可申请窗口，请联系管理员维护。' : '暂无营业中的食堂。' }}</text>
        </view>

        <view v-if="isIndependentApplication" class="stall-preview">
          <view>
            <text class="stall-title">{{ selectedIndependentType.label }}独立店铺</text>
            <text class="stall-meta">无需选择窗口，系统会按商家名称创建独立店铺。</text>
          </view>
        </view>

        <view class="form-row">
          <text class="label">营业时间</text>
          <view class="time-pickers">
            <picker mode="time" :value="businessStartTime" @change="onBusinessStartChange">
              <view class="time-value">{{ businessStartTime }}</view>
            </picker>
            <text class="time-separator">至</text>
            <picker mode="time" :value="businessEndTime" @change="onBusinessEndChange">
              <view class="time-value">{{ businessEndTime }}</view>
            </picker>
          </view>
        </view>
        <view class="form-row">
          <text class="label">联系电话</text>
          <input v-model="form.contactPhone" type="number" placeholder="用于审核联系" />
        </view>
        <view class="form-row block">
          <text class="label">商家简介</text>
          <textarea v-model="form.description" maxlength="500" placeholder="写清楚主营品类、位置和特色菜" />
        </view>

        <button class="primary-btn submit-btn" :disabled="submitting" @tap="submit">
          {{ submitText }}
        </button>
      </view>

      <view class="hint-card">
        <text class="hint-title">审核后可以做什么</text>
        <view class="hint-line"><text>1</text><text>发布和维护自己的菜品</text></view>
        <view class="hint-line"><text>2</text><text>设置今日主推、新品和招牌菜</text></view>
        <view class="hint-line"><text>3</text><text>回复学生反馈并发布改进记录</text></view>
      </view>
    </view>
  </view>
</template>

<script>
import {
  applyMerchant,
  fetchCanteensForApply,
  fetchMerchantProfile,
  fetchStallsForApply,
  merchantLogin
} from '../../../services/merchant.js'

export default {
  data() {
    return {
      loading: false,
      submitting: false,
      loadError: '',
      applyStatus: '',
      rejectReason: '',
      profileStallId: null,
      canteens: [],
      allStalls: [],
      canteenIndex: -1,
      stallIndex: -1,
      independentIndex: 0,
      businessStartTime: '10:00',
      businessEndTime: '20:30',
      storeTypes: [
        { label: '食堂窗口', value: 'CANTEEN' },
        { label: '独立店铺', value: 'INDEPENDENT' }
      ],
      independentTypes: [
        { label: '校内', value: 'CAMPUS_SHOP' },
        { label: '校外', value: 'PERIPHERY_SHOP' }
      ],
      form: {
        merchantName: '',
        storeType: 'CANTEEN',
        businessHours: '',
        contactPhone: '',
        description: ''
      }
    }
  },
  computed: {
    isCanteenApplication() {
      return this.form.storeType === 'CANTEEN'
    },
    isIndependentApplication() {
      return this.form.storeType === 'CAMPUS_SHOP' || this.form.storeType === 'PERIPHERY_SHOP'
    },
    canteenOptions() {
      return this.canteens.filter((item) => item.type === 'CANTEEN')
    },
    canteenNames() {
      return this.canteenOptions.map((item) => item.name)
    },
    independentTypeNames() {
      return this.independentTypes.map((item) => item.label)
    },
    independentTypeIndex() {
      return this.independentIndex
    },
    selectedIndependentType() {
      return this.independentTypes[this.independentIndex] || this.independentTypes[0]
    },
    availableStalls() {
      return this.allStalls.filter((item) => {
        const isOpen = item.status === 'OPEN'
        const isOwnSelected = this.profileStallId && Number(item.id) === Number(this.profileStallId)
        return isOpen && (!item.merchantName || isOwnSelected)
      })
    },
    stallNames() {
      return this.availableStalls.map((item) => `${item.name}${item.location ? ` · ${item.location}` : ''}`)
    },
    canteenPickerIndex() {
      return this.canteenIndex >= 0 ? this.canteenIndex : 0
    },
    stallPickerIndex() {
      return this.stallIndex >= 0 ? this.stallIndex : 0
    },
    selectedCanteen() {
      return this.canteenIndex >= 0 ? this.canteenOptions[this.canteenIndex] : null
    },
    selectedStall() {
      return this.stallIndex >= 0 ? this.availableStalls[this.stallIndex] : null
    },
    statusHint() {
      const map = {
        PENDING: '资料已提交，等待管理员审核。',
        APPROVED: '已审核通过，无需重复提交。',
        REJECTED: '请修改资料后重新提交。'
      }
      return map[this.applyStatus] || '请完善资料后提交。'
    },
    statusClass() {
      const map = {
        PENDING: 'pending',
        APPROVED: 'approved',
        REJECTED: 'rejected'
      }
      return map[this.applyStatus] || ''
    },
    submitText() {
      if (this.submitting) return '提交中...'
      if (this.applyStatus === 'APPROVED') return '已通过审核'
      return this.applyStatus === 'REJECTED' ? '重新提交申请' : '提交申请'
    },
    businessHoursText() {
      return `${this.businessStartTime}-${this.businessEndTime}`
    }
  },
  onLoad() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      this.loadError = ''
      try {
        await merchantLogin()
        await this.loadCanteens()
        const profile = await this.safeFetchProfile()
        if (profile) {
          this.fillProfile(profile)
        }
        this.selectInitialCanteen(profile)
        await this.loadStalls()
        this.selectInitialStall(profile)
      } catch (error) {
        this.loadError = error.message || '入驻资料加载失败'
        uni.showToast({ title: this.loadError, icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    async safeFetchProfile() {
      try {
        return await fetchMerchantProfile()
      } catch (error) {
        if (this.isMissingMerchant(error)) {
          return null
        }
        throw error
      }
    },
    isMissingMerchant(error) {
      const message = (error && error.message) || ''
      return message.includes('404') && message.includes('Merchant not found')
    },
    fillProfile(profile) {
      this.applyStatus = profile.applyStatus || ''
      this.rejectReason = profile.rejectReason || ''
      this.profileStallId = profile.stallId || null
      this.form = {
        merchantName: profile.name || '',
        storeType: profile.canteenType || 'CANTEEN',
        businessHours: profile.businessHours || '',
        contactPhone: profile.contactPhone || '',
        description: profile.description || ''
      }
      const independentIndex = this.independentTypes.findIndex((item) => item.value === this.form.storeType)
      this.independentIndex = independentIndex >= 0 ? independentIndex : 0
      this.fillBusinessHours(profile.businessHours)
    },
    async loadCanteens() {
      const list = await fetchCanteensForApply()
      this.canteens = (list || []).filter((item) => item.status === 'OPEN')
    },
    selectInitialCanteen(profile) {
      if (!this.isCanteenApplication || !this.canteenOptions.length) {
        this.canteenIndex = -1
        return
      }
      const profileIndex = profile && profile.canteenId
        ? this.canteenOptions.findIndex((item) => Number(item.id) === Number(profile.canteenId))
        : -1
      this.canteenIndex = profileIndex >= 0 ? profileIndex : 0
    },
    async loadStalls() {
      if (!this.isCanteenApplication || !this.selectedCanteen) {
        this.allStalls = []
        this.stallIndex = -1
        return
      }
      this.allStalls = await fetchStallsForApply(this.selectedCanteen.id)
      if (!this.availableStalls.length) {
        this.stallIndex = -1
      }
    },
    selectInitialStall(profile) {
      if (!this.availableStalls.length) {
        this.stallIndex = -1
        return
      }
      const profileIndex = profile && profile.stallId
        ? this.availableStalls.findIndex((item) => Number(item.id) === Number(profile.stallId))
        : -1
      this.stallIndex = profileIndex >= 0 ? profileIndex : 0
    },
    async onCanteenChange(event) {
      this.canteenIndex = Number(event.detail.value)
      this.stallIndex = -1
      await this.loadStalls()
      this.selectInitialStall(null)
    },
    async selectStoreType(type) {
      if (type === 'CANTEEN') {
        this.form.storeType = 'CANTEEN'
        this.selectInitialCanteen(null)
        await this.loadStalls()
        this.selectInitialStall(null)
        return
      }
      this.form.storeType = this.selectedIndependentType.value
      this.canteenIndex = -1
      this.stallIndex = -1
      this.allStalls = []
    },
    onIndependentTypeChange(event) {
      this.independentIndex = Number(event.detail.value)
      this.form.storeType = this.selectedIndependentType.value
    },
    onStallChange(event) {
      this.stallIndex = Number(event.detail.value)
    },
    onBusinessStartChange(event) {
      this.businessStartTime = event.detail.value
    },
    onBusinessEndChange(event) {
      this.businessEndTime = event.detail.value
    },
    fillBusinessHours(value) {
      const match = String(value || '').match(/^(\d{2}:\d{2})-(\d{2}:\d{2})$/)
      if (!match) return
      this.businessStartTime = match[1]
      this.businessEndTime = match[2]
    },
    statusText(status) {
      const map = {
        PENDING: '入驻审核中',
        APPROVED: '入驻已通过',
        REJECTED: '入驻被驳回'
      }
      return map[status] || '入驻资料'
    },
    async submit() {
      if (this.applyStatus === 'APPROVED') {
        uni.showToast({ title: '已审核通过，无需重复提交', icon: 'none' })
        return
      }
      if (!this.form.merchantName) {
        uni.showToast({ title: '请填写商家名称', icon: 'none' })
        return
      }
      if (this.isCanteenApplication && !this.selectedStall) {
        uni.showToast({ title: '请选择窗口', icon: 'none' })
        return
      }
      if (this.businessStartTime >= this.businessEndTime) {
        uni.showToast({ title: '结束时间需晚于开始时间', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const payload = {
          ...this.form,
          businessHours: this.businessHoursText,
          storeType: this.form.storeType,
          campusId: 1
        }
        if (this.isCanteenApplication) {
          payload.stallId = Number(this.selectedStall.id)
        }
        await applyMerchant(payload)
        uni.showToast({ title: '已提交审核', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 600)
      } catch (error) {
        uni.showToast({ title: error.message || '提交失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style>
.apply-page {
  padding-top: 24rpx;
}

.apply-hero {
  padding: 32rpx;
  border-radius: 34rpx;
  background:
    radial-gradient(circle at 18% 16%, rgba(127, 189, 65, 0.38), transparent 170rpx),
    linear-gradient(135deg, #fff7d6 0%, #ffd064 48%, #ff8b4a 100%);
}

.eyebrow,
.hero-title,
.hero-sub {
  display: block;
}

.eyebrow {
  color: #4f8f20;
  font-size: 23rpx;
  font-weight: 900;
}

.hero-title {
  margin-top: 10rpx;
  color: #241811;
  font-size: 46rpx;
  font-weight: 900;
}

.hero-sub {
  margin-top: 14rpx;
  color: #6f5e4e;
  font-size: 25rpx;
  line-height: 1.45;
}

.state-card,
.form-card,
.hint-card,
.audit-card {
  margin-top: 24rpx;
  padding: 24rpx;
  border-radius: 30rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.state-card {
  text-align: center;
  color: #7b6046;
  font-size: 26rpx;
  font-weight: 800;
}

.state-card.error {
  color: #e94b35;
}

.retry-btn {
  margin-top: 18rpx;
  height: 64rpx;
  border-radius: 999rpx;
  background: #2b2118;
  color: #fffdf7;
  font-size: 24rpx;
  font-weight: 900;
}

.audit-card {
  border-color: rgba(255, 154, 57, 0.22);
  background: #fff7ea;
}

.audit-card.approved {
  border-color: rgba(79, 143, 32, 0.2);
  background: #f3fbdc;
}

.audit-card.rejected {
  border-color: rgba(233, 75, 53, 0.2);
  background: #fff0ea;
}

.audit-title,
.audit-sub {
  display: block;
}

.audit-title {
  color: #241811;
  font-size: 30rpx;
  font-weight: 900;
}

.audit-sub {
  margin-top: 8rpx;
  color: #7b6046;
  font-size: 24rpx;
  line-height: 1.45;
}

.form-row {
  display: flex;
  align-items: center;
  min-height: 88rpx;
  border-bottom: 2rpx solid rgba(43, 33, 24, 0.06);
}

.apply-type-tabs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin: 22rpx 0 8rpx;
}

.apply-type-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 74rpx;
  border-radius: 22rpx;
  background: #fff7ea;
  color: #7b6046;
  font-size: 25rpx;
  font-weight: 900;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.apply-type-tab.active {
  background: #2b2118;
  color: #fffdf7;
  border-color: #2b2118;
}

.form-row.block {
  display: block;
  padding: 22rpx 0;
}

.form-row:last-of-type {
  border-bottom: 0;
}

.label {
  width: 150rpx;
  color: #7b6046;
  font-size: 25rpx;
  font-weight: 900;
}

.form-row input {
  flex: 1;
  height: 82rpx;
  color: #241811;
  font-size: 27rpx;
}

.time-pickers {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.time-pickers picker {
  flex: 1;
}

.time-value {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 62rpx;
  border-radius: 18rpx;
  background: #fff7ea;
  color: #241811;
  font-size: 26rpx;
  font-weight: 900;
}

.time-separator {
  flex: 0 0 auto;
  margin: 0 16rpx;
  color: #9d8466;
  font-size: 24rpx;
  font-weight: 900;
}

.picker-row picker {
  flex: 1;
  min-width: 0;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 82rpx;
  color: #241811;
  font-size: 27rpx;
}

.picker-value text:first-child {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.picker-arrow {
  margin-left: 16rpx;
  color: #e94b35;
  font-size: 23rpx;
  font-weight: 900;
}

.stall-preview,
.empty-stall {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 18rpx 0 6rpx;
  padding: 20rpx;
  border-radius: 24rpx;
  background: #fff7ea;
}

.empty-stall {
  color: #9d8466;
  font-size: 24rpx;
  line-height: 1.45;
}

.stall-title,
.stall-meta {
  display: block;
}

.stall-title {
  color: #241811;
  font-size: 28rpx;
  font-weight: 900;
}

.stall-meta {
  margin-top: 8rpx;
  color: #7b6046;
  font-size: 23rpx;
}

.stall-owner {
  flex: 0 0 auto;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #e9f8cf;
  color: #4f8f20;
  font-size: 21rpx;
  font-weight: 900;
}

.form-row textarea {
  width: 100%;
  height: 180rpx;
  margin-top: 16rpx;
  padding: 18rpx;
  border-radius: 22rpx;
  background: #fff7ea;
  color: #241811;
  font-size: 26rpx;
  line-height: 1.45;
}

.submit-btn {
  margin-top: 24rpx;
}

.hint-title {
  display: block;
  margin-bottom: 16rpx;
  color: #241811;
  font-size: 30rpx;
  font-weight: 900;
}

.hint-line {
  display: flex;
  align-items: center;
  min-height: 64rpx;
}

.hint-line text:first-child {
  width: 42rpx;
  height: 42rpx;
  border-radius: 14rpx;
  background: #e9f8cf;
  color: #4f8f20;
  text-align: center;
  line-height: 42rpx;
  font-size: 22rpx;
  font-weight: 900;
}

.hint-line text:last-child {
  margin-left: 16rpx;
  color: #5d4a3b;
  font-size: 25rpx;
  font-weight: 700;
}
</style>
