# UI Implementation Plan (Version 1)

This plan follows the task list in the V1 report (`02-task-assignment.tex`, tasks **T01–T09**)
and the feature codes **F01–F21**. Rows are ordered by date. Completed rows show the commit or check date.
Pending rows show **proposed** target dates. Adjust them to the real V1 deadline, which is not recorded in the repo.

Owners: **Châu Vũ Trung** = Admin subsystem (F13–F21) and report assembly.
**Nguyễn Sơn Hải** = User subsystem (F01–F12).

> **Blocking rule from the spec:** a member with no commits gets 0 points. Both members now have commits
> (Hải: `830c8ab`, User UI). Each member still saves their own commit screenshots to `git/` (T09).

## Timeline

| Date | Task | Owner | Work | Features | Status |
| --- | --- | --- | --- | --- | --- |
| 05/11/2026 | T01 | Both | Analyse the spec; define V1 scope and the F01–F21 catalogue | F01–F21 | Done (`97c5565`) |
| 05/11/2026 | T08 | Trung | Maven multi-module scaffold and LaTeX report draft | — | Done (`97c5565`) |
| 11/11/2026 | T02 | Trung | Figma design and Swing Admin UI for all admin screens | F13–F21 | Done (`865bd92`, `4ec453b`) |
| 11/11/2026 | T06 | Trung | Admin report section, screen table, screenshots | F13–F21 | Done (`865bd92`, `4ec453b`) |
| 16/11/2026 | T02 | Trung | Admin Login page; "Admin" identity; group ids; screenshots re-captured | F13, F15 | Done (`f16a7bc`) |
| 16/11/2026 | T05 | Both | First cross-check of screens and report vs. spec (findings below) | F01–F21 | Done |
| 16/11/2026 | T06 | Trung | Report: role table fixed, Admin Login added, yearly period stated; `doc/ADMIN_UI.md` | F13–F21 | Done (`f16a7bc`) |
| 16/11/2026 | T03 | Hải | User UI per [`USER_UI.md`](USER_UI.md): pre-login flow + Chat, Group, Friend, Account pages; `PreviewServer`; `UserUiTest` + `SearchResultsTest` (21 tests) | F01–F12 | Done (`830c8ab`) |
| 16/11/2026 | T07 | Hải | User report section: navigation, screen table, 14 `user-swing-*.png` screenshots; results and contribution rows | F01–F12 | Done (`830c8ab`) |
| 18/11/2026 | T02 | Trung | Admin and User sample data unified (Admin is the base: 14 accounts, locks, groups, messages, spam reports); Admin screenshots re-captured | F13–F21 | Done (`f16a7bc`) |
| 18/11/2026 | T03 | Hải | Review fixes: Improve refused in encrypted groups; Sent / Waiting / Delivered from `deliveredAt`; blocked and locked chats closed; locked accounts flagged with no actions except Unfriend / Decline / Remove; removal by `leftAt`; tests (25) | F06–F12 | Done (`830c8ab`) |
| 18/11/2026 | T07 | Both | Report: Sign in naming, sample accounts, `Message.deliveredAt` in the ERD and data dictionary, locked and `leftAt` rules, 50/50 contribution, T03 link; User screenshots re-captured (+ `chat-locked`) | F01–F21 | Done (`50eaea3`, `830c8ab`) |
| 19/11/2026 | T04 | Trung | ERD decisions: keep Activity/Monitoring (`GroupEvent`), keep lock history (`AccountLockLog`), end-to-end group encryption (`UserKey`, `GroupKey`, `GroupKeyShare`, `Message.keyVersion`, `SpamReport.reportedContent`); ERD, data dictionary and constraints updated | F12g, F15, F16 | Done (uncommitted) |
| 19/11/2026 | T04 | Trung | `script/create_database.sql` (16 tables, constraints, indexes; soft delete; PBKDF2 hashes) and `script/sample_data.sql` (25 accounts in high / regular / low activity profiles, 1 Oct–19 Nov 2026, ≥3 cases per feature); tested on PostgreSQL 17 | F01–F21 | Done (uncommitted) |
| 25/11 | T05 | Both | Final cross-check: every F-code has a screen and a screenshot; ERD ↔ script ↔ UI fields agree | F01–F21 | To do |
| 26/11 | T08 | Trung | Assemble report: results table, contribution table (sum 100), README check, package zip (`src/`, `script/`, `git/`, report) | — | To do |
| 27/11 | T09 | Each | Commit own work; save per-member commit screenshots to `git/` | — | To do |

## T03: User UI (Hải) — done

The as-built description is in **[`USER_UI.md`](USER_UI.md)**: decisions, the feature → page map covering every spec item,
screens, code structure, tests, screenshots and the deviations from the original plan.

Summary:
- **Pre-login flow** (separate from Admin Login): Login (F02), Register (F01), Forgot password (F04).
- **Sidebar pages.** Each feature lives on exactly one page; other pages only link to it:
  - **Chat**: private and group conversations, history, delivery status, delete for me, search in a chat or across
    all chats with jump, LLM Suggest/Improve, Report spam (F10, F11, F12f).
  - **Group**: create (≥1 other member), rename, add member, make admin, remove member (admins only),
    encryption (F12a–e, g).
  - **Friend**: Friends, Online, Requests, Find people tabs (F06–F09).
  - **Account**: profile and change password (F03, F05).
- Navigable like the Admin UI, backed by an in-memory `PreviewServer` built on `Views`. Admin visual style; built from
  the Figma User pages (icons exported from Figma).
- Run: `java -cp "src/user-client/target/classes;src/common/target/classes" chatapp.user.UserApp`, sign in as
  `mayachen` / `chatapp123` (every sample account uses this password; `ryanh` is locked).
- Report: `03-02-02-user-application.tex` (screen table + figures); T03/T07 result rows and Hải's contribution
  text in `02-task-assignment.tex`. Committed by Hải in `830c8ab`.

## Findings from the T05 cross-check (16/11)

Still open for V1:
- `git/` has no commit screenshots yet (T09).

Resolved on 18–19/11:
- Hải committed the User UI (`830c8ab`); the contribution table is 50/50.
- `script/create_database.sql` and `script/sample_data.sql` are written and tested on PostgreSQL 17 (T04).

Resolved on 16/11 (later):
- User UI screens are built (F01–F12), with report section and screenshots (T03, T07).

Resolved on 16/11:
- The report role table (`01-team-information.tex`) contradicted the task table. It now says Trung = Admin, Hải = User.
- The period is defined as a calendar year in the report and docs, consistent with the Admin UI.
- The Admin client had no login page. `AdminLogin` was added, separate from the future User login.
- The admin identity was a made-up person. It is now "Admin".
- Groups were matched by name. They now use ids.

Resolved on 19/11:
- Chat Groups Activity/Monitoring tabs and lock history are kept, backed by `GroupEvent` and `AccountLockLog`.
- Group encryption is end-to-end: per-user RSA keys (`UserKey`), versioned AES group keys (`GroupKey`) wrapped per
  member (`GroupKeyShare`), rotated when a member leaves or is removed; `Message.keyVersion` marks encrypted messages.

## After V1 (Version 2 outline)

DTOs and enums in `common` → TCP protocol → server `network` / `service` / `dao` (JDBC) → replace both
preview models with network clients behind service interfaces → real-time push for chat →
`release/` with admin and user jars plus one config file.
