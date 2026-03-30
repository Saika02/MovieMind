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

const pageCopy = computed(() => (mode.value === 'login' ? copy.auth.login : copy.auth.register))

async function submit() {
  errorMessage.value = ''

  try {
    if (mode.value === 'login') {
      await authStore.login(form)
    } else {
      await authStore.register(form)
    }

    router.push(route.query.redirect || '/')
  } catch (error) {
    errorMessage.value = error.message
  }
}
</script>

<template>
  <section class="auth">
    <div class="auth__poster">
      <div class="auth__overlay">
        <span>{{ pageCopy.eyebrow }}</span>
        <h1>{{ pageCopy.title }}</h1>
        <p>{{ pageCopy.subtitle }}</p>
      </div>
    </div>

    <div class="auth__panel">
      <div class="auth__brand">MovieMind</div>
      <h2>{{ pageCopy.welcome }}</h2>
      <p class="auth__lead">{{ copy.auth.lead }}</p>

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
  grid-template-columns: 1.35fr minmax(340px, 420px);
  background:
    radial-gradient(circle at 20% 20%, rgba(211, 164, 74, 0.2), transparent 24%),
    linear-gradient(145deg, #06090d 0%, #0e1620 44%, #090c12 100%);
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
  max-width: 32rem;
}

.auth__overlay span {
  display: inline-block;
  margin-bottom: 1rem;
  color: var(--accent-gold);
  text-transform: uppercase;
  letter-spacing: 0.14em;
}

.auth__overlay h1 {
  margin-bottom: 1rem;
  font-size: clamp(2.6rem, 5vw, 4.7rem);
}

.auth__overlay p {
  max-width: 26rem;
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

.auth__brand {
  font-family: var(--font-display);
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--accent-gold);
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

@media (max-width: 960px) {
  .auth {
    grid-template-columns: 1fr;
  }

  .auth__poster {
    min-height: 45vh;
  }

  .auth__overlay {
    inset: auto 1.5rem 1.5rem 1.5rem;
  }
}
</style>
