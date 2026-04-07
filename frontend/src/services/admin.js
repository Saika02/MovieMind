import { apiClient, unwrap } from './api'

export function getAdminMoviePage(params) {
  return unwrap(apiClient.get('/admin/movies/page', { params }))
}

export function getAdminMovieDetail(id) {
  return unwrap(apiClient.get(`/admin/movies/${id}`))
}

export function createAdminMovie(payload) {
  return unwrap(apiClient.post('/admin/movies', payload))
}

export function updateAdminMovie(id, payload) {
  return unwrap(apiClient.put(`/admin/movies/${id}`, payload))
}

export function deleteAdminMovie(id) {
  return unwrap(apiClient.delete(`/admin/movies/${id}`))
}

export function uploadAdminMoviePoster(file) {
  const formData = new FormData()
  formData.append('file', file)
  return unwrap(
    apiClient.post('/admin/movies/upload-poster', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
  )
}

export function getAdminReviewPage(params) {
  return unwrap(apiClient.get('/admin/reviews/page', { params }))
}

export function deleteAdminReview(id) {
  return unwrap(apiClient.delete(`/admin/reviews/${id}`))
}
