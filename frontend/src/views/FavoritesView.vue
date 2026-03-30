<script setup>
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import MoviePosterCard from '../components/MoviePosterCard.vue'
import EmptyState from '../components/EmptyState.vue'
import PaginationBar from '../components/PaginationBar.vue'
import { useFavoriteStore } from '../stores/favorites'
import { getMovies } from '../services/movies'
import { copy } from '../content/copy'

const favoriteStore = useFavoriteStore()
const { pageData } = storeToRefs(favoriteStore)

const favoriteMovies = computed(() => pageData.value.items ?? [])

async function load(page = 1) {
  const response = await favoriteStore.fetchPage(page, 12)
  const favorites = response.items ?? []
  const allMovies = await getMovies()
  const ids = new Set(favorites.map((item) => item.movieId))
  pageData.value = {
    ...response,
    items: allMovies.filter((movie) => ids.has(movie.id)),
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

  <section v-if="favoriteMovies.length" class="favorites-grid">
    <MoviePosterCard v-for="movie in favoriteMovies" :key="movie.id" :movie="movie" />
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
