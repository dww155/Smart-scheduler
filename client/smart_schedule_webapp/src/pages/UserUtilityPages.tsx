import { useEffect, useState, type FormEvent } from 'react'
import { useApp } from '../context/AppContext'
import { api } from '../lib/api'
import { navigate } from '../lib/router'
import { cx, formatDate, formatDateTime } from '../lib/utils'
import type { ProjectView, Task, Workspace, WorkspaceRole } from '../types'
import { Link } from '../components/shells'
import { TaskList } from '../components/tasks'
import { Avatar, Badge, Button, EmptyState, Icon, IconButton, Modal } from '../components/ui'
import { PageHeader } from './UserPages'

export function SearchPage({ onOpenTask }: { onOpenTask: (task: Task) => void }) {
  const { tasks, projects, labels, workspaces } = useApp()
  const params = new URLSearchParams(window.location.search)
  const [query, setQuery] = useState(params.get('q') || '')
  const [type, setType] = useState<'all' | 'tasks' | 'projects' | 'labels'>('all')
  const [onlyActive, setOnlyActive] = useState(true)
  const normalized = query.trim().toLowerCase()
  const taskResults = tasks.filter((task) => (!onlyActive || !['COMPLETED', 'CANCELLED'].includes(task.status)) && (!normalized || `${task.title} ${task.description || ''}`.toLowerCase().includes(normalized)))
  const projectResults = projects.filter((project) => !normalized || `${project.name} ${project.description || ''}`.toLowerCase().includes(normalized))
  const labelResults = labels.filter((label) => !normalized || label.name.toLowerCase().includes(normalized))
  useEffect(() => {
    const timer = window.setTimeout(() => {
      const url = query ? `/app/search?q=${encodeURIComponent(query)}` : '/app/search'
      window.history.replaceState({}, '', url)
    }, 200)
    return () => clearTimeout(timer)
  }, [query])
  return (
    <div className="page search-page">
      <PageHeader title="Tìm kiếm" description="Tìm nhanh công việc, dự án và nhãn trong mọi workspace." icon="search" />
      <div className="global-search-box"><Icon name="search" size={21} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Nhập tên công việc, dự án hoặc nhãn…" autoFocus /><kbd>ESC</kbd>{query && <IconButton icon="close" label="Xóa tìm kiếm" onClick={() => setQuery('')} />}</div>
      <div className="search-controls"><div className="segmented"><button className={type === 'all' ? 'is-active' : ''} onClick={() => setType('all')}>Tất cả</button><button className={type === 'tasks' ? 'is-active' : ''} onClick={() => setType('tasks')}>Công việc <span>{taskResults.length}</span></button><button className={type === 'projects' ? 'is-active' : ''} onClick={() => setType('projects')}>Dự án <span>{projectResults.length}</span></button><button className={type === 'labels' ? 'is-active' : ''} onClick={() => setType('labels')}>Nhãn <span>{labelResults.length}</span></button></div><label className="toggle-line"><input type="checkbox" checked={onlyActive} onChange={(event) => setOnlyActive(event.target.checked)} /><span />Chỉ đang hoạt động</label></div>
      {!query && (
        <section className="search-suggestions"><span className="eyebrow">GỢI Ý TÌM KIẾM</span><div>{['Công việc quá hạn', 'Ưu tiên cao', 'Được giao cho tôi', 'Không có ngày hạn'].map((item) => <button key={item} onClick={() => setQuery(item.replace('Công việc ', ''))}><Icon name="sparkles" size={15} />{item}</button>)}</div></section>
      )}
      {query && <div className="search-results">
        {(type === 'all' || type === 'tasks') && <section><header><h3>Công việc</h3><span>{taskResults.length} kết quả</span></header>{taskResults.length ? <TaskList tasks={taskResults.slice(0, type === 'all' ? 6 : 50)} onOpen={onOpenTask} /> : <p className="no-result">Không có công việc phù hợp.</p>}</section>}
        {(type === 'all' || type === 'projects') && projectResults.length > 0 && <section><header><h3>Dự án</h3><span>{projectResults.length} kết quả</span></header><div className="search-entity-list">{projectResults.map((project) => <Link key={project.id} to={`/app/project/${project.id}`}><span className="entity-icon" style={{ color: project.color, background: `${project.color}15` }}><Icon name="project" size={17} /></span><div><strong>{project.name}</strong><small>{workspaces.find((item) => item.id === project.workspaceId)?.name}</small></div><Icon name="chevron-right" size={16} /></Link>)}</div></section>}
        {(type === 'all' || type === 'labels') && labelResults.length > 0 && <section><header><h3>Nhãn</h3><span>{labelResults.length} kết quả</span></header><div className="search-labels">{labelResults.map((label) => <Link key={label.id} to={`/app/labels?label=${label.id}`}><span style={{ background: label.color }} />{label.name}<small>{tasks.filter((task) => task.labelIds.includes(label.id)).length}</small></Link>)}</div></section>}
        {!taskResults.length && !projectResults.length && !labelResults.length && <EmptyState icon="search" title="Không tìm thấy kết quả" description={`Không có dữ liệu nào khớp với “${query}”. Hãy thử một từ khóa khác.`} />}
      </div>}
    </div>
  )
}

const labelColors = ['#6d5dfc', '#3b82f6', '#14b8a6', '#22c55e', '#eab308', '#f97316', '#ef4444', '#ec4899']

export function LabelsPage({ onOpenTask }: { onOpenTask: (task: Task) => void }) {
  const { labels, tasks, activeWorkspaceId, createLabel, updateLabel, deleteLabel, notify } = useApp()
  const visible = labels.filter((item) => item.workspaceId === activeWorkspaceId)
  const [selectedId, setSelectedId] = useState(new URLSearchParams(window.location.search).get('label') || visible[0]?.id || '')
  const [modal, setModal] = useState<'create' | 'edit' | null>(null)
  const selected = visible.find((item) => item.id === selectedId)
  const [name, setName] = useState('')
  const [color, setColor] = useState(labelColors[0])
  const labelTasks = selected ? tasks.filter((task) => task.labelIds.includes(selected.id)) : []
  useEffect(() => { if (selected) { setName(selected.name); setColor(selected.color || labelColors[0]) } }, [selected])
  const openCreate = () => { setName(''); setColor(labelColors[visible.length % labelColors.length]); setModal('create') }
  const save = async () => {
    if (!name.trim()) return
    try {
      if (modal === 'create') { const created = await createLabel(activeWorkspaceId, name.trim(), color); setSelectedId(created.id) }
      else if (selected) await updateLabel({ ...selected, name: name.trim(), color })
      setModal(null)
    } catch (error) { notify(error instanceof Error ? error.message : 'Không thể lưu nhãn', 'error') }
  }
  return (
    <div className="page">
      <PageHeader title="Nhãn" description="Nhóm công việc theo ngữ cảnh để tìm lại nhanh hơn." icon="label"><Button icon="plus" onClick={openCreate}>Tạo nhãn</Button></PageHeader>
      <div className="labels-layout">
        <aside className="labels-sidebar"><header><span>Tất cả nhãn</span><small>{visible.length}</small></header>{visible.map((label) => <button className={cx(selectedId === label.id && 'is-active')} key={label.id} onClick={() => setSelectedId(label.id)}><span style={{ background: label.color }} /><strong>{label.name}</strong><em>{tasks.filter((task) => task.labelIds.includes(label.id)).length}</em></button>)}{!visible.length && <p>Chưa có nhãn.</p>}</aside>
        <section className="labels-content">{selected ? <><header><div><span className="large-label-dot" style={{ background: selected.color }} /><div><span className="eyebrow">NHÃN</span><h2>{selected.name}</h2><p>{labelTasks.length} công việc đang dùng nhãn này</p></div></div><div><Button variant="secondary" size="sm" icon="edit" onClick={() => setModal('edit')}>Chỉnh sửa</Button><IconButton icon="trash" label="Lưu trữ nhãn" variant="danger" onClick={async () => { if (!confirm(`Lưu trữ nhãn “${selected.name}”?`)) return; await deleteLabel(selected); const next = visible.find((item) => item.id !== selected.id); setSelectedId(next?.id || '') }} /></div></header><TaskList tasks={labelTasks} onOpen={onOpenTask} emptyTitle="Chưa có công việc" emptyDescription="Gắn nhãn này vào một công việc để thấy nó ở đây." /></> : <EmptyState icon="label" title="Chọn một nhãn" description="Chọn nhãn bên trái hoặc tạo nhãn mới." action={<Button icon="plus" onClick={openCreate}>Tạo nhãn</Button>} />}</section>
      </div>
      <Modal open={Boolean(modal)} onClose={() => setModal(null)} title={modal === 'create' ? 'Tạo nhãn mới' : 'Chỉnh sửa nhãn'} width="sm" footer={<><Button variant="ghost" onClick={() => setModal(null)}>Hủy</Button><Button onClick={save} disabled={!name.trim()}>Lưu nhãn</Button></>}><div className="form-stack"><label><span>Tên nhãn</span><input value={name} onChange={(event) => setName(event.target.value)} placeholder="Ví dụ: Quan trọng" autoFocus /></label><label><span>Màu nhãn</span><div className="color-picker">{labelColors.map((item) => <button key={item} className={cx(color === item && 'is-active')} style={{ background: item }} onClick={() => setColor(item)}>{color === item && <Icon name="check" size={14} />}</button>)}</div></label><div className="label-preview"><span>Xem trước</span><Badge tone="purple"><i style={{ background: color }} />{name || 'Tên nhãn'}</Badge></div></div></Modal>
    </div>
  )
}

export function WorkspacePage() {
  const app = useApp()
  const { activeWorkspace, activeWorkspaceId, members, users, projects, tasks, currentUser, updateWorkspace, deleteWorkspace, addMember, updateMemberRole, removeMember, notify } = app
  const [tab, setTab] = useState<'overview' | 'members' | 'settings'>('overview')
  const [edit, setEdit] = useState<Workspace | null>(activeWorkspace || null)
  const [inviteOpen, setInviteOpen] = useState(false)
  const [candidates, setCandidates] = useState<Array<{ id: string; username: string; email?: string }>>([])
  const [userId, setUserId] = useState('')
  const [role, setRole] = useState<WorkspaceRole>('MEMBER')
  useEffect(() => setEdit(activeWorkspace || null), [activeWorkspaceId, activeWorkspace])
  useEffect(() => {
    if (!inviteOpen || !activeWorkspace) return
    if (app.demoMode) {
      setCandidates(users.filter((user) => !members.some((member) => member.workspaceId === activeWorkspace.id && member.userId === user.id) && user.active))
      return
    }
    api.memberCandidates(activeWorkspace.id)
      .then(setCandidates)
      .catch((error) => notify(error instanceof Error ? error.message : 'Không thể tải danh sách người dùng', 'error'))
  }, [inviteOpen, activeWorkspaceId, activeWorkspace, app.demoMode, users, members, notify])
  if (!activeWorkspace || !edit) return <div className="page"><EmptyState icon="workspace" title="Chưa có workspace" description="Tạo workspace để bắt đầu tổ chức công việc." /></div>
  const visibleMembers = members.filter((item) => item.workspaceId === activeWorkspace.id)
  const visibleProjects = projects.filter((item) => item.workspaceId === activeWorkspace.id)
  const projectIds = new Set(visibleProjects.map((item) => item.id))
  const visibleTasks = tasks.filter((item) => projectIds.has(item.projectId))
  const myMembership = visibleMembers.find((item) => item.userId === currentUser?.id)
  const canManage = app.isAdmin || ['OWNER', 'ADMIN'].includes(myMembership?.role || '') || activeWorkspace.ownerId === currentUser?.id
  const canChangeRoles = app.isAdmin || myMembership?.role === 'OWNER' || activeWorkspace.ownerId === currentUser?.id
  const availableUsers = candidates
  return (
    <div className="page workspace-page">
      <PageHeader eyebrow="WORKSPACE" title={activeWorkspace.name} description={activeWorkspace.description || 'Quản lý không gian, thành viên và quyền truy cập.'}><Badge tone={activeWorkspace.type === 'TEAM' ? 'purple' : 'blue'}>{activeWorkspace.type === 'TEAM' ? 'Nhóm' : 'Cá nhân'}</Badge>{canManage && <Button icon="plus" onClick={() => setInviteOpen(true)}>Thêm thành viên</Button>}</PageHeader>
      <div className="settings-tabs"><button className={tab === 'overview' ? 'is-active' : ''} onClick={() => setTab('overview')}>Tổng quan</button><button className={tab === 'members' ? 'is-active' : ''} onClick={() => setTab('members')}>Thành viên <span>{visibleMembers.length}</span></button><button className={tab === 'settings' ? 'is-active' : ''} onClick={() => setTab('settings')}>Cài đặt</button></div>
      {tab === 'overview' && <div className="workspace-overview"><div className="workspace-stats"><article><span className="stat-icon stat-icon--purple"><Icon name="project" size={19} /></span><div><small>Dự án</small><strong>{visibleProjects.length}</strong></div></article><article><span className="stat-icon stat-icon--blue"><Icon name="task" size={19} /></span><div><small>Công việc</small><strong>{visibleTasks.length}</strong></div></article><article><span className="stat-icon stat-icon--green"><Icon name="check-circle" size={19} /></span><div><small>Hoàn thành</small><strong>{visibleTasks.filter((item) => item.status === 'COMPLETED').length}</strong></div></article><article><span className="stat-icon stat-icon--amber"><Icon name="users" size={19} /></span><div><small>Thành viên</small><strong>{visibleMembers.length}</strong></div></article></div><section className="content-card"><header className="content-card__heading"><div><h3>Dự án đang hoạt động</h3><p>Tình hình chung trong workspace</p></div><Link to="/app/projects">Xem tất cả <Icon name="arrow-right" size={14} /></Link></header><div className="workspace-project-rows">{visibleProjects.map((project) => { const projectTasks = visibleTasks.filter((item) => item.projectId === project.id); const done = projectTasks.filter((item) => item.status === 'COMPLETED').length; return <Link to={`/app/project/${project.id}`} key={project.id}><span className="entity-icon" style={{ color: project.color, background: `${project.color}15` }}><Icon name="project" size={17} /></span><div><strong>{project.name}</strong><small>{projectTasks.length} công việc · {done} hoàn thành</small></div><div className="mini-progress"><span style={{ width: `${projectTasks.length ? done / projectTasks.length * 100 : 0}%`, background: project.color }} /></div><Icon name="chevron-right" size={16} /></Link>})}</div></section></div>}
      {tab === 'members' && <section className="content-card members-card"><header className="content-card__heading"><div><h3>Thành viên workspace</h3><p>Phân quyền và quản lý quyền truy cập.</p></div>{canManage && <Button size="sm" icon="plus" onClick={() => setInviteOpen(true)}>Thêm thành viên</Button>}</header><div className="members-table table-scroll"><table><thead><tr><th>Thành viên</th><th>Vai trò</th><th>Tham gia</th><th>Trạng thái</th><th /></tr></thead><tbody>{visibleMembers.map((member) => { const user = users.find((item) => item.id === member.userId); const name = member.username || user?.username || (member.userId === currentUser?.id ? currentUser.username : `User ${member.userId.slice(0, 6)}`); const isOwner = member.role === 'OWNER' || member.userId === activeWorkspace.ownerId; return <tr key={member.id}><td><div className="table-user"><Avatar name={name} size="sm" /><div><strong>{name}{member.userId === currentUser?.id && <small> Bạn</small>}</strong><span>{member.email || user?.email || 'Thành viên workspace'}</span></div></div></td><td>{canChangeRoles && !isOwner ? <select className="role-select" value={member.role} onChange={async (event) => { try { await updateMemberRole(member, event.target.value as WorkspaceRole) } catch (error) { notify(error instanceof Error ? error.message : 'Không thể đổi vai trò', 'error') } }}><option value="ADMIN">Admin</option><option value="MEMBER">Thành viên</option><option value="VIEWER">Chỉ xem</option></select> : <Badge tone={isOwner ? 'purple' : member.role === 'ADMIN' ? 'blue' : 'neutral'}>{isOwner ? 'Chủ sở hữu' : member.role === 'ADMIN' ? 'Admin' : member.role === 'VIEWER' ? 'Chỉ xem' : 'Thành viên'}</Badge>}</td><td>{formatDate(member.joinedAt)}</td><td><Badge tone={(member.active ?? user?.active) === false ? 'red' : 'green'} dot>{(member.active ?? user?.active) === false ? 'Tạm khóa' : 'Hoạt động'}</Badge></td><td>{canManage && !isOwner && <IconButton icon="trash" label="Xóa thành viên" onClick={async () => { if (!confirm(`Xóa ${name} khỏi workspace?`)) return; await removeMember(member) }} />}</td></tr>})}</tbody></table></div></section>}
      {tab === 'settings' && <div className="settings-columns"><section className="settings-panel"><header><h3>Thông tin chung</h3><p>Tên, mô tả và nhận diện của workspace.</p></header><div className="form-stack"><label><span>Tên workspace</span><input value={edit.name} disabled={!canManage} onChange={(event) => setEdit({ ...edit, name: event.target.value })} /></label><label><span>Mô tả</span><textarea rows={4} value={edit.description || ''} disabled={!canManage} onChange={(event) => setEdit({ ...edit, description: event.target.value })} /></label><div className="form-grid"><label><span>Loại workspace</span><select value={edit.type} disabled={!canManage} onChange={(event) => setEdit({ ...edit, type: event.target.value as Workspace['type'] })}><option value="PERSONAL">Cá nhân</option><option value="TEAM">Nhóm</option></select></label><label><span>Màu nhận diện</span><div className="color-input"><input type="color" value={edit.color || '#6d5dfc'} disabled={!canManage} onChange={(event) => setEdit({ ...edit, color: event.target.value })} /><span>{edit.color}</span></div></label></div>{canManage && <div className="form-actions"><Button onClick={async () => { try { await updateWorkspace(edit) } catch (error) { notify(error instanceof Error ? error.message : 'Không thể lưu workspace', 'error') } }}>Lưu thay đổi</Button></div>}</div></section><section className="settings-panel danger-zone"><header><h3>Vùng nguy hiểm</h3><p>Các thao tác dưới đây có thể ảnh hưởng đến toàn bộ dữ liệu.</p></header><div><div><strong>Xóa workspace</strong><p>Xóa workspace cùng quyền truy cập tới dự án và công việc bên trong.</p></div><Button variant="danger" disabled={!canChangeRoles} onClick={async () => { if (!confirm(`Bạn chắc chắn muốn xóa “${activeWorkspace.name}”?`)) return; await deleteWorkspace(activeWorkspace); navigate('/app/today') }}>Xóa workspace</Button></div></section></div>}
      <Modal open={inviteOpen} onClose={() => setInviteOpen(false)} title="Thêm thành viên" eyebrow={activeWorkspace.name} width="sm" footer={<><Button variant="ghost" onClick={() => setInviteOpen(false)}>Hủy</Button><Button disabled={!userId} onClick={async () => { try { await addMember(activeWorkspace.id, userId, role); setUserId(''); setInviteOpen(false) } catch (error) { notify(error instanceof Error ? error.message : 'Không thể thêm thành viên', 'error') } }}>Thêm vào workspace</Button></>}><div className="form-stack"><label><span>Người dùng</span>{availableUsers.length ? <select value={userId} onChange={(event) => setUserId(event.target.value)} autoFocus><option value="">Chọn người dùng</option>{availableUsers.map((user) => <option value={user.id} key={user.id}>{user.username} — {user.email}</option>)}</select> : <div className="inline-notice"><Icon name="mail" size={17} /><p>Backend hiện chỉ hỗ trợ thêm bằng UUID. Không còn user khả dụng trong danh sách bạn có quyền xem.</p></div>}</label><label><span>Vai trò</span><select value={role} onChange={(event) => setRole(event.target.value as WorkspaceRole)}><option value="MEMBER">Thành viên — tạo và sửa công việc</option><option value="ADMIN">Admin — quản lý dự án và nhãn</option><option value="VIEWER">Chỉ xem — không chỉnh sửa</option></select></label></div></Modal>
    </div>
  )
}

export function SettingsPage() {
  const app = useApp()
  const { currentUser, preferences, setPreferences, notify, reload, demoMode } = app
  const [tab, setTab] = useState<'profile' | 'preferences' | 'notifications' | 'security'>('profile')
  const [username, setUsername] = useState(currentUser?.username || '')
  const [saving, setSaving] = useState(false)
  const saveProfile = async () => {
    if (!currentUser || !username.trim()) return
    setSaving(true)
    try {
      if (!demoMode) await api.updateUser(currentUser.id, username.trim())
      await reload()
      notify('Đã cập nhật hồ sơ')
    } catch (error) { notify(error instanceof Error ? error.message : 'Không thể cập nhật hồ sơ', 'error') }
    finally { setSaving(false) }
  }
  return (
    <div className="page settings-page">
      <PageHeader title="Cài đặt cá nhân" description="Tùy chỉnh hồ sơ và trải nghiệm Smartly." icon="settings" />
      <div className="settings-layout"><aside>{[
        ['profile', 'user', 'Hồ sơ'], ['preferences', 'settings', 'Tùy chọn'], ['notifications', 'bell', 'Thông báo'], ['security', 'lock', 'Bảo mật'],
      ].map(([id, icon, label]) => <button key={id} className={tab === id ? 'is-active' : ''} onClick={() => setTab(id as typeof tab)}><Icon name={icon as 'user'} size={17} />{label}</button>)}</aside><main>
        {tab === 'profile' && <section className="settings-panel"><header><h3>Thông tin hồ sơ</h3><p>Thông tin này được hiển thị với thành viên trong workspace.</p></header><div className="profile-avatar-editor"><Avatar name={username} size="xl" /><div><Button variant="secondary" size="sm" icon="upload">Tải ảnh mới</Button><p>JPG, PNG hoặc GIF. Tối đa 2 MB.</p></div></div><div className="form-stack"><label><span>Tên hiển thị</span><input value={username} onChange={(event) => setUsername(event.target.value)} /></label><label><span>Email</span><input type="email" value={currentUser?.email || ''} disabled /><small>Backend hiện chưa hỗ trợ thay đổi email.</small></label><div className="form-actions"><Button onClick={saveProfile} loading={saving} disabled={username.trim() === currentUser?.username}>Lưu hồ sơ</Button></div></div></section>}
        {tab === 'preferences' && <section className="settings-panel"><header><h3>Giao diện & trải nghiệm</h3><p>Chọn cách Smartly hiển thị trên thiết bị này.</p></header><div className="preference-list"><div><div><strong>Giao diện</strong><p>Chọn chế độ màu phù hợp với bạn.</p></div><div className="theme-options">{(['light', 'dark', 'system'] as const).map((theme) => <button key={theme} className={preferences.theme === theme ? 'is-active' : ''} onClick={() => setPreferences({ ...preferences, theme })}><Icon name={theme === 'light' ? 'sun' : theme === 'dark' ? 'moon' : 'settings'} size={17} />{theme === 'light' ? 'Sáng' : theme === 'dark' ? 'Tối' : 'Hệ thống'}</button>)}</div></div><ToggleSetting title="Chế độ thu gọn" description="Hiển thị nhiều công việc hơn trên màn hình." checked={preferences.compactMode} onChange={(value) => setPreferences({ ...preferences, compactMode: value })} /><ToggleSetting title="Tuần bắt đầu từ thứ Hai" description="Áp dụng cho lịch và màn hình sắp tới." checked={preferences.weekStartsMonday} onChange={(value) => setPreferences({ ...preferences, weekStartsMonday: value })} /></div></section>}
        {tab === 'notifications' && <section className="settings-panel"><header><h3>Thông báo</h3><p>Quản lý cách bạn nhận cập nhật và nhắc việc.</p></header><div className="preference-list"><ToggleSetting title="Thông báo trên desktop" description="Nhận nhắc việc khi Smartly đang mở." checked={preferences.desktopNotifications} onChange={(value) => setPreferences({ ...preferences, desktopNotifications: value })} /><ToggleSetting title="Email tổng hợp mỗi ngày" description="Bản tóm tắt công việc đến hạn mỗi sáng." checked={false} onChange={() => notify('Kênh email sẽ được bổ sung khi backend có worker gửi mail', 'info')} /><ToggleSetting title="Hoạt động workspace" description="Nhận tin khi có người giao việc hoặc bình luận." checked={true} onChange={() => notify('Đã lưu tùy chọn', 'success')} /></div><div className="inline-notice inline-notice--amber"><Icon name="warning" size={18} /><p>Backend hiện lưu reminder nhưng chưa có scheduler gửi thông báo. Giao diện đã sẵn sàng cho worker ở bước tiếp theo.</p></div></section>}
        {tab === 'security' && <section className="settings-panel"><header><h3>Bảo mật tài khoản</h3><p>Mật khẩu và các phiên đăng nhập của bạn.</p></header><div className="security-cards"><div><span className="detail-icon detail-icon--purple"><Icon name="lock" size={18} /></span><div><strong>Mật khẩu</strong><p>Đổi mật khẩu định kỳ để bảo vệ tài khoản.</p></div><Button variant="secondary" disabled>Đổi mật khẩu</Button></div><div><span className="detail-icon detail-icon--green"><Icon name="activity" size={18} /></span><div><strong>Phiên hiện tại</strong><p>Trình duyệt này · hoạt động bây giờ</p></div><Badge tone="green" dot>Hiện tại</Badge></div></div><div className="inline-notice"><Icon name="lock" size={18} /><p>API đổi mật khẩu và quản lý phiên chưa có trong backend. Không nhập dữ liệu nhạy cảm cho đến khi endpoint hoàn thiện.</p></div></section>}
      </main></div>
    </div>
  )
}

function ToggleSetting({ title, description, checked, onChange }: { title: string; description: string; checked: boolean; onChange: (value: boolean) => void }) {
  return <label className="preference-toggle"><div><strong>{title}</strong><p>{description}</p></div><input type="checkbox" checked={checked} onChange={(event) => onChange(event.target.checked)} /><span /></label>
}

export function NotificationsPage({ onOpenTask }: { onOpenTask: (task: Task) => void }) {
  const { getDemoExtras, tasks } = useApp()
  const reminders = getDemoExtras().reminders
  return <div className="page"><PageHeader title="Thông báo" description="Nhắc việc và cập nhật liên quan đến bạn." icon="bell"><Button variant="secondary" size="sm">Đánh dấu đã đọc</Button></PageHeader><div className="notification-list">{reminders.map((reminder) => { const task = tasks.find((item) => item.id === reminder.taskId); return <button key={reminder.id} onClick={() => task && onOpenTask(task)}><span className="notification-icon"><Icon name="clock" size={18} /></span><div><strong>Sắp đến hạn: {task?.title || 'Công việc'}</strong><p>Nhắc bạn vào {formatDateTime(reminder.remindAt)}</p><small>{formatDate(reminder.remindAt)}</small></div><i /></button>})}{!reminders.length && <EmptyState icon="bell" title="Không có thông báo mới" description="Các nhắc việc và cập nhật sẽ xuất hiện ở đây." />}</div></div>
}

export function CreateProjectModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { activeWorkspaceId, createProject, notify } = useApp()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [viewType, setViewType] = useState<ProjectView>('LIST')
  const [color, setColor] = useState('#6d5dfc')
  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (!name.trim()) return
    try { const project = await createProject({ workspaceId: activeWorkspaceId, name: name.trim(), description, viewType, color, icon: 'hash' }); setName(''); setDescription(''); onClose(); navigate(`/app/project/${project.id}`) } catch (error) { notify(error instanceof Error ? error.message : 'Không thể tạo dự án', 'error') }
  }
  return <Modal open={open} onClose={onClose} title="Tạo dự án mới" eyebrow="PROJECT" width="md"><form className="form-stack" onSubmit={submit}><label><span>Tên dự án</span><input value={name} onChange={(event) => setName(event.target.value)} placeholder="Ví dụ: Ra mắt sản phẩm" autoFocus /></label><label><span>Mô tả</span><textarea rows={3} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Mục tiêu của dự án…" /></label><label><span>Kiểu hiển thị</span><div className="view-type-picker">{([['LIST', 'list', 'Danh sách'], ['BOARD', 'board', 'Bảng Kanban'], ['CALENDAR', 'calendar', 'Lịch']] as const).map(([value, icon, label]) => <button type="button" key={value} className={viewType === value ? 'is-active' : ''} onClick={() => setViewType(value)}><Icon name={icon} size={20} /><strong>{label}</strong>{viewType === value && <Icon name="check-circle" size={16} />}</button>)}</div></label><label><span>Màu dự án</span><div className="color-picker">{['#6d5dfc', '#3b82f6', '#14b8a6', '#22c55e', '#f59e0b', '#ef4444', '#ec4899'].map((item) => <button type="button" key={item} style={{ background: item }} className={color === item ? 'is-active' : ''} onClick={() => setColor(item)}>{color === item && <Icon name="check" size={14} />}</button>)}</div></label><footer className="form-modal-footer"><Button type="button" variant="ghost" onClick={onClose}>Hủy</Button><Button type="submit" disabled={!name.trim()} icon="plus">Tạo dự án</Button></footer></form></Modal>
}

export function CreateWorkspaceModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { createWorkspace, notify } = useApp()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [type, setType] = useState<Workspace['type']>('TEAM')
  const submit = async (event: FormEvent) => { event.preventDefault(); if (!name.trim()) return; try { await createWorkspace({ name: name.trim(), description, type, color: '#6d5dfc' }); setName(''); setDescription(''); onClose(); navigate('/app/workspace') } catch (error) { notify(error instanceof Error ? error.message : 'Không thể tạo workspace', 'error') } }
  return <Modal open={open} onClose={onClose} title="Tạo workspace" eyebrow="NEW SPACE" width="md"><form className="form-stack" onSubmit={submit}><label><span>Tên workspace</span><input value={name} onChange={(event) => setName(event.target.value)} placeholder="Tên đội hoặc không gian" autoFocus /></label><label><span>Mô tả</span><textarea rows={3} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Workspace này dùng cho việc gì?" /></label><label><span>Loại workspace</span><div className="workspace-type-picker"><button type="button" className={type === 'PERSONAL' ? 'is-active' : ''} onClick={() => setType('PERSONAL')}><Icon name="user" size={21} /><div><strong>Cá nhân</strong><small>Chỉ dành cho bạn</small></div></button><button type="button" className={type === 'TEAM' ? 'is-active' : ''} onClick={() => setType('TEAM')}><Icon name="users" size={21} /><div><strong>Nhóm</strong><small>Cộng tác với thành viên</small></div></button></div></label><footer className="form-modal-footer"><Button type="button" variant="ghost" onClick={onClose}>Hủy</Button><Button type="submit" disabled={!name.trim()} icon="plus">Tạo workspace</Button></footer></form></Modal>
}
