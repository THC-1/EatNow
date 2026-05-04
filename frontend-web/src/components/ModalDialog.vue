<script setup>
import { X } from 'lucide-vue-next'

defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '',
  },
  wide: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close'])
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="modal-backdrop" @click.self="emit('close')">
      <section class="modal-panel" :class="{ 'is-wide': wide }">
        <header class="modal-head">
          <h3>{{ title }}</h3>
          <button class="icon-btn" type="button" title="关闭" @click="emit('close')">
            <X :size="18" />
          </button>
        </header>
        <slot />
      </section>
    </div>
  </Teleport>
</template>
