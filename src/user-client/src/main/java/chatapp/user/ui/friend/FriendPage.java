package chatapp.user.ui.friend;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.ServiceException;
import chatapp.user.preview.Views.Person;
import chatapp.user.preview.Views.Relation;
import chatapp.user.preview.Views.Request;
import chatapp.user.ui.Navigator;
import chatapp.user.ui.shared.Dialogs;
import chatapp.user.ui.shared.RowTable;
import chatapp.user.ui.shared.RowTable.Column;
import chatapp.user.ui.shared.Screen;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Friend page (Figma F1–F9) with four tabs: Friends (F06: filter, status, Chat, Unfriend, Block),
 * Online (F08: Chat, select people → Create group), Requests (F07: accept or decline) and Find people
 * (F09 + F06b: people who blocked me never appear; the action follows the relationship).
 * Chat and Create group are links to the Chat and Group pages.
 */
public final class FriendPage extends Screen {
    public static final String FRIENDS = "Friends", ONLINE = "Online", REQUESTS = "Requests", FIND = "Find people";

    private final PreviewServer server;
    private final Navigator nav;
    private String tab;
    private String filter = "", search = "";
    private final Set<String> selected = new LinkedHashSet<>();

    public FriendPage(PreviewServer server, Navigator nav, String tab) {
        this.server = server;
        this.nav = nav;
        this.tab = List.of(FRIENDS, ONLINE, REQUESTS, FIND).contains(tab) ? tab : FRIENDS;
        render();
    }

    private void render() {
        JPanel page = page("Friend");
        int friends = server.friends("").size(), online = server.online("").size(), requests = server.requests("").size();
        String[] labels = {FRIENDS + " (" + friends + ")", ONLINE + " (" + online + ")", REQUESTS + " (" + requests + ")", FIND};
        String current = switch (tab) { case ONLINE -> labels[1]; case REQUESTS -> labels[2]; case FIND -> labels[3]; default -> labels[0]; };
        Ui.add(page, Ui.tabs(current, labels, label -> {
            String next = label.replaceFirst(" \\(\\d+\\)$", "");
            if (!next.equals(tab)) { filter = ""; selected.clear(); }
            tab = next;
            render();
        }));
        switch (tab) {
            case ONLINE -> online(page);
            case REQUESTS -> requests(page);
            case FIND -> find(page);
            default -> friends(page);
        }
        display(page);
    }

    // ------------------------------------------------------------------ Friends (F06)

    private void friends(JPanel page) {
        JTextField field = filterField(page, "Filter friends by name", 24);
        JPanel results = Ui.column();
        Ui.add(page, results);
        Runnable update = () -> {
            filter = field.getText();
            results.removeAll();
            List<Person> people = server.friends(filter);
            if (people.isEmpty()) {
                Ui.add(results, filter.isBlank()
                    ? Ui.empty("friend", "No friends yet", "Find people by name or username and send a request.", findButton(""))
                    : Ui.empty("search", "No friends match “" + filter.trim() + "”",
                        "Try another name, or look for new people in Find people.", findButton(filter.trim())));
            } else {
                RowTable table = new RowTable(56, Column.grow("Full name", 3), Column.grow("Status", 1.2),
                    Column.grow("Friends since", 1.4), Column.fixed("", 290));
                for (Person p : people) {
                    // A locked friend can only be unfriended: no chat and no block (F13 lock).
                    JButton unfriend = Ui.button("Unfriend", () -> unfriend(p));
                    table.addRow(null, Ui.person(p.fullName(), p.username(), p.online()), Ui.status(p.online(), p.locked()),
                        Ui.label(Ui.date(p.since()), 14, false),
                        p.locked() ? actions(unfriend) : actions(chatButton(p), unfriend,
                            Ui.button("Block", Ui.Style.DANGER_OUTLINE, "block", () -> block(p))));
                }
                Ui.add(results, table);
                long on = people.stream().filter(Person::online).count();
                Ui.add(results, Ui.muted(people.size() + (people.size() == 1 ? " friend" : " friends") + "  ·  " + on + " online"));
            }
            results.revalidate();
            results.repaint();
        };
        Ui.onText(field, update);
        update.run();
    }

    private JButton findButton(String query) {
        return Ui.button("Find people", Ui.Style.PRIMARY, "friend", () -> {
            tab = FIND;
            search = query;
            render();
        });
    }

    private void unfriend(Person p) {
        if (Dialogs.confirm(this, "Unfriend", p.fullName() + " (@" + p.username() + ")",
                "You will no longer be friends. Your chat history stays, and either of you can send a new request later.",
                "Unfriend", Ui.Style.DANGER, null)) {
            act(() -> server.unfriend(p.id()), "You are no longer friends with " + p.fullName() + ".");
        }
    }

    private void block(Person p) {
        String first = p.fullName().split(" ")[0];
        if (Dialogs.confirm(this, "Block account", p.fullName() + " (@" + p.username() + ")",
                first + " will be removed from your friends, can't send you friend requests again, and won't find you in search.",
                "Block", Ui.Style.DANGER, "block")) {
            act(() -> server.block(p.id()), p.fullName() + " is blocked.");
        }
    }

    // ------------------------------------------------------------------ Online (F08)

    private void online(JPanel page) {
        JTextField field = Ui.field("Filter by name", 22);
        field.setText(filter);
        JLabel count = Ui.muted("");
        JButton create = Ui.button("Create group", Ui.Style.PRIMARY, "group", () -> nav.openCreateGroup(List.copyOf(selected)));
        Ui.add(page, Ui.header(Ui.search(field), count, create));
        JPanel results = Ui.column();
        Ui.add(page, results);
        Runnable counter = () -> {
            count.setText(selected.isEmpty() ? "Select people to start a group" : selected.size() + " selected");
            create.setEnabled(!selected.isEmpty());
            create.setForeground(selected.isEmpty() ? Ui.MUTED : Color.WHITE);
        };
        Runnable update = () -> {
            filter = field.getText();
            results.removeAll();
            List<Person> people = server.online(filter);
            if (people.isEmpty()) {
                Ui.add(results, Ui.empty("friend", filter.isBlank() ? "No friends are online" : "No online friends match “" + filter.trim() + "”",
                    "Friends appear here while they are signed in.", null));
            } else {
                RowTable table = new RowTable(56, Column.fixed("", 28), Column.grow("Full name", 3),
                    Column.grow("Status", 1.5), Column.fixed("", 90));
                for (Person p : people) {
                    JCheckBox pick = Ui.checkbox("");
                    pick.getAccessibleContext().setAccessibleName("Select " + p.fullName());
                    pick.setSelected(selected.contains(p.username()));
                    JPanel row = table.addRow(null, pick, Ui.person(p.fullName(), p.username(), p.online()),
                        Ui.status(p.online()), actions(chatButton(p)));
                    table.select(row, pick.isSelected());
                    pick.addActionListener(e -> {
                        if (pick.isSelected()) selected.add(p.username()); else selected.remove(p.username());
                        table.select(row, pick.isSelected());
                        counter.run();
                    });
                }
                Ui.add(results, table);
                Ui.add(results, Ui.muted("Create group opens Group → Create group with the selected people."));
            }
            counter.run();
            results.revalidate();
            results.repaint();
        };
        Ui.onText(field, update);
        update.run();
    }

    // ------------------------------------------------------------------ Requests (F07)

    private void requests(JPanel page) {
        JTextField field = filterField(page, "Filter requests by name", 24);
        JPanel results = Ui.column();
        Ui.add(page, results);
        Runnable update = () -> {
            filter = field.getText();
            results.removeAll();
            Ui.add(results, Ui.label("Received", 17, true));
            List<Request> received = server.requests(filter);
            if (received.isEmpty()) {
                Ui.add(results, Ui.muted(filter.isBlank() ? "No friend requests right now." : "No requests match “" + filter.trim() + "”."));
            } else {
                RowTable table = new RowTable(56, Column.grow("From", 3), Column.grow("Sent", 2), Column.fixed("", 200));
                for (Request r : received) {
                    Person p = r.sender();
                    JButton decline = Ui.button("Decline",
                        () -> act(() -> server.decline(r.id()), "Request from " + p.fullName() + " declined."));
                    table.addRow(null, Ui.person(p.fullName(), p.username(), p.online(), p.locked()),
                        Ui.label(Ui.DATE_TIME.format(r.sentAt()), 14, false),
                        p.locked() ? actions(decline) : actions(Ui.button("Accept", Ui.Style.PRIMARY, "check",
                                () -> act(() -> server.accept(r.id()), "You and " + p.fullName() + " are now friends.")),
                            decline));
                }
                Ui.add(results, table);
            }
            Ui.add(results, Ui.label("Sent", 17, true));
            List<Request> sent = server.sent();
            if (sent.isEmpty()) {
                Ui.add(results, Ui.muted("You have no pending requests."));
            } else {
                RowTable table = new RowTable(56, Column.grow("To", 3), Column.grow("Sent", 2), Column.fixed("", 200));
                for (Request r : sent) {
                    Person p = r.sender();
                    table.addRow(null, Ui.person(p.fullName(), p.username(), p.online(), p.locked()),
                        Ui.label(Ui.DATE_TIME.format(r.sentAt()), 14, false), actions(Ui.pill("Pending", Ui.Tone.WARNING)));
                }
                Ui.add(results, table);
            }
            results.revalidate();
            results.repaint();
        };
        Ui.onText(field, update);
        update.run();
    }

    // ------------------------------------------------------------------ Find people (F09, F06b)

    private void find(JPanel page) {
        JTextField field = Ui.field("Search by name or username", 34);
        field.setText(search);
        Ui.add(page, Ui.search(field));
        Ui.add(page, Ui.banner(Ui.Banner.INFO, "People who blocked you don't appear in search."));
        JPanel results = Ui.column();
        Ui.add(page, results);
        Runnable update = () -> {
            search = field.getText();
            results.removeAll();
            List<Person> people = server.findPeople(search);
            if (search.isBlank()) {
                Ui.add(results, Ui.empty("search", "Find people", "Search by username or full name.", null));
            } else if (people.isEmpty()) {
                Ui.add(results, Ui.empty("search", "No people match “" + search.trim() + "”", "Check the username or try a full name.", null));
            } else {
                RowTable table = new RowTable(56, Column.grow("Person", 3), Column.grow("Relationship", 2), Column.fixed("", 430));
                for (Person p : people) table.addRow(null, Ui.person(p.fullName(), p.username(), p.online(), p.locked()),
                    relationPill(p.relation()), findActions(p));
                Ui.add(results, table);
            }
            results.revalidate();
            results.repaint();
        };
        Ui.onText(field, update);
        update.run();
        SwingUtilities.invokeLater(field::requestFocusInWindow);
    }

    private static JLabel relationPill(Relation relation) {
        return Ui.pill(relation.label(), switch (relation) {
            case FRIEND -> Ui.Tone.INFO;
            case REQUEST_SENT, REQUEST_RECEIVED -> Ui.Tone.WARNING;
            case BLOCKED -> Ui.Tone.DANGER;
            default -> Ui.Tone.NEUTRAL;
        });
    }

    private JPanel findActions(Person p) {
        List<JComponent> buttons = new ArrayList<>();
        if (p.relation() == Relation.BLOCKED) return actions(Ui.muted("Blocked"));
        if (p.locked()) return actions(Ui.muted("Account locked"));
        switch (p.relation()) {
            case NONE -> buttons.add(Ui.button("Add friend", Ui.Style.PRIMARY, "friend",
                () -> act(() -> server.sendRequest(p.id()), "Friend request sent to " + p.fullName() + ".")));
            case REQUEST_RECEIVED -> buttons.add(Ui.button("Accept", Ui.Style.PRIMARY, "check",
                () -> act(() -> server.acceptFrom(p.id()), "You and " + p.fullName() + " are now friends.")));
            case REQUEST_SENT -> buttons.add(Ui.pill("Pending", Ui.Tone.WARNING));
            default -> { }
        }
        buttons.add(chatButton(p));
        buttons.add(Ui.button("Create group", Ui.Style.SECONDARY, "group", () -> nav.openCreateGroup(List.of(p.username()))));
        if (p.relation() == Relation.NONE) buttons.add(Ui.button("Block", Ui.Style.DANGER_OUTLINE, "block", () -> block(p)));
        return actions(buttons.toArray(JComponent[]::new));
    }

    // ------------------------------------------------------------------ shared

    private JTextField filterField(JPanel page, String hint, int columns) {
        JTextField field = Ui.field(hint, columns);
        field.setText(filter);
        Ui.add(page, Ui.search(field));
        return field;
    }

    private JButton chatButton(Person p) {
        return Ui.button("Chat", Ui.Style.SECONDARY, "chat", () -> {
            try {
                nav.openChat(server.chatWith(p.id()).id());
            } catch (ServiceException e) {
                notice(Ui.Banner.ERROR, e.getMessage());
                render();
            }
        });
    }

    private static JPanel actions(JComponent... items) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        row.setOpaque(false);
        for (JComponent item : items) row.add(item);
        return row;
    }

    /** Runs a server action, then re-renders with a success or error banner. */
    private void act(Runnable action, String success) {
        try {
            action.run();
            notice(Ui.Banner.SUCCESS, success);
        } catch (ServiceException e) {
            notice(Ui.Banner.ERROR, e.getMessage());
        }
        render();
    }
}
