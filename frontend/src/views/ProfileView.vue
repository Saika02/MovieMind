<script setup>
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import EmptyState from '../components/EmptyState.vue'
import PaginationBar from '../components/PaginationBar.vue'
import FormNotice from '../components/FormNotice.vue'
import { useProfileStore } from '../stores/profile'
import { useReviewStore } from '../stores/reviews'
import { normalizePoster, formatDate } from '../utils/formatters'
import { copy } from '../content/copy'

const profileStore = useProfileStore()
const reviewStore = useReviewStore()
const { profile } = storeToRefs(profileStore)
const { myPage } = storeToRefs(reviewStore)
const uploadMessage = ref('')
const uploadType = ref('success')

const avatar = computed(() => normalizePoster(profile.value?.avatarUrl))

onMounted(async () => {
  await Promise.all([profileStore.fetchProfile(), reviewStore.fetchMyReviews(1, 8)])
})

async function onAvatarSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    await profileStore.uploadAvatar(file)
    uploadMessage.value = copy.profile.uploadSuccess
    uploadType.value = 'success'
  } catch (error) {
    uploadMessage.value = error.message
    uploadType.value = 'error'
  }
}

async function changePage(page) {
  await reviewStore.fetchMyReviews(page, myPage.value.size)
}
</script>

<template>
  <section class="profile-hero">
    <img :src="avatar" :alt="copy.common.profileAvatarAlt" />
    <div>
      <span class="eyebrow">{{ copy.profile.eyebrow }}</span>
      <h1>{{ profile?.username || copy.profile.fallbackName }}</h1>
      <p>{{ profile?.bio || copy.profile.fallbackBio }}</p>
      <label class="avatar-upload">
        <span>{{ copy.profile.uploadAvatar }}</span>
        <input type="file" accept="image/*" @change="onAvatarSelected" />
      </label>
    </div>
  </section>

  <FormNotice v-if="uploadMessage" :message="uploadMessage" :type="uploadType" @close="uploadMessage = ''" />

  <section class="profile-reviews">
    <div class="section-title">
      <div>
        <span class="eyebrow">{{ copy.profile.reviewsEyebrow }}</span>
        <h2>{{ copy.profile.reviewsTitle }}</h2>
      </div>
    </div>

    <div v-if="myPage.items.length" class="profile-reviews__list">
      <article v-for="review in myPage.items" :key="review.id">
        <div>
          <strong>{{ copy.profile.moviePrefix }} #{{ review.movieId }}</strong>
          <small>{{ formatDate(review.updatedAt || review.createdAt) }}</small>
        </div>
        <span>{{ Number(review.score).toFixed(1) }}</span>
        <p>{{ review.content }}</p>
      </article>
    </div>
    <EmptyState
      v-else
      :title="copy.profile.emptyTitle"
      :description="copy.profile.emptyDescription"
    />

    <PaginationBar
      v-if="myPage.total > myPage.size"
      :page="myPage.page"
      :size="myPage.size"
      :total="myPage.total"
      @change="changePage"
    />
  </section>
</template>

<style scoped>
.profile-hero {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 2rem;
  align-items: center;
  margin: 1rem 0 2rem;
}

.profile-hero img {
  width: 180px;
  height: 180px;
  border-radius: 28px;
  object-fit: cover;
}

.profile-hero p {
  max-width: 34rem;
  color: var(--text-muted);
}

.avatar-upload {
  display: inline-flex;
  align-items: center;
  gap: 0.8rem;
  margin-top: 1rem;
  color: var(--text-primary);
}

.avatar-upload input {
  max-width: 16rem;
}

.profile-reviews {
  display: grid;
  gap: 1.2rem;
  padding: 1.6rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.03);
}

.profile-reviews__list article {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.8rem;
  padding: 1rem 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.profile-reviews__list article:first-child {
  border-top: none;
  padding-top: 0;
}

.profile-reviews__list small,
.profile-reviews__list p {
  color: var(--text-muted);
}

.profile-reviews__list p {
  grid-column: 1 / -1;
  margin: 0;
}

@media (max-width: 720px) {
  .profile-hero {
    grid-template-columns: 1fr;
  }
}
</style>
