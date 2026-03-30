<script setup>
import { ref, watch } from 'vue'
import { copy } from '../content/copy'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  compact: {
    type: Boolean,
    default: false,
  },
  placeholder: {
    type: String,
    default: copy.search.placeholder,
  },
})

const emit = defineEmits(['update:modelValue', 'search'])
const localValue = ref(props.modelValue)

watch(
  () => props.modelValue,
  (value) => {
    localValue.value = value
  },
)

function submit() {
  emit('update:modelValue', localValue.value)
  emit('search', localValue.value.trim())
}
</script>

<template>
  <form class="search" :class="{ 'search--compact': compact }" @submit.prevent="submit">
    <input
      v-model="localValue"
      :placeholder="placeholder"
      type="search"
      autocomplete="off"
      @keyup.enter="submit"
    />
    <button type="submit">{{ copy.search.button }}</button>
  </form>
</template>

<style scoped>
.search {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.35rem;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  background: rgba(8, 13, 20, 0.82);
}

.search input {
  min-width: 16rem;
  border: none;
  background: transparent;
  color: var(--text-primary);
}

.search input:focus {
  outline: none;
}

.search button {
  padding-inline: 1rem;
}

.search--compact {
  max-width: 19rem;
}

.search--compact input {
  min-width: 8rem;
}
</style>
