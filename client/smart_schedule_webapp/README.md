# Smartly Web App

Frontend React/TypeScript cho Smart Scheduler. Ứng dụng gồm landing page, xác thực, toàn bộ không gian làm việc của user và Admin Console.

## Chạy local

```bash
npm install
npm run dev
```

Vite chạy tại `http://localhost:5173` và proxy `/smart-scheduler` sang backend `http://localhost:8080`.

Nếu frontend/backend chạy trên domain khác, đặt:

```bash
VITE_API_URL=http://localhost:8080/smart-scheduler
```

## Chế độ demo

Không cần backend hoặc PostgreSQL:

- Mở `/demo/user` để xem app user.
- Mở `/demo/admin` để xem Admin Console.
- Có thể vào từ nút **Khám phá bản demo** trên landing/login.

Dữ liệu demo được lưu trong `localStorage`, nên các thao tác tạo/sửa/xóa vẫn hoạt động qua lần refresh trang.

## Các màn hình chính

- User: Today, Inbox, Upcoming, All tasks, Completed, Calendar, Search, Projects (list/board/calendar), Labels, Workspace members/settings, Profile/preferences/notifications/security.
- Task drawer: trạng thái, hạn, ưu tiên, section, assignee, recurrence, label, checklist, subtask, reminder, attachment và comment.
- Admin: dashboard KPI, users, workspaces, project/task data và system health/settings.

## Kiểm tra

```bash
npm run lint
npm run build
```
