import type {
  ApiEnvelope,
  AuthTokens,
  ChecklistItem,
  Label,
  PageResult,
  Project,
  Task,
  TaskAttachment,
  TaskComment,
  TaskDraft,
  TaskReminder,
  TaskSection,
  User,
  Workspace,
  WorkspaceMember,
} from '../types'
import { normalizeTask } from './utils'

export const API_BASE = (import.meta.env.VITE_API_URL || '/smart-scheduler').replace(/\/$/, '')

export class ApiError extends Error {
  status: number
  code?: number

  constructor(message: string, status: number, code?: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

let accessToken = localStorage.getItem('smartly_access_token') || ''
let refreshInFlight: Promise<boolean> | null = null

export const setAccessToken = (token?: string) => {
  accessToken = token || ''
  if (token) localStorage.setItem('smartly_access_token', token)
  else localStorage.removeItem('smartly_access_token')
}

export const getAccessToken = () => accessToken

async function refreshSession(): Promise<boolean> {
  const refreshToken = localStorage.getItem('smartly_refresh_token')
  if (!refreshToken) return false
  if (refreshInFlight) return refreshInFlight

  refreshInFlight = fetch(`${API_BASE}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
    .then(async (response) => {
      if (!response.ok) return false
      const body = (await response.json()) as ApiEnvelope<AuthTokens>
      if (!body.success || !body.result?.accessToken) return false
      setAccessToken(body.result.accessToken)
      if (body.result.refreshToken) localStorage.setItem('smartly_refresh_token', body.result.refreshToken)
      return true
    })
    .catch(() => false)
    .finally(() => { refreshInFlight = null })

  return refreshInFlight
}

async function request<T>(path: string, options: RequestInit = {}, retried = false): Promise<T> {
  const isForm = options.body instanceof FormData
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...(isForm ? {} : { 'Content-Type': 'application/json' }),
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...options.headers,
    },
  })

  const contentType = response.headers.get('content-type') || ''
  const body = contentType.includes('application/json') ? await response.json() : null

  if (!response.ok || (body && body.success === false)) {
    if (response.status === 401 && !retried && !path.startsWith('/auth/')) {
      const refreshed = await refreshSession()
      if (refreshed) return request<T>(path, options, true)
    }
    if (response.status === 401) {
      setAccessToken()
      localStorage.removeItem('smartly_refresh_token')
    }
    throw new ApiError(body?.message || `Yêu cầu thất bại (${response.status})`, response.status, body?.code)
  }

  return (body as ApiEnvelope<T>)?.result as T
}

const json = (body: unknown) => JSON.stringify(body)

export const api = {
  login: (username: string, password: string) =>
    request<AuthTokens>('/auth/login', { method: 'POST', body: json({ username, password }) }),
  register: (username: string, email: string, password: string) =>
    request<AuthTokens>('/auth/register', { method: 'POST', body: json({ username, email, password }) }),
  logout: (refreshToken?: string | null) =>
    request<void>('/auth/logout', { method: 'POST', body: json({ refreshToken: refreshToken || null }) }),
  me: () => request<User>('/auth/myinfo'),

  users: () => request<User[]>('/users'),
  createUser: (payload: { username: string; email: string; password: string; roleNames: string[] }) =>
    request<User>('/users', { method: 'POST', body: json(payload) }),
  updateUser: (id: string, username: string) =>
    request<User>(`/users/${id}`, { method: 'PUT', body: json({ username }) }),
  updateUserStatus: (id: string, payload: { active?: boolean; roleNames?: string[] }) =>
    request<void>(`/users/${id}/status`, { method: 'PATCH', body: json(payload) }),
  deleteUser: (id: string) => request<void>(`/users/${id}`, { method: 'DELETE' }),

  workspaces: () => request<Workspace[]>('/workspaces'),
  createWorkspace: (payload: Pick<Workspace, 'name' | 'type'> & Partial<Workspace>) =>
    request<Workspace>('/workspaces', { method: 'POST', body: json(payload) }),
  updateWorkspace: (workspace: Workspace) =>
    request<Workspace>(`/workspaces/${workspace.id}`, {
      method: 'PUT',
      body: json({
        name: workspace.name,
        description: workspace.description || '',
        type: workspace.type,
        color: workspace.color || '#6d5dfc',
      }),
    }),
  deleteWorkspace: (id: string) => request<void>(`/workspaces/${id}`, { method: 'DELETE' }),

  members: (workspaceId: string) => request<WorkspaceMember[]>(`/workspaces/${workspaceId}/members`),
  memberCandidates: (workspaceId: string, query = '') =>
    request<Array<Pick<User, 'id' | 'username' | 'email'>>>(`/workspaces/${workspaceId}/members/candidates?q=${encodeURIComponent(query)}`),
  addMember: (workspaceId: string, userId: string, role: string) =>
    request<WorkspaceMember>(`/workspaces/${workspaceId}/members`, {
      method: 'POST',
      body: json({ userId, role }),
    }),
  updateMemberRole: (workspaceId: string, memberId: string, role: string) =>
    request<WorkspaceMember>(`/workspaces/${workspaceId}/members/${memberId}/role`, {
      method: 'PATCH',
      body: json({ role }),
    }),
  removeMember: (workspaceId: string, memberId: string) =>
    request<void>(`/workspaces/${workspaceId}/members/${memberId}`, { method: 'DELETE' }),

  projects: (workspaceId: string) => request<Project[]>(`/workspaces/${workspaceId}/projects`),
  createProject: (workspaceId: string, payload: Partial<Project> & Pick<Project, 'name'>) =>
    request<Project>(`/workspaces/${workspaceId}/projects`, {
      method: 'POST',
      body: json({
        name: payload.name,
        description: payload.description || '',
        color: payload.color || '#6d5dfc',
        icon: payload.icon || 'hash',
        viewType: payload.viewType || 'LIST',
        sortOrder: payload.sortOrder || 0,
      }),
    }),
  updateProject: (project: Project) =>
    request<Project>(`/workspaces/${project.workspaceId}/projects/${project.id}`, {
      method: 'PUT',
      body: json({
        name: project.name,
        description: project.description || '',
        color: project.color || '#6d5dfc',
        icon: project.icon || 'hash',
        viewType: project.viewType,
        sortOrder: project.sortOrder,
      }),
    }),
  deleteProject: (project: Project) =>
    request<void>(`/workspaces/${project.workspaceId}/projects/${project.id}`, { method: 'DELETE' }),

  sections: (workspaceId: string, projectId: string) =>
    request<TaskSection[]>(`/workspaces/${workspaceId}/projects/${projectId}/sections`),
  createSection: (workspaceId: string, projectId: string, name: string, sortOrder: number) =>
    request<TaskSection>(`/workspaces/${workspaceId}/projects/${projectId}/sections`, {
      method: 'POST',
      body: json({ name, description: '', sortOrder }),
    }),
  updateSection: (workspaceId: string, section: TaskSection) =>
    request<TaskSection>(`/workspaces/${workspaceId}/projects/${section.projectId}/sections/${section.id}`, {
      method: 'PUT',
      body: json({ name: section.name, description: section.description || '', sortOrder: section.sortOrder }),
    }),
  deleteSection: (workspaceId: string, section: TaskSection) =>
    request<void>(`/workspaces/${workspaceId}/projects/${section.projectId}/sections/${section.id}`, {
      method: 'DELETE',
    }),

  labels: (workspaceId: string) => request<Label[]>(`/workspaces/${workspaceId}/labels`),
  createLabel: (workspaceId: string, name: string, color: string) =>
    request<Label>(`/workspaces/${workspaceId}/labels`, { method: 'POST', body: json({ name, color }) }),
  updateLabel: (label: Label) =>
    request<Label>(`/workspaces/${label.workspaceId}/labels/${label.id}`, {
      method: 'PUT',
      body: json({ name: label.name, color: label.color || '#6d5dfc' }),
    }),
  deleteLabel: (label: Label) =>
    request<void>(`/workspaces/${label.workspaceId}/labels/${label.id}`, { method: 'DELETE' }),

  tasks: async (projectId: string) =>
    (await request<Task[]>(`/projects/${projectId}/tasks`)).map(normalizeTask),
  visibleTasks: async () =>
    (await request<Task[]>('/tasks')).map(normalizeTask),
  task: async (id: string) => normalizeTask(await request<Task>(`/tasks/${id}`)),
  createTask: async (draft: TaskDraft) =>
    normalizeTask(
      await request<Task>(`/projects/${draft.projectId}/tasks`, {
        method: 'POST',
        body: json(draft),
      }),
    ),
  updateTask: async (task: Task) =>
    normalizeTask(
      await request<Task>(`/tasks/${task.id}`, {
        method: 'PUT',
        body: json({
          sectionId: task.sectionId || null,
          parentTaskId: task.parentTaskId || null,
          assigneeId: task.assigneeId || null,
          title: task.title,
          description: task.description || '',
          priority: task.priority,
          startAt: task.startAt || null,
          dueAt: task.dueAt || null,
          allDay: task.allDay,
          recurrenceRule: task.recurrenceRule || null,
          recurrenceMode: task.recurrenceMode,
          timeZone: task.timeZone || Intl.DateTimeFormat().resolvedOptions().timeZone,
          sortOrder: task.sortOrder,
          labelIds: task.labelIds,
        }),
      }),
    ),
  updateTaskStatus: async (id: string, status: Task['status']) =>
    normalizeTask(await request<Task>(`/tasks/${id}/status`, { method: 'PATCH', body: json({ status }) })),
  deleteTask: (id: string) => request<void>(`/tasks/${id}`, { method: 'DELETE' }),

  checklist: (taskId: string) => request<ChecklistItem[]>(`/tasks/${taskId}/checklist-items`),
  addChecklist: (taskId: string, content: string, sortOrder: number) =>
    request<ChecklistItem>(`/tasks/${taskId}/checklist-items`, {
      method: 'POST',
      body: json({ content, sortOrder }),
    }),
  toggleChecklist: (taskId: string, id: string, completed: boolean) =>
    request<ChecklistItem>(`/tasks/${taskId}/checklist-items/${id}/status`, {
      method: 'PATCH',
      body: json({ completed }),
    }),
  deleteChecklist: (taskId: string, id: string) =>
    request<void>(`/tasks/${taskId}/checklist-items/${id}`, { method: 'DELETE' }),

  comments: (taskId: string) =>
    request<PageResult<TaskComment>>(`/tasks/${taskId}/comments?page=0&size=50`),
  addComment: (taskId: string, content: string) =>
    request<TaskComment>(`/tasks/${taskId}/comments`, { method: 'POST', body: json({ content }) }),
  deleteComment: (taskId: string, id: string) =>
    request<void>(`/tasks/${taskId}/comments/${id}`, { method: 'DELETE' }),

  reminders: (taskId: string) => request<TaskReminder[]>(`/tasks/${taskId}/reminders`),
  addReminder: (taskId: string, remindAt: string) =>
    request<TaskReminder>(`/tasks/${taskId}/reminders`, {
      method: 'POST',
      body: json({ remindAt, timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone, channel: 'IN_APP' }),
    }),
  deleteReminder: (taskId: string, id: string) =>
    request<void>(`/tasks/${taskId}/reminders/${id}`, { method: 'DELETE' }),

  attachments: (taskId: string) => request<TaskAttachment[]>(`/tasks/${taskId}/attachments`),
  addAttachment: (taskId: string, file: File) => {
    const form = new FormData()
    form.append('file', file)
    return request<TaskAttachment>(`/tasks/${taskId}/attachments`, { method: 'POST', body: form })
  },
  deleteAttachment: (taskId: string, id: string) =>
    request<void>(`/tasks/${taskId}/attachments/${id}`, { method: 'DELETE' }),
  attachmentUrl: (taskId: string, id: string) => `${API_BASE}/tasks/${taskId}/attachments/${id}/download`,
}
