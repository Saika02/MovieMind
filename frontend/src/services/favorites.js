import { apiClient, unwrap } from './api'

export function getFavoriteStatus(movieId) {
  return unwrap(apiClient.get('/favorites/status', { params: { movieId } }))
}

export function createFavorite(movieId) {
  return unwrap(apiClient.post('/favorites', { movieId }))
}

export function getFavoritesPage(page, size) {
  return unwrap(apiClient.get('/favorites/page', { params: { page, size } }))
}

export function getFavorites() {
  return unwrap(apiClient.get('/favorites'))
}

export function deleteFavorite(id) {
  return unwrap(apiClient.delete('/favorites/delete', { params: { id } }))
}
