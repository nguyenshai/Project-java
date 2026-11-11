package chatapp.admin.ui.accounts;

import chatapp.admin.ui.preview.PreviewData;
import chatapp.admin.ui.preview.PreviewData.User;
import chatapp.admin.ui.shared.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class Accounts extends Screen {
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.ENGLISH);
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("MMM d, uuuu · HH:mm", Locale.ENGLISH);
    private final PreviewData data;
    private String query = "";
    private String statusFilter = "All statuses";
    private PeriodPicker.Range period = PeriodPicker.SAMPLE;

    public Accounts(PreviewData data) {
        this.data = data;
        directory();
    }

    private void directory() {
        JPanel page = page("Users");
        JLabel title = (JLabel) page.getComponent(0);
        page.remove(title);
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(title, BorderLayout.WEST);
        heading.add(Ui.button("Add user", Ui.BLUE, Color.WHITE, () -> form(null)), BorderLayout.EAST);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(heading, 0);

        JPanel filters = Ui.row();
        JTextField search = Ui.field("Search name or username", 23);
        search.setText(query);
        filters.add(Ui.search(search));
        var status = Ui.choice(new String[]{"All statuses", "Active", "Locked"});
        status.setSelectedItem(statusFilter);
        filters.add(status);
        Ui.add(page, filters);

        JTable table = Ui.table("Full name", "Username", "Email", "Status", "Created", "");
        table.setRowHeight(55);
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(210);
        table.getColumnModel().getColumn(5).setMaxWidth(40);
        JScrollPane scroll = Ui.scrollTable(table);
        Ui.add(page, scroll);
        JLabel total = Ui.muted("");
        Ui.add(page, total);
        java.util.List<User> visible = new java.util.ArrayList<>();
        Runnable refresh = () -> {
            query = search.getText();
            statusFilter = (String) status.getSelectedItem();
            visible.clear();
            data.users.stream().filter(u -> Ui.matches(query, u.name(), u.username(), "@" + u.username())
                && (statusFilter.equals("All statuses") || statusFilter.equals(data.status(u.id()))))
                .sorted(Comparator.comparing(User::created).reversed()).forEach(visible::add);
            Ui.rows(table, visible.stream().map(u -> new Object[]{u.name(), "@" + u.username(), u.email(),
                data.status(u.id()), u.created().toLocalDate(), "›"}).toList());
            int height = Math.min(482, 38 + 55 * visible.size());
            scroll.setPreferredSize(new Dimension(0, Math.max(88, height)));
            scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(88, height)));
            total.setText(visible.size() + " users");
        };
        Ui.onText(search, refresh);
        status.addActionListener(e -> refresh.run());
        Ui.onRow(table, i -> detail(visible.get(i).id(), "Profile"));
        refresh.run();
        display(page);
    }

    private void detail(String id, String tab) {
        User user = data.user(id);
        if (user == null) { directory(); return; }
        JPanel page = page(user.name());
        JButton back = Ui.button("Users", this::directory);
        back.setIcon(Ui.icon("back", 16));
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(back, 0);
        page.add(Box.createVerticalStrut(8), 1);
        Ui.add(page, Ui.muted("@" + user.username() + "  ·  " + data.status(id)));
        Ui.add(page, Ui.tabs(tab, new String[]{"Profile", "Login History", "Friends & Groups"}, t -> detail(id, t)));
        switch (tab) {
            case "Login History" -> history(page, user);
            case "Friends & Groups" -> relationships(page, user);
            default -> profile(page, user);
        }
        display(page);
    }

    private void profile(JPanel page, User u) {
        JPanel card = Ui.card(new BorderLayout(0, 16));
        card.add(Ui.label("Account details", 18, true), BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridLayout(2, 3, 14, 14));
        fields.setOpaque(false);
        fields.add(info("Full name", u.name()));
        fields.add(info("Username", u.username()));
        fields.add(info("Email", u.email()));
        fields.add(info("Date of birth", u.birth()));
        fields.add(info("Gender", u.gender()));
        fields.add(info("Address", u.address()));
        card.add(fields);
        JPanel actions = Ui.row();
        actions.add(Ui.button("Edit user", Ui.BLUE, Color.WHITE, () -> form(u)));
        actions.add(Ui.button("Update password", () -> password(u)));
        String lock = u.locked() ? "Unlock account" : "Lock account";
        actions.add(Ui.button(lock, () -> {
            String message = u.locked() ? "Restore sign-in access?" : "This user will lose access until the account is unlocked.";
            if (confirm(lock, u, message)) { data.lock(u.id(), !u.locked()); detail(u.id(), "Profile"); }
        }));
        actions.add(Ui.button("Delete account", Ui.RED, Color.WHITE, () -> {
            if (confirm("Delete account", u, "Confirm that you want to delete this account.")) {
                data.users.removeIf(item -> item.id().equals(u.id()));
                directory();
            }
        }));
        card.add(actions, BorderLayout.SOUTH);
        Ui.add(page, card);
    }

    private boolean confirm(String title, User u, String body) {
        return JOptionPane.showOptionDialog(this, new Object[]{u.name() + " (@" + u.username() + ")", body},
            title, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
            new String[]{"Cancel", title}, "Cancel") == 1;
    }

    private JPanel info(String name, String value) {
        JPanel panel = Ui.column();
        panel.add(Ui.muted(name));
        panel.add(Ui.label(value.isBlank() ? "—" : value, 14, false));
        return panel;
    }

    private void history(JPanel page, User u) {
        Ui.add(page, PeriodPicker.range(period, value -> { period = value; detail(u.id(), "Login History"); }));
        var events = data.signIns.stream().filter(s -> s.userId().equals(u.id()) && period.includes(s.at().toLocalDate()))
            .sorted(Comparator.comparing(PreviewData.SignIn::at).reversed()).toList();
        JPanel split = new JPanel(new GridLayout(1, 2, 18, 0));
        split.setOpaque(false);
        JPanel history = Ui.card(new BorderLayout(0, 9));
        history.add(Ui.label("Sign-in history", 17, true), BorderLayout.NORTH);
        JTable table = Ui.table("Timestamp");
        history.add(Ui.scrollTable(table), BorderLayout.CENTER);
        Ui.rows(table, events.stream().map(e -> new Object[]{TIME.format(e.at())}).toList());
        split.add(history);
        JPanel activity = Ui.card(new BorderLayout(0, 9));
        activity.add(Ui.label("Sign-in Activity", 17, true), BorderLayout.NORTH);
        JPanel stats = Ui.row();
        stats.add(info("Total sign-ins", String.valueOf(events.size())));
        stats.add(info("Days with sign-ins", String.valueOf(events.stream().map(e -> e.at().toLocalDate()).distinct().count())));
        String last = data.signIns.stream().filter(e -> e.userId().equals(u.id())).map(PreviewData.SignIn::at)
            .max(Comparator.naturalOrder()).map(TIME::format).orElse("—");
        stats.add(info("Last sign-in", last));
        JPanel chart = Ui.column();
        Ui.add(chart, stats);
        Ui.add(chart, Ui.label("Daily sign-ins", 14, true));
        Ui.add(chart, BarChart.forDates(period, events.stream().map(e -> e.at().toLocalDate()).toList()));
        activity.add(chart);
        split.add(activity);
        Ui.add(page, split);
    }

    private void relationships(JPanel page, User u) {
        var groupRows = data.groups.stream().flatMap(g -> g.members().stream().filter(m -> m.userId().equals(u.id()))
            .map(m -> new Object[]{g.name(), m.role(), g.members().stream().filter(member -> data.user(member.userId()) != null).count(),
                g.created(), m.joined()})).toList();
        var friendRows = data.friendships(u.id()).stream().map(f -> {
            User person = data.user(f.other(u.id()));
            return new Object[]{person.name(), "@" + person.username(), person.email(), data.status(person.id()), f.since()};
        }).toList();
        Ui.add(page, list("Chat Groups", "Find a group", new String[]{"All group roles", "Group admin", "Member"}, 1,
            new String[]{"Group name", "Group role", "Members", "Created", "Joined"}, groupRows));
        Ui.add(page, list("Friends", "Find a person by name or username", new String[]{"Account status", "Active", "Locked"}, 3,
            new String[]{"Full name", "Username", "Email", "Account status", "Friends since"}, friendRows));
    }

    private JPanel list(String title, String hint, String[] filters, int column, String[] headers, List<Object[]> records) {
        JPanel section = Ui.column();
        JPanel header = Ui.row();
        header.add(Ui.label(title, 17, true));
        JPanel body = Ui.column();
        JButton toggle = Ui.button("", () -> {});
        toggle.setIcon(Ui.icon("down", 15));
        toggle.addActionListener(e -> {
            body.setVisible(!body.isVisible());
            toggle.setIcon(Ui.icon(body.isVisible() ? "down" : "chevron", 15));
            section.revalidate();
        });
        toggle.getAccessibleContext().setAccessibleName("Show or hide " + title);
        header.add(toggle);
        Ui.add(section, header);
        JPanel toolbar = Ui.row();
        JTextField search = Ui.field(hint, 22);
        toolbar.add(Ui.search(search));
        var select = Ui.choice(filters);
        toolbar.add(select);
        Ui.add(body, toolbar);
        JTable table = Ui.table(headers);
        table.setRowHeight(36);
        Ui.add(body, Ui.scrollTable(table));
        Runnable update = () -> Ui.rows(table, records.stream().filter(r ->
            Ui.matches(search.getText(), String.valueOf(r[0]), String.valueOf(r[1]))
            && (select.getSelectedIndex() == 0 || select.getSelectedItem().equals(r[column]))).toList());
        Ui.onText(search, update);
        select.addActionListener(e -> update.run());
        update.run();
        section.add(body);
        return section;
    }

    private void form(User u) {
        boolean edit = u != null;
        JPanel page = page(edit ? "Edit user" : "Add user");
        JButton back = Ui.button("Users", this::directory);
        back.setIcon(Ui.icon("back", 16));
        page.add(back, 0);
        page.add(Box.createVerticalStrut(8), 1);
        JPanel card = Ui.card(new BorderLayout(0, 16));
        JPanel fields = new JPanel(new GridLayout(0, 2, 16, 10));
        fields.setOpaque(false);
        JTextField name = input(fields, "Full name", edit ? u.name() : "");
        JTextField username = input(fields, "Username", edit ? u.username() : "");
        JTextField email = input(fields, "Email", edit ? u.email() : "");
        JTextField address = input(fields, "Address (optional)", edit ? u.address() : "");
        JTextField birth = input(fields, "Date of birth (optional, YYYY-MM-DD)", edit ? u.birth() : "");
        var gender = Ui.choice(new String[]{"Select", "Female", "Male", "Other", "Not specified"});
        if (edit) gender.setSelectedItem(u.gender());
        fields.add(labeled("Gender", gender));
        JPasswordField pass = new JPasswordField(), repeat = new JPasswordField();
        if (!edit) {
            fields.add(labeled("Password", pass));
            fields.add(labeled("Confirm password", repeat));
        }
        card.add(fields);
        JPanel footer = Ui.column();
        JLabel error = Ui.muted("");
        error.setForeground(Ui.RED);
        footer.add(error);
        JPanel actions = Ui.row();
        actions.add(Ui.button("Cancel", () -> { if (edit) detail(u.id(), "Profile"); else directory(); }));
        actions.add(Ui.button(edit ? "Save changes" : "Add user", Ui.BLUE, Color.WHITE, () -> {
            String handle = username.getText().trim(), mail = email.getText().trim(), date = birth.getText().trim();
            if (name.getText().isBlank() || handle.isEmpty() || !mail.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || gender.getSelectedIndex() == 0) {
                error.setText("Enter a name, username, valid email, and gender.");
                return;
            }
            if (data.users.stream().anyMatch(other -> (!edit || !other.id().equals(u.id()))
                    && (other.username().equalsIgnoreCase(handle) || other.email().equalsIgnoreCase(mail)))) {
                error.setText("The username or email is already in use.");
                return;
            }
            try {
                if (!date.isEmpty() && LocalDate.parse(date).isAfter(PreviewData.NOW.toLocalDate()))
                    throw new java.time.DateTimeException("future");
            } catch (java.time.DateTimeException e) { error.setText("Enter a valid birth date (YYYY-MM-DD)."); return; }
            if (!edit && !passwordsMatch(pass, repeat)) { error.setText("Enter matching passwords."); return; }
            String id = edit ? u.id() : UUID.randomUUID().toString();
            data.save(new User(id, name.getText().trim(), handle, mail, address.getText().trim(), date,
                (String) gender.getSelectedItem(), edit ? u.created() : PreviewData.NOW, edit && u.locked()));
            pass.setText("");
            repeat.setText("");
            detail(id, "Profile");
        }));
        footer.add(actions);
        card.add(footer, BorderLayout.SOUTH);
        Ui.add(page, card);
        display(page);
    }

    private JTextField input(JPanel grid, String label, String value) {
        JTextField field = Ui.field(label, 17);
        field.setText(value);
        grid.add(labeled(label, field));
        return field;
    }

    private JPanel labeled(String title, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel label = Ui.label(title, 13, true);
        label.setLabelFor(field);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field);
        return panel;
    }

    private void password(User u) {
        JPasswordField first = new JPasswordField(20), second = new JPasswordField(20);
        JPanel fields = Ui.column();
        Ui.add(fields, labeled("New password", first));
        Ui.add(fields, labeled("Confirm password", second));
        if (JOptionPane.showConfirmDialog(this, fields, "Update password · " + u.name(),
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(this, passwordsMatch(first, second)
                ? "Password form validated for the V1 preview." : "Enter matching passwords.");
        }
        first.setText("");
        second.setText("");
    }

    private static boolean passwordsMatch(JPasswordField first, JPasswordField second) {
        char[] a = first.getPassword(), b = second.getPassword();
        try { return a.length > 0 && java.util.Arrays.equals(a, b); }
        finally { java.util.Arrays.fill(a, '\0'); java.util.Arrays.fill(b, '\0'); }
    }
}
