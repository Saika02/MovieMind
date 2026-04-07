<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { session } = storeToRefs(authStore)

const navItems = [
  { label: '电影管理', to: '/admin/movies' },
  { label: '评论管理', to: '/admin/reviews' },
]

const pageTitle = computed(() => route.meta.adminTitle || '管理中心')

function isActive(target) {
  return route.path === target || route.path.startsWith(`${target}/`)
}

async function handleLogout() {
  await authStore.logout()
  router.push('/admin/login')
}
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-shell__sidebar">
      <div class="admin-shell__brand">
        <strong>MovieMind</strong>
        <span>管理中心</span>
      </div>

      <nav class="admin-shell__nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="admin-shell__nav-item"
          :class="{ 'is-active': isActive(item.to) }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <div class="admin-shell__main">
      <header class="admin-shell__header">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>在这里整理电影内容，处理评论记录。</p>
        </div>

        <div class="admin-shell__account">
          <span>{{ session.username || '管理员' }}</span>
          <button type="button" class="admin-shell__logout" @click="handleLogout">退出登录</button>
        </div>
      </header>

      <main class="admin-shell__content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px 1fr;
  background: #f5f7fa;
  color: #1f2937;
}

.admin-shell__sidebar {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 1.5rem 1rem;
  background: #1f2937;
  color: #f9fafb;
}

.admin-shell__brand strong,
.admin-shell__brand span {
  display: block;
}

.admin-shell__brand strong {
  font-size: 1.1rem;
}

.admin-shell__brand span {
  margin-top: 0.35rem;
  color: rgba(249, 250, 251, 0.68);
  font-size: 0.9rem;
}

.admin-shell__nav {
  display: grid;
  gap: 0.35rem;
}

.admin-shell__nav-item {
  padding: 0.85rem 1rem;
  border-radius: 10px;
  color: rgba(249, 250, 251, 0.82);
  transition: background 160ms ease, color 160ms ease;
}

.admin-shell__nav-item:hover,
.admin-shell__nav-item.is-active {
  background: #374151;
  color: #ffffff;
}

.admin-shell__main {
  display: grid;
  grid-template-rows: auto 1fr;
  min-width: 0;
}

.admin-shell__header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
}

.admin-shell__header h1 {
  margin: 0;
  font-size: 1.5rem;
  font-family: var(--font-sans);
  letter-spacing: 0;
  line-height: 1.3;
}

.admin-shell__header p {
  margin: 0.25rem 0 0;
  color: #6b7280;
}

.admin-shell__account {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #4b5563;
}

.admin-shell__logout {
  padding: 0.7rem 1rem;
  border-radius: 10px;
  background: #2563eb;
  color: #ffffff;
  box-shadow: none;
}

.admin-shell__content {
  padding: 1.5rem;
}

@media (max-width: 900px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .admin-shell__sidebar {
    padding-bottom: 0.5rem;
  }

  .admin-shell__nav {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-shell__header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
