<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import EmptyState from '../components/EmptyState.vue'
import PaginationBar from '../components/PaginationBar.vue'
import FormNotice from '../components/FormNotice.vue'
import ReviewComposer from '../components/ReviewComposer.vue'
import { useProfileStore } from '../stores/profile'
import { useReviewStore } from '../stores/reviews'
import { normalizePoster, formatDate } from '../utils/formatters'
import { copy } from '../content/copy'

const profileStore = useProfileStore()
const reviewStore = useReviewStore()
const { profile } = storeToRefs(profileStore)
const { myPage, busy } = storeToRefs(reviewStore)
const noticeMessage = ref('')
const noticeType = ref('success')
const editingReviewId = ref(null)
const avatarInput = ref(null)

const avatar = computed(() => normalizePoster(profile.value?.avatarUrl))
const reviewItems = computed(() => myPage.value.items ?? [])
const editingReview = computed(
  () => reviewItems.value.find((item) => item.id === editingReviewId.value) ?? null,
)

onMounted(async () => {
  await Promise.all([profileStore.fetchProfile(), reviewStore.fetchMyReviews(1, 8)])
})

async function onAvatarSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    await profileStore.uploadAvatar(file)
    noticeMessage.value = copy.profile.uploadSuccess
    noticeType.value = 'success'
  } catch (error) {
    noticeMessage.value = error.message
    noticeType.value = 'error'
  }
}

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

async function changePage(page) {
  editingReviewId.value = null
  await reviewStore.fetchMyReviews(page, myPage.value.size)
}

function startEdit(review) {
  editingReviewId.value = review.id
}

function cancelEdit() {
  editingReviewId.value = null
}

async function submitReview(payload) {
  try {
    await reviewStore.saveReview(payload)
    noticeMessage.value = copy.profile.reviewEditSuccess
    noticeType.value = 'success'
    editingReviewId.value = null
    await reviewStore.fetchMyReviews(myPage.value.page, myPage.value.size)
  } catch (error) {
    noticeMessage.value = error.message
    noticeType.value = 'error'
  }
}

async function removeReview(id) {
  if (!globalThis.confirm(copy.profile.reviewDeleteConfirm)) return

  try {
    await reviewStore.removeReview(id)
    noticeMessage.value = copy.profile.reviewDeleteSuccess
    noticeType.value = 'success'
    editingReviewId.value = null
    const isLastItemOnPage = reviewItems.value.length === 1 && myPage.value.page > 1
    const nextPage = isLastItemOnPage ? myPage.value.page - 1 : myPage.value.page
    await reviewStore.fetchMyReviews(nextPage, myPage.value.size)
  } catch (error) {
    noticeMessage.value = error.message
    noticeType.value = 'error'
  }
}
</script>

<template>
  <section class="profile-hero">
    <button type="button" class="avatar-trigger" @click="triggerAvatarUpload">
      <img :src="avatar" :alt="copy.common.profileAvatarAlt" />
      <span class="avatar-trigger__veil">
        <strong>{{ copy.profile.uploadAvatar }}</strong>
        <small>{{ copy.profile.uploadAvatarHint }}</small>
      </span>
    </button>
    <div>
      <span class="eyebrow">{{ copy.profile.eyebrow }}</span>
      <h1>{{ profile?.username || copy.profile.fallbackName }}</h1>
      <p>{{ profile?.bio || copy.profile.fallbackBio }}</p>
      <button type="button" class="ghost avatar-upload" @click="triggerAvatarUpload">
        {{ copy.profile.uploadAvatar }}
      </button>
      <input
        ref="avatarInput"
        class="avatar-input"
        type="file"
        accept="image/*"
        @change="onAvatarSelected"
      />
    </div>
  </section>

  <FormNotice v-if="noticeMessage" :message="noticeMessage" :type="noticeType" @close="noticeMessage = ''" />

  <section class="profile-reviews">
    <div class="section-title">
      <div>
        <span class="eyebrow">{{ copy.profile.reviewsEyebrow }}</span>
        <h2>{{ copy.profile.reviewsTitle }}</h2>
      </div>
    </div>

    <div v-if="reviewItems.length" class="profile-reviews__list">
      <article v-for="review in reviewItems" :key="review.id" class="review-record">
        <RouterLink :to="`/movies/${review.movieId}`" class="review-record__link">
          <img
            :src="normalizePoster(review.moviePosterFile)"
            :alt="review.movieTitle || `${copy.profile.moviePrefix} ${review.movieId}`"
            class="review-record__poster"
          />
          <div class="review-record__body">
            <div class="review-record__header">
              <div>
                <strong>{{ review.movieTitle || `${copy.profile.moviePrefix} #${review.movieId}` }}</strong>
                <small>{{ formatDate(review.updatedAt || review.createdAt) }}</small>
              </div>
              <span>{{ Number(review.score).toFixed(1) }}</span>
            </div>
            <p>{{ review.content || copy.profile.reviewCardFallback }}</p>
            <span class="review-record__action">{{ copy.profile.reviewCardAction }}</span>
          </div>
        </RouterLink>

        <div class="review-record__toolbar">
          <button type="button" class="ghost" :disabled="busy" @click="startEdit(review)">
            {{ copy.reviewActions.edit }}
          </button>
          <button type="button" class="ghost" :disabled="busy" @click="removeReview(review.id)">
            {{ copy.reviewActions.delete }}
          </button>
        </div>

        <ReviewComposer
          v-if="editingReviewId === review.id"
          :movie-id="review.movieId"
          :review="editingReview"
          :pending="busy"
          @submit="submitReview"
          @cancel="cancelEdit"
        />
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

.avatar-trigger {
  position: relative;
  width: 180px;
  height: 180px;
  padding: 0;
  border: none;
  border-radius: 28px;
  overflow: hidden;
  background: transparent;
  box-shadow: none;
}

.avatar-trigger:hover {
  transform: none;
  box-shadow: none;
}

.avatar-trigger__veil {
  position: absolute;
  inset: 0;
  display: grid;
  place-content: end start;
  gap: 0.25rem;
  padding: 1rem;
  background: linear-gradient(180deg, rgba(8, 12, 18, 0.06), rgba(8, 12, 18, 0.78));
  opacity: 0;
  transition: opacity 180ms ease;
  text-align: left;
}

.avatar-trigger__veil strong {
  color: var(--text-primary);
}

.avatar-trigger__veil small {
  color: var(--text-soft);
}

.avatar-trigger:hover .avatar-trigger__veil,
.avatar-trigger:focus-visible .avatar-trigger__veil {
  opacity: 1;
}

.profile-hero p {
  max-width: 34rem;
  color: var(--text-muted);
}

.avatar-upload {
  margin-top: 1rem;
}

.avatar-input {
  display: none;
}

.ghost {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.16);
  color: var(--text-primary);
}

.profile-reviews {
  display: grid;
  gap: 1.2rem;
  padding: 1.6rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.03);
}

.profile-reviews__list {
  display: grid;
  gap: 1rem;
}

.review-record {
  display: grid;
  gap: 1rem;
  padding: 1rem 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.review-record:first-child {
  border-top: none;
  padding-top: 0;
}

.review-record__link {
  display: grid;
  grid-template-columns: 84px 1fr;
  gap: 1rem;
  color: var(--text-primary);
}

.review-record__poster {
  width: 84px;
  height: 120px;
  border-radius: 18px;
  object-fit: cover;
}

.review-record__body {
  display: grid;
  gap: 0.55rem;
}

.review-record__header {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.8rem;
  align-items: start;
}

.review-record__header small,
.review-record__body p {
  color: var(--text-muted);
}

.review-record__body p {
  margin: 0;
}

.review-record__action {
  color: var(--accent-gold);
  font-size: 0.9rem;
}

.review-record__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.7rem;
}

@media (max-width: 720px) {
  .profile-hero {
    grid-template-columns: 1fr;
  }

  .review-record__link {
    grid-template-columns: 1fr;
  }

  .review-record__poster {
    width: 100%;
    height: 220px;
  }
}
</style>
