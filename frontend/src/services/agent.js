import { apiClient, unwrap } from './api'

export function recommendWithAgent(payload) {
  return unwrap(apiClient.post('/agent/recommend', payload))
}
