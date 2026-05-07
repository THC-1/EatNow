<template>
  <view class="page ai-page">
    <view class="nav">
      <view class="back" @tap="goBack"></view>
      <view>
        <text class="nav-kicker">EatNow</text>
        <text class="nav-title">AI 点餐</text>
      </view>
      <view class="status-dot" :class="{ muted: moduleUnavailable }"></view>
    </view>

    <view class="hero">
      <view class="hero-copy">
        <text class="hero-title">先聊两句，\n再认真选菜</text>
        <text class="hero-sub">它会记住你常吃的、避开的，也会照顾这一次的小状况。</text>
      </view>
      <view class="plate-scene">
        <view class="plate"></view>
        <view class="steam one"></view>
        <view class="steam two"></view>
        <view class="steam three"></view>
      </view>
    </view>

    <view class="chips">
      <view v-for="item in quickPrompts" :key="item" class="chip" @tap="sendPreset(item)">
        <text>{{ item }}</text>
      </view>
    </view>

    <scroll-view
      class="chat"
      scroll-y
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
      show-scrollbar="false"
    >
      <view
        v-for="(message, index) in messages"
        :id="`msg-${index}`"
        :key="message.id"
        class="message-wrap"
        :class="message.role"
      >
        <view class="bubble">
          <text class="bubble-text">{{ message.content }}</text>
        </view>

        <view v-if="message.recommendations && message.recommendations.length" class="recommend-list">
          <view
            v-for="dish in message.recommendations"
            :key="dish.dishId"
            class="recommend-card"
            @tap="goDish(dish.dishId)"
          >
            <view class="dish-cover">
              <image v-if="dishImage(dish)" :src="dishImage(dish)" mode="aspectFill" />
              <text v-else>{{ shortName(dish.name) }}</text>
            </view>
            <view class="dish-info">
              <view class="dish-head">
                <text class="dish-name">{{ dish.name }}</text>
                <text class="dish-price">¥{{ price(dish.price) }}</text>
              </view>
              <text class="dish-place">{{ dish.canteenName || '校内' }} · {{ dish.stallName || dish.merchantName || '窗口' }}</text>
              <text class="dish-reason">{{ dish.recommendReason || '综合你的偏好和当前条件推荐。' }}</text>
              <view class="dish-actions">
                <text class="dish-score">{{ dish.score || '4.5' }} 分</text>
                <button v-if="!isMerchant" class="want-btn" @tap.stop="wantEat(dish)">想吃</button>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="sending" id="msg-loading" class="message-wrap assistant">
        <view class="bubble loading-bubble">
          <view class="typing-dot"></view>
          <view class="typing-dot"></view>
          <view class="typing-dot"></view>
        </view>
      </view>
    </scroll-view>

    <view v-if="moduleUnavailable" class="notice">
      <text>智能推荐暂时没有连上，其他功能照常可用。</text>
    </view>

    <view class="composer">
      <input
        v-model="draft"
        class="composer-input"
        confirm-type="send"
        placeholder="比如：想吃清淡点，预算 15"
        :disabled="sending"
        @confirm="sendDraft"
      />
      <button class="send-btn" :class="{ disabled: sending || !draft.trim() }" @tap="sendDraft">
        <text>发送</text>
      </button>
    </view>
  </view>
</template>

<script>
import { chatWithMerchantLlmRecommendation } from '../../services/merchant.js'
import { chatWithLlmRecommendation, createEatList } from '../../services/student.js'
import { assetUrl, hasToken } from '../../utils/request.js'

const CONVERSATION_KEY = 'eatnow_ai_conversation_id'

export default {
  data() {
    return {
      role: '',
      conversationId: '',
      draft: '',
      sending: false,
      moduleUnavailable: false,
      scrollIntoView: '',
      messages: [
        {
          id: 'welcome',
          role: 'assistant',
          content: '今天想吃什么方向？你可以先说口味、预算、食堂，准备好了再让我推荐。'
        }
      ],
      quickPrompts: ['清淡一点', '15 元以内', '想吃辣的', '可以了，帮我推荐']
    }
  },
  onLoad() {
    this.role = uni.getStorageSync('eatnow_role') || 'STUDENT'
    this.conversationId = uni.getStorageSync(this.conversationKey()) || ''
  },
  computed: {
    isMerchant() {
      return this.role === 'MERCHANT'
    }
  },
  methods: {
    goBack() {
      uni.navigateBack({
        fail: () => uni.switchTab({ url: '/pages/index/index' })
      })
    },
    requireLogin() {
      if (hasToken()) {
        return true
      }
      uni.showToast({ title: '请先登录再使用 AI 点餐', icon: 'none' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/mine/index' })
      }, 600)
      return false
    },
    sendPreset(text) {
      if (this.sending) return
      this.sendMessage(text)
    },
    sendDraft() {
      if (this.sending) return
      const text = this.draft.trim()
      if (!text) return
      this.sendMessage(text)
    },
    async sendMessage(text) {
      if (!this.requireLogin()) return
      const content = text.trim()
      if (!content) return
      this.draft = ''
      this.addMessage({ role: 'user', content })
      this.sending = true
      this.moduleUnavailable = false
      this.scrollLoading()
      try {
        const data = await this.sendChatRequest({
          conversationId: this.conversationId || undefined,
          message: content,
          limit: 3
        })
        this.conversationId = data.conversationId || this.conversationId
        if (this.conversationId) {
          uni.setStorageSync(this.conversationKey(), this.conversationId)
        }
        this.addMessage({
          role: 'assistant',
          content: data.reply || '我再确认一下你的口味和预算。',
          action: data.action || 'ASK',
          recommendations: data.recommendations || []
        })
      } catch (error) {
        this.moduleUnavailable = true
        this.addMessage({
          role: 'assistant',
          content: this.errorText(error)
        })
      } finally {
        this.sending = false
        this.scrollToBottom()
      }
    },
    addMessage(message) {
      this.messages.push({
        id: `${Date.now()}-${this.messages.length}`,
        recommendations: [],
        ...message
      })
      this.$nextTick(this.scrollToBottom)
    },
    conversationKey() {
      return `${CONVERSATION_KEY}_${this.role || 'STUDENT'}`
    },
    sendChatRequest(payload) {
      if (this.isMerchant) {
        return chatWithMerchantLlmRecommendation(payload)
      }
      return chatWithLlmRecommendation(payload)
    },
    scrollToBottom() {
      this.scrollIntoView = `msg-${Math.max(this.messages.length - 1, 0)}`
    },
    scrollLoading() {
      this.$nextTick(() => {
        this.scrollIntoView = 'msg-loading'
      })
    },
    errorText(error) {
      const message = error && error.message ? error.message : ''
      if (message.includes('503') || message.includes('disabled')) {
        return '智能推荐现在还没开启。你可以先去抽签或菜品页看看。'
      }
      if (message.includes('401')) {
        return '登录状态过期了，先去我的页面重新登录一下。'
      }
      return '这次没有连上智能推荐，稍后再试。'
    },
    dishImage(dish) {
      const image = dish && dish.images && dish.images.length ? dish.images[0] : ''
      return assetUrl(image)
    },
    shortName(name = '') {
      return name ? name.slice(0, 2) : '饭'
    },
    price(value) {
      if (value === undefined || value === null || value === '') {
        return '--'
      }
      return Number(value).toFixed(0)
    },
    goDish(id) {
      if (!id) return
      uni.navigateTo({ url: `/pages/dish-detail/index?id=${id}` })
    },
    async wantEat(dish) {
      if (!this.requireLogin()) return
      try {
        await createEatList(dish.dishId)
        uni.showToast({ title: '已加入想吃', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: error.message || '加入失败', icon: 'none' })
      }
    }
  }
}
</script>

<style>
.ai-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 68rpx 28rpx 24rpx;
  background:
    linear-gradient(90deg, rgba(36, 24, 17, 0.04) 1rpx, transparent 1rpx),
    linear-gradient(180deg, rgba(36, 24, 17, 0.04) 1rpx, transparent 1rpx),
    radial-gradient(circle at 18% 8%, rgba(255, 214, 78, 0.38), transparent 260rpx),
    radial-gradient(circle at 82% 18%, rgba(127, 189, 65, 0.22), transparent 260rpx),
    #fff7ea;
  background-size: 44rpx 44rpx, 44rpx 44rpx, auto, auto, auto;
}

.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back {
  position: relative;
  width: 58rpx;
  height: 58rpx;
  border-radius: 16rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.1);
}

.back::before {
  content: "";
  position: absolute;
  left: 22rpx;
  top: 18rpx;
  width: 18rpx;
  height: 18rpx;
  border-left: 5rpx solid #2b2118;
  border-bottom: 5rpx solid #2b2118;
  transform: rotate(45deg);
}

.nav-kicker,
.nav-title {
  display: block;
  text-align: center;
}

.nav-kicker {
  color: #7f8f2e;
  font-size: 20rpx;
  font-weight: 900;
  letter-spacing: 0;
}

.nav-title {
  margin-top: 2rpx;
  color: #241811;
  font-size: 34rpx;
  font-weight: 900;
}

.status-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #7fbd41;
  box-shadow: 0 0 0 10rpx rgba(127, 189, 65, 0.12);
}

.status-dot.muted {
  background: #e94b35;
  box-shadow: 0 0 0 10rpx rgba(233, 75, 53, 0.12);
}

.hero {
  position: relative;
  display: flex;
  min-height: 252rpx;
  margin-top: 26rpx;
  padding: 28rpx;
  overflow: hidden;
  border-radius: 16rpx;
  background:
    linear-gradient(135deg, rgba(255, 253, 247, 0.96), rgba(255, 238, 195, 0.96)),
    #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
  box-shadow: 0 24rpx 40rpx rgba(93, 74, 59, 0.1);
}

.hero::after {
  content: "";
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  bottom: 24rpx;
  height: 4rpx;
  background: repeating-linear-gradient(90deg, #e94b35 0 24rpx, transparent 24rpx 36rpx);
  opacity: 0.42;
}

.hero-copy {
  position: relative;
  z-index: 1;
  flex: 1;
}

.hero-title {
  display: block;
  color: #241811;
  font-size: 50rpx;
  font-weight: 900;
  line-height: 1.08;
}

.hero-sub {
  display: block;
  width: 400rpx;
  margin-top: 14rpx;
  color: #6b5644;
  font-size: 24rpx;
  line-height: 1.42;
}

.plate-scene {
  position: relative;
  width: 190rpx;
  height: 190rpx;
  margin-top: 18rpx;
}

.plate {
  position: absolute;
  right: 0;
  bottom: 8rpx;
  width: 156rpx;
  height: 156rpx;
  border-radius: 50%;
  background:
    radial-gradient(circle, #fffdf7 0 34rpx, #ffd64e 35rpx 68rpx, #e94b35 69rpx 78rpx, #fffdf7 79rpx);
  box-shadow: 0 20rpx 30rpx rgba(233, 75, 53, 0.18);
}

.steam {
  position: absolute;
  top: 0;
  width: 16rpx;
  height: 70rpx;
  border-radius: 999rpx;
  background: rgba(127, 189, 65, 0.36);
  transform: rotate(18deg);
}

.steam.one {
  right: 34rpx;
}

.steam.two {
  right: 78rpx;
  height: 92rpx;
  background: rgba(233, 75, 53, 0.24);
}

.steam.three {
  right: 122rpx;
  height: 58rpx;
}

.chips {
  display: flex;
  gap: 14rpx;
  margin-top: 20rpx;
  overflow-x: auto;
  white-space: nowrap;
}

.chip {
  flex: 0 0 auto;
  padding: 16rpx 22rpx;
  border-radius: 999rpx;
  background: #241811;
  color: #fffdf7;
  font-size: 24rpx;
  font-weight: 800;
}

.chat {
  flex: 1;
  height: 1rpx;
  margin-top: 20rpx;
  padding-bottom: 18rpx;
}

.message-wrap {
  margin-bottom: 20rpx;
}

.message-wrap.user {
  display: flex;
  justify-content: flex-end;
}

.message-wrap.assistant {
  display: block;
}

.bubble {
  max-width: 560rpx;
  padding: 20rpx 22rpx;
  border-radius: 16rpx;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
}

.assistant .bubble {
  background: #fffdf7;
  color: #2b2118;
  box-shadow: 0 12rpx 24rpx rgba(93, 74, 59, 0.08);
}

.user .bubble {
  background: #e94b35;
  color: #fffdf7;
  border-color: #e94b35;
}

.bubble-text {
  font-size: 27rpx;
  line-height: 1.48;
}

.loading-bubble {
  display: flex;
  gap: 10rpx;
  width: 116rpx;
  min-height: 62rpx;
  align-items: center;
  justify-content: center;
}

.typing-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #e94b35;
  opacity: 0.45;
}

.recommend-list {
  margin-top: 16rpx;
}

.recommend-card {
  display: flex;
  gap: 18rpx;
  margin-bottom: 16rpx;
  padding: 16rpx;
  border-radius: 16rpx;
  background: #fffdf7;
  border: 2rpx solid rgba(43, 33, 24, 0.08);
  box-shadow: 0 18rpx 32rpx rgba(93, 74, 59, 0.08);
}

.dish-cover {
  position: relative;
  flex: 0 0 148rpx;
  width: 148rpx;
  height: 148rpx;
  overflow: hidden;
  border-radius: 14rpx;
  background:
    radial-gradient(circle at 28% 24%, rgba(255, 253, 247, 0.86) 0 22rpx, transparent 23rpx),
    linear-gradient(135deg, #ffd64e, #ff8b4a 58%, #e94b35);
}

.dish-cover image {
  width: 100%;
  height: 100%;
}

.dish-cover text {
  position: absolute;
  left: 18rpx;
  bottom: 16rpx;
  color: #fffdf7;
  font-size: 38rpx;
  font-weight: 900;
}

.dish-info {
  flex: 1;
  min-width: 0;
}

.dish-head,
.dish-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.dish-name {
  flex: 1;
  min-width: 0;
  color: #241811;
  font-size: 30rpx;
  font-weight: 900;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.dish-price {
  color: #e94b35;
  font-size: 30rpx;
  font-weight: 900;
}

.dish-place {
  display: block;
  margin-top: 8rpx;
  color: #8a7a68;
  font-size: 22rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.dish-reason {
  display: block;
  height: 64rpx;
  margin-top: 10rpx;
  color: #5d4a3b;
  font-size: 23rpx;
  line-height: 1.36;
  overflow: hidden;
}

.dish-actions {
  margin-top: 12rpx;
}

.dish-score {
  color: #4f8f20;
  font-size: 23rpx;
  font-weight: 900;
}

.want-btn {
  width: 108rpx;
  height: 50rpx;
  border-radius: 999rpx;
  background: #241811;
  color: #fffdf7;
  font-size: 22rpx;
  font-weight: 900;
}

.notice {
  margin-bottom: 14rpx;
  padding: 14rpx 18rpx;
  border-radius: 14rpx;
  background: rgba(233, 75, 53, 0.1);
  color: #a93a28;
  font-size: 23rpx;
  font-weight: 800;
}

.composer {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 14rpx;
  border-radius: 16rpx;
  background: rgba(255, 253, 247, 0.96);
  border: 2rpx solid rgba(43, 33, 24, 0.1);
  box-shadow: 0 -10rpx 30rpx rgba(93, 74, 59, 0.08);
}

.composer-input {
  flex: 1;
  height: 72rpx;
  padding: 0 18rpx;
  border-radius: 12rpx;
  background: #fff7ea;
  color: #241811;
  font-size: 26rpx;
}

.send-btn {
  width: 116rpx;
  height: 72rpx;
  border-radius: 12rpx;
  background: #e94b35;
  color: #fffdf7;
  font-size: 25rpx;
  font-weight: 900;
}

.send-btn.disabled {
  opacity: 0.45;
}
</style>
