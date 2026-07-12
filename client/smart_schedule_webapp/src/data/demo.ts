import type {
  ChecklistItem,
  Label,
  Project,
  Task,
  TaskAttachment,
  TaskComment,
  TaskReminder,
  TaskSection,
  User,
  Workspace,
  WorkspaceMember,
} from '../types'
import { addDays } from '../lib/utils'

const isoAt = (days: number, hour = 9, minute = 0) => {
  const date = addDays(new Date(), days)
  date.setHours(hour, minute, 0, 0)
  return date.toISOString().slice(0, 19)
}

export interface DemoData {
  users: User[]
  workspaces: Workspace[]
  members: WorkspaceMember[]
  projects: Project[]
  sections: TaskSection[]
  labels: Label[]
  tasks: Task[]
  checklist: ChecklistItem[]
  comments: TaskComment[]
  reminders: TaskReminder[]
  attachments: TaskAttachment[]
}

export const demoUsers: User[] = [
  {
    id: 'user-linh',
    username: 'Linh Nguyễn',
    email: 'linh@smartly.vn',
    active: true,
    roleNames: ['user'],
    createdAt: isoAt(-96),
  },
  {
    id: 'user-minh',
    username: 'Minh Trần',
    email: 'minh@smartly.vn',
    active: true,
    roleNames: ['user'],
    createdAt: isoAt(-84),
  },
  {
    id: 'user-an',
    username: 'An Lê',
    email: 'an@smartly.vn',
    active: true,
    roleNames: ['user'],
    createdAt: isoAt(-51),
  },
  {
    id: 'user-mai',
    username: 'Mai Phạm',
    email: 'mai@smartly.vn',
    active: false,
    roleNames: ['user'],
    createdAt: isoAt(-29),
  },
  {
    id: 'user-admin',
    username: 'Quản trị viên',
    email: 'admin@smartly.vn',
    active: true,
    roleNames: ['user', 'admin'],
    createdAt: isoAt(-180),
  },
]

const workspaces: Workspace[] = [
  {
    id: 'ws-product',
    ownerId: 'user-linh',
    name: 'Nova Product',
    description: 'Không gian chung cho đội sản phẩm Nova',
    type: 'TEAM',
    color: '#6d5dfc',
    createdAt: isoAt(-90),
  },
  {
    id: 'ws-personal',
    ownerId: 'user-linh',
    name: 'Cá nhân',
    description: 'Kế hoạch cá nhân và thói quen',
    type: 'PERSONAL',
    color: '#ed7c5b',
    createdAt: isoAt(-95),
  },
  {
    id: 'ws-marketing',
    ownerId: 'user-minh',
    name: 'Growth Lab',
    description: 'Workspace chiến dịch marketing',
    type: 'TEAM',
    color: '#1fa67a',
    createdAt: isoAt(-64),
  },
]

const projects: Project[] = [
  {
    id: 'project-launch',
    workspaceId: 'ws-product',
    createdById: 'user-linh',
    name: 'Ra mắt ứng dụng 2.0',
    description: 'Các đầu việc cho phiên bản mới trong quý này',
    color: '#6d5dfc',
    icon: 'rocket',
    viewType: 'BOARD',
    sortOrder: 0,
    createdAt: isoAt(-45),
  },
  {
    id: 'project-website',
    workspaceId: 'ws-product',
    createdById: 'user-minh',
    name: 'Website mới',
    description: 'Thiết kế và phát triển trang marketing',
    color: '#e85d75',
    icon: 'globe',
    viewType: 'LIST',
    sortOrder: 1,
    createdAt: isoAt(-32),
  },
  {
    id: 'project-personal',
    workspaceId: 'ws-personal',
    createdById: 'user-linh',
    name: 'Việc cá nhân',
    description: 'Inbox và việc thường ngày',
    color: '#ed7c5b',
    icon: 'home',
    viewType: 'LIST',
    sortOrder: 0,
    createdAt: isoAt(-90),
  },
  {
    id: 'project-campaign',
    workspaceId: 'ws-marketing',
    createdById: 'user-minh',
    name: 'Summer Campaign',
    description: 'Kế hoạch chiến dịch mùa hè',
    color: '#1fa67a',
    icon: 'megaphone',
    viewType: 'CALENDAR',
    sortOrder: 0,
    createdAt: isoAt(-30),
  },
]

const sections: TaskSection[] = [
  { id: 'sec-ideas', projectId: 'project-launch', name: 'Ý tưởng', description: '', sortOrder: 0 },
  { id: 'sec-progress', projectId: 'project-launch', name: 'Đang thực hiện', description: '', sortOrder: 1 },
  { id: 'sec-review', projectId: 'project-launch', name: 'Đang duyệt', description: '', sortOrder: 2 },
  { id: 'sec-done', projectId: 'project-launch', name: 'Hoàn thành', description: '', sortOrder: 3 },
  { id: 'sec-design', projectId: 'project-website', name: 'Thiết kế', description: '', sortOrder: 0 },
  { id: 'sec-build', projectId: 'project-website', name: 'Phát triển', description: '', sortOrder: 1 },
  { id: 'sec-personal', projectId: 'project-personal', name: 'Việc cần làm', description: '', sortOrder: 0 },
  { id: 'sec-campaign', projectId: 'project-campaign', name: 'Chiến dịch', description: '', sortOrder: 0 },
]

const labels: Label[] = [
  { id: 'label-design', workspaceId: 'ws-product', createdById: 'user-linh', name: 'Design', color: '#a855f7' },
  { id: 'label-dev', workspaceId: 'ws-product', createdById: 'user-minh', name: 'Development', color: '#3b82f6' },
  { id: 'label-research', workspaceId: 'ws-product', createdById: 'user-an', name: 'Research', color: '#14b8a6' },
  { id: 'label-urgent', workspaceId: 'ws-product', createdById: 'user-linh', name: 'Khẩn cấp', color: '#ef4444' },
  { id: 'label-personal', workspaceId: 'ws-personal', createdById: 'user-linh', name: 'Cá nhân', color: '#f97316' },
  { id: 'label-health', workspaceId: 'ws-personal', createdById: 'user-linh', name: 'Sức khỏe', color: '#22c55e' },
  { id: 'label-content', workspaceId: 'ws-marketing', createdById: 'user-minh', name: 'Content', color: '#eab308' },
]

const task = (
  id: string,
  projectId: string,
  sectionId: string | null,
  title: string,
  dueDays: number | null,
  priority: Task['priority'],
  status: Task['status'],
  labelIds: string[] = [],
  assigneeId = 'user-linh',
  description = '',
): Task => ({
  id,
  projectId,
  sectionId,
  parentTaskId: null,
  createdById: 'user-linh',
  assigneeId,
  title,
  description,
  status,
  priority,
  startAt: dueDays === null ? null : isoAt(dueDays, 8),
  dueAt: dueDays === null ? null : isoAt(dueDays, 17, 30),
  allDay: true,
  recurrenceMode: 'NONE',
  recurrenceRule: null,
  timeZone: 'Asia/Ho_Chi_Minh',
  completedAt: status === 'COMPLETED' ? isoAt(-1, 15) : null,
  sortOrder: 0,
  labelIds,
  createdAt: isoAt(-10),
  updatedAt: isoAt(-1),
})

const tasks: Task[] = [
  task('task-research', 'project-launch', 'sec-progress', 'Tổng hợp kết quả phỏng vấn người dùng', 0, 'HIGH', 'IN_PROGRESS', ['label-research'], 'user-linh', 'Tóm tắt insight từ 12 buổi phỏng vấn và đề xuất ba hướng ưu tiên cho sprint tiếp theo.'),
  task('task-wireframe', 'project-launch', 'sec-review', 'Duyệt wireframe onboarding mới', 0, 'URGENT', 'TODO', ['label-design', 'label-urgent'], 'user-minh'),
  task('task-analytics', 'project-launch', 'sec-ideas', 'Xác định bộ chỉ số activation', 1, 'MEDIUM', 'TODO', ['label-research'], 'user-an'),
  task('task-api', 'project-launch', 'sec-progress', 'Hoàn thiện API đồng bộ lịch', 2, 'HIGH', 'IN_PROGRESS', ['label-dev'], 'user-minh'),
  task('task-copy', 'project-launch', 'sec-ideas', 'Viết microcopy cho empty states', 4, 'LOW', 'TODO', ['label-design'], 'user-an'),
  task('task-qa', 'project-launch', 'sec-review', 'QA luồng tạo task định kỳ', 6, 'MEDIUM', 'TODO', ['label-dev'], 'user-linh'),
  task('task-navigation', 'project-website', 'sec-design', 'Thiết kế navigation responsive', -1, 'HIGH', 'TODO', ['label-design'], 'user-linh'),
  task('task-landing', 'project-website', 'sec-build', 'Xây landing page beta', 3, 'MEDIUM', 'IN_PROGRESS', ['label-dev'], 'user-minh'),
  task('task-seo', 'project-website', 'sec-build', 'Thiết lập metadata và sitemap', 8, 'LOW', 'TODO', ['label-dev'], 'user-an'),
  task('task-groceries', 'project-personal', null, 'Mua thực phẩm cho tuần mới', 0, 'NONE', 'TODO', ['label-personal']),
  task('task-run', 'project-personal', 'sec-personal', 'Chạy bộ 5 km', 1, 'LOW', 'TODO', ['label-health']),
  task('task-book', 'project-personal', 'sec-personal', 'Đọc xong chương 6', null, 'NONE', 'TODO', ['label-personal']),
  task('task-report', 'project-launch', 'sec-done', 'Báo cáo sprint 18', -2, 'MEDIUM', 'COMPLETED', ['label-research']),
  task('task-content', 'project-campaign', 'sec-campaign', 'Chốt content calendar tháng 8', 2, 'HIGH', 'TODO', ['label-content'], 'user-minh'),
]

const members: WorkspaceMember[] = [
  { id: 'member-linh-product', workspaceId: 'ws-product', userId: 'user-linh', username: 'Linh Nguyễn', email: 'linh@smartly.vn', active: true, role: 'OWNER', joinedAt: isoAt(-90) },
  { id: 'member-minh-product', workspaceId: 'ws-product', userId: 'user-minh', username: 'Minh Trần', email: 'minh@smartly.vn', active: true, role: 'ADMIN', joinedAt: isoAt(-82) },
  { id: 'member-an-product', workspaceId: 'ws-product', userId: 'user-an', username: 'An Lê', email: 'an@smartly.vn', active: true, role: 'MEMBER', joinedAt: isoAt(-47) },
  { id: 'member-linh-personal', workspaceId: 'ws-personal', userId: 'user-linh', username: 'Linh Nguyễn', email: 'linh@smartly.vn', active: true, role: 'OWNER', joinedAt: isoAt(-95) },
  { id: 'member-minh-growth', workspaceId: 'ws-marketing', userId: 'user-minh', username: 'Minh Trần', email: 'minh@smartly.vn', active: true, role: 'OWNER', joinedAt: isoAt(-64) },
  { id: 'member-an-growth', workspaceId: 'ws-marketing', userId: 'user-an', username: 'An Lê', email: 'an@smartly.vn', active: true, role: 'MEMBER', joinedAt: isoAt(-40) },
]

export const makeDemoData = (): DemoData => ({
  users: demoUsers.map((item) => ({ ...item })),
  workspaces: workspaces.map((item) => ({ ...item })),
  members: members.map((item) => ({ ...item })),
  projects: projects.map((item) => ({ ...item })),
  sections: sections.map((item) => ({ ...item })),
  labels: labels.map((item) => ({ ...item })),
  tasks: tasks.map((item) => ({ ...item, labelIds: [...item.labelIds] })),
  checklist: [
    { id: 'check-1', taskId: 'task-research', content: 'Gắn tag các insight chính', completed: true, completedAt: isoAt(-1), sortOrder: 0 },
    { id: 'check-2', taskId: 'task-research', content: 'Nhóm insight theo chủ đề', completed: true, completedAt: isoAt(-1), sortOrder: 1 },
    { id: 'check-3', taskId: 'task-research', content: 'Viết executive summary', completed: false, sortOrder: 2 },
    { id: 'check-4', taskId: 'task-research', content: 'Chia sẻ với product team', completed: false, sortOrder: 3 },
  ],
  comments: [
    { id: 'comment-1', taskId: 'task-research', authorId: 'user-minh', content: 'Mình đã thêm ghi chú từ nhóm khách hàng enterprise vào tài liệu.', createdAt: isoAt(-1, 10) },
    { id: 'comment-2', taskId: 'task-research', authorId: 'user-linh', content: 'Tuyệt, chiều nay mình sẽ tổng hợp nốt phần đề xuất.', createdAt: isoAt(-1, 14) },
  ],
  reminders: [
    { id: 'reminder-1', taskId: 'task-research', recipientId: 'user-linh', remindAt: isoAt(0, 16), timeZone: 'Asia/Ho_Chi_Minh', channel: 'IN_APP', status: 'PENDING' },
  ],
  attachments: [
    { id: 'attachment-1', taskId: 'task-research', uploadedById: 'user-minh', originalFileName: 'user-research-summary.pdf', contentType: 'application/pdf', sizeBytes: 1_840_000, createdAt: isoAt(-1) },
  ],
})

