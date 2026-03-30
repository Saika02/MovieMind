<script setup>
import { computed, reactive, ref } from 'vue'
import MoviePosterCard from '../components/MoviePosterCard.vue'
import EmptyState from '../components/EmptyState.vue'
import FormNotice from '../components/FormNotice.vue'
import { recommendWithAgent } from '../services/agent'
import { copy } from '../content/copy'

const form = reactive({
  question: '',
  limit: 6,
})

const loading = ref(false)
const errorMessage = ref('')
const answer = ref('')
const movies = ref([])
const hasSubmitted = ref(false)

const hasMovies = computed(() => movies.value.length > 0)
const canSubmit = computed(() => form.question.trim().length > 0 && !loading.value)

async function submitRecommendation() {
  const question = form.question.trim()
  const limit = Math.min(12, Math.max(1, Number(form.limit) || 6))
  hasSubmitted.value = true
  errorMessage.value = ''
  form.limit = limit

  if (!question) {
    errorMessage.value = copy.recommend.validation
    answer.value = ''
    movies.value = []
    return
  }

  loading.value = true

  try {
    const response = await recommendWithAgent({
      question,
      limit,
    })

    answer.value = response?.answer || ''
    movies.value = response?.movies || []
  } catch (error) {
    answer.value = ''
    movies.value = []
    errorMessage.value = error.message || copy.recommend.errorFallback
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="recommend-hero">
    <div>
      <span class="eyebrow">{{ copy.recommend.eyebrow }}</span>
      <h1>{{ copy.recommend.title }}</h1>
      <p>{{ copy.recommend.description }}</p>
    </div>
  </section>

  <section class="recommend-layout">
    <div class="recommend-panel">
      <div class="recommend-panel__header">
        <h2>{{ copy.recommend.panelTitle }}</h2>
        <p>{{ copy.recommend.panelHint }}</p>
      </div>

      <FormNotice
        v-if="errorMessage"
        :message="errorMessage"
        type="error"
        @close="errorMessage = ''"
      />

      <form class="recommend-form" @submit.prevent="submitRecommendation">
        <label>
          <span>{{ copy.recommend.demandLabel }}</span>
          <textarea
            v-model="form.question"
            rows="6"
            maxlength="300"
            :placeholder="copy.recommend.textareaPlaceholder"
          />
        </label>

        <label class="recommend-form__limit">
          <span>{{ copy.recommend.limitLabel }}</span>
          <input v-model.number="form.limit" type="number" min="1" max="12" />
        </label>

        <button type="submit" :disabled="!canSubmit">
          {{ loading ? copy.common.submitting : copy.recommend.submit }}
        </button>
      </form>
    </div>

    <div class="recommend-results">
      <section v-if="answer" class="answer-panel">
        <span class="eyebrow">{{ copy.recommend.answerTitle }}</span>
        <p>{{ answer }}</p>
      </section>

      <section v-if="hasMovies" class="result-panel">
        <div class="section-title">
          <div>
            <span class="eyebrow">{{ copy.recommend.resultTitle }}</span>
            <h2>{{ copy.recommend.resultHeading }}</h2>
          </div>
        </div>

        <div class="result-grid">
          <MoviePosterCard v-for="movie in movies" :key="movie.id" :movie="movie" />
        </div>
      </section>

      <EmptyState
        v-else-if="hasSubmitted && !loading && !errorMessage"
        :title="copy.recommend.noResultTitle"
        :description="copy.recommend.noResultDescription"
      />

      <EmptyState
        v-else-if="!hasSubmitted && !loading"
        :title="copy.recommend.emptyTitle"
        :description="copy.recommend.emptyDescription"
      />
    </div>
  </section>
</template>

<style scoped>
.recommend-hero {
  margin: 1rem 0 2rem;
}

.recommend-hero p {
  max-width: 48rem;
  color: var(--text-muted);
}

.recommend-layout {
  display: grid;
  grid-template-columns: minmax(300px, 420px) 1fr;
  gap: 1.6rem;
  align-items: start;
}

.recommend-panel,
.answer-panel,
.result-panel {
  padding: 1.6rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.03);
}

.recommend-panel {
  position: sticky;
  top: 5.5rem;
  display: grid;
  gap: 1rem;
}

.recommend-panel__header p,
.answer-panel p {
  color: var(--text-muted);
}

.recommend-form {
  display: grid;
  gap: 1rem;
}

.recommend-form label {
  display: grid;
  gap: 0.5rem;
  color: var(--text-muted);
}

.recommend-form__limit {
  max-width: 10rem;
}

.recommend-results {
  display: grid;
  gap: 1.4rem;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1.4rem;
  margin-top: 1rem;
}

@media (max-width: 1040px) {
  .recommend-layout {
    grid-template-columns: 1fr;
  }

  .recommend-panel {
    position: static;
  }

  .result-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .result-grid {
    grid-template-columns: 1fr;
  }
}
</style>
