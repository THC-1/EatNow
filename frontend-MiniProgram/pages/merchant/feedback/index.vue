<template>
  <view class="page feedback-page">
    <view class="top-panel">
      <text class="eyebrow">Feedback Loop</text>
      <text class="page-title">留言反馈</text>
      <text class="page-sub">回复学生建议，把已处理的问题沉淀成改进记录。</text>
    </view>

    <scroll-view scroll-x class="filters" show-scrollbar="false">
      <view class="filter-row">
        <view v-for="item in statusFilters" :key="item.value" :class="['filter', status === item.value ? 'active' : '']" @tap="setStatus(item.value)">
          <text>{{ item.label }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="feedback-list">
      <view v-for="item in feedbacks" :key="item.id" class="feedback-card">
        <view class="feedback-top">
          <view>
            <text class="dish-name">{{ item.dishName }}</text>
            <text class="feedback-sub">{{ item.userNickname || '匿名同学' }} · {{ typeText(item.feedbackType) }} · {{ item.createdAt }}</text>
          </view>
          <text class="status-badge">{{ statusText(item.status) }}</text>
        </view>
        <text class="feedback-content">{{ item.content }}</text>
        <view v-if="item.replyContent" class="reply-box">
          <text>商家回复</text>
          <text>{{ item.replyContent }}</text>
        </view>
        <textarea v-if="activeReplyId === item.id" v-model="replyContent" class="reply-input" maxlength="500" placeholder="输入回复内容" />
        <view class="actions">
          <button @tap="toggleReply(item)">{{ activeReplyId === item.id ? '取消' : '回复' }}</button>
          <button @tap="submitReply(item)">发送</button>
          <button @tap="changeStatus(item, 'ACCEPTED')">采纳</button>
          <button @tap="changeStatus(item, 'IMPROVED')">已改进</button>
        </view>
        <view class="improve-entry" @tap="startImprovement(item)">
          <text>发布改进记录</text>
          <text>让学生看到这条反馈的处理结果</text>
        </view>
      </view>
    </view>

    <view v-if="!feedbacks.length" class="empty">暂无对应反馈</view>

    <view v-if="improving" class="form-card">
      <view class="form-head">
        <text>发布改进记录</text>
        <text @tap="improving = false">收起</text>
      </view>
      <view class="linked-feedback">
        <text>{{ improvement.dishName }}</text>
        <text>{{ improvement.feedbackContent }}</text>
      </view>
      <view class="form-row">
        <text class="label">标题</text>
        <input v-model="improvement.title" placeholder="例如：米饭分量已统一" />
      </view>
      <view class="form-row block">
        <text class="label">改进内容</text>
        <textarea v-model="improvement.content" maxlength="5000" placeholder="说明具体怎么改、何时生效" />
      </view>
      <view class="form-row">
        <text class="label">改进前</text>
        <input v-model="improvement.beforeDescription" placeholder="可选" />
      </view>
      <view class="form-row">
        <text class="label">改进后</text>
        <input v-model="improvement.afterDescription" placeholder="可选" />
      </view>
      <view class="public-row">
        <text>公开展示</text>
        <switch :checked="improvement.isPublic" color="#E94B35" @change="improvement.isPublic = $event.detail.value" />
      </view>
      <button class="primary-btn" @tap="submitImprovement">发布改进</button>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">改进记录</text>
        <text class="section-link" @tap="loadImprovements">刷新</text>
      </view>
      <view class="improvement-list">
        <view v-for="item in improvements" :key="item.id" class="improvement-card">
          <text class="improvement-title">{{ item.title }}</text>
          <text class="improvement-sub">{{ item.dishName || '通用改进' }} · {{ recordStatusText(item.status) }}</text>
          <text class="improvement-content">{{ item.content }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import {
  createImprovementRecord,
  fetchImprovementRecords,
  fetchMerchantFeedback,
  replyFeedback,
  updateFeedbackStatus
} from '../../../services/merchant.js'

function emptyImprovement() {
  return {
    dishId: '',
    dishName: '',
    feedbackId: '',
    feedbackContent: '',
    title: '',
    content: '',
    beforeDescription: '',
    afterDescription: '',
    status: 'PUBLISHED',
    isPublic: true
  }
}

export default {
  data() {
    return {
      status: '',
      feedbacks: [],
      improvements: [],
      activeReplyId: '',
      replyContent: '',
      improving: false,
      improvement: emptyImprovement(),
      statusFilters: [
        { label: '全部', value: '' },
        { label: '待查看', value: 'PENDING' },
        { label: '已查看', value: 'VIEWED' },
        { label: '已采纳', value: 'ACCEPTED' },
        { label: '已改进', value: 'IMPROVED' }
      ]
    }
  },
  onLoad() {
    this.loadFeedback()
    this.loadImprovements()
  },
  methods: {
    async loadFeedback() {
      const page = await fetchMerchantFeedback({ page: 1, size: 30, status: this.status })
      this.feedbacks = page.records || []
    },
    async loadImprovements() {
      const page = await fetchImprovementRecords({ page: 1, size: 20 })
      this.improvements = page.records || []
    },
    setStatus(status) {
      this.status = status
      this.loadFeedback()
    },
    toggleReply(item) {
      if (this.activeReplyId === item.id) {
        this.activeReplyId = ''
        this.replyContent = ''
        return
      }
      this.activeReplyId = item.id
      this.replyContent = item.replyContent || ''
    },
    async submitReply(item) {
      if (this.activeReplyId !== item.id) {
        this.toggleReply(item)
        return
      }
      if (!this.replyContent) {
        uni.showToast({ title: '请填写回复内容', icon: 'none' })
        return
      }
      await replyFeedback(item.id, this.replyContent)
      uni.showToast({ title: '已回复', icon: 'success' })
      this.activeReplyId = ''
      this.replyContent = ''
      this.loadFeedback()
    },
    async changeStatus(item, status) {
      await updateFeedbackStatus(item.id, status)
      uni.showToast({ title: '状态已更新', icon: 'success' })
      this.loadFeedback()
    },
    startImprovement(item) {
      this.improvement = {
        ...emptyImprovement(),
        dishId: item.dishId,
        dishName: item.dishName,
        feedbackId: item.id,
        feedbackContent: item.content,
        title: `${item.dishName}已优化`,
        beforeDescription: item.content
      }
      this.improving = true
    },
    async submitImprovement() {
      if (!this.improvement.title || !this.improvement.content) {
        uni.showToast({ title: '请填写标题和内容', icon: 'none' })
        return
      }
      await createImprovementRecord({
        dishId: this.improvement.dishId,
        feedbackId: this.improvement.feedbackId,
        title: this.improvement.title,
        content: this.improvement.content,
        beforeDescription: this.improvement.beforeDescription,
        afterDescription: this.improvement.afterDescription,
        status: this.improvement.status,
        isPublic: this.improvement.isPublic
      })
      if (this.improvement.feedbackId) {
        await updateFeedbackStatus(this.improvement.feedbackId, 'IMPROVED')
      }
      uni.showToast({ title: '已发布改进', icon: 'success' })
      this.improving = false
      this.improvement = emptyImprovement()
      this.loadFeedback()
      this.loadImprovements()
    },
    typeText(type) {
      const map = { TASTE: '口味', PORTION: '分量', PRICE: '价格', SERVICE: '服务', HYGIENE: '卫生', OTHER: '其他' }
      return map[type] || '其他'
    },
    statusText(status) {
      const map = { PENDING: '待查看', VIEWED: '已查看', ACCEPTED: '已采纳', IMPROVED: '已改进', REJECTED: '暂不处理' }
      return map[status] || '待查看'
    },
    recordStatusText(status) {
      const map = { DRAFT: '草稿', PUBLISHED: '已发布', ARCHIVED: '已归档' }
      return map[status] || '已发布'
    }
  }
}
</script>

<style>
.feedback-page {
  padding-top: 24rpx;
}

.top-panel {
  padding: 32rpx;
  border-radius: 34rpx;
  background:
    radial-gradient(circle at 16% 20%, rgba(255, 214, 78, 0.6), transparent 150rpx),
    linear-gradient(135deg, #fff4df 0%, #ffb765 54%, #e94b35 100%);
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

.filters {
  width: 100%;
  margin-top: 22rpx;
  white-space: nowrap;
}

.filter-row {
  display: inline-flex;
  gap: 12rpx;
  padding-right: 28rpx;
}

.filter {
  height: 60rpx;
  padding: 0 20rpx;
  border-radius: 999rpx;
  background: #fffdf7;
  color: #8a7a68;
  line-height: 60rpx;
  font-size: 23rpx;
  font-weight: 900;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.filter.active {
  background: #e9f8cf;
  color: #4f8f20;
}

.feedback-list,
.improvement-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 22rpx;
}

.feedback-card,
.form-card,
.improvement-card {
  padding: 24rpx;
  border-radius: 30rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.feedback-top,
.actions,
.form-head,
.form-row,
.public-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dish-name,
.feedback-sub,
.feedback-content,
.improvement-title,
.improvement-sub,
.improvement-content,
.reply-box text,
.linked-feedback text {
  display: block;
}

.dish-name {
  color: #241811;
  font-size: 29rpx;
  font-weight: 900;
}

.feedback-sub {
  margin-top: 8rpx;
  color: #9d8466;
  font-size: 22rpx;
}

.status-badge {
  padding: 10rpx 14rpx;
  border-radius: 999rpx;
  background: #fff1d1;
  color: #e94b35;
  font-size: 21rpx;
  font-weight: 900;
}

.feedback-content {
  margin-top: 20rpx;
  color: #4d3d31;
  font-size: 26rpx;
  line-height: 1.5;
}

.reply-box,
.linked-feedback {
  margin-top: 18rpx;
  padding: 18rpx;
  border-radius: 22rpx;
  background: #fff7ea;
}

.reply-box text:first-child,
.linked-feedback text:first-child {
  color: #e94b35;
  font-size: 22rpx;
  font-weight: 900;
}

.reply-box text:last-child,
.linked-feedback text:last-child {
  margin-top: 8rpx;
  color: #5d4a3b;
  font-size: 24rpx;
  line-height: 1.4;
}

.reply-input,
.form-row textarea {
  width: 100%;
  height: 150rpx;
  margin-top: 18rpx;
  padding: 18rpx;
  border-radius: 22rpx;
  background: #fff7ea;
  color: #241811;
  font-size: 26rpx;
  line-height: 1.45;
}

.actions {
  gap: 10rpx;
  margin-top: 18rpx;
}

.actions button {
  flex: 1;
  height: 56rpx;
  border-radius: 999rpx;
  background: #fff1d1;
  color: #7b6046;
  font-size: 22rpx;
  font-weight: 900;
}

.actions button:first-child,
.actions button:nth-child(2) {
  background: #2b2118;
  color: #fffdf7;
}

.improve-entry {
  margin-top: 18rpx;
  padding: 18rpx;
  border-radius: 22rpx;
  background: #e9f8cf;
}

.improve-entry text:first-child {
  display: block;
  color: #4f8f20;
  font-size: 25rpx;
  font-weight: 900;
}

.improve-entry text:last-child {
  display: block;
  margin-top: 6rpx;
  color: #5d7341;
  font-size: 22rpx;
}

.form-card {
  margin-top: 24rpx;
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
  width: 130rpx;
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

.public-row {
  min-height: 88rpx;
  color: #241811;
  font-size: 26rpx;
  font-weight: 900;
}

.improvement-title {
  color: #241811;
  font-size: 28rpx;
  font-weight: 900;
}

.improvement-sub {
  margin-top: 8rpx;
  color: #9d8466;
  font-size: 22rpx;
}

.improvement-content {
  margin-top: 16rpx;
  color: #5d4a3b;
  font-size: 24rpx;
  line-height: 1.45;
}
</style>
