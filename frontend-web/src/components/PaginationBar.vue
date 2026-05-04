<script setup>
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

const props = defineProps({
  page: {
    type: Number,
    required: true,
  },
  size: {
    type: Number,
    required: true,
  },
  total: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits(['update:page'])
const pageCount = computed(() => Math.max(1, Math.ceil(props.total / props.size)))
</script>

<template>
  <div class="pagination-bar">
    <span>共 {{ total }} 条，第 {{ page }} / {{ pageCount }} 页</span>
    <div class="pager-buttons">
      <button class="icon-btn" type="button" :disabled="page <= 1" @click="emit('update:page', page - 1)">
        <ChevronLeft :size="16" />
      </button>
      <button class="icon-btn" type="button" :disabled="page >= pageCount" @click="emit('update:page', page + 1)">
        <ChevronRight :size="16" />
      </button>
    </div>
  </div>
</template>
