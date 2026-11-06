# 🌐 WebServerPro & WebBrowserPro — Lab 4: HTTP Web Server & Web Browser (Java 21 + JavaFX 21)

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![JavaFX 21](https://img.shields.io/badge/JavaFX-21.0.6-blue.svg)](https://openjfx.io/)
[![Protocol](https://img.shields.io/badge/Protocol-HTTP%2F1.1%20(RFC%207230)-green.svg)](#)
[![Dual Database](https://img.shields.io/badge/Database-MySQL%20%7C%20SQLite%20Fallback-blue.svg)](#)
[![Theme](https://img.shields.io/badge/UI%20Design-High--Legibility%20%7C%20Chrome%20Style-cyan.svg)](#)
[![Tests](https://img.shields.io/badge/Unit%20Tests-9%2F9%20Passing-brightgreen.svg)](#)

Dự án **WebServerPro & WebBrowserPro** là giải pháp phần mềm mạng toàn diện được xây dựng cho **Bài tập lớn Lab 4 môn Lập trình mạng (LTM)**, tuân thủ nghiêm ngặt chuẩn kiến trúc công nghệ và quy định chấm điểm của Trường Đại học Công nghệ Thông tin & Truyền thông Việt - Hàn (VKU).

---

## 📑 Mục Lục
1. [Đáp Ứng Tiêu Chuẩn Chấm Điểm Lab 4](#-1-đáp-ứng-tiêu-chuẩn-chấm-điểm-lab-4)
2. [Các Tính Năng Cốt Lõi & Kiến Trúc](#-2-các-tính-năng-cốt-lõi--kiến-trúc)
3. [Cấu Trúc Cây Thư Mục Dự Án (Default-Tree)](#-3-cấu-trúc-cây-thư-mục-dự-án-default-tree)
4. [Hướng Dẫn Khởi Chạy 1-Click](#-4-hướng-dẫn-khởi-chạy-1-click)
5. [Kịch Bản Thuyết Trình & Chấm Điểm Chi Tiết](#-5-kịch-bản-thuyết-trình--chấm-điểm-chi-tiết)
6. [Kiểm Thử Tự Động (Automated Unit Tests)](#-6-kiểm-thử-tự-động-automated-unit-tests)

---

## 🎯 1. Đáp Ứng Tiêu Chuẩn Chấm Điểm Lab 4

| Tiêu chí | Đề bài Giảng viên yêu cầu | Triển khai thực tế trong Dự án | Đánh giá |
| :--- | :--- | :--- | :---: |
| **Giao thức** | Chuẩn **HTTP** (`HTTP/1.1`) | Tự xây dựng Socket HTTP Engine qua TCP Socket đa luồng, tuân thủ chuẩn RFC 2616 / RFC 7230 | ✅ Đạt 100% |
| **Dịch vụ Server** | Có **hơn 1 dịch vụ** (vd: `.pdf`, `.mp3`, `.mp4`...) | Phục vụ 7 loại dịch vụ: Web HTML5/CSS/JS, Stream Video `.mp4`, Stream Nhạc `.mp3`, Xem tài liệu `.pdf`, Thư viện ảnh `.png`, Tải tệp nén `.zip`, REST API `.json` | ✅ Vượt chuẩn |
| **Stream Đa phương tiện** | Phát video, âm thanh mượt mà | Hỗ trợ **HTTP 206 Partial Content (Range Requests)** cho phép tua video/nhạc tức thời | ✅ Chuẩn RFC |
| **Browser (Web Client)** | Tương tự **Chrome hoặc Cốc Cốc** | Trình duyệt đa tab (Tabbed Browsing), thanh địa chỉ Omnibox, Back, Forward, Reload, Home, Bookmarks | ✅ Đạt 100% |
| **Các Method HTTP** | Hỗ trợ `GET`, `HEAD`, `POST` | - `GET`: Render trang web qua WebKit Engine.<br>- `HEAD`: Hộp thoại **Inspect HTTP Headers** chuyên sâu.<br>- `POST`: Form Submit và File Upload trực tiếp lên Server | ✅ Đạt 100% |
| **Lịch sử (History)** | Quản lý lịch sử duyệt web | Panel tra cứu lịch sử theo thời gian, lọc tìm kiếm, lưu trữ CSDL và hỗ trợ xóa lịch sử | ✅ Đạt 100% |
| **Tải xuống (Download)** | Trình quản lý tải file | Download Manager hiển thị danh sách tệp tải về kèm ProgressBar, tốc độ truyền và nút mở thư mục | ✅ Đạt 100% |
| **Cơ sở dữ liệu** | Quản lý nhật ký, dữ liệu | Tự động kết nối **MySQL** (kèm **SQLite fallback** nhúng tự động) lưu Access Logs, Uploads và History | ✅ Đạt 100% |

---

## 🌟 2. Các Tính Năng Cốt Lõi & Kiến Trúc

### 2.1. Phía HTTP Web Server (WebServerPro)
1. **Multi-Threaded Socket Engine:**
   - Xử lý đồng thời hàng trăm kết nối qua Thread Pool tái sử dụng tài nguyên.
   - Timeout kết nối 30s chống treo socket.
2. **Hỗ Trợ HTTP 206 Partial Content (Range Requests):**
   - Phân tích header `Range: bytes=start-end`.
   - Đọc trực tiếp byte stream từ offset `start` tới `end`, gửi kèm header `Content-Range: bytes start-end/total`.
   - Trình duyệt có thể kéo thanh tua video `.mp4` hoặc nhạc `.mp3` tại bất kỳ vị trí nào mà không cần tải toàn bộ file.
3. **HTTP HEAD & POST Processing:**
   - `HEAD`: Trả về mã trạng thái và toàn bộ metadata headers (`Content-Type`, `Content-Length`, `Last-Modified`, `Accept-Ranges`) với phần thân body rỗng.
   - `POST`: Xử lý form submit (`/api/submit`) và tải lên tệp tin nhị phân (`/api/upload`).
4. **Server Dashboard UI Hiện Đại:**
   - Đèn LED trạng thái máy chủ (`🟢 SERVER ONLINE` / `🔴 SERVER STOPPED`).
   - Lưới hiển thị nhật ký truy cập thời gian thực (Live Access Logs) với mã màu phân biệt trạng thái (`200 OK` xanh lá, `206 Partial` xanh cyan, `404 Not Found` vàng cam, `500 Error` đỏ).
   - Thống kê thời gian thực: Tổng lượt yêu cầu, Tổng lưu lượng băng thông truyền tải (Bytes).

### 2.2. Phía Web Browser Client (WebBrowserPro — Chrome / Cốc Cốc Style)
1. **Tabbed Browsing & Navigation:**
   - Mở nhiều tab duyệt web độc lập, nút thêm tab mới `[➕]`, đóng tab.
   - Thanh Omnibox tự động hoàn thiện URL, điều hướng qua các nút `◀ Back`, `▶ Forward`, `🔄 Reload`, `🏠 Home`.
   - Tích hợp JavaFX `WebView` & `WebEngine` (WebKit core) tương thích hoàn hảo chuẩn HTML5, CSS3, JavaScript.
2. **Bộ Công Cụ HTTP Inspector (HEAD Request):**
   - Nút `[📋 HEAD Inspector]` mở hộp thoại gửi yêu cầu `HEAD` tới URL hiện tại và tra cứu chi tiết bảng Headers trả về từ máy chủ.
3. **Bộ Công Cụ POST Tester (POST Request):**
   - Nút `[🧪 POST Tester]` cho phép sinh viên thử nghiệm gửi dữ liệu biểu mẫu (Form URL-Encoded hoặc JSON) lên Server và xem phản hồi tức thì.
4. **Trình Quản Lý Tải Xuống (Download Manager):**
   - Theo dõi tiến trình tải tệp (`.zip`, `.pdf`, `.mp4`...), thanh ProgressBar phần trăm, đo tốc độ truyền tải thời gian thực và nút mở thư mục lưu trữ.
5. **Lịch Sử Duyệt Web (History):**
   - Tự động lưu vết các trang web đã truy cập vào CSDL MySQL/SQLite.
   - Hộp thoại tìm kiếm, nhấp đúp để mở lại trang, xóa dòng chọn hoặc xóa toàn bộ lịch sử.

---

## 🏗️ 3. Cấu Trúc Cây Thư Mục Dự Án (Default-Tree)

```text
HTTPApplication/
├── pom.xml                                   # Cấu hình Maven (Java 21, JavaFX 21.0.6, JavaFX-Web, MySQL, SQLite, JUnit 5)
├── START-SERVER.bat / .vbs                   # 1-Click khởi chạy HTTP Web Server Dashboard
├── START-BROWSER.bat / .vbs                  # 1-Click khởi chạy Trình duyệt Web Browser
├── db.properties                             # Cấu hình CSDL MySQL & SQLite Fallback
├── web_root/                                 # Thư mục chứa tài nguyên Web Server phục vụ
│   ├── index.html                            # Trang chủ giới thiệu các dịch vụ đa phương tiện
│   ├── css/style.css                         # CSS giao diện trang chủ hiện đại
│   ├── js/app.js                             # JavaScript xử lý Form POST và DOM
│   ├── documents/sample.pdf                  # Tệp PDF mẫu để demo xem tài liệu trực tiếp
│   ├── media/audio.mp3                       # Tệp âm thanh mẫu để demo stream nhạc
│   ├── media/video.mp4                       # Tệp video mẫu để demo stream video
│   ├── images/banner.png                     # Tệp ảnh mẫu để demo hiển thị đồ họa
│   ├── downloads/project.zip                 # Tệp nén mẫu để demo Download Manager
│   └── uploads/                              # Thư mục lưu trữ tệp khi người dùng tải lên
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java              # Cấu hình module JavaFX, Web, Media, SQL
    │   │   └── com/fiwpr06/websocket/
    │   │       ├── common/
    │   │       │   ├── constant/             # HttpMethod, HttpStatus, MimeType
    │   │       │   ├── model/                # HttpRequest, HttpResponse, AccessLog, HistoryItem, DownloadTask
    │   │       │   └── util/                 # AlertHelper, NetworkUtil, DateTimeUtil, FileUtils
    │   │       ├── server/
    │   │       │   ├── ServerLauncher.java   # Điểm khởi chạy Server Dashboard
    │   │       │   ├── ServerApp.java        # JavaFX Application cho Server
    │   │       │   ├── controller/           # ServerController.java
    │   │       │   ├── core/                 # HttpServerEngine.java, ClientHandler.java, ServerListener.java
    │   │       │   ├── database/             # DatabaseManager.java, AccessLogDao.java, HistoryDao.java
    │   │       │   └── handler/              # GetRequestHandler.java, HeadRequestHandler.java, PostRequestHandler.java
    │   │       └── client/
    │   │           ├── BrowserLauncher.java  # Điểm khởi chạy Trình duyệt Web
    │   │           ├── BrowserApp.java       # JavaFX Application cho Browser
    │   │           ├── controller/           # BrowserController, HistoryController, DownloadController, InspectorController, PostTesterController
    │   │           └── engine/               # HttpDirectClient.java, DownloadEngine.java
    │   └── resources/
    │       └── com/fiwpr06/websocket/
    │           ├── server/server-dashboard.fxml
    │           ├── client/browser-main.fxml, history-modal.fxml, download-modal.fxml, inspector-modal.fxml, post-tester-modal.fxml
    │           └── css/server-style.css, browser-style.css
    └── test/
        └── java/com/fiwpr06/httpapplication/
            ├── HttpRequestParserTest.java    # Kiểm thử phân tích HTTP Request, Query & Headers
            ├── MimeTypeTest.java             # Kiểm thử nhận diện các định dạng MIME
            ├── RangeRequestTest.java         # Kiểm thử xử lý Range Requests 206
            └── DatabaseManagerTest.java      # Kiểm thử kết nối CSDL và lưu trữ bản ghi
```

---

## 🚀 4. Hướng Dẫn Khởi Chạy 1-Click

Hệ thống đã được đóng gói sẵn các script khởi chạy tức thời:

### Bước 1: Khởi động HTTP Web Server
- Nhấp đúp vào [`START-SERVER.bat`](START-SERVER.bat) (hoặc [`START-SERVER.vbs`](START-SERVER.vbs) để chạy ẩn console).
- Cửa sổ **WebServerPro Dashboard** sẽ xuất hiện và tự động kích hoạt máy chủ tại cổng `8080`.

### Bước 2: Khởi động Trình duyệt Web Browser
- Nhấp đúp vào [`START-BROWSER.bat`](START-BROWSER.bat) (hoặc [`START-BROWSER.vbs`](START-BROWSER.vbs)).
- Cửa sổ **WebBrowserPro** xuất hiện với giao diện chuẩn Chrome/Cốc Cốc, tự động mở trang chủ `http://localhost:8080/`.

---

## 🎓 5. Kịch Bản Thuyết Trình & Chấm Điểm Chi Tiết

Khi trình bày bài thi trước Giảng viên, bạn thực hiện theo 7 bước sau:

1. **Giới thiệu giao diện Server Dashboard:**
   - Chỉ vào huy hiệu `🟢 SERVER ONLINE: 192.168.x.x:8080` và huy hiệu `DATABASE: MYSQL` (hoặc `SQLITE`).
   - Giới thiệu khả năng tùy biến cổng port và chọn thư mục Web Root linh hoạt.
2. **Kiểm tra Dịch vụ 1 — Stream Video (`.mp4`):**
   - Trên thanh Bookmarks của Trình duyệt, bấm `🎬 Video (.mp4)`.
   - Kéo thanh thời gian tua video tới giữa và cuối $\to$ Video phát mượt mà tức thời.
   - Nhìn sang màn hình Server Dashboard $\to$ Xuất hiện dòng nhật ký với **Mã HTTP 206 (Partial Content)** và dung lượng byte chunk tương ứng.
3. **Kiểm tra Dịch vụ 2 — Stream Nhạc (`.mp3`):**
   - Bấm `🎵 Âm Thanh (.mp3)` $\to$ Trình duyệt phát âm thanh trực tiếp.
4. **Kiểm tra Dịch vụ 3 — Đọc Tài Liệu (`.pdf`):**
   - Bấm `📄 Tài Liệu (.pdf)` $\to$ Tài liệu PDF mở trực tiếp ngay trên tab trình duyệt.
5. **Kiểm tra Phương thức HTTP HEAD:**
   - Bấm nút `[📋 HEAD Inspector]` trên thanh công cụ.
   - Nhấn `Gửi HEAD Request` $\to$ Bảng tra cứu hiển thị mã `200 OK` và toàn bộ danh sách headers (`Server`, `Content-Type`, `Content-Length`, `Accept-Ranges`, `Last-Modified`) mà không tải thân file.
6. **Kiểm tra Phương thức HTTP POST:**
   - Bấm nút `[🧪 POST Tester]` hoặc điền form tại chân trang chủ rồi bấm `Gửi Dữ Liệu`.
   - Server ghi nhận dữ liệu POST, lưu CSDL và phản hồi JSON xác nhận tức thì.
7. **Kiểm tra Download Manager & Lịch Sử (History):**
   - Bấm `📦 Tải File (.zip)` $\to$ Mở hộp thoại `[📥 Downloads]` thấy file đang tải, thanh tiến trình và nút mở thư mục.
   - Bấm `[🕒 Lịch Sử]` $\to$ Toàn bộ các trang vừa xem được lưu trữ đầy đủ kèm mốc thời gian.

---

## 🧪 6. Kiểm Thử Tự Động (Automated Unit Tests)

Dự án tích hợp đầy đủ 9 bài kiểm thử đơn vị với **JUnit 5**:
```powershell
cmd /c "cd /d d:\Workspace\Practice\Java\HTTPApplication && mvnw.cmd test"
```
Kết quả kiểm thử thực tế:
```text
[INFO] Running com.fiwpr06.httpapplication.DatabaseManagerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.fiwpr06.httpapplication.HttpRequestParserTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.fiwpr06.httpapplication.MimeTypeTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.fiwpr06.httpapplication.RangeRequestTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
