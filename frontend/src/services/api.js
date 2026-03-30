import axios from 'axios'

export const apiClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.response.use(
  (response) => {
    const payload = response.data

    if (payload?.success === false) {
      return Promise.reject(new Error(payload.message || 'Request failed'))
    }

    return response
  },
  (error) => {
    const message =
      error.response?.data?.message ||
      error.message ||
      'Unable to reach MovieMind right now.'

    return Promise.reject(new Error(message))
  },
)

export async function unwrap(request) {
  const response = await request
  return response.data.data
}
