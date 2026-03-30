import { apiClient, unwrap } from './api'

export function login(payload) {
  return unwrap(apiClient.post('/auth/login', payload))
}

export function register(payload) {
  return unwrap(apiClient.post('/auth/register', payload))
}

export function getSession() {
  return unwrap(apiClient.get('/auth/me'))
}

export function logout() {
  return unwrap(apiClient.post('/auth/logout'))
}
