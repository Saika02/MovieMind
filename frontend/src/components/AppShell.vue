<script setup>
import TopNav from './TopNav.vue'
</script>

<template>
  <div class="shell">
    <TopNav />
    <main class="shell__content">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(203, 161, 75, 0.16), transparent 30%),
    radial-gradient(circle at 80% 0%, rgba(49, 91, 154, 0.18), transparent 24%),
    linear-gradient(180deg, #091018 0%, #101722 42%, #080b10 100%);
}

.shell__content {
  width: min(1240px, calc(100% - 2rem));
  margin: 0 auto;
  padding: 1.25rem 0 4rem;
}
</style>
