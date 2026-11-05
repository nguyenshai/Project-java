# Hệ thống chat — RushDL - 13

Đồ án Lập trình ứng dụng Java, lớp 24KTPM2.

- Nguyễn Sơn Hải — 24127163
- Châu Vũ Trung — 24127256

## Phạm vi hiện tại

Đã tạo khung Maven và chuyển báo cáo sang `doc/report version 1/`.
Các package đang để trống, được giữ bằng `.gitkeep`. Chưa có lớp main, màn hình,
nghiệp vụ, TCP listener hoặc kết nối cơ sở dữ liệu; các JAR tạo ra chưa chạy được.
JUnit đã được cấu hình nhưng chưa có bài kiểm thử. Mockito chưa được thêm.

Version 1 tập trung thiết kế giao diện và cơ sở dữ liệu. Khung server và network
được chuẩn bị cho giai đoạn triển khai sau, không phải minh chứng đã hoàn thành chức năng.

## Cấu trúc

```text
Project/
├── pom.xml                       # Maven cha: phiên bản thư viện và các module
├── mvnw / mvnw.cmd                # Chạy Maven bằng wrapper
├── src/
│   ├── common/                    # DTO và enum dùng chung
│   ├── user-client/               # Giao diện Swing cho người sử dụng
│   ├── admin-client/              # Giao diện Swing cho quản trị viên
│   └── server/                    # Spring Boot, nghiệp vụ, JDBC và TCP
├── script/create_database.sql     # File chờ triển khai SQL
├── doc/
│   ├── Final Project.pdf          # Đề bài
│   └── report version 1/          # Báo cáo LaTeX, ảnh và sơ đồ gốc
└── git/                           # Minh chứng commit thực tế khi có
```

Trong mỗi module, mã Java nằm ở `src/main/java`, tài nguyên nằm ở
`src/main/resources`, bài kiểm thử nằm ở `src/test/java` theo quy ước Maven.
Thư mục `src/` bên ngoài chỉ gom các module để thuận tiện chuẩn bị hồ sơ đồ án.

| Module | Package gốc | Thư mục con và trách nhiệm |
| --- | --- | --- |
| common | `chatapp.common` | `dto`: dữ liệu trao đổi; `enums`: các giá trị dùng chung |
| user-client | `chatapp.user` | `ui`: màn hình Swing; `network`: gửi/nhận qua TCP |
| admin-client | `chatapp.admin` | `ui`: màn hình Swing; `network`: gửi/nhận qua TCP |
| server | `chatapp.server` | `model`: dữ liệu nghiệp vụ; `service`: xử lý nghiệp vụ; `dao`: truy vấn JDBC; `network`: nhận/gửi TCP; `config`: cấu hình |

Ba module ứng dụng cùng phụ thuộc `common`. Hai client không phụ thuộc server
và không kết nối trực tiếp PostgreSQL. Quyền Admin/User phải được kiểm tra ở server
khi triển khai; việc tách hai giao diện không tự tạo ra cơ chế phân quyền.

## Công nghệ

Java 21, Java Swing (có sẵn trong JDK), Spring Boot 3.5.16, socket TCP Java,
PostgreSQL JDBC Driver 42.7.14, Maven 3.9.11 và JUnit Jupiter 5.13.4.
Server dùng Spring Boot starter cơ bản, không thêm web starter hoặc WebSocket.
DAO sẽ dùng JDBC trực tiếp; chưa thêm JPA hoặc ORM.

Các phiên bản thư viện được đặt tại `pom.xml` gốc để dễ quản lý.
Chỉ server có driver PostgreSQL. Chưa cần cài PostgreSQL để kiểm tra khung dự án.

## Mở và kiểm tra dự án

1. Cài JDK 21 trở lên, ưu tiên JDK 21 cho dự án này; cấu hình `JAVA_HOME`.
2. Mở `pom.xml` ở thư mục gốc bằng IDE và import dưới dạng Maven project.
3. Trong PowerShell tại thư mục gốc, chạy:

```powershell
.\mvnw.cmd verify
```

Lần đầu cần Internet để tải Maven và thư viện. Lệnh này kiểm tra cấu hình/build;
khi chưa có bài test, thông báo `No tests to run` là đúng trạng thái hiện tại.
Không chạy `spring-boot:run` ở giai đoạn này vì chưa có lớp khởi động server.

Để sửa báo cáo, mở `doc/report version 1/main.tex`; giữ nguyên các thư mục
`config`, `cover`, `sections`, `images` bên cạnh để các lệnh `\input` và ảnh hoạt động.
