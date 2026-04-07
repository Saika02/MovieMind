<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import FormNotice from '../components/FormNotice.vue'
import { useAuthStore } from '../stores/auth'
import { copy } from '../content/copy'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const mode = computed(() => route.meta.mode || 'login')
const busy = computed(() => authStore.pending)
const errorMessage = ref('')

const form = reactive({
  username: '',
  password: '',
})

const pageMap = {
  login: {
    title: copy.auth.login.title,
    subtitle: copy.auth.login.subtitle,
    submit: copy.auth.login.submit,
    switchLabel: copy.auth.login.switchLabel,
    switchAction: copy.auth.login.switchAction,
    switchTo: copy.auth.login.switchTo,
    welcome: copy.auth.login.welcome,
    brand: 'MovieMind',
    lead: copy.auth.lead,
    simple: false,
    redirectAfterSuccess: '/',
  },
  register: {
    title: copy.auth.register.title,
    subtitle: copy.auth.register.subtitle,
    submit: copy.auth.register.submit,
    switchLabel: copy.auth.register.switchLabel,
    switchAction: copy.auth.register.switchAction,
    switchTo: copy.auth.register.switchTo,
    welcome: copy.auth.register.welcome,
    brand: 'MovieMind',
    lead: copy.auth.lead,
    simple: false,
    redirectAfterSuccess: '/',
  },
  'admin-login': {
    title: '管理员登录',
    subtitle: '登录后台管理平台，维护电影资料与评论内容。',
    submit: '登录后台',
    switchLabel: '需要前台用户入口？',
    switchAction: '前往普通登录',
    switchTo: '/login',
    welcome: '后台管理平台',
    brand: 'MovieMind Admin',
    lead: '仅管理员账号可登录。登录成功后将进入独立后台，不与用户前台界面混合。',
    simple: true,
    redirectAfterSuccess: '/admin/movies',
  },
}

const pageCopy = computed(() => pageMap[mode.value] || pageMap.login)
const isAdminMode = computed(() => mode.value === 'admin-login')

async function submit() {
  errorMessage.value = ''

  try {
    if (mode.value === 'register') {
      await authStore.register(form)
      router.push(route.query.redirect || pageCopy.value.redirectAfterSuccess)
      return
    }

    await authStore.login(form)

    if (isAdminMode.value && !authStore.isAdmin) {
      await authStore.logout()
      errorMessage.value = '该账号无管理员权限'
      return
    }

    router.push(route.query.redirect || pageCopy.value.redirectAfterSuccess)
  } catch (error) {
    errorMessage.value = error.message
  }
}
</script>

<template>
  <section class="auth" :class="{ 'auth--simple': pageCopy.simple }">
    <div v-if="!pageCopy.simple" class="auth__poster">
      <div class="auth__overlay">
        <h1>{{ pageCopy.title }}</h1>
        <p>{{ pageCopy.subtitle }}</p>
      </div>
    </div>

    <div class="auth__panel">
      <div class="auth__brand">{{ pageCopy.brand }}</div>
      <h2>{{ pageCopy.welcome }}</h2>
      <p class="auth__lead">{{ pageCopy.lead }}</p>

      <FormNotice v-if="errorMessage" :message="errorMessage" type="error" @close="errorMessage = ''" />

      <form class="auth__form" @submit.prevent="submit">
        <label>
          {{ copy.auth.username }}
          <input v-model="form.username" type="text" minlength="3" maxlength="24" required />
        </label>

        <label>
          {{ copy.auth.password }}
          <input v-model="form.password" type="password" minlength="6" maxlength="32" required />
        </label>

        <button type="submit" :disabled="busy">{{ pageCopy.submit }}</button>
      </form>

      <p class="auth__switch">
        {{ pageCopy.switchLabel }}
        <RouterLink :to="pageCopy.switchTo">{{ pageCopy.switchAction }}</RouterLink>
      </p>
    </div>
  </section>
</template>

<style scoped>
.auth {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.25fr minmax(340px, 420px);
  background:
    radial-gradient(circle at 20% 20%, rgba(211, 164, 74, 0.2), transparent 24%),
    linear-gradient(145deg, #06090d 0%, #0e1620 44%, #090c12 100%);
}

.auth--simple {
  grid-template-columns: minmax(360px, 460px);
  justify-content: center;
  align-content: center;
  background: #f3f4f6;
}

.auth__poster {
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(160deg, rgba(7, 12, 20, 0.25), rgba(7, 12, 20, 0.82)),
    url('https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=1600&q=80')
      center/cover;
}

.auth__overlay {
  position: absolute;
  inset: auto auto 3rem 3rem;
  max-width: 30rem;
}

.auth__overlay h1 {
  margin: 0 0 1rem;
  font-size: clamp(2.6rem, 5vw, 4.7rem);
}

.auth__overlay p {
  margin: 0;
  color: rgba(244, 239, 232, 0.78);
}

.auth__panel {
  display: grid;
  align-content: center;
  gap: 1rem;
  padding: 2.2rem;
  background: rgba(7, 10, 16, 0.78);
  backdrop-filter: blur(22px);
}

.auth--simple .auth__panel {
  border: 1px solid #d1d5db;
  border-radius: 12px;
  background: #ffffff;
  backdrop-filter: none;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.auth__brand {
  font-family: var(--font-display);
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--accent-gold);
}

.auth--simple .auth__brand {
  font-family: var(--font-sans);
  letter-spacing: 0;
  text-transform: none;
  color: #2563eb;
  font-weight: 700;
}

.auth--simple h2,
.auth--simple label,
.auth--simple .auth__lead,
.auth--simple .auth__switch {
  color: #111827;
}

.auth__lead,
.auth__switch {
  color: var(--text-muted);
}

.auth__form {
  display: grid;
  gap: 1rem;
}

.auth__form label {
  display: grid;
  gap: 0.5rem;
  color: var(--text-muted);
}

.auth--simple input {
  background: #ffffff;
  border-color: #d1d5db;
  color: #111827;
}

.auth--simple button {
  background: #2563eb;
  color: #ffffff;
  box-shadow: none;
}

@media (max-width: 960px) {
  .auth {
    grid-template-columns: 1fr;
  }

  .auth__poster {
    min-height: 42vh;
  }

  .auth__overlay {
    inset: auto 1.5rem 1.5rem 1.5rem;
  }
}
</style>
