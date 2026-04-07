import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AuthView from '../views/AuthView.vue'
import HomeView from '../views/HomeView.vue'
import MovieListView from '../views/MovieListView.vue'
import MovieDetailView from '../views/MovieDetailView.vue'
import FavoritesView from '../views/FavoritesView.vue'
import ProfileView from '../views/ProfileView.vue'
import RecommendView from '../views/RecommendView.vue'
import AdminMovieManageView from '../views/AdminMovieManageView.vue'
import AdminReviewManageView from '../views/AdminReviewManageView.vue'

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
    path: '/admin/login',
    name: 'admin-login',
    component: AuthView,
    meta: { guestOnly: true, layout: 'auth', mode: 'admin-login' },
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
    path: '/admin',
    redirect: '/admin/movies',
  },
  {
    path: '/admin/movies',
    name: 'admin-movies',
    component: AdminMovieManageView,
    meta: { requiresAuth: true, requiresAdmin: true, layout: 'admin', adminTitle: '电影管理' },
  },
  {
    path: '/admin/reviews',
    name: 'admin-reviews',
    component: AdminReviewManageView,
    meta: { requiresAuth: true, requiresAdmin: true, layout: 'admin', adminTitle: '评论管理' },
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
    const loginRoute = to.meta.requiresAdmin ? 'admin-login' : 'login'
    return {
      name: loginRoute,
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.guestOnly && authStore.loggedIn) {
    if (to.name === 'admin-login' && authStore.isAdmin) {
      return { name: 'admin-movies' }
    }
    return { name: authStore.isAdmin ? 'admin-movies' : 'home' }
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: 'home' }
  }

  return true
})

export default router
