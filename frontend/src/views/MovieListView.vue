<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import MoviePosterCard from '../components/MoviePosterCard.vue'
import PaginationBar from '../components/PaginationBar.vue'
import SearchBar from '../components/SearchBar.vue'
import EmptyState from '../components/EmptyState.vue'
import { useMovieStore } from '../stores/movies'
import { copy } from '../content/copy'

const route = useRoute()
const router = useRouter()
const movieStore = useMovieStore()
const { list, pagination, listing } = storeToRefs(movieStore)
const keyword = ref(route.query.keyword?.toString() || '')

const title = computed(() => (keyword.value ? copy.movieList.resultsTitle(keyword.value) : copy.movieList.allTitle))

async function loadPage(page = 1) {
  await movieStore.fetchList({
    page,
    size: 12,
    keyword: keyword.value,
  })
}

onMounted(() => {
  loadPage(Number(route.query.page || 1))
})

watch(
  () => route.query,
  async (query) => {
    keyword.value = query.keyword?.toString() || ''
    await loadPage(Number(query.page || 1))
  },
)

function submitSearch(value) {
  router.push({
    name: 'movies',
    query: {
      ...(value ? { keyword: value } : {}),
      page: 1,
    },
  })
}

function changePage(page) {
  router.push({
    name: 'movies',
    query: {
      ...(keyword.value ? { keyword: keyword.value } : {}),
      page,
    },
  })
}
</script>

<template>
  <section class="listing-header">
    <div>
      <span class="eyebrow">{{ copy.movieList.eyebrow }}</span>
      <h1>{{ title }}</h1>
      <p>{{ copy.movieList.description }}</p>
    </div>
    <SearchBar v-model="keyword" @search="submitSearch" />
  </section>

  <section v-if="list.length" class="listing-grid">
    <MoviePosterCard v-for="movie in list" :key="movie.id" :movie="movie" />
  </section>
  <EmptyState
    v-else-if="!listing"
    :title="copy.movieList.emptyTitle"
    :description="copy.movieList.emptyDescription"
  />

  <PaginationBar
    v-if="pagination.total > pagination.size"
    :page="pagination.page"
    :size="pagination.size"
    :total="pagination.total"
    class="listing-pagination"
    @change="changePage"
  />
</template>

<style scoped>
.listing-header {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 1.5rem;
  align-items: end;
  margin: 1rem 0 2rem;
}

.listing-header p {
  max-width: 36rem;
  color: var(--text-muted);
}

.listing-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1.4rem;
}

.listing-pagination {
  margin-top: 2rem;
}

@media (max-width: 960px) {
  .listing-header {
    grid-template-columns: 1fr;
  }

  .listing-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .listing-grid {
    grid-template-columns: 1fr;
  }
}
</style>
