# Carbon Credit Frontend

Frontend UI cho Carbon Credit Marketplace.

## Quick Start

### 1. Đảm bảo backend đang chạy

Backend services phải được khởi động trước từ `Carbon-Credit-Server`:

```bash
cd ../Carbon-Credit-Server
docker-compose up -d
```

### 2. Chạy Frontend

```bash
docker-compose up -d
```

### 3. Truy cập

- **Frontend**: http://localhost:5173
- **API Gateway**: http://localhost:8222

## Cấu hình

| Port | Service |
|------|---------|
| 5173 | Frontend UI |
| 8222 | API Gateway (backend) |

## Kiến trúc

```
Browser (localhost:5173)
    ↓
Nginx Container (port 5173:80)
    ↓ proxy /api/ requests
Backend API Gateway (host.docker.internal:8222)
```

- Frontend gọi API qua relative path `/api`
- Nginx proxy requests đến backend
- Nginx xử lý CORS để tránh duplicate headers

## Troubleshooting

### Network không tồn tại

Nếu gặp lỗi `network microservices-network not found`:

```bash
# Chạy backend trước để tạo network
cd ../Carbon-Credit-Server
docker-compose up -d

# Sau đó chạy frontend
cd ../Carbon-Credit-UI
docker-compose up -d
```

### Xem logs

```bash
docker logs carbon-frontend -f
```

### Rebuild image

```bash
docker-compose pull
docker-compose up -d
```

## Image Info

- **Repository**: henryngyn2264/carbon-frontend
- **Tag**: 1.0.0, latest
- **Base**: nginx:alpine
