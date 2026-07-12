export type ID = string

export type UserRole = 'user' | 'admin'
export type WorkspaceType = 'PERSONAL' | 'TEAM'
export type WorkspaceRole = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'
export type ProjectView = 'LIST' | 'BOARD' | 'CALENDAR'
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type TaskPriority = 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
export type RecurrenceMode = 'NONE' | 'FIXED_SCHEDULE' | 'AFTER_COMPLETION'

export interface ApiEnvelope<T> {
  timestamp?: string
  success: boolean
  code: number
  message: string
  result: T
}

export interface PageResult<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface AuthTokens {
  valid: boolean
  accessToken: string
  refreshToken?: string
}

export interface User {
  id: ID
  username: string
  email?: string
  active: boolean
  roleNames?: UserRole[]
  createdAt?: string
  updatedAt?: string
}

export interface Workspace {
  id: ID
  ownerId: ID
  name: string
  description?: string
  type: WorkspaceType
  color?: string
  archivedAt?: string | null
  deletedAt?: string | null
  createdAt?: string
  updatedAt?: string
  version?: number
}

export interface WorkspaceMember {
  id: ID
  workspaceId: ID
  userId: ID
  username?: string
  email?: string
  active?: boolean
  role: WorkspaceRole
  joinedAt?: string
  updatedAt?: string
  version?: number
}

export interface Project {
  id: ID
  workspaceId: ID
  createdById?: ID
  name: string
  description?: string
  color?: string
  icon?: string
  viewType: ProjectView
  sortOrder: number
  archivedAt?: string | null
  deletedAt?: string | null
  createdAt?: string
  updatedAt?: string
  version?: number
}

export interface TaskSection {
  id: ID
  projectId: ID
  name: string
  description?: string
  sortOrder: number
  archivedAt?: string | null
  createdAt?: string
  updatedAt?: string
  version?: number
}

export interface Label {
  id: ID
  workspaceId: ID
  createdById?: ID
  name: string
  color?: string
  archivedAt?: string | null
  createdAt?: string
  updatedAt?: string
  version?: number
}

export interface Task {
  id: ID
  projectId: ID
  sectionId?: ID | null
  parentTaskId?: ID | null
  createdById?: ID
  assigneeId?: ID | null
  title: string
  description?: string
  status: TaskStatus
  priority: TaskPriority
  startAt?: string | null
  dueAt?: string | null
  allDay: boolean
  recurrenceRule?: string | null
  recurrenceMode: RecurrenceMode
  timeZone?: string | null
  completedAt?: string | null
  archivedAt?: string | null
  deletedAt?: string | null
  sortOrder: number
  labelIds: ID[]
  createdAt?: string
  updatedAt?: string
  version?: number
}

export interface ChecklistItem {
  id: ID
  taskId: ID
  content: string
  completed: boolean
  completedAt?: string | null
  sortOrder: number
  createdAt?: string
  updatedAt?: string
}

export interface TaskComment {
  id: ID
  taskId: ID
  authorId: ID
  content: string
  createdAt?: string
  updatedAt?: string
}

export interface TaskReminder {
  id: ID
  taskId: ID
  recipientId: ID
  remindAt: string
  timeZone?: string
  channel: 'IN_APP' | 'EMAIL'
  status: 'PENDING' | 'SENT' | 'CANCELLED' | 'FAILED'
  sentAt?: string | null
}

export interface TaskAttachment {
  id: ID
  taskId: ID
  uploadedById: ID
  originalFileName: string
  contentType?: string
  sizeBytes?: number
  createdAt?: string
}

export interface TaskDraft {
  projectId: ID
  sectionId?: ID | null
  parentTaskId?: ID | null
  assigneeId?: ID | null
  title: string
  description?: string
  priority: TaskPriority
  startAt?: string | null
  dueAt?: string | null
  allDay: boolean
  recurrenceRule?: string | null
  recurrenceMode: RecurrenceMode
  timeZone?: string | null
  sortOrder: number
  labelIds: ID[]
}

export type Theme = 'light' | 'dark' | 'system'

export interface Preferences {
  theme: Theme
  compactMode: boolean
  weekStartsMonday: boolean
  desktopNotifications: boolean
}

