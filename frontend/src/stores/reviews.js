import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as reviewApi from '../services/reviews'

export const useReviewStore = defineStore('reviews', () => {
  const moviePage = ref({ items: [], total: 0, page: 1, size: 8 })
  const myPage = ref({ items: [], total: 0, page: 1, size: 8 })
  const busy = ref(false)

  async function fetchMovieReviews(movieId, page = 1, size = 8) {
    moviePage.value = await reviewApi.getMovieReviews(movieId, page, size)
    return moviePage.value
  }

  async function fetchMyReviews(page = 1, size = 8) {
    myPage.value = await reviewApi.getMyReviews(page, size)
    return myPage.value
  }

  async function saveReview({ id, ...payload }) {
    busy.value = true
    try {
      if (id) {
        return await reviewApi.updateReview(id, payload)
      }

      return await reviewApi.createReview(payload)
    } finally {
      busy.value = false
    }
  }

  async function removeReview(id) {
    busy.value = true
    try {
      await reviewApi.deleteReview(id)
    } finally {
      busy.value = false
    }
  }

  return {
    moviePage,
    myPage,
    busy,
    fetchMovieReviews,
    fetchMyReviews,
    saveReview,
    removeReview,
  }
})
