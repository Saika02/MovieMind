<script setup>
import { reactive, watch } from 'vue'
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
      <label>
        {{ copy.reviewComposer.score }}
        <input v-model="form.score" type="number" min="0" max="10" step="0.1" required />
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

.composer input,
.composer textarea {
  width: 100%;
}

.ghost {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.16);
  color: var(--text-primary);
}
</style>
