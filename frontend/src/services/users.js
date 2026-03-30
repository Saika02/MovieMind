import { apiClient, unwrap } from './api'

export function getCurrentUser() {
  return unwrap(apiClient.get('/users/me'))
}

export async function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)

  return unwrap(
    apiClient.post('/users/me/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
  )
}
