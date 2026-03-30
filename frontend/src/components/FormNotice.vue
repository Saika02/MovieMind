<script setup>
import { copy } from '../content/copy'

defineProps({
  message: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: 'info',
  },
})

defineEmits(['close'])
</script>

<template>
  <div class="notice" :data-type="type">
    <span>{{ message }}</span>
    <button type="button" class="notice__close" @click="$emit('close')">{{ copy.common.close }}</button>
  </div>
</template>

<style scoped>
.notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.9rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  background: rgba(10, 16, 24, 0.94);
  color: var(--text-primary);
  box-shadow: var(--shadow-soft);
}

.notice[data-type='success'] {
  border-color: rgba(94, 215, 160, 0.34);
}

.notice[data-type='error'] {
  border-color: rgba(255, 108, 108, 0.34);
}

.notice__close {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--accent-gold);
}
</style>
