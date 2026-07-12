import type { Task, TaskPriority, TaskStatus } from '../types'

export const cx = (...classes: Array<string | false | null | undefined>) =>
  classes.filter(Boolean).join(' ')

export const uid = (prefix = 'local') =>
  `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`

export const initials = (name?: string) => {
  if (!name) return 'U'
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('') || 'U'
}

export const toLocalInput = (value?: string | null) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const offset = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

export const toApiDate = (value?: string | null) => {
  if (!value) return null
  return value.length === 16 ? `${value}:00` : value
}

export const startOfDay = (date = new Date()) => {
  const copy = new Date(date)
  copy.setHours(0, 0, 0, 0)
  return copy
}

export const endOfDay = (date = new Date()) => {
  const copy = new Date(date)
  copy.setHours(23, 59, 59, 999)
  return copy
}

export const addDays = (date: Date, days: number) => {
  const copy = new Date(date)
  copy.setDate(copy.getDate() + days)
  return copy
}

export const isToday = (value?: string | null) => {
  if (!value) return false
  const date = new Date(value)
  const today = new Date()
  return date.toDateString() === today.toDateString()
}

export const isOverdue = (task: Task) =>
  Boolean(
    task.dueAt &&
      task.status !== 'COMPLETED' &&
      task.status !== 'CANCELLED' &&
      new Date(task.dueAt).getTime() < startOfDay().getTime(),
  )

export const isUpcoming = (value?: string | null, days = 7) => {
  if (!value) return false
  const time = new Date(value).getTime()
  return time >= startOfDay().getTime() && time <= endOfDay(addDays(new Date(), days)).getTime()
}

const dateFormatter = new Intl.DateTimeFormat('vi-VN', {
  day: '2-digit',
  month: 'short',
})

const fullDateFormatter = new Intl.DateTimeFormat('vi-VN', {
  weekday: 'short',
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
})

export const formatDate = (value?: string | null, full = false) => {
  if (!value) return 'Chưa đặt hạn'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'Không rõ'
  if (isToday(value)) return 'Hôm nay'
  return (full ? fullDateFormatter : dateFormatter).format(date)
}

export const formatDateTime = (value?: string | null) => {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

export const relativeDate = (value?: string | null) => {
  if (!value) return ''
  const date = new Date(value)
  const diff = date.getTime() - startOfDay().getTime()
  const days = Math.round(diff / 86_400_000)
  if (days === -1) return 'Hôm qua'
  if (days === 0) return 'Hôm nay'
  if (days === 1) return 'Ngày mai'
  if (days > 1 && days < 7) return `${days} ngày nữa`
  return formatDate(value)
}

export const statusLabel: Record<TaskStatus, string> = {
  TODO: 'Cần làm',
  IN_PROGRESS: 'Đang làm',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
}

export const priorityLabel: Record<TaskPriority, string> = {
  NONE: 'Không ưu tiên',
  LOW: 'Thấp',
  MEDIUM: 'Trung bình',
  HIGH: 'Cao',
  URGENT: 'Khẩn cấp',
}

export const normalizeTask = (task: Task): Task => ({
  ...task,
  labelIds: Array.isArray(task.labelIds) ? task.labelIds : Array.from(task.labelIds || []),
})

export const decodeRoles = (token?: string): string[] => {
  if (!token) return []
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    const scope = payload.scope ?? payload.scp ?? ''
    return (Array.isArray(scope) ? scope : String(scope).split(/\s+/))
      .filter(Boolean)
      .map((role: string) => role.replace(/^ROLE_/i, '').toLowerCase())
  } catch {
    return []
  }
}

export const fileSize = (bytes?: number) => {
  if (!bytes) return '0 KB'
  if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

