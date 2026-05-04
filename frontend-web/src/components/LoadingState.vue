<script setup>
defineProps({
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: '',
  },
  empty: {
    type: Boolean,
    default: false,
  },
  emptyText: {
    type: String,
    default: '暂无数据',
  },
})
</script>

<template>
  <div v-if="loading" class="state-card">正在加载数据...</div>
  <div v-else-if="error" class="state-card is-error">{{ error }}</div>
  <div v-else-if="empty" class="state-card">{{ emptyText }}</div>
  <slot v-else />
</template>
