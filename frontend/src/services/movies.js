import { apiClient, unwrap } from './api'

export function getMovies(params = {}) {
  return unwrap(apiClient.get('/movies', { params }))
}

export function getMoviePage(params) {
  return unwrap(apiClient.get('/movies/page', { params }))
}

export function getMovieDetail(id) {
  return unwrap(apiClient.get('/movies/detail', { params: { id } }))
}
