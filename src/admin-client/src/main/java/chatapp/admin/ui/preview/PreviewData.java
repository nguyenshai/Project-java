package chatapp.admin.ui.preview;

import chatapp.admin.ui.shared.PeriodPicker.Range;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public final class PreviewData {
    public record User(String id, String name, String username, String email, String address,
                       String birth, String gender, LocalDateTime created, boolean locked) {
        public User withLock(boolean value) {
            return new User(id, name, username, email, address, birth, gender, created, value);
        }
    }
    public record SignIn(String userId, LocalDateTime at) {}
    public record AppOpen(String userId, LocalDate date) {}
    public record Friend(String first, String second, LocalDate since) {
        public String other(String id) { return first.equals(id) ? second : first; }
    }
    public record Member(String userId, String role, LocalDate joined) {}
    /** A chat group, identified by its conversation id; names are not unique. */
    public record Group(long id, String name, LocalDate created, String creatorId, List<Member> members) {}
    public record Event(long groupId, LocalDateTime at, String type, String detail) {}
    /** {@code groupId} is null for a private message. */
    public record Message(String sender, String recipient, Long groupId, LocalDateTime at) {}
    public record Report(int number, String reported, String reporter, LocalDateTime at, String reason, String message) {}
    public record LockAction(String userId, LocalDateTime at, boolean locked) {}
    public record Activity(int signIns, int opens, int people, int groups) {}

    /** Display name of the signed-in administrator until accounts come from the server. */
    public static final String ADMIN_NAME = "Admin";
    private static final long PROJECT_TEAM = 104;
    public static final LocalDateTime NOW = LocalDateTime.parse("2026-11-11T12:00");
    public final List<User> users = new ArrayList<>();
    public final List<SignIn> signIns = new ArrayList<>();
    public final List<AppOpen> appOpens = new ArrayList<>();
    public final List<Friend> friends = new ArrayList<>();
    public final List<Group> groups = new ArrayList<>();
    public final List<Event> events = new ArrayList<>();
    public final List<Message> messages = new ArrayList<>();
    public final List<Report> reports = new ArrayList<>();
    public final List<LockAction> locks = new ArrayList<>();

    /**
     * The shared sample data set. The User client's {@code chatapp.user.preview.SampleData} holds the same
     * people, friendships, groups, messages and spam reports; change both together.
     */
    public PreviewData() {
        add("mayachen", "Maya Chen", "maya.chen", "11-08T14:20", "Female", "2003-06-14", "18 Nguyen Van Cu, Ho Chi Minh City", false);
        add("noahp", "Noah Patel", "noah.patel", "11-07T11:34", "Male", "2002-03-02", "", false);
        add("linhng", "Linh Nguyen", "linh.nguyen", "11-05T09:10", "Female", "2004-09-09", "District 5, Ho Chi Minh City", true);
        add("avab", "Ava Brooks", "ava.brooks", "11-04T16:42", "Female", "2003-01-21", "", false);
        add("ethanp", "Ethan Park", "ethan.park", "11-02T10:00", "Male", "", "", false);
        add("sofiareyes", "Sofia Reyes", "sofia.reyes", "10-30T09:00", "Female", "2003-11-30", "", false);
        add("owentran", "Owen Tran", "owen.tran", "10-28T09:00", "Male", "", "", true);
        add("isabelc", "Isabel Cruz", "isabel.cruz", "10-24T09:00", "Female", "", "", false);
        add("analopez", "Ana Lopez", "ana.lopez", "11-02T15:00", "Female", "", "", false);
        add("davelin", "Dave Lin", "dave.lin", "11-03T09:30", "Male", "", "", false);
        add("dkang", "Daniel Kang", "daniel.kang", "11-04T18:00", "Male", "", "", false);
        add("zoem", "Zoe Martin", "zoe.martin", "11-05T07:45", "Female", "", "", false);
        add("ryanh", "Ryan Hall", "ryan.hall", "10-15T12:00", "Male", "", "", true);
        add("oliviar", "Olivia Reed", "olivia.reed", "11-06T10:00", "Female", "", "", false);
        addSignIns("mayachen", "11-08T16:05", "11-08T18:47", "11-09T09:12");
        addSignIns("noahp", "11-08T13:28", "11-09T10:04");
        addSignIns("linhng", "11-05T09:15", "11-08T15:40");
        addSignIns("avab", "11-08T09:41", "11-09T08:46");
        addSignIns("ethanp", "11-09T08:03");
        addSignIns("sofiareyes", "10-30T11:00", "10-31T10:00", "11-08T21:16");
        addSignIns("owentran", "10-28T11:00", "10-29T10:00", "11-09T08:30");
        addSignIns("isabelc", "10-24T11:00", "10-26T10:00", "11-08T16:32");
        addSignIns("analopez", "11-02T15:05", "11-10T19:00");
        addSignIns("davelin", "11-03T09:35");
        addSignIns("dkang", "11-04T18:05");
        addSignIns("zoem", "11-05T07:50", "11-10T20:10");
        addSignIns("ryanh", "10-15T12:05", "10-18T09:00");
        addSignIns("oliviar", "11-06T10:05", "11-07T12:50");
        for (String id : List.of("sofiareyes", "owentran", "isabelc"))
            appOpens.add(new AppOpen(id, user(id).created().toLocalDate()));
        addOpens("mayachen", "11-08", 8);
        addOpens("noahp", "11-08", 6);
        addOpens("linhng", "11-06", 2);
        addOpens("linhng", "11-08", 1);
        addOpens("avab", "11-08", 5);
        addOpens("ethanp", "11-09", 4);
        addOpens("sofiareyes", "11-08", 2);
        addOpens("owentran", "11-09", 1);
        addOpens("isabelc", "11-08", 1);
        addOpens("analopez", "11-10", 2);
        addOpens("davelin", "11-03", 1);
        addOpens("dkang", "11-04", 1);
        addOpens("zoem", "11-10", 2);
        addOpens("ryanh", "10-18", 1);
        addOpens("oliviar", "11-07", 2);
        connect("mayachen", "noahp", "11-08");
        connect("mayachen", "linhng", "11-08");
        connect("mayachen", "avab", "11-08");
        connect("mayachen", "ethanp", "11-08");
        connect("noahp", "linhng", "11-07");
        connect("noahp", "sofiareyes", "11-07");
        connect("avab", "ethanp", "11-04");
        connect("avab", "owentran", "11-05");
        connect("ethanp", "isabelc", "11-02");
        connect("sofiareyes", "isabelc", "10-31");

        // Project Team: everyone in the original eight accounts; Maya and Noah are group admins.
        List<String> team = List.of("mayachen", "noahp", "linhng", "avab", "ethanp", "sofiareyes", "owentran", "isabelc");
        List<Member> project = team.stream().map(id -> new Member(id,
            Set.of("mayachen", "noahp").contains(id) ? "Group admin" : "Member",
            date(id.equals("mayachen") ? "11-08" : "11-07"))).toList();
        groups.add(new Group(PROJECT_TEAM, "Project Team", date("11-07"), "noahp", project));
        addGroup(103, "Campus Events", "11-01", "isabelc", "mayachen", "avab");
        addGroup(102, "Java Study Group", "10-30", "sofiareyes", "mayachen", "noahp", "ethanp");
        addGroup(101, "Class Announcements", "10-24", "isabelc", "mayachen", "linhng", "sofiareyes");
        for (int i = 0; i < project.size(); i++) {
            Member member = project.get(i);
            LocalDateTime at = member.joined().atTime(member.userId().equals("mayachen") ? 15 : 12,
                    member.userId().equals("mayachen") ? 5 : i * 6);
            events.add(new Event(PROJECT_TEAM, at, "Member joined", name(member.userId()) + " joined the group."));
        }
        event("11-07T12:54", "Member joined", "Olivia Reed joined the group.");
        event("11-08T16:14", "Group renamed", "Noah Patel renamed Java Project to Project Team.");
        event("11-09T10:06", "Member removed", "Noah Patel removed Olivia Reed.");
        groupMessage(PROJECT_TEAM, "mayachen", "11-08T15:20", "I've uploaded the draft report.");
        groupMessage(PROJECT_TEAM, "noahp", "11-08T15:28", "I'll review the database section today.");
        groupMessage(PROJECT_TEAM, "avab", "11-09T08:45", "The screenshot list is ready.");
        groupMessage(PROJECT_TEAM, "mayachen", "11-09T09:02", "Thanks, I'll check it against the brief.");
        groupMessage(PROJECT_TEAM, "noahp", "11-09T09:30", "I added my comments to the draft.");
        // Java Study Group is encrypted, so the admin log does not show what was written.
        groupMessage(102, "sofiareyes", "11-09T18:10", null);
        groupMessage(102, "noahp", "11-09T18:12", null);
        groupMessage(102, "mayachen", "11-09T18:15", null);
        groupMessage(102, "ethanp", "11-09T18:16", null);
        groupMessage(103, "avab", "11-06T14:00", "The music night moved to Friday.");
        groupMessage(101, "isabelc", "11-05T07:30", "Final project reports are due on Nov 27.");
        privateMessage("isabelc", "sofiareyes", "10-31T13:00");
        privateMessage("avab", "mayachen", "11-08T17:20");
        privateMessage("linhng", "isabelc", "11-08T15:50");
        privateMessage("linhng", "mayachen", "11-08T16:55");
        privateMessage("mayachen", "linhng", "11-08T17:05");
        privateMessage("ethanp", "avab", "11-08T18:10");
        privateMessage("owentran", "noahp", "11-09T08:40");
        privateMessage("avab", "mayachen", "11-09T08:50");
        privateMessage("noahp", "mayachen", "11-09T09:58");
        privateMessage("mayachen", "noahp", "11-09T10:05");
        privateMessage("noahp", "mayachen", "11-09T10:08");
        privateMessage("ethanp", "mayachen", "11-09T10:12");
        privateMessage("ethanp", "mayachen", "11-09T10:16");
        privateMessage("mayachen", "ethanp", "11-09T10:18");
        // Reasons use the choices of the User client's Report spam dialog.
        report(1042, "ethanp", "mayachen", "11-09T10:20", "Unwanted promotions or ads", "Buy now! Reply for the promo code.");
        report(1041, "owentran", "noahp", "11-09T08:46", "Repeated unsolicited messages", "Join our offers group today.");
        report(1040, "ethanp", "avab", "11-08T18:31", "Unwanted promotions or ads", "I have another deal for you.");
        report(1039, "linhng", "isabelc", "11-08T16:05", "Unwanted promotions or ads", "See this offer.");
        locks.add(new LockAction("ryanh", at("10-20T10:00"), true));
        locks.add(new LockAction("ethanp", at("11-04T17:02"), true));
        locks.add(new LockAction("ethanp", at("11-05T09:15"), false));
        locks.add(new LockAction("linhng", at("11-08T17:15"), true));
        locks.add(new LockAction("owentran", at("11-10T08:20"), true));
    }

    private void add(String id, String name, String email, String created, String gender, String birth, String address,
                     boolean locked) {
        users.add(new User(id, name, id, email + "@example.com", address, birth, gender, at(created), locked));
    }

    private void addOpens(String id, String day, int count) {
        for (int n = 0; n < count; n++) appOpens.add(new AppOpen(id, date(day)));
    }

    private void privateMessage(String sender, String recipient, String time) {
        messages.add(new Message(sender, recipient, null, at(time)));
    }

    private void addSignIns(String id, String... times) {
        for (String time : times) signIns.add(new SignIn(id, at(time)));
    }

    private void connect(String a, String b, String since) { friends.add(new Friend(a, b, date(since))); }

    private void addGroup(long groupId, String name, String created, String creator, String... memberIds) {
        List<Member> members = new ArrayList<>();
        members.add(new Member(creator, "Group admin", date(created)));
        for (String id : memberIds) {
            LocalDate joined = id.equals("mayachen") ? date(name.equals("Campus Events") ? "11-09" : "11-08")
                : Collections.max(List.of(date(created), user(id).created().toLocalDate()));
            members.add(new Member(id, "Member", joined));
        }
        groups.add(new Group(groupId, name, date(created), creator, members));
    }

    private void event(String time, String type, String detail) {
        events.add(new Event(PROJECT_TEAM, at(time), type, detail));
    }

    /** {@code content} is null for an encrypted group: the event only records that a message was sent. */
    private void groupMessage(long groupId, String user, String time, String content) {
        messages.add(new Message(user, "", groupId, at(time)));
        events.add(new Event(groupId, at(time), "Message",
            name(user) + (content == null ? " sent an encrypted message." : ": " + content)));
    }

    private void report(int number, String subject, String reporter, String when, String reason, String content) {
        reports.add(new Report(number, subject, reporter, at(when), reason, content));
    }

    public User user(String id) { return users.stream().filter(u -> u.id().equals(id)).findFirst().orElse(null); }
    public String name(String id) { User u = user(id); return u == null ? "Deleted account" : u.name(); }
    public String handle(String id) { User u = user(id); return u == null ? "—" : "@" + u.username(); }
    public String status(String id) { User u = user(id); return u == null ? "—" : u.locked() ? "Locked" : "Active"; }

    public void save(User u) {
        users.removeIf(old -> old.id().equals(u.id()));
        users.add(u);
    }

    public void lock(String id, boolean value) {
        User u = user(id);
        if (u != null && u.locked() != value) {
            save(u.withLock(value));
            locks.add(new LockAction(id, NOW, value));
        }
    }

    public List<Friend> friendships(String id) {
        return friends.stream().filter(f -> (f.first().equals(id) || f.second().equals(id)) && user(f.other(id)) != null).toList();
    }

    public Map<String, Set<String>> mutualFriends(String id) {
        Set<String> direct = new HashSet<>();
        for (Friend edge : friendships(id)) direct.add(edge.other(id));
        Map<String, Set<String>> result = new TreeMap<>();
        for (String friend : direct) {
            for (Friend edge : friendships(friend)) {
                String candidate = edge.other(friend);
                if (!candidate.equals(id) && !direct.contains(candidate))
                    result.computeIfAbsent(candidate, k -> new TreeSet<>()).add(friend);
            }
        }
        return result;
    }

    public Activity activity(String id, Range range) {
        int sign = (int) signIns.stream().filter(e -> e.userId().equals(id) && range.includes(e.at().toLocalDate())).count();
        int open = (int) appOpens.stream().filter(e -> e.userId().equals(id) && range.includes(e.date())).count();
        Set<String> contacts = new HashSet<>();
        Set<Long> groupChats = new HashSet<>();
        for (Message m : messages) {
            if (!range.includes(m.at().toLocalDate())) continue;
            if (m.sender().equals(id)) {
                if (m.groupId() == null) contacts.add(m.recipient());
                else groupChats.add(m.groupId());
            } else if (m.recipient().equals(id)) contacts.add(m.sender());
        }
        return new Activity(sign, open, contacts.size(), groupChats.size());
    }

    private static LocalDate date(String s) { return LocalDate.parse("2026-" + s); }
    private static LocalDateTime at(String s) { return LocalDateTime.parse("2026-" + s); }
}
