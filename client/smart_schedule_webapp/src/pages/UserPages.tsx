import { useEffect, useState, type ReactNode } from 'react'
import { useApp } from '../context/AppContext'
import { navigate } from '../lib/router'
import {
  addDays,
  cx,
  endOfDay,
  formatDate,
  isOverdue,
  isToday,
  priorityLabel,
  startOfDay,
  statusLabel,
} from '../lib/utils'
import type { Project, ProjectView, Task, TaskPriority, TaskStatus } from '../types'
import { Link } from '../components/shells'
import { KanbanBoard, MonthCalendar, TaskGroup, TaskList, TaskRow } from '../components/tasks'
import { Avatar, Badge, Button, EmptyState, Icon, IconButton, Modal, Progress, TextInput, type IconName } from '../components/ui'

export function PageHeader({ eyebrow, title, description, icon, children, compact = false }: { eyebrow?: string; title: string; description?: string; icon?: IconName; children?: ReactNode; compact?: boolean }) {
  return (
    <header className={cx('page-header', compact && 'page-header--compact')}>
      <div>{eyebrow && <span className="eyebrow">{eyebrow}</span>}<h1>{icon && <span className="page-title-icon"><Icon name={icon} size={22} /></span>}{title}</h1>{description && <p>{description}</p>}</div>
      {children && <div className="page-header__actions">{children}</div>}
    </header>
  )
}

function useVisibleTasks() {
  const { tasks, projects, workspaces } = useApp()
  const workspaceIds = new Set(workspaces.map((item) => item.id))
  const projectIds = new Set(projects.filter((item) => workspaceIds.has(item.workspaceId)).map((item) => item.id))
  return tasks.filter((task) => projectIds.has(task.projectId) && !task.parentTaskId)
}

export function TodayPage({ onOpenTask, onQuickAdd }: { onOpenTask: (task: Task) => void; onQuickAdd: () => void }) {
  const { currentUser } = useApp()
  const visibleTasks = useVisibleTasks()
  const activeTasks = visibleTasks.filter((task) => !['COMPLETED', 'CANCELLED'].includes(task.status))
  const overdue = activeTasks.filter(isOverdue).sort(sortByDue)
  const today = activeTasks.filter((task) => isToday(task.dueAt)).sort(sortByPriority)
  const completedToday = visibleTasks.filter((task) => task.status === 'COMPLETED' && isToday(task.completedAt))
  const soon = activeTasks.filter((task) => task.dueAt && new Date(task.dueAt).getTime() > endOfDay().getTime() && new Date(task.dueAt).getTime() <= endOfDay(addDays(new Date(), 3)).getTime())
  const total = overdue.length + today.length + completedToday.length
  const progress = total ? Math.round(completedToday.length / total * 100) : 0
  const hour = new Date().getHours()
  const greeting = hour < 11 ? 'Chào buổi sáng' : hour < 18 ? 'Chào buổi chiều' : 'Chào buổi tối'
  return (
    <div className="page page--today">
      <PageHeader eyebrow={new Intl.DateTimeFormat('vi-VN', { weekday: 'long', day: '2-digit', month: 'long' }).format(new Date())} title={`${greeting}, ${currentUser?.username?.split(' ')[0] || 'bạn'}!`} description="Tập trung vào điều quan trọng nhất hôm nay.">
        <Button variant="secondary" icon="calendar" onClick={() => navigate('/app/calendar')}>Mở lịch</Button>
        <Button icon="plus" onClick={onQuickAdd}>Thêm công việc</Button>
      </PageHeader>
      <section className="daily-overview">
        <div className="daily-progress-card">
          <div className="progress-ring" style={{ '--progress': `${progress * 3.6}deg` } as React.CSSProperties}><span>{progress}%</span></div>
          <div><span>Tiến độ hôm nay</span><strong>{completedToday.length} trong {Math.max(total, today.length + overdue.length)} công việc đã xong</strong><Progress value={progress} /></div>
        </div>
        <div className="daily-mini-stat"><span className="stat-icon stat-icon--red"><Icon name="warning" size={18} /></span><div><strong>{overdue.length}</strong><small>Quá hạn</small></div></div>
        <div className="daily-mini-stat"><span className="stat-icon stat-icon--purple"><Icon name="sun" size={18} /></span><div><strong>{today.length}</strong><small>Hôm nay</small></div></div>
        <div className="daily-mini-stat"><span className="stat-icon stat-icon--green"><Icon name="check" size={18} /></span><div><strong>{completedToday.length}</strong><small>Đã xong</small></div></div>
      </section>
      <div className="task-page-content">
        <TaskGroup title="Quá hạn" tasks={overdue} onOpen={onOpenTask} tone="danger" />
        <TaskGroup title="Hôm nay" tasks={today} onOpen={onOpenTask} tone="today" />
        {completedToday.length > 0 && <TaskGroup title="Đã hoàn thành hôm nay" tasks={completedToday} onOpen={onOpenTask} initiallyOpen={false} />}
        {!overdue.length && !today.length && (
          <EmptyState icon="sparkles" title="Lịch hôm nay thật thoáng" description="Bạn đã xử lý hết công việc có hạn hôm nay. Thêm một việc mới hoặc lên kế hoạch cho những ngày tới." action={<Button icon="plus" onClick={onQuickAdd}>Thêm công việc</Button>} />
        )}
        {soon.length > 0 && (
          <section className="up-next-card">
            <header><div><span className="eyebrow">TIẾP THEO</span><h3>Sắp đến hạn</h3></div><Link to="/app/upcoming">Xem tất cả <Icon name="arrow-right" size={14} /></Link></header>
            <div>{soon.slice(0, 3).map((task) => <TaskRow key={task.id} task={task} onOpen={onOpenTask} />)}</div>
          </section>
        )}
      </div>
    </div>
  )
}

export function InboxPage({ onOpenTask, onQuickAdd }: { onOpenTask: (task: Task) => void; onQuickAdd: () => void }) {
  const tasks = useVisibleTasks().filter((task) => !task.sectionId && task.status !== 'COMPLETED' && task.status !== 'CANCELLED')
  return (
    <div className="page">
      <PageHeader title="Hộp thư" description="Nơi lưu nhanh mọi ý tưởng trước khi bạn sắp xếp chúng." icon="inbox"><Badge tone="purple">{tasks.length} chưa phân loại</Badge><Button icon="plus" onClick={onQuickAdd}>Thêm công việc</Button></PageHeader>
      <div className="content-card task-content-card">
        <div className="content-card__toolbar"><span><Icon name="inbox" size={17} />Công việc chưa phân loại</span><div><Button variant="ghost" size="sm" icon="filter">Lọc</Button><Button variant="ghost" size="sm" icon="more">Sắp xếp</Button></div></div>
        <TaskList tasks={tasks} onOpen={onOpenTask} emptyTitle="Hộp thư trống" emptyDescription="Ghi nhanh ý tưởng bằng Quick Add. Bạn có thể phân loại chúng sau." />
        <button className="inline-create-task" onClick={onQuickAdd}><Icon name="plus" size={17} />Thêm công việc vào hộp thư</button>
      </div>
    </div>
  )
}

export function UpcomingPage({ onOpenTask, onQuickAdd }: { onOpenTask: (task: Task) => void; onQuickAdd: () => void }) {
  const active = useVisibleTasks().filter((task) => task.dueAt && !['COMPLETED', 'CANCELLED'].includes(task.status) && new Date(task.dueAt).getTime() >= startOfDay().getTime()).sort(sortByDue)
  const [range, setRange] = useState<7 | 14 | 30>(14)
  const days = Array.from({ length: range }, (_, index) => addDays(new Date(), index))
  return (
    <div className="page">
      <PageHeader title="Sắp tới" description="Lập kế hoạch và cân bằng khối lượng công việc phía trước." icon="calendar"><div className="segmented"><button className={range === 7 ? 'is-active' : ''} onClick={() => setRange(7)}>7 ngày</button><button className={range === 14 ? 'is-active' : ''} onClick={() => setRange(14)}>14 ngày</button><button className={range === 30 ? 'is-active' : ''} onClick={() => setRange(30)}>30 ngày</button></div><Button icon="plus" onClick={onQuickAdd}>Lên lịch</Button></PageHeader>
      <div className="upcoming-timeline">
        {days.map((date, index) => {
          const dayTasks = active.filter((task) => new Date(task.dueAt!).toDateString() === date.toDateString())
          if (!dayTasks.length && index > 6) return null
          const isCurrent = date.toDateString() === new Date().toDateString()
          return (
            <section className={cx('timeline-day', isCurrent && 'is-today')} key={date.toISOString()}>
              <div className="timeline-date"><span>{new Intl.DateTimeFormat('vi-VN', { weekday: 'short' }).format(date)}</span><strong>{date.getDate()}</strong><small>{new Intl.DateTimeFormat('vi-VN', { month: 'short' }).format(date)}</small></div>
              <div className="timeline-content"><header><h3>{isCurrent ? 'Hôm nay' : index === 1 ? 'Ngày mai' : new Intl.DateTimeFormat('vi-VN', { weekday: 'long' }).format(date)}</h3><span>{dayTasks.length} công việc</span><button onClick={() => { const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16); window.dispatchEvent(new CustomEvent('smartly:quick-add', { detail: { defaultDueAt: local } })) }}><Icon name="plus" size={15} /></button></header>{dayTasks.length ? <TaskList tasks={dayTasks} onOpen={onOpenTask} /> : <p className="timeline-empty">Chưa có kế hoạch</p>}</div>
            </section>
          )
        })}
      </div>
    </div>
  )
}

export function AllTasksPage({ onOpenTask, onQuickAdd }: { onOpenTask: (task: Task) => void; onQuickAdd: () => void }) {
  const allTasks = useVisibleTasks()
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState<'ACTIVE' | TaskStatus | 'ALL'>('ACTIVE')
  const [priority, setPriority] = useState<'ALL' | TaskPriority>('ALL')
  const [sort, setSort] = useState<'due' | 'priority' | 'created'>('due')
  const filtered = allTasks
    .filter((task) => !query || `${task.title} ${task.description || ''}`.toLowerCase().includes(query.toLowerCase()))
    .filter((task) => status === 'ALL' || (status === 'ACTIVE' ? !['COMPLETED', 'CANCELLED'].includes(task.status) : task.status === status))
    .filter((task) => priority === 'ALL' || task.priority === priority)
    .sort(sort === 'priority' ? sortByPriority : sort === 'created' ? (a, b) => (b.createdAt || '').localeCompare(a.createdAt || '') : sortByDue)
  return (
    <div className="page">
      <PageHeader title="Tất cả công việc" description={`${allTasks.length} công việc trong các workspace của bạn.`} icon="check-circle"><Button icon="plus" onClick={onQuickAdd}>Thêm công việc</Button></PageHeader>
      <div className="filter-bar">
        <TextInput icon="search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Tìm trong công việc…" />
        <label><Icon name="activity" size={15} /><select value={status} onChange={(event) => setStatus(event.target.value as typeof status)}><option value="ACTIVE">Đang hoạt động</option><option value="ALL">Mọi trạng thái</option>{Object.entries(statusLabel).map(([value, text]) => <option key={value} value={value}>{text}</option>)}</select></label>
        <label><Icon name="flag" size={15} /><select value={priority} onChange={(event) => setPriority(event.target.value as typeof priority)}><option value="ALL">Mọi ưu tiên</option>{Object.entries(priorityLabel).map(([value, text]) => <option key={value} value={value}>{text}</option>)}</select></label>
        <label><Icon name="filter" size={15} /><select value={sort} onChange={(event) => setSort(event.target.value as typeof sort)}><option value="due">Hạn gần nhất</option><option value="priority">Ưu tiên cao</option><option value="created">Mới tạo</option></select></label>
      </div>
      <div className="content-card task-content-card"><div className="content-card__toolbar"><span>{filtered.length} kết quả</span><small>Cập nhật tự động</small></div><TaskList tasks={filtered} onOpen={onOpenTask} emptyTitle="Không tìm thấy công việc" emptyDescription="Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm." /></div>
    </div>
  )
}

export function CompletedPage({ onOpenTask }: { onOpenTask: (task: Task) => void }) {
  const tasks = useVisibleTasks().filter((task) => task.status === 'COMPLETED').sort((a, b) => (b.completedAt || '').localeCompare(a.completedAt || ''))
  const groups = new Map<string, Task[]>()
  tasks.forEach((task) => {
    const key = task.completedAt ? new Intl.DateTimeFormat('vi-VN', { month: 'long', year: 'numeric' }).format(new Date(task.completedAt)) : 'Không rõ thời gian'
    groups.set(key, [...(groups.get(key) || []), task])
  })
  return <div className="page"><PageHeader title="Đã hoàn thành" description="Nhìn lại tiến độ và những việc bạn đã hoàn tất." icon="archive"><Badge tone="green" dot>{tasks.length} công việc</Badge></PageHeader><div className="task-page-content">{Array.from(groups.entries()).map(([key, items]) => <TaskGroup key={key} title={key} tasks={items} onOpen={onOpenTask} />)}{!tasks.length && <EmptyState icon="archive" title="Chưa có công việc hoàn thành" description="Các công việc bạn hoàn thành sẽ xuất hiện tại đây." />}</div></div>
}

export function CalendarPage({ onOpenTask, onQuickAdd }: { onOpenTask: (task: Task) => void; onQuickAdd: () => void }) {
  const tasks = useVisibleTasks().filter((task) => task.dueAt)
  return <div className="page page--wide"><PageHeader title="Lịch công việc" description="Xem tổng thể deadline từ mọi dự án." icon="calendar"><Button variant="secondary" icon="filter">Bộ lọc</Button><Button icon="plus" onClick={onQuickAdd}>Thêm lịch</Button></PageHeader><MonthCalendar tasks={tasks} onOpen={onOpenTask} /></div>
}

export function ProjectsPage({ onCreate }: { onCreate: () => void }) {
  const { projects, tasks, activeWorkspace, activeWorkspaceId } = useApp()
  const visible = projects.filter((item) => item.workspaceId === activeWorkspaceId)
  return (
    <div className="page">
      <PageHeader title="Dự án" description={`Tổ chức công việc trong ${activeWorkspace?.name || 'workspace'}.`} icon="project"><div className="segmented"><button className="is-active"><Icon name="grid" size={15} /></button><button><Icon name="list" size={15} /></button></div><Button icon="plus" onClick={onCreate}>Dự án mới</Button></PageHeader>
      <div className="project-grid">
        {visible.map((project) => {
          const projectTasks = tasks.filter((task) => task.projectId === project.id && !task.parentTaskId)
          const completed = projectTasks.filter((task) => task.status === 'COMPLETED').length
          const progress = projectTasks.length ? Math.round(completed / projectTasks.length * 100) : 0
          const overdue = projectTasks.filter(isOverdue).length
          return <Link to={`/app/project/${project.id}`} className="project-card" key={project.id}><header><span className="project-card__icon" style={{ background: `${project.color}18`, color: project.color }}><Icon name={project.icon === 'home' ? 'home' : project.icon === 'globe' ? 'workspace' : 'project'} size={21} /></span><Icon name="more" size={18} /></header><h3>{project.name}</h3><p>{project.description || 'Chưa có mô tả cho dự án này.'}</p><div className="project-card__meta"><span><Icon name="task" size={14} />{projectTasks.length} công việc</span>{overdue > 0 && <span className="is-overdue"><Icon name="warning" size={14} />{overdue} quá hạn</span>}</div><footer><div><span>Tiến độ</span><strong>{progress}%</strong></div><Progress value={progress} color={project.color} /><small>Cập nhật {project.updatedAt ? formatDate(project.updatedAt) : 'gần đây'}</small></footer></Link>
        })}
        <button className="project-card project-card--new" onClick={onCreate}><span><Icon name="plus" size={24} /></span><strong>Tạo dự án mới</strong><small>List, board hoặc calendar</small></button>
      </div>
      {!visible.length && <EmptyState icon="project" title="Bắt đầu với dự án đầu tiên" description="Dự án giúp gom các công việc cùng mục tiêu và theo dõi tiến độ." action={<Button icon="plus" onClick={onCreate}>Tạo dự án</Button>} />}
    </div>
  )
}

export function ProjectPage({ projectId, onOpenTask, onQuickAdd }: { projectId: string; onOpenTask: (task: Task) => void; onQuickAdd: () => void }) {
  const { projects, tasks, sections, updateProject, deleteProject, notify } = useApp()
  const project = projects.find((item) => item.id === projectId)
  const [view, setView] = useState<ProjectView>(project?.viewType || 'LIST')
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState<'ALL' | TaskStatus>('ALL')
  const [settingsOpen, setSettingsOpen] = useState(false)
  const [editProject, setEditProject] = useState<Project | null>(project || null)
  useEffect(() => { setView(project?.viewType || 'LIST'); setEditProject(project || null) }, [projectId, project])
  if (!project) return <div className="page"><EmptyState icon="project" title="Không tìm thấy dự án" description="Dự án có thể đã bị xóa hoặc bạn không còn quyền truy cập." action={<Button onClick={() => navigate('/app/projects')}>Về danh sách dự án</Button>} /></div>
  const projectTasks = tasks.filter((task) => task.projectId === project.id && !task.parentTaskId).filter((task) => !query || `${task.title} ${task.description}`.toLowerCase().includes(query.toLowerCase())).filter((task) => status === 'ALL' || task.status === status)
  const projectSections = sections.filter((section) => section.projectId === project.id).sort((a, b) => a.sortOrder - b.sortOrder)
  return (
    <div className="page page--wide project-page">
      <PageHeader eyebrow="DỰ ÁN" title={project.name} description={project.description || 'Quản lý mọi công việc của dự án.'}><div className="avatar-stack"><Avatar name="Linh" size="sm" /><Avatar name="Minh" size="sm" /><Avatar name="An" size="sm" /><span>+2</span></div><IconButton icon="more" label="Cài đặt dự án" variant="secondary" onClick={() => setSettingsOpen(true)} /><Button icon="plus" onClick={onQuickAdd}>Thêm công việc</Button></PageHeader>
      <div className="project-toolbar">
        <div className="view-tabs"><button className={view === 'LIST' ? 'is-active' : ''} onClick={() => setView('LIST')}><Icon name="list" size={16} />Danh sách</button><button className={view === 'BOARD' ? 'is-active' : ''} onClick={() => setView('BOARD')}><Icon name="board" size={16} />Bảng</button><button className={view === 'CALENDAR' ? 'is-active' : ''} onClick={() => setView('CALENDAR')}><Icon name="calendar" size={16} />Lịch</button></div>
        <div><TextInput icon="search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Tìm trong dự án" /><label className="toolbar-select"><Icon name="filter" size={15} /><select value={status} onChange={(event) => setStatus(event.target.value as typeof status)}><option value="ALL">Mọi trạng thái</option>{Object.entries(statusLabel).map(([value, text]) => <option key={value} value={value}>{text}</option>)}</select></label></div>
      </div>
      {view === 'LIST' && <div className="project-list-view">{projectSections.map((section) => <TaskGroup key={section.id} title={section.name} tasks={projectTasks.filter((task) => task.sectionId === section.id)} onOpen={onOpenTask} showProject={false} />)}<TaskGroup title="Chưa phân loại" tasks={projectTasks.filter((task) => !task.sectionId)} onOpen={onOpenTask} showProject={false} />{!projectTasks.length && <EmptyState icon="task" title="Dự án chưa có công việc" description="Thêm công việc đầu tiên để bắt đầu theo dõi tiến độ." action={<Button icon="plus" onClick={onQuickAdd}>Thêm công việc</Button>} />}</div>}
      {view === 'BOARD' && <KanbanBoard project={project} projectTasks={projectTasks} projectSections={projectSections} onOpen={onOpenTask} />}
      {view === 'CALENDAR' && <MonthCalendar tasks={projectTasks.filter((task) => task.dueAt)} onOpen={onOpenTask} />}
      {editProject && <Modal open={settingsOpen} onClose={() => setSettingsOpen(false)} title="Cài đặt dự án" width="md" footer={<><Button variant="danger" icon="trash" onClick={async () => { if (!window.confirm(`Xóa dự án “${project.name}”?`)) return; await deleteProject(project); setSettingsOpen(false); navigate('/app/projects') }}>Xóa dự án</Button><span className="footer-spacer" /><Button variant="ghost" onClick={() => setSettingsOpen(false)}>Hủy</Button><Button onClick={async () => { try { await updateProject(editProject); setSettingsOpen(false) } catch (error) { notify(error instanceof Error ? error.message : 'Không thể cập nhật', 'error') } }}>Lưu thay đổi</Button></>}><div className="form-stack"><label><span>Tên dự án</span><input value={editProject.name} onChange={(event) => setEditProject({ ...editProject, name: event.target.value })} /></label><label><span>Mô tả</span><textarea rows={4} value={editProject.description || ''} onChange={(event) => setEditProject({ ...editProject, description: event.target.value })} /></label><div className="form-grid"><label><span>Màu</span><input type="color" value={editProject.color || '#6d5dfc'} onChange={(event) => setEditProject({ ...editProject, color: event.target.value })} /></label><label><span>View mặc định</span><select value={editProject.viewType} onChange={(event) => setEditProject({ ...editProject, viewType: event.target.value as ProjectView })}><option value="LIST">Danh sách</option><option value="BOARD">Bảng</option><option value="CALENDAR">Lịch</option></select></label></div></div></Modal>}
    </div>
  )
}

function sortByDue(a: Task, b: Task) {
  if (!a.dueAt) return 1
  if (!b.dueAt) return -1
  return new Date(a.dueAt).getTime() - new Date(b.dueAt).getTime()
}

const priorityWeight: Record<TaskPriority, number> = { URGENT: 5, HIGH: 4, MEDIUM: 3, LOW: 2, NONE: 1 }
function sortByPriority(a: Task, b: Task) { return priorityWeight[b.priority] - priorityWeight[a.priority] || sortByDue(a, b) }
