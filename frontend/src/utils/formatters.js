import { copy } from '../content/copy'

export function formatDate(input) {
  if (!input) return copy.common.unknownRelease

  const date = new Date(input)
  if (Number.isNaN(date.getTime())) return input

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date)
}

export function formatScore(value, digits = 1) {
  const number = Number(value)
  if (Number.isNaN(number)) return '--'
  return number.toFixed(digits)
}

export function formatVotes(value) {
  const number = Number(value)
  if (Number.isNaN(number) || number <= 0) return copy.common.noVotes
  return copy.common.votes(number)
}

export function splitTags(value) {
  if (!value) return []
  return value
    .split(/[|,]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

export function normalizePoster(path) {
  if (!path) {
    return 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=900&q=80'
  }

  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }

  if (path.startsWith('/')) {
    return path
  }

  return `/uploads/${path.replace(/^\/+/, '')}`
}
