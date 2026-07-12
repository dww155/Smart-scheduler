import { useEffect, useMemo, useState, type MouseEvent, type ReactNode } from 'react'
import { useApp } from '../context/AppContext'
import { navigate } from '../lib/router'
import { cx } from '../lib/utils'
import type { Project } from '../types'
import { Avatar, Badge, Button, Icon, IconButton, type IconName } from './ui'

export function Link({ to, className, children, onClick }: { to: string; className?: string; children: ReactNode; onClick?: () => void }) {
  const click = (event: MouseEvent<HTMLAnchorElement>) => {
    if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return
    event.preventDefault()
    navigate(to)
    onClick?.()
  }
  return <a href={to} className={className} onClick={click}>{children}</a>
}

const userNav: Array<{ to: string; label: string; icon: IconName; badge?: string }> = [
  { to: '/app/today', label: 'Hôm nay', icon: 'sun' },
  { to: '/app/inbox', label: 'Hộp thư', icon: 'inbox' },
  { to: '/app/upcoming', label: 'Sắp tới', icon: 'calendar' },
  { to: '/app/all', label: 'Tất cả công việc', icon: 'check-circle' },
  { to: '/app/completed', label: 'Đã hoàn thành', icon: 'archive' },
]

export function Brand({ compact = false, light = false }: { compact?: boolean; light?: boolean }) {
  return <Link to="/" className={cx('brand', compact && 'brand--compact', light && 'brand--light')}><span className="brand__mark"><Icon name="check" size={18} /></span>{!compact && <span>Smartly</span>}</Link>
}

export function AppShell({ children, pathname, onQuickAdd }: { children: ReactNode; pathname: string; onQuickAdd: () => void }) {
  const { currentUser, isAdmin, workspaces, activeWorkspace, activeWorkspaceId, setActiveWorkspaceId, projects, tasks, logout, demoMode, preferences, setPreferences } = useApp()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [projectsOpen, setProjectsOpen] = useState(true)
  const [userMenu, setUserMenu] = useState(false)
  const [workspaceMenu, setWorkspaceMenu] = useState(false)
  const visibleProjects = projects.filter((project) => project.workspaceId === activeWorkspaceId)
  const todayCount = tasks.filter((task) => task.status !== 'COMPLETED' && task.status !== 'CANCELLED' && task.dueAt && new Date(task.dueAt).toDateString() === new Date().toDateString()).length
  const inboxCount = tasks.filter((task) => task.status !== 'COMPLETED' && !task.sectionId).length
  const taskCount = (project: Project) => tasks.filter((task) => task.projectId === project.id && task.status !== 'COMPLETED' && !task.parentTaskId).length

  useEffect(() => setSidebarOpen(false), [pathname])
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      const tag = (event.target as HTMLElement)?.tagName
      if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return
      if (event.key.toLowerCase() === 'q') onQuickAdd()
      if (event.key === '/' || ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k')) {
        event.preventDefault()
        navigate('/app/search')
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onQuickAdd])

  const isActive = (to: string) => pathname === to || (to.includes('/project/') && pathname.startsWith(to))
  return (
    <div className="app-layout">
      {sidebarOpen && <button className="mobile-overlay" onClick={() => setSidebarOpen(false)} aria-label="Đóng menu" />}
      <aside className={cx('app-sidebar', sidebarOpen && 'is-open')}>
        <div className="sidebar-top"><Brand /><IconButton icon="close" label="Đóng menu" className="mobile-only" onClick={() => setSidebarOpen(false)} /></div>
        <div className="workspace-switcher-wrap">
          <button className="workspace-switcher" onClick={() => setWorkspaceMenu((value) => !value)}>
            <span className="workspace-avatar" style={{ background: activeWorkspace?.color || '#6d5dfc' }}>{activeWorkspace?.name?.[0] || 'S'}</span>
            <span><strong>{activeWorkspace?.name || 'Workspace'}</strong><small>{activeWorkspace?.type === 'TEAM' ? 'Không gian nhóm' : 'Cá nhân'}</small></span>
            <Icon name="chevron-down" size={15} />
          </button>
          {workspaceMenu && (
            <div className="workspace-menu popover-card">
              <small>WORKSPACE CỦA BẠN</small>
              {workspaces.map((workspace) => <button key={workspace.id} className={cx(workspace.id === activeWorkspaceId && 'is-active')} onClick={() => { setActiveWorkspaceId(workspace.id); setWorkspaceMenu(false); navigate('/app/today') }}><span className="workspace-avatar" style={{ background: workspace.color }}>{workspace.name[0]}</span><span>{workspace.name}<small>{workspace.type === 'TEAM' ? 'Nhóm' : 'Cá nhân'}</small></span>{workspace.id === activeWorkspaceId && <Icon name="check" size={15} />}</button>)}
              <span className="menu-separator" />
              <Link to="/app/workspace?create=1" onClick={() => setWorkspaceMenu(false)}><Icon name="plus" size={15} />Tạo workspace mới</Link>
              <Link to="/app/workspace" onClick={() => setWorkspaceMenu(false)}><Icon name="settings" size={15} />Quản lý workspace</Link>
            </div>
          )}
        </div>
        <Button className="sidebar-quick-add" icon="plus" onClick={onQuickAdd}><span>Thêm công việc</span><kbd>Q</kbd></Button>
        <nav className="sidebar-nav" aria-label="Điều hướng chính">
          {userNav.map((item) => <Link key={item.to} to={item.to} className={cx('sidebar-link', isActive(item.to) && 'is-active')}><Icon name={item.icon} size={18} /><span>{item.label}</span>{item.to === '/app/today' && todayCount > 0 && <em>{todayCount}</em>}{item.to === '/app/inbox' && inboxCount > 0 && <em>{inboxCount}</em>}</Link>)}
          <Link to="/app/calendar" className={cx('sidebar-link', pathname === '/app/calendar' && 'is-active')}><Icon name="calendar" size={18} /><span>Lịch</span></Link>
          <Link to="/app/search" className={cx('sidebar-link', pathname === '/app/search' && 'is-active')}><Icon name="search" size={18} /><span>Tìm kiếm</span><kbd>/</kbd></Link>
        </nav>
        <div className="sidebar-section">
          <button className="sidebar-section__title" onClick={() => setProjectsOpen((value) => !value)}><span><Icon name={projectsOpen ? 'chevron-down' : 'chevron-right'} size={14} />DỰ ÁN</span><Icon name="plus" size={15} /></button>
          {projectsOpen && <div className="project-links">{visibleProjects.map((project) => <Link key={project.id} to={`/app/project/${project.id}`} className={cx('sidebar-link sidebar-project', pathname === `/app/project/${project.id}` && 'is-active')}><span className="project-symbol" style={{ color: project.color }}>{project.icon === 'rocket' ? '↗' : '#'}</span><span>{project.name}</span><em>{taskCount(project)}</em></Link>)}<Link to="/app/projects" className={cx('sidebar-link sidebar-link--muted', pathname === '/app/projects' && 'is-active')}><Icon name="grid" size={16} /><span>Xem tất cả dự án</span></Link></div>}
        </div>
        <div className="sidebar-section sidebar-section--utility">
          <Link to="/app/labels" className={cx('sidebar-link', pathname === '/app/labels' && 'is-active')}><Icon name="label" size={17} /><span>Nhãn</span></Link>
          <Link to="/app/workspace" className={cx('sidebar-link', pathname === '/app/workspace' && 'is-active')}><Icon name="users" size={17} /><span>Thành viên</span></Link>
        </div>
        <div className="sidebar-profile-wrap">
          {userMenu && <div className="profile-menu popover-card">
            <div className="profile-menu__user"><Avatar name={currentUser?.username} size="sm" /><span><strong>{currentUser?.username}</strong><small>{currentUser?.email || 'Tài khoản Smartly'}</small></span></div>
            {demoMode && <Badge tone="purple" dot>Chế độ demo</Badge>}
            <span className="menu-separator" />
            <Link to="/app/settings"><Icon name="settings" size={16} />Cài đặt cá nhân</Link>
            {isAdmin && <Link to="/admin/overview"><Icon name="admin" size={16} />Trang quản trị</Link>}
            <button onClick={() => setPreferences({ ...preferences, theme: preferences.theme === 'dark' ? 'light' : 'dark' })}><Icon name={preferences.theme === 'dark' ? 'sun' : 'moon'} size={16} />{preferences.theme === 'dark' ? 'Giao diện sáng' : 'Giao diện tối'}</button>
            <span className="menu-separator" />
            <button className="danger-text" onClick={() => { logout(); navigate('/login') }}><Icon name="logout" size={16} />Đăng xuất</button>
          </div>}
          <button className="sidebar-profile" onClick={() => setUserMenu((value) => !value)}><Avatar name={currentUser?.username} size="sm" /><span><strong>{currentUser?.username}</strong><small>{isAdmin ? 'Quản trị viên' : 'Thành viên'}</small></span><Icon name="more" size={17} /></button>
        </div>
      </aside>
      <main className="app-main">
        <header className="mobile-topbar"><IconButton icon="menu" label="Mở menu" onClick={() => setSidebarOpen(true)} /><Brand /><IconButton icon="plus" label="Thêm công việc" variant="soft" onClick={onQuickAdd} /></header>
        {children}
      </main>
      <nav className="mobile-bottom-nav">
        {[
          ['/app/today', 'sun', 'Hôm nay'], ['/app/upcoming', 'calendar', 'Sắp tới'], ['/app/projects', 'project', 'Dự án'], ['/app/search', 'search', 'Tìm kiếm'], ['/app/settings', 'menu', 'Thêm'],
        ].map(([to, icon, label]) => <Link key={to} to={to} className={cx(pathname === to && 'is-active')}><Icon name={icon as IconName} size={20} /><span>{label}</span></Link>)}
      </nav>
    </div>
  )
}

const adminNav: Array<{ to: string; label: string; icon: IconName }> = [
  { to: '/admin/overview', label: 'Tổng quan', icon: 'dashboard' },
  { to: '/admin/users', label: 'Người dùng', icon: 'users' },
  { to: '/admin/workspaces', label: 'Workspaces', icon: 'workspace' },
  { to: '/admin/content', label: 'Dữ liệu công việc', icon: 'task' },
  { to: '/admin/system', label: 'Hệ thống', icon: 'settings' },
]

export function AdminShell({ children, pathname }: { children: ReactNode; pathname: string }) {
  const { currentUser, demoMode, logout } = useApp()
  const [mobileOpen, setMobileOpen] = useState(false)
  const title = useMemo(() => adminNav.find((item) => pathname.startsWith(item.to))?.label || 'Quản trị', [pathname])
  return (
    <div className="admin-layout">
      {mobileOpen && <button className="mobile-overlay" onClick={() => setMobileOpen(false)} />}
      <aside className={cx('admin-sidebar', mobileOpen && 'is-open')}>
        <div className="admin-sidebar__brand"><Brand light /><Badge tone="purple">ADMIN</Badge><IconButton className="mobile-only" icon="close" label="Đóng" onClick={() => setMobileOpen(false)} /></div>
        <nav>{adminNav.map((item) => <Link key={item.to} to={item.to} className={cx(pathname === item.to && 'is-active')} onClick={() => setMobileOpen(false)}><Icon name={item.icon} size={18} /><span>{item.label}</span>{pathname === item.to && <Icon name="chevron-right" size={15} />}</Link>)}</nav>
        <div className="admin-sidebar__bottom">
          <Link to="/app/today"><Icon name="arrow-left" size={17} />Quay lại ứng dụng</Link>
          <button onClick={() => { logout(); navigate('/login') }}><Icon name="logout" size={17} />Đăng xuất</button>
          <div><Avatar name={currentUser?.username} size="sm" /><span><strong>{currentUser?.username}</strong><small>{demoMode ? 'Demo administrator' : currentUser?.email}</small></span></div>
        </div>
      </aside>
      <main className="admin-main">
        <header className="admin-topbar"><div><IconButton className="mobile-only" icon="menu" label="Mở menu" onClick={() => setMobileOpen(true)} /><div><span>ADMIN CONSOLE</span><h1>{title}</h1></div></div><div><button className="admin-search" onClick={() => pathname !== '/admin/users' && navigate('/admin/users')}><Icon name="search" size={16} />Tìm kiếm hệ thống <kbd>⌘ K</kbd></button><IconButton icon="bell" label="Thông báo" /><Avatar name={currentUser?.username} size="sm" /></div></header>
        {children}
      </main>
    </div>
  )
}

