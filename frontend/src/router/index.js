import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AuthView from '../views/AuthView.vue'
import HomeView from '../views/HomeView.vue'
import MovieListView from '../views/MovieListView.vue'
import MovieDetailView from '../views/MovieDetailView.vue'
import FavoritesView from '../views/FavoritesView.vue'
import ProfileView from '../views/ProfileView.vue'
import RecommendView from '../views/RecommendView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: AuthView,
    meta: { guestOnly: true, layout: 'auth', mode: 'login' },
  },
  {
    path: '/register',
    name: 'register',
    component: AuthView,
    meta: { guestOnly: true, layout: 'auth', mode: 'register' },
  },
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: true },
  },
  {
    path: '/movies',
    name: 'movies',
    component: MovieListView,
    meta: { requiresAuth: true },
  },
  {
    path: '/movies/:id',
    name: 'movie-detail',
    component: MovieDetailView,
    meta: { requiresAuth: true },
  },
  {
    path: '/recommend',
    name: 'recommend',
    component: RecommendView,
    meta: { requiresAuth: true },
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: FavoritesView,
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfileView,
    meta: { requiresAuth: true },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (!authStore.bootstrapped) {
    await authStore.initialize()
  }

  if (to.meta.requiresAuth && !authStore.loggedIn) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.guestOnly && authStore.loggedIn) {
    return { name: 'home' }
  }

  return true
})

export default router
