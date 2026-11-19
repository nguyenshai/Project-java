# Database scripts

| File | What it does |
| --- | --- |
| `create_database.sql` | Creates the 16 tables of the ERD with keys, checks and indexes. **Rerunnable: drops existing tables (all data is lost).** |
| `sample_data.sql` | Empties the tables and loads the team's shared sample data: simulated use of the app from 1 Oct to 19 Nov 2026, with at least three cases of every feature. |

Both run in one transaction each, so a failure leaves the database unchanged.

## Run

You need PostgreSQL 17, either from Docker or from a local install.

```powershell
# Docker (example; pick your own password and keep it out of the repo)
docker run -d --name chatapp-db -e POSTGRES_PASSWORD=<password> -e POSTGRES_DB=chatapp -p 5433:5432 postgres:17
docker cp script/. chatapp-db:/script
docker exec chatapp-db psql -U postgres -d chatapp -v ON_ERROR_STOP=1 -f /script/create_database.sql
docker exec chatapp-db psql -U postgres -d chatapp -v ON_ERROR_STOP=1 -f /script/sample_data.sql

# Local PostgreSQL
createdb chatapp
psql -d chatapp -v ON_ERROR_STOP=1 -f script/create_database.sql
psql -d chatapp -v ON_ERROR_STOP=1 -f script/sample_data.sql
```

The scripts also run unchanged in pgAdmin or DBeaver (open the file and execute it as a whole script).
The database owner needs the right to create the `pg_trgm` extension (any owner can: it is a trusted extension).

## Sample data

Every account's password is `chatapp123`. Activity is simulated day by day from 1 Oct to 19 Nov 2026 12:00;
registrations are spread over 2025–2026 so the registration chart has data in every month.

| Profile | Accounts | Behaviour |
| --- | --- | --- |
| Administrator | `admin` | Admin client only |
| Highly active | `mayachen`, `noahp`, `avab`, `isabelc`, `sofiareyes`, `lucasb` | Almost every day, 3–7 app opens a day, most of the messages |
| Regular | `ethanp`, `linhng`, `analopez`, `davelin`, `oliviar`, `khoatran`, `minhle`, `priyas` | Most days, never more than two days off in a row |
| Low / new | `owentran`, `dkang`, `zoem`, `ryanh`, `tomwu`, `kevinm`, `quanph`, `hanab`, `jakeo`, `lilyv` | Scattered: new sign-ups, rare users, locked or deleted accounts |

Totals: 38 friendships, 48 friend requests, 23 private chats, 7 groups, 895 messages (69 encrypted),
about 2000 app opens and 700 sign-ins.

| Feature | Cases |
| --- | --- |
| Account status | Locked: `linhng`, `owentran`, `ryanh`; deleted (soft): `tomwu`, `kevinm`; locked then unlocked: `ethanp`, `davelin` |
| Friend requests | 6 pending, 38 accepted (one per friendship), 4 declined |
| Blocks | `mayachen`→`dkang`, `zoem`→`mayachen`, `lucasb`→`jakeo`, `priyas`→`ryanh` |
| Groups (2–6 members) | Class Announcements, Java Study Group (encrypted), Campus Events, Project Team, Weekend Football, Design Club (encrypted), Roommates (2 members, encryption turned on then off) |
| Group events | 3 renames, 3 role changes, 3 members who left, 3 members removed, 3 encryption on, 1 encryption off |
| Key rotation | Java Study Group and Design Club each have key versions 1 and 2; the member who was removed (`davelin`, Java Study Group) or left (`analopez`, Design Club) holds only version 1 |
| Chat history | 10 hidden messages, 4 cleared chats, 3 private messages still waiting for delivery (group messages always keep `delivered_at` empty) |
| Spam reports | 8 reports, 2 on encrypted messages (with `reported_content`), 4 of them led to a lock |
| Lock history | 5 locks, 2 unlocks |

## Conventions

- Table and column names are the snake_case form of the ERD names (`ConversationMember.joinedAt` → `conversation_member.joined_at`).
- Times are `timestamptz`; the database time zone is set to `Asia/Ho_Chi_Minh`. A reporting year runs from
  1 January 00:00 local time up to the next 1 January.
- Value lists are `varchar` with `CHECK` constraints (`ACTIVE`/`LOCKED`/`DELETED`, `PENDING`/`ACCEPTED`/`DECLINED`, …).
- Deleting an account sets `status = 'DELETED'` and `deleted_at`; rows are never removed, so every foreign key to
  `user_account` is `RESTRICT`. A deleted account's username and email can be registered again.
- Rules that depend on other rows are enforced by the server: locked accounts, blocks, group admin rights, at least
  two members per group, key rotation, keys only for group conversations and current members, and hiding only
  messages the user can see.
- Only private messages use `delivered_at`; to find waiting messages, join `conversation` with `type = 'PRIVATE'`.
- "Contains" searches must be written as `lower(col) LIKE '%' || lower(?) || '%'` to use the trigram indexes
  (`ILIKE` does not use them).

## Notes for the server (Version 2)

- The JDBC driver sends the JVM time zone when it connects, which overrides the database default. Run
  `SET TIME ZONE 'Asia/Ho_Chi_Minh'` on every new connection (or start the JVM with `-Duser.timezone=Asia/Ho_Chi_Minh`),
  otherwise yearly and monthly reports split at the wrong midnight.
- Database credentials come from `application-local.properties` or environment variables, never from a committed file.

## Formats the server and clients must use (all binary values Base64)

| Value | Format |
| --- | --- |
| `user_account.password_hash` | `pbkdf2$<iterations>$<salt>$<hash>`: PBKDF2WithHmacSHA256, 210000 iterations, 16-byte salt, 256-bit hash |
| `user_key.public_key` | RSA-2048 public key, X.509 SubjectPublicKeyInfo DER (`X509EncodedKeySpec`) |
| `user_key.encrypted_private_key` | IV (12 bytes) ‖ AES-256-GCM of the PKCS#8 DER private key, 128-bit tag; the AES key is PBKDF2WithHmacSHA256(password, `key_salt`, 210000, 256 bits) |
| `group_key_share.wrapped_key` | The 32-byte AES group key encrypted with `RSA/ECB/OAEPWithSHA-256AndMGF1Padding` and `OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSpecified.DEFAULT)` (Java's default MGF1 is SHA-1, so pass the spec) |
| `message.content` when `key_version` is set | IV (12 bytes) ‖ AES-256-GCM of the UTF-8 text, 128-bit tag, no associated data |

`sample_data.sql` is the team's single shared dataset. Change it by editing the file and committing it, so everyone
works with the same accounts, ids, keys and messages.
