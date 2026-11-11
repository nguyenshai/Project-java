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
    public record Group(String name, LocalDate created, String creatorId, List<Member> members) {}
    public record Event(String group, LocalDateTime at, String type, String detail) {}
    public record Message(String sender, String recipient, String group, LocalDateTime at) {}
    public record Report(int number, String reported, String reporter, LocalDateTime at, String reason, String message) {}
    public record LockAction(String userId, LocalDateTime at, boolean locked) {}
    public record Activity(int signIns, int opens, int people, int groups) {}

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

    public PreviewData() {
        add("mayachen", "Maya Chen", "maya.chen", "11-08T14:20", false);
        add("noahp", "Noah Patel", "noah.patel", "11-07T11:34", false);
        add("linhng", "Linh Nguyen", "linh.nguyen", "11-05T09:10", true);
        add("avab", "Ava Brooks", "ava.brooks", "11-04T16:42", false);
        add("ethanp", "Ethan Park", "ethan.park", "11-02T10:00", false);
        add("sofiareyes", "Sofia Reyes", "sofia.reyes", "10-30T09:00", false);
        add("owentran", "Owen Tran", "owen.tran", "10-28T09:00", true);
        add("isabelc", "Isabel Cruz", "isabel.cruz", "10-24T09:00", false);
        addSignIns("mayachen", "11-08T16:05", "11-08T18:47", "11-09T09:12");
        addSignIns("noahp", "11-08T13:28", "11-09T10:04");
        addSignIns("avab", "11-08T09:41", "11-09T08:46");
        addSignIns("ethanp", "11-09T08:03");
        addSignIns("sofiareyes", "10-30T11:00", "10-31T10:00", "11-08T21:16");
        addSignIns("owentran", "10-28T11:00", "10-29T10:00");
        addSignIns("isabelc", "10-24T11:00", "10-26T10:00", "11-08T16:32");
        for (String id : List.of("sofiareyes", "owentran", "isabelc"))
            appOpens.add(new AppOpen(id, user(id).created().toLocalDate()));
        String[] openDays = {"11-08", "11-08", "11-06", "11-08", "11-09", "11-08", "11-10", "11-08"};
        int[] opens = {8, 6, 2, 5, 4, 2, 1, 1};
        for (int i = 0; i < users.size(); i++) {
            for (int n = 0; n < opens[i]; n++) appOpens.add(new AppOpen(users.get(i).id(), date(openDays[i])));
        }
        connect("mayachen", "noahp", "11-08");
        connect("mayachen", "linhng", "11-09");
        connect("mayachen", "avab", "11-08");
        connect("mayachen", "ethanp", "11-08");
        connect("noahp", "linhng", "11-07");
        connect("noahp", "sofiareyes", "11-07");
        connect("avab", "ethanp", "11-04");
        connect("avab", "owentran", "11-05");
        connect("ethanp", "isabelc", "11-02");
        connect("sofiareyes", "isabelc", "10-31");

        List<Member> project = users.stream().map(u -> new Member(u.id(),
            Set.of("mayachen", "noahp").contains(u.id()) ? "Group admin" : "Member",
            date(u.id().equals("mayachen") ? "11-08" : "11-07"))).toList();
        groups.add(new Group("Project Team", date("11-07"), "noahp", project));
        addGroup("Campus Events", "11-01", "isabelc", "mayachen", "avab");
        addGroup("Java Study Group", "10-30", "sofiareyes", "mayachen", "noahp", "ethanp");
        addGroup("Class Announcements", "10-24", "isabelc", "mayachen", "linhng", "sofiareyes");
        for (int i = 0; i < project.size(); i++) {
            Member member = project.get(i);
            LocalDateTime at = member.joined().atTime(member.userId().equals("mayachen") ? 15 : 12,
                    member.userId().equals("mayachen") ? 5 : i * 6);
            events.add(new Event("Project Team", at, "Member joined", name(member.userId()) + " joined the group."));
        }
        event("11-07T12:54", "Member joined", "Olivia Reed joined the group.");
        event("11-08T16:14", "Group renamed", "Noah Patel renamed Java Project to Project Team.");
        event("11-09T10:06", "Member removed", "Noah Patel removed Olivia Reed.");
        groupMessage("mayachen", "11-08T15:20", "I’ve uploaded the draft report.");
        groupMessage("noahp", "11-08T15:28", "I’ll review the database section today.");
        groupMessage("avab", "11-09T08:45", "The screenshot list is ready.");
        groupMessage("mayachen", "11-09T09:02", "Thanks, I’ll check it against the brief.");
        groupMessage("noahp", "11-09T09:30", "I added my comments to the draft.");
        messages.add(new Message("ethanp", "avab", "", at("11-08T18:10")));
        messages.add(new Message("ethanp", "mayachen", "", at("11-09T10:16")));
        messages.add(new Message("mayachen", "noahp", "", at("11-09T10:05")));
        messages.add(new Message("isabelc", "sofiareyes", "", at("10-31T13:00")));
        report(1042, "ethanp", "mayachen", "11-09T10:20", "Repeated unsolicited promotions", "Buy now! Reply for the promo code.");
        report(1041, "owentran", "noahp", "11-09T08:46", "Repeated unsolicited messages", "Join our offers group today.");
        report(1040, "ethanp", "avab", "11-08T18:31", "Unwanted promotional links", "I have another deal for you.");
        report(1039, "linhng", "isabelc", "11-08T16:05", "Repeated promotional messages", "See this offer.");
        locks.add(new LockAction("ethanp", at("11-04T17:02"), true));
        locks.add(new LockAction("ethanp", at("11-05T09:15"), false));
        locks.add(new LockAction("linhng", at("11-08T17:15"), true));
        locks.add(new LockAction("owentran", at("11-10T08:20"), true));
    }

    private void add(String id, String name, String email, String created, boolean locked) {
        boolean maya = id.equals("mayachen");
        users.add(new User(id, name, id, email + "@example.com",
            maya ? "18 Nguyen Van Cu, Ho Chi Minh City" : "",
            maya ? "2003-06-14" : "", maya ? "Female" : "Not specified", at(created), locked));
    }

    private void addSignIns(String id, String... times) {
        for (String time : times) signIns.add(new SignIn(id, at(time)));
    }

    private void connect(String a, String b, String since) { friends.add(new Friend(a, b, date(since))); }

    private void addGroup(String name, String created, String creator, String... memberIds) {
        List<Member> members = new ArrayList<>();
        members.add(new Member(creator, "Group admin", date(created)));
        for (String id : memberIds) {
            LocalDate joined = id.equals("mayachen") ? date(name.equals("Campus Events") ? "11-09" : "11-08")
                : Collections.max(List.of(date(created), user(id).created().toLocalDate()));
            members.add(new Member(id, "Member", joined));
        }
        groups.add(new Group(name, date(created), creator, members));
    }

    private void event(String time, String type, String detail) {
        events.add(new Event("Project Team", at(time), type, detail));
    }

    private void groupMessage(String user, String time, String content) {
        messages.add(new Message(user, "", "Project Team", at(time)));
        event(time, "Message", name(user) + ": " + content);
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
        Set<String> contacts = new HashSet<>(), groupChats = new HashSet<>();
        for (Message m : messages) {
            if (!range.includes(m.at().toLocalDate())) continue;
            if (m.sender().equals(id)) {
                if (m.group().isEmpty()) contacts.add(m.recipient());
                else groupChats.add(m.group());
            } else if (m.recipient().equals(id)) contacts.add(m.sender());
        }
        return new Activity(sign, open, contacts.size(), groupChats.size());
    }

    private static LocalDate date(String s) { return LocalDate.parse("2026-" + s); }
    private static LocalDateTime at(String s) { return LocalDateTime.parse("2026-" + s); }
}
