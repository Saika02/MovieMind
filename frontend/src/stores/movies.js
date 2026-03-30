import { reactive, ref } from 'vue'
import { defineStore } from 'pinia'
import * as movieApi from '../services/movies'

export const useMovieStore = defineStore('movies', () => {
  const featured = ref({ items: [] })
  const list = ref([])
  const detail = ref(null)
  const loading = ref(false)
  const listing = ref(false)
  const detailLoading = ref(false)
  const pagination = reactive({
    page: 1,
    size: 12,
    total: 0,
  })

  async function fetchFeatured() {
    loading.value = true
    try {
      featured.value = await movieApi.getMoviePage({ page: 1, size: 8 })
      return featured.value
    } finally {
      loading.value = false
    }
  }

  async function fetchList({ page = 1, size = 12, keyword = '' } = {}) {
    listing.value = true
    try {
      const response = await movieApi.getMoviePage({ page, size, keyword: keyword || undefined })
      list.value = response.items ?? []
      pagination.page = response.page
      pagination.size = response.size
      pagination.total = response.total
      return response
    } finally {
      listing.value = false
    }
  }

  async function fetchDetail(id) {
    detailLoading.value = true
    try {
      detail.value = await movieApi.getMovieDetail(id)
      return detail.value
    } finally {
      detailLoading.value = false
    }
  }

  return {
    featured,
    list,
    detail,
    loading,
    listing,
    detailLoading,
    pagination,
    fetchFeatured,
    fetchList,
    fetchDetail,
  }
})
