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
const sort = ref(route.query.sort?.toString() || 'popular')

const title = computed(() => (keyword.value ? copy.movieList.resultsTitle(keyword.value) : copy.movieList.allTitle))

async function loadPage(page = 1) {
  await movieStore.fetchList({
    page,
    size: 12,
    keyword: keyword.value,
    sort: sort.value,
  })
}

onMounted(() => {
  loadPage(Number(route.query.page || 1))
})

watch(
  () => route.query,
  async (query) => {
    keyword.value = query.keyword?.toString() || ''
    sort.value = query.sort?.toString() || 'popular'
    await loadPage(Number(query.page || 1))
  },
)

function submitSearch(value) {
  router.push({
    name: 'movies',
    query: {
      ...(value ? { keyword: value } : {}),
      sort: sort.value,
      page: 1,
    },
  })
}

function changeSort(value) {
  router.push({
    name: 'movies',
    query: {
      ...(keyword.value ? { keyword: keyword.value } : {}),
      ...(value ? { sort: value } : {}),
      page: 1,
    },
  })
}

function changePage(page) {
  router.push({
    name: 'movies',
    query: {
      ...(keyword.value ? { keyword: keyword.value } : {}),
      sort: sort.value,
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
    <div class="listing-header__actions">
      <SearchBar v-model="keyword" @search="submitSearch" />
      <label class="listing-sort">
        <span>{{ copy.movieList.sortLabel }}</span>
        <select :value="sort" @change="changeSort($event.target.value)">
          <option value="popular">{{ copy.movieList.sortOptions.popular }}</option>
          <option value="rating">{{ copy.movieList.sortOptions.rating }}</option>
          <option value="latest">{{ copy.movieList.sortOptions.latest }}</option>
        </select>
      </label>
    </div>
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

.listing-header__actions {
  display: grid;
  gap: 0.9rem;
  justify-items: end;
}

.listing-sort {
  display: grid;
  gap: 0.4rem;
  min-width: 180px;
  color: var(--text-muted);
}

.listing-sort select {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-primary);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  padding: 0.75rem 2.6rem 0.75rem 0.9rem;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='14' height='14' viewBox='0 0 14 14' fill='none'%3E%3Cpath d='M3.5 5.25L7 8.75L10.5 5.25' stroke='%23F4EFE8' stroke-width='1.6' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.9rem center;
  cursor: pointer;
}

.listing-sort select option {
  color: #111827;
  background: #ffffff;
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

  .listing-header__actions {
    justify-items: stretch;
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
