package chatapp.user.preview;

import chatapp.user.preview.PreviewServer.Conv;
import chatapp.user.preview.PreviewServer.Msg;
import chatapp.user.preview.PreviewServer.User;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sample data for the User preview. It is the same data set as the Admin client's
 * {@code chatapp.admin.ui.preview.PreviewData} (people, created dates, locked accounts, friendships,
 * groups, messages and spam reports); change both together. Every account uses {@link #PASSWORD}.
 * <p>
 * Signed-in user: Maya Chen (@mayachen). Her friends are Noah and Ava (online), Ethan (offline) and
 * Linh (locked by an administrator). Sofia and Owen (locked) sent her requests; she sent one to
 * Isabel. Maya blocked Daniel; Zoe blocked Maya. Private chats with Noah, Ethan (the spam message),
 * Ava and Linh. Groups: Project Team (Maya is a group admin; Olivia was removed), Java Study Group
 * (encrypted; Maya is a member), Campus Events and Class Announcements.
 */
final class SampleData {
    static final String PASSWORD = PreviewServer.SAMPLE_PASSWORD;

    private SampleData() {}

    private static LocalDateTime at(int month, int day, int hour, int minute) {
        return LocalDateTime.of(2026, month, day, hour, minute);
    }

    static void load(PreviewServer s) {
        User maya = s.addUser("mayachen", "Maya Chen", "maya.chen@example.com", "18 Nguyen Van Cu, Ho Chi Minh City",
            LocalDate.of(2003, 6, 14), "Female", at(11, 8, 14, 20), false);
        User noah = s.addUser("noahp", "Noah Patel", "noah.patel@example.com", "",
            LocalDate.of(2002, 3, 2), "Male", at(11, 7, 11, 34), true);
        User linh = s.addUser("linhng", "Linh Nguyen", "linh.nguyen@example.com", "District 5, Ho Chi Minh City",
            LocalDate.of(2004, 9, 9), "Female", at(11, 5, 9, 10), false);
        User ava = s.addUser("avab", "Ava Brooks", "ava.brooks@example.com", "",
            LocalDate.of(2003, 1, 21), "Female", at(11, 4, 16, 42), true);
        User ethan = s.addUser("ethanp", "Ethan Park", "ethan.park@example.com", "",
            null, "Male", at(11, 2, 10, 0), false);
        User sofia = s.addUser("sofiareyes", "Sofia Reyes", "sofia.reyes@example.com", "",
            LocalDate.of(2003, 11, 30), "Female", at(10, 30, 9, 0), false);
        User owen = s.addUser("owentran", "Owen Tran", "owen.tran@example.com", "",
            null, "Male", at(10, 28, 9, 0), false);
        User isabel = s.addUser("isabelc", "Isabel Cruz", "isabel.cruz@example.com", "",
            null, "Female", at(10, 24, 9, 0), true);
        s.addUser("analopez", "Ana Lopez", "ana.lopez@example.com", "",
            null, "Female", at(11, 2, 15, 0), true);
        s.addUser("davelin", "Dave Lin", "dave.lin@example.com", "",
            null, "Male", at(11, 3, 9, 30), false);
        User daniel = s.addUser("dkang", "Daniel Kang", "daniel.kang@example.com", "",
            null, "Male", at(11, 4, 18, 0), false);
        User zoe = s.addUser("zoem", "Zoe Martin", "zoe.martin@example.com", "",
            null, "Female", at(11, 5, 7, 45), true);
        User ryan = s.addUser("ryanh", "Ryan Hall", "ryan.hall@example.com", "",
            null, "Male", at(10, 15, 12, 0), false);
        User olivia = s.addUser("oliviar", "Olivia Reed", "olivia.reed@example.com", "",
            null, "Female", at(11, 6, 10, 0), false);
        // Locked by an administrator (Admin › Account History): Ryan 20/10, Linh 08/11 17:15, Owen 10/11.
        linh.locked = true;
        owen.locked = true;
        ryan.locked = true;

        // Friends (F06), requests (F07), blocks (F06d / F09)
        s.befriend(maya, noah, at(11, 8, 16, 20));
        s.befriend(maya, ava, at(11, 8, 16, 40));
        s.befriend(maya, linh, at(11, 8, 16, 50));
        s.befriend(maya, ethan, at(11, 8, 17, 0));
        s.befriend(noah, linh, at(11, 7, 12, 0));
        s.befriend(noah, sofia, at(11, 7, 12, 0));
        s.befriend(ava, ethan, at(11, 4, 17, 0));
        s.befriend(ava, owen, at(11, 5, 12, 0));
        s.befriend(ethan, isabel, at(11, 2, 12, 0));
        s.befriend(sofia, isabel, at(10, 31, 12, 0));
        s.request(sofia, maya, at(11, 8, 21, 30));
        s.request(owen, maya, at(11, 9, 8, 42));
        s.request(maya, isabel, at(11, 9, 9, 30));
        s.blockUser(maya, daniel);
        s.blockUser(zoe, maya);

        // Private chats (F11). Every message is delivered at once except Maya's last one to Ethan.
        Conv withIsabelSofia = s.privateChat(isabel, sofia, at(10, 31, 13, 0));
        s.post(withIsabelSofia, isabel, at(10, 31, 13, 0), "Are you coming to the club fair?");

        Conv withLinhIsabel = s.privateChat(linh, isabel, at(11, 8, 15, 50));
        Msg offer = s.post(withLinhIsabel, linh, at(11, 8, 15, 50), "See this offer.");

        Conv withLinh = s.privateChat(linh, maya, at(11, 8, 16, 55));
        s.post(withLinh, linh, at(11, 8, 16, 55), "Can you share the class slides?");
        s.post(withLinh, maya, at(11, 8, 17, 5), "Sure, I'll send the slides tonight.");

        Conv withAva = s.privateChat(ava, maya, at(11, 8, 17, 20));
        s.post(withAva, ava, at(11, 8, 17, 20), "Draft screenshots are in the folder.");
        s.post(withAva, ava, at(11, 9, 8, 50), "I sent you the screenshot list.");

        Conv withEthanAva = s.privateChat(ethan, ava, at(11, 8, 18, 10));
        Msg deal = s.post(withEthanAva, ethan, at(11, 8, 18, 10), "I have another deal for you.");

        Conv withOwenNoah = s.privateChat(owen, noah, at(11, 9, 8, 40));
        Msg join = s.post(withOwenNoah, owen, at(11, 9, 8, 40), "Join our offers group today.");

        Conv withNoah = s.privateChat(maya, noah, at(11, 9, 9, 58));
        s.post(withNoah, noah, at(11, 9, 9, 58), "Hi Maya, did you get the draft report?");
        s.post(withNoah, maya, at(11, 9, 10, 5), "Yes! I'm reading the report now.");
        s.post(withNoah, noah, at(11, 9, 10, 8), "I'll review the database section today.");

        Conv withEthan = s.privateChat(ethan, maya, at(11, 9, 10, 12));
        s.post(withEthan, ethan, at(11, 9, 10, 12), "Hey, are you free this weekend?");
        Msg promo = s.post(withEthan, ethan, at(11, 9, 10, 16), "Buy now! Reply for the promo code.");
        s.postWaiting(withEthan, maya, at(11, 9, 10, 18), "Who is this?");

        // Spam reports (F10), as the Admin Spam Reports page lists them (F16)
        s.report(isabel, offer, "Unwanted promotions or ads", at(11, 8, 16, 5));
        s.report(ava, deal, "Unwanted promotions or ads", at(11, 8, 18, 31));
        s.report(noah, join, "Repeated unsolicited messages", at(11, 9, 8, 46));
        s.report(maya, promo, "Unwanted promotions or ads", at(11, 9, 10, 20));

        // Groups (F12). Project Team was renamed from "Java Project"; Noah removed Olivia on 09/11.
        Conv project = s.groupChat("Project Team", noah, at(11, 7, 11, 50), false);
        s.join(project, noah, true, at(11, 7, 12, 6));
        s.join(project, linh, false, at(11, 7, 12, 12));
        s.join(project, ava, false, at(11, 7, 12, 18));
        s.join(project, ethan, false, at(11, 7, 12, 24));
        s.join(project, sofia, false, at(11, 7, 12, 30));
        s.join(project, owen, false, at(11, 7, 12, 36));
        s.join(project, isabel, false, at(11, 7, 12, 42));
        s.join(project, olivia, false, at(11, 7, 12, 54));
        s.join(project, maya, true, at(11, 8, 15, 5));
        s.leave(project, olivia, at(11, 9, 10, 6));
        s.post(project, maya, at(11, 8, 15, 20), "I've uploaded the draft report.");
        s.post(project, noah, at(11, 8, 15, 28), "I'll review the database section today.");
        s.post(project, ava, at(11, 9, 8, 45), "The screenshot list is ready.");
        s.post(project, maya, at(11, 9, 9, 2), "Thanks, I'll check it against the brief.");
        s.post(project, noah, at(11, 9, 9, 30), "I added my comments to the draft.");

        Conv study = s.groupChat("Java Study Group", sofia, at(10, 30, 19, 0), true);
        s.join(study, sofia, true, at(10, 30, 19, 0));
        s.join(study, ethan, false, at(11, 2, 12, 0));
        s.join(study, noah, false, at(11, 7, 12, 0));
        s.join(study, maya, false, at(11, 8, 16, 0));
        s.post(study, sofia, at(11, 9, 18, 10), "Quiz review at 7pm?");
        s.post(study, noah, at(11, 9, 18, 12), "Works for me.");
        s.post(study, maya, at(11, 9, 18, 15), "I'll bring the practice questions.");
        s.post(study, ethan, at(11, 9, 18, 16), "Thanks Maya!");

        Conv campus = s.groupChat("Campus Events", isabel, at(11, 1, 10, 0), false);
        s.join(campus, isabel, true, at(11, 1, 10, 0));
        s.join(campus, ava, false, at(11, 4, 17, 0));
        s.join(campus, maya, false, at(11, 9, 10, 30));
        s.post(campus, ava, at(11, 6, 14, 0), "The music night moved to Friday.");

        Conv announcements = s.groupChat("Class Announcements", isabel, at(10, 24, 9, 30), false);
        s.join(announcements, isabel, true, at(10, 24, 9, 30));
        s.join(announcements, sofia, false, at(10, 30, 12, 0));
        s.join(announcements, linh, false, at(11, 5, 12, 0));
        s.join(announcements, maya, false, at(11, 8, 16, 10));
        s.post(announcements, isabel, at(11, 5, 7, 30), "Final project reports are due on Nov 27.");
    }
}
