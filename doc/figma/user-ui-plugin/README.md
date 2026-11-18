# ChatApp User UI — Figma generator (fallback)

> **The User UI design now lives in the Admin Figma file** (`maCogjZs9eOOhfkxsOZoIU`, pages `user - Pre-login`, `user - Chat`,
> `user - Group`, `user - Friend`, `user - Account`). It was built directly with the Admin components and `Admin / Color` tokens.
> Use this plugin only to regenerate an approximate draft somewhere else.

A local Figma plugin that builds the V1 **User UI design** from the User UI plan (now [`doc/USER_UI.md`](../../USER_UI.md)) in the
same style as the Admin design: the Admin palette and sizes from `Ui.java`, a 224 px navy sidebar, 1280×800 screens,
Admin-style tables, tabs and cards.

It exists because the Figma MCP connection used here is on the Starter plan, which has a monthly tool-call cap and
a 3-page limit, and the account seat is View-only. The plugin runs under your own Figma login, with no MCP calls,
so you can run it directly in the Admin file's team project.

## Run it (Figma desktop app)

1. Open the target design file. Use the Admin file's team project (e.g. a new file "ChatApp User" there),
   or the draft file already created: <https://www.figma.com/design/QBdPvoLcEruKt4ojhUcZlC>.
2. **Plugins → Development → Import plugin from manifest…** and select `doc/figma/user-ui-plugin/manifest.json`.
3. **Plugins → Development → ChatApp User UI Generator**. It takes a few seconds and ends with
   "ChatApp User UI generated: 42 screens + components."

It writes to a page named **User UI**. If the file has no free page (Starter plan: 3 pages), it uses the current page.
Everything it creates is named `ChatApp User UI · …`, so running it again replaces only its own output.
Color variables (`ChatApp tokens / color/*`) and text styles are reused by name if they already exist.

> If you run it in the draft file `QBdPvoLcEruKt4ojhUcZlC`, its **Foundations** page holds a first batch of icon
> components from the earlier MCP attempt. They are unused, so you can delete them.

## What it builds

| Section | Screens | Features |
| --- | --- | --- |
| Components | Icons, Button (8 styles, optional icon), Field (5 states), Avatar (online dot), Pill (5 tones), Conversation item, Message bubble (side × state), Sidebar (Page = Chat/Group/Friend/Account) | — |
| Pre-login | A1 Log in · A2 missing fields · A3 locked account · A4 Register · A5 Register errors · A6 Forgot password · A7 Email sent | F01, F02, F04 |
| Chat | C1 friend online · C2 friend offline · C3 encrypted group · C4 select messages · C5 report spam · C6 clear history · C7 search in chat · C8 search all · C9 AI suggest · C10 AI improve · C11 empty | F10, F11a–h, F12f |
| Group | G1 my groups · G2 create · G3 create error · G4 admin view · G5 member view · G6 add member · G7 rename · G8 remove member · G9 turn on encryption | F12a–e, g |
| Friend | F1 friends · F2 no match · F3 unfriend · F4 block · F5 online → create group · F6 requests · F7 find people · F8 request sent · F9 no results | F06–F09 |
| Account | AC1 profile · AC2 edit · AC3 edit errors · AC4 change password · AC5 errors / success · AC6 sign out | F03, F05 |

Each screen has a caption with its code, the case it shows, and the F-codes it covers. The sample data matches the
Admin preview data: the signed-in user is Maya Chen (@mayachen).

## Notes
- The type is **Open Sans**, because Segoe UI (the Swing font) isn't available in Figma. The sizes and weights match the Swing UI.
- The plugin was checked with `node --check` and a strict mock of the Figma Plugin API (layout-sizing order,
  token names, text ranges). It has not been rendered in Figma yet, so give the first run a visual pass.
