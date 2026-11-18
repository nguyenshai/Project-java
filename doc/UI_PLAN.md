# UI Implementation Plan (Version 1)

This plan follows the task list in the V1 report (`02-task-assignment.tex`, tasks **T01–T09**)
and the feature codes **F01–F21**. Rows are ordered by date. Completed rows show the commit or check date.
Pending rows show **proposed** target dates. Adjust them to the real V1 deadline, which is not recorded in the repo.

Owners: **Châu Vũ Trung** = Admin subsystem (F13–F21) and report assembly.
**Nguyễn Sơn Hải** = User subsystem (F01–F12).

> **Blocking rule from the spec:** a member with no commits gets 0 points. Today every commit is by Trung.
> The User UI work (T03, T07) is done but uncommitted. Hải will commit it under his own git identity.

## Timeline

| Date | Task | Owner | Work | Features | Status |
| --- | --- | --- | --- | --- | --- |
| 05/11/2026 | T01 | Both | Analyse the spec; define V1 scope and the F01–F21 catalogue | F01–F21 | Done (`97c5565`) |
| 05/11/2026 | T08 | Trung | Maven multi-module scaffold and LaTeX report draft | — | Done (`97c5565`) |
| 11/11/2026 | T02 | Trung | Figma design and Swing Admin UI for all admin screens | F13–F21 | Done (`865bd92`, `4ec453b`) |
| 11/11/2026 | T06 | Trung | Admin report section, screen table, screenshots | F13–F21 | Done (`865bd92`, `4ec453b`) |
| 16/11/2026 | T02 | Trung | Admin Login page; "Admin" identity; group ids; screenshots re-captured | F13, F15 | Done (uncommitted) |
| 16/11/2026 | T05 | Both | First cross-check of screens and report vs. spec (findings below) | F01–F21 | Done |
| 16/11/2026 | T06 | Trung | Report: role table fixed, Admin Login added, yearly period stated; `doc/ADMIN_UI.md` | F13–F21 | Done (uncommitted) |
| 16/11/2026 | T03 | Hải | User UI per [`USER_UI.md`](USER_UI.md): pre-login flow + Chat, Group, Friend, Account pages; `PreviewServer`; `UserUiTest` + `SearchResultsTest` (21 tests) | F01–F12 | Done (uncommitted; Hải to commit) |
| 16/11/2026 | T07 | Hải | User report section: navigation, screen table, 14 `user-swing-*.png` screenshots; results and contribution rows | F01–F12 | Done (uncommitted; Hải to commit) |
| 18/11/2026 | T02 | Trung | Admin and User sample data unified (Admin is the base: 14 accounts, locks, groups, messages, spam reports); Admin screenshots re-captured | F13–F21 | Done (uncommitted) |
| 18/11/2026 | T03 | Hải | Review fixes: Improve refused in encrypted groups; Sent / Waiting / Delivered from `deliveredAt`; blocked and locked chats closed; locked accounts flagged with no actions except Unfriend / Decline / Remove; removal by `leftAt`; tests (25) | F06–F12 | Done (uncommitted; Hải to commit) |
| 18/11/2026 | T07 | Both | Report: Sign in naming, sample accounts, `Message.deliveredAt` in the ERD and data dictionary, locked and `leftAt` rules, 50/50 contribution, T03 link; User screenshots re-captured (+ `chat-locked`) | F01–F21 | Done (uncommitted) |
| 17/11–20/11 | T04 | Both | `script/create_database.sql` from the 11-entity ERD (+ decide on lock log / group events) | F01–F21 | To do |
| 25/11 | T05 | Both | Final cross-check: every F-code has a screen and a screenshot; ERD ↔ script ↔ UI fields agree | F01–F21 | To do |
| 25/11 | T02 | Trung | Decide on Chat Groups Activity/Monitoring tabs (keep with ERD support, or remove) | F15 | To do |
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
  text in `02-task-assignment.tex`. The T03 evidence link is Figma for now; the code link/commit is added after Hải commits.

## Findings from the T05 cross-check (16/11)

Blocking for V1:
- `script/create_database.sql` is a placeholder (V1 requires it).
- `git/` has no commit screenshots, and the contribution table has no percentages.
- Hải has no commits yet. The User UI work is ready for him to commit (then add the commit link to the T03 row).

Resolved on 16/11 (later):
- User UI screens are built (F01–F12), with report section and screenshots (T03, T07).

Resolved on 16/11:
- The report role table (`01-team-information.tex`) contradicted the task table. It now says Trung = Admin, Hải = User.
- The period is defined as a calendar year in the report and docs, consistent with the Admin UI.
- The Admin client had no login page. `AdminLogin` was added, separate from the future User login.
- The admin identity was a made-up person. It is now "Admin".
- Groups were matched by name. They now use ids.

Open for the T05 final check:
- Chat Groups Activity/Monitoring tabs and lock history need ERD support or removal (see `ADMIN_UI.md`).
- The ERD needs a decision on storing encrypted group content (`Conversation.isEncrypted` is only a flag).

## After V1 (Version 2 outline)

DTOs and enums in `common` → TCP protocol → server `network` / `service` / `dao` (JDBC) → replace both
preview models with network clients behind service interfaces → real-time push for chat →
`release/` with admin and user jars plus one config file.
