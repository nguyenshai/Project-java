# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

University Java course project (Java Application Programming, 24KTPM2, team "RushDL - 13"): a chat system with a Swing user client, a Swing admin client, and a Spring Boot TCP server backed by PostgreSQL. The assignment spec is `doc/Final Project.pdf`; the LaTeX report lives in `doc/report version 1/` (entry point `main.tex`; keep its `config/ cover/ sections/ images/` siblings in place because `\input` and image paths are relative). Everything is in English: the report, README, `doc/*.md`, code and UI labels (the spec sets no language; member names keep their Vietnamese spelling). See `doc/AGENT.md` (project rules, F-codes), `doc/ADMIN_UI.md` (admin UI reference) `doc/UI_PLAN.md` (V1 plan by report task) and `doc/USER_UI.md` (User UI as built: pre-login flow + Chat/Group/Friend/Account pages, rules, tests).

Version 1 focuses on UI and database design. **Current state:** `admin-client` has real code (Admin Login + a fully navigable Swing UI driven by in-memory preview data). `user-client` has the full User UI (pre-login flow + Chat/Group/Friend/Account, F01–F12) driven by an in-memory `chatapp.user.preview.PreviewServer` (sign in as `mayachen` / `chatapp123`; `linhng`, `owentran`, `ryanh` are locked; launch `chatapp.user.UserApp`). The Admin Sign in page accepts any non-empty credentials in V1. The User UI is committed by Hải (`830c8ab`). `common` and `server` are empty package skeletons held by `.gitkeep`; the server has no main class yet. `script/` holds the PostgreSQL 17 schema (`create_database.sql`) and the team's shared sample data (`sample_data.sql`, edited by hand and never regenerated; 25 accounts with simulated activity 1 Oct–19 Nov 2026, every password `chatapp123`); see `script/README.md` for formats. The sample data is richer than the Swing previews' in-memory data and does not match it one to one. Every reporting period is a calendar year (shared `PeriodPicker`).

## Commands

Run from the repo root (PowerShell). Java 21 (`maven.compiler.release=21`), Maven via wrapper.

```powershell
.\mvnw.cmd verify                                   # build + test all modules
.\mvnw.cmd -pl src/admin-client -am test            # one module (plus its deps, i.e. common)
.\mvnw.cmd -pl src/admin-client -am test "-Dtest=AdminUiTest#lockAndUnlockShareOneAccountStateAndHistory" "-Dsurefire.failIfNoSpecifiedTests=false"
```

There is no exec plugin configured. To launch the admin UI, run `chatapp.admin.AdminApp` from the IDE, or after compiling:
`java -cp "src/admin-client/target/classes;src/common/target/classes" chatapp.admin.AdminApp`.
Do not run `spring-boot:run` — the server has no startup class yet.

## Architecture

Maven multi-module build. Root `pom.xml` (groupId `vn.rushdl.chatapp`) owns all versions (Spring Boot 3.5.16 BOM, JUnit 5.13.4 BOM, PostgreSQL driver 42.7.14) and adds JUnit Jupiter to every module. Modules live under `src/<module>/` with standard Maven layout inside each.

| Module | Root package | Intended layers |
| --- | --- | --- |
| `common` | `chatapp.common` | `dto`, `enums` shared over the wire |
| `server` | `chatapp.server` | `network` (TCP) → `service` → `dao` (plain JDBC) / `model`, `config` |
| `admin-client` | `chatapp.admin` | `ui` (Swing), `network` (TCP client) |
| `user-client` | `chatapp.user` | `ui` (Swing), `network` (TCP client) |

Design constraints stated in README (keep to them when implementing):
- All three apps depend only on `common`; clients never depend on `server` and never talk to PostgreSQL directly — everything goes through the server over raw Java TCP sockets.
- Server uses only `spring-boot-starter` with `spring.main.web-application-type=none` — no web starter, no WebSocket, no JPA/ORM. DAOs use JDBC directly. Only `server` has the PostgreSQL driver.
- Admin vs. user authorization must be enforced on the server; separate client apps are not a security boundary.
- Do not put DB passwords in `application.properties`; `application-local.properties` and `.env*` are gitignored for local secrets.

### Admin client UI structure

- `AdminFrame` shows `login/AdminLogin` first (separate from the future User login); signing in builds the workspace and Sign Out returns to login. `AdminFrame.createContent()` builds the signed-in workspace (sidebar + `CardLayout` content) as a `JPanel`; `AdminFrame.createLogin(onSignIn)` builds the login page. Tests use `createContent()` directly and drive navigation by finding buttons by text and calling `doClick()`, then asserting on `JTable` column names — so renaming button labels or column headers breaks `AdminUiTest`.
- Screens (`accounts/Accounts`, `moderation/Moderation`, `analytics/Analytics`) extend `shared/Screen`, which provides `page(title)` and `display(page)` (rebuilds the scrollable body on each render).
- `shared/Ui` is the single source of colors, fonts, and widget factories (`button`, `table`, `rows`, `onRow`, `tabs`, `search`, `icon`, …). Icons load from `src/main/resources/images/admin/<name>.png` (with `-active` variants for the selected sidebar item). Reuse these helpers rather than styling Swing components ad hoc.
- `shared/PeriodPicker` provides the year-based reporting period (`Range.year/month`) shared across reporting screens; all screens share one `PeriodPicker` and one `PreviewData` instance from the workspace.
- `preview/PreviewData` is an in-memory stand-in for the future server: Java records (`User`, `SignIn`, `Friend`, `Group`, `Report`, `LockAction`, …) with a fixed clock `PreviewData.NOW = 2026-11-11T12:00`. Groups are keyed by numeric `id` (never by name); the signed-in admin is `PreviewData.ADMIN_NAME = "Admin"`. It is the same sample data set as the User client's `chatapp.user.preview.SampleData` (same accounts, locks, friendships, groups, messages, spam reports): change both together. Mutations (e.g. lock/unlock) update shared state and history so every screen stays consistent. When the network layer is built, this is the seam to replace.
- Report screenshots in `doc/report version 1/images/admin-swing-*.png` document this UI; update them when visible UI changes.
