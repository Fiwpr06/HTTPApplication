# HTTPApplication — Lab 4: HTTP Multi-Service Web Server (Java 21 + JavaFX 21)

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![JavaFX 21](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg)](https://openjfx.io/)
[![Protocol](https://img.shields.io/badge/Protocol-HTTP%2F1.1%20(RFC%207230)-green.svg)](#)
[![Dual Database](https://img.shields.io/badge/Database-MySQL%20%7C%20SQLite%20Fallback-blue.svg)](#)
[![Tests](https://img.shields.io/badge/Unit%20Tests-9%2F9%20Passing-brightgreen.svg)](#)

Dự án **HTTPApplication** được xây dựng chuyên biệt theo đúng tiêu chí **Khuyến Khích `(kk)`** trong bảng yêu cầu 5 bài Lab môn Lập trình mạng (LTM) của Giảng viên: **Chỉ tập trung vào Web Server đa dịch vụ**, loại bỏ hoàn toàn các thành phần dư thừa, dễ hiểu 100% khi vấn đáp và chấm điểm.

---

## 1. Đáp Ứng Đúng Yêu Cầu & Cách Chấm Của Giảng Viên

| Tiêu chí | Đề bài yêu cầu (Lab 4) | Triển khai thực tế trong Dự án | Trạng thái |
| :--- | :--- | :--- | :---: |
| **Loại bài làm** | **Web Server(kk)** hoặc Web Client(Browser) | Chọn **Web Server (Khuyến khích `kk`)** | [OK] Đạt chuẩn |
| **Giao thức** | **Có giao thức HTTP** | Tự xây dựng Server TCP Socket xử lý HTTP/1.1 (RFC 2616 & RFC 7230) | [OK] Đạt 100% |
| **Cách chấm của Thầy** | **Có hơn 1 dịch vụ của Server (vd: `.pdf` - `.mp3` - `.pm4`...)** | Phục vụ trọn vẹn đúng 3 dịch vụ: **[1] Stream Nhạc MP3**, **[2] Stream Video MP4**, **[3] Xem Tài Liệu PDF** | [OK] Chuẩn 100% |
| **Trình duyệt kiểm thử** | Tương tự Chrome hoặc Cốc Cốc | Sử dụng chính **Google Chrome / Cốc Cốc / Safari** trên máy tính hoặc điện thoại | [OK] Trực quan |
| **Cơ sở dữ liệu** | Quản lý dữ liệu | Tự động kết nối **MySQL** (kèm **SQLite fallback** nhúng) lưu Access Logs | [OK] Đạt 100% |

---

## 2. Ba Dịch Vụ Mạng Cốt Lõi Trên Máy Chủ

1. **[Dịch Vụ 1] Stream Âm Thanh MP3 (`/media/audio.mp3`)**:
   - Giao thức: `HTTP 206 Partial Content` (Range Requests RFC 7233).
   - Nghe nhạc trực tuyến, bấm Play/Pause và tua thanh thời gian bài hát. Máy chủ tự động bóc tách Header `Range: bytes=start-end` và phản hồi đúng đoạn byte âm thanh đó.
2. **[Dịch Vụ 2] Stream Video MP4 (`/media/video.mp4`)**:
   - Giao thức: `HTTP 206 Partial Content` (Range Requests).
   - Phát video MP4 mượt mà, cho phép người dùng nhảy tới bất kỳ mốc thời gian nào trên thanh Seekbar mà không cần tải lại toàn bộ file.
3. **[Dịch Vụ 3] Xem Tài Liệu PDF Trực Tiếp (`/documents/sample.pdf`)**:
   - Giao thức: `HTTP 200 OK` với `Content-Type: application/pdf`.
   - Xem tài liệu, đồ án và giáo trình trực tiếp ngay trong trình duyệt (In-browser viewing).

---

## 3. Khởi Chạy 1-Click Cực Kỳ Đơn Giản

* **Cách chạy:** Nhấp đúp chuột vào tệp [`CHAY-SERVER.bat`](CHAY-SERVER.bat) (hoặc [`START.bat`](START.bat)).
* Bảng điều khiển Server mở lên tại cổng `8080`.
* Bấm nút **[Mở Trình Duyệt]** trên giao diện Server $\to$ Trình duyệt Chrome/Cốc Cốc sẽ tự động mở trang web máy chủ phục vụ `http://localhost:8080/`.

---

## 4. Hướng Dẫn Kết Nối 2 Thiết Bị (Máy Tính & Điện Thoại Qua Wi-Fi)

1. **Bước 1:** Kết nối cả máy tính chạy Server và điện thoại vào **chung một mạng Wi-Fi**.
2. **Bước 2:** Bật Server bằng [`CHAY-SERVER.bat`](CHAY-SERVER.bat) $\to$ Nhìn ô màu xanh lá **LAN**: `http://192.168.x.x:8080/`.
3. **Bước 3:** Trên điện thoại, mở Safari/Chrome gõ địa chỉ trên.
4. **Kết quả:** Màn hình Console của Server sẽ in ra dòng log thời gian thực:
   ```text
   [STREAM NHAC MP3]   [192.168.1.45] GET /media/audio.mp3     -> 206 Partial Content | 256 KB
   [STREAM VIDEO MP4]  [192.168.1.45] GET /media/video.mp4     -> 206 Partial Content | 512 KB
   [XEM TAI LIEU PDF]  [192.168.1.45] GET /documents/sample.pdf -> 200 OK              | 652 B
   ```

---

## 5. Kiểm Thử Tự Động (Automated Tests)

Chạy kiểm thử đơn vị với JUnit 5:
```powershell
cmd /c "cd /d D:\Workspace\Practice\Java\HTTPApplication && mvnw.cmd test"
```
Kết quả kiểm thử: **9/9 Tests Pass 100% (BUILD SUCCESS)**.
