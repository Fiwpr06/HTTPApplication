# 🌐 HTTPApplication — Lab 4: HTTP Web Server & Web Browser (Java 21 + JavaFX 21)

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![JavaFX 21](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg)](https://openjfx.io/)
[![Protocol](https://img.shields.io/badge/Protocol-HTTP%2F1.1%20(RFC%207230)-green.svg)](#)
[![Dual Database](https://img.shields.io/badge/Database-MySQL%20%7C%20SQLite%20Fallback-blue.svg)](#)
[![Tests](https://img.shields.io/badge/Unit%20Tests-9%2F9%20Passing-brightgreen.svg)](#)

Dự án **HTTPApplication** là ứng dụng mạng được thiết kế tối ưu, trực quan và chuyên sâu cho **Bài tập lớn Lab 4 môn Lập trình mạng (LTM)**. Máy chủ phục vụ **3 dịch vụ mạng đa phương tiện cốt lõi** (`.pdf`, `.mp3`, `.mp4`) và hỗ trợ **kết nối thật giữa 2 máy tính hoặc giữa máy tính và điện thoại qua Wi-Fi / LAN**.

---

## 🎯 1. Đáp Ứng Chuẩn Đề Bài Lab 4 Của Giảng Viên

| Tiêu chí | Đề bài yêu cầu | Triển khai thực tế trong Dự án | Trạng thái |
| :--- | :--- | :--- | :---: |
| **Giao thức** | Chuẩn **HTTP** (`HTTP/1.1`) | Tự xây dựng Socket HTTP Engine qua TCP Socket đa luồng (RFC 2616 & RFC 7230) | ✅ Đạt 100% |
| **Dịch vụ Server** | Có **nhiều dịch vụ** (`.pdf`, `.mp3`, `.mp4`) | **[1] Stream Nhạc MP3**, **[2] Stream Video MP4**, **[3] Xem Tài Liệu PDF trực tiếp** | ✅ Đạt chuẩn 100% |
| **Stream Đa phương tiện** | Tua mượt mà video và nhạc | Hỗ trợ **HTTP 206 Partial Content (Range Requests)** cho phép tua seekbar tức thời | ✅ Chuẩn RFC |
| **Kết nối 2 thiết bị** | Chạy mạng thực tế | Hỗ trợ 2 máy tính hoặc điện thoại cùng Wi-Fi kết nối trực tiếp qua IP LAN | ✅ Đạt 100% |
| **Browser (Web Client)** | Tương tự Chrome/Cốc Cốc | Trình duyệt 4 Tab: Duyệt web, Kiểm tra HTTP Headers, Lịch sử và Tải file | ✅ Đạt 100% |
| **Các Method HTTP** | Hỗ trợ `GET`, `HEAD`, `POST` | Tab **Kiểm Tra HTTP** riêng biệt để gửi request và bóc tách Response Headers & Body | ✅ Đạt 100% |
| **Cơ sở dữ liệu** | Quản lý dữ liệu | Tự động kết nối **MySQL** (kèm **SQLite fallback** nhúng) lưu Access Logs và History | ✅ Đạt 100% |

---

## 🖥️ 2. Ba Dịch Vụ Mạng Cốt Lõi Trên Máy Chủ

1. **🎵 Dịch Vụ 1: Stream Âm Thanh MP3 (`/media/audio.mp3`)**:
   - Giao thức: `HTTP 206 Partial Content` (Range Requests).
   - Cho phép nghe nhạc trực tuyến, bấm Play/Pause và tua thanh thời gian bài hát. Máy chủ tự động bóc tách Header `Range: bytes=start-end` và phản hồi đúng đoạn byte âm thanh đó.
2. **🎬 Dịch Vụ 2: Stream Video MP4 (`/media/video.mp4`)**:
   - Giao thức: `HTTP 206 Partial Content` (Range Requests).
   - Phát video chuẩn H.264/MP4, cho phép người dùng nhảy tới bất kỳ mốc thời gian nào trên thanh Seekbar mà không cần tải lại toàn bộ video.
3. **📄 Dịch Vụ 3: Xem Tài Liệu PDF Trực Tiếp (`/documents/sample.pdf`)**:
   - Giao thức: `HTTP 200 OK` với `Content-Type: application/pdf`.
   - Xem tài liệu, đồ án và giáo trình trực tiếp ngay trong trình duyệt (In-browser viewing).

---

## 📱 3. Hướng Dẫn Kết Nối Giữa 2 Máy Hoặc Điện Thoại Qua Wi-Fi

1. **Bước 1:** Kết nối cả 2 máy (hoặc máy tính và điện thoại) vào **chung một mạng Wi-Fi**.
2. **Bước 2 (Trên Máy Chủ):** Nhấp đúp vào [`START-SERVER.bat`](START-SERVER.bat) $\to$ Bấm **▶ Bật Máy Chủ**.
   - Máy chủ sẽ hiển thị địa chỉ LAN màu xanh lá: `http://192.168.x.x:8080/`.
3. **Bước 3 (Trên Máy Khách hoặc Điện thoại):**
   - Mở Chrome/Safari trên điện thoại hoặc máy tính thứ 2, gõ địa chỉ `http://192.168.x.x:8080/`.
   - Bấm nghe nhạc, xem video hoặc mở tài liệu PDF.
4. **Kết quả:** Trên màn hình Server sẽ in log thời gian thực:
   ```text
   🎵 [STREAM NHẠC MP3] [192.168.1.45] GET /media/audio.mp3 -> 206 Partial Content | 256 KB
   🎬 [STREAM VIDEO MP4] [192.168.1.45] GET /media/video.mp4 -> 206 Partial Content | 512 KB
   📄 [XEM TÀI LIỆU PDF] [192.168.1.45] GET /documents/sample.pdf -> 200 OK | 652 B
   ```

---

## 🚀 4. Hướng Dẫn Khởi Chạy 1-Click

* **Bật Server:** Nhấp đúp vào [`START-SERVER.bat`](START-SERVER.bat) $\to$ Bảng điều khiển Server mở lên tại cổng `8080`.
* **Mở Trình duyệt Client:** Nhấp đúp vào [`START-BROWSER.bat`](START-BROWSER.bat) $\to$ Trình duyệt mở lên và tự động vào trang chủ `http://localhost:8080/`.

---

## 🧪 5. Kiểm Thử Tự Động (Automated Tests)

Chạy kiểm thử đơn vị với JUnit 5:
```powershell
cmd /c "cd /d D:\Workspace\Practice\Java\HTTPApplication && mvnw.cmd test"
```
Kết quả kiểm thử: **9/9 Tests Pass 100% (BUILD SUCCESS)**.
