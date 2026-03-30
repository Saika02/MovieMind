<script setup>
import { computed, onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import AppShell from './components/AppShell.vue'
import FormNotice from './components/FormNotice.vue'
import { useAuthStore } from './stores/auth'
import { copy } from './content/copy'

const route = useRoute()
const authStore = useAuthStore()
const { bootstrapped, flashMessage, flashType } = storeToRefs(authStore)

const isAuthRoute = computed(() => route.meta.layout === 'auth')

onMounted(() => {
  authStore.initialize()
})
</script>

<template>
  <div v-if="!bootstrapped" class="boot-screen">
    <div class="boot-screen__brand">MovieMind</div>
    <p>{{ copy.common.loadingApp }}</p>
  </div>
  <template v-else>
    <FormNotice
      v-if="flashMessage"
      :message="flashMessage"
      :type="flashType"
      class="global-notice"
      @close="authStore.clearFlash()"
    />
    <RouterView v-if="isAuthRoute" />
    <AppShell v-else>
      <RouterView />
    </AppShell>
  </template>
</template>

<style scoped>
.boot-screen {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background:
    radial-gradient(circle at top, rgba(204, 169, 88, 0.24), transparent 32%),
    linear-gradient(160deg, #0a1018 0%, #111a25 45%, #06080c 100%);
  color: var(--text-primary);
  text-align: center;
}

.boot-screen__brand {
  margin-bottom: 0.75rem;
  font-family: var(--font-display);
  font-size: clamp(2.8rem, 9vw, 5rem);
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.boot-screen p {
  margin: 0;
  color: var(--text-muted);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.global-notice {
  position: fixed;
  inset: 1rem 1rem auto;
  z-index: 40;
}
</style>
