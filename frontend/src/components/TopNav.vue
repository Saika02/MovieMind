<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'
import { useProfileStore } from '../stores/profile'
import SearchBar from './SearchBar.vue'
import { normalizePoster } from '../utils/formatters'
import { copy } from '../content/copy'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const profileStore = useProfileStore()
const { session } = storeToRefs(authStore)
const { profile } = storeToRefs(profileStore)

const navItems = copy.nav.items

const avatarUrl = computed(() => normalizePoster(profile.value?.avatarUrl))

function isActiveRoute(target) {
  if (target === '/') {
    return route.path === '/'
  }

  return route.path === target || route.path.startsWith(`${target}/`)
}

onMounted(() => {
  if (!profile.value) {
    profileStore.fetchProfile().catch(() => {})
  }
})

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}

function handleSearch(keyword) {
  router.push({
    name: 'movies',
    query: keyword ? { keyword } : {},
  })
}
</script>

<template>
  <header class="nav">
    <RouterLink to="/" class="nav__brand">
      <span class="nav__mark">MM</span>
      <span>
        <strong>MovieMind</strong>
        <small>{{ copy.brand.tagline }}</small>
      </span>
    </RouterLink>

    <nav class="nav__links">
      <RouterLink
        v-for="item in navItems"
        :key="item.to"
        :to="item.to"
        class="nav__link"
        :class="{ 'is-active': isActiveRoute(item.to) }"
      >
        {{ item.label }}
      </RouterLink>
    </nav>

    <div class="nav__actions">
      <SearchBar compact @search="handleSearch" />
      <RouterLink to="/profile" class="nav__profile">
        <img :src="avatarUrl" :alt="copy.common.userAvatarAlt" />
        <span>{{ profile?.username || session.username || copy.brand.memberFallback }}</span>
      </RouterLink>
      <button type="button" class="nav__logout" @click="handleLogout">{{ copy.nav.logout }}</button>
    </div>
  </header>
</template>

<style scoped>
.nav {
  position: sticky;
  top: 0;
  z-index: 30;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 1.5rem;
  align-items: center;
  width: min(1320px, calc(100% - 2rem));
  margin: 0 auto;
  padding: 1rem 0;
  backdrop-filter: blur(18px);
}

.nav__brand {
  display: inline-flex;
  align-items: center;
  gap: 0.9rem;
  color: var(--text-primary);
}

.nav__mark {
  width: 2.7rem;
  height: 2.7rem;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-gold), #f4d79a);
  color: #111;
  font-family: var(--font-display);
  letter-spacing: 0.08em;
}

.nav__brand strong,
.nav__brand small {
  display: block;
}

.nav__brand strong {
  font-family: var(--font-display);
  font-size: 1.1rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.nav__brand small {
  color: var(--text-muted);
}

.nav__links {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.nav__link {
  padding: 0.55rem 0.8rem;
  border-radius: 999px;
  color: var(--text-muted);
  transition: color 180ms ease, background 180ms ease, transform 180ms ease;
}

.nav__link:hover,
.nav__link.is-active {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.06);
  transform: translateY(-1px);
}

.nav__actions {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.nav__profile {
  display: inline-flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.4rem 0.7rem 0.4rem 0.45rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  color: var(--text-primary);
  background: rgba(8, 13, 20, 0.72);
}

.nav__profile img {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  object-fit: cover;
}

.nav__logout {
  padding: 0.8rem 1rem;
}

@media (max-width: 1040px) {
  .nav {
    grid-template-columns: 1fr;
  }

  .nav__links {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .nav__actions {
    flex-wrap: wrap;
    justify-content: space-between;
  }
}
</style>
