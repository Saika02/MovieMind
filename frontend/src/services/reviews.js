import { apiClient, unwrap } from './api'

export function getMovieReviews(movieId, page = 1, size = 8) {
  return unwrap(
    apiClient.get('/reviews/movie/page', {
      params: { movieId, page, size },
    }),
  )
}

export function getMyReviews(page = 1, size = 8) {
  return unwrap(apiClient.get('/reviews/page', { params: { page, size } }))
}

export function createReview(payload) {
  return unwrap(apiClient.post('/reviews', payload))
}

export function updateReview(id, payload) {
  return unwrap(apiClient.put('/reviews/update', payload, { params: { id } }))
}

export function deleteReview(id) {
  return unwrap(apiClient.delete('/reviews/delete', { params: { id } }))
}
