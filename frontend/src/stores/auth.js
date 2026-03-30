import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '../services/auth'
import { copy } from '../content/copy'

export const useAuthStore = defineStore('auth', () => {
  const session = ref({
    loggedIn: false,
    userId: null,
    username: null,
    role: null,
  })
  const bootstrapped = ref(false)
  const pending = ref(false)
  const flashMessage = ref('')
  const flashType = ref('info')

  const loggedIn = computed(() => Boolean(session.value.loggedIn))

  function setFlash(message, type = 'info') {
    flashMessage.value = message
    flashType.value = type
  }

  function clearFlash() {
    flashMessage.value = ''
    flashType.value = 'info'
  }

  async function initialize() {
    if (bootstrapped.value) return

    try {
      const response = await authApi.getSession()
      session.value = {
        loggedIn: Boolean(response?.loggedIn),
        userId: response?.userId ?? null,
        username: response?.username ?? null,
        role: response?.role ?? null,
      }
    } catch {
      session.value = {
        loggedIn: false,
        userId: null,
        username: null,
        role: null,
      }
    } finally {
      bootstrapped.value = true
    }
  }

  async function login(payload) {
    pending.value = true

    try {
      const response = await authApi.login(payload)
      session.value = {
        loggedIn: true,
        userId: response.userId,
        username: response.username,
        role: response.role,
      }
      setFlash(copy.flash.loginSuccess, 'success')
    } finally {
      pending.value = false
    }
  }

  async function register(payload) {
    pending.value = true

    try {
      const response = await authApi.register(payload)
      session.value = {
        loggedIn: true,
        userId: response.userId,
        username: response.username,
        role: response.role,
      }
      setFlash(copy.flash.registerSuccess, 'success')
    } finally {
      pending.value = false
    }
  }

  async function logout() {
    pending.value = true

    try {
      await authApi.logout()
      session.value = {
        loggedIn: false,
        userId: null,
        username: null,
        role: null,
      }
      setFlash(copy.flash.logoutSuccess, 'info')
    } finally {
      pending.value = false
    }
  }

  return {
    bootstrapped,
    pending,
    session,
    loggedIn,
    flashMessage,
    flashType,
    initialize,
    login,
    register,
    logout,
    setFlash,
    clearFlash,
  }
})
