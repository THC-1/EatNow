<script setup>
import { computed } from 'vue'
import { labelFor, toneFor } from '@/utils/format'

const props = defineProps({
  group: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number, Boolean],
    default: '',
  },
})

const label = computed(() => labelFor(props.group, props.value))
const tone = computed(() => toneFor(props.group, props.value))
</script>

<template>
  <span class="status-badge" :class="`tone-${tone}`">{{ label }}</span>
</template>
