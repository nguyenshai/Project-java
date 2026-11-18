package chatapp.user.preview;

import chatapp.user.preview.Views.*;
import java.security.SecureRandom;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * In-memory stand-in for the future TCP server. Every User page calls only this class, so
 * Version 2 can swap it for a network client. All rules live here, not in the UI: sign-in and
 * lock checks, the block rules, locked accounts (no chat, friend request, accept, block or group add;
 * a group admin can only remove them), the "at least one other member" rule, admin-only group rights,
 * removal by {@code leftAt}, delivery by {@code deliveredAt} and per-user history hiding (hidden messages
 * and a history-cleared time), all as in the report ERD.
 * Methods throw {@link ServiceException} with a message that is shown to the user as-is.
 */
public final class PreviewServer {
    /** Fixed preview clock, the same instant as the Admin preview data. */
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 11, 12, 0);
    public static final int MIN_PASSWORD = 8;
    /** A group always has at least this many people: creating needs one other member, removing stops here. */
    public static final int MIN_GROUP_SIZE = 2;
    /** The sample signed-in user and the password shared by every sample account. */
    public static final String SAMPLE_USERNAME = "mayachen", SAMPLE_PASSWORD = "chatapp123";
    private static final int MAX_MESSAGE = 2000;
    private static final int MAX_GROUP_NAME = 60;

    // ------------------------------------------------------------------ stored state
    static final class User {
        final long id;
        String username, fullName, email, address, gender, password;
        LocalDate birthDate;
        final LocalDateTime createdAt;
        boolean online, locked;

        User(long id, String username, String fullName, String email, String address, LocalDate birthDate,
             String gender, String password, LocalDateTime createdAt) {
            this.id = id; this.username = username; this.fullName = fullName; this.email = email;
            this.address = address; this.birthDate = birthDate; this.gender = gender;
            this.password = password; this.createdAt = createdAt;
        }
    }

    static final class Conv {
        final long id;
        final boolean group;
        String name;
        boolean encrypted;
        final LocalDateTime createdAt;
        final long creatorId;
        final Map<Long, Membership> members = new LinkedHashMap<>();

        Conv(long id, boolean group, String name, LocalDateTime createdAt, long creatorId) {
            this.id = id; this.group = group; this.name = name; this.createdAt = createdAt; this.creatorId = creatorId;
        }
    }

    /** One row of ConversationMember: removing someone sets {@code leftAt} instead of deleting the row (ERD). */
    static final class Membership {
        boolean admin;
        LocalDateTime joinedAt;
        LocalDateTime leftAt;
        Membership(boolean admin, LocalDateTime joinedAt) { this.admin = admin; this.joinedAt = joinedAt; }
        boolean active() { return leftAt == null; }
    }

    static final class Msg {
        final long id, convId, senderId;
        final String text;
        final LocalDateTime sentAt;
        final boolean encrypted;
        /** Message.deliveredAt in the ERD: when a private message reached its recipient; null while waiting and in groups. */
        LocalDateTime deliveredAt;

        Msg(long id, long convId, long senderId, String text, LocalDateTime sentAt, boolean encrypted,
            LocalDateTime deliveredAt) {
            this.id = id; this.convId = convId; this.senderId = senderId; this.text = text;
            this.sentAt = sentAt; this.encrypted = encrypted; this.deliveredAt = deliveredAt;
        }
    }

    record FriendRequest(long id, long from, long to, LocalDateTime at) {}
    record Pair(long low, long high) {
        static Pair of(long a, long b) { return new Pair(Math.min(a, b), Math.max(a, b)); }
    }
    record Block(long by, long target) {}
    record Cleared(long userId, long convId) {}
    /** A spam report as the admin client will list it (F10 → F16). */
    public record SpamReport(long reporterId, long messageId, long reportedUserId, String reason, LocalDateTime at) {}

    final Map<Long, User> users = new LinkedHashMap<>();
    final Map<Long, Conv> convs = new LinkedHashMap<>();
    final List<Msg> messages = new ArrayList<>();
    final List<FriendRequest> requests = new ArrayList<>();
    final Map<Pair, LocalDateTime> friendships = new HashMap<>();
    final Set<Block> blocks = new HashSet<>();
    final Map<Long, Set<Long>> hidden = new HashMap<>();
    final Map<Cleared, LocalDateTime> cleared = new HashMap<>();
    final List<SpamReport> reports = new ArrayList<>();
    final List<Email> outbox = new ArrayList<>();

    private long nextUserId = 1000, nextConvId = 1000, nextMessageId = 1000, nextRequestId = 1000;
    private LocalDateTime clock = NOW;
    private final Random random;
    private Long meId;

    /** A server with the sample data loaded (signed-in user: Maya Chen, password {@link SampleData#PASSWORD}). */
    public PreviewServer() {
        this(new SecureRandom());
    }

    PreviewServer(Random random) {
        this.random = random;
        SampleData.load(this);
    }

    // ------------------------------------------------------------------ account (F01–F05)

    /** F02. Signs in and marks the user online; their waiting messages are delivered. */
    public Account signIn(String username, String password) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(username)) errors.put("username", "Enter your username.");
        if (password == null || password.isEmpty()) errors.put("password", "Enter your password.");
        if (!errors.isEmpty()) throw new ServiceException(errors);
        User user = byUsername(username.trim());
        if (user == null || !user.password.equals(password)) throw new ServiceException("Incorrect username or password.");
        if (user.locked) {
            throw new ServiceException(ServiceException.Code.ACCOUNT_LOCKED,
                "This account is locked by an administrator. Contact support to restore access.");
        }
        user.online = true;
        meId = user.id;
        LocalDateTime arrived = null;
        for (Msg m : messages) {
            Conv conv = convs.get(m.convId);
            if (m.deliveredAt == null && !conv.group && m.senderId != user.id && isMember(conv, user.id)) {
                if (arrived == null) arrived = now();
                m.deliveredAt = arrived;
            }
        }
        return account(user);
    }

    public void signOut() {
        if (meId != null) users.get(meId).online = false;
        meId = null;
    }

    public boolean signedIn() {
        return meId != null;
    }

    /** F01. Creates the account and signs it in. Every failed field is reported at once. */
    public Account register(String username, String fullName, String email, String address, String birthDate,
                            String gender, String password, String confirm) {
        Map<String, String> errors = new LinkedHashMap<>();
        String handle = trim(username);
        if (handle.isEmpty()) errors.put("username", "Enter a username.");
        else if (!handle.matches("[A-Za-z0-9._]{3,30}")) {
            errors.put("username", "Use 3–30 letters, digits, dots or underscores.");
        } else if (byUsername(handle) != null) errors.put("username", "This username is already taken.");
        if (blank(fullName)) errors.put("fullName", "Enter your full name.");
        checkEmail(errors, email, null);
        if (!Views.GENDERS.contains(gender)) errors.put("gender", "Choose a gender.");
        LocalDate birth = parseBirthDate(errors, birthDate);
        checkNewPassword(errors, "password", "confirm", password, confirm);
        if (!errors.isEmpty()) throw new ServiceException(errors);
        User user = new User(nextUserId++, handle, fullName.trim(), email.trim(), trim(address), birth, gender,
            password, now());
        users.put(user.id, user);
        return signIn(handle, password);
    }

    /** F04. Sets a random password and "sends" it by email; the email is returned for the preview. */
    public Email resetPassword(String email) {
        if (blank(email)) throw new ServiceException("email", "Enter the email on your account.");
        if (!validEmail(email.trim())) throw new ServiceException("email", "Enter a valid email address.");
        User user = users.values().stream().filter(u -> u.email.equalsIgnoreCase(email.trim())).findFirst()
            .orElseThrow(() -> new ServiceException("email", "No account uses this email."));
        user.password = randomPassword();
        String first = user.fullName.split(" ")[0];
        Email mail = new Email(user.email, "Your new ChatApp password",
            "Hi " + first + ", your password was reset. Your new password is " + user.password
                + ". Sign in and change it in Account › Change password.", now());
        outbox.add(mail);
        return mail;
    }

    public Account me() {
        return account(meUser());
    }

    /** F03. The username cannot be changed. */
    public Account updateProfile(String fullName, String email, String address, String birthDate, String gender) {
        User me = meUser();
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(fullName)) errors.put("fullName", "Enter your full name.");
        checkEmail(errors, email, me);
        if (!Views.GENDERS.contains(gender)) errors.put("gender", "Choose a gender.");
        LocalDate birth = parseBirthDate(errors, birthDate);
        if (!errors.isEmpty()) throw new ServiceException(errors);
        me.fullName = fullName.trim();
        me.email = email.trim();
        me.address = trim(address);
        me.birthDate = birth;
        me.gender = gender;
        return account(me);
    }

    /** F05. */
    public void changePassword(String current, String password, String confirm) {
        User me = meUser();
        Map<String, String> errors = new LinkedHashMap<>();
        if (current == null || current.isEmpty()) errors.put("current", "Enter your current password.");
        else if (!current.equals(me.password)) errors.put("current", "Current password is incorrect.");
        checkNewPassword(errors, "password", "confirm", password, confirm);
        if (errors.isEmpty() && password.equals(current)) errors.put("password", "Choose a password you haven't used here.");
        if (!errors.isEmpty()) throw new ServiceException(errors);
        me.password = password;
    }

    // ------------------------------------------------------------------ friends (F06–F09)

    /** F06. My friends, filtered by name or username. */
    public List<Person> friends(String query) {
        long me = meId();
        return users.values().stream().filter(u -> friendships.containsKey(Pair.of(me, u.id)))
            .filter(u -> matches(query, u.fullName, u.username)).map(this::person)
            .sorted(Comparator.comparing(Person::fullName)).toList();
    }

    /** F08. Friends who are online now. */
    public List<Person> online(String query) {
        return friends(query).stream().filter(Person::online).toList();
    }

    /** F07. Requests sent to me; {@link Request#sender()} is the person who asked. */
    public List<Request> requests(String query) {
        long me = meId();
        return requests.stream().filter(r -> r.to() == me).map(r -> new Request(r.id(), person(users.get(r.from())), r.at()))
            .filter(r -> matches(query, r.sender().fullName(), r.sender().username()))
            .sorted(Comparator.comparing(Request::sentAt).reversed()).toList();
    }

    /** Requests I sent and are still pending; here {@link Request#sender()} holds the recipient. */
    public List<Request> sent() {
        long me = meId();
        return requests.stream().filter(r -> r.from() == me).map(r -> new Request(r.id(), person(users.get(r.to())), r.at()))
            .sorted(Comparator.comparing(Request::sentAt).reversed()).toList();
    }

    /** F07. A request from a locked account can only be declined. */
    public void accept(long requestId) {
        FriendRequest request = received(requestId);
        refuseLocked(users.get(request.from()), "This account is locked. You can only decline the request.");
        requests.remove(request);
        friendships.put(Pair.of(request.from(), request.to()), now());
    }

    /** Accepts the pending request from this person (used by Find people). */
    public void acceptFrom(long personId) {
        long me = meId();
        FriendRequest request = requests.stream().filter(r -> r.from() == personId && r.to() == me).findFirst()
            .orElseThrow(() -> new ServiceException("This request is no longer pending."));
        accept(request.id());
    }

    public void decline(long requestId) {
        requests.remove(received(requestId));
    }

    public void unfriend(long personId) {
        if (friendships.remove(Pair.of(meId(), personId)) == null) throw new ServiceException("You are not friends with this person.");
    }

    /** F06d. Blocking also removes the friendship and any pending request; they can't request again. */
    public void block(long personId) {
        long me = meId();
        User target = user(personId);
        if (target.id == me) throw new ServiceException("You can't block yourself.");
        refuseLocked(target, "This account is locked. You can only unfriend it.");
        if (blocks.contains(new Block(me, personId))) throw new ServiceException("You already blocked this person.");
        friendships.remove(Pair.of(me, personId));
        requests.removeIf(r -> Pair.of(r.from(), r.to()).equals(Pair.of(me, personId)));
        blocks.add(new Block(me, personId));
    }

    /** F09. Search by username or full name; people who blocked me are never returned. */
    public List<Person> findPeople(String query) {
        if (blank(query)) return List.of();
        long me = meId();
        return users.values().stream().filter(u -> u.id != me && !blocks.contains(new Block(u.id, me)))
            .filter(u -> matches(query, u.fullName, u.username)).map(this::person)
            .sorted(Comparator.comparing(Person::fullName)).toList();
    }

    /** F06b. If they already asked me, this accepts their request instead. */
    public void sendRequest(long personId) {
        long me = meId();
        User target = user(personId);
        if (target.id == me) throw new ServiceException("You can't send a friend request to yourself.");
        if (blocks.contains(new Block(personId, me))) throw new ServiceException("You can't send a friend request to this person.");
        refuseLocked(target, "This account is locked. You can't send it a friend request.");
        switch (relation(me, personId)) {
            case BLOCKED -> throw new ServiceException("You blocked this person.");
            case FRIEND -> throw new ServiceException("You are already friends.");
            case REQUEST_SENT -> throw new ServiceException("Your request is already pending.");
            case REQUEST_RECEIVED -> acceptFrom(personId);
            case NONE -> requests.add(new FriendRequest(nextRequestId++, me, personId, now()));
        }
    }

    // ------------------------------------------------------------------ chat (F10, F11, F12f)

    /** Private and group conversations I belong to, most recent first, filtered by title. */
    public List<Chat> chats(String query) {
        long me = meId();
        return convs.values().stream().filter(c -> isMember(c, me)).map(c -> chat(c, me))
            .filter(c -> matches(query, c.title()))
            .sorted(Comparator.comparing(Chat::lastActivity).reversed()).toList();
    }

    public Chat chat(long chatId) {
        return chat(memberConv(chatId), meId());
    }

    /** Opens (or creates) my private conversation with this person. */
    public Chat chatWith(long personId) {
        long me = meId();
        User other = user(personId);
        if (other.id == me) throw new ServiceException("You can't chat with yourself.");
        if (blockedEitherWay(me, personId)) throw new ServiceException("You can't message this person.");
        refuseLocked(other, "This account is locked. You can't message it.");
        Conv conv = privateConv(me, personId);
        if (conv == null) {
            conv = new Conv(nextConvId++, false, null, now(), me);
            conv.members.put(me, new Membership(false, conv.createdAt));
            conv.members.put(personId, new Membership(false, conv.createdAt));
            convs.put(conv.id, conv);
        }
        return chat(conv, me);
    }

    /** F11c. History I can still see, oldest first. */
    public List<Message> messages(long chatId) {
        Conv conv = memberConv(chatId);
        long me = meId();
        return messages.stream().filter(m -> m.convId == conv.id && visible(m, me)).map(m -> message(m, me)).toList();
    }

    /**
     * F11a/b and F12f. A private message is delivered at once if the recipient is online, otherwise it waits
     * until they sign in. Nobody can message a person they blocked, who blocked them, or whose account is locked.
     */
    public Message send(long chatId, String text) {
        Conv conv = memberConv(chatId);
        long me = meId();
        refuseClosed(conv, me);
        if (blank(text)) throw new ServiceException("Type a message first.");
        if (text.length() > MAX_MESSAGE) throw new ServiceException("Messages can be at most " + MAX_MESSAGE + " characters.");
        LocalDateTime sentAt = now();
        LocalDateTime deliveredAt = !conv.group && users.get(otherMember(conv, me)).online ? sentAt : null;
        Msg msg = new Msg(nextMessageId++, conv.id, me, text.strip(), sentAt, conv.encrypted, deliveredAt);
        messages.add(msg);
        return message(msg, me);
    }

    /** F11e. Hides the selected messages for me only. */
    public void delete(long chatId, Collection<Long> messageIds) {
        Conv conv = memberConv(chatId);
        if (messageIds == null || messageIds.isEmpty()) throw new ServiceException("Select at least one message.");
        long me = meId();
        for (long id : messageIds) {
            Msg msg = messageById(id);
            if (msg.convId != conv.id || !visible(msg, me)) throw new ServiceException("That message is no longer in this chat.");
        }
        hidden.computeIfAbsent(me, k -> new HashSet<>()).addAll(messageIds);
    }

    /** F11d. Clears the whole history for me only; the other members keep theirs. */
    public void clearHistory(long chatId) {
        Conv conv = memberConv(chatId);
        cleared.put(new Cleared(meId(), conv.id), now());
    }

    /** F11f. Matches in one conversation, oldest first. */
    public List<SearchHit> search(long chatId, String query) {
        Conv conv = memberConv(chatId);
        if (blank(query)) return List.of();
        long me = meId();
        String title = chat(conv, me).title();
        return messages.stream().filter(m -> m.convId == conv.id && visible(m, me) && contains(m.text, query))
            .map(m -> new SearchHit(conv.id, title, message(m, me))).toList();
    }

    /** F11g. Matches across all my conversations, newest first. */
    public List<SearchHit> searchAll(String query) {
        if (blank(query)) return List.of();
        long me = meId();
        return messages.stream().filter(m -> isMember(convs.get(m.convId), me) && visible(m, me) && contains(m.text, query))
            .sorted(Comparator.comparing((Msg m) -> m.sentAt).reversed())
            .map(m -> new SearchHit(m.convId, chat(convs.get(m.convId), me).title(), message(m, me))).toList();
    }

    /** F11h. Reply ideas from the last messages (canned in the preview; an LLM call in V2). */
    public List<String> suggest(long chatId) {
        Conv conv = assistantConv(chatId);
        long me = meId();
        Msg last = null;
        for (Msg m : messages) if (m.convId == conv.id && m.senderId != me && visible(m, me)) last = m;
        if (last == null) return List.of("Hi! How are you?", "Hey, do you have a minute to talk?", "Hello! Nice to meet you here.");
        String name = users.get(last.senderId).fullName.split(" ")[0];
        String text = last.text.toLowerCase(Locale.ROOT);
        if (text.contains("?")) {
            return List.of("Yes, I can do that, " + name + ".", "Let me check and get back to you soon.",
                "Not yet, " + name + ". Can we talk about it later today?");
        }
        return List.of("Thanks " + name + "! Let me know if anything needs fixing.",
            "Great, I'll take a look after your review.", "Perfect, can we sync at 3pm?");
    }

    public static final List<String> TONES = List.of("Friendly", "Formal", "Shorter");

    /**
     * F11h. Rewrites a draft for this conversation in a tone (rule-based in the preview; an LLM call in V2).
     * Like {@link #suggest}, it is refused in encrypted conversations: the draft would leave the client unencrypted.
     */
    public String improve(long chatId, String draft, String tone) {
        assistantConv(chatId);
        if (blank(draft)) throw new ServiceException("Type a draft to improve.");
        if (!TONES.contains(tone)) throw new ServiceException("Choose a tone.");
        String text = " " + draft.strip() + " ";
        String[][] words = {{"u", "you"}, {"ur", "your"}, {"r", "are"}, {"tmr", "tomorrow"}, {"pls", "please"},
            {"plz", "please"}, {"thx", "thanks"}, {"ty", "thank you"}, {"idk", "I don't know"}, {"btw", "by the way"},
            {"msg", "message"}, {"i", "I"}, {"im", "I'm"}, {"dont", "don't"}, {"cant", "can't"}};
        for (String[] w : words) text = text.replaceAll("(?i)(?<=\\W)" + w[0] + "(?=\\W)", w[1]);
        text = text.strip().replaceAll("[.!?]+$", "");
        boolean question = draft.strip().endsWith("?") || text.matches("(?i)^(can|could|will|would|do|does|is|are|did)\\b.*");
        text = text.replaceAll("(?i)^can you", "could you").replaceAll("(?i),?\\s*please$", "");
        if (!text.isEmpty()) text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
        return switch (tone) {
            case "Formal" -> (question ? text.replaceFirst("^Could you", "Could you please") + "?" : text + ".")
                + " Thank you.";
            case "Shorter" -> text + (question ? "?" : ".");
            default -> question ? text + ", please? Thanks!" : text + ". Thanks!";
        };
    }

    /** F10. Reports a message someone sent me; admins review it on the Spam Reports page. */
    public void reportSpam(long messageId, String reason) {
        long me = meId();
        Msg msg = messageById(messageId);
        if (!isMember(convs.get(msg.convId), me) || !visible(msg, me)) throw new ServiceException("That message is no longer in this chat.");
        if (msg.senderId == me) throw new ServiceException("You can only report messages from other people.");
        if (blank(reason)) throw new ServiceException("Choose a reason.");
        if (reported(me, messageId)) throw new ServiceException("You already reported this message.");
        reports.add(new SpamReport(me, messageId, msg.senderId, reason.strip(), now()));
    }

    private boolean reported(long reporterId, long messageId) {
        return reports.stream().anyMatch(r -> r.reporterId() == reporterId && r.messageId() == messageId);
    }

    public List<SpamReport> spamReports() {
        return List.copyOf(reports);
    }

    // ------------------------------------------------------------------ groups (F12)

    /** My groups, newest first, filtered by name. */
    public List<Group> groups(String query) {
        long me = meId();
        return convs.values().stream().filter(c -> c.group && isMember(c, me) && matches(query, c.name))
            .sorted(Comparator.comparing((Conv c) -> c.createdAt).reversed()).map(c -> group(c, me)).toList();
    }

    public Group group(long groupId) {
        return group(memberGroup(groupId), meId());
    }

    /** F12a. Needs a name and at least one other existing username; I become the group admin. */
    public Group createGroup(String name, Collection<String> usernames) {
        long me = meId();
        Map<String, String> errors = new LinkedHashMap<>();
        if (blank(name)) errors.put("name", "Enter a group name.");
        else if (name.strip().length() > MAX_GROUP_NAME) errors.put("name", "Use at most " + MAX_GROUP_NAME + " characters.");
        List<User> others = new ArrayList<>();
        for (String handle : usernames == null ? List.<String>of() : usernames) {
            User user = byUsername(handle.replaceFirst("^@", "").trim());
            if (user == null) { errors.putIfAbsent("members", "No account uses @" + handle.replaceFirst("^@", "") + "."); continue; }
            if (user.id == me || others.contains(user)) continue;
            if (blockedEitherWay(me, user.id)) { errors.putIfAbsent("members", "You can't add @" + user.username + " to a group."); continue; }
            if (user.locked) { errors.putIfAbsent("members", "@" + user.username + " is locked and can't be added."); continue; }
            others.add(user);
        }
        if (others.isEmpty() && !errors.containsKey("members")) errors.put("members", "Add at least one other member.");
        if (!errors.isEmpty()) throw new ServiceException(errors);
        Conv conv = new Conv(nextConvId++, true, name.strip(), now(), me);
        conv.members.put(me, new Membership(true, conv.createdAt));
        for (User user : others) conv.members.put(user.id, new Membership(false, conv.createdAt));
        convs.put(conv.id, conv);
        return group(conv, me);
    }

    /** F12b. Any member can rename. */
    public Group rename(long groupId, String name) {
        Conv conv = memberGroup(groupId);
        if (blank(name)) throw new ServiceException("name", "Enter a group name.");
        if (name.strip().length() > MAX_GROUP_NAME) throw new ServiceException("name", "Use at most " + MAX_GROUP_NAME + " characters.");
        conv.name = name.strip();
        return group(conv, meId());
    }

    /** F12c. Any member can add people, but not a locked account. Someone removed earlier rejoins as a member. */
    public Group addMember(long groupId, String username) {
        Conv conv = memberGroup(groupId);
        long me = meId();
        User user = byUsername(trim(username).replaceFirst("^@", ""));
        if (user == null) throw new ServiceException("No account uses @" + trim(username).replaceFirst("^@", "") + ".");
        if (isMember(conv, user.id)) throw new ServiceException("@" + user.username + " is already a member.");
        if (blockedEitherWay(me, user.id)) throw new ServiceException("You can't add @" + user.username + " to a group.");
        refuseLocked(user, "@" + user.username + " is locked and can't be added.");
        Membership old = conv.members.get(user.id);
        if (old == null) {
            conv.members.put(user.id, new Membership(false, now()));
        } else {                        // one ConversationMember row per person: reuse it
            old.admin = false;
            old.joinedAt = now();
            old.leftAt = null;
        }
        return group(conv, me);
    }

    /** F12d. Group admins only; a locked member can only be removed. */
    public Group makeAdmin(long groupId, long userId) {
        Conv conv = adminGroup(groupId, "Only group admins can make admins.");
        if (!isMember(conv, userId)) throw new ServiceException("This person is not in the group.");
        Membership membership = conv.members.get(userId);
        if (membership.admin) throw new ServiceException("This person is already a group admin.");
        refuseLocked(users.get(userId), "This account is locked. You can only remove it from the group.");
        membership.admin = true;
        return group(conv, meId());
    }

    /** F12e. Group admins only. Sets leftAt, so the member's messages stay in the history. */
    public Group removeMember(long groupId, long userId) {
        Conv conv = adminGroup(groupId, "Only group admins can remove members.");
        if (userId == meId()) throw new ServiceException("You can't remove yourself.");
        if (!isMember(conv, userId)) throw new ServiceException("This person is not in the group.");
        if (activeCount(conv) <= MIN_GROUP_SIZE) {
            throw new ServiceException("A group needs at least " + MIN_GROUP_SIZE + " people. Add someone before removing "
                + users.get(userId).fullName + ".");
        }
        conv.members.get(userId).leftAt = now();
        return group(conv, meId());
    }

    /** F12g. Group admins only. New messages are stored encrypted while it is on. */
    public Group setEncrypted(long groupId, boolean encrypted) {
        Conv conv = adminGroup(groupId, "Only group admins can change encryption.");
        conv.encrypted = encrypted;
        return group(conv, meId());
    }

    // ------------------------------------------------------------------ sample-data helpers (package-private)

    User addUser(String username, String fullName, String email, String address, LocalDate birth, String gender,
                 LocalDateTime createdAt, boolean online) {
        User user = new User(nextUserId++, username, fullName, email, address, birth, gender, SampleData.PASSWORD, createdAt);
        user.online = online;
        users.put(user.id, user);
        return user;
    }

    void befriend(User a, User b, LocalDateTime since) { friendships.put(Pair.of(a.id, b.id), since); }

    void request(User from, User to, LocalDateTime at) { requests.add(new FriendRequest(nextRequestId++, from.id, to.id, at)); }

    void blockUser(User by, User target) { blocks.add(new Block(by.id, target.id)); }

    Conv privateChat(User a, User b, LocalDateTime createdAt) {
        Conv conv = new Conv(nextConvId++, false, null, createdAt, a.id);
        conv.members.put(a.id, new Membership(false, createdAt));
        conv.members.put(b.id, new Membership(false, createdAt));
        convs.put(conv.id, conv);
        return conv;
    }

    Conv groupChat(String name, User creator, LocalDateTime createdAt, boolean encrypted) {
        Conv conv = new Conv(nextConvId++, true, name, createdAt, creator.id);
        conv.encrypted = encrypted;
        conv.members.put(creator.id, new Membership(true, createdAt));
        convs.put(conv.id, conv);
        return conv;
    }

    void join(Conv conv, User user, boolean admin, LocalDateTime at) { conv.members.put(user.id, new Membership(admin, at)); }

    void leave(Conv conv, User user, LocalDateTime at) { conv.members.get(user.id).leftAt = at; }

    /** A message that reached its recipient when it was sent (group messages keep deliveredAt empty). */
    Msg post(Conv conv, User sender, LocalDateTime at, String text) {
        Msg msg = new Msg(nextMessageId++, conv.id, sender.id, text, at, conv.encrypted, conv.group ? null : at);
        messages.add(msg);
        return msg;
    }

    /** A private message still waiting for its offline recipient. */
    Msg postWaiting(Conv conv, User sender, LocalDateTime at, String text) {
        Msg msg = new Msg(nextMessageId++, conv.id, sender.id, text, at, conv.encrypted, null);
        messages.add(msg);
        return msg;
    }

    void report(User reporter, Msg msg, String reason, LocalDateTime at) {
        reports.add(new SpamReport(reporter.id, msg.id, msg.senderId, reason, at));
    }

    // ------------------------------------------------------------------ internals

    /** The preview clock moves forward one minute per action, so new items sort after the sample data. */
    private LocalDateTime now() {
        clock = clock.plusMinutes(1);
        return clock;
    }

    private long meId() {
        if (meId == null) throw new ServiceException("Sign in first.");
        return meId;
    }

    private User meUser() {
        return users.get(meId());
    }

    private User user(long id) {
        User user = users.get(id);
        if (user == null) throw new ServiceException("This account no longer exists.");
        return user;
    }

    private User byUsername(String username) {
        return users.values().stream().filter(u -> u.username.equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    private Msg messageById(long id) {
        return messages.stream().filter(m -> m.id == id).findFirst()
            .orElseThrow(() -> new ServiceException("That message is no longer in this chat."));
    }

    private FriendRequest received(long requestId) {
        long me = meId();
        return requests.stream().filter(r -> r.id() == requestId && r.to() == me).findFirst()
            .orElseThrow(() -> new ServiceException("This request is no longer pending."));
    }

    private static boolean isMember(Conv conv, long userId) {
        if (conv == null) return false;
        Membership membership = conv.members.get(userId);
        return membership != null && membership.active();
    }

    private static long activeCount(Conv conv) {
        return conv.members.values().stream().filter(Membership::active).count();
    }

    private static void refuseLocked(User user, String message) {
        if (user.locked) throw new ServiceException(message);
    }

    /** Whether I can send in this conversation: a private chat closes when either side blocks or the other is locked. */
    private Access access(Conv conv, long me) {
        if (conv.group) return Access.OPEN;
        long other = otherMember(conv, me);
        if (blocks.contains(new Block(me, other))) return Access.YOU_BLOCKED;
        if (blocks.contains(new Block(other, me))) return Access.BLOCKED_YOU;
        if (users.get(other).locked) return Access.LOCKED;
        return Access.OPEN;
    }

    private void refuseClosed(Conv conv, long me) {
        switch (access(conv, me)) {
            case YOU_BLOCKED, BLOCKED_YOU -> throw new ServiceException("You can't message this person.");
            case LOCKED -> throw new ServiceException("This account is locked. You can't message it.");
            case OPEN -> { }
        }
    }

    /** The conversation the AI assistant may read (F11h): not encrypted, and open for sending. */
    private Conv assistantConv(long chatId) {
        Conv conv = memberConv(chatId);
        if (conv.encrypted) throw new ServiceException("The AI assistant can't read encrypted conversations.");
        refuseClosed(conv, meId());
        return conv;
    }

    private Conv memberConv(long chatId) {
        Conv conv = convs.get(chatId);
        if (!isMember(conv, meId())) throw new ServiceException("You are no longer in this conversation.");
        return conv;
    }

    private Conv memberGroup(long groupId) {
        Conv conv = convs.get(groupId);
        if (conv == null || !conv.group || !isMember(conv, meId())) throw new ServiceException("You are not a member of this group.");
        return conv;
    }

    private Conv adminGroup(long groupId, String message) {
        Conv conv = memberGroup(groupId);
        if (!conv.members.get(meId()).admin) throw new ServiceException(message);
        return conv;
    }

    private Conv privateConv(long a, long b) {
        return convs.values().stream().filter(c -> !c.group && c.members.containsKey(a) && c.members.containsKey(b))
            .findFirst().orElse(null);
    }

    private static long otherMember(Conv conv, long me) {
        return conv.members.keySet().stream().filter(id -> id != me).findFirst().orElse(me);
    }

    private boolean blockedEitherWay(long a, long b) {
        return blocks.contains(new Block(a, b)) || blocks.contains(new Block(b, a));
    }

    private boolean visible(Msg msg, long userId) {
        if (hidden.getOrDefault(userId, Set.of()).contains(msg.id)) return false;
        LocalDateTime clearedAt = cleared.get(new Cleared(userId, msg.convId));
        return clearedAt == null || msg.sentAt.isAfter(clearedAt);
    }

    private Relation relation(long me, long other) {
        if (blocks.contains(new Block(me, other))) return Relation.BLOCKED;
        if (friendships.containsKey(Pair.of(me, other))) return Relation.FRIEND;
        for (FriendRequest r : requests) {
            if (r.from() == me && r.to() == other) return Relation.REQUEST_SENT;
            if (r.from() == other && r.to() == me) return Relation.REQUEST_RECEIVED;
        }
        return Relation.NONE;
    }

    private Person person(User user) {
        long me = meId();
        Relation relation = relation(me, user.id);
        LocalDateTime since = switch (relation) {
            case FRIEND -> friendships.get(Pair.of(me, user.id));
            case REQUEST_SENT, REQUEST_RECEIVED -> requests.stream()
                .filter(r -> Pair.of(r.from(), r.to()).equals(Pair.of(me, user.id))).map(FriendRequest::at).findFirst().orElse(null);
            default -> null;
        };
        return new Person(user.id, user.fullName, user.username, user.online, user.locked, relation, since);
    }

    private static Account account(User u) {
        return new Account(u.id, u.username, u.fullName, u.email, u.address, u.birthDate, u.gender, u.createdAt);
    }

    private Chat chat(Conv conv, long me) {
        Msg last = null;
        for (Msg m : messages) if (m.convId == conv.id && visible(m, me)) last = m;
        String preview = "No messages yet";
        if (last != null) {
            String who = last.senderId == me ? "You: " : conv.group ? users.get(last.senderId).fullName.split(" ")[0] + ": " : "";
            preview = who + last.text;
        }
        LocalDateTime activity = last == null ? conv.createdAt : last.sentAt;
        if (conv.group) return new Chat(conv.id, true, conv.name, preview, activity, false, conv.encrypted, Access.OPEN);
        User other = users.get(otherMember(conv, me));
        Access access = access(conv, me);
        return new Chat(conv.id, false, other.fullName, preview, activity, access.canSend() && other.online, false, access);
    }

    private Message message(Msg m, long me) {
        User sender = users.get(m.senderId);
        String name = sender == null ? "Deleted account" : sender.fullName;
        Delivery delivery = convs.get(m.convId).group ? Delivery.SENT
            : m.deliveredAt == null ? Delivery.WAITING : Delivery.DELIVERED;
        return new Message(m.id, m.convId, m.senderId, name, m.senderId == me, m.text, m.encrypted, m.sentAt,
            m.deliveredAt, delivery, reported(me, m.id));
    }

    private Group group(Conv conv, long me) {
        List<Member> members = conv.members.entrySet().stream().filter(e -> e.getValue().active()).map(e -> {
            User u = users.get(e.getKey());
            return new Member(u.id, u.fullName, u.username, e.getValue().admin, u.online, u.locked, e.getValue().joinedAt);
        }).toList();
        User creator = users.get(conv.creatorId);
        String creatorName = creator == null ? "Deleted account" : creator.fullName + " (@" + creator.username + ")";
        return new Group(conv.id, conv.name, conv.encrypted, conv.createdAt, creatorName,
            conv.members.get(me).admin, members);
    }

    private void checkEmail(Map<String, String> errors, String email, User owner) {
        if (blank(email)) { errors.put("email", "Enter your email."); return; }
        if (!validEmail(email.trim())) { errors.put("email", "Enter a valid email address."); return; }
        if (users.values().stream().anyMatch(u -> u != owner && u.email.equalsIgnoreCase(email.trim()))) {
            errors.put("email", "This email is already in use.");
        }
    }

    private static LocalDate parseBirthDate(Map<String, String> errors, String text) {
        if (blank(text)) return null;
        try {
            LocalDate date = LocalDate.parse(text.trim());
            if (!date.isBefore(NOW.toLocalDate())) throw new DateTimeException("future");
            return date;
        } catch (DateTimeException e) {
            errors.put("birthDate", "Enter a valid past date (YYYY-MM-DD).");
            return null;
        }
    }

    private static void checkNewPassword(Map<String, String> errors, String field, String confirmField,
                                         String password, String confirm) {
        if (password == null || password.length() < MIN_PASSWORD) {
            errors.put(field, "Use at least " + MIN_PASSWORD + " characters.");
        } else if (!password.equals(confirm)) {
            errors.put(confirmField, "Passwords do not match.");
        }
    }

    private String randomPassword() {
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ", lower = "abcdefghijkmnpqrstuvwxyz", digits = "23456789", symbols = "#$%&@!";
        String all = upper + lower + digits + symbols;
        List<Character> chars = new ArrayList<>();
        for (String set : List.of(upper, lower, digits, symbols)) chars.add(set.charAt(random.nextInt(set.length())));
        while (chars.size() < 10) chars.add(all.charAt(random.nextInt(all.length())));
        Collections.shuffle(chars, random);
        StringBuilder out = new StringBuilder();
        for (char c : chars) out.append(c);
        return out.toString();
    }

    static boolean validEmail(String email) {
        return email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    }

    private static boolean matches(String query, String... fields) {
        if (blank(query)) return true;
        for (String field : fields) if (field != null && contains(field, query.trim().replaceFirst("^@", ""))) return true;
        return false;
    }

    private static boolean contains(String text, String query) {
        return indexOfIgnoreCase(text, query.trim(), 0) >= 0;
    }

    /**
     * Where {@code query} next occurs in {@code text} from {@code from}, ignoring case, or -1. Compares
     * character by character, so the index is valid in {@code text} itself (lower-casing the whole string
     * can change its length, e.g. "İ"). Search and the result highlight both use this one rule.
     */
    public static int indexOfIgnoreCase(String text, String query, int from) {
        if (query.isEmpty()) return -1;
        for (int i = Math.max(0, from); i + query.length() <= text.length(); i++) {
            if (text.regionMatches(true, i, query, 0, query.length())) return i;
        }
        return -1;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
