package chatapp.user.preview;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Read-only values handed from the preview server to the UI. */
public final class Views {
    private Views() {}

    public static final List<String> GENDERS = List.of("Male", "Female", "Other");

    public record Account(long id, String username, String fullName, String email, String address,
                          LocalDate birthDate, String gender, LocalDateTime createdAt) {}

    public enum Relation {
        FRIEND("Friend"), REQUEST_SENT("Request sent"), REQUEST_RECEIVED("Request received"),
        NONE("Not connected"), BLOCKED("Blocked by you");

        private final String label;
        Relation(String label) { this.label = label; }
        public String label() { return label; }
    }

    /**
     * Someone else. {@code locked} means an administrator locked the account (F13): it can't sign in, and
     * nobody can chat with it, send or accept its friend requests, or add it to a group.
     */
    public record Person(long id, String fullName, String username, boolean online, boolean locked, Relation relation,
                         LocalDateTime since) {}

    public record Request(long id, Person sender, LocalDateTime sentAt) {}

    /** Whether I can send messages in a conversation; group chats are always {@link #OPEN} to members. */
    public enum Access {
        OPEN, YOU_BLOCKED, BLOCKED_YOU, LOCKED;

        public boolean canSend() { return this == OPEN; }
    }

    /** {@code online} is false whenever {@code access} isn't {@link Access#OPEN}. */
    public record Chat(long id, boolean group, String title, String preview, LocalDateTime lastActivity,
                       boolean online, boolean encrypted, Access access) {}

    /**
     * Status of a message I sent (F11a/b). {@code SENT}: the server has it (group messages, and in V2 the
     * moment before delivery). {@code WAITING}: the recipient is offline. {@code DELIVERED}: the recipient
     * has it, at once if they were online or when they next signed in.
     */
    public enum Delivery {
        SENT("Sent"), WAITING("Waiting"), DELIVERED("Delivered");

        private final String label;
        Delivery(String label) { this.label = label; }
        public String label() { return label; }
    }

    /** {@code deliveredAt} is null until a private message reaches its recipient (always null in groups). */
    public record Message(long id, long conversationId, long senderId, String senderName, boolean mine,
                          String text, boolean encrypted, LocalDateTime sentAt, LocalDateTime deliveredAt,
                          Delivery delivery, boolean reportedByMe) {}

    public record SearchHit(long conversationId, String conversationTitle, Message message) {}

    public record Member(long userId, String fullName, String username, boolean admin, boolean online, boolean locked,
                         LocalDateTime joinedAt) {}

    public record Group(long id, String name, boolean encrypted, LocalDateTime createdAt, String creatorName,
                        boolean meAdmin, List<Member> members) {}

    public record Email(String to, String subject, String body, LocalDateTime sentAt) {}
}
