# 🌐 WebServer & WebBrowser — Lab 4: HTTP Web Server & Web Browser (Java 21 + JavaFX 21)

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![JavaFX 21](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg)](https://openjfx.io/)
[![Protocol](https://img.shields.io/badge/Protocol-HTTP%2F1.1%20(RFC%207230)-green.svg)](#)
[![Dual Database](https://img.shields.io/badge/Database-MySQL%20%7C%20SQLite%20Fallback-blue.svg)](#)
[![Tests](https://img.shields.io/badge/Unit%20Tests-9%2F9%20Passing-brightgreen.svg)](#)

Dự án **HTTPApplication** là giải pháp phần mềm mạng trực quan, gọn gàng, được thiết kế riêng cho **Bài tập lớn Lab 4 môn Lập trình mạng (LTM)**. Giao diện được tối ưu đơn giản, thân thiện, dễ hiểu cho sinh viên khi thuyết trình và chấm điểm.

---

## 🎯 1. Đáp Ứng Chuẩn Đề Bài Lab 4 Của Giảng Viên

| Tiêu chí | Đề bài yêu cầu | Triển khai thực tế trong Dự án | Trạng thái |
| :--- | :--- | :--- | :---: |
| **Giao thức** | Chuẩn **HTTP** (`HTTP/1.1`) | Tự xây dựng Socket HTTP Engine qua TCP Socket đa luồng | ✅ Đạt 100% |
| **Dịch vụ Server** | Có **hơn 1 dịch vụ** (vd: `.pdf`, `.mp3`, `.mp4`...) | Phục vụ đầy đủ: Web HTML5, Stream Video `.mp4`, Stream Nhạc `.mp3`, Xem tài liệu `.pdf`, Tải tệp nén `.zip` | ✅ Vượt chuẩn |
| **Stream Đa phương tiện** | Xem video, nhạc mượt mà | Hỗ trợ **HTTP 206 Partial Content (Range Requests)** cho phép tua video/nhạc tức thời | ✅ Chuẩn RFC |
| **Browser (Web Client)** | Tương tự **Chrome hoặc Cốc Cốc** | Trình duyệt giao diện sáng sủa: Thanh URL, Back, Forward, Reload, Home, Bookmarks | ✅ Đạt 100% |
| **Các Method HTTP** | Hỗ trợ `GET`, `HEAD`, `POST` | Có tab **Kiểm Tra HTTP** riêng biệt để gửi GET, HEAD, POST và xem chi tiết Headers & Body trả về | ✅ Đạt 100% |
| **Lịch sử (History)** | Quản lý lịch sử duyệt web | Tab **Lịch Sử Duyệt** lưu vào CSDL, nhấp đúp để mở lại trang, có nút xóa | ✅ Đạt 100% |
| **Tải xuống (Download)** | Trình quản lý tải file | Tab **Quản Lý Tải Về** hiển thị ProgressBar, tốc độ truyền và nút mở thư mục | ✅ Đạt 100% |
| **Cơ sở dữ liệu** | Quản lý dữ liệu | Tự động kết nối **MySQL** (kèm **SQLite fallback** nhúng) lưu Access Logs và History | ✅ Đạt 100% |

---

## 🖥️ 2. Hai Ứng Dụng Độc Lập — Phân Biệt Rõ Ràng

Dự án tách bạch hoàn toàn giữa 2 ứng dụng:

### 1. Máy Chủ HTTP Server (`START-SERVER.bat`)
- Giao diện nền tối phong cách Console Log mạng.
- Ô nhập cổng: `8080`, nút **[▶ Bật Máy Chủ]** và **[⏹ Tắt Máy Chủ]**.
- **Khung Console Log thời gian thực**: In ra từng dòng request gửi đến máy chủ (IP, Method, URI, Mã HTTP 200/206/404, Dung lượng). Thầy cô nhìn vào thấy ngay máy chủ đang hoạt động ra sao.

### 2. Trình Duyệt Web Browser (`START-BROWSER.bat`)
- Giao diện nền sáng thanh lịch, tích hợp 4 Tab rõ ràng:
  - **Tab 1: 🌐 Duyệt Web Trực Quan**: Thanh URL, điều hướng, thanh phím tắt dịch vụ (`Trang Chủ`, `Video .mp4`, `Nhạc .mp3`, `Đọc PDF`, `Tải Zip`), hiển thị trang web mượt mà.
  - **Tab 2: 🧪 Kiểm Tra HTTP (GET, HEAD, POST)**: Chọn phương thức (GET, HEAD, POST), nhập URL, bấm gửi là xem được ngay Mã phản hồi + Bảng Headers + Nội dung Body.
  - **Tab 3: 🕒 Lịch Sử Duyệt (History)**: Danh sách các trang vừa xem, nhấp đúp để mở lại.
  - **Tab 4: 📥 Quản Lý Tải Về (Downloads)**: Danh sách file đã tải kèm thanh tiến trình và nút mở thư mục.

---

## 🚀 3. Hướng Dẫn Khởi Chạy 1-Click

1. **Bật Server:** Nhấp đúp vào [`START-SERVER.bat`](START-SERVER.bat) $\to$ Bảng điều khiển Server mở lên tại cổng `8080`.
2. **Mở Trình duyệt:** Nhấp đúp vào [`START-BROWSER.bat`](START-BROWSER.bat) $\to$ Trình duyệt mở lên và tự động vào trang chủ `http://localhost:8080/`.

---

## 🧪 4. Kiểm Thử Tự Động (Automated Tests)

Chạy kiểm thử đơn vị với JUnit 5:
```powershell
cmd /c "cd /d D:\Workspace\Practice\Java\HTTPApplication && mvnw.cmd test"
```
Kết quả kiểm thử: **9/9 Tests Đạt (BUILD SUCCESS)**.
