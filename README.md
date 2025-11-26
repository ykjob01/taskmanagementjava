# Task Management System

Hệ thống quản lý công việc (task) với xác thực JWT và phân quyền người dùng. Dự án được containerized hoàn toàn với Docker, chỉ cần 1 lệnh để chạy toàn bộ hệ thống.

## Công nghệ sử dụng

### Backend
- Spring Boot 3.2.0
- Java 17
- Spring Security + JWT
- Spring Data JPA
- SQL Server Driver
- Maven

### Frontend
- Vue.js 3
- Vite
- Axios
- Vue Router
- Vanilla CSS

### Database
- Microsoft SQL Server 2022

### DevOps
- Docker
- Docker Compose
- Multi-stage builds

## Tính năng

### Xác thực & Phân quyền
- Đăng ký tài khoản mới
- Đăng nhập với JWT token
- Mã hóa mật khẩu BCrypt
- Token hết hạn sau 24 giờ
- Auto logout khi token expired
- 2 vai trò: USER và ADMIN

### Quản lý Task
- Tạo, xem, cập nhật, xóa task
- Đánh dấu hoàn thành
- Phân trang (pagination)
- Sắp xếp theo ngày tạo hoặc deadline
- Lọc theo trạng thái (PENDING/COMPLETED)

### Phân quyền
- USER: Chỉ quản lý tasks của chính mình
- ADMIN: Xem tất cả tasks của mọi user

## Cài đặt và chạy

### Yêu cầu
- Docker Desktop
- 4GB RAM khả dụng
- 5GB disk space

### Khởi động hệ thống

```bash
docker compose up -d
```

Lệnh này sẽ:
1. Build backend Spring Boot
2. Build frontend Vue.js
3. Khởi động SQL Server
4. Tự động tạo database
5. Tự động tạo admin user

### Truy cập

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api
- Database: localhost:1433

### Tài khoản mặc định

```
Username: admin
Password: admin123
Role: ADMIN
```

### Dừng hệ thống

```bash
docker compose down
```

### Xóa toàn bộ dữ liệu

```bash
docker compose down -v
```

## Cấu trúc dự án

```
taskmanagement/
├── backend/              # Spring Boot API
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/             # Vue.js SPA
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── database/             # SQL Server setup
│   ├── Dockerfile
│   ├── entrypoint.sh
│   └── init.sql
└── docker-compose.yaml   # Orchestration
```

## API Endpoints

### Authentication
```
POST /api/auth/register   # Đăng ký
POST /api/auth/login      # Đăng nhập
```

### Tasks (Authenticated)
```
GET    /api/tasks              # Lấy danh sách (pagination, sort, filter)
GET    /api/tasks/{id}         # Lấy chi tiết task
POST   /api/tasks              # Tạo task mới
PUT    /api/tasks/{id}         # Cập nhật task
DELETE /api/tasks/{id}         # Xóa task
```

### Query Parameters
```
page     - Số trang (default: 0)
size     - Số items mỗi trang (default: 10)
sortBy   - Sắp xếp theo (createdAt, deadline)
status   - Lọc theo trạng thái (PENDING, COMPLETED)
```

## Database Schema

### Table: users
```sql
id          BIGINT PRIMARY KEY AUTO_INCREMENT
username    VARCHAR(255) UNIQUE NOT NULL
email       VARCHAR(255) UNIQUE NOT NULL
password    VARCHAR(255) NOT NULL
role        VARCHAR(50) NOT NULL
created_at  DATETIME
updated_at  DATETIME
```

### Table: tasks
```sql
id          BIGINT PRIMARY KEY AUTO_INCREMENT
title       VARCHAR(255) NOT NULL
description TEXT
status      VARCHAR(50) NOT NULL
deadline    DATETIME
user_id     BIGINT NOT NULL
created_at  DATETIME
updated_at  DATETIME
```

## Kiến trúc

### 3-Tier Architecture
```
Frontend (Vue.js) 
    ↓ HTTP/REST
Backend (Spring Boot)
    ↓ JDBC
Database (SQL Server)
```

### Docker Network
- Network: task-network (bridge)
- Service discovery: Container names
- Health checks: SQL Server

### Security
- Stateless JWT authentication
- BCrypt password hashing (strength 10)
- CORS enabled cho frontend
- Role-based access control
- Global exception handling

## Development

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Database
```bash
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=YourPassword" \
  -p 1433:1433 mcr.microsoft.com/mssql/server:2022-latest
```

## Troubleshooting

### Container không khởi động
```bash
docker compose logs -f
```

### Rebuild toàn bộ
```bash
docker compose down -v
docker compose up -d --build
```

### Kiểm tra database
```bash
docker compose exec sqlserver /opt/mssql-tools/bin/sqlcmd \
  -S localhost -U sa -P '@@@SQLserver' \
  -Q "SELECT name FROM sys.databases"
```

## License

MIT License
