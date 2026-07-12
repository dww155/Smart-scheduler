import { useState, type FormEvent } from 'react'
import { useApp } from '../context/AppContext'
import { ApiError, getAccessToken } from '../lib/api'
import { navigate } from '../lib/router'
import { cx, decodeRoles } from '../lib/utils'
import { Brand, Link } from '../components/shells'
import { Avatar, Badge, Button, Icon, IconButton, Progress } from '../components/ui'

export function LandingPage() {
  const { isAuthenticated, isAdmin, enterDemo } = useApp()
  return (
    <div className="landing">
      <header className="landing-nav"><Brand /><nav><a href="#features">Tính năng</a><a href="#workflow">Quy trình</a><a href="#teams">Cho đội nhóm</a></nav><div>{isAuthenticated ? <Button onClick={() => navigate(isAdmin ? '/admin/overview' : '/app/today')}>Mở ứng dụng</Button> : <><Link to="/login" className="nav-login">Đăng nhập</Link><Button onClick={() => navigate('/register')}>Bắt đầu miễn phí</Button></>}</div></header>
      <main>
        <section className="landing-hero">
          <div className="hero-glow hero-glow--one" /><div className="hero-glow hero-glow--two" />
          <div className="hero-copy"><Badge tone="purple"><Icon name="sparkles" size={13} />Làm việc thông minh hơn, mỗi ngày</Badge><h1>Biến kế hoạch thành<br /><em>kết quả rõ ràng.</em></h1><p>Smartly giúp bạn sắp xếp công việc, cộng tác cùng đội nhóm và tập trung vào những điều thật sự quan trọng.</p><div className="hero-actions"><Button size="lg" icon="arrow-right" onClick={() => navigate('/register')}>Bắt đầu miễn phí</Button><Button size="lg" variant="secondary" icon="sparkles" onClick={() => { enterDemo('user'); navigate('/app/today') }}>Khám phá bản demo</Button></div><small><Icon name="check" size={14} />Không cần thẻ tín dụng <span /> Thiết lập trong 2 phút</small></div>
          <div className="hero-product">
            <div className="floating-note floating-note--left"><span><Icon name="check" size={13} /></span><div><strong>Đã hoàn thành!</strong><small>12 công việc hôm nay</small></div></div>
            <div className="floating-note floating-note--right"><Avatar name="Linh" size="sm" /><div><strong>Linh đã bình luận</strong><small>“Mình đã cập nhật bản thiết kế”</small></div></div>
            <div className="product-window"><header><div className="window-dots"><i /><i /><i /></div><span>app.smartly.vn</span><Icon name="lock" size={12} /></header><div className="product-body"><aside><Brand compact /><div className="fake-workspace"><span>N</span><strong>Nova Product</strong><Icon name="chevron-down" size={11} /></div><button className="fake-add"><Icon name="plus" size={12} />Thêm công việc</button>{[['sun', 'Hôm nay', '5'], ['inbox', 'Hộp thư', '3'], ['calendar', 'Sắp tới', '']].map(([icon, label, count], index) => <div className={cx('fake-nav', index === 0 && 'active')} key={label}><Icon name={icon as 'sun'} size={12} /><span>{label}</span><em>{count}</em></div>)}<small>DỰ ÁN</small>{[['#6d5dfc', 'Ra mắt ứng dụng 2.0'], ['#e85d75', 'Website mới'], ['#1fa67a', 'Product Research']].map(([color, label]) => <div className="fake-project" key={label}><i style={{ color }}>#</i>{label}</div>)}</aside><section><header><div><small>THỨ HAI, 13 THÁNG 7</small><h3>Chào buổi sáng, Linh! 👋</h3><p>Tập trung vào điều quan trọng nhất hôm nay.</p></div><button><Icon name="plus" size={12} />Thêm công việc</button></header><div className="fake-progress"><div className="mini-ring">68%</div><div><small>Tiến độ hôm nay</small><strong>8 trong 12 công việc đã xong</strong><Progress value={68} /></div><span><b>2</b> Quá hạn</span><span><b>5</b> Hôm nay</span></div><div className="fake-task-group"><header><span>Hôm nay</span><i />5</header>{[
              ['Tổng hợp kết quả phỏng vấn người dùng', 'Research', 'Hôm nay', '#14b8a6'], ['Duyệt wireframe onboarding mới', 'Design', 'Hôm nay', '#a855f7'], ['Hoàn thiện API đồng bộ lịch', 'Development', '17:30', '#3b82f6'], ['Viết microcopy cho empty states', 'Design', 'Ngày mai', '#a855f7'],
            ].map(([title, label, due, color], index) => <article key={title}><span className={index === 3 ? 'done' : ''}>{index === 3 && <Icon name="check" size={8} />}</span><div><strong>{title}</strong><small><i style={{ background: color }} />{label} · {due}</small></div><Avatar name={index % 2 ? 'Minh' : 'Linh'} size="xs" /></article>)}</div></section></div></div>
          </div>
        </section>
        <section className="logo-strip"><span>Được tin dùng để tổ chức công việc mỗi ngày</span><div><strong>ORBIT</strong><strong>Northstar</strong><strong>Vertex</strong><strong>monocle</strong><strong>HORIZON</strong></div></section>
        <section className="landing-features" id="features"><div className="section-heading"><span className="eyebrow">MỌI THỨ BẠN CẦN</span><h2>Một nơi cho mọi công việc.</h2><p>Từ ý tưởng nhỏ đến dự án lớn, Smartly giúp mọi thứ luôn trong tầm kiểm soát.</p></div><div className="feature-grid"><Feature icon="sparkles" title="Tập trung mỗi ngày" copy="Today và Upcoming tự động gom đúng việc bạn cần làm, đúng thời điểm." color="purple" /><Feature icon="board" title="Nhiều góc nhìn" copy="Chuyển linh hoạt giữa danh sách, Kanban và lịch mà không mất dữ liệu." color="blue" /><Feature icon="users" title="Cộng tác rõ ràng" copy="Giao việc, checklist, bình luận và file đính kèm trong cùng một luồng." color="green" /><Feature icon="bell" title="Không bỏ lỡ deadline" copy="Đặt lịch nhắc và công việc lặp lại theo cách phù hợp với nhịp làm việc." color="amber" /><Feature icon="label" title="Tổ chức theo cách bạn" copy="Nhãn, section, mức ưu tiên và bộ lọc giúp tìm việc trong vài giây." color="red" /><Feature icon="admin" title="Quản trị minh bạch" copy="Admin console theo dõi người dùng, workspace và sức khỏe hệ thống." color="purple" /></div></section>
        <section className="landing-workflow" id="workflow"><div className="workflow-card"><div><span className="eyebrow">QUY TRÌNH NHẸ NHÀNG</span><h2>Thu thập. Sắp xếp.<br />Hoàn thành.</h2><p>Ghi nhanh mọi ý tưởng vào Inbox, sắp xếp chúng vào dự án và bắt đầu ngày mới với một kế hoạch rõ ràng.</p><ol><li><span>1</span><div><strong>Ghi lại trong vài giây</strong><small>Quick Add hoạt động ở mọi màn hình với phím Q.</small></div></li><li><span>2</span><div><strong>Sắp xếp theo ngữ cảnh</strong><small>Project, section, nhãn, ưu tiên và người phụ trách.</small></div></li><li><span>3</span><div><strong>Tập trung và hoàn thành</strong><small>Today chỉ đưa ra những gì cần sự chú ý ngay lúc này.</small></div></li></ol><Button variant="secondary" icon="arrow-right" onClick={() => navigate('/register')}>Bắt đầu quy trình của bạn</Button></div><div className="workflow-visual"><div className="workflow-column"><header><span className="kanban-dot" />Ý tưởng <small>3</small></header><article><Badge tone="purple">Research</Badge><strong>Phân tích phản hồi beta</strong><small><Icon name="calendar" size={12} />15 tháng 7</small></article><article><Badge tone="amber">Content</Badge><strong>Viết hướng dẫn onboarding</strong><div className="avatar-stack"><Avatar name="An" size="xs" /><Avatar name="Linh" size="xs" /></div></article></div><div className="workflow-column workflow-column--focus"><header><span className="kanban-dot" />Đang làm <small>2</small></header><article><Badge tone="blue">Development</Badge><strong>Tích hợp calendar API</strong><Progress value={72} /><small>3/4 checklist</small></article><article><Badge tone="red">Khẩn cấp</Badge><strong>Fix luồng đăng nhập mobile</strong><small><Icon name="clock" size={12} />Hôm nay, 16:00</small></article></div><div className="workflow-column"><header><span className="kanban-dot" />Hoàn thành <small>4</small></header><article className="done"><Icon name="check-circle" size={17} /><strong>Chốt design system</strong></article><article className="done"><Icon name="check-circle" size={17} /><strong>Setup CI pipeline</strong></article></div></div></div></section>
        <section className="team-cta" id="teams"><div><span className="eyebrow">SẴN SÀNG BẮT ĐẦU?</span><h2>Một ngày rõ ràng hơn<br />bắt đầu từ hôm nay.</h2><p>Thử Smartly miễn phí và biến danh sách dài thành những bước tiến nhỏ, đều đặn.</p><div><Button size="lg" variant="secondary" onClick={() => navigate('/register')}>Tạo tài khoản miễn phí</Button><Button size="lg" variant="ghost" onClick={() => { enterDemo('admin'); navigate('/admin/overview') }}>Xem demo Admin</Button></div></div></section>
      </main>
      <footer className="landing-footer"><Brand /><p>© 2026 Smartly. Làm việc nhẹ nhàng, tiến xa mỗi ngày.</p><nav><a href="#features">Tính năng</a><a href="#">Bảo mật</a><a href="#">Trợ giúp</a></nav></footer>
    </div>
  )
}

function Feature({ icon, title, copy, color }: { icon: 'sparkles' | 'board' | 'users' | 'bell' | 'label' | 'admin'; title: string; copy: string; color: string }) { return <article className="feature-card"><span className={`feature-icon feature-icon--${color}`}><Icon name={icon} size={22} /></span><h3>{title}</h3><p>{copy}</p><a href="#workflow">Tìm hiểu thêm <Icon name="arrow-right" size={14} /></a></article> }

export function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const app = useApp()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [accepted, setAccepted] = useState(false)
  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    if (mode === 'register' && password !== confirmPassword) { setError('Mật khẩu xác nhận không khớp.'); return }
    try {
      if (mode === 'login') await app.login(username.trim(), password)
      else await app.register(username.trim(), email.trim(), password)
      navigate(decodeRoles(getAccessToken()).includes('admin') ? '/admin/overview' : '/app/today', true)
    } catch (err) {
      if (err instanceof ApiError && err.status === 0) setError('Không thể kết nối backend. Hãy kiểm tra server đang chạy ở cổng 8080.')
      else setError(err instanceof Error ? err.message : 'Đã có lỗi xảy ra. Vui lòng thử lại.')
    }
  }
  return (
    <div className="auth-page">
      <div className="auth-brand"><Brand /><Link to="/">Về trang chủ</Link></div>
      <div className="auth-decoration"><span /><span /><span /></div>
      <main className="auth-card">
        <header><span className="auth-icon"><Icon name={mode === 'login' ? 'lock' : 'sparkles'} size={23} /></span><h1>{mode === 'login' ? 'Chào mừng trở lại' : 'Bắt đầu với Smartly'}</h1><p>{mode === 'login' ? 'Đăng nhập để tiếp tục ngày làm việc của bạn.' : 'Tạo không gian để biến ý tưởng thành tiến độ.'}</p></header>
        {error && <div className="auth-error"><Icon name="warning" size={17} /><span>{error}</span><IconButton icon="close" label="Đóng lỗi" onClick={() => setError('')} /></div>}
        <form onSubmit={submit}>
          <label><span>Tên đăng nhập</span><div className="auth-input"><Icon name="user" size={17} /><input value={username} onChange={(event) => setUsername(event.target.value)} minLength={3} maxLength={50} autoComplete="username" placeholder="Nhập tên đăng nhập" required /></div></label>
          {mode === 'register' && <label><span>Email</span><div className="auth-input"><Icon name="mail" size={17} /><input value={email} onChange={(event) => setEmail(event.target.value)} type="email" autoComplete="email" placeholder="ban@congty.vn" required /></div></label>}
          <label><span>Mật khẩu</span><div className="auth-input"><Icon name="lock" size={17} /><input value={password} onChange={(event) => setPassword(event.target.value)} type={showPassword ? 'text' : 'password'} autoComplete={mode === 'login' ? 'current-password' : 'new-password'} placeholder="Ít nhất 8 ký tự" minLength={8} required /><button type="button" onClick={() => setShowPassword((value) => !value)}>{showPassword ? 'Ẩn' : 'Hiện'}</button></div></label>
          {mode === 'register' && <><label><span>Xác nhận mật khẩu</span><div className="auth-input"><Icon name="lock" size={17} /><input value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} type={showPassword ? 'text' : 'password'} autoComplete="new-password" placeholder="Nhập lại mật khẩu" minLength={8} required /></div></label><div className="password-rules"><span className={password.length >= 8 ? 'is-ok' : ''}><Icon name="check" size={11} />Từ 8 ký tự</span><span className={/[A-Z]/.test(password) && /[a-z]/.test(password) ? 'is-ok' : ''}><Icon name="check" size={11} />Có chữ hoa & thường</span><span className={/\d/.test(password) && /[@$!%*?&]/.test(password) ? 'is-ok' : ''}><Icon name="check" size={11} />Có số & ký tự đặc biệt</span></div></>}
          {mode === 'login' ? <div className="auth-options"><label><input type="checkbox" /><span />Ghi nhớ đăng nhập</label><Link to="/forgot-password">Quên mật khẩu?</Link></div> : <label className="auth-terms"><input type="checkbox" checked={accepted} onChange={(event) => setAccepted(event.target.checked)} /><span />Tôi đồng ý với <a href="#">Điều khoản</a> và <a href="#">Chính sách bảo mật</a>.</label>}
          <Button size="lg" type="submit" loading={app.loading} disabled={mode === 'register' && (!accepted || password !== confirmPassword)}>{mode === 'login' ? 'Đăng nhập' : 'Tạo tài khoản'}<Icon name="arrow-right" size={17} /></Button>
        </form>
        <div className="auth-divider"><span>hoặc</span></div>
        <div className="demo-buttons"><Button variant="secondary" onClick={() => { app.enterDemo('user'); navigate('/app/today') }}><Icon name="sparkles" size={17} />Dùng thử với dữ liệu demo</Button>{mode === 'login' && <Button variant="ghost" onClick={() => { app.enterDemo('admin'); navigate('/admin/overview') }}>Demo Admin</Button>}</div>
        <footer>{mode === 'login' ? <>Chưa có tài khoản? <Link to="/register">Đăng ký miễn phí</Link></> : <>Đã có tài khoản? <Link to="/login">Đăng nhập</Link></>}</footer>
      </main>
      <p className="auth-bottom-note"><Icon name="lock" size={13} />Thông tin đăng nhập được bảo vệ bằng kết nối an toàn.</p>
    </div>
  )
}

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  return <div className="auth-page"><div className="auth-brand"><Brand /><Link to="/login">Quay lại đăng nhập</Link></div><main className="auth-card auth-card--small">{sent ? <div className="auth-success"><span><Icon name="mail" size={25} /></span><h1>Kiểm tra hộp thư</h1><p>Nếu tài khoản <strong>{email}</strong> tồn tại, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu.</p><div className="inline-notice inline-notice--amber"><Icon name="warning" size={17} /><p>Backend reset password chưa được triển khai; đây là màn hình sẵn sàng cho endpoint tương ứng.</p></div><Button onClick={() => navigate('/login')}>Về trang đăng nhập</Button></div> : <><header><span className="auth-icon"><Icon name="mail" size={23} /></span><h1>Quên mật khẩu?</h1><p>Nhập email, chúng tôi sẽ gửi hướng dẫn khôi phục tài khoản.</p></header><form onSubmit={(event) => { event.preventDefault(); setSent(true) }}><label><span>Email</span><div className="auth-input"><Icon name="mail" size={17} /><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="ban@congty.vn" required autoFocus /></div></label><Button size="lg" type="submit">Gửi hướng dẫn</Button></form><footer><Link to="/login"><Icon name="arrow-left" size={14} />Quay lại đăng nhập</Link></footer></>}</main></div>
}
