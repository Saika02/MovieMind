import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as userApi from '../services/users'

export const useProfileStore = defineStore('profile', () => {
  const profile = ref(null)
  const loading = ref(false)

  async function fetchProfile() {
    loading.value = true
    try {
      profile.value = await userApi.getCurrentUser()
      return profile.value
    } finally {
      loading.value = false
    }
  }

  async function uploadAvatar(file) {
    loading.value = true
    try {
      const avatarUrl = await userApi.uploadAvatar(file)
      profile.value = {
        ...profile.value,
        avatarUrl,
      }
      return avatarUrl
    } finally {
      loading.value = false
    }
  }

  return {
    profile,
    loading,
    fetchProfile,
    uploadAvatar,
  }
})
