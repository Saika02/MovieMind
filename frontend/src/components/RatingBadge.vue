<script setup>
import { computed } from 'vue'
import { formatScore, formatVotes } from '../utils/formatters'
import { copy } from '../content/copy'

const props = defineProps({
  score: {
    type: [Number, String],
    default: null,
  },
  votes: {
    type: [Number, String],
    default: 0,
  },
  label: {
    type: String,
    default: copy.common.noVotes,
  },
})

const displayScore = computed(() => formatScore(props.score))
const displayVotes = computed(() => formatVotes(props.votes))
</script>

<template>
  <div class="rating">
    <small>{{ label }}</small>
    <strong>{{ displayScore }}</strong>
    <span>{{ displayVotes }}</span>
  </div>
</template>

<style scoped>
.rating {
  display: inline-grid;
  gap: 0.18rem;
  min-width: 7rem;
  padding: 0.9rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.03);
}

.rating small {
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.rating strong {
  font-size: 1.6rem;
  color: var(--accent-gold);
}

.rating span {
  color: var(--text-soft);
}
</style>
