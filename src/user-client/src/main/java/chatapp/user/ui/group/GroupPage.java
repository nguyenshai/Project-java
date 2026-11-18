package chatapp.user.ui.group;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.Views.Group;
import chatapp.user.ui.Navigator;
import chatapp.user.ui.shared.RowTable;
import chatapp.user.ui.shared.RowTable.Column;
import chatapp.user.ui.shared.Screen;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.util.List;

/**
 * Group page (Figma G1–G9): my groups with a name and role filter, the Create group form (F12a)
 * and the group detail (F12b–e, g). Group chat itself (F12f) is on the Chat page; "Open chat" links there.
 */
public final class GroupPage extends Screen {
    private static final String[] ROLES = {"All roles", "Group admin", "Member"};
    final PreviewServer server;
    final Navigator nav;
    private String query = "", role = ROLES[0];

    public GroupPage(PreviewServer server, Navigator nav) {
        this.server = server;
        this.nav = nav;
        showList();
    }

    /** The page opened on a group's detail (Navigator link). */
    public static GroupPage detail(PreviewServer server, Navigator nav, long groupId) {
        GroupPage page = new GroupPage(server, nav);
        page.showDetail(groupId);
        return page;
    }

    /** The page opened on Create group with these usernames already added (Navigator link). */
    public static GroupPage create(PreviewServer server, Navigator nav, List<String> usernames) {
        GroupPage page = new GroupPage(server, nav);
        page.showCreate(usernames);
        return page;
    }

    void showList() {
        JPanel page = page("Group", Ui.button("Create group", Ui.Style.PRIMARY, "plus", () -> showCreate(List.of())));
        JTextField search = Ui.field("Search group name", 22);
        search.setText(query);
        JComboBox<String> roles = Ui.choice(ROLES);
        roles.setSelectedItem(role);
        JPanel filters = Ui.row();
        filters.add(Ui.search(search));
        filters.add(roles);
        Ui.add(page, filters);
        JPanel results = Ui.column();
        Ui.add(page, results);
        Runnable update = () -> {
            query = search.getText();
            role = (String) roles.getSelectedItem();
            results.removeAll();
            List<Group> groups = server.groups(query).stream()
                .filter(g -> role.equals(ROLES[0]) || role.equals(roleOf(g))).toList();
            if (groups.isEmpty()) {
                boolean none = query.isBlank() && role.equals(ROLES[0]);
                Ui.add(results, Ui.empty("group", none ? "No groups yet" : "No groups match",
                    none ? "Create a group with at least one other person." : "Try another name or role.",
                    none ? Ui.button("Create group", Ui.Style.PRIMARY, "plus", () -> showCreate(List.of())) : null));
            } else {
                RowTable table = new RowTable(52, Column.grow("Group name", 3), Column.grow("Members", 1),
                    Column.grow("My role", 1.5), Column.grow("Encryption", 1.5), Column.grow("Created", 1.4), Column.fixed("", 30));
                for (Group g : groups) {
                    table.addRow(() -> showDetail(g.id()), Ui.label(g.name(), 14, true),
                        Ui.label(String.valueOf(g.members().size()), 14, false),
                        Ui.pill(roleOf(g), g.meAdmin() ? Ui.Tone.MEMBERSHIP : Ui.Tone.NEUTRAL),
                        g.encrypted() ? Ui.pill("Encrypted", Ui.Tone.MEMBERSHIP) : Ui.muted("Off"),
                        Ui.label(Ui.date(g.createdAt()), 14, false), new JLabel(Ui.icon("chevron", 16)));
                }
                Ui.add(results, table);
                Ui.add(results, Ui.muted(groups.size() + (groups.size() == 1 ? " group" : " groups")));
            }
            results.revalidate();
            results.repaint();
        };
        Ui.onText(search, update);
        roles.addActionListener(e -> update.run());
        update.run();
        display(page);
    }

    static String roleOf(Group g) {
        return g.meAdmin() ? "Group admin" : "Member";
    }

    void showCreate(List<String> usernames) {
        new CreateGroupForm(this, usernames).show();
    }

    void showDetail(long groupId) {
        new GroupDetail(this, groupId).show();
    }

    // Screen helpers opened up to the form and detail views in this package.
    JPanel newPage(String title, JComponent... actions) {
        return actions.length == 0 ? page(title) : page(title, actions);
    }

    void backTo(JPanel page) {
        back(page, "Groups", this::showList);
    }

    void banner(Ui.Banner kind, String text) {
        notice(kind, text);
    }

    void show(JPanel page) {
        display(page);
    }
}
