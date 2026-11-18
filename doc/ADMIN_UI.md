# Admin UI (Version 1)

Swing client for system administrators, module `src/admin-client`, root package `chatapp.admin`.
V1 is a UI prototype: every screen is navigable and driven by in-memory preview data. There is no
network or database access yet. Owner: Châu Vũ Trung (tasks T02, T06 in the report).

Design source: Figma file `maCogjZs9eOOhfkxsOZoIU` ("ChatApp Admin"). Node links per screen are in
`doc/report version 1/sections/03-02-01-admin-application.tex`.

## Run and test

```powershell
.\mvnw.cmd -pl src/admin-client -am test        # AdminUiTest
.\mvnw.cmd -pl src/admin-client -am package -DskipTests
java -cp "src/admin-client/target/classes;src/common/target/classes" chatapp.admin.AdminApp
```

Or run `chatapp.admin.AdminApp` from the IDE. The app opens on the **Admin Sign in** page (class `AdminLogin`).
In the preview any non-empty username and password signs in, e.g. `admin` / `admin`. **Sign Out** returns to the login page, and each sign-in
starts a fresh workspace, so preview edits are discarded.

## Structure

```text
chatapp.admin
├── AdminApp                 main(): shows AdminFrame
└── ui
    ├── AdminFrame           CardLayout: Login ↔ Workspace (sidebar + CardLayout of screens)
    ├── login/AdminLogin     Admin-only sign-in page (separate from the User client's login)
    ├── accounts/Accounts    Users screen
    ├── moderation/Moderation  Account History, Chat Groups, Spam Reports
    ├── analytics/Analytics  Overview, Friend Counts, Activity
    ├── preview/PreviewData  in-memory stand-in for the server
    └── shared
        ├── Screen           base class: page(title) builds a page, display(page) swaps the scrollable body
        ├── Ui               colors, fonts, widget factories (button, field, search, table, rows, onRow, tabs, choice, card, icon)
        ├── PeriodPicker     the single shared reporting year ("Period: 2026" button + popup)
        └── BarChart         12-month bar chart; BarChart.forDates(year, dates)
```

- `AdminFrame.createContent()` returns the signed-in workspace (tests use it directly).
  `AdminFrame.createLogin(onSignIn)` returns the login page.
- All screens share one `PreviewData` and one `PeriodPicker`, so a lock or a year change is visible everywhere.
- Icons: `src/main/resources/images/admin/<name>.png`, with `-active` variants for the selected sidebar item.
- The signed-in administrator is shown as **"Admin"** (`PreviewData.ADMIN_NAME`). It is also used as the actor in lock history.

## Preview data

`PreviewData` holds Java records (`User`, `SignIn`, `AppOpen`, `Friend`, `Group`, `Member`, `Event`, `Message`,
`Report`, `LockAction`) with a fixed clock `PreviewData.NOW = 2026-11-11T12:00`.

- **One sample data set for both apps.** The User client's `chatapp.user.preview.SampleData` holds the same 14
  accounts, created dates, locked accounts (Linh, Owen, Ryan), friendships, groups (creator, members, roles, joined
  dates), messages and spam reports (reasons use the User client's Report spam choices). Change both files together.
  Olivia Reed is the member Noah removed from Project Team; the encrypted Java Study Group logs messages without content.

- Groups are identified by a numeric `id` (mirrors `Conversation.id`), never by name, because group names are not unique.
  `Event.groupId` and `Message.groupId` reference it. `Message.groupId == null` means a private message.
- `lock(id, locked)` updates the user and appends a `LockAction` only when the state changes.
- Deleting a user removes the `User` only. Other records resolve the missing user to "Deleted account".

**Integration seam (V2):** replace direct `PreviewData` access with an `AdminService` interface that has two
implementations: the preview one and a TCP client in `chatapp.admin.network`. DTOs live in `common`.

## Spec coverage

The reporting period is a **calendar year**. The team applies "khoảng thời gian" (time range) in the spec
as a year selected with **Period**. Every chart always shows Jan–Dec.
Sorting is done by clicking a column header (`Ui.table` enables `setAutoCreateRowSorter`).

| Spec (Admin) | Code | Screen → where | Class / method |
| --- | --- | --- | --- |
| (sign-in, implied by server-side authorization) | — | Admin Sign in | `AdminLogin` |
| 1 Manage users: list, filter name/username/status, sort name/created | F13 | Users | `Accounts.directory` |
| 1b Add / edit / delete | F13 | Users → Add user, Edit user, Delete account | `Accounts.form`, `Accounts.profile` |
| 1c Lock / unlock | F13 | Users → profile | `Accounts.profile` → `PreviewData.lock` |
| 1d Update password | F13 | Users → Update password | `Accounts.password` (validates only) |
| 1e Login history | F13 | Users → Login History tab | `Accounts.history` |
| 1f Friend list | F13 | Users → Friends & Groups tab | `Accounts.relationships` |
| 2 Sign-ins in time order (time, username, full name) | F14 | Account History → Sign-ins | `Moderation.history(false)` |
| 3 Chat groups: sort name/created, filter name, members, admins | F15 | Chat Groups → Members / Group admins tabs | `Moderation.groups`, `Moderation.group` |
| 4 Spam reports: sort time/username, filter time/username, lock | F16 | Spam Reports → Reports, report detail | `Moderation.reports`, `Moderation.reportDetail` |
| 5 New registrations in a period: sort, filter name | F17 | Account History → New Accounts | `Moderation.history(true)` |
| 6 Registrations per month chart | F18 | Analytics → Overview (Graph: Registrations) | `Analytics.overview` |
| 7 Users with direct / friends-of-friends counts, filters `= < >` | F19 | Analytics → Friend Counts | `Analytics.friendCounts`, `friendDetail` |
| 8 Active users in a period with opens / people / groups, filters `= < >` | F20 | Analytics → Activity | `Analytics.activity` |
| 9 Active users per month chart | F21 | Analytics → Overview (Graph: Active users) | `Analytics.overview` |

### Extras beyond the spec
- Spam Reports → **By account** (report count, prior locks, lock history per account).
- Sign-in metrics and charts on Account History, Login History and Activity; **Sign-ins** and **Messages sent** graphs on Overview.
- Chat Groups → **Activity** and **Monitoring** tabs (see Future items).

## Test contract

`AdminUiTest` (7 tests) finds buttons by their **text** and checks **JTable column names**. Renaming a
sidebar item, tab, button or column header breaks the tests. Update the tests and the report
together with the UI.

## Screenshots

`doc/report version 1/images/admin-swing-*.png`: `login`, `users`, `account-history`, `period-picker`,
`new-accounts`, `groups`, `spam-reports`, `overview`, `active-users`, `friend-counts`, `activity`.
They are rendered at 1280×800 from `AdminFrame.createContent()`. Re-capture them whenever visible UI changes.

## Future items (decide when finalizing V1)

- **Chat Groups → Activity / Monitoring tabs.** These are not required by the spec. They need group events
  (rename, role change, removal vs. leaving) that the ERD does not store yet. Before submission, either
  add a `GroupEvent` table to the ERD and script, or remove the tabs (also update the test that opens Monitoring,
  the report table, and the screenshots).
- **Lock history** (`LockAction`). This also has no ERD table. Add `AccountLockLog(userId, adminId, locked, at)` or drop the view.
- Admin password update only validates the form in V1.

## Known issues (minor)

- Integer columns use JTable's default number renderer (right-aligned, no custom padding), so they look slightly
  different from text columns.
- Spam report username search matches the **reported** user only, not the reporter.
- The admin login accepts any non-empty credentials (preview). V2 must check the password hash and
  `systemRole = ADMIN` on the server, and reject `LOCKED` accounts.
