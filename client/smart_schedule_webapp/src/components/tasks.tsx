import { useEffect, useState, type DragEvent, type FormEvent } from 'react'
import { useApp } from '../context/AppContext'
import { api, getAccessToken } from '../lib/api'
import {
  cx,
  fileSize,
  formatDate,
  formatDateTime,
  isOverdue,
  priorityLabel,
  relativeDate,
  statusLabel,
  toApiDate,
  toLocalInput,
} from '../lib/utils'
import type {
  ChecklistItem,
  Project,
  Task,
  TaskAttachment,
  TaskComment,
  TaskPriority,
  TaskReminder,
  TaskSection,
  TaskStatus,
} from '../types'
import { Avatar, Button, EmptyState, Icon, IconButton, Modal, Progress } from './ui'

const priorityTone: Record<TaskPriority, string> = {
  NONE: 'var(--text-tertiary)',
  LOW: 'var(--blue)',
  MEDIUM: 'var(--amber)',
  HIGH: 'var(--orange)',
  URGENT: 'var(--red)',
}

export function TaskCheckbox({ task, compact = false }: { task: Task; compact?: boolean }) {
  const { setTaskStatus, notify } = useApp()
  const completed = task.status === 'COMPLETED'
  const toggle = async (event: React.MouseEvent) => {
    event.stopPropagation()
    try {
      await setTaskStatus(task, completed ? 'TODO' : 'COMPLETED')
    } catch (error) {
      notify(error instanceof Error ? error.message : 'Không thể cập nhật công việc', 'error')
    }
  }
  return (
    <button
      className={cx('task-checkbox', completed && 'is-completed', compact && 'is-compact')}
      style={{ '--priority-color': priorityTone[task.priority] } as React.CSSProperties}
      onClick={toggle}
      aria-label={completed ? 'Đánh dấu chưa hoàn thành' : 'Đánh dấu hoàn thành'}
    >
      {completed && <Icon name="check" size={13} />}
    </button>
  )
}

export function TaskRow({ task, onOpen, showProject = true }: { task: Task; onOpen: (task: Task) => void; showProject?: boolean }) {
  const { projects, labels, users, currentUser } = useApp()
  const project = projects.find((item) => item.id === task.projectId)
  const taskLabels = labels.filter((label) => task.labelIds.includes(label.id))
  const assignee = users.find((user) => user.id === task.assigneeId) || (task.assigneeId === currentUser?.id ? currentUser : undefined)
  return (
    <article className={cx('task-row', task.status === 'COMPLETED' && 'is-completed')} onClick={() => onOpen(task)} tabIndex={0} onKeyDown={(event) => event.key === 'Enter' && onOpen(task)}>
      <TaskCheckbox task={task} />
      <div className="task-row__body">
        <div className="task-row__title-line">
          <span className="task-row__title">{task.title}</span>
          {task.priority !== 'NONE' && <Icon name="flag" size={14} className="priority-icon" />}
        </div>
        {task.description && <p className="task-row__description">{task.description}</p>}
        <div className="task-row__meta">
          {task.dueAt && (
            <span className={cx('task-meta', isOverdue(task) && 'is-overdue')}>
              <Icon name="calendar" size={13} />{relativeDate(task.dueAt)}
            </span>
          )}
          {showProject && project && <span className="task-meta"><span className="color-dot" style={{ background: project.color }} />{project.name}</span>}
          {taskLabels.slice(0, 3).map((label) => <span className="label-chip" key={label.id} style={{ '--label-color': label.color || '#6d5dfc' } as React.CSSProperties}>{label.name}</span>)}
          {task.parentTaskId && <span className="task-meta"><Icon name="arrow-right" size={12} />Công việc con</span>}
        </div>
      </div>
      <div className="task-row__right">
        {assignee && <Avatar name={assignee.username} size="xs" />}
        <IconButton icon="chevron-right" label="Mở chi tiết" />
      </div>
    </article>
  )
}

export function TaskList({ tasks, onOpen, emptyTitle = 'Không có công việc', emptyDescription = 'Mọi thứ đã được xử lý. Hãy tận hưởng khoảng trống này!', showProject = true }: { tasks: Task[]; onOpen: (task: Task) => void; emptyTitle?: string; emptyDescription?: string; showProject?: boolean }) {
  if (!tasks.length) return <EmptyState icon="check-circle" title={emptyTitle} description={emptyDescription} />
  return <div className="task-list">{tasks.map((task) => <TaskRow key={task.id} task={task} onOpen={onOpen} showProject={showProject} />)}</div>
}

export function TaskGroup({ title, count, tasks, onOpen, tone, initiallyOpen = true, showProject = true }: { title: string; count?: number; tasks: Task[]; onOpen: (task: Task) => void; tone?: 'danger' | 'today' | 'neutral'; initiallyOpen?: boolean; showProject?: boolean }) {
  const [open, setOpen] = useState(initiallyOpen)
  if (!tasks.length) return null
  return (
    <section className={cx('task-group', tone && `task-group--${tone}`)}>
      <button className="task-group__header" onClick={() => setOpen((value) => !value)}>
        <span><Icon name={open ? 'chevron-down' : 'chevron-right'} size={15} />{title}<span className="task-group__count">{count ?? tasks.length}</span></span>
        <span className="task-group__line" />
      </button>
      {open && <TaskList tasks={tasks} onOpen={onOpen} showProject={showProject} />}
    </section>
  )
}

export function KanbanBoard({ project, projectTasks, projectSections, onOpen }: { project: Project; projectTasks: Task[]; projectSections: TaskSection[]; onOpen: (task: Task) => void }) {
  const { updateTask, notify, createSection } = useApp()
  const [draggedId, setDraggedId] = useState<string | null>(null)
  const [adding, setAdding] = useState(false)
  const [sectionName, setSectionName] = useState('')
  const columns = projectSections.length ? projectSections : [{ id: 'unsectioned', projectId: project.id, name: 'Chưa phân loại', sortOrder: 0 }]

  const onDrop = async (event: DragEvent, sectionId: string) => {
    event.preventDefault()
    const id = event.dataTransfer.getData('text/task-id') || draggedId
    const task = projectTasks.find((item) => item.id === id)
    if (!task) return
    try {
      await updateTask({ ...task, sectionId: sectionId === 'unsectioned' ? null : sectionId })
    } catch (error) {
      notify(error instanceof Error ? error.message : 'Không thể di chuyển công việc', 'error')
    } finally {
      setDraggedId(null)
    }
  }

  return (
    <div className="kanban-wrap">
      <div className="kanban-board">
        {columns.map((section, index) => {
          const sectionTasks = projectTasks.filter((task) => (task.sectionId || 'unsectioned') === section.id)
          return (
            <section className="kanban-column" key={section.id} onDragOver={(event) => event.preventDefault()} onDrop={(event) => onDrop(event, section.id)}>
              <header className="kanban-column__header">
                <div><span className="kanban-dot" style={{ background: [project.color, '#f2aa4c', '#8b5cf6', '#1fa67a'][index % 4] }} />{section.name}<span>{sectionTasks.length}</span></div>
                <IconButton icon="more" label="Tùy chọn cột" />
              </header>
              <div className="kanban-column__body">
                {sectionTasks.map((task) => (
                  <article
                    className={cx('task-card', draggedId === task.id && 'is-dragging')}
                    key={task.id}
                    draggable
                    onDragStart={(event) => { setDraggedId(task.id); event.dataTransfer.setData('text/task-id', task.id); event.dataTransfer.effectAllowed = 'move' }}
                    onDragEnd={() => setDraggedId(null)}
                    onClick={() => onOpen(task)}
                  >
                    <div className="task-card__top"><TaskCheckbox task={task} compact />{task.priority !== 'NONE' && <Icon name="flag" size={14} className={`priority--${task.priority.toLowerCase()}`} />}</div>
                    <h4>{task.title}</h4>
                    {task.description && <p>{task.description}</p>}
                    <div className="task-card__labels">{task.labelIds.slice(0, 2).map((id) => <TaskLabel key={id} id={id} />)}</div>
                    <footer>{task.dueAt ? <span className={cx(isOverdue(task) && 'is-overdue')}><Icon name="calendar" size={13} />{relativeDate(task.dueAt)}</span> : <span />}{task.assigneeId && <TaskAssignee id={task.assigneeId} />}</footer>
                  </article>
                ))}
                {!sectionTasks.length && <div className="kanban-empty">Thả công việc vào đây</div>}
                <Button variant="ghost" size="sm" icon="plus" onClick={() => window.dispatchEvent(new CustomEvent('smartly:quick-add', { detail: { projectId: project.id, sectionId: section.id === 'unsectioned' ? null : section.id } }))}>Thêm công việc</Button>
              </div>
            </section>
          )
        })}
        <section className="kanban-add-column">
          {adding ? (
            <form onSubmit={async (event) => { event.preventDefault(); if (!sectionName.trim()) return; await createSection(project.id, sectionName.trim()); setSectionName(''); setAdding(false) }}>
              <input value={sectionName} onChange={(event) => setSectionName(event.target.value)} placeholder="Tên section" autoFocus />
              <div><Button size="sm" type="submit">Thêm</Button><Button size="sm" variant="ghost" type="button" onClick={() => setAdding(false)}>Hủy</Button></div>
            </form>
          ) : <Button variant="soft" icon="plus" onClick={() => setAdding(true)}>Thêm section</Button>}
        </section>
      </div>
    </div>
  )
}

function TaskLabel({ id }: { id: string }) {
  const { labels } = useApp()
  const label = labels.find((item) => item.id === id)
  return label ? <span className="label-chip" style={{ '--label-color': label.color || '#6d5dfc' } as React.CSSProperties}>{label.name}</span> : null
}

function TaskAssignee({ id }: { id: string }) {
  const { users, members, currentUser } = useApp()
  const user = users.find((item) => item.id === id) || (currentUser?.id === id ? currentUser : undefined)
  const member = members.find((item) => item.userId === id)
  return <Avatar name={user?.username || member?.username || 'User'} size="xs" />
}

export function MonthCalendar({ tasks, onOpen, month = new Date() }: { tasks: Task[]; onOpen: (task: Task) => void; month?: Date }) {
  const [cursor, setCursor] = useState(new Date(month.getFullYear(), month.getMonth(), 1))
  const year = cursor.getFullYear()
  const monthIndex = cursor.getMonth()
  const firstDay = new Date(year, monthIndex, 1)
  const offset = (firstDay.getDay() + 6) % 7
  const gridStart = new Date(year, monthIndex, 1 - offset)
  const days = Array.from({ length: 42 }, (_, index) => {
    const date = new Date(gridStart)
    date.setDate(gridStart.getDate() + index)
    return date
  })
  const title = new Intl.DateTimeFormat('vi-VN', { month: 'long', year: 'numeric' }).format(cursor)
  const sameDate = (value: string | null | undefined, date: Date) => value ? new Date(value).toDateString() === date.toDateString() : false
  return (
    <div className="calendar-view">
      <header className="calendar-toolbar">
        <div><IconButton icon="chevron-left" label="Tháng trước" onClick={() => setCursor(new Date(year, monthIndex - 1, 1))} /><Button variant="secondary" size="sm" onClick={() => setCursor(new Date(new Date().getFullYear(), new Date().getMonth(), 1))}>Hôm nay</Button><IconButton icon="chevron-right" label="Tháng sau" onClick={() => setCursor(new Date(year, monthIndex + 1, 1))} /></div>
        <h3>{title}</h3>
        <div className="calendar-legend"><span><i className="dot dot--purple" />Công việc</span><span><i className="dot dot--red" />Quá hạn</span></div>
      </header>
      <div className="calendar-grid calendar-grid--heading">{['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'].map((day) => <span key={day}>{day}</span>)}</div>
      <div className="calendar-grid calendar-grid--days">
        {days.map((date) => {
          const dayTasks = tasks.filter((task) => sameDate(task.dueAt, date))
          const outside = date.getMonth() !== monthIndex
          const today = date.toDateString() === new Date().toDateString()
          return (
            <div className={cx('calendar-day', outside && 'is-outside', today && 'is-today')} key={date.toISOString()}>
              <span className="calendar-day__number">{date.getDate()}</span>
              <div className="calendar-day__tasks">
                {dayTasks.slice(0, 3).map((task) => <button key={task.id} className={cx('calendar-task', isOverdue(task) && 'is-overdue', task.status === 'COMPLETED' && 'is-completed')} onClick={() => onOpen(task)}><span style={{ background: priorityTone[task.priority] }} />{task.title}</button>)}
                {dayTasks.length > 3 && <button className="calendar-more">+{dayTasks.length - 3} công việc</button>}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

interface QuickTaskModalProps {
  open: boolean
  onClose: () => void
  initialProjectId?: string
  initialSectionId?: string | null
  initialParentTaskId?: string | null
  defaultDueAt?: string
  onCreated?: (task: Task) => void
}

export function QuickTaskModal({ open, onClose, initialProjectId, initialSectionId, initialParentTaskId, defaultDueAt, onCreated }: QuickTaskModalProps) {
  const { projects, labels, activeWorkspaceId, createTask, loading, notify } = useApp()
  const workspaceProjects = projects.filter((project) => project.workspaceId === activeWorkspaceId)
  const defaultProjectId = initialProjectId || workspaceProjects[0]?.id || projects[0]?.id || ''
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [projectId, setProjectId] = useState(initialProjectId || workspaceProjects[0]?.id || projects[0]?.id || '')
  const [dueAt, setDueAt] = useState(defaultDueAt || '')
  const [priority, setPriority] = useState<TaskPriority>('NONE')
  const [labelIds, setLabelIds] = useState<string[]>([])
  const [advanced, setAdvanced] = useState(false)
  const project = projects.find((item) => item.id === projectId)
  const availableLabels = labels.filter((label) => label.workspaceId === project?.workspaceId)

  useEffect(() => {
    if (!open) return
    setProjectId(defaultProjectId)
    setDueAt(defaultDueAt || '')
  }, [open, defaultProjectId, defaultDueAt])

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (!title.trim() || !projectId) return
    try {
      const created = await createTask({
        projectId,
        sectionId: initialProjectId === projectId ? initialSectionId || null : null,
        parentTaskId: initialParentTaskId || null,
        assigneeId: null,
        title: title.trim(),
        description: description.trim(),
        priority,
        startAt: null,
        dueAt: toApiDate(dueAt),
        allDay: !dueAt.includes('T'),
        recurrenceRule: null,
        recurrenceMode: 'NONE',
        timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
        sortOrder: 0,
        labelIds,
      })
      setTitle('')
      setDescription('')
      setDueAt('')
      setPriority('NONE')
      setLabelIds([])
      setAdvanced(false)
      onCreated?.(created)
      onClose()
    } catch (error) {
      notify(error instanceof Error ? error.message : 'Không thể tạo công việc', 'error')
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Thêm công việc" eyebrow="Quick add" width="lg">
      <form className="quick-task-form" onSubmit={submit}>
        <input className="quick-task-title" value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Bạn cần làm gì?" maxLength={500} autoFocus />
        <textarea value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Thêm mô tả hoặc ghi chú…" rows={advanced ? 4 : 2} />
        <div className="quick-task-chips">
          <label className="select-chip"><Icon name="project" size={15} /><select value={projectId} onChange={(event) => setProjectId(event.target.value)}>{projects.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}</select><Icon name="chevron-down" size={13} /></label>
          <label className="select-chip"><Icon name="calendar" size={15} /><input type="datetime-local" value={dueAt} onChange={(event) => setDueAt(event.target.value)} /><span>{dueAt ? formatDate(toApiDate(dueAt)) : 'Ngày đến hạn'}</span></label>
          <label className="select-chip"><Icon name="flag" size={15} /><select value={priority} onChange={(event) => setPriority(event.target.value as TaskPriority)}>{Object.entries(priorityLabel).map(([value, text]) => <option key={value} value={value}>{text}</option>)}</select><span>{priorityLabel[priority]}</span></label>
          <Button type="button" size="sm" variant="ghost" icon={advanced ? 'chevron-down' : 'more'} onClick={() => setAdvanced((value) => !value)}>{advanced ? 'Thu gọn' : 'Thêm tùy chọn'}</Button>
        </div>
        {advanced && (
          <div className="quick-task-advanced">
            <span className="form-label">Nhãn</span>
            <div className="label-picker">{availableLabels.length ? availableLabels.map((label) => <button key={label.id} type="button" className={cx('label-option', labelIds.includes(label.id) && 'is-selected')} onClick={() => setLabelIds((items) => items.includes(label.id) ? items.filter((id) => id !== label.id) : [...items, label.id])}><span style={{ background: label.color }} />{label.name}{labelIds.includes(label.id) && <Icon name="check" size={13} />}</button>) : <span className="muted">Workspace này chưa có nhãn.</span>}</div>
          </div>
        )}
        <footer className="quick-task-footer">
          <span className="keyboard-hint"><kbd>Esc</kbd> đóng · <kbd>Enter</kbd> tạo</span>
          <div><Button type="button" variant="ghost" onClick={onClose}>Hủy</Button><Button type="submit" icon="plus" disabled={!title.trim() || !projectId} loading={loading}>Tạo công việc</Button></div>
        </footer>
      </form>
    </Modal>
  )
}

interface TaskDetailProps { task: Task | null; onClose: () => void }

export function TaskDetailDrawer({ task, onClose }: TaskDetailProps) {
  const app = useApp()
  const { projects, sections, labels, users, members, currentUser, tasks, updateTask, setTaskStatus, deleteTask, loadTaskExtras, notify } = app
  const liveTask = tasks.find((item) => item.id === task?.id) || task
  const [draft, setDraft] = useState<Task | null>(liveTask)
  const [tab, setTab] = useState<'details' | 'activity'>('details')
  const [loadingExtras, setLoadingExtras] = useState(false)
  const [checklist, setChecklist] = useState<ChecklistItem[]>([])
  const [comments, setComments] = useState<TaskComment[]>([])
  const [reminders, setReminders] = useState<TaskReminder[]>([])
  const [attachments, setAttachments] = useState<TaskAttachment[]>([])
  const [newCheck, setNewCheck] = useState('')
  const [newComment, setNewComment] = useState('')
  const [newReminder, setNewReminder] = useState('')
  const [showDelete, setShowDelete] = useState(false)

  useEffect(() => {
    setDraft(liveTask ? { ...liveTask, labelIds: [...liveTask.labelIds] } : null)
  }, [liveTask])

  useEffect(() => {
    if (!task) return
    setLoadingExtras(true)
    loadTaskExtras(task.id)
      .then((result) => { setChecklist(result.checklist); setComments(result.comments); setReminders(result.reminders); setAttachments(result.attachments) })
      .catch(() => notify('Không thể tải toàn bộ nội dung chi tiết', 'error'))
      .finally(() => setLoadingExtras(false))
  }, [task, loadTaskExtras, notify])

  useEffect(() => {
    if (!task) return
    const onKey = (event: KeyboardEvent) => event.key === 'Escape' && onClose()
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [task, onClose])

  if (!draft || !task) return null
  const project = projects.find((item) => item.id === draft.projectId)
  const projectSections = sections.filter((item) => item.projectId === draft.projectId)
  const workspaceLabels = labels.filter((item) => item.workspaceId === project?.workspaceId)
  const workspaceMembers = members.filter((item) => item.workspaceId === project?.workspaceId)
  const subtasks = tasks.filter((item) => item.parentTaskId === draft.id)
  const checked = checklist.filter((item) => item.completed).length
  const dirty = JSON.stringify(draft) !== JSON.stringify(liveTask)

  const save = async () => {
    try { setDraft(await updateTask(draft)) } catch (error) { notify(error instanceof Error ? error.message : 'Không thể lưu công việc', 'error') }
  }

  const remove = async () => {
    try { await deleteTask(draft); onClose() } catch (error) { notify(error instanceof Error ? error.message : 'Không thể xóa công việc', 'error') }
  }

  const download = async (attachment: TaskAttachment) => {
    if (app.demoMode) { notify('File demo không có nội dung tải xuống', 'info'); return }
    try {
      const response = await fetch(api.attachmentUrl(attachment.taskId, attachment.id), { headers: { Authorization: `Bearer ${getAccessToken()}` } })
      if (!response.ok) throw new Error('Không thể tải file')
      const blob = await response.blob()
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = attachment.originalFileName
      anchor.click()
      URL.revokeObjectURL(url)
    } catch (error) { notify(error instanceof Error ? error.message : 'Không thể tải file', 'error') }
  }

  return (
    <div className="drawer-layer">
      <button className="drawer-backdrop" onClick={onClose} aria-label="Đóng chi tiết" />
      <aside className="task-drawer" role="dialog" aria-modal="true" aria-label={`Chi tiết ${draft.title}`}>
        <header className="task-drawer__header">
          <div className="task-breadcrumb"><span className="color-dot" style={{ background: project?.color }} />{project?.name || 'Dự án'}<Icon name="chevron-right" size={13} />Công việc</div>
          <div><IconButton icon="more" label="Tùy chọn" /><IconButton icon="close" label="Đóng" onClick={onClose} /></div>
        </header>
        <div className="task-drawer__tabs"><button className={cx(tab === 'details' && 'is-active')} onClick={() => setTab('details')}>Chi tiết</button><button className={cx(tab === 'activity' && 'is-active')} onClick={() => setTab('activity')}>Hoạt động <span>{comments.length}</span></button></div>
        {tab === 'details' ? (
          <div className="task-drawer__content">
            <div className="task-title-editor">
              <TaskCheckbox task={draft} />
              <textarea value={draft.title} rows={2} maxLength={500} onChange={(event) => setDraft({ ...draft, title: event.target.value })} />
            </div>
            <textarea className="task-description-editor" value={draft.description || ''} rows={4} placeholder="Thêm mô tả chi tiết…" onChange={(event) => setDraft({ ...draft, description: event.target.value })} />
            <div className="task-properties">
              <label><span><Icon name="activity" size={16} />Trạng thái</span><select value={draft.status} onChange={(event) => setTaskStatus(draft, event.target.value as TaskStatus)}>{Object.entries(statusLabel).map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></label>
              <label><span><Icon name="calendar" size={16} />Ngày đến hạn</span><input type="datetime-local" value={toLocalInput(draft.dueAt)} onChange={(event) => setDraft({ ...draft, dueAt: toApiDate(event.target.value) })} /></label>
              <label><span><Icon name="flag" size={16} />Ưu tiên</span><select value={draft.priority} onChange={(event) => setDraft({ ...draft, priority: event.target.value as TaskPriority })}>{Object.entries(priorityLabel).map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></label>
              <label><span><Icon name="board" size={16} />Section</span><select value={draft.sectionId || ''} onChange={(event) => setDraft({ ...draft, sectionId: event.target.value || null })}><option value="">Chưa phân loại</option>{projectSections.map((section) => <option key={section.id} value={section.id}>{section.name}</option>)}</select></label>
              <label><span><Icon name="user" size={16} />Người phụ trách</span><select value={draft.assigneeId || ''} onChange={(event) => setDraft({ ...draft, assigneeId: event.target.value || null })}><option value="">Chưa giao</option>{workspaceMembers.map((member) => <option key={member.id} value={member.userId}>{member.username || users.find((u) => u.id === member.userId)?.username || member.userId.slice(0, 8)}</option>)}</select></label>
              <label><span><Icon name="repeat" size={16} />Lặp lại</span><select value={draft.recurrenceMode} onChange={(event) => setDraft({ ...draft, recurrenceMode: event.target.value as Task['recurrenceMode'], recurrenceRule: event.target.value === 'NONE' ? null : draft.recurrenceRule || 'FREQ=WEEKLY' })}><option value="NONE">Không lặp</option><option value="FIXED_SCHEDULE">Theo lịch cố định</option><option value="AFTER_COMPLETION">Sau khi hoàn thành</option></select></label>
            </div>
            <section className="detail-section">
              <header><div><Icon name="label" size={17} /><h3>Nhãn</h3></div></header>
              <div className="detail-labels">{workspaceLabels.map((label) => <button key={label.id} className={cx('label-option', draft.labelIds.includes(label.id) && 'is-selected')} onClick={() => setDraft({ ...draft, labelIds: draft.labelIds.includes(label.id) ? draft.labelIds.filter((id) => id !== label.id) : [...draft.labelIds, label.id] })}><span style={{ background: label.color }} />{label.name}{draft.labelIds.includes(label.id) && <Icon name="check" size={13} />}</button>)}</div>
            </section>
            <section className="detail-section">
              <header><div><Icon name="check-circle" size={17} /><h3>Checklist</h3><span>{checked}/{checklist.length}</span></div>{checklist.length > 0 && <strong>{Math.round(checked / checklist.length * 100)}%</strong>}</header>
              {checklist.length > 0 && <Progress value={checked / checklist.length * 100} color="var(--green)" />}
              <div className="checklist-items">{checklist.map((item) => <label key={item.id} className={cx(item.completed && 'is-completed')}><button onClick={async () => { const updated = await app.toggleChecklist(item); setChecklist((items) => items.map((entry) => entry.id === updated.id ? updated : entry)) }}>{item.completed && <Icon name="check" size={12} />}</button><span>{item.content}</span><IconButton icon="close" label="Xóa mục" onClick={async () => { await app.deleteChecklist(item); setChecklist((items) => items.filter((entry) => entry.id !== item.id)) }} /></label>)}</div>
              <form className="inline-add" onSubmit={async (event) => { event.preventDefault(); if (!newCheck.trim()) return; const created = await app.addChecklist(draft.id, newCheck.trim()); setChecklist((items) => [...items, created]); setNewCheck('') }}><Icon name="plus" size={16} /><input value={newCheck} onChange={(event) => setNewCheck(event.target.value)} placeholder="Thêm mục checklist" /></form>
            </section>
            <section className="detail-section">
              <header><div><Icon name="task" size={17} /><h3>Công việc con</h3><span>{subtasks.length}</span></div></header>
              <div className="subtask-list">{subtasks.map((subtask) => <button key={subtask.id} onClick={() => window.dispatchEvent(new CustomEvent('smartly:open-task', { detail: subtask }))}><TaskCheckbox task={subtask} compact /><span>{subtask.title}</span><small>{formatDate(subtask.dueAt)}</small></button>)}</div>
              <Button variant="ghost" size="sm" icon="plus" onClick={() => window.dispatchEvent(new CustomEvent('smartly:quick-add', { detail: { projectId: draft.projectId, parentTaskId: draft.id } }))}>Thêm công việc con</Button>
            </section>
            <section className="detail-section">
              <header><div><Icon name="bell" size={17} /><h3>Nhắc việc</h3><span>{reminders.length}</span></div></header>
              {reminders.map((reminder) => <div className="reminder-row" key={reminder.id}><span className="detail-icon detail-icon--purple"><Icon name="clock" size={15} /></span><div><strong>{formatDateTime(reminder.remindAt)}</strong><small>Thông báo trong ứng dụng</small></div><IconButton icon="close" label="Xóa nhắc việc" onClick={async () => { await app.deleteReminder(reminder); setReminders((items) => items.filter((entry) => entry.id !== reminder.id)) }} /></div>)}
              <form className="inline-add" onSubmit={async (event) => { event.preventDefault(); if (!newReminder) return; try { const created = await app.addReminder(draft.id, toApiDate(newReminder)!); setReminders((items) => [...items, created]); setNewReminder('') } catch (error) { notify(error instanceof Error ? error.message : 'Không thể thêm nhắc việc', 'error') } }}><Icon name="plus" size={16} /><input type="datetime-local" value={newReminder} min={toLocalInput(new Date(Date.now() + 60_000).toISOString())} onChange={(event) => setNewReminder(event.target.value)} /><Button type="submit" variant="ghost" size="sm">Thêm</Button></form>
            </section>
            <section className="detail-section">
              <header><div><Icon name="paperclip" size={17} /><h3>Tệp đính kèm</h3><span>{attachments.length}</span></div><label className="file-upload"><Icon name="upload" size={15} />Tải lên<input type="file" onChange={async (event) => { const file = event.target.files?.[0]; if (!file) return; try { const created = await app.addAttachment(draft.id, file); setAttachments((items) => [...items, created]) } catch (error) { notify(error instanceof Error ? error.message : 'Tải file thất bại', 'error') } }} /></label></header>
              <div className="attachment-list">{attachments.map((attachment) => <div className="attachment-row" key={attachment.id}><span className="file-icon"><Icon name="file" size={18} /></span><div><strong>{attachment.originalFileName}</strong><small>{fileSize(attachment.sizeBytes)} · {formatDate(attachment.createdAt)}</small></div><IconButton icon="download" label="Tải file" onClick={() => download(attachment)} /><IconButton icon="trash" label="Xóa file" onClick={async () => { await app.deleteAttachment(attachment); setAttachments((items) => items.filter((entry) => entry.id !== attachment.id)) }} /></div>)}</div>
            </section>
          </div>
        ) : (
          <div className="task-drawer__content activity-panel">
            <div className="activity-summary"><span className="detail-icon detail-icon--green"><Icon name="activity" size={17} /></span><div><strong>Hoạt động gần đây</strong><p>Cập nhật và trao đổi xoay quanh công việc này.</p></div></div>
            {comments.map((comment) => {
              const author = users.find((item) => item.id === comment.authorId)
              return <article className="comment" key={comment.id}><Avatar name={author?.username || members.find((m) => m.userId === comment.authorId)?.username || 'Thành viên'} size="sm" /><div><header><strong>{author?.username || members.find((m) => m.userId === comment.authorId)?.username || 'Thành viên'}</strong><time>{formatDateTime(comment.createdAt)}</time>{(comment.authorId === currentUser?.id || app.isAdmin) && <IconButton icon="more" label="Tùy chọn bình luận" />}</header><p>{comment.content}</p></div></article>
            })}
            {!comments.length && !loadingExtras && <EmptyState icon="comment" title="Chưa có trao đổi" description="Bắt đầu cuộc trò chuyện về công việc này." />}
            <form className="comment-composer" onSubmit={async (event) => { event.preventDefault(); if (!newComment.trim()) return; const created = await app.addComment(draft.id, newComment.trim()); setComments((items) => [...items, created]); setNewComment('') }}><Avatar name={currentUser?.username} size="sm" /><div><textarea value={newComment} onChange={(event) => setNewComment(event.target.value)} placeholder="Viết bình luận…" rows={3} /><footer><span><Icon name="paperclip" size={15} /> <span className="muted">Hỗ trợ văn bản</span></span><Button size="sm" type="submit" disabled={!newComment.trim()}>Gửi bình luận</Button></footer></div></form>
          </div>
        )}
        <footer className="task-drawer__footer">
          <Button variant="danger" size="sm" icon="trash" onClick={() => setShowDelete(true)}>Xóa</Button>
          <div>{dirty && <span className="unsaved"><i />Chưa lưu</span>}<Button variant="secondary" onClick={onClose}>Đóng</Button><Button onClick={save} disabled={!dirty || !draft.title.trim()}>Lưu thay đổi</Button></div>
        </footer>
      </aside>
      <Modal open={showDelete} onClose={() => setShowDelete(false)} title="Xóa công việc?" width="sm" footer={<><Button variant="ghost" onClick={() => setShowDelete(false)}>Hủy</Button><Button variant="danger" onClick={remove}>Xóa công việc</Button></>}><div className="confirm-copy"><span className="confirm-icon"><Icon name="trash" size={24} /></span><p>“{draft.title}” sẽ được chuyển vào thùng rác. Bạn không thể hoàn tác thao tác này từ giao diện hiện tại.</p></div></Modal>
    </div>
  )
}
