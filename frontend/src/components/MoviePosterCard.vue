<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { formatDate, normalizePoster, splitTags } from '../utils/formatters'
import { copy } from '../content/copy'

const props = defineProps({
  movie: {
    type: Object,
    required: true,
  },
})

const posterUrl = computed(() => normalizePoster(props.movie.posterFile))
const genres = computed(() => splitTags(props.movie.genres).slice(0, 2))
</script>

<template>
  <RouterLink :to="`/movies/${movie.id}`" class="movie-card">
    <div class="movie-card__poster">
      <img :src="posterUrl" :alt="movie.title" />
    </div>
    <div class="movie-card__meta">
      <div class="movie-card__topline">
        <span>{{ formatDate(movie.releaseDate) }}</span>
        <strong>{{ movie.tmdbVoteAverage ? Number(movie.tmdbVoteAverage).toFixed(1) : '--' }}</strong>
      </div>
      <h3>{{ movie.title }}</h3>
      <p>{{ movie.tagline || copy.common.fallbackTagline }}</p>
      <div class="movie-card__genres">
        <span v-for="genre in genres" :key="genre">{{ genre }}</span>
      </div>
    </div>
  </RouterLink>
</template>

<style scoped>
.movie-card {
  display: grid;
  gap: 1rem;
  color: var(--text-primary);
  transition: transform 220ms ease, opacity 220ms ease;
}

.movie-card:hover {
  transform: translateY(-5px);
}

.movie-card__poster {
  aspect-ratio: 0.72;
  overflow: hidden;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.05);
}

.movie-card__poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 360ms ease;
}

.movie-card:hover .movie-card__poster img {
  transform: scale(1.04);
}

.movie-card__meta {
  display: grid;
  gap: 0.6rem;
}

.movie-card__topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--text-muted);
  font-size: 0.88rem;
}

.movie-card__meta h3 {
  margin: 0;
  font-size: 1.18rem;
}

.movie-card__meta p {
  margin: 0;
  color: var(--text-soft);
}

.movie-card__genres {
  display: flex;
  flex-wrap: wrap;
  gap: 0.55rem;
}

.movie-card__genres span {
  padding: 0.35rem 0.7rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  color: var(--text-muted);
  font-size: 0.8rem;
}
</style>
