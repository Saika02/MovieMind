<script setup>
import { reactive, ref, onMounted } from 'vue'
import PaginationBar from '../components/PaginationBar.vue'
import EmptyState from '../components/EmptyState.vue'
import { formatDate, formatScore } from '../utils/formatters'
import * as adminApi from '../services/admin'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const filters = reactive({
  keyword: '',
  movieId: '',
  userId: '',
})

const pagination = reactive({
  page: 1,
  size: 8,
  total: 0,
})

const list = ref([])
const loading = ref(false)
const deletingId = ref(null)

onMounted(() => {
  loadReviews()
})

async function loadReviews(page = pagination.page) {
  loading.value = true
  try {
    const response = await adminApi.getAdminReviewPage({
      page,
      size: pagination.size,
      keyword: filters.keyword || undefined,
      movieId: normalizeNumber(filters.movieId),
      userId: normalizeNumber(filters.userId),
    })
    list.value = response.items || []
    pagination.page = response.page || page
    pagination.size = response.size || pagination.size
    pagination.total = response.total || 0
  } finally {
    loading.value = false
  }
}

async function deleteReview(review) {
  if (!window.confirm(`确定删除用户 ${review.username} 的这条评论吗？`)) {
    return
  }

  deletingId.value = review.id
  try {
    await adminApi.deleteAdminReview(review.id)
    authStore.setFlash('评论已删除', 'info')
    await loadReviews(pagination.page)
  } finally {
    deletingId.value = null
  }
}

function normalizeNumber(value) {
  if (value === '' || value == null) return null
  const number = Number(value)
  return Number.isNaN(number) ? null : number
}
</script>

<template>
  <section class="admin-page">
    <section class="admin-card">
      <div class="admin-card__title">
        <h2>筛选条件</h2>
        <span>按关键词、电影或用户快速找到对应评论。</span>
      </div>

      <div class="admin-filters">
        <label class="admin-page__field">
          <span>关键词</span>
          <input v-model="filters.keyword" type="text" placeholder="评论内容 / 电影名 / 用户名" />
        </label>
        <label class="admin-page__field">
          <span>电影 ID</span>
          <input v-model="filters.movieId" type="number" min="0" />
        </label>
        <label class="admin-page__field">
          <span>用户 ID</span>
          <input v-model="filters.userId" type="number" min="0" />
        </label>
      </div>

      <div class="admin-actions">
        <button type="button" @click="loadReviews(1)">查询评论</button>
      </div>
    </section>

    <section class="admin-card">
      <div class="admin-card__title">
        <h2>评论列表</h2>
        <span>共 {{ pagination.total }} 条</span>
      </div>

      <div v-if="list.length" class="admin-table-wrap">
        <table class="admin-table">
          <thead>
            <tr>
              <th>电影</th>
              <th>用户</th>
              <th>评分</th>
              <th>评论内容</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="review in list" :key="review.id">
              <td>{{ review.movieTitle || `电影 #${review.movieId}` }}</td>
              <td>{{ review.username }}（ID: {{ review.userId }}）</td>
              <td>{{ formatScore(review.score) }}</td>
              <td class="admin-table__content">{{ review.content }}</td>
              <td>{{ formatDate(review.updatedAt || review.createdAt) }}</td>
              <td>
                <button
                  type="button"
                  class="admin-link admin-link--danger"
                  :disabled="deletingId === review.id"
                  @click="deleteReview(review)"
                >
                  {{ deletingId === review.id ? '删除中' : '删除评论' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <EmptyState
        v-else-if="!loading"
        title="暂无评论数据"
        description="当前筛选条件下没有匹配的评论记录。"
      />

      <div class="admin-page__pagination">
        <PaginationBar
          v-if="pagination.total > pagination.size"
          :page="pagination.page"
          :size="pagination.size"
          :total="pagination.total"
          @change="loadReviews"
        />
      </div>
    </section>
  </section>
</template>

<style scoped>
.admin-page {
  display: grid;
  gap: 1rem;
}

.admin-card {
  padding: 1rem;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  background: #ffffff;
}

.admin-card__title,
.admin-actions {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}

.admin-card__title {
  margin-bottom: 1rem;
}

.admin-card__title h2 {
  font-family: var(--font-sans);
  font-size: 1.1rem;
}

.admin-card__title span {
  color: #6b7280;
  font-size: 0.9rem;
}

.admin-filters {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.9rem;
}

.admin-page__field {
  display: grid;
  gap: 0.45rem;
}

.admin-page__field span {
  color: #374151;
  font-size: 0.9rem;
}

.admin-page__field input {
  background: #ffffff;
  color: #111827;
  border-color: #d1d5db;
  border-radius: 8px;
}

.admin-actions {
  justify-content: end;
  margin-top: 1rem;
}

.admin-table-wrap {
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th,
.admin-table td {
  padding: 0.85rem 0.75rem;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  vertical-align: top;
}

.admin-table th {
  background: #f9fafb;
  color: #374151;
}

.admin-table__content {
  min-width: 260px;
  white-space: pre-wrap;
  color: #374151;
}

.admin-link {
  padding: 0;
  background: none;
  border: 0;
  color: #2563eb;
  box-shadow: none;
}

.admin-link--danger {
  color: #dc2626;
}

.admin-page__pagination :deep(.pagination) {
  margin-top: 1rem;
  border-radius: 8px;
  border-color: #d1d5db;
  background: #ffffff;
}

.admin-page__pagination :deep(.pagination span) {
  color: #4b5563;
}

@media (max-width: 800px) {
  .admin-card__title,
  .admin-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .admin-filters {
    grid-template-columns: 1fr;
  }
}
</style>
