package chatapp.admin.ui.analytics;

import chatapp.admin.ui.preview.PreviewData;
import chatapp.admin.ui.preview.PreviewData.User;
import chatapp.admin.ui.shared.BarChart;
import chatapp.admin.ui.shared.PeriodPicker;
import chatapp.admin.ui.shared.PeriodPicker.Range;
import chatapp.admin.ui.shared.Screen;
import chatapp.admin.ui.shared.Ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.IntPredicate;

public final class Analytics extends Screen {
    private static final String[] TABS = {"Overview", "Friend Counts", "Activity"};
    private static final String[] METRICS = {"Registrations", "Sign-ins", "Active users", "Messages sent"};
    private static final String[] MONTHS = java.util.Arrays.stream(Month.values())
        .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)).toArray(String[]::new);
    private final PreviewData data;
    private String tab = TABS[0];
    private int overviewYear = PreviewData.NOW.getYear();
    private int activityYear = overviewYear;
    private String metric = METRICS[0];
    private Range activityRange = PeriodPicker.SAMPLE;

    public Analytics(PreviewData data) {
        this.data = data;
        refresh();
    }

    public void refresh() {
        JPanel page = page("Analytics");
        Ui.add(page, Ui.tabs(tab, TABS, selected -> {
            tab = selected;
            refresh();
        }));
        switch (tab) {
            case "Friend Counts" -> friendCounts(page);
            case "Activity" -> activity(page);
            default -> overview(page);
        }
        display(page);
    }

    private void overview(JPanel page) {
        JPanel filters = Ui.row();
        filters.add(PeriodPicker.year(overviewYear, year -> {
            overviewYear = year;
            refresh();
        }));
        Ui.add(page, filters);
        JPanel summaries = new JPanel(new GridLayout(1, 4, 12, 0));
        summaries.setOpaque(false);
        for (String label : METRICS) {
            JPanel card = Ui.card(new BorderLayout(0, 6));
            card.add(Ui.muted(label), BorderLayout.NORTH);
            card.add(Ui.label(Integer.toString(count(label, Range.year(overviewYear))), 22, true));
            summaries.add(card);
        }
        Ui.add(page, summaries);
        JPanel chart = Ui.card(new BorderLayout(0, 12));
        JPanel header = Ui.row();
        header.add(Ui.label("Monthly overview", 16, true));
        JComboBox<String> picker = Ui.choice(METRICS);
        picker.setSelectedItem(metric);
        picker.getAccessibleContext().setAccessibleName("Select graph");
        header.add(Ui.muted("Graph"));
        header.add(picker);
        chart.add(header, BorderLayout.NORTH);
        BarChart graph = new BarChart(MONTHS, monthly(metric, overviewYear));
        chart.add(graph);
        JLabel note = Ui.muted(chartNote(metric));
        chart.add(note, BorderLayout.SOUTH);
        picker.addActionListener(e -> {
            metric = (String) picker.getSelectedItem();
            graph.setSeries(MONTHS, monthly(metric, overviewYear));
            note.setText(chartNote(metric));
        });
        Ui.add(page, chart);
    }

    private String chartNote(String selected) {
        String definition = selected.equals("Active users") ? "Distinct accounts that opened the app. " : "";
        return definition + (overviewYear == PreviewData.NOW.getYear()
            ? "Sample data through November 11, 2026." : "No sample records for this year.");
    }

    private int[] monthly(String label, int year) {
        int[] counts = new int[12];
        for (Month month : Month.values()) counts[month.getValue() - 1] = count(label, Range.month(year, month));
        return counts;
    }

    private int count(String label, Range range) {
        return switch (label) {
            case "Registrations" -> (int) data.users.stream().filter(u -> range.includes(u.created().toLocalDate())).count();
            case "Sign-ins" -> (int) data.signIns.stream().filter(s -> range.includes(s.at().toLocalDate())).count();
            case "Active users" -> (int) data.appOpens.stream().filter(o -> range.includes(o.date()))
                .map(PreviewData.AppOpen::userId).distinct().count();
            case "Messages sent" -> (int) data.messages.stream().filter(m -> range.includes(m.at().toLocalDate())).count();
            default -> throw new IllegalArgumentException(label);
        };
    }

    private void friendCounts(JPanel page) {
        JPanel filters = Ui.row();
        JTextField search = Ui.field("Find a person", 19);
        filters.add(Ui.search(search));
        filters.add(Ui.label("Direct friends", 13, true));
        JComboBox<String> comparison = Ui.choice(new String[]{"=", ">", "<"});
        comparison.getAccessibleContext().setAccessibleName("Compare direct friends");
        filters.add(comparison);
        JTextField count = Ui.field("Any count", 7);
        filters.add(count);
        Ui.add(page, filters);

        JTable table = Ui.table("Full name", "Username", "Created", "Direct friends", "Total network", "");
        Ui.onRow(table, row -> {
            String username = table.getModel().getValueAt(row, 1).toString();
            data.users.stream().filter(u -> data.handle(u.id()).equals(username)).findFirst().ifPresent(this::friendDetail);
        });
        Runnable update = () -> {
            IntPredicate matches = numberFilter(count, (String) comparison.getSelectedItem());
            Ui.rows(table, data.users.stream()
                .filter(u -> Ui.matches(search.getText(), u.name(), u.username()))
                .filter(u -> matches.test(data.friendships(u.id()).size()))
                .map(u -> {
                    int direct = data.friendships(u.id()).size();
                    return new Object[]{u.name(), data.handle(u.id()), u.created().toLocalDate(), direct,
                        direct + data.mutualFriends(u.id()).size(), "›"};
                }).toList());
        };
        Ui.onText(search, update);
        Ui.onText(count, update);
        comparison.addActionListener(e -> update.run());
        update.run();
        Ui.add(page, Ui.scrollTable(table));
    }

    private void friendDetail(User user) {
        JPanel page = page("Analytics");
        Ui.add(page, Ui.tabs(tab, TABS, selected -> {
            tab = selected;
            refresh();
        }));
        JPanel heading = Ui.row();
        heading.add(Ui.button("Back to Friend Counts", this::refresh));
        heading.add(Ui.label(user.name(), 20, true));
        heading.add(Ui.muted(data.handle(user.id())));
        Ui.add(page, heading);
        String id = user.id();
        List<PreviewData.Friend> friends = data.friendships(id);
        var indirect = data.mutualFriends(id);
        Ui.add(page, Ui.label("Direct friends  " + friends.size() + "     Friends of friends  " + indirect.size()
            + "     Total network  " + (friends.size() + indirect.size()), 14, true));
        section(page, "Friends", new String[]{"Full name", "Username", "Friends since"}, friends.stream()
            .map(f -> new Object[]{data.name(f.other(id)), data.handle(f.other(id)), f.since()}).toList());
        section(page, "Groups", new String[]{"Group name", "Group role", "Joined"}, data.groups.stream()
            .flatMap(g -> g.members().stream().filter(m -> m.userId().equals(id))
                .map(m -> new Object[]{g.name(), m.role(), m.joined()})).toList());
        section(page, "Friends of Friends", new String[]{"Full name", "Username", "Mutual friends"}, indirect.entrySet().stream()
            .map(entry -> new Object[]{data.name(entry.getKey()), data.handle(entry.getKey()),
                String.join(", ", entry.getValue().stream().map(data::name).sorted().toList())}).toList());
        display(page);
    }

    private void section(JPanel page, String title, String[] columns, List<Object[]> rows) {
        JPanel section = Ui.column();
        JPanel heading = Ui.row();
        heading.add(Ui.label(title, 16, true));
        JTable table = Ui.table(columns);
        Ui.rows(table, rows);
        JComponent content = rows.isEmpty() ? Ui.muted("No records") : Ui.scrollTable(table);
        JButton toggle = Ui.button("", () -> {});
        toggle.setIcon(Ui.icon("down", 16));
        toggle.getAccessibleContext().setAccessibleName("Hide " + title);
        toggle.addActionListener(e -> {
            boolean visible = !content.isVisible();
            content.setVisible(visible);
            toggle.setIcon(Ui.icon(visible ? "down" : "chevron", 16));
            toggle.getAccessibleContext().setAccessibleName((visible ? "Hide " : "Show ") + title);
            section.revalidate();
        });
        heading.add(toggle);
        Ui.add(section, heading);
        Ui.add(section, content);
        Ui.add(page, section);
    }

    private void activity(JPanel page) {
        JPanel filters = Ui.row();
        JTextField search = Ui.field("Find a person", 15);
        filters.add(Ui.search(search));
        filters.add(PeriodPicker.range(activityRange, range -> {
            activityRange = range;
            refresh();
        }));
        String[] metrics = {"Sign-ins", "App opens", "People chatted with", "Groups chatted in"};
        JComboBox<String> metricFilter = Ui.choice(metrics);
        metricFilter.getAccessibleContext().setAccessibleName("Activity to compare");
        filters.add(metricFilter);
        JComboBox<String> comparison = Ui.choice(new String[]{"=", ">", "<"});
        comparison.getAccessibleContext().setAccessibleName("Compare activity count");
        filters.add(comparison);
        JTextField count = Ui.field("Any count", 7);
        filters.add(count);
        Ui.add(page, filters);
        JTable table = Ui.table("Full name", "Username", "Created", metrics[0], metrics[1], metrics[2], metrics[3]);
        table.setRowHeight(36);
        table.getColumnModel().getColumn(5).setPreferredWidth(152);
        table.getColumnModel().getColumn(6).setPreferredWidth(148);
        Runnable update = () -> {
            IntPredicate matches = numberFilter(count, (String) comparison.getSelectedItem());
            Ui.rows(table, data.users.stream().filter(u -> Ui.matches(search.getText(), u.name(), u.username()))
                .map(u -> {
                    PreviewData.Activity record = data.activity(u.id(), activityRange);
                    return new Object[]{u.name(), data.handle(u.id()), u.created().toLocalDate(), record.signIns(), record.opens(), record.people(), record.groups()};
                }).filter(row -> (int) row[3] + (int) row[4] + (int) row[5] + (int) row[6] > 0)
                .filter(row -> matches.test((int) row[metricFilter.getSelectedIndex() + 3])).toList());
        };
        Ui.onText(search, update);
        Ui.onText(count, update);
        metricFilter.addActionListener(e -> update.run());
        comparison.addActionListener(e -> update.run());
        update.run();
        Ui.add(page, Ui.scrollTable(table));
        JPanel chart = Ui.card(new BorderLayout(0, 12));
        JPanel header = Ui.row();
        header.add(Ui.label("Monthly sign-ins", 16, true));
        BarChart graph = new BarChart(MONTHS, monthly("Sign-ins", activityYear));
        header.add(PeriodPicker.year(activityYear, year -> {
            activityYear = year;
            graph.setSeries(MONTHS, monthly("Sign-ins", year));
        }));
        chart.add(header, BorderLayout.NORTH);
        chart.add(graph);
        chart.add(Ui.muted("Sign-in events per month."), BorderLayout.SOUTH);
        Ui.add(page, chart);
    }

    private IntPredicate numberFilter(JTextField field, String comparison) {
        String value = field.getText().strip();
        if (value.isEmpty()) {
            field.setToolTipText(null);
            return count -> true;
        }
        try {
            int target = Integer.parseInt(value);
            if (target < 0) throw new NumberFormatException();
            field.setToolTipText(null);
            return switch (comparison) {
                case ">" -> count -> count > target;
                case "<" -> count -> count < target;
                default -> count -> count == target;
            };
        } catch (NumberFormatException e) {
            field.setToolTipText("Enter a whole number of zero or more.");
            return count -> false;
        }
    }
}
