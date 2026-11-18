# User UI (Version 1)

Report task **T03** (UI) and **T07** (report section). Owner: **Nguyễn Sơn Hải**. Features **F01–F12**
(`03-02-02-user-application.tex`), spec section "User subsystem" items 1–12.

## Run it

```powershell
.\mvnw.cmd -pl src/user-client -am test       # 25 cases: UserUiTest (23) + SearchResultsTest (2)
java -cp "src/user-client/target/classes;src/common/target/classes" chatapp.user.UserApp
```

Sign in as `mayachen` / `chatapp123`. Every sample account uses the same password.
`linhng`, `owentran` and `ryanh` are locked, so they show the lock error. You can also create a new account on Register.
The sample data is the same data set as the Admin client's `PreviewData` (see "Sample data" below).
Data lives in memory: changes last until the window closes.

## Decisions

| Topic | Decision |
| --- | --- |
| Navigation | Sidebar with four pages: **Chat, Group** (Messages), **Friend** (People), **Account** (Settings). Each feature lives on exactly one page. Other pages only *link* to it through `Navigator` (e.g. "Create group" on Friend opens the Group page's create form). |
| Sign in, Register, Forgot password | A pre-login flow outside the sidebar. Separate from the Admin Sign in page, in a separate app. Register signs the new account in right away. |
| Group messaging (12f) | On the **Chat** page, which lists private and group conversations together (one `Conversation` table in the ERD). The Group page only manages groups. |
| Interactivity | Same as Admin: navigable screens backed by an in-memory `PreviewServer`. Actions change the preview state. Covered by `UserUiTest`. |
| Visual style | The Admin look (colors, fonts, widgets, sidebar) with English labels, built from the Figma User pages. |
| Design source | Figma file `maCogjZs9eOOhfkxsOZoIU`, pages `user - Pre-login` (129:929), `user - Chat` (129:930), `user - Group` (129:931), `user - Friend` (129:932), `user - Account` (129:933): 43 screens (synced 18/11/2026: Sign in naming, Locked pills, Sent / Waiting / Delivered labels, the "Private chat · Locked account" screen), plus shared components in `user - Chat › 00 · Shared user components`. The 28 icons were exported from that frame. |
| Period / time | Messages and history show timestamps. No year filter on User pages. The preview clock is fixed at `2026-11-11 12:00`, as in Admin. |
| Simulated in V1 | Live delivery, the reset email, the AI assistant (canned replies and rule-based rewrite) and group encryption (a flag) are simulated. V2 replaces them with the server, SMTP and an LLM. |

## Feature → page map (one page per feature)

| Spec | Code | Feature | Page → place |
| --- | --- | --- | --- |
| 1 | F01 | Register | **Pre-login** → Register |
| 5 | F02 | Sign in (wrong password, locked account) | **Pre-login** → Sign in |
| 3 | F04 | Reset password: random, sent by email | **Pre-login** → Forgot password → "Check your email" card |
| 2 | F03 | Update account info | **Account** → Edit profile |
| 4 | F05 | Change password | **Account** → Change password |
| 6a | F06 | Friend list, name filter, online/offline status | **Friend** → Friends tab |
| 6b | F06 | Search by name and send friend request | **Friend** → Find people tab → Add friend |
| 6c | F06 | Unfriend | **Friend** → Friends tab |
| 6d | F06 | Block (and unfriend); blocked user cannot re-request | **Friend** → Friends tab / Find people (Not connected rows) |
| 7a–b | F07 | Requests list, name filter, accept / decline | **Friend** → Requests tab |
| 8 | F08 | Online friends, name filter; chat, create group | **Friend** → Online tab (Chat → Chat page; select people → Create group → Group page form) |
| 9 | F09 | Find people by username or name, excluding people who blocked me; chat, create group | **Friend** → Find people tab |
| 10 | F10 | Report spam on a message from someone | **Chat** → select one received message → Report spam (reason) |
| 11a | F11 | Real-time when the friend is online | **Chat** → header "Online · messages arrive instantly", "Delivered" |
| 11b | F11 | Offline friend sees messages later | **Chat** → "Waiting" and the offline note; turns "Delivered" when they sign in |
| 11c | F11 | View history | **Chat** → message pane |
| 11d | F11 | Delete all history (for me) | **Chat** → Clear history |
| 11e | F11 | Delete one or more messages (for me) | **Chat** → click messages → Delete for me |
| 11f | F11 | Search in one conversation and jump | **Chat** → Search in chat ("n of m", previous/next) |
| 11g | F11 | Search across all conversations and jump | **Chat** → Search all messages → open a result |
| 11h | F11 | LLM suggest / rewrite | **Chat** → Suggest (Insert) / Improve (Friendly, Formal, Shorter; Replace draft) |
| 12f | F12 | Group chat | **Chat** (group conversations; lock badge when encrypted) |
| 12a | F12 | Create group with ≥1 other username | **Group** → Create group |
| 12b | F12 | Rename group | **Group** → detail → Rename (any member) |
| 12c | F12 | Add member | **Group** → detail → Add member (any member) |
| 12d | F12 | Make member a group admin | **Group** → detail → Make admin (admins only) |
| 12e | F12 | Remove member, admins only | **Group** → detail → Remove (admins only) |
| 12g | F12 | Group encryption | **Group** → detail → Encrypted group toggle (admins only, with confirm) |

## Screens (as built)

### Pre-login (`ui/auth`)
- **Sign in (A1–A3, class `Login`):** username, password, **Sign in**, links **Forgot password?** and **Create account**.
  Empty fields are marked under each field. A wrong password or a locked account shows a red banner.
- **Register (A4–A5):** username, full name, email, gender, date of birth (optional, YYYY-MM-DD), address (optional),
  password (at least 8 characters), confirm. The server returns every failed field at once and the page marks them
  all: username or email taken, invalid email or date, short or mismatched password, no gender.
- **Forgot password (A6–A7):** email → "Check your email" with the generated `Views.Email` (from, to, subject,
  body with the new random password).

### Main shell (`ui/UserFrame`)
The sidebar has the brand, sections Messages / People / Settings, the signed-in user (avatar and name) and **Sign Out**
(asks to confirm, then returns to Sign in). It opens on **Chat**. One `PreviewServer` lives as long as the window, so a
registered account can sign in again after Sign Out.

### Chat page (`ui/chat`)
- **Left column:** "Search all messages" (F11g; replaces the list with results, "n results in m conversations",
  match in bold blue), "Filter conversations by name", and the list. Each item has an avatar with an online dot,
  title, last message, time, a lock for encrypted groups and a **Locked** / **Blocked** pill for closed private chats.
- **Header:** avatar, title, "Online · messages arrive instantly" / "Offline" / "n members · m online",
  an **Encrypted** pill, **Search in chat**, **Clear history** (confirm; for me only). A private chat with a locked
  account shows a **Locked** pill and "Account locked by an administrator"; one with someone I blocked shows
  **Blocked** and "You blocked this person"; one with someone who blocked me shows "Unavailable". None of them show
  online status.
- **Messages:** day separators, mine on the right, theirs on the left with an avatar (sender name in groups), times, and
  the status of my last message: **Sent** (group messages; in V2 also the moment before delivery), **Waiting** (the
  recipient is offline) or **Delivered** (the recipient has it, at once or when they signed in). Clicking messages
  selects them (checkboxes). The bar then shows **Delete for me** (confirm), **Report spam** (one received message I
  haven't reported yet; reasons in a dialog) and **Cancel**. Search matches are amber and the focused one is scrolled into view.
- **Composer:** text field, **Suggest** (three replies, **Insert**), **Improve** (tones Friendly / Formal / Shorter,
  Original vs Improved, **Keep original**, **Try again**, **Replace draft**), **Send**. Errors (empty draft,
  "The AI assistant can't read encrypted conversations." for both Suggest and Improve) show above the field.
  In a closed private chat (blocked either way, or a locked account) a grey bar explains why instead of the composer.

### Group page (`ui/group`)
- **List (G1):** search by name and an "All roles / Group admin / Member" filter. Columns: Group name, Members, My role,
  Encryption, Created, ›. Button **Create group**.
- **Create group (G2–G3):** group name, member search with suggestions (**Add**), chips with ×. With no other
  member it shows "Add at least one other member." (rule in `PreviewServer`).
- **Detail (G4–G9):** **Open chat**, **Rename** (dialog), the meta line (created, creator, members, my role),
  the Encrypted group card with a toggle (admins only, confirm dialog), and **Add member** (search dialog). Members table:
  Member, Group role, Status, Joined, then **Make admin** / **Admin** and **Remove** (confirm). For non-admins these are
  disabled with "Only group admins can …", and an info banner explains why. A locked member shows **Locked** in the
  Status column: a group admin sees only **Remove** for them, other members see "Account locked". Removing sets
  `leftAt`, so the person's messages stay; adding them again reuses the same membership row.
  In the Add member dialog and the Create group suggestions, locked people show "Account locked" instead of **Add**.

### Friend page (`ui/friend/FriendPage`)
Tabs show counts: **Friends (n) · Online (n) · Requests (n) · Find people**. Actions show a success or error banner.
- **Friends:** filter. Columns: Full name, Status, Friends since. Actions: **Chat**, **Unfriend** (confirm),
  **Block** (confirm). A locked friend shows **Locked** as status and only **Unfriend**. An empty filter shows
  "No friends match …" with **Find people**.
- **Online:** checkboxes; "n selected" + **Create group** (opens the Group form prefilled). **Chat** per row.
- **Requests:** Received (From, Sent; **Accept** / **Decline**; a request from a locked account has a **Locked** pill
  and only **Decline**) and Sent (To, Sent; *Pending*).
- **Find people:** search by name or username; info "People who blocked you don't appear in search."; columns Person,
  Relationship. Actions by relationship: Not connected → **Add friend**, **Chat**, **Create group**, **Block**;
  Friend → **Chat**, **Create group**; Request received → **Accept**, **Chat**, **Create group**;
  Request sent → *Pending*, **Chat**, **Create group**; Blocked by you → *Blocked*. A locked account has a **Locked**
  pill and only the text *Account locked*.

### Account page (`ui/account/AccountPage`)
- **Profile (AC1):** avatar, name, @username, member since; "Account details" card; **Edit profile**, **Change password**.
- **Edit profile (AC2–AC3):** full name, username (read-only, "Username can't be changed."), email, gender,
  date of birth, address. Errors show a red banner and red fields.
- **Change password (AC4–AC5):** current, new (at least 8 characters), confirm. Shows an error under each field or a
  success banner.

## Code structure

```text
chatapp.user
├── UserApp                      main(): shows UserFrame
├── preview/
│   ├── Views                    records + Relation, Access and Delivery enums
│   ├── ServiceException         user-fixable error; can name failed form fields (fieldErrors) and carry a Code
│   ├── PreviewServer            in-memory "server": every page calls only this; all rules live here
│   └── SampleData               the shared sample data set (same as Admin PreviewData)
└── ui/
    ├── UserFrame                CardLayout Auth ↔ Workspace; createContent(), createContent(server, onSignOut), createAuth(server, onSignIn)
    ├── Navigator                openChat, openGroup, openCreateGroup, openFriends, profileChanged, signOut
    ├── auth/    AuthCard, Login, Register, ForgotPassword
    ├── chat/    ChatPage, ConversationList, MessagePane, Composer, SearchResults
    ├── group/   GroupPage, CreateGroupForm, GroupDetail
    ├── friend/  FriendPage
    ├── account/ AccountPage
    └── shared/  Ui, Screen (copied from Admin and extended), RowTable, Form, Dialogs
```

- **Shared look:** `Ui` and `Screen` are copied from `src/admin-client/.../ui/shared/`, because `common` stays
  DTO/enum-only and the clients must not depend on each other. `Ui` adds the Figma widgets: button styles with
  icons, avatar, pill, banner, toggle, field states and runtime icon tinting.
  `RowTable` is the Admin table look with real components in cells (avatars, pills, buttons).
  `Form` holds labeled fields with an error line. `Dialogs` holds the Figma-style confirm dialogs.
  Tests answer the dialogs through `Dialogs.answerForTests`.
- **Icons:** `src/user-client/src/main/resources/images/user/`: 28 Lucide icons exported from the Figma icon frame
  (`chat`, `group`, `friend`, `account` with `-active` variants, `brand`, `exit`, `search`, `lock`, `send`,
  `sparkles`, `flag`, `trash`, …) plus `sort` copied from Admin.
- **`PreviewServer` API** (V2 swaps in a TCP client behind the same calls):
  `signIn, signOut, register, resetPassword → Email, me, updateProfile, changePassword`;
  `friends(q), online(q), requests(q), sent(), accept, acceptFrom, decline, unfriend, block, findPeople(q), sendRequest`;
  `chats(q), chat(id), chatWith(personId), messages(chatId), send, delete(chatId, ids), clearHistory, search(chatId, q),
  searchAll(q), suggest(chatId), improve(chatId, draft, tone), reportSpam(messageId, reason), spamReports()`;
  `groups(q), group(id), createGroup(name, usernames), rename, addMember, makeAdmin, removeMember, setEncrypted`.
  Rules: lock and credential checks; block rules (no requests or chat either way; people who blocked me are hidden);
  locked accounts (no chat, friend request, accept, block, group add or make admin; others can only unfriend them,
  decline their requests and, as group admin, remove them); the AI assistant is refused in encrypted and closed chats;
  a group always has at least `MIN_GROUP_SIZE` = 2 people (create needs ≥1 other member; Remove is refused, and
  disabled in Group detail, when only two are left); admin-only make admin, remove and encryption; any member renames
  or adds; removal sets `leftAt` (one `ConversationMember` row per person); per-user history hiding (hidden message ids
  and a history-cleared time, as in the report ERD); messages to an offline user are delivered when they sign in.
- **Delivery status:** only `deliveredAt` is stored (ERD `Message.deliveredAt`). A private message gets
  `deliveredAt = sentAt` if the recipient is online, or the time they next sign in. `Message.delivery()` derives
  **Sent** (groups), **Waiting** (`deliveredAt` empty) or **Delivered**. There is no read receipt.
- **Error codes:** pages that treat a failure differently check `ServiceException.code()` (e.g. `ACCOUNT_LOCKED`
  shows the lock icon on Login), never the message wording.
- **Untrusted text:** Swing renders label or button text that starts with `<html>` as HTML. `Ui.label`, `Ui.button`,
  `Ui.checkbox` and `Ui.pill` set `html.disable` (`Ui.plain`), so names, group names and message previews are shown
  literally. The two places that build HTML on purpose (message bubbles, search snippets) escape the text first.
- **Search:** server search and the result highlight share one case-insensitive matcher
  (`PreviewServer.indexOfIgnoreCase`), so highlight positions never drift. Each keystroke in the in-chat search
  calls the server once.
- **Sample data:** one data set shared with the Admin client (`chatapp.admin.ui.preview.PreviewData`): the same 14
  accounts, created dates, locked accounts, friendships, groups (creator, members, roles, joined dates), messages and
  spam reports. Change both files together. Signed-in user Maya Chen (@mayachen). Friends: Noah and Ava (online),
  Ethan (offline) and Linh (locked). Received requests from Sofia and Owen (locked); a sent request to Isabel. Maya
  blocked Daniel; Zoe blocked Maya. Linh, Owen and Ryan are locked. Private chats with Noah, Ethan (the spam message
  Maya already reported, and her last message still Waiting), Ava and Linh (locked). Groups: Project Team (Maya and
  Noah are group admins; Linh and Owen are locked members; Olivia was removed), Java Study Group (encrypted, Maya is a
  member), Campus Events, Class Announcements.

## Differences from the original plan

- Extra files: `SampleData`, `AuthCard`, `RowTable`, `Form`, `Dialogs`. Chat and Group are split into several files.
- `ServiceException` can carry field errors, so a form marks every failed field at once.
- `Views`: added `Access` (closed chats), `Delivery` with `Message.deliveredAt`, `Message.reportedByMe` and the
  `locked` flag on `Person` and `Member`. The Figma unread-count badges are left out: they are not in the spec
  or in `Views`.
- Find people offers **Create group** on every non-blocked row. **Block** is on "Not connected" rows (as in Figma)
  and on the Friends tab.
- Figma uses the same sample people as the code; Figma groups show fewer members than the data (illustrative).
- Delete for me asks to confirm. Report spam lets the user choose one or more reasons.

## Tests (`src/user-client/src/test/java/chatapp/user/`)
25 cases. `UserUiTest` has 23 in the `AdminUiTest` style (`UserFrame.createContent()`, click buttons by text, assert labels and table
columns):
- the sidebar opens all four pages;
- login rejects empty, wrong and locked input and accepts valid input;
- register marks every failed field, then signs the new account in;
- forgot password shows the email, and the new password works;
- account edit keeps the username read-only and rejects a taken email;
- Friend → Chat and Online → Create group link to their pages; global search opens the hit's conversation;
- selecting a received message allows Report spam (not for one already reported) and Delete for me (other people
  keep their copy);
- a message is Delivered at once or Waiting, offline friends get it on sign-in (Delivered, later `deliveredAt`), and
  group messages are Sent; clear history and search work per user;
- the AI assistant suggests and improves, and refuses encrypted groups (also from the Chat page's Improve button);
  spam reports only for other people's messages, only once;
- a blocked chat explains why and hides the message box;
- locked accounts are flagged and can only be unfriended, declined or removed from a group;
- removed members keep their messages in the history and can be added back;
- people who blocked me are hidden, and blocked people cannot request or chat; requests can be accepted, declined
  and sent;
- group rules: another member is required, a group never drops below 2 people, and admin rights are enforced; group
  detail disables admin actions for members; the Create group form shows the rule and creates the group;
- names, group names and message text containing `<html>` are shown literally, never rendered;
- losing access to the open chat (e.g. removed from the group) closes it with "You are no longer in this
  conversation." instead of a silent empty pane.

`ui/chat/SearchResultsTest` (2) checks the search highlight: every match, case-insensitive, escaped, and no drift for
characters such as "İ" that grow when lower-cased.

## Report and screenshots (T07): done
- 15 screenshots at 1280×800, captured from the running `UserFrame`, in `doc/report version 1/images/user-swing-*.png`:
  `login`, `register`, `forgot-password`, `chat`, `chat-select`, `chat-search`, `chat-llm`, `chat-locked`, `group`,
  `group-detail`, `group-create`, `friend`, `friend-requests`, `find-people`, `account`.
- `03-02-02-user-application.tex`: navigation paragraph, screen table (`tab:user-screen-map`, Figma links per page)
  and 9 figures. `02-task-assignment.tex`: T03 and T07 result rows and both contributions (50% each).
- Update the screenshots whenever visible User UI changes.

## Verification
- `.\mvnw.cmd -pl src/user-client -am test` passes (25), and `.\mvnw.cmd verify` is green for all modules.
- Run `UserApp` and walk every row of the feature map above.
- Every F01–F12 code in the report's screen table points to a screenshot.
