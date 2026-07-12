import { useEffect, useRef, type ButtonHTMLAttributes, type InputHTMLAttributes, type ReactNode } from 'react'
import { cx, initials } from '../lib/utils'

export type IconName =
  | 'activity' | 'admin' | 'archive' | 'arrow-left' | 'arrow-right' | 'bell' | 'board'
  | 'calendar' | 'check' | 'check-circle' | 'chevron-down' | 'chevron-left' | 'chevron-right'
  | 'clock' | 'close' | 'comment' | 'dashboard' | 'download' | 'edit' | 'file' | 'filter'
  | 'flag' | 'folder' | 'grid' | 'hash' | 'home' | 'inbox' | 'label' | 'list' | 'lock'
  | 'logout' | 'mail' | 'menu' | 'moon' | 'more' | 'paperclip' | 'plus' | 'project'
  | 'refresh' | 'repeat' | 'search' | 'settings' | 'sparkles' | 'sun' | 'task' | 'team'
  | 'trash' | 'trend' | 'upload' | 'user' | 'users' | 'warning' | 'workspace'

const iconPaths: Record<IconName, ReactNode> = {
  activity: <><path d="M3 12h4l2-7 4 14 2-7h6" /></>,
  admin: <><path d="M12 3 4.5 6v5.2c0 4.7 3.2 8.8 7.5 9.8 4.3-1 7.5-5.1 7.5-9.8V6L12 3Z"/><path d="M9.5 12 11 13.5l3.5-4"/></>,
  archive: <><path d="M4 7v13h16V7"/><path d="M2.5 3h19v4h-19zM9 11h6"/></>,
  'arrow-left': <><path d="m15 18-6-6 6-6"/></>,
  'arrow-right': <><path d="m9 18 6-6-6-6"/></>,
  bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></>,
  board: <><rect x="3" y="4" width="7" height="16" rx="1"/><rect x="14" y="4" width="7" height="10" rx="1"/></>,
  calendar: <><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18"/></>,
  check: <><path d="m5 12 4 4L19 6"/></>,
  'check-circle': <><circle cx="12" cy="12" r="9"/><path d="m8 12 2.5 2.5L16 9"/></>,
  'chevron-down': <><path d="m6 9 6 6 6-6"/></>,
  'chevron-left': <><path d="m15 18-6-6 6-6"/></>,
  'chevron-right': <><path d="m9 18 6-6-6-6"/></>,
  clock: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
  close: <><path d="m6 6 12 12M18 6 6 18"/></>,
  comment: <><path d="M21 15a4 4 0 0 1-4 4H8l-5 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z"/></>,
  dashboard: <><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></>,
  download: <><path d="M12 3v12m0 0 5-5m-5 5-5-5M4 21h16"/></>,
  edit: <><path d="m4 16-1 5 5-1L19 9l-4-4L4 16Z"/><path d="m13.5 6.5 4 4"/></>,
  file: <><path d="M6 2h8l4 4v16H6z"/><path d="M14 2v5h5"/></>,
  filter: <><path d="M3 5h18l-7 8v6l-4 2v-8z"/></>,
  flag: <><path d="M5 21V4m0 1h12l-2 4 2 4H5"/></>,
  folder: <><path d="M3 6h7l2 2h9v12H3z"/></>,
  grid: <><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></>,
  hash: <><path d="M9 3 7 21M17 3l-2 18M4 9h17M3 15h17"/></>,
  home: <><path d="m3 11 9-8 9 8v10h-6v-7H9v7H3z"/></>,
  inbox: <><path d="M4 4h16l2 12H16l-2 3h-4l-2-3H2z"/><path d="M2 16h6"/></>,
  label: <><path d="M20 13 13 20 3 10V3h7z"/><circle cx="8" cy="8" r="1"/></>,
  list: <><path d="M9 6h12M9 12h12M9 18h12"/><circle cx="4" cy="6" r="1"/><circle cx="4" cy="12" r="1"/><circle cx="4" cy="18" r="1"/></>,
  lock: <><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></>,
  logout: <><path d="M10 4H4v16h6M14 8l4 4-4 4m4-4H9"/></>,
  mail: <><rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3 7 9 6 9-6"/></>,
  menu: <><path d="M4 7h16M4 12h16M4 17h16"/></>,
  moon: <><path d="M20 15.5A8 8 0 0 1 8.5 4 8.5 8.5 0 1 0 20 15.5Z"/></>,
  more: <><circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/></>,
  paperclip: <><path d="m20 12-8 8a6 6 0 0 1-8.5-8.5l9-9a4 4 0 0 1 5.7 5.7l-9 9a2 2 0 1 1-2.8-2.8l8-8"/></>,
  plus: <><path d="M12 5v14M5 12h14"/></>,
  project: <><path d="M4 5h16v14H4z"/><path d="M8 9h8M8 13h5"/></>,
  refresh: <><path d="M20 6v5h-5M4 18v-5h5"/><path d="M18.5 9A7 7 0 0 0 6 6l-2 3m2 6a7 7 0 0 0 12 3l2-3"/></>,
  repeat: <><path d="m17 2 4 4-4 4M3 11V9a3 3 0 0 1 3-3h15M7 22l-4-4 4-4m14-1v2a3 3 0 0 1-3 3H3"/></>,
  search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
  settings: <><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.6 1.6 0 0 0 .3 1.8l.1.1-2.9 2.9-.1-.1a1.6 1.6 0 0 0-1.8-.3 1.6 1.6 0 0 0-1 1.5V21h-4v-.1A1.6 1.6 0 0 0 9 19.4a1.6 1.6 0 0 0-1.8.3l-.1.1-2.9-2.9.1-.1A1.6 1.6 0 0 0 4.6 15a1.6 1.6 0 0 0-1.5-1H3v-4h.1A1.6 1.6 0 0 0 4.6 9a1.6 1.6 0 0 0-.3-1.8l-.1-.1 2.9-2.9.1.1A1.6 1.6 0 0 0 9 4.6a1.6 1.6 0 0 0 1-1.5V3h4v.1a1.6 1.6 0 0 0 1 1.5 1.6 1.6 0 0 0 1.8-.3l.1-.1 2.9 2.9-.1.1a1.6 1.6 0 0 0-.3 1.8 1.6 1.6 0 0 0 1.5 1h.1v4h-.1a1.6 1.6 0 0 0-1.5 1Z"/></>,
  sparkles: <><path d="m12 3 1.2 3.8L17 8l-3.8 1.2L12 13l-1.2-3.8L7 8l3.8-1.2zM5 14l.8 2.2L8 17l-2.2.8L5 20l-.8-2.2L2 17l2.2-.8zM19 14l.8 2.2L22 17l-2.2.8L19 20l-.8-2.2L16 17l2.2-.8z"/></>,
  sun: <><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></>,
  task: <><rect x="3" y="4" width="18" height="16" rx="2"/><path d="m7 10 2 2 4-4M7 16h10"/></>,
  team: <><circle cx="9" cy="8" r="3"/><circle cx="17" cy="9" r="2"/><path d="M3 20c0-4 2.5-6 6-6s6 2 6 6M15 15c3 0 5 1.7 5 5"/></>,
  trash: <><path d="M4 7h16M9 3h6l1 4H8zM6 7l1 14h10l1-14M10 11v6M14 11v6"/></>,
  trend: <><path d="m3 17 6-6 4 4 8-9"/><path d="M15 6h6v6"/></>,
  upload: <><path d="M12 17V5m0 0L7 10m5-5 5 5M4 21h16"/></>,
  user: <><circle cx="12" cy="8" r="4"/><path d="M4 21c0-5 3-8 8-8s8 3 8 8"/></>,
  users: <><circle cx="9" cy="8" r="3"/><circle cx="17" cy="9" r="2"/><path d="M3 20c0-4 2.5-6 6-6s6 2 6 6M15 15c3 0 5 1.7 5 5"/></>,
  warning: <><path d="M12 3 2.5 20h19z"/><path d="M12 9v4M12 17h.01"/></>,
  workspace: <><path d="M3 4h8v7H3zM13 4h8v4h-8zM13 10h8v10h-8zM3 13h8v7H3z"/></>,
}

export function Icon({ name, size = 18, className }: { name: IconName; size?: number; className?: string }) {
  return (
    <svg className={cx('icon', className)} width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {iconPaths[name]}
    </svg>
  )
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'soft'
  size?: 'sm' | 'md' | 'lg' | 'icon'
  icon?: IconName
  loading?: boolean
}

export function Button({ variant = 'primary', size = 'md', icon, loading, className, children, disabled, ...props }: ButtonProps) {
  return (
    <button className={cx('button', `button--${variant}`, `button--${size}`, className)} disabled={disabled || loading} {...props}>
      {loading ? <span className="spinner" /> : icon ? <Icon name={icon} size={size === 'sm' ? 15 : 17} /> : null}
      {children}
    </button>
  )
}

export function IconButton({ label, icon, ...props }: Omit<ButtonProps, 'children' | 'size'> & { label: string; icon: IconName }) {
  return <Button size="icon" variant={props.variant || 'ghost'} icon={icon} aria-label={label} title={label} {...props} />
}

export function TextInput({ icon, className, ...props }: InputHTMLAttributes<HTMLInputElement> & { icon?: IconName }) {
  return (
    <label className={cx('input-wrap', className)}>
      {icon && <Icon name={icon} size={17} />}
      <input {...props} />
    </label>
  )
}

export function Avatar({ name, size = 'md', color }: { name?: string; size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'; color?: string }) {
  return <span className={cx('avatar', `avatar--${size}`)} style={color ? { background: color } : undefined}>{initials(name)}</span>
}

export function Badge({ children, tone = 'neutral', dot }: { children: ReactNode; tone?: 'neutral' | 'purple' | 'green' | 'red' | 'amber' | 'blue'; dot?: boolean }) {
  return <span className={cx('badge', `badge--${tone}`)}>{dot && <span className="badge__dot" />}{children}</span>
}

export function Progress({ value, color }: { value: number; color?: string }) {
  return <div className="progress"><span style={{ width: `${Math.min(100, Math.max(0, value))}%`, background: color }} /></div>
}

export function Modal({ open, onClose, title, eyebrow, children, width = 'md', footer }: { open: boolean; onClose: () => void; title: string; eyebrow?: string; children: ReactNode; width?: 'sm' | 'md' | 'lg'; footer?: ReactNode }) {
  const dialogRef = useRef<HTMLDivElement>(null)
  useEffect(() => {
    if (!open) return
    const previous = document.activeElement as HTMLElement
    const onKey = (event: KeyboardEvent) => event.key === 'Escape' && onClose()
    document.addEventListener('keydown', onKey)
    document.body.classList.add('no-scroll')
    window.setTimeout(() => dialogRef.current?.querySelector<HTMLElement>('input, button, textarea, select')?.focus(), 0)
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.classList.remove('no-scroll')
      previous?.focus()
    }
  }, [open, onClose])
  if (!open) return null
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className={cx('modal', `modal--${width}`)} role="dialog" aria-modal="true" aria-labelledby="modal-title" ref={dialogRef}>
        <header className="modal__header">
          <div>{eyebrow && <span className="eyebrow">{eyebrow}</span>}<h2 id="modal-title">{title}</h2></div>
          <IconButton icon="close" label="Đóng" onClick={onClose} />
        </header>
        <div className="modal__body">{children}</div>
        {footer && <footer className="modal__footer">{footer}</footer>}
      </div>
    </div>
  )
}

export function EmptyState({ icon = 'task', title, description, action }: { icon?: IconName; title: string; description: string; action?: ReactNode }) {
  return (
    <div className="empty-state">
      <span className="empty-state__icon"><Icon name={icon} size={28} /></span>
      <h3>{title}</h3>
      <p>{description}</p>
      {action}
    </div>
  )
}

export function Skeleton({ className }: { className?: string }) {
  return <span className={cx('skeleton', className)} />
}

