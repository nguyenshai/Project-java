package chatapp.user;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.ServiceException;
import chatapp.user.preview.Views.Access;
import chatapp.user.preview.Views.Chat;
import chatapp.user.preview.Views.Delivery;
import chatapp.user.preview.Views.Group;
import chatapp.user.preview.Views.Message;
import chatapp.user.preview.Views.Person;
import chatapp.user.preview.Views.Relation;
import chatapp.user.ui.UserFrame;
import chatapp.user.ui.shared.Dialogs;
import chatapp.user.ui.shared.RowTable;
import chatapp.user.ui.shared.Ui;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import static chatapp.user.preview.PreviewServer.SAMPLE_PASSWORD;
import static chatapp.user.preview.PreviewServer.SAMPLE_USERNAME;
import static org.junit.jupiter.api.Assertions.*;

class UserUiTest {
    private final List<String> asked = new ArrayList<>();

    @BeforeEach
    void answerDialogsWithYes() {
        Dialogs.answerForTests(ask -> asked.add(ask.title()));
    }

    @AfterEach
    void restoreDialogs() {
        Dialogs.answerForTests(null);
    }

    // ------------------------------------------------------------------ navigation and pages

    @Test
    void sidebarOpensAllFourPages() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent();
            assertNotNull(field(shell, "Search all messages"));
            assertNotNull(field(shell, "Filter conversations by name"));
            assertTrue(hasText(shell, "Select a conversation"));
            button(shell, "Group").doClick(0);
            assertColumns(shell, "Group name", "Members", "My role", "Encryption", "Created");
            button(shell, "Friend").doClick(0);
            assertColumns(shell, "Full name", "Status", "Friends since");
            assertNotNull(button(shell, "Online (2)"));
            assertNotNull(button(shell, "Requests (2)"));
            button(shell, "Account").doClick(0);
            assertTrue(hasText(shell, "Account details"));
            assertNotNull(button(shell, "Edit profile"));
            button(shell, "Chat").doClick(0);
            assertNotNull(field(shell, "Search all messages"));
        });
    }

    @Test
    void loginRejectsEmptyWrongAndLockedInputAndAcceptsValidInput() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            int[] signIns = {0};
            PreviewServer server = new PreviewServer();
            JPanel auth = UserFrame.createAuth(server, () -> signIns[0]++);
            button(auth, "Sign in").doClick(0);
            assertTrue(hasText(auth, "Enter your username."));
            assertTrue(hasText(auth, "Enter your password."));
            signIn(auth, SAMPLE_USERNAME, "wrong-password");
            assertTrue(hasText(auth, "Incorrect username or password."));
            signIn(auth, "ryanh", SAMPLE_PASSWORD);
            assertTrue(hasText(auth, "This account is locked by an administrator. Contact support to restore access."));
            assertEquals(ServiceException.Code.ACCOUNT_LOCKED,
                assertThrows(ServiceException.class, () -> server.signIn("ryanh", SAMPLE_PASSWORD)).code());
            assertEquals(0, signIns[0]);
            signIn(auth, SAMPLE_USERNAME, SAMPLE_PASSWORD);
            assertEquals(1, signIns[0]);
            assertEquals("Maya Chen", server.me().fullName());
        });
    }

    @Test
    void registerMarksEveryFailedFieldThenSignsTheNewAccountIn() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            int[] signIns = {0};
            PreviewServer server = new PreviewServer();
            JPanel auth = UserFrame.createAuth(server, () -> signIns[0]++);
            button(auth, "Create account").doClick(0);
            field(auth, "Choose a username").setText("noahp");
            field(auth, "name@example.com").setText("not-an-email");
            password(auth, "At least 8 characters").setText("short");
            button(auth, "Create account").doClick(0);
            assertTrue(hasText(auth, "This username is already taken."));
            assertTrue(hasText(auth, "Enter a valid email address."));
            assertTrue(hasText(auth, "Use at least 8 characters."));
            assertTrue(hasText(auth, "Choose a gender."));
            assertEquals(0, signIns[0]);

            field(auth, "Choose a username").setText("newuser");
            field(auth, "Your full name").setText("New User");
            field(auth, "name@example.com").setText("new.user@example.com");
            combo(auth).setSelectedItem("Other");
            password(auth, "At least 8 characters").setText("longpassword");
            password(auth, "Re-enter the password").setText("longpassword");
            button(auth, "Create account").doClick(0);
            assertEquals(1, signIns[0]);
            assertEquals("newuser", server.me().username());
        });
    }

    @Test
    void forgotPasswordShowsTheEmailWithANewWorkingPassword() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PreviewServer server = new PreviewServer();
            JPanel auth = UserFrame.createAuth(server, () -> {});
            button(auth, "Forgot password?").doClick(0);
            button(auth, "Send new password").doClick(0);
            assertTrue(hasText(auth, "Enter the email on your account."));
            field(auth, "name@example.com").setText("maya.chen@example.com");
            button(auth, "Send new password").doClick(0);
            assertTrue(hasText(auth, "A new password was sent to maya.chen@example.com."));
            assertTrue(hasText(auth, "Subject: Your new ChatApp password"));
        });
        PreviewServer server = new PreviewServer();
        var mail = server.resetPassword("maya.chen@example.com");
        String newPassword = mail.body().replaceAll(".*new password is (\\S+)\\. Sign in.*", "$1");
        assertThrows(ServiceException.class, () -> server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD));
        assertEquals("Maya Chen", server.signIn(SAMPLE_USERNAME, newPassword).fullName());
    }

    @Test
    void accountEditKeepsUsernameAndRejectsATakenEmail() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent();
            button(shell, "Account").doClick(0);
            button(shell, "Edit profile").doClick(0);
            assertFalse(field(shell, "Username").isEditable());
            field(shell, "name@example.com").setText("noah.patel@example.com");
            button(shell, "Save changes").doClick(0);
            assertTrue(hasText(shell, "This email is already in use."));
            field(shell, "name@example.com").setText("maya.new@example.com");
            field(shell, "Your full name").setText("Maya C. Chen");
            button(shell, "Save changes").doClick(0);
            assertTrue(hasText(shell, "Profile updated."));
            assertTrue(hasText(shell, "Maya C. Chen"));
        });
    }

    // ------------------------------------------------------------------ cross-page links (Navigator)

    @Test
    void friendChatAndOnlineCreateGroupLinkToTheirPages() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent();
            button(shell, "Friend").doClick(0);
            buttons(shell, "Chat").get(1).doClick(0); // first row's Chat (index 0 is the sidebar link)
            assertTrue(hasText(shell, "Ava Brooks"));
            assertNotNull(button(shell, "Search in chat"));

            button(shell, "Friend").doClick(0);
            button(shell, "Online (2)").doClick(0);
            for (JCheckBox box : all(shell, JCheckBox.class)) box.doClick(0);
            assertTrue(hasText(shell, "2 selected"));
            buttons(shell, "Create group").getFirst().doClick(0);
            assertTrue(hasText(shell, "New group"));
            assertTrue(hasText(shell, "Ava Brooks"));
            assertTrue(hasText(shell, "Noah Patel"));
        });
    }

    @Test
    void globalSearchOpensTheConversationOfTheHit() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent();
            field(shell, "Search all messages").setText("promo");
            assertTrue(hasText(shell, "1 result in 1 conversation"));
            accessible(shell, "Ethan Park: Buy now!").getActionMap().get("open").actionPerformed(null);
            assertNotNull(button(shell, "Clear history"));
            assertTrue(all(shell, JLabel.class).stream().anyMatch(l -> "Ethan Park".equals(l.getText())));
        });
    }

    @Test
    void textOtherPeopleControlIsNeverRenderedAsHtml() throws Exception {
        String markup = "<html><img src='http://example.invalid/x.png' width=900>";
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        server.send(server.chats("Noah").getFirst().id(), markup);
        server.createGroup(markup, List.of("noahp"));
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent(server, () -> {});
            List<JLabel> raw = all(shell, JLabel.class).stream().filter(l -> markup.equals(l.getText())).toList();
            assertFalse(raw.isEmpty(), "the conversation list shows the name and the preview");
            raw.forEach(l -> assertNull(l.getClientProperty("html"), "label text must stay literal"));
            button(shell, "Group").doClick(0);
            assertTrue(all(shell, JLabel.class).stream().anyMatch(l -> markup.equals(l.getText())));
            all(shell, JLabel.class).stream().filter(l -> markup.equals(l.getText()))
                .forEach(l -> assertNull(l.getClientProperty("html")));
            assertNull(Ui.button(markup, () -> {}).getClientProperty("html"));
            assertNull(Ui.checkbox(markup).getClientProperty("html"));
        });
    }

    @Test
    void losingAccessToTheOpenChatShowsWhyInsteadOfAnEmptyPane() throws Exception {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        long maya = server.me().id();
        Group study = server.groups("Java Study").getFirst();
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent(server, () -> {});
            accessible(shell, "Java Study Group").getActionMap().get("open").actionPerformed(null);
            button(shell, "Search in chat").doClick(0);
            server.signOut();                                  // meanwhile the group admin removes Maya
            server.signIn("sofiareyes", SAMPLE_PASSWORD);
            server.removeMember(study.id(), maya);
            server.signOut();
            server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
            field(shell, "Search in this chat").setText("quiz");
            assertTrue(hasText(shell, "You are no longer in this conversation."));
            assertTrue(hasText(shell, "Select a conversation"));
        });
    }

    // ------------------------------------------------------------------ chat actions

    @Test
    void selectingAReceivedMessageAllowsReportAndDeleteForMeOnly() throws Exception {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        Chat ethan = server.chats("Ethan").getFirst();
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent(server, () -> {});
            accessible(shell, "Ethan Park").getActionMap().get("open").actionPerformed(null);
            accessible(shell, "Ethan Park: Buy now!").getActionMap().get("toggle").actionPerformed(null);
            assertFalse(button(shell, "Report spam").isEnabled(), "Maya already reported this one (Admin report 1042)");
            accessible(shell, "Ethan Park: Buy now!").getActionMap().get("toggle").actionPerformed(null);
            accessible(shell, "Ethan Park: Hey, are you free").getActionMap().get("toggle").actionPerformed(null);
            assertTrue(hasText(shell, "1 message selected"));
            JButton report = button(shell, "Report spam");
            assertTrue(report.isEnabled());
            report.doClick(0);
            assertEquals(List.of("Report spam"), asked);
            assertTrue(hasText(shell, "Report sent. Administrators will review the message from Ethan Park."));

            accessible(shell, "You: Who is this?").getActionMap().get("toggle").actionPerformed(null);
            assertTrue(hasText(shell, "Waiting"), "Ethan is offline, so Maya's last message is still waiting");
            assertFalse(button(shell, "Report spam").isEnabled());
            button(shell, "Delete for me").doClick(0);
            assertTrue(hasText(shell, "1 message was deleted for you."));
        });
        assertEquals(5, server.spamReports().size(), "the 4 sample reports (as in Admin) and the new one");
        assertTrue(server.messages(ethan.id()).stream().noneMatch(m -> m.text().equals("Who is this?")));
        server.signOut();
        server.signIn("ethanp", SAMPLE_PASSWORD);
        assertTrue(server.messages(ethan.id()).stream().anyMatch(m -> m.text().equals("Who is this?")),
            "Delete for me must not delete the other person's copy");
    }

    @Test
    void sendIsDeliveredOrWaitsAndOfflineFriendsGetItOnSignIn() {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        Chat noah = server.chats("Noah").getFirst();
        Chat ethan = server.chats("Ethan").getFirst();
        Message delivered = server.send(noah.id(), "Hello Noah");
        assertEquals(Delivery.DELIVERED, delivered.delivery());
        assertEquals(delivered.sentAt(), delivered.deliveredAt(), "Noah is online, so it arrives at once");
        Message waiting = server.send(ethan.id(), "Hello Ethan");
        assertEquals(Delivery.WAITING, waiting.delivery());
        assertNull(waiting.deliveredAt());
        assertEquals(Delivery.SENT, server.send(server.chats("Project").getFirst().id(), "Hi team").delivery(),
            "group messages are not tracked per member");
        assertThrows(ServiceException.class, () -> server.send(noah.id(), "   "));
        server.signOut();
        server.signIn("ethanp", SAMPLE_PASSWORD);
        server.signOut();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        Message after = server.messages(ethan.id()).stream().filter(m -> m.id() == waiting.id()).findFirst().orElseThrow();
        assertEquals(Delivery.DELIVERED, after.delivery(), "Ethan signing in delivers the waiting message");
        assertTrue(after.deliveredAt().isAfter(after.sentAt()));
    }

    @Test
    void clearHistoryAndSearchWorkPerUser() {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        Chat noah = server.chats("Noah").getFirst();
        assertEquals(2, server.search(noah.id(), "report").size());
        assertFalse(server.searchAll("draft").isEmpty());
        server.clearHistory(noah.id());
        assertTrue(server.messages(noah.id()).isEmpty());
        assertTrue(server.search(noah.id(), "report").isEmpty());
        server.send(noah.id(), "Fresh start");
        assertEquals(1, server.messages(noah.id()).size());
        server.signOut();
        server.signIn("noahp", SAMPLE_PASSWORD);
        assertEquals(4, server.messages(noah.id()).size(), "Noah keeps his history");
    }

    @Test
    void improveIsRefusedInAnEncryptedGroupOnTheChatPage() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent();
            accessible(shell, "Java Study Group").getActionMap().get("open").actionPerformed(null);
            field(shell, "Type a message...").setText("can u send the notes pls");
            button(shell, "Improve").doClick(0);
            assertTrue(hasText(shell, "The AI assistant can't read encrypted conversations."));
            assertFalse(hasText(shell, "Improve message"));
        });
    }

    @Test
    void aiAssistantSuggestsImprovesAndSkipsEncryptedGroups() {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        Chat noah = server.chats("Noah").getFirst();
        assertEquals(3, server.suggest(noah.id()).size());
        assertEquals("Could you send me the report tomorrow, please? Thanks!",
            server.improve(noah.id(), "can u send me the report tmr pls", "Friendly"));
        assertEquals("Could you please send me the report tomorrow? Thank you.",
            server.improve(noah.id(), "can u send me the report tmr pls", "Formal"));
        Chat study = server.chats("Java Study").getFirst();
        String encrypted = "The AI assistant can't read encrypted conversations.";
        assertEquals(encrypted, assertThrows(ServiceException.class, () -> server.suggest(study.id())).getMessage());
        assertEquals(encrypted, assertThrows(ServiceException.class,
            () -> server.improve(study.id(), "can u send the notes", "Friendly")).getMessage());
        assertThrows(ServiceException.class, () -> server.improve(noah.id(), " ", "Friendly"));
    }

    @Test
    void spamReportsOnlyForOtherPeoplesMessagesAndOnlyOnce() {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        List<Message> messages = server.messages(server.chats("Ethan").getFirst().id());
        Message reported = messages.stream().filter(m -> m.text().startsWith("Buy now")).findFirst().orElseThrow();
        Message other = messages.stream().filter(m -> m.text().startsWith("Hey")).findFirst().orElseThrow();
        Message mine = messages.stream().filter(Message::mine).findFirst().orElseThrow();
        assertTrue(reported.reportedByMe(), "the sample report 1042 from the Admin data");
        assertThrows(ServiceException.class, () -> server.reportSpam(reported.id(), "Other"));
        assertThrows(ServiceException.class, () -> server.reportSpam(mine.id(), "Other"));
        assertThrows(ServiceException.class, () -> server.reportSpam(other.id(), " "));
        server.reportSpam(other.id(), "Unwanted promotions or ads");
        assertThrows(ServiceException.class, () -> server.reportSpam(other.id(), "Other"));
    }

    // ------------------------------------------------------------------ friends and blocks

    @Test
    void peopleWhoBlockedMeAreHiddenAndBlockedPeopleCannotRequest() {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        assertTrue(server.findPeople("zoe").isEmpty(), "Zoe blocked Maya, so Maya can't find her");
        assertEquals(Relation.BLOCKED, server.findPeople("dkang").getFirst().relation());
        Person ava = server.friends("Ava").getFirst();
        server.block(ava.id());
        assertTrue(server.friends("Ava").isEmpty());
        Chat avaChat = server.chats("Ava Brooks").getFirst();
        assertEquals(Access.YOU_BLOCKED, avaChat.access());
        assertFalse(avaChat.online(), "a blocked chat doesn't show online status");
        assertThrows(ServiceException.class, () -> server.send(avaChat.id(), "hi"));
        long mayaId = server.me().id();
        server.signOut();
        server.signIn("avab", SAMPLE_PASSWORD);
        assertTrue(server.findPeople("maya").isEmpty());
        assertThrows(ServiceException.class, () -> server.sendRequest(mayaId));
        assertThrows(ServiceException.class, () -> server.chatWith(mayaId));
    }

    @Test
    void requestsCanBeAcceptedDeclinedAndFindPeopleSendsThem() throws Exception {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent(server, () -> {});
            button(shell, "Friend").doClick(0);
            button(shell, "Requests (2)").doClick(0);
            buttons(shell, "Accept").getFirst().doClick(0);
            assertTrue(hasText(shell, "You and Sofia Reyes are now friends."));
            button(shell, "Decline").doClick(0);
            assertNotNull(button(shell, "Requests (0)"));
            button(shell, "Find people").doClick(0);
            field(shell, "Search by name or username").setText("ana");
            button(shell, "Add friend").doClick(0);
            assertTrue(hasText(shell, "Friend request sent to Ana Lopez."));
        });
        assertEquals(5, server.friends("").size());
        assertEquals(2, server.sent().size());
    }

    // ------------------------------------------------------------------ groups

    @Test
    void groupRulesNeedAnotherMemberAndAdminRights() {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        ServiceException none = assertThrows(ServiceException.class, () -> server.createGroup("Solo", List.of()));
        assertEquals("Add at least one other member.", none.fieldErrors().get("members"));
        assertThrows(ServiceException.class, () -> server.createGroup("Ghosts", List.of("nobody")));
        Group created = server.createGroup("Final presentation", List.of("noahp", "@avab", "mayachen"));
        assertEquals(3, created.members().size());
        assertTrue(created.meAdmin());

        Group study = server.groups("Java Study").getFirst();
        assertFalse(study.meAdmin());
        long sofia = study.members().stream().filter(m -> m.username().equals("sofiareyes")).findFirst().orElseThrow().userId();
        long noah = study.members().stream().filter(m -> m.username().equals("noahp")).findFirst().orElseThrow().userId();
        assertThrows(ServiceException.class, () -> server.removeMember(study.id(), sofia));
        assertThrows(ServiceException.class, () -> server.makeAdmin(study.id(), noah));
        assertThrows(ServiceException.class, () -> server.setEncrypted(study.id(), false));
        assertEquals("Study Group", server.rename(study.id(), "Study Group").name(), "any member can rename");
        assertEquals(5, server.addMember(study.id(), "avab").members().size(), "any member can add");

        Group project = server.groups("Project").getFirst();
        long ethan = project.members().stream().filter(m -> m.username().equals("ethanp")).findFirst().orElseThrow().userId();
        assertTrue(server.makeAdmin(project.id(), ethan).members().stream().anyMatch(m -> m.userId() == ethan && m.admin()));
        assertEquals(7, server.removeMember(project.id(), ethan).members().size());

        Group pair = server.createGroup("Just us", List.of("noahp"));
        long noahId = pair.members().stream().filter(m -> !m.username().equals(SAMPLE_USERNAME)).findFirst().orElseThrow().userId();
        ServiceException tooSmall = assertThrows(ServiceException.class, () -> server.removeMember(pair.id(), noahId));
        assertTrue(tooSmall.getMessage().startsWith("A group needs at least 2 people."));
        assertEquals(2, server.group(pair.id()).members().size());
        assertTrue(server.setEncrypted(project.id(), true).encrypted());
        assertTrue(server.chat(project.id()).encrypted(), "the Chat page shows the lock badge");
    }

    @Test
    void groupDetailDisablesAdminActionsForMembersAndEnablesThemForAdmins() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent();
            button(shell, "Group").doClick(0);
            RowTable list = all(shell, RowTable.class).getFirst();
            list.open(2); // Java Study Group: Maya is a member
            assertTrue(hasText(shell, "Only group admins can make admins, remove members or change encryption."));
            assertTrue(buttons(shell, "Remove").stream().noneMatch(JButton::isEnabled));
            assertTrue(all(shell, JToggleButton.class).stream().filter(t -> !(t instanceof JCheckBox)).noneMatch(JToggleButton::isEnabled));

            button(shell, "Groups").doClick(0);
            all(shell, RowTable.class).getFirst().open(0); // Project Team: Maya is an admin
            assertTrue(buttons(shell, "Remove").stream().allMatch(JButton::isEnabled));
            buttons(shell, "Remove").getFirst().doClick(0);
            assertTrue(asked.contains("Remove member"));
            assertTrue(hasText(shell, "Noah Patel was removed from the group."));
            button(shell, "Open chat").doClick(0);
            assertNotNull(button(shell, "Search in chat"));
        });
    }

    @Test
    void createGroupFormShowsTheMemberRuleAndCreatesTheGroup() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent();
            button(shell, "Group").doClick(0);
            button(shell, "Create group").doClick(0);
            field(shell, "e.g. Final presentation").setText("Final presentation");
            button(shell, "Create group").doClick(0);
            assertTrue(hasText(shell, "Add at least one other member."));
            field(shell, "Add by username or name").setText("davelin");
            button(shell, "Add").doClick(0);
            button(shell, "Create group").doClick(0);
            assertTrue(hasText(shell, "Group “Final presentation” created with 1 other member."));
            assertColumns(shell, "Member", "Group role", "Status", "Joined");
        });
    }

    @Test
    void blockedChatShowsWhyAndHidesTheMessageBox() throws Exception {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        server.block(server.friends("Noah").getFirst().id());
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent(server, () -> {});
            accessible(shell, "Noah Patel").getActionMap().get("open").actionPerformed(null);
            assertTrue(hasText(shell, "You blocked Noah Patel. You can't send messages in this chat."));
            assertFalse(hasText(shell, "messages arrive instantly"));
            assertTrue(buttons(shell, "Send").isEmpty());
        });
    }

    @Test
    void lockedAccountsAreFlaggedAndCanOnlyBeUnfriendedDeclinedOrRemoved() throws Exception {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        Person linh = server.friends("Linh").getFirst();
        assertTrue(linh.locked());
        Person ryan = server.findPeople("ryanh").getFirst();
        assertTrue(ryan.locked());
        assertThrows(ServiceException.class, () -> server.sendRequest(ryan.id()));
        assertThrows(ServiceException.class, () -> server.chatWith(linh.id()));
        assertThrows(ServiceException.class, () -> server.block(linh.id()));
        Chat linhChat = server.chats("Linh").getFirst();
        assertEquals(Access.LOCKED, linhChat.access());
        assertThrows(ServiceException.class, () -> server.send(linhChat.id(), "hi"));
        assertThrows(ServiceException.class, () -> server.suggest(linhChat.id()));
        var owen = server.requests("Owen").getFirst();
        assertTrue(owen.sender().locked());
        assertThrows(ServiceException.class, () -> server.accept(owen.id()));
        assertEquals("@linhng is locked and can't be added.", assertThrows(ServiceException.class,
            () -> server.createGroup("Slides", List.of("linhng"))).fieldErrors().get("members"));
        Group campus = server.groups("Campus").getFirst();
        assertThrows(ServiceException.class, () -> server.addMember(campus.id(), "owentran"));
        Group project = server.groups("Project").getFirst();
        assertThrows(ServiceException.class, () -> server.makeAdmin(project.id(), linh.id()));
        server.decline(owen.id());

        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = UserFrame.createContent(server, () -> {});
            accessible(shell, "Linh Nguyen").getActionMap().get("open").actionPerformed(null);
            assertTrue(hasText(shell, "Linh Nguyen's account is locked by an administrator."));
            assertTrue(buttons(shell, "Send").isEmpty());
            button(shell, "Friend").doClick(0);
            assertTrue(hasText(shell, "Locked"));
            assertEquals(4, buttons(shell, "Chat").size(), "the sidebar link and 3 rows: none on Linh's row");
            assertEquals(4, buttons(shell, "Unfriend").size());
            assertEquals(3, buttons(shell, "Block").size(), "no Block on Linh's row");
            button(shell, "Group").doClick(0);
            all(shell, RowTable.class).getFirst().open(0); // Project Team: Maya is a group admin
            assertEquals(4, buttons(shell, "Make admin").size(), "not for Linh or Owen, who are locked");
            assertEquals(7, buttons(shell, "Remove").size(), "group admins can still remove locked members");
        });
        assertEquals(7, server.removeMember(project.id(), linh.id()).members().size());
    }

    @Test
    void removedMembersLeaveTheirMessagesAndCanBeAddedBack() {
        PreviewServer server = new PreviewServer();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        Group project = server.groups("Project").getFirst();
        assertTrue(project.members().stream().noneMatch(m -> m.username().equals("oliviar")), "Olivia was removed (leftAt)");
        long ava = project.members().stream().filter(m -> m.username().equals("avab")).findFirst().orElseThrow().userId();
        server.removeMember(project.id(), ava);
        assertTrue(server.messages(project.id()).stream().anyMatch(m -> m.senderId() == ava),
            "a removed member's messages stay in the history");
        server.signOut();
        server.signIn("avab", SAMPLE_PASSWORD);
        assertTrue(server.chats("Project").isEmpty());
        server.signOut();
        server.signIn(SAMPLE_USERNAME, SAMPLE_PASSWORD);
        assertEquals(8, server.addMember(project.id(), "avab").members().size());
        assertEquals(9, server.addMember(project.id(), "oliviar").members().size());
    }

    // ------------------------------------------------------------------ helpers

    private static void signIn(JPanel auth, String username, String password) {
        field(auth, "Enter your username").setText(username);
        password(auth, "Enter your password").setText(password);
        button(auth, "Sign in").doClick(0);
    }

    private static JTextField field(Container root, String hint) {
        return all(root, JTextField.class).stream().filter(f -> !(f instanceof JPasswordField))
            .filter(f -> hint.equals(f.getAccessibleContext().getAccessibleName())).findFirst()
            .orElseThrow(() -> new AssertionError("Missing field: " + hint));
    }

    private static JPasswordField password(Container root, String hint) {
        return all(root, JPasswordField.class).stream()
            .filter(f -> hint.equals(f.getAccessibleContext().getAccessibleName())).findFirst()
            .orElseThrow(() -> new AssertionError("Missing password field: " + hint));
    }

    @SuppressWarnings("unchecked")
    private static JComboBox<String> combo(Container root) {
        return all(root, JComboBox.class).getFirst();
    }

    private static JComponent accessible(Container root, String prefix) {
        return all(root, JComponent.class).stream().filter(c -> c.getAccessibleContext() != null)
            .filter(c -> {
                String name = c.getAccessibleContext().getAccessibleName();
                return name != null && name.startsWith(prefix) && c.getActionMap().size() > 0
                    && (c.getActionMap().get("open") != null || c.getActionMap().get("toggle") != null);
            }).findFirst().orElseThrow(() -> new AssertionError("Missing item: " + prefix));
    }

    private static JButton button(Container root, String label) {
        return buttons(root, label).stream().findFirst().orElseThrow(() -> new AssertionError("Missing button: " + label));
    }

    private static List<JButton> buttons(Container root, String label) {
        return all(root, JButton.class).stream().filter(b -> label.equals(b.getText())).toList();
    }

    private static boolean hasText(Container root, String text) {
        for (Component c : components(root)) {
            if (c instanceof JLabel l && l.getText() != null && l.getText().contains(text)) return true;
            if (c instanceof JTextArea a && a.getText().contains(text)) return true;
        }
        return false;
    }

    private static void assertColumns(Container root, String... columns) {
        assertTrue(all(root, RowTable.class).stream().anyMatch(t -> t.columnNames().containsAll(List.of(columns))),
            "Missing columns: " + List.of(columns));
    }

    private static <T> List<T> all(Container root, Class<T> type) {
        return components(root).stream().filter(type::isInstance).map(type::cast).toList();
    }

    private static List<Component> components(Container root) {
        List<Component> result = new ArrayList<>();
        for (Component child : root.getComponents()) {
            if (!child.isVisible()) continue;
            result.add(child);
            if (child instanceof Container container) result.addAll(components(container));
        }
        return result;
    }
}
