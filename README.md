# Chat System — RushDL - 13

Final project for "Java Application Programming", class 24KTPM2.

- Nguyễn Sơn Hải — 24127163 (User subsystem)
- Châu Vũ Trung — 24127256 (Admin subsystem)

## Current status (Version 1)

- **Admin client:** complete V1 Swing prototype. It has an Admin Login page and every admin screen (F13–F21),
  driven by in-memory preview data and covered by `AdminUiTest`. See [`doc/ADMIN_UI.md`](doc/ADMIN_UI.md).
- **User client:** complete V1 Swing prototype. It has the pre-login flow (Login, Register, Forgot password) and
  the Chat, Group, Friend and Account pages (F01–F12), driven by an in-memory `PreviewServer` and covered by
  `UserUiTest`. Sign in as `mayachen` / `chatapp123`. See [`doc/USER_UI.md`](doc/USER_UI.md).
- **Server, common:** empty package skeletons. There is no server main class yet.
- **Database:** `script/create_database.sql` (PostgreSQL 17 schema, 16 tables) and `script/sample_data.sql`
  (the team's shared sample data). See [`script/README.md`](script/README.md). The ERD is in the report.

V1 focuses on UI and database design. The plan and remaining work are in [`doc/UI_PLAN.md`](doc/UI_PLAN.md).
User UI reference: [`doc/USER_UI.md`](doc/USER_UI.md).
Contributor and agent guide: [`doc/AGENT.md`](doc/AGENT.md).

## Layout

```text
Project/
├── pom.xml                       # Parent Maven build: versions and modules
├── mvnw / mvnw.cmd               # Maven wrapper
├── src/
│   ├── common/                   # Shared DTOs and enums
│   ├── user-client/              # Swing app for users
│   ├── admin-client/             # Swing app for administrators
│   └── server/                   # Spring Boot (no web), services, JDBC, TCP
├── script/                       # create_database.sql, sample_data.sql, README.md (PostgreSQL 17)
├── doc/
│   ├── Final Project.pdf         # Assignment spec
│   ├── ADMIN_UI.md, UI_PLAN.md, USER_UI.md, AGENT.md
│   └── report version 1/         # LaTeX report (English), images and diagrams
└── git/                          # Commit screenshots for submission
```

Each module uses the standard Maven layout (`src/main/java`, `src/main/resources`, `src/test/java`).

| Module | Root package | Packages |
| --- | --- | --- |
| common | `chatapp.common` | `dto`, `enums` |
| user-client | `chatapp.user` | `ui`, `network`, `preview` |
| admin-client | `chatapp.admin` | `ui`, `network` |
| server | `chatapp.server` | `model`, `service`, `dao`, `network`, `config` |

All three apps depend only on `common`. The clients never depend on the server and never connect to
PostgreSQL. Admin/User authorization is enforced on the server. Two separate client apps are not a
security boundary.

## Technology

Java 21, Swing, Spring Boot 3.5.16 (basic starter, no web or WebSocket), Java TCP sockets,
PostgreSQL JDBC driver 42.7.14 (server only), Maven 3.9.11, JUnit Jupiter 5.13.4.
Library versions are managed in the root `pom.xml`. DAOs use JDBC directly (no JPA/ORM).

## Build and run

1. Install JDK 21 and set `JAVA_HOME`.
2. Import the root `pom.xml` as a Maven project.
3. From the repo root (PowerShell):

```powershell
.\mvnw.cmd verify                                        # build and test all modules
java -cp "src/admin-client/target/classes;src/common/target/classes" chatapp.admin.AdminApp
java -cp "src/user-client/target/classes;src/common/target/classes" chatapp.user.UserApp
```

The first build needs Internet access to download Maven and the libraries. Do not run `spring-boot:run`:
the server has no startup class yet.

## Sample accounts (Version 1 preview)

Both apps run on the same in-memory sample data, and changes last until the window closes.

| App | Sign in with | Notes |
| --- | --- | --- |
| User (`UserApp`) | `mayachen` / `chatapp123` | Maya Chen. Every sample account uses the password `chatapp123`, e.g. `noahp`, `avab`, `ethanp`. |
| User (`UserApp`) | `linhng`, `owentran`, `ryanh` | Locked by an administrator: sign-in shows the lock error. |
| Admin (`AdminApp`) | any non-empty username and password, e.g. `admin` / `admin` | V1 only checks that both fields are filled; the server checks the ADMIN role in V2. |

V1 keeps passwords as plain text in the preview data. The database stores only PBKDF2 password hashes.
The database sample data (`script/sample_data.sql`) is larger than the in-memory preview data and does not match it one to one.

To edit the report, open `doc/report version 1/main.tex`. Keep the `config`, `cover`, `sections` and
`images` folders next to it so `\input` and image paths resolve.
