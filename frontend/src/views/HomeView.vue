<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import MoviePosterCard from '../components/MoviePosterCard.vue'
import SearchBar from '../components/SearchBar.vue'
import EmptyState from '../components/EmptyState.vue'
import { useMovieStore } from '../stores/movies'
import { normalizePoster, splitTags } from '../utils/formatters'
import { copy } from '../content/copy'

const router = useRouter()
const movieStore = useMovieStore()
const { featured, loading } = storeToRefs(movieStore)
const keyword = ref('')

const heroMovie = computed(() => featured.value?.items?.[0] ?? null)
const galleryMovies = computed(() => featured.value?.items?.slice(1) ?? [])
const heroGenres = computed(() => splitTags(heroMovie.value?.genres).slice(0, 3))
const heroPoster = computed(() => normalizePoster(heroMovie.value?.posterFile))

onMounted(async () => {
  await movieStore.fetchFeatured()
})

function submitSearch(value) {
  router.push({
    name: 'movies',
    query: value ? { keyword: value } : {},
  })
}
</script>

<template>
  <section class="hero">
    <div
      class="hero__visual"
      :style="{ backgroundImage: `linear-gradient(90deg, rgba(3, 7, 11, 0.72), rgba(3, 7, 11, 0.24)), url(${heroPoster})` }"
    />
    <div class="hero__copy">
      <span class="eyebrow">{{ copy.home.eyebrow }}</span>
      <h1>{{ copy.home.title }}</h1>
      <p>{{ copy.home.description }}</p>
      <SearchBar v-model="keyword" :placeholder="copy.search.heroPlaceholder" @search="submitSearch" />
      <div class="hero__meta" v-if="heroMovie">
        <strong>{{ heroMovie.title }}</strong>
        <div class="hero__chips">
          <span v-for="genre in heroGenres" :key="genre">{{ genre }}</span>
        </div>
      </div>
    </div>
  </section>

  <section class="section-heading">
    <div>
      <span class="eyebrow">{{ copy.home.featuredEyebrow }}</span>
      <h2>{{ copy.home.featuredTitle }}</h2>
    </div>
    <RouterLink class="text-link" to="/movies">{{ copy.home.openCatalogue }}</RouterLink>
  </section>

  <section v-if="!loading && galleryMovies.length" class="poster-grid">
    <MoviePosterCard v-for="movie in galleryMovies" :key="movie.id" :movie="movie" />
  </section>
  <EmptyState
    v-else-if="!loading"
    :title="copy.home.emptyTitle"
    :description="copy.home.emptyDescription"
  />

  <section class="support">
    <div>
      <span class="eyebrow">{{ copy.home.whyEyebrow }}</span>
      <h2>{{ copy.home.whyTitle }}</h2>
    </div>
    <div class="support__list">
      <article v-for="item in copy.home.highlights" :key="item.title">
        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  min-height: calc(100svh - 110px);
  display: grid;
  align-items: end;
  overflow: hidden;
  border-radius: 34px;
}

.hero__visual {
  position: absolute;
  inset: 0;
  background-position: center;
  background-size: cover;
  transform: scale(1.02);
}

.hero__copy {
  position: relative;
  z-index: 1;
  max-width: 40rem;
  padding: 3rem;
}

.hero__copy h1 {
  margin-bottom: 1rem;
  font-size: clamp(3rem, 7vw, 5.7rem);
}

.hero__copy p {
  max-width: 32rem;
  margin-bottom: 1.5rem;
  color: rgba(244, 239, 232, 0.82);
  font-size: 1.05rem;
}

.hero__meta {
  margin-top: 1.5rem;
}

.hero__meta strong {
  display: block;
  margin-bottom: 0.8rem;
  font-size: 1.2rem;
}

.hero__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.7rem;
}

.hero__chips span {
  padding: 0.45rem 0.8rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
}

.section-heading,
.support {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 1rem;
  align-items: end;
  margin-top: 2.5rem;
}

.poster-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1.4rem;
  margin-top: 1.6rem;
}

.support__list {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1.5rem;
}

.support article {
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.support p {
  color: var(--text-muted);
}

@media (max-width: 960px) {
  .hero {
    min-height: 72svh;
  }

  .hero__copy {
    padding: 1.5rem;
  }

  .poster-grid,
  .support__list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .section-heading,
  .support {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .poster-grid,
  .support__list {
    grid-template-columns: 1fr;
  }
}
</style>
