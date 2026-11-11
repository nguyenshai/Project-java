package chatapp.admin.ui.moderation;

import chatapp.admin.ui.preview.PreviewData;
import chatapp.admin.ui.preview.PreviewData.*;
import chatapp.admin.ui.preview.PreviewData.Event;
import chatapp.admin.ui.shared.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import static chatapp.admin.ui.accounts.Accounts.DATE;
import static chatapp.admin.ui.accounts.Accounts.TIME;

public final class Moderation extends Screen {
    private final PreviewData data;
    private final PeriodPicker period;
    private String reportQuery = "";
    private String reportStatus = "All statuses";
    private String notice = "";

    public Moderation(PreviewData data, PeriodPicker period, String page) {
        this.data = data;
        this.period = period;
        switch (page) {
            case "Chat Groups" -> groups();
            case "Spam Reports" -> reports(false);
            default -> history(false);
        }
    }

    private void history(boolean registrations) {
        JPanel page = page("Account History");
        Ui.add(page, Ui.tabs(registrations ? "New Accounts" : "Sign-ins", new String[]{"Sign-ins", "New Accounts"},
            t -> history(t.equals("New Accounts"))));
        JPanel tools = Ui.row();
        JTextField search = Ui.field("Search name or username", 24);
        tools.add(Ui.search(search));
        JPanel results = Ui.column();
        Runnable update = () -> {
            results.removeAll();
            var range = period.range();
            if (registrations) {
                JTable table = Ui.table("Created", "Username", "Full name");
                var users = data.users.stream().filter(u -> range.includes(u.created().toLocalDate())
                    && Ui.matches(search.getText(), u.name(), data.handle(u.id())))
                    .sorted(Comparator.comparing(User::created).reversed()).toList();
                JPanel split = new JPanel(new GridLayout(1, 2, 16, 0));
                split.setOpaque(false);
                split.add(Ui.scrollTable(table));
                Ui.rows(table, users.stream().map(u -> new Object[]{u.created(), data.handle(u.id()), u.name()}).toList());
                timestampColumn(table, 0);
                JPanel chart = Ui.card(new BorderLayout(0, 12));
                chart.add(Ui.label("Monthly registrations", 17, true), BorderLayout.NORTH);
                chart.add(BarChart.forDates(period.year(), users.stream().map(u -> u.created().toLocalDate()).toList()));
                split.add(chart);
                Ui.add(results, split);
            } else {
                var rows = data.signIns.stream().filter(s -> range.includes(s.at().toLocalDate())
                    && Ui.matches(search.getText(), data.name(s.userId()), data.handle(s.userId())))
                    .sorted(Comparator.comparing(SignIn::at).reversed()).toList();
                JPanel split = new JPanel(new GridLayout(1, 2, 16, 0));
                split.setOpaque(false);
                JTable table = Ui.table("Timestamp", "Username", "Full name");
                split.add(Ui.scrollTable(table));
                Ui.rows(table, rows.stream().map(s -> new Object[]{s.at(), data.handle(s.userId()), data.name(s.userId())}).toList());
                timestampColumn(table, 0);
                JPanel chart = Ui.column();
                Ui.add(chart, Ui.label("Sign-in Activity", 17, true));
                var dates = rows.stream().map(s -> s.at().toLocalDate()).toList();
                var byDay = dates.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                LocalDate peak = byDay.keySet().stream().max(Comparator.comparingLong((LocalDate d) -> byDay.get(d))
                    .thenComparing(Comparator.naturalOrder())).orElse(null);
                LocalDate cutoff = PreviewData.NOW.toLocalDate();
                LocalDate lastDay = range.to().isBefore(cutoff) ? range.to() : cutoff;
                LocalDate quiet = dates.isEmpty() ? null : range.from().datesUntil(lastDay.plusDays(1))
                    .min(Comparator.comparingLong((LocalDate d) -> byDay.getOrDefault(d, 0L)).thenComparing(Comparator.reverseOrder()))
                    .orElse(null);
                Ui.add(chart, metrics(new String[]{"Total sign-ins", "Unique users", dayLabel("Peak day", peak), dayLabel("Quietest day", quiet)},
                    new long[]{rows.size(), rows.stream().map(SignIn::userId).distinct().count(),
                        peak == null ? 0 : byDay.get(peak), quiet == null ? 0 : byDay.getOrDefault(quiet, 0L)}));
                Ui.add(chart, Ui.label("Monthly sign-ins", 14, true));
                Ui.add(chart, BarChart.forDates(period.year(), dates));
                JPanel chartCard = Ui.card(new BorderLayout());
                chartCard.add(chart);
                split.add(chartCard);
                Ui.add(results, split);
            }
            results.revalidate();
            results.repaint();
        };
        tools.add(period.button(update));
        Ui.add(page, tools);
        Ui.add(page, results);
        Ui.onText(search, update);
        update.run();
        display(page);
    }

    private static String dayLabel(String title, LocalDate date) {
        return date == null ? title : title + " · " + date.format(DateTimeFormatter.ofPattern("MMM d", java.util.Locale.ENGLISH));
    }

    private void groups() {
        JPanel page = page("Chat Groups");
        JTextField search = Ui.field("Search group name", 24);
        Ui.add(page, Ui.search(search));
        JTable table = Ui.table("Group name", "Created", "Members", "Group admins", "");
        table.getColumnModel().getColumn(4).setMaxWidth(40);
        dateColumn(table, 1);
        Ui.add(page, Ui.scrollTable(table));
        JLabel total = Ui.muted("");
        Ui.add(page, total);
        List<Group> visible = new ArrayList<>();
        Runnable update = () -> {
            visible.clear();
            data.groups.stream().filter(g -> Ui.matches(search.getText(), g.name()))
                .sorted(Comparator.comparing(Group::created).reversed()).forEach(visible::add);
            Ui.rows(table, visible.stream().map(g -> new Object[]{g.name(), g.created(),
                members(g).size(), members(g).stream().filter(m -> m.role().equals("Group admin")).count(), "›"}).toList());
            total.setText(visible.size() + " groups");
        };
        Ui.onText(search, update);
        Ui.onRow(table, row -> group(visible.get(row), "Members"));
        update.run();
        display(page);
    }

    private List<Member> members(Group group) {
        return group.members().stream().filter(m -> data.user(m.userId()) != null).toList();
    }

    private void group(Group group, String tab) {
        JPanel page = page(group.name());
        addBack(page, "Chat Groups", this::groups);
        Ui.add(page, Ui.muted("Created " + DATE.format(group.created()) + "  ·  Created by "
            + data.name(group.creatorId()) + " (" + data.handle(group.creatorId()) + ")"));
        Ui.add(page, Ui.tabs(tab, new String[]{"Members", "Group admins", "Activity", "Monitoring"}, t -> group(group, t)));
        switch (tab) {
            case "Activity" -> timeline(page, group);
            case "Monitoring" -> monitoring(page, group);
            default -> {
                JTable table = Ui.table("Full name", "Username", "Group role", "Joined");
                dateColumn(table, 3);
                Ui.add(page, Ui.scrollTable(table));
                Ui.rows(table, members(group).stream()
                    .filter(m -> !tab.equals("Group admins") || m.role().equals("Group admin"))
                    .map(m -> new Object[]{data.name(m.userId()), data.handle(m.userId()), m.role(), m.joined()}).toList());
            }
        }
        display(page);
    }

    private void timeline(JPanel page, Group group) {
        JPanel bar = Ui.row();
        JTextField search = Ui.field("Search people or messages", 20);
        bar.add(Ui.search(search));
        var kinds = Ui.choice(new String[]{"All events", "Message", "Member joined", "Member left", "Member removed", "Group renamed", "Role changed"});
        bar.add(kinds);
        JTable table = Ui.table("Timestamp", "Event type", "Details");
        table.setRowHeight(30);
        table.getColumnModel().getColumn(2).setPreferredWidth(520);
        timestampColumn(table, 0);
        Runnable update = () -> Ui.rows(table, groupEvents(group).stream()
            .filter(e -> Ui.matches(search.getText(), e.detail()) && (kinds.getSelectedIndex() == 0 || e.type().equals(kinds.getSelectedItem())))
            .map(e -> new Object[]{e.at(), e.type(), e.detail()}).toList());
        bar.add(period.button(update));
        Ui.add(page, bar);
        Ui.add(page, Ui.muted("Activity timeline  ·  Read only"));
        Ui.add(page, Ui.scrollTable(table));
        Ui.onText(search, update);
        kinds.addActionListener(e -> update.run());
        update.run();
    }

    private List<Event> groupEvents(Group group) {
        var range = period.range();
        return data.events.stream().filter(e -> e.group().equals(group.name()) && range.includes(e.at().toLocalDate()))
            .sorted(Comparator.comparing(Event::at).reversed()).toList();
    }

    private void monitoring(JPanel page, Group group) {
        JPanel tools = Ui.row();
        var graph = Ui.choice(new String[]{"Activities", "Messages", "Members joined"});
        JPanel results = Ui.column();
        Runnable update = () -> {
            results.removeAll();
            var events = groupEvents(group);
            var messages = events.stream().filter(e -> e.type().equals("Message")).toList();
            var joins = memberJoinDates(group);
            long joinedThisYear = joins.stream().filter(date -> date.getYear() == period.year()).count();
            Ui.add(results, metrics(new String[]{"Activities", "Messages", "Members joined"},
                new long[]{events.size(), messages.size(), joinedThisYear}));
            JPanel chart = Ui.column();
            Ui.add(chart, Ui.label("Monthly " + graph.getSelectedItem().toString().toLowerCase(java.util.Locale.ENGLISH), 17, true));
            var dates = switch (graph.getSelectedIndex()) {
                case 1 -> messages.stream().map(e -> e.at().toLocalDate()).toList();
                case 2 -> joins;
                default -> events.stream().map(e -> e.at().toLocalDate()).toList();
            };
            Ui.add(chart, BarChart.forDates(period.year(), dates));
            JPanel card = Ui.card(new BorderLayout());
            card.add(chart);
            Ui.add(results, card);
            results.revalidate();
            results.repaint();
        };
        tools.add(period.button(update));
        tools.add(Ui.muted("Graph"));
        tools.add(graph);
        Ui.add(page, tools);
        Ui.add(page, results);
        graph.addActionListener(e -> update.run());
        update.run();
    }

    private List<LocalDate> memberJoinDates(Group group) {
        var dates = data.events.stream().filter(e -> e.group().equals(group.name()) && e.type().equals("Member joined"))
            .map(e -> e.at().toLocalDate()).toList();
        return dates.isEmpty() ? group.members().stream().map(Member::joined).toList() : dates;
    }

    private void reports(boolean byAccount) {
        JPanel page = page("Spam Reports");
        Ui.add(page, Ui.tabs(byAccount ? "By account" : "Reports", new String[]{"Reports", "By account"},
            label -> reports(label.equals("By account"))));
        JPanel filters = Ui.row();
        JTextField search = Ui.field(byAccount ? "Search name or username" : "Search reported username", 22);
        search.setText(reportQuery);
        filters.add(Ui.search(search));
        JPanel results = Ui.column();
        var status = Ui.choice(new String[]{"All statuses", "Active", "Locked"});
        status.setSelectedItem(reportStatus);
        Runnable update = () -> {
            reportQuery = search.getText();
            reportStatus = (String) status.getSelectedItem();
            results.removeAll();
            List<Report> rows = reportsInPeriod().stream()
                .filter(r -> Ui.matches(reportQuery, data.handle(r.reported()), byAccount ? data.name(r.reported()) : "")).toList();
            if (byAccount) {
                var grouped = rows.stream().collect(Collectors.groupingBy(Report::reported));
                List<String> visible = grouped.keySet().stream()
                    .filter(id -> reportStatus.equals("All statuses") || reportStatus.equals(data.status(id)))
                    .sorted(Comparator.comparing((String id) -> grouped.get(id).getFirst().at()).reversed()).toList();
                var range = period.range();
                JTable table = Ui.table("Reported account", "Username", "Reports", "Last reported", "Prior locks", "Account status", "");
                table.getColumnModel().getColumn(6).setMaxWidth(40);
                timestampColumn(table, 3);
                Ui.add(results, Ui.scrollTable(table));
                Ui.rows(table, visible.stream().map(id -> new Object[]{data.name(id), data.handle(id), grouped.get(id).size(),
                    grouped.get(id).getFirst().at(), data.locks.stream().filter(e -> e.userId().equals(id) && e.locked() && range.includes(e.at().toLocalDate())).count(),
                    data.status(id), "›"}).toList());
                Ui.onRow(table, row -> accountReports(visible.get(row)));
                Ui.add(results, Ui.muted(visible.size() + " reported accounts"));
            } else {
                reportTable(results, rows, r -> reportDetail(r, () -> reports(false)));
            }
            results.revalidate();
            results.repaint();
        };
        filters.add(period.button(update));
        if (byAccount) filters.add(status);
        Ui.add(page, filters);
        if (!notice.isEmpty()) Ui.add(page, Ui.label(notice, 14, true));
        Ui.add(page, results);
        Ui.onText(search, update);
        status.addActionListener(e -> update.run());
        update.run();
        display(page);
    }

    private List<Report> reportsInPeriod() {
        var range = period.range();
        return data.reports.stream().filter(r -> range.includes(r.at().toLocalDate()))
            .sorted(Comparator.comparing(Report::at).reversed()).toList();
    }

    private void reportTable(JPanel page, List<Report> rows, java.util.function.Consumer<Report> open) {
        JTable table = Ui.table("Reported at", "Reported username", "Reporter", "Reason", "Account status", "");
        table.getColumnModel().getColumn(3).setPreferredWidth(240);
        table.getColumnModel().getColumn(5).setMaxWidth(40);
        timestampColumn(table, 0);
        Ui.add(page, Ui.scrollTable(table));
        Ui.rows(table, rows.stream().map(r -> new Object[]{r.at(), data.handle(r.reported()),
            data.handle(r.reporter()), r.reason(), data.status(r.reported()), "›"}).toList());
        Ui.onRow(table, row -> open.accept(rows.get(row)));
        Ui.add(page, Ui.muted(rows.size() + " reports"));
    }

    private void accountReports(String id) {
        JPanel page = page(data.name(id));
        addBack(page, "Reported accounts", () -> reports(true));
        Ui.add(page, Ui.muted(data.handle(id) + "  ·  " + data.status(id)));
        Ui.add(page, period.button(() -> accountReports(id)));
        Ui.add(page, Ui.label("Reports in selected year", 16, true));
        reportTable(page, reportsInPeriod().stream().filter(r -> r.reported().equals(id)).toList(),
            r -> reportDetail(r, () -> accountReports(id)));
        Ui.add(page, Ui.label("Admin lock history", 16, true));
        JTable table = Ui.table("When", "Action", "Admin");
        timestampColumn(table, 0);
        Ui.add(page, Ui.scrollTable(table));
        var range = period.range();
        Ui.rows(table, data.locks.stream().filter(e -> e.userId().equals(id) && range.includes(e.at().toLocalDate()))
            .sorted(Comparator.comparing(LockAction::at).reversed())
            .map(e -> new Object[]{e.at(), e.locked() ? "Account locked" : "Account unlocked", "Alex Morgan"}).toList());
        display(page);
    }

    private void reportDetail(Report r, Runnable back) {
        JPanel page = page("Report #" + r.number());
        addBack(page, "Spam Reports", back);
        JPanel card = Ui.card(new BorderLayout(0, 12)), body = Ui.column();
        Ui.add(body, Ui.label("Report details", 17, true));
        Ui.add(body, Ui.muted("Reported account: " + data.name(r.reported()) + " (" + data.handle(r.reported())
            + ")  ·  " + data.status(r.reported())));
        Ui.add(body, Ui.muted("Reported by: " + data.name(r.reporter()) + " (" + data.handle(r.reporter())
            + ")  ·  " + TIME.format(r.at())));
        Ui.add(body, Ui.label("Reason", 13, true));
        Ui.add(body, Ui.label(r.reason(), 14, false));
        JPanel message = Ui.card(new BorderLayout(0, 8));
        message.setBackground(Ui.SOFT);
        message.add(Ui.muted("Reported message"), BorderLayout.NORTH);
        message.add(Ui.label(r.message(), 14, false));
        Ui.add(body, message);
        card.add(body);
        if (data.user(r.reported()) != null && !data.user(r.reported()).locked()) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actions.setOpaque(false);
            actions.add(Ui.button("Lock account", Ui.RED, Color.WHITE, () -> {
                int result = JOptionPane.showOptionDialog(this, new Object[]{data.name(r.reported()) + " (" + data.handle(r.reported()) + ")",
                    "This user will lose access until the account is unlocked."}, "Lock account",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Cancel", "Lock account"}, "Cancel");
                if (result == 1) {
                    data.lock(r.reported(), true);
                    notice = "Account locked: " + data.handle(r.reported());
                    back.run();
                }
            }));
            card.add(actions, BorderLayout.SOUTH);
        }
        Ui.add(page, card);
        display(page);
    }

    private void addBack(JPanel page, String label, Runnable action) {
        JButton back = Ui.button(label, action);
        back.setIcon(Ui.icon("back", 16));
        page.add(back, 0);
        page.add(Box.createVerticalStrut(8), 1);
    }

    private JPanel metrics(String[] labels, long[] values) {
        JPanel row = new JPanel(new GridLayout(labels.length == 4 ? 2 : 1, 0, 8, 8));
        row.setOpaque(false);
        for (int i = 0; i < labels.length; i++) {
            JPanel card = Ui.card(new BorderLayout(0, 3));
            card.setBackground(Ui.SOFT);
            card.add(Ui.muted(labels[i]), BorderLayout.NORTH);
            card.add(Ui.label(String.valueOf(values[i]), 21, true));
            row.add(card);
        }
        return row;
    }

    private static void dateColumn(JTable table, int i) { formattedColumn(table, i, DATE); }
    private static void timestampColumn(JTable table, int i) { formattedColumn(table, i, TIME); }
    private static void formattedColumn(JTable table, int i, DateTimeFormatter formatter) {
        table.getColumnModel().getColumn(i).setCellRenderer((grid, value, selected, focus, row, col) ->
            grid.getDefaultRenderer(Object.class).getTableCellRendererComponent(grid,
                value instanceof java.time.temporal.TemporalAccessor date ? formatter.format(date) : value, selected, focus, row, col));
    }
}
