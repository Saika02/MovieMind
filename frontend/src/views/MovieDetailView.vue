<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import RatingBadge from '../components/RatingBadge.vue'
import ReviewComposer from '../components/ReviewComposer.vue'
import PaginationBar from '../components/PaginationBar.vue'
import EmptyState from '../components/EmptyState.vue'
import { useMovieStore } from '../stores/movies'
import { useFavoriteStore } from '../stores/favorites'
import { useReviewStore } from '../stores/reviews'
import { useAuthStore } from '../stores/auth'
import { formatDate, normalizePoster, splitTags } from '../utils/formatters'
import { copy } from '../content/copy'

const route = useRoute()
const movieStore = useMovieStore()
const favoriteStore = useFavoriteStore()
const reviewStore = useReviewStore()
const authStore = useAuthStore()

const { detail, detailLoading } = storeToRefs(movieStore)
const { status, busy: favoriteBusy } = storeToRefs(favoriteStore)
const { moviePage, busy: reviewBusy } = storeToRefs(reviewStore)
const editingReview = ref(null)
const localError = ref('')

const posterUrl = computed(() => normalizePoster(detail.value?.posterFile))
const genreTags = computed(() => splitTags(detail.value?.genres))
const castTags = computed(() => splitTags(detail.value?.castList))
const mine = computed(
  () => moviePage.value.items.find((item) => item.userId === authStore.session.userId) ?? null,
)

async function load(movieId) {
  localError.value = ''
  editingReview.value = null

  try {
    await Promise.all([
      movieStore.fetchDetail(movieId),
      favoriteStore.fetchStatus(movieId),
      reviewStore.fetchMovieReviews(movieId, 1, 8),
    ])
  } catch (error) {
    localError.value = error.message
  }
}

onMounted(() => {
  load(Number(route.params.id))
})

watch(
  () => route.params.id,
  (value) => {
    load(Number(value))
  },
)

async function toggleFavorite() {
  await favoriteStore.toggle(Number(route.params.id))
}

async function submitReview(payload) {
  await reviewStore.saveReview(payload)
  editingReview.value = null
  await reviewStore.fetchMovieReviews(Number(route.params.id), moviePage.value.page, moviePage.value.size)
}

async function removeReview(id) {
  await reviewStore.removeReview(id)
  editingReview.value = null
  await reviewStore.fetchMovieReviews(Number(route.params.id), 1, 8)
}

function editReview(review) {
  editingReview.value = review
}

function cancelEdit() {
  editingReview.value = null
}

async function changeReviewPage(page) {
  await reviewStore.fetchMovieReviews(Number(route.params.id), page, moviePage.value.size)
}
</script>

<template>
  <div v-if="localError" class="page-error">{{ localError }}</div>
  <template v-else-if="detail">
    <section class="detail-hero">
      <div class="detail-hero__poster">
        <img :src="posterUrl" :alt="detail.title" />
      </div>

      <div class="detail-hero__copy">
        <span class="eyebrow">{{ copy.movieDetail.eyebrow }}</span>
        <h1>{{ detail.title }}</h1>
        <p class="detail-hero__tagline">{{ detail.tagline || copy.movieDetail.fallbackTagline }}</p>
        <p class="detail-hero__overview">{{ detail.overview }}</p>

        <div class="detail-hero__chips">
          <span>{{ formatDate(detail.releaseDate) }}</span>
          <span v-if="detail.runtime">{{ detail.runtime }} {{ copy.movieDetail.runtimeUnit }}</span>
          <span v-for="genre in genreTags" :key="genre">{{ genre }}</span>
        </div>

        <div class="detail-hero__ratings">
          <RatingBadge label="TMDB" :score="detail.tmdbVoteAverage" :votes="detail.tmdbVoteCount" />
          <RatingBadge label="MovieMind" :score="detail.siteVoteAverage" :votes="detail.siteVoteCount" />
        </div>

        <div class="detail-hero__actions">
          <button type="button" :disabled="favoriteBusy" @click="toggleFavorite">
            {{ status?.favorited ? copy.movieDetail.favoriteRemove : copy.movieDetail.favoriteAdd }}
          </button>
          <span class="detail-hero__status">
            {{ status?.favorited ? copy.movieDetail.favoriteSaved : copy.movieDetail.favoriteUnsaved }}
          </span>
        </div>
      </div>
    </section>

    <section class="detail-grid">
      <article>
        <span class="eyebrow">{{ copy.movieDetail.castEyebrow }}</span>
        <div class="token-wrap">
          <span v-for="member in castTags" :key="member">{{ member }}</span>
        </div>
      </article>

      <article>
        <span class="eyebrow">{{ copy.movieDetail.productionEyebrow }}</span>
        <p>{{ detail.productionCompanies || copy.movieDetail.productionFallback }}</p>
        <p>{{ copy.movieDetail.keywordsLabel }}：{{ detail.keywords || copy.movieDetail.keywordsFallback }}</p>
        <p>{{ copy.movieDetail.producersLabel }}：{{ detail.producers || copy.movieDetail.producersFallback }}</p>
      </article>
    </section>

    <section class="review-section">
      <div class="section-title">
        <div>
          <span class="eyebrow">{{ copy.movieDetail.reviewsEyebrow }}</span>
          <h2>{{ copy.movieDetail.reviewsTitle }}</h2>
        </div>
      </div>

      <ReviewComposer
        :movie-id="Number(route.params.id)"
        :review="editingReview || mine"
        :pending="reviewBusy"
        @submit="submitReview"
        @cancel="cancelEdit"
      />

      <div v-if="moviePage.items.length" class="review-list">
        <article v-for="review in moviePage.items" :key="review.id" class="review-card">
          <div class="review-card__header">
            <div>
              <strong>{{ review.username }}</strong>
              <small>{{ formatDate(review.updatedAt || review.createdAt) }}</small>
            </div>
            <span>{{ Number(review.score).toFixed(1) }}</span>
          </div>
          <p>{{ review.content }}</p>
          <div v-if="review.userId === authStore.session.userId" class="review-card__actions">
            <button type="button" class="ghost" @click="editReview(review)">{{ copy.reviewActions.edit }}</button>
            <button type="button" class="ghost" @click="removeReview(review.id)">{{ copy.reviewActions.delete }}</button>
          </div>
        </article>
      </div>
      <EmptyState
        v-else
        :title="copy.movieDetail.emptyTitle"
        :description="copy.movieDetail.emptyDescription"
      />

      <PaginationBar
        v-if="moviePage.total > moviePage.size"
        :page="moviePage.page"
        :size="moviePage.size"
        :total="moviePage.total"
        @change="changeReviewPage"
      />
    </section>
  </template>
  <div v-else-if="detailLoading" class="page-error">{{ copy.common.loadingMovieDetail }}</div>
</template>

<style scoped>
.page-error {
  padding: 2rem;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.03);
}

.detail-hero {
  display: grid;
  grid-template-columns: minmax(260px, 360px) 1fr;
  gap: 2rem;
  align-items: center;
}

.detail-hero__poster {
  overflow: hidden;
  border-radius: 30px;
  aspect-ratio: 0.72;
}

.detail-hero__poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-hero__tagline {
  color: var(--accent-gold);
  font-size: 1.05rem;
}

.detail-hero__overview,
.detail-grid p,
.detail-hero__status {
  color: var(--text-muted);
}

.detail-hero__chips,
.detail-hero__ratings,
.token-wrap,
.review-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.7rem;
}

.detail-hero__chips span,
.token-wrap span {
  padding: 0.45rem 0.8rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.05);
}

.detail-hero__actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1.2rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
}

.detail-grid article,
.review-section {
  padding: 1.6rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.03);
}

.review-section {
  margin-top: 2rem;
  display: grid;
  gap: 1.4rem;
}

.review-list {
  display: grid;
  gap: 1rem;
}

.review-card {
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.review-card:first-child {
  padding-top: 0;
  border-top: none;
}

.review-card__header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 1rem;
}

.review-card__header small {
  display: block;
  margin-top: 0.3rem;
  color: var(--text-soft);
}

.ghost {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.16);
  color: var(--text-primary);
}

@media (max-width: 960px) {
  .detail-hero,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-hero__actions {
    flex-direction: column;
    align-items: start;
  }
}
</style>
