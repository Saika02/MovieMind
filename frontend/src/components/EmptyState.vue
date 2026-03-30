<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
})
</script>

<template>
  <div class="empty-state">
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
  </div>
</template>

<style scoped>
.empty-state {
  padding: 2rem;
  border: 1px dashed rgba(255, 255, 255, 0.16);
  border-radius: 24px;
  text-align: center;
  background: rgba(255, 255, 255, 0.02);
}

.empty-state h3 {
  margin-bottom: 0.5rem;
}

.empty-state p {
  margin: 0;
  color: var(--text-muted);
}
</style>
