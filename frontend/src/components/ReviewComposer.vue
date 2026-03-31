<script setup>
import { computed, reactive, watch } from 'vue'
import { copy } from '../content/copy'

const props = defineProps({
  movieId: {
    type: Number,
    required: true,
  },
  review: {
    type: Object,
    default: null,
  },
  pending: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['submit', 'cancel'])

const form = reactive({
  score: '8.0',
  content: '',
})

const scoreValue = computed(() => Number(form.score || 0).toFixed(1))

watch(
  () => props.review,
  (value) => {
    form.score = value?.score ? String(value.score) : '8.0'
    form.content = value?.content ?? ''
  },
  { immediate: true },
)

function submit() {
  emit('submit', {
    id: props.review?.id,
    movieId: props.movieId,
    score: Number(form.score),
    content: form.content,
  })
}
</script>

<template>
  <form class="composer" @submit.prevent="submit">
    <div class="composer__row">
      <label class="composer__score">
        <span>{{ copy.reviewComposer.score }}</span>
        <div class="composer__score-topline">
          <strong>{{ scoreValue }}</strong>
          <small>{{ copy.reviewComposer.scoreHint }}</small>
        </div>
        <input v-model="form.score" type="range" min="0" max="10" step="0.1" required />
        <div class="composer__ticks" aria-hidden="true">
          <span>0</span>
          <span>5</span>
          <span>10</span>
        </div>
      </label>
      <button v-if="review" type="button" class="ghost" @click="$emit('cancel')">{{ copy.reviewComposer.cancelEdit }}</button>
    </div>
    <label>
      {{ copy.reviewComposer.content }}
      <textarea
        v-model="form.content"
        rows="5"
        maxlength="500"
        :placeholder="copy.reviewComposer.placeholder"
        required
      />
    </label>
    <button type="submit" :disabled="pending">
      {{ review ? copy.reviewComposer.submitUpdate : copy.reviewComposer.submitCreate }}
    </button>
  </form>
</template>

<style scoped>
.composer {
  display: grid;
  gap: 1rem;
  padding: 1.4rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.03);
}

.composer__row {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 1rem;
}

.composer label {
  display: grid;
  gap: 0.5rem;
  color: var(--text-muted);
}

.composer__score {
  flex: 1;
}

.composer__score-topline {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
}

.composer__score-topline strong {
  font-size: 2rem;
  line-height: 1;
  color: var(--text-primary);
  font-family: var(--font-display);
}

.composer__score-topline small {
  color: var(--text-soft);
  text-align: right;
}

.composer input:not([type='range']),
.composer textarea {
  width: 100%;
}

.composer input[type='range'] {
  width: 100%;
  padding: 0;
  border: none;
  border-radius: 999px;
  background: transparent;
  accent-color: var(--accent-gold);
}

.composer__ticks {
  display: flex;
  justify-content: space-between;
  color: var(--text-soft);
  font-size: 0.8rem;
}

.ghost {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.16);
  color: var(--text-primary);
}

@media (max-width: 720px) {
  .composer__row {
    flex-direction: column;
    align-items: stretch;
  }

  .composer__score-topline {
    flex-direction: column;
    align-items: start;
  }
}
</style>
