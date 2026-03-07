# 🌿 Carbon Credit Marketplace - Docker Deployment

Hướng dẫn triển khai dự án Carbon Credit Marketplace bằng Docker.

## 📋 Mục lục

- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Cấu trúc thư mục](#-cấu-trúc-thư-mục)
- [Hướng dẫn cài đặt](#-hướng-dẫn-cài-đặt)
- [Danh sách Services](#-danh-sách-services)
- [Truy cập ứng dụng](#-truy-cập-ứng-dụng)
- [Quản lý & Monitoring](#-quản-lý--monitoring)
- [Troubleshooting](#-troubleshooting)

---

## 💻 Yêu cầu hệ thống

| Thành phần | Yêu cầu tối thiểu |
|------------|-------------------|
| **Docker** | v20.10+ |
| **Docker Compose** | v2.0+ |
| **RAM** | 8GB (khuyến nghị 16GB) |
| **Disk** | 10GB trống |
| **OS** | Windows 10/11, macOS, Linux |

### Kiểm tra Docker

```bash
docker --version
docker-compose --version
```

---

## 🏗 Kiến trúc hệ thống

```
                                    ┌─────────────────┐
                                    │   Frontend UI   │
                                    │   (Port 5173)   │
                                    └────────┬────────┘
                                             │
                                    ┌────────▼────────┐
                                    │   API Gateway   │
                                    │   (Port 8222)   │
                                    └────────┬────────┘
                                             │
        ┌────────────────────────────────────┼────────────────────────────────────┐
        │                                    │                                    │
┌───────▼───────┐  ┌───────▼───────┐  ┌──────▼──────┐  ┌───────▼───────┐  ┌──────▼──────┐
│ User Service  │  │Vehicle Service│  │Wallet Service│  │Market Service │  │Transaction  │
│  (Port 8081)  │  │  (Port 8082)  │  │ (Port 8084) │  │  (Port 8086)  │  │   Service   │
└───────────────┘  └───────────────┘  └─────────────┘  └───────────────┘  │ (Port 8087) │
                                                                          └─────────────┘
        │                    │                 │                │                │
        └────────────────────┴─────────────────┴────────────────┴────────────────┘
                                             │
              ┌──────────────────────────────┼──────────────────────────────┐
              │                              │                              │
     ┌────────▼────────┐           ┌─────────▼─────────┐          ┌────────▼────────┐
     │   PostgreSQL    │           │      Kafka        │          │      Redis      │
     │   (Port 5432)   │           │   (Port 9092)     │          │   (Port 6379)   │
     └─────────────────┘           └───────────────────┘          └─────────────────┘
```

---

## 📁 Cấu trúc thư mục

```
cc-marketplace-docker/
├── Carbon-Credit-Server/          # Backend Services
│   ├── docker-compose.yml         # Docker compose cho backend
│   ├── config-server/
│   │   └── src/main/resources/
│   │       └── configurations/    # ⚠️ CẦN COPY CONFIG VÀO ĐÂY
│   ├── prometheus/
│   │   ├── prometheus.yml         # Cấu hình Prometheus
│   │   └── rules/                 # Alert rules
│   └── grafana/                   # Grafana plugins & data
│
├── Carbon-Credit-UI/              # Frontend
│   ├── docker-compose.yml         # Docker compose cho frontend
│   └── README.md
│
└── README.md                      # File này
```

---

## 🚀 Hướng dẫn cài đặt

### Bước 1: Clone/Download project

```bash
# Nếu chưa có project
git clone https://github.com/bangnguyen2264/CC-MarkertPlace.git
cd cc-marketplace-docker
```
### Bước 2: Khởi động Backend Services

```bash
cd Carbon-Credit-Server

# Khởi động tất cả services
docker-compose up -d

# Hoặc xem logs trong khi khởi động
docker-compose up
```

**⏳ Thời gian khởi động:** ~3-5 phút (do health checks và dependencies)

### Bước 3: Kiểm tra Backend đã sẵn sàng

```bash
# Kiểm tra tất cả containers đang chạy
docker-compose ps

# Kiểm tra health của các services
docker ps --format "table {{.Names}}\t{{.Status}}"
```

Đợi cho đến khi tất cả services hiển thị `healthy`.

Truy cập vào http://localhost:8222/swagger-ui/index.html để xem api doc

### Bước 5: Khởi động Frontend

```bash
cd ../Carbon-Credit-UI

# Khởi động frontend
docker-compose up -d
```

### Bước 6: Xác nhận hoạt động

```bash
# Kiểm tra tất cả containers
docker ps

# Hoặc kiểm tra từng compose
cd ../Carbon-Credit-Server && docker-compose ps
cd ../Carbon-Credit-UI && docker-compose ps
```

---

## 📦 Danh sách Services

### Infrastructure Services

| Service | Port | Mô tả |
|---------|------|-------|
| **Config Server** | 8888 | Quản lý cấu hình tập trung |
| **Eureka Server** | 8761 | Service Discovery |
| **API Gateway** | 8222 | API Gateway & Load Balancer |
| **PostgreSQL** | 5432 | Database chính |
| **Redis** | 6379 | Cache & Session |
| **Kafka** | 9092 | Message Broker |
| **Zookeeper** | 2181 | Kafka Coordination |

### Business Services

| Service | Port | Mô tả |
|---------|------|-------|
| **User Service** | 8081 | Quản lý người dùng & xác thực |
| **Vehicle Service** | 8082 | Quản lý phương tiện |
| **Verification Service** | 8083 | Xác minh thông tin |
| **Wallet Service** | 8084 | Quản lý ví & tín chỉ carbon |
| **Media Service** | 8085 | Quản lý file/media |
| **Market Service** | 8086 | Sàn giao dịch carbon |
| **Transaction Service** | 8087 | Xử lý giao dịch & thanh toán |

### Monitoring Services

| Service | Port | Mô tả |
|---------|------|-------|
| **Prometheus** | 9495 | Metrics collection |
| **Grafana** | 3000 | Monitoring Dashboard |
| **Ngrok** | 4040 | Tunnel cho VNPay callback |

### Frontend

| Service | Port | Mô tả |
|---------|------|-------|
| **Frontend UI** | 5173 | Giao diện người dùng |

---

## 🌐 Truy cập ứng dụng

| Ứng dụng | URL | Tài khoản mặc định |
|----------|-----|-------------------|
| **Frontend** | http://localhost:5173 | - |
| **API Gateway** | http://localhost:8222 | - |
| **Swagger UI** | http://localhost:8222/swagger-ui.html | - |
| **Eureka Dashboard** | http://localhost:8761 | - |
| **Grafana** | http://localhost:3000 | admin / password |
| **Prometheus** | http://localhost:9495 | - |
| **Ngrok Inspector** | http://localhost:4040 | - |

---

## 📊 Quản lý & Monitoring

### Xem logs

```bash
# Logs của tất cả services
cd Carbon-Credit-Server
docker-compose logs -f

# Logs của service cụ thể
docker-compose logs -f user-service
docker-compose logs -f api-gateway

# Logs của frontend
cd ../Carbon-Credit-UI
docker-compose logs -f
```

### Restart services

```bash
# Restart một service
docker-compose restart user-service

# Restart tất cả
docker-compose restart
```

### Dừng services

```bash
# Dừng tất cả (giữ data)
docker-compose stop

# Dừng và xóa containers (giữ volumes/data)
docker-compose down

# Dừng và xóa tất cả (bao gồm data) ⚠️ CẢNH BÁO
docker-compose down -v
```

### Kiểm tra resource usage

```bash
docker stats
```

---

## 🔧 Troubleshooting

### 1. Service không khởi động được

```bash
# Xem logs chi tiết
docker-compose logs <service-name>

# Ví dụ
docker-compose logs user-service
```

### 2. Lỗi "network microservices-network not found"

```bash
# Chạy backend trước để tạo network
cd Carbon-Credit-Server
docker-compose up -d

# Sau đó mới chạy frontend
cd ../Carbon-Credit-UI
docker-compose up -d
```

### 3. Config Server không tìm thấy configuration

Đảm bảo đã copy các file `.yml` vào:
```
Carbon-Credit-Server/config-server/src/main/resources/configurations/
```

### 4. Kafka không healthy

```bash
# Restart Kafka và Zookeeper
docker-compose restart zookeeper kafka

# Đợi 1-2 phút và kiểm tra
docker-compose ps kafka
```

### 5. Database connection refused

```bash
# Kiểm tra PostgreSQL đang chạy
docker-compose ps postgres

# Xem logs
docker-compose logs postgres
```

### 6. Out of Memory

Tăng RAM cho Docker:
- **Docker Desktop (Windows/Mac)**: Settings → Resources → Memory → 8GB+
- **Linux**: Không giới hạn (sử dụng RAM hệ thống)

### 7. Port đã được sử dụng

```bash
# Kiểm tra port đang sử dụng (Windows)
netstat -ano | findstr :8222

# Kiểm tra port đang sử dụng (Linux/Mac)
lsof -i :8222

# Dừng process hoặc đổi port trong docker-compose.yml
```

### 8. Reset toàn bộ

```bash
# Xóa tất cả containers và volumes
cd Carbon-Credit-Server
docker-compose down -v

cd ../Carbon-Credit-UI
docker-compose down

# Xóa images (nếu cần)
docker rmi $(docker images -q henryngyn2264/*)

# Khởi động lại từ đầu
cd ../Carbon-Credit-Server
docker-compose up -d
```

---

## 📝 Ghi chú

- **Thứ tự khởi động**: Backend → Frontend
- **Health checks**: Các services có health check, đợi ~3-5 phút để tất cả sẵn sàng
- **Data persistence**: Data được lưu trong Docker volumes (`postgres_data`, `redis_data`, `prometheus-data`)
- **Ngrok**: Được sử dụng để tạo public URL cho VNPay callback

---

## 🆘 Hỗ trợ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra logs của service gặp lỗi
2. Đảm bảo đã copy đầy đủ configuration files
3. Kiểm tra Docker có đủ resources (RAM, CPU)

---
# Carbon Credit Marketplace for EV Owners - Nền tảng giao dịch tín chỉ carbon cho chủ sở hữu xe điện

> **Lưu ý:** Repository này chỉ chứa mã nguồn Backend. Vui lòng truy cập [tại đây](https://github.com/bangnguyen2264/cc-market-ui.git) để xem mã nguồn ReactJS UI.
**Version**: 1.1.2  
**Last Updated**: November 2025
