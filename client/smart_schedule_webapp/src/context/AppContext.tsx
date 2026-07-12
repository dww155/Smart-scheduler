/* eslint-disable react-refresh/only-export-components */
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { api, getAccessToken, setAccessToken } from '../lib/api'
import { decodeRoles, uid } from '../lib/utils'
import { demoUsers, makeDemoData, type DemoData } from '../data/demo'
import type {
  ChecklistItem,
  Label,
  Preferences,
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
  WorkspaceRole,
} from '../types'

interface ToastMessage {
  id: string
  message: string
  tone: 'success' | 'error' | 'info'
}

interface AppContextValue {
  ready: boolean
  loading: boolean
  demoMode: boolean
  currentUser: User | null
  isAuthenticated: boolean
  isAdmin: boolean
  users: User[]
  workspaces: Workspace[]
  projects: Project[]
  sections: TaskSection[]
  labels: Label[]
  tasks: Task[]
  members: WorkspaceMember[]
  activeWorkspaceId: string
  activeWorkspace?: Workspace
  preferences: Preferences
  toasts: ToastMessage[]
  login: (username: string, password: string) => Promise<void>
  register: (username: string, email: string, password: string) => Promise<void>
  enterDemo: (role?: 'user' | 'admin') => void
  logout: () => void
  reload: () => Promise<void>
  setActiveWorkspaceId: (id: string) => void
  setPreferences: (value: Preferences) => void
  notify: (message: string, tone?: ToastMessage['tone']) => void
  dismissToast: (id: string) => void
  createTask: (draft: TaskDraft) => Promise<Task>
  updateTask: (task: Task) => Promise<Task>
  setTaskStatus: (task: Task, status: Task['status']) => Promise<Task>
  deleteTask: (task: Task) => Promise<void>
  createWorkspace: (payload: Partial<Workspace> & Pick<Workspace, 'name' | 'type'>) => Promise<Workspace>
  updateWorkspace: (workspace: Workspace) => Promise<Workspace>
  deleteWorkspace: (workspace: Workspace) => Promise<void>
  createProject: (payload: Partial<Project> & Pick<Project, 'name' | 'workspaceId'>) => Promise<Project>
  updateProject: (project: Project) => Promise<Project>
  deleteProject: (project: Project) => Promise<void>
  createSection: (projectId: string, name: string) => Promise<TaskSection>
  updateSection: (section: TaskSection) => Promise<TaskSection>
  deleteSection: (section: TaskSection) => Promise<void>
  createLabel: (workspaceId: string, name: string, color: string) => Promise<Label>
  updateLabel: (label: Label) => Promise<Label>
  deleteLabel: (label: Label) => Promise<void>
  addMember: (workspaceId: string, userId: string, role: WorkspaceRole) => Promise<WorkspaceMember>
  updateMemberRole: (member: WorkspaceMember, role: WorkspaceRole) => Promise<WorkspaceMember>
  removeMember: (member: WorkspaceMember) => Promise<void>
  createUser: (payload: { username: string; email: string; password: string; roleNames: string[] }) => Promise<User>
  updateUserStatus: (user: User, active: boolean, roleNames?: string[]) => Promise<void>
  deleteUser: (user: User) => Promise<void>
  loadTaskExtras: (taskId: string) => Promise<{
    checklist: ChecklistItem[]
    comments: TaskComment[]
    reminders: TaskReminder[]
    attachments: TaskAttachment[]
  }>
  addChecklist: (taskId: string, content: string) => Promise<ChecklistItem>
  toggleChecklist: (item: ChecklistItem) => Promise<ChecklistItem>
  deleteChecklist: (item: ChecklistItem) => Promise<void>
  addComment: (taskId: string, content: string) => Promise<TaskComment>
  deleteComment: (comment: TaskComment) => Promise<void>
  addReminder: (taskId: string, remindAt: string) => Promise<TaskReminder>
  deleteReminder: (reminder: TaskReminder) => Promise<void>
  addAttachment: (taskId: string, file: File) => Promise<TaskAttachment>
  deleteAttachment: (attachment: TaskAttachment) => Promise<void>
  getDemoExtras: () => Pick<DemoData, 'checklist' | 'comments' | 'reminders' | 'attachments'>
}

const defaultPreferences: Preferences = {
  theme: 'light',
  compactMode: false,
  weekStartsMonday: true,
  desktopNotifications: true,
}

const AppContext = createContext<AppContextValue | null>(null)
const DEMO_KEY = 'smartly_demo_data_v1'

const loadStoredDemo = (): DemoData => {
  try {
    const stored = localStorage.getItem(DEMO_KEY)
    return stored ? (JSON.parse(stored) as DemoData) : makeDemoData()
  } catch {
    return makeDemoData()
  }
}

export function AppProvider({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false)
  const [loading, setLoading] = useState(false)
  const [demoMode, setDemoMode] = useState(Boolean(localStorage.getItem('smartly_demo_role')))
  const [currentUser, setCurrentUser] = useState<User | null>(null)
  const [users, setUsers] = useState<User[]>([])
  const [workspaces, setWorkspaces] = useState<Workspace[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [sections, setSections] = useState<TaskSection[]>([])
  const [labels, setLabels] = useState<Label[]>([])
  const [tasks, setTasks] = useState<Task[]>([])
  const [members, setMembers] = useState<WorkspaceMember[]>([])
  const [extras, setExtras] = useState(() => {
    const data = loadStoredDemo()
    return {
      checklist: data.checklist,
      comments: data.comments,
      reminders: data.reminders,
      attachments: data.attachments,
    }
  })
  const [activeWorkspaceId, setActiveWorkspaceIdState] = useState(
    localStorage.getItem('smartly_workspace') || '',
  )
  const [preferences, setPreferencesState] = useState<Preferences>(() => {
    try {
      return { ...defaultPreferences, ...JSON.parse(localStorage.getItem('smartly_preferences') || '{}') }
    } catch {
      return defaultPreferences
    }
  })
  const [toasts, setToasts] = useState<ToastMessage[]>([])

  const roles = useMemo(
    () => currentUser?.roleNames?.map((role) => role.toLowerCase()) || decodeRoles(getAccessToken()),
    [currentUser],
  )
  const isAdmin = roles.includes('admin')

  const notify = useCallback((message: string, tone: ToastMessage['tone'] = 'success') => {
    const id = uid('toast')
    setToasts((items) => [...items, { id, message, tone }])
    window.setTimeout(() => setToasts((items) => items.filter((item) => item.id !== id)), 3600)
  }, [])

  const dismissToast = useCallback((id: string) => {
    setToasts((items) => items.filter((item) => item.id !== id))
  }, [])

  const hydrateDemo = useCallback((role: 'user' | 'admin') => {
    const data = loadStoredDemo()
    const user = role === 'admin' ? demoUsers.find((item) => item.id === 'user-admin')! : demoUsers[0]
    setCurrentUser({ ...user })
    setUsers(data.users)
    setWorkspaces(role === 'admin' ? data.workspaces : data.workspaces.filter((ws) => data.members.some((m) => m.workspaceId === ws.id && m.userId === user.id)))
    setProjects(data.projects)
    setSections(data.sections)
    setLabels(data.labels)
    setTasks(data.tasks)
    setMembers(data.members)
    setExtras({ checklist: data.checklist, comments: data.comments, reminders: data.reminders, attachments: data.attachments })
    setActiveWorkspaceIdState((current) => current || data.workspaces[0]?.id || '')
    setReady(true)
  }, [])

  const loadRemoteData = useCallback(async (providedUser?: User) => {
    setLoading(true)
    try {
      const me = providedUser || (await api.me())
      const tokenRoles = decodeRoles(getAccessToken())
      const enrichedMe = { ...me, roleNames: (me.roleNames?.length ? me.roleNames : tokenRoles) as User['roleNames'] }
      setCurrentUser(enrichedMe)
      const wsList = await api.workspaces()
      setWorkspaces(wsList)

      let allUsers: User[] = [enrichedMe]
      if (tokenRoles.includes('admin') || me.roleNames?.includes('admin')) {
        allUsers = await api.users().catch(() => [enrichedMe])
      }
      setUsers(allUsers)

      const workspaceBundles = await Promise.all(
        wsList.map(async (workspace) => {
          const [workspaceProjects, workspaceLabels, workspaceMembers] = await Promise.all([
            api.projects(workspace.id).catch(() => []),
            api.labels(workspace.id).catch(() => []),
            api.members(workspace.id).catch(() => []),
          ])
          return { workspace, workspaceProjects, workspaceLabels, workspaceMembers }
        }),
      )

      const allProjects = workspaceBundles.flatMap((bundle) => bundle.workspaceProjects)
      const allLabels = workspaceBundles.flatMap((bundle) => bundle.workspaceLabels)
      const allMembers = workspaceBundles.flatMap((bundle) => bundle.workspaceMembers).map((member) => {
        const user = allUsers.find((item) => item.id === member.userId)
        return { ...member, username: member.username || user?.username, email: member.email || user?.email, active: member.active ?? user?.active }
      })
      setProjects(allProjects)
      setLabels(allLabels)
      setMembers(allMembers)

      const projectSections = await Promise.all(
        allProjects.map(async (project) => {
          const workspace = wsList.find((item) => item.id === project.workspaceId)
          return workspace ? api.sections(workspace.id, project.id).catch(() => []) : Promise.resolve([])
        }),
      )
      const allVisibleTasks = await api.visibleTasks().catch(async () => {
        const taskLists = await Promise.all(allProjects.map((project) => api.tasks(project.id).catch(() => [])))
        return taskLists.flat()
      })
      setSections(projectSections.flat())
      setTasks(allVisibleTasks)
      setActiveWorkspaceIdState((current) =>
        wsList.some((item) => item.id === current) ? current : wsList[0]?.id || '',
      )
    } finally {
      setLoading(false)
      setReady(true)
    }
  }, [])

  useEffect(() => {
    const demoRole = localStorage.getItem('smartly_demo_role') as 'user' | 'admin' | null
    if (demoRole) {
      hydrateDemo(demoRole)
      return
    }
    if (!getAccessToken()) {
      setReady(true)
      return
    }
    loadRemoteData().catch(() => {
      setAccessToken()
      setCurrentUser(null)
      setReady(true)
    })
  }, [hydrateDemo, loadRemoteData])

  useEffect(() => {
    localStorage.setItem('smartly_workspace', activeWorkspaceId)
  }, [activeWorkspaceId])

  useEffect(() => {
    localStorage.setItem('smartly_preferences', JSON.stringify(preferences))
    document.documentElement.dataset.theme = preferences.theme
    document.documentElement.dataset.compact = String(preferences.compactMode)
  }, [preferences])

  useEffect(() => {
    if (!demoMode || !ready) return
    const allDemoWorkspaces = makeDemoData().workspaces
    const data: DemoData = {
      users,
      workspaces: isAdmin ? workspaces : Array.from(new Map([...allDemoWorkspaces, ...workspaces].map((item) => [item.id, item])).values()),
      members,
      projects,
      sections,
      labels,
      tasks,
      ...extras,
    }
    localStorage.setItem(DEMO_KEY, JSON.stringify(data))
  }, [demoMode, ready, users, workspaces, members, projects, sections, labels, tasks, extras, isAdmin])

  const login = async (username: string, password: string) => {
    setLoading(true)
    try {
      const tokens = await api.login(username, password)
      setAccessToken(tokens.accessToken)
      if (tokens.refreshToken) localStorage.setItem('smartly_refresh_token', tokens.refreshToken)
      localStorage.removeItem('smartly_demo_role')
      setDemoMode(false)
      await loadRemoteData()
    } finally {
      setLoading(false)
    }
  }

  const register = async (username: string, email: string, password: string) => {
    setLoading(true)
    try {
      const tokens = await api.register(username, email, password)
      setAccessToken(tokens.accessToken)
      if (tokens.refreshToken) localStorage.setItem('smartly_refresh_token', tokens.refreshToken)
      localStorage.removeItem('smartly_demo_role')
      setDemoMode(false)
      await loadRemoteData()
    } finally {
      setLoading(false)
    }
  }

  const enterDemo = (role: 'user' | 'admin' = 'user') => {
    setAccessToken()
    localStorage.setItem('smartly_demo_role', role)
    setDemoMode(true)
    hydrateDemo(role)
  }

  const logout = () => {
    if (!demoMode && getAccessToken()) {
      void api.logout(localStorage.getItem('smartly_refresh_token')).catch(() => undefined)
    }
    setAccessToken()
    localStorage.removeItem('smartly_refresh_token')
    localStorage.removeItem('smartly_demo_role')
    setDemoMode(false)
    setCurrentUser(null)
    setUsers([])
    setWorkspaces([])
    setProjects([])
    setSections([])
    setLabels([])
    setTasks([])
    setMembers([])
  }

  const reload = async () => {
    if (demoMode) {
      hydrateDemo(isAdmin ? 'admin' : 'user')
      return
    }
    await loadRemoteData()
  }

  const setActiveWorkspaceId = (id: string) => setActiveWorkspaceIdState(id)
  const setPreferences = (value: Preferences) => setPreferencesState(value)
  const activeWorkspace = workspaces.find((workspace) => workspace.id === activeWorkspaceId) || workspaces[0]

  const createTask = async (draft: TaskDraft) => {
    const created = demoMode
      ? ({ ...draft, id: uid('task'), status: 'TODO', completedAt: null, createdById: currentUser?.id, createdAt: new Date().toISOString() } as Task)
      : await api.createTask(draft)
    setTasks((items) => [...items, created])
    notify('Đã tạo công việc mới')
    return created
  }

  const updateTask = async (task: Task) => {
    const updated = demoMode ? { ...task, updatedAt: new Date().toISOString() } : await api.updateTask(task)
    setTasks((items) => items.map((item) => (item.id === updated.id ? updated : item)))
    notify('Đã lưu thay đổi')
    return updated
  }

  const setTaskStatus = async (task: Task, status: Task['status']) => {
    const updated = demoMode
      ? { ...task, status, completedAt: status === 'COMPLETED' ? new Date().toISOString() : null }
      : await api.updateTaskStatus(task.id, status)
    setTasks((items) => items.map((item) => (item.id === updated.id ? updated : item)))
    notify(status === 'COMPLETED' ? 'Tuyệt! Công việc đã hoàn thành' : 'Đã cập nhật trạng thái')
    return updated
  }

  const deleteTask = async (task: Task) => {
    if (!demoMode) await api.deleteTask(task.id)
    setTasks((items) => {
      const removedIds = new Set([task.id])
      let changed = true
      while (changed) {
        changed = false
        items.forEach((item) => {
          if (item.parentTaskId && removedIds.has(item.parentTaskId) && !removedIds.has(item.id)) {
            removedIds.add(item.id)
            changed = true
          }
        })
      }
      return items.filter((item) => !removedIds.has(item.id))
    })
    notify('Đã chuyển công việc vào thùng rác', 'info')
  }

  const createWorkspace = async (payload: Partial<Workspace> & Pick<Workspace, 'name' | 'type'>) => {
    const created = demoMode
      ? ({ ...payload, id: uid('ws'), ownerId: currentUser!.id, color: payload.color || '#6d5dfc', createdAt: new Date().toISOString() } as Workspace)
      : await api.createWorkspace(payload)
    setWorkspaces((items) => [...items, created])
    setMembers((items) => [...items, { id: uid('member'), workspaceId: created.id, userId: currentUser!.id, username: currentUser!.username, email: currentUser!.email, role: 'OWNER' }])
    setActiveWorkspaceIdState(created.id)
    notify('Workspace đã sẵn sàng')
    return created
  }

  const updateWorkspace = async (workspace: Workspace) => {
    const updated = demoMode ? workspace : await api.updateWorkspace(workspace)
    setWorkspaces((items) => items.map((item) => (item.id === updated.id ? updated : item)))
    notify('Đã cập nhật workspace')
    return updated
  }

  const deleteWorkspace = async (workspace: Workspace) => {
    if (!demoMode) await api.deleteWorkspace(workspace.id)
    setWorkspaces((items) => items.filter((item) => item.id !== workspace.id))
    setActiveWorkspaceIdState((current) => current === workspace.id ? workspaces.find((item) => item.id !== workspace.id)?.id || '' : current)
    notify('Đã xóa workspace', 'info')
  }

  const createProject = async (payload: Partial<Project> & Pick<Project, 'name' | 'workspaceId'>) => {
    const created = demoMode
      ? ({ id: uid('project'), description: '', color: '#6d5dfc', icon: 'hash', viewType: 'LIST', sortOrder: projects.filter((p) => p.workspaceId === payload.workspaceId).length, ...payload, createdById: currentUser!.id } as Project)
      : await api.createProject(payload.workspaceId, payload)
    setProjects((items) => [...items, created])
    notify('Đã tạo dự án')
    return created
  }

  const updateProject = async (project: Project) => {
    const updated = demoMode ? project : await api.updateProject(project)
    setProjects((items) => items.map((item) => item.id === updated.id ? updated : item))
    notify('Đã cập nhật dự án')
    return updated
  }

  const deleteProject = async (project: Project) => {
    if (!demoMode) await api.deleteProject(project)
    setProjects((items) => items.filter((item) => item.id !== project.id))
    setTasks((items) => items.filter((item) => item.projectId !== project.id))
    notify('Đã xóa dự án', 'info')
  }

  const createSection = async (projectId: string, name: string) => {
    const project = projects.find((item) => item.id === projectId)!
    const sortOrder = sections.filter((item) => item.projectId === projectId).length
    const created = demoMode
      ? { id: uid('section'), projectId, name, description: '', sortOrder }
      : await api.createSection(project.workspaceId, projectId, name, sortOrder)
    setSections((items) => [...items, created])
    notify('Đã thêm section')
    return created
  }

  const updateSection = async (section: TaskSection) => {
    const project = projects.find((item) => item.id === section.projectId)!
    const updated = demoMode ? section : await api.updateSection(project.workspaceId, section)
    setSections((items) => items.map((item) => item.id === updated.id ? updated : item))
    notify('Đã cập nhật section')
    return updated
  }

  const deleteSection = async (section: TaskSection) => {
    const project = projects.find((item) => item.id === section.projectId)!
    if (!demoMode) await api.deleteSection(project.workspaceId, section)
    setSections((items) => items.filter((item) => item.id !== section.id))
    setTasks((items) => items.map((item) => item.sectionId === section.id ? { ...item, sectionId: null } : item))
    notify('Đã lưu trữ section', 'info')
  }

  const createLabel = async (workspaceId: string, name: string, color: string) => {
    const created = demoMode
      ? { id: uid('label'), workspaceId, name, color, createdById: currentUser!.id }
      : await api.createLabel(workspaceId, name, color)
    setLabels((items) => [...items, created])
    notify('Đã tạo nhãn')
    return created
  }

  const updateLabel = async (label: Label) => {
    const updated = demoMode ? label : await api.updateLabel(label)
    setLabels((items) => items.map((item) => item.id === updated.id ? updated : item))
    notify('Đã cập nhật nhãn')
    return updated
  }

  const deleteLabel = async (label: Label) => {
    if (!demoMode) await api.deleteLabel(label)
    setLabels((items) => items.filter((item) => item.id !== label.id))
    setTasks((items) => items.map((item) => ({ ...item, labelIds: item.labelIds.filter((id) => id !== label.id) })))
    notify('Đã lưu trữ nhãn', 'info')
  }

  const addMember = async (workspaceId: string, userId: string, role: WorkspaceRole) => {
    const user = users.find((item) => item.id === userId)
    const created = demoMode
      ? { id: uid('member'), workspaceId, userId, role, username: user?.username, email: user?.email, active: user?.active }
      : await api.addMember(workspaceId, userId, role)
    setMembers((items) => [...items, created])
    notify('Đã thêm thành viên')
    return created
  }

  const updateMemberRole = async (member: WorkspaceMember, role: WorkspaceRole) => {
    const updated = demoMode ? { ...member, role } : await api.updateMemberRole(member.workspaceId, member.id, role)
    setMembers((items) => items.map((item) => item.id === updated.id ? { ...item, ...updated } : item))
    notify('Đã cập nhật vai trò')
    return updated
  }

  const removeMember = async (member: WorkspaceMember) => {
    if (!demoMode) await api.removeMember(member.workspaceId, member.id)
    setMembers((items) => items.filter((item) => item.id !== member.id))
    notify('Đã xóa thành viên khỏi workspace', 'info')
  }

  const createUser = async (payload: { username: string; email: string; password: string; roleNames: string[] }) => {
    const created = demoMode
      ? { id: uid('user'), username: payload.username, email: payload.email, active: true, roleNames: payload.roleNames as User['roleNames'], createdAt: new Date().toISOString() }
      : await api.createUser(payload)
    setUsers((items) => [...items, created])
    notify('Đã tạo tài khoản')
    return created
  }

  const updateUserStatus = async (user: User, active: boolean, roleNames?: string[]) => {
    if (!demoMode) await api.updateUserStatus(user.id, { active, roleNames })
    setUsers((items) => items.map((item) => item.id === user.id ? { ...item, active, roleNames: (roleNames || item.roleNames) as User['roleNames'] } : item))
    notify(active ? 'Đã kích hoạt tài khoản' : 'Đã tạm khóa tài khoản')
  }

  const deleteUser = async (user: User) => {
    if (!demoMode) await api.deleteUser(user.id)
    setUsers((items) => items.filter((item) => item.id !== user.id))
    notify('Đã xóa tài khoản', 'info')
  }

  const loadTaskExtras = async (taskId: string) => {
    if (demoMode) {
      return {
        checklist: extras.checklist.filter((item) => item.taskId === taskId),
        comments: extras.comments.filter((item) => item.taskId === taskId),
        reminders: extras.reminders.filter((item) => item.taskId === taskId),
        attachments: extras.attachments.filter((item) => item.taskId === taskId),
      }
    }
    const [checklist, comments, reminders, attachments] = await Promise.all([
      api.checklist(taskId),
      api.comments(taskId).then((page) => page.content),
      api.reminders(taskId),
      api.attachments(taskId),
    ])
    return { checklist, comments, reminders, attachments }
  }

  const addChecklist = async (taskId: string, content: string) => {
    const sortOrder = extras.checklist.filter((item) => item.taskId === taskId).length
    const created = demoMode
      ? { id: uid('check'), taskId, content, completed: false, sortOrder }
      : await api.addChecklist(taskId, content, sortOrder)
    setExtras((value) => ({ ...value, checklist: [...value.checklist, created] }))
    return created
  }

  const toggleChecklist = async (item: ChecklistItem) => {
    const updated = demoMode ? { ...item, completed: !item.completed, completedAt: !item.completed ? new Date().toISOString() : null } : await api.toggleChecklist(item.taskId, item.id, !item.completed)
    setExtras((value) => ({ ...value, checklist: value.checklist.map((entry) => entry.id === updated.id ? updated : entry) }))
    return updated
  }

  const deleteChecklist = async (item: ChecklistItem) => {
    if (!demoMode) await api.deleteChecklist(item.taskId, item.id)
    setExtras((value) => ({ ...value, checklist: value.checklist.filter((entry) => entry.id !== item.id) }))
  }

  const addComment = async (taskId: string, content: string) => {
    const created = demoMode
      ? { id: uid('comment'), taskId, authorId: currentUser!.id, content, createdAt: new Date().toISOString() }
      : await api.addComment(taskId, content)
    setExtras((value) => ({ ...value, comments: [...value.comments, created] }))
    return created
  }

  const deleteComment = async (comment: TaskComment) => {
    if (!demoMode) await api.deleteComment(comment.taskId, comment.id)
    setExtras((value) => ({ ...value, comments: value.comments.filter((entry) => entry.id !== comment.id) }))
  }

  const addReminder = async (taskId: string, remindAt: string) => {
    const created = demoMode
      ? { id: uid('reminder'), taskId, recipientId: currentUser!.id, remindAt, timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone, channel: 'IN_APP' as const, status: 'PENDING' as const }
      : await api.addReminder(taskId, remindAt)
    setExtras((value) => ({ ...value, reminders: [...value.reminders, created] }))
    return created
  }

  const deleteReminder = async (reminder: TaskReminder) => {
    if (!demoMode) await api.deleteReminder(reminder.taskId, reminder.id)
    setExtras((value) => ({ ...value, reminders: value.reminders.filter((entry) => entry.id !== reminder.id) }))
  }

  const addAttachment = async (taskId: string, file: File) => {
    const created = demoMode
      ? { id: uid('attachment'), taskId, uploadedById: currentUser!.id, originalFileName: file.name, contentType: file.type, sizeBytes: file.size, createdAt: new Date().toISOString() }
      : await api.addAttachment(taskId, file)
    setExtras((value) => ({ ...value, attachments: [...value.attachments, created] }))
    return created
  }

  const deleteAttachment = async (attachment: TaskAttachment) => {
    if (!demoMode) await api.deleteAttachment(attachment.taskId, attachment.id)
    setExtras((value) => ({ ...value, attachments: value.attachments.filter((entry) => entry.id !== attachment.id) }))
  }

  const value: AppContextValue = {
    ready, loading, demoMode, currentUser, isAuthenticated: Boolean(currentUser), isAdmin,
    users, workspaces, projects, sections, labels, tasks, members,
    activeWorkspaceId: activeWorkspace?.id || '', activeWorkspace, preferences, toasts,
    login, register, enterDemo, logout, reload, setActiveWorkspaceId, setPreferences,
    notify, dismissToast, createTask, updateTask, setTaskStatus, deleteTask,
    createWorkspace, updateWorkspace, deleteWorkspace,
    createProject, updateProject, deleteProject,
    createSection, updateSection, deleteSection,
    createLabel, updateLabel, deleteLabel,
    addMember, updateMemberRole, removeMember,
    createUser, updateUserStatus, deleteUser,
    loadTaskExtras, addChecklist, toggleChecklist, deleteChecklist,
    addComment, deleteComment, addReminder, deleteReminder,
    addAttachment, deleteAttachment,
    getDemoExtras: () => extras,
  }

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>
}

export const useApp = () => {
  const context = useContext(AppContext)
  if (!context) throw new Error('useApp must be used inside AppProvider')
  return context
}
