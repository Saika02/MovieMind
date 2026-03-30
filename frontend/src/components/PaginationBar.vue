<script setup>
import { computed } from 'vue'
import { copy } from '../content/copy'

const props = defineProps({
  page: {
    type: Number,
    default: 1,
  },
  size: {
    type: Number,
    default: 12,
  },
  total: {
    type: Number,
    default: 0,
  },
})

const emit = defineEmits(['change'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))

function goTo(nextPage) {
  if (nextPage < 1 || nextPage > totalPages.value || nextPage === props.page) {
    return
  }

  emit('change', nextPage)
}
</script>

<template>
  <div class="pagination">
    <button type="button" :disabled="page <= 1" @click="goTo(page - 1)">{{ copy.common.previousPage }}</button>
    <span>{{ copy.common.pageLabel(page, totalPages) }}</span>
    <button type="button" :disabled="page >= totalPages" @click="goTo(page + 1)">{{ copy.common.nextPage }}</button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.03);
}

.pagination span {
  color: var(--text-muted);
}
</style>
