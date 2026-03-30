import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as favoriteApi from '../services/favorites'

export const useFavoriteStore = defineStore('favorites', () => {
  const status = ref(null)
  const pageData = ref({ items: [], total: 0, page: 1, size: 12 })
  const busy = ref(false)

  async function fetchStatus(movieId) {
    status.value = await favoriteApi.getFavoriteStatus(movieId)
    return status.value
  }

  async function toggle(movieId) {
    busy.value = true
    try {
      if (status.value?.favorited && status.value.favoriteId) {
        await favoriteApi.deleteFavorite(status.value.favoriteId)
        status.value = { movieId, favorited: false, favoriteId: null }
      } else {
        const created = await favoriteApi.createFavorite(movieId)
        status.value = { movieId, favorited: true, favoriteId: created.id }
      }
      return status.value
    } finally {
      busy.value = false
    }
  }

  async function fetchPage(page = 1, size = 12) {
    pageData.value = await favoriteApi.getFavoritesPage(page, size)
    return pageData.value
  }

  return {
    status,
    pageData,
    busy,
    fetchStatus,
    fetchPage,
    toggle,
  }
})
