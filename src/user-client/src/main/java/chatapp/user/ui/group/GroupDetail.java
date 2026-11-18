package chatapp.user.ui.group;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.ServiceException;
import chatapp.user.preview.Views.Group;
import chatapp.user.preview.Views.Member;
import chatapp.user.preview.Views.Person;
import chatapp.user.preview.Views.Relation;
import chatapp.user.ui.shared.Dialogs;
import chatapp.user.ui.shared.Form;
import chatapp.user.ui.shared.RowTable;
import chatapp.user.ui.shared.RowTable.Column;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;

/**
 * Group detail (Figma G4–G9): Rename (F12b) and Add member (F12c) for every member; Make admin
 * (F12d), Remove (F12e) and the Encryption toggle (F12g) for group admins only. For other members
 * those controls are disabled and say why. "Open chat" links to the Chat page.
 */
final class GroupDetail {
    private final GroupPage host;
    private final long groupId;

    GroupDetail(GroupPage host, long groupId) {
        this.host = host;
        this.groupId = groupId;
    }

    void show() {
        Group g;
        try {
            g = host.server.group(groupId);
        } catch (ServiceException e) {
            host.banner(Ui.Banner.ERROR, e.getMessage());
            host.showList();
            return;
        }
        long myId = host.server.me().id();
        JPanel page = host.newPage(g.name(),
            Ui.button("Open chat", Ui.Style.PRIMARY, "chat", () -> host.nav.openChat(g.id())),
            Ui.button("Rename", Ui.Style.SECONDARY, "edit", () -> rename(g)));
        host.backTo(page);
        Ui.add(page, Ui.muted("Created " + Ui.date(g.createdAt()) + "   ·   Created by " + g.creatorName() + "   ·   "
            + g.members().size() + " members   ·   " + (g.meAdmin() ? "You are a group admin" : "You are a member")));
        if (!g.meAdmin()) Ui.add(page, Ui.banner(Ui.Banner.INFO, "Only group admins can make admins, remove members or change encryption."));
        Ui.add(page, encryption(g));

        JButton add = Ui.button("Add member", g.meAdmin() ? Ui.Style.PRIMARY : Ui.Style.SECONDARY, "plus", () -> addMember(g));
        Ui.add(page, Ui.header(Ui.label("Members", 18, true), add));
        RowTable table = new RowTable(56, Column.grow("Member", 3), Column.grow("Group role", 1.4),
            Column.grow("Status", 1.1), Column.grow("Joined", 1.3), Column.fixed("", 240));
        for (Member m : g.members()) {
            table.addRow(null, Ui.person(m.fullName(), m.username(), m.online()),
                Ui.pill(m.admin() ? "Group admin" : "Member", m.admin() ? Ui.Tone.MEMBERSHIP : Ui.Tone.NEUTRAL),
                Ui.status(m.online(), m.locked()), Ui.label(Ui.date(m.joinedAt()), 14, false), memberActions(g, m, myId));
        }
        Ui.add(page, table);
        host.show(page);
    }

    private JComponent memberActions(Group g, Member m, long myId) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        row.setOpaque(false);
        if (m.userId() == myId) {
            row.add(Ui.muted("You"));
            return row;
        }
        JButton remove = Ui.button("Remove", Ui.Style.DANGER_OUTLINE, "close", () -> remove(g, m));
        if (m.locked()) {
            // A locked account can only be removed, and only by a group admin.
            if (!g.meAdmin()) {
                row.add(Ui.muted("Account locked"));
            } else {
                if (g.members().size() <= PreviewServer.MIN_GROUP_SIZE) {
                    Ui.disabled(remove, "A group needs at least " + PreviewServer.MIN_GROUP_SIZE + " people.");
                }
                row.add(remove);
            }
            return row;
        }
        JButton admin = m.admin()
            ? Ui.disabled(Ui.button("Admin", Ui.Style.SECONDARY, "crown", () -> {}), m.fullName() + " is already a group admin.")
            : Ui.button("Make admin", Ui.Style.SECONDARY, "crown", () -> act(() -> host.server.makeAdmin(g.id(), m.userId()),
                m.fullName() + " is now a group admin."));
        if (!g.meAdmin()) {
            if (!m.admin()) Ui.disabled(admin, "Only group admins can make admins.");
            Ui.disabled(remove, "Only group admins can remove members.");
        } else if (g.members().size() <= PreviewServer.MIN_GROUP_SIZE) {
            Ui.disabled(remove, "A group needs at least " + PreviewServer.MIN_GROUP_SIZE + " people.");
        }
        row.add(admin);
        row.add(remove);
        return row;
    }

    private JComponent encryption(Group g) {
        JPanel card = Ui.card(new BorderLayout(12, 0));
        JLabel lock = new JLabel(Ui.icon("lock", 20, Ui.INK));
        card.add(lock, BorderLayout.WEST);
        JPanel text = Ui.column();
        text.add(Ui.label("Encrypted group", 14, true));
        text.add(Ui.muted(g.encrypted() ? "On · only members can read messages." : "Off · messages are stored readable on the server."));
        card.add(text);
        JToggleButton toggle = Ui.toggle(g.encrypted(), "Encrypted group");
        if (g.meAdmin()) {
            toggle.addActionListener(e -> {
                boolean on = toggle.isSelected();
                if (confirmEncryption(g, on)) {
                    act(() -> host.server.setEncrypted(g.id(), on), on ? "Encryption is on for new messages." : "Encryption is off.");
                } else {
                    toggle.setSelected(!on);
                }
            });
        } else {
            toggle.setEnabled(false);
            toggle.setToolTipText("Only group admins can change encryption.");
        }
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.add(toggle);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private boolean confirmEncryption(Group g, boolean on) {
        JComponent extra = on ? Ui.banner(Ui.Banner.LOCK, "Members will see an “Encrypted” badge on this group.") : null;
        return Dialogs.confirm(host, new Dialogs.Ask(on ? "Turn on encryption" : "Turn off encryption", g.name(),
            on ? "New messages will be encrypted so only members can read them. Older messages stay as they are, "
                + "and the AI assistant can't read encrypted messages."
                : "New messages will be stored readable on the server again. Messages sent while encryption was on stay encrypted.",
            extra, on ? "Turn on" : "Turn off", Ui.Style.PRIMARY, "lock"));
    }

    private void rename(Group g) {
        Form form = new Form();
        Form.Field name = form.text("name", "Group name", "Group name", null);
        name.set(g.name());
        JPanel extra = Ui.column();
        extra.add(name);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (!Dialogs.confirm(host, new Dialogs.Ask("Rename group", null, "Members see the new name right away.",
                extra, "Save", Ui.Style.PRIMARY, "check"))) return;
        String value = form.value("name");
        act(() -> host.server.rename(g.id(), value), "Group renamed to “" + value.strip() + "”.");
    }

    private void remove(Group g, Member m) {
        String first = m.fullName().split(" ")[0];
        if (Dialogs.confirm(host, "Remove member", m.fullName() + " (@" + m.username() + ")",
                first + " will leave " + g.name() + " and won't see new messages. Only group admins can remove members.",
                "Remove", Ui.Style.DANGER, "close")) {
            act(() -> host.server.removeMember(g.id(), m.userId()), m.fullName() + " was removed from the group.");
        }
    }

    /** F12c: search people and add them one by one; the dialog's "Done" closes it. */
    private void addMember(Group g) {
        JPanel extra = Ui.column();
        JTextField search = Ui.field("Search by username or name", 26);
        JPanel box = Ui.search(search);
        box.setMaximumSize(null);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        extra.add(box);
        extra.add(Box.createVerticalStrut(10));
        JPanel results = Ui.column();
        results.setAlignmentX(Component.LEFT_ALIGNMENT);
        extra.add(results);
        JLabel status = Ui.muted(" ");
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        extra.add(status);
        boolean[] changed = {false};
        Runnable[] update = new Runnable[1];
        update[0] = () -> {
            results.removeAll();
            Group current = host.server.group(g.id());
            String q = search.getText();
            if (!q.isBlank()) {
                var found = host.server.findPeople(q).stream().filter(p -> p.relation() != Relation.BLOCKED).limit(5).toList();
                if (found.isEmpty()) results.add(Ui.muted("No people match “" + q.trim() + "”."));
                for (Person p : found) {
                    boolean member = current.members().stream().anyMatch(m -> m.userId() == p.id());
                    JPanel row = new JPanel(new BorderLayout(10, 0));
                    row.setOpaque(false);
                    row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
                    row.add(Ui.person(p.fullName(), p.username(), p.online(), p.locked()));
                    row.add(member ? Ui.muted("Already a member") : p.locked() ? Ui.muted("Account locked")
                        : Ui.button("Add", Ui.Style.PRIMARY, "plus", () -> {
                        try {
                            host.server.addMember(g.id(), p.username());
                            changed[0] = true;
                            status.setForeground(Ui.BLUE);
                            status.setText(p.fullName() + " was added.");
                        } catch (ServiceException e) {
                            status.setForeground(Ui.RED);
                            status.setText(e.getMessage());
                        }
                        update[0].run();
                    }), BorderLayout.EAST);
                    row.setAlignmentX(Component.LEFT_ALIGNMENT);
                    results.add(row);
                }
            }
            results.revalidate();
            results.repaint();
            Window window = SwingUtilities.getWindowAncestor(results);
            if (window instanceof JDialog dialog) dialog.pack();
        };
        Ui.onText(search, update[0]);
        Dialogs.confirm(host, new Dialogs.Ask("Add member", g.name(), null, extra, "Done", Ui.Style.PRIMARY, null));
        if (changed[0]) host.banner(Ui.Banner.SUCCESS, "Members updated.");
        show();
    }

    /** Runs a server change, then shows the detail again with a success or error banner. */
    private void act(Runnable action, String success) {
        try {
            action.run();
            host.banner(Ui.Banner.SUCCESS, success);
        } catch (ServiceException e) {
            host.banner(Ui.Banner.ERROR, e.getMessage());
        }
        show();
    }
}
