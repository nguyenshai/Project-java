# AGENT.md

A guide for AI agents and new contributors. Build commands and module architecture are in
[`../CLAUDE.md`](../CLAUDE.md). This file covers the project context, rules and conventions.

## What this project is

Final project for "Java Application Programming" (class 24KTPM2, team **RushDL - 13**): a chat system with
two desktop subsystems, **Admin** and **User**, a server, and PostgreSQL. The spec is
[`Final Project.pdf`](Final%20Project.pdf).

| Version | Weight | Deliverables |
| --- | --- | --- |
| V1 | 20% | `src/` screens of **both** subsystems (UI only, no event logic required) · `script/` DB creation script · `git/` commit screenshots per member · report: team info, task list, contribution table (sum 100), DB design, screen list with screenshots |
| V2 | 80% | Runnable `src/` · `release/` with ≥2 jars (admin, user) + 1 config file · `script/` · `git/` · report (adds highlights, system design, user guide, YouTube demo, GitHub link) · self-assessment |

General rules from the spec: use git; **a member with no commits gets 0 points**; a wrong archive
structure or an incomplete report gets 0 points.

## Team and ownership

| Member | ID | Owns |
| --- | --- | --- |
| Châu Vũ Trung | 24127256 | Admin subsystem (F13–F21), report assembly |
| Nguyễn Sơn Hải | 24127163 | User subsystem (F01–F12) |

Both members share the DB design and the script. Each member must commit their own work.

## Where things are

| Path | Content |
| --- | --- |
| `doc/ADMIN_UI.md` | Admin UI reference: structure, spec map, test contract, future items |
| `doc/UI_PLAN.md` | V1 UI plan by report task (T01–T09), by date |
| `doc/USER_UI.md` | User UI reference (as built): pre-login flow, Chat / Group / Friend / Account pages, feature → page map, rules, tests |
| `doc/report version 1/main.tex` | Report (English LaTeX). Keep `config/ cover/ sections/ images/` beside it |
| `doc/report version 1/images/ERD-chatapp-v1.drawio` | ERD source (16 entities) |
| `script/` | `create_database.sql` (schema, follows the ERD), `sample_data.sql` (shared sample data, edited by hand), `README.md` (how to run, formats) |
| `git/` | Commit screenshots for submission (not `.git/`) |
| Figma `maCogjZs9eOOhfkxsOZoIU` | Admin design (`admin - …` pages) and User design (`user - …` pages, components in `user - Chat › 00 · Shared user components`) |
| `doc/figma/user-ui-plugin/` | Fallback plugin that regenerates a User UI draft offline (the real design is in the Figma file) |

## Feature codes

The report uses these codes and they are stable. Reference them in commits, tests and docs.

| Code | Spec item | Code | Spec item |
| --- | --- | --- | --- |
| F01 | User 1 Register | F12 | User 12 Group chat (create, rename, add, admin, remove, chat, encrypt) |
| F02 | User 5 Login | F13 | Admin 1 Manage users |
| F03 | User 2 Update profile | F14 | Admin 2 Sign-in list |
| F04 | User 3 Reset password by email | F15 | Admin 3 Chat groups |
| F05 | User 4 Change password | F16 | Admin 4 Spam reports |
| F06 | User 6 Friends (list, request, unfriend, block) | F17 | Admin 5 New registrations |
| F07 | User 7 Friend requests | F18 | Admin 6 Registrations chart |
| F08 | User 8 Online friends | F19 | Admin 7 Friend counts |
| F09 | User 9 Find people | F20 | Admin 8 Active users |
| F10 | User 10 Report spam | F21 | Admin 9 Active users chart |
| F11 | User 11 Chat (realtime, offline, history, delete, search, LLM) | | |

**Period rule:** every time filter is a **calendar year** (one shared `PeriodPicker`). This is how the team
applies "khoảng thời gian" in the spec. Charts always show 12 months.

## Rules

- Clients depend only on `common`. They never depend on `server` and never open a DB connection.
- The server uses plain `spring-boot-starter` with no web server, raw `ServerSocket`, and JDBC DAOs. No JPA and no WebSocket.
- Authorization (ADMIN vs. USER, group-admin rights, block rules) is enforced on the **server**.
  Separate client apps are not a security boundary.
- Admin and User have **separate login pages** in separate apps.
- No secrets in `application.properties`. Use the gitignored `application-local.properties` or `.env`.
- Identify entities by id, never by display name (group names are not unique).
- Store passwords only as hashes.

## Conventions

- **Language:** everything is in English: code, UI labels, repo docs (`README.md`, `doc/*.md`) and the
  submitted report. The spec (`Final Project.pdf`) sets no report language. Member names keep their Vietnamese
  spelling. Section files and LaTeX labels use English names (e.g. `03-02-02-user-application.tex`, `tab:user-features`).
- **Swing:** build on `shared/Ui` (colors, fonts, widgets) and `shared/Screen`. Do not style components ad hoc.
- **Preview first:** V1 screens read from a preview model (`PreviewData` for admin, `chatapp.user.preview` for user).
  Keep that model the single seam that V2 replaces with network calls.
- **One sample data set:** Admin `PreviewData` and User `SampleData` describe the same people, locks, friendships,
  groups, messages and spam reports. Change both together, then re-capture the affected screenshots.
- **Tests find UI by text.** Renaming a button, tab or column header means updating tests, the report and screenshots together.
- **When visible UI changes:** re-capture the screenshot at 1280×800 into `doc/report version 1/images/`
  (`admin-swing-*.png`, `user-swing-*.png`) and update the report's screen table.
- Commit messages: imperative, and mention F-codes or T-codes where relevant. Do not commit or push unless asked.
