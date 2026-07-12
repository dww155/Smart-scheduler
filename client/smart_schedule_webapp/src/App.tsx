import { useCallback, useEffect, useState } from 'react'
import './App.css'
import { AppProvider, useApp } from './context/AppContext'
import { usePathname, navigate } from './lib/router'
import type { Task } from './types'
import { AdminShell, AppShell } from './components/shells'
import { QuickTaskModal, TaskDetailDrawer } from './components/tasks'
import { Button, EmptyState, Icon } from './components/ui'
import { AuthPage, ForgotPasswordPage, LandingPage } from './pages/PublicPages'
import {
  AllTasksPage,
  CalendarPage,
  CompletedPage,
  InboxPage,
  ProjectPage,
  ProjectsPage,
  TodayPage,
  UpcomingPage,
} from './pages/UserPages'
import {
  CreateProjectModal,
  CreateWorkspaceModal,
  LabelsPage,
  NotificationsPage,
  SearchPage,
  SettingsPage,
  WorkspacePage,
} from './pages/UserUtilityPages'
import {
  AdminContentPage,
  AdminOverviewPage,
  AdminSystemPage,
  AdminUsersPage,
  AdminWorkspacesPage,
} from './pages/AdminPages'

interface QuickConfig {
  projectId?: string
  sectionId?: string | null
  parentTaskId?: string | null
  defaultDueAt?: string
}

function AppRouter() {
  const pathname = usePathname()
  const app = useApp()
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const [quickOpen, setQuickOpen] = useState(false)
  const [quickConfig, setQuickConfig] = useState<QuickConfig>({})
  const [projectModal, setProjectModal] = useState(false)
  const [workspaceModal, setWorkspaceModal] = useState(false)

  const openQuick = useCallback((config: QuickConfig = {}) => {
    setQuickConfig(config)
    setQuickOpen(true)
  }, [])

  useEffect(() => {
    const onQuick = (event: Event) => openQuick((event as CustomEvent<QuickConfig>).detail || {})
    const onTask = (event: Event) => setSelectedTask((event as CustomEvent<Task>).detail)
    window.addEventListener('smartly:quick-add', onQuick)
    window.addEventListener('smartly:open-task', onTask)
    return () => { window.removeEventListener('smartly:quick-add', onQuick); window.removeEventListener('smartly:open-task', onTask) }
  }, [openQuick])

  useEffect(() => {
    if (pathname === '/app') navigate('/app/today', true)
    if (pathname === '/admin') navigate('/admin/overview', true)
    if (pathname === '/app/workspace' && new URLSearchParams(window.location.search).get('create') === '1') {
      setWorkspaceModal(true)
      window.history.replaceState({}, '', '/app/workspace')
    }
  }, [pathname])

  if (!app.ready) return <LoadingScreen />

  if (pathname === '/') return <LandingPage />
  if (pathname === '/demo/user') return <DemoRedirect role="user" />
  if (pathname === '/demo/admin') return <DemoRedirect role="admin" />
  if (pathname === '/login') return app.isAuthenticated ? <Redirect to={app.isAdmin ? '/admin/overview' : '/app/today'} /> : <AuthPage mode="login" />
  if (pathname === '/register') return app.isAuthenticated ? <Redirect to="/app/today" /> : <AuthPage mode="register" />
  if (pathname === '/forgot-password' || pathname === '/reset-password') return <ForgotPasswordPage />

  if (pathname.startsWith('/admin')) {
    if (!app.isAuthenticated) return <Redirect to="/login" />
    if (!app.isAdmin) return <ForbiddenPage />
    let page: React.ReactNode
    if (pathname === '/admin/overview') page = <AdminOverviewPage />
    else if (pathname === '/admin/users') page = <AdminUsersPage />
    else if (pathname === '/admin/workspaces') page = <AdminWorkspacesPage />
    else if (pathname === '/admin/content') page = <AdminContentPage />
    else if (pathname === '/admin/system') page = <AdminSystemPage />
    else page = <NotFoundPage />
    return <AdminShell pathname={pathname}>{page}<ToastRegion /></AdminShell>
  }

  if (pathname.startsWith('/app')) {
    if (!app.isAuthenticated) return <Redirect to="/login" />
    if (!app.workspaces.length && pathname !== '/app/settings') {
      return <AppShell pathname={pathname} onQuickAdd={() => setWorkspaceModal(true)}><OnboardingPage onCreate={() => setWorkspaceModal(true)} /><CreateWorkspaceModal open={workspaceModal} onClose={() => setWorkspaceModal(false)} /><ToastRegion /></AppShell>
    }

    const projectMatch = pathname.match(/^\/app\/project\/([^/]+)$/)
    let page: React.ReactNode
    if (pathname === '/app/today') page = <TodayPage onOpenTask={setSelectedTask} onQuickAdd={() => openQuick()} />
    else if (pathname === '/app/inbox') page = <InboxPage onOpenTask={setSelectedTask} onQuickAdd={() => openQuick()} />
    else if (pathname === '/app/upcoming') page = <UpcomingPage onOpenTask={setSelectedTask} onQuickAdd={() => openQuick()} />
    else if (pathname === '/app/all') page = <AllTasksPage onOpenTask={setSelectedTask} onQuickAdd={() => openQuick()} />
    else if (pathname === '/app/completed') page = <CompletedPage onOpenTask={setSelectedTask} />
    else if (pathname === '/app/calendar') page = <CalendarPage onOpenTask={setSelectedTask} onQuickAdd={() => openQuick()} />
    else if (pathname === '/app/projects') page = <ProjectsPage onCreate={() => setProjectModal(true)} />
    else if (projectMatch) {
      const projectId = decodeURIComponent(projectMatch[1])
      page = <ProjectPage projectId={projectId} onOpenTask={setSelectedTask} onQuickAdd={() => openQuick({ projectId })} />
    } else if (pathname === '/app/labels') page = <LabelsPage onOpenTask={setSelectedTask} />
    else if (pathname === '/app/search') page = <SearchPage onOpenTask={setSelectedTask} />
    else if (pathname === '/app/workspace') page = <WorkspacePage />
    else if (pathname === '/app/settings') page = <SettingsPage />
    else if (pathname === '/app/notifications') page = <NotificationsPage onOpenTask={setSelectedTask} />
    else page = <NotFoundPage />

    return (
      <AppShell pathname={pathname} onQuickAdd={() => openQuick()}>
        {page}
        <QuickTaskModal open={quickOpen} onClose={() => setQuickOpen(false)} initialProjectId={quickConfig.projectId} initialSectionId={quickConfig.sectionId} initialParentTaskId={quickConfig.parentTaskId} defaultDueAt={quickConfig.defaultDueAt} onCreated={setSelectedTask} />
        <TaskDetailDrawer task={selectedTask} onClose={() => setSelectedTask(null)} />
        <CreateProjectModal open={projectModal} onClose={() => setProjectModal(false)} />
        <CreateWorkspaceModal open={workspaceModal} onClose={() => setWorkspaceModal(false)} />
        <ToastRegion />
      </AppShell>
    )
  }

  return <NotFoundPage />
}

function Redirect({ to }: { to: string }) {
  useEffect(() => navigate(to, true), [to])
  return <LoadingScreen />
}

function DemoRedirect({ role }: { role: 'user' | 'admin' }) {
  const { enterDemo } = useApp()
  useEffect(() => {
    enterDemo(role)
    navigate(role === 'admin' ? '/admin/overview' : '/app/today', true)
  }, [enterDemo, role])
  return <LoadingScreen />
}

function LoadingScreen() {
  return <div className="loading-screen"><span className="loading-logo"><Icon name="check" size={26} /></span><div className="loading-pulse"><i /><i /><i /></div><p>Đang chuẩn bị không gian của bạn…</p></div>
}

function OnboardingPage({ onCreate }: { onCreate: () => void }) {
  return <div className="onboarding-page"><div className="onboarding-art"><span><Icon name="sparkles" size={35} /></span><i /><i /><i /></div><span className="eyebrow">CHÀO MỪNG ĐẾN SMARTLY</span><h1>Tạo không gian đầu tiên</h1><p>Workspace là nơi chứa dự án, công việc và thành viên. Bắt đầu với không gian cá nhân hoặc dành cho đội nhóm.</p><Button size="lg" icon="plus" onClick={onCreate}>Tạo workspace</Button><small>Chỉ mất khoảng 30 giây</small></div>
}

function ForbiddenPage() {
  return <div className="standalone-state"><EmptyState icon="lock" title="Bạn không có quyền truy cập" description="Trang này chỉ dành cho quản trị viên hệ thống." action={<Button onClick={() => navigate('/app/today')}>Về ứng dụng</Button>} /></div>
}

function NotFoundPage() {
  return <div className="standalone-state"><div className="not-found-code">404</div><EmptyState icon="search" title="Không tìm thấy trang" description="Đường dẫn này không tồn tại hoặc nội dung đã được di chuyển." action={<Button onClick={() => navigate('/')}>Về trang chủ</Button>} /></div>
}

function ToastRegion() {
  const { toasts, dismissToast } = useApp()
  return <div className="toast-region" aria-live="polite">{toasts.map((toast) => <div key={toast.id} className={`toast toast--${toast.tone}`}><span><Icon name={toast.tone === 'error' ? 'warning' : toast.tone === 'info' ? 'bell' : 'check'} size={16} /></span><p>{toast.message}</p><button onClick={() => dismissToast(toast.id)}><Icon name="close" size={14} /></button></div>)}</div>
}

export default function App() {
  return <AppProvider><AppRouter /></AppProvider>
}
