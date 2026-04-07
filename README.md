# 📧 SpringMail - Hệ thống quản lý Email thông minh

SpringMail là ứng dụng quản lý Email hiện đại, hỗ trợ tích hợp Gmail API với khả năng phân loại và xử lý email bằng AI. Dự án sử dụng Spring Boot cho Backend và React cho Frontend.

---

## 🛠 Công nghệ sử dụng

### Backend
- **Spring Boot 4.0.5** / **Java 21**
- **Spring Security & OAuth2 Client** (Google Login)
- **Spring Session Data Redis** (Quản lý phiên đăng nhập)
- **Spring Data MongoDB** (Lưu trữ cache email)
- **Netty Native Transport** (Tối ưu hóa cho MacOS)

### Frontend
- **React (Vite 6.x)**
- **Tailwind CSS** (Styling)
- **Lucide React** (Icons)
- **Axios** (API Client)

---

## 🍎 Cấu hình đặc biệt cho MacOS & Java 21+

Dự án này được tối ưu hóa đặc biệt cho người dùng MacOS (đặc biệt là chip Apple Silicon M1/M2/M3) và Java phiên bản mới nhất:

### 1. Tối ưu phân giải DNS (Netty Native)
Để tránh lỗi DNS chậm hoặc cảnh báo "Can not find MacOSDnsServerAddressStreamProvider", dự án đã tích hợp thư viện native của Netty:
```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-resolver-dns-native-macos</artifactId>
    <classifier>osx-aarch_64</classifier>
    <scope>runtime</scope>
</dependency>
```

### 2. Cấp quyền truy cập Native (Java 21+)
Kể từ Java 21+, việc sử dụng mã máy (native code) yêu cầu xác nhận quyền truy cập. Bạn cần đảm bảo tham số sau được sử dụng khi chạy ứng dụng:
- **Tham số VM:** `--enable-native-access=ALL-UNNAMED`

*Tham số này đã được cấu hình sẵn trong `pom.xml` của Backend. Nếu chạy từ IDE (VS Code/IntelliJ), hãy đảm bảo đã thêm nó vào VM Options của Run Configuration.*

---

## 🚀 Hướng dẫn cài đặt & Chạy

### Yêu cầu hệ thống
- **Java 21** (JDK 21)
- **Node.js** & npm (v18+)
- **Redis Server** (Cổng 6379)
- **MongoDB Server** (Cổng 27017)

### 1. Khởi động Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 2. Khởi động Frontend
```bash
cd frontend
npm install
npm run dev
```

---

## 🔐 Cấu hình Bảo mật & Biến môi trường
Đảm bảo bạn có file `.env` trong thư mục `backend/` (hoặc các biến môi trường hệ thống) với các thông tin sau:
- `GOOGLE_CLIENT_ID`: Client ID từ Google Cloud Console.
- `GOOGLE_CLIENT_SECRET`: Client Secret từ Google Cloud Console.

---

## 📝 Ghi chú về kiến trúc (TH: MacOS DNS Optimization)
Việc giải quyết vấn đề DNS trên MacOS trong dự án này giúp:
- **Tăng tốc độ kết nối:** Giảm thời gian trễ (latency) khi gọi Gmail API và cơ sở dữ liệu.
- **Tính ổn định:** Sử dụng cơ chế phân giải DNS không đồng bộ (non-blocking) thay cho cơ chế mặc định của Java.
- **Log sạch:** Loại bỏ hoàn toàn các cảnh báo Native Access trên Java 21.
