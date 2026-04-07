<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import PaginationBar from '../components/PaginationBar.vue'
import EmptyState from '../components/EmptyState.vue'
import { formatDate, formatScore } from '../utils/formatters'
import * as adminApi from '../services/admin'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const filters = reactive({
  keyword: '',
})

const pagination = reactive({
  page: 1,
  size: 8,
  total: 0,
})

const list = ref([])
const loading = ref(false)
const submitting = ref(false)
const deletingId = ref(null)
const selectedMovieId = ref(null)
const uploadingPoster = ref(false)
const uploadedPosterName = ref('')

const form = reactive(createEmptyMovieForm())
const posterHint = computed(() => {
  if (uploadingPoster.value) {
    return '海报上传中...'
  }
  if (uploadedPosterName.value) {
    return `当前海报：${uploadedPosterName.value}`
  }
  return '选择一张图片作为海报'
})

onMounted(() => {
  loadMovies()
})

async function loadMovies(page = pagination.page) {
  loading.value = true
  try {
    const response = await adminApi.getAdminMoviePage({
      page,
      size: pagination.size,
      keyword: filters.keyword || undefined,
    })
    list.value = response.items || []
    pagination.page = response.page || page
    pagination.size = response.size || pagination.size
    pagination.total = response.total || 0
  } finally {
    loading.value = false
  }
}

async function editMovie(id) {
  selectedMovieId.value = id
  const detail = await adminApi.getAdminMovieDetail(id)
  applyMovie(detail)
}

function createMovie() {
  selectedMovieId.value = null
  Object.assign(form, createEmptyMovieForm())
  uploadedPosterName.value = ''
}

async function submitMovie() {
  submitting.value = true
  try {
    const isEditing = Boolean(selectedMovieId.value)
    const payload = buildPayload()
    const response = isEditing
      ? await adminApi.updateAdminMovie(selectedMovieId.value, payload)
      : await adminApi.createAdminMovie(payload)

    selectedMovieId.value = response.id
    applyMovie(response)
    authStore.setFlash(isEditing ? '电影信息已保存' : '电影已新增', 'success')
    await loadMovies(isEditing ? pagination.page : 1)
  } finally {
    submitting.value = false
  }
}

async function deleteMovie(movie) {
  if (!window.confirm(`确定删除《${movie.title}》吗？`)) {
    return
  }

  deletingId.value = movie.id
  try {
    await adminApi.deleteAdminMovie(movie.id)
    if (selectedMovieId.value === movie.id) {
      createMovie()
    }
    authStore.setFlash('电影已删除', 'info')
    await loadMovies(1)
  } finally {
    deletingId.value = null
  }
}

async function uploadPoster(event) {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }

  uploadingPoster.value = true
  try {
    form.posterFile = await adminApi.uploadAdminMoviePoster(file)
    uploadedPosterName.value = file.name
    authStore.setFlash('海报上传成功', 'success')
  } finally {
    uploadingPoster.value = false
    event.target.value = ''
  }
}

function applyMovie(detail) {
  form.title = detail.title ?? ''
  form.overview = detail.overview ?? ''
  form.genres = detail.genres ?? ''
  form.keywords = detail.keywords ?? ''
  form.castList = detail.castList ?? ''
  form.producers = detail.producers ?? ''
  form.releaseDate = detail.releaseDate ?? ''
  form.runtime = detail.runtime ?? ''
  form.productionCompanies = detail.productionCompanies ?? ''
  form.tagline = detail.tagline ?? ''
  form.posterFile = detail.posterFile ?? ''
  uploadedPosterName.value = extractFilename(form.posterFile)
}

function buildPayload() {
  return {
    title: form.title.trim(),
    overview: normalizeString(form.overview),
    genres: normalizeString(form.genres),
    keywords: normalizeString(form.keywords),
    castList: normalizeString(form.castList),
    producers: normalizeString(form.producers),
    releaseDate: normalizeString(form.releaseDate),
    runtime: normalizeNumber(form.runtime),
    productionCompanies: normalizeString(form.productionCompanies),
    tagline: normalizeString(form.tagline),
    posterFile: normalizeString(form.posterFile),
  }
}

function createEmptyMovieForm() {
  return {
    title: '',
    overview: '',
    genres: '',
    keywords: '',
    castList: '',
    producers: '',
    releaseDate: '',
    runtime: '',
    productionCompanies: '',
    tagline: '',
    posterFile: '',
  }
}

function extractFilename(path) {
  if (!path) return ''
  const normalized = String(path).trim()
  if (!normalized) return ''
  const segments = normalized.split('/')
  return segments[segments.length - 1] || ''
}

function normalizeString(value) {
  if (value == null) return null
  const normalized = String(value).trim()
  return normalized || null
}

function normalizeNumber(value) {
  if (value === '' || value == null) return null
  const number = Number(value)
  return Number.isNaN(number) ? null : number
}
</script>

<template>
  <section class="admin-page">
    <div class="admin-page__toolbar">
      <div class="admin-page__filters">
        <label class="admin-page__field">
          <span>关键词</span>
          <input v-model="filters.keyword" type="text" placeholder="请输入电影标题或简介关键词" />
        </label>
        <button type="button" @click="loadMovies(1)">查询</button>
      </div>

      <button type="button" class="admin-page__secondary" @click="createMovie">新增电影</button>
    </div>

    <div class="admin-page__layout">
      <section class="admin-card">
        <div class="admin-card__title">
          <h2>电影列表</h2>
          <span>共 {{ pagination.total }} 条</span>
        </div>

        <div v-if="list.length" class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>电影标题</th>
                <th>类型</th>
                <th>上映日期</th>
                <th>评分</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="movie in list" :key="movie.id">
                <td>{{ movie.title }}</td>
                <td>{{ movie.genres || '未填写' }}</td>
                <td>{{ formatDate(movie.releaseDate) }}</td>
                <td>{{ formatScore(movie.tmdbVoteAverage) }}</td>
                <td class="admin-table__actions">
                  <button type="button" class="admin-link" @click="editMovie(movie.id)">编辑</button>
                  <button
                    type="button"
                    class="admin-link admin-link--danger"
                    :disabled="deletingId === movie.id"
                    @click="deleteMovie(movie)"
                  >
                    {{ deletingId === movie.id ? '删除中' : '删除' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <EmptyState
          v-else-if="!loading"
          title="暂无电影数据"
          description="点击右上角“新增电影”，先把第一部电影加进来。"
        />

        <div class="admin-page__pagination">
          <PaginationBar
            v-if="pagination.total > pagination.size"
            :page="pagination.page"
            :size="pagination.size"
            :total="pagination.total"
            @change="loadMovies"
          />
        </div>
      </section>

      <section class="admin-card">
        <div class="admin-card__title">
          <h2>{{ selectedMovieId ? '编辑电影' : '新增电影' }}</h2>
          <span>补充好资料后，前台会更方便展示和检索。</span>
        </div>

        <form class="admin-form" @submit.prevent="submitMovie">
          <div class="admin-form__grid">
            <label class="admin-page__field">
              <span>标题</span>
              <input v-model="form.title" type="text" required />
            </label>
            <label class="admin-page__field admin-page__field--wide">
              <span>标语</span>
              <input v-model="form.tagline" type="text" />
            </label>
            <label class="admin-page__field admin-page__field--wide">
              <span>电影海报</span>
              <input type="file" accept="image/*" @change="uploadPoster" />
              <small class="admin-page__hint">{{ posterHint }}</small>
            </label>
            <label class="admin-page__field admin-page__field--wide">
              <span>简介</span>
              <textarea v-model="form.overview" rows="5" />
            </label>
            <label class="admin-page__field">
              <span>类型</span>
              <input v-model="form.genres" type="text" />
            </label>
            <label class="admin-page__field">
              <span>关键词</span>
              <input v-model="form.keywords" type="text" />
            </label>
            <label class="admin-page__field">
              <span>演员</span>
              <input v-model="form.castList" type="text" />
            </label>
            <label class="admin-page__field">
              <span>制片人</span>
              <input v-model="form.producers" type="text" />
            </label>
            <label class="admin-page__field">
              <span>制作公司</span>
              <input v-model="form.productionCompanies" type="text" />
            </label>
            <label class="admin-page__field">
              <span>上映日期</span>
              <input v-model="form.releaseDate" type="date" />
            </label>
            <label class="admin-page__field">
              <span>时长（分钟）</span>
              <input v-model="form.runtime" type="number" min="0" />
            </label>
          </div>

          <div class="admin-form__actions">
            <button type="button" class="admin-page__secondary" @click="createMovie">清空</button>
            <button type="submit" :disabled="submitting">{{ submitting ? '保存中' : '保存' }}</button>
          </div>
        </form>
      </section>
    </div>
  </section>
</template>

<style scoped>
.admin-page {
  display: grid;
  gap: 1rem;
}

.admin-page__toolbar,
.admin-page__filters,
.admin-card__title,
.admin-form__actions {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}

.admin-page__filters {
  flex: 1;
}

.admin-page__layout {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 1rem;
}

.admin-card {
  padding: 1rem;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  background: #ffffff;
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

.admin-page__field {
  display: grid;
  gap: 0.45rem;
}

.admin-page__field span {
  color: #374151;
  font-size: 0.9rem;
}

.admin-page__hint {
  color: #6b7280;
  font-size: 0.85rem;
}

.admin-page__field input,
.admin-page__field textarea {
  background: #ffffff;
  color: #111827;
  border-color: #d1d5db;
  border-radius: 8px;
}

.admin-page__field--wide {
  grid-column: span 2;
}

.admin-page__secondary {
  background: #ffffff;
  color: #374151;
  border: 1px solid #d1d5db;
  box-shadow: none;
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
  font-size: 0.95rem;
}

.admin-table th {
  background: #f9fafb;
  color: #374151;
}

.admin-table__actions {
  display: flex;
  gap: 0.75rem;
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

.admin-form {
  display: grid;
  gap: 1rem;
}

.admin-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.9rem;
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

@media (max-width: 1100px) {
  .admin-page__layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .admin-page__toolbar,
  .admin-page__filters,
  .admin-card__title,
  .admin-form__actions {
    flex-direction: column;
    align-items: stretch;
  }

  .admin-form__grid {
    grid-template-columns: 1fr;
  }

  .admin-page__field--wide {
    grid-column: span 1;
  }
}
</style>
