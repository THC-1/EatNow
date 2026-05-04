<script setup>
import ModalDialog from '@/components/ModalDialog.vue'

defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '确认操作',
  },
  message: {
    type: String,
    required: true,
  },
  danger: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'confirm'])
</script>

<template>
  <ModalDialog :open="open" :title="title" @close="emit('close')">
    <p class="confirm-message">{{ message }}</p>
    <footer class="modal-actions">
      <button class="secondary-button" type="button" @click="emit('close')">取消</button>
      <button :class="danger ? 'danger-button' : 'primary-button'" type="button" @click="emit('confirm')">确认</button>
    </footer>
  </ModalDialog>
</template>
