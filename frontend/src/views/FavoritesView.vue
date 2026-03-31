<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import EmptyState from '../components/EmptyState.vue'
import PaginationBar from '../components/PaginationBar.vue'
import FormNotice from '../components/FormNotice.vue'
import { useFavoriteStore } from '../stores/favorites'
import { getMovies } from '../services/movies'
import { formatDate, normalizePoster, splitTags } from '../utils/formatters'
import { copy } from '../content/copy'

const favoriteStore = useFavoriteStore()
const { pageData, busy } = storeToRefs(favoriteStore)
const favoriteEntries = ref([])
const noticeMessage = ref('')
const noticeType = ref('success')

const favoriteMovies = computed(() => favoriteEntries.value)

async function load(page = 1) {
  const response = await favoriteStore.fetchPage(page, 12)
  const favorites = response.items ?? []
  const allMovies = await getMovies()
  const movieMap = new Map(allMovies.map((movie) => [movie.id, movie]))
  favoriteEntries.value = favorites
    .map((item) => {
      const movie = movieMap.get(item.movieId)
      if (!movie) return null

      return {
        favoriteId: item.id,
        ...movie,
      }
    })
    .filter(Boolean)
}

async function removeFavorite(entry) {
  if (!globalThis.confirm(copy.favorites.removeConfirm)) return

  try {
    await favoriteStore.removeFavorite(entry.favoriteId)
    noticeMessage.value = copy.favorites.removeSuccess
    noticeType.value = 'success'

    const isLastItemOnPage = favoriteEntries.value.length === 1 && pageData.value.page > 1
    const nextPage = isLastItemOnPage ? pageData.value.page - 1 : pageData.value.page
    await load(nextPage)
  } catch (error) {
    noticeMessage.value = error.message
    noticeType.value = 'error'
  }
}

onMounted(() => {
  load()
})
</script>

<template>
  <section class="page-head">
    <div>
      <span class="eyebrow">{{ copy.favorites.eyebrow }}</span>
      <h1>{{ copy.favorites.title }}</h1>
      <p>{{ copy.favorites.description }}</p>
    </div>
  </section>

  <FormNotice v-if="noticeMessage" :message="noticeMessage" :type="noticeType" @close="noticeMessage = ''" />

  <section v-if="favoriteMovies.length" class="favorites-grid">
    <article v-for="movie in favoriteMovies" :key="movie.favoriteId" class="favorite-card">
      <RouterLink :to="`/movies/${movie.id}`" class="favorite-card__link">
        <div class="favorite-card__poster">
          <img :src="normalizePoster(movie.posterFile)" :alt="movie.title" />
        </div>
        <div class="favorite-card__meta">
          <div class="favorite-card__topline">
            <span>{{ formatDate(movie.releaseDate) }}</span>
            <strong>{{ movie.tmdbVoteAverage ? Number(movie.tmdbVoteAverage).toFixed(1) : '--' }}</strong>
          </div>
          <h3>{{ movie.title }}</h3>
          <p>{{ movie.tagline || copy.common.fallbackTagline }}</p>
          <div class="favorite-card__genres">
            <span v-for="genre in splitTags(movie.genres).slice(0, 2)" :key="genre">{{ genre }}</span>
          </div>
        </div>
      </RouterLink>
      <button
        type="button"
        class="ghost favorite-card__remove"
        :disabled="busy"
        @click="removeFavorite(movie)"
      >
        {{ copy.favorites.removeAction }}
      </button>
    </article>
  </section>
  <EmptyState
    v-else
    :title="copy.favorites.emptyTitle"
    :description="copy.favorites.emptyDescription"
  />

  <PaginationBar
    v-if="pageData.total > pageData.size"
    :page="pageData.page"
    :size="pageData.size"
    :total="pageData.total"
    class="favorites-pagination"
    @change="load"
  />
</template>

<style scoped>
.page-head {
  margin: 1rem 0 2rem;
}

.page-head p {
  max-width: 35rem;
  color: var(--text-muted);
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1.4rem;
}

.favorite-card {
  display: grid;
  gap: 0.9rem;
}

.favorite-card__link {
  display: grid;
  gap: 1rem;
  color: var(--text-primary);
  transition: transform 220ms ease;
}

.favorite-card__link:hover {
  transform: translateY(-5px);
}

.favorite-card__poster {
  aspect-ratio: 0.72;
  overflow: hidden;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.05);
}

.favorite-card__poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-card__meta {
  display: grid;
  gap: 0.6rem;
}

.favorite-card__topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--text-muted);
  font-size: 0.88rem;
}

.favorite-card__meta h3,
.favorite-card__meta p {
  margin: 0;
}

.favorite-card__meta p {
  color: var(--text-soft);
}

.favorite-card__genres {
  display: flex;
  flex-wrap: wrap;
  gap: 0.55rem;
}

.favorite-card__genres span {
  padding: 0.35rem 0.7rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  color: var(--text-muted);
  font-size: 0.8rem;
}

.favorite-card__remove {
  justify-self: start;
}

.favorites-pagination {
  margin-top: 2rem;
}

@media (max-width: 960px) {
  .favorites-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .favorites-grid {
    grid-template-columns: 1fr;
  }
}
</style>
