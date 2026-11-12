# MediaHub — Local Media Streaming & Document Server

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![JavaFX 21](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg)](https://openjfx.io/)
[![Protocol](https://img.shields.io/badge/Protocol-HTTP%2F1.1%20(RFC%207230)-green.svg)](#)
[![Dual Database](https://img.shields.io/badge/Database-MySQL%20%7C%20SQLite%20Fallback-blue.svg)](#)
[![Tests](https://img.shields.io/badge/Unit%20Tests-9%2F9%20Passing-brightgreen.svg)](#)

**MediaHub** là một máy chủ lưu trữ và truyền thông đa phương tiện hoạt động trong mạng cục bộ (LAN / Wi-Fi). Hoạt động tương tự như một Google Drive kết hợp Spotify mini, MediaHub cho phép người dùng mở đọc trực tiếp tài liệu PDF, stream các bài hát MP3/WAV và clip video MP4 với cơ chế tua phân đoạn (HTTP 206 Range Requests).

---

## 🌟 1. Tính Năng Chính Của MediaHub

| Dịch vụ | Giao thức | Mô tả kỹ thuật |
| :--- | :--- | :--- |
| **Stream Âm Thanh** | `HTTP 206 Partial Content` | Phát trực tuyến các tệp nhạc MP3, WAV. Cho phép tua bài hát mượt mà qua cơ chế Byte-Range (RFC 7233). |
| **Stream Video** | `HTTP 206 Partial Content` | Phát video MP4 trực tiếp trên mọi trình duyệt. Khi nhảy tới một mốc thời gian bất kỳ, máy chủ chỉ gửi đoạn byte tương ứng. |
| **Xem Tài Liệu PDF** | `HTTP 200 OK` | Đọc tài liệu, sách, báo cáo trực tiếp trong trình duyệt qua MIME `application/pdf` mà không cần cài phần mềm ngoài. |
| **Tự Động Duyệt Thư Mục** | `Auto Directory Indexing` | Tự động sinh bảng danh mục tệp khi truy cập vào thư mục con (tương tự Apache / Nginx). |
| **Nhật Ký Truy Cập** | `SQLite / MySQL Dual DB` | Tự động ghi nhận mọi lượt truy cập (IP, Method, URI, Status Code, Bytes) vào cơ sở dữ liệu. |

---

## 🚀 2. Khởi Chạy Nhanh 1-Click

* **Khởi chạy không hiện cửa sổ đen (Khuyên dùng):** Nhấp đúp vào [`CHAY-SERVER.vbs`](CHAY-SERVER.vbs) (hoặc [`START.vbs`](START.vbs)).
* **Khởi chạy có cửa sổ dòng lệnh:** Nhấp đúp vào [`CHAY-SERVER.bat`](CHAY-SERVER.bat).
* Bảng điều khiển Server mở lên tại cổng `8080`.
* Bấm nút **[Mở Trình Duyệt]** trên giao diện Server $\to$ Trình duyệt tự mở trang chủ `http://localhost:8080/`.

---

## 📱 3. Kết Nối Điện Thoại Hoặc Máy Tính Khác Qua Wi-Fi

1. Kết nối máy tính chạy Server và điện thoại vào **chung một mạng Wi-Fi**.
2. Khởi động MediaHub $\to$ Quan sát ô màu xanh lá **LAN**: `http://192.168.x.x:8080/`.
3. Trên điện thoại, mở trình duyệt bất kỳ (Safari, Chrome) và gõ địa chỉ trên.
4. Bạn có thể nghe nhạc, xem phim và đọc tài liệu trực tiếp từ điện thoại.

---

## 🧪 4. Kiểm Thử Tự Động (Unit Tests)

Chạy kiểm thử toàn diện với lệnh:
```powershell
cmd /c "cd /d D:\Workspace\Practice\Java\HTTPApplication && .\mvnw.cmd clean test"
```
Kết quả kiểm thử: **9/9 Tests Pass 100% (BUILD SUCCESS)**.
