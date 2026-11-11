package chatapp.admin;

import chatapp.admin.ui.AdminFrame;
import chatapp.admin.ui.moderation.Moderation;
import chatapp.admin.ui.shared.BarChart;
import chatapp.admin.ui.shared.PeriodPicker;
import chatapp.admin.ui.preview.PreviewData;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AdminUiTest {
    @Test
    void mainNavigationAndAnalyticsExposeTheRequiredViews() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = AdminFrame.createContent();
            assertColumns(shell, "Full name", "Status", "Created");
            button(shell, "Account History").doClick();
            assertColumns(shell, "Timestamp", "Username", "Full name");
            button(shell, "New Accounts").doClick();
            assertColumns(shell, "Created", "Username", "Full name");
            button(shell, "Chat Groups").doClick();
            assertColumns(shell, "Group name", "Members", "Group admins");
            button(shell, "Spam Reports").doClick();
            assertColumns(shell, "Reported username", "Account status");
            button(shell, "Analytics").doClick();
            assertNotNull(button(shell, "Friend Counts"));
            button(shell, "Friend Counts").doClick();
            assertColumns(shell, "Direct friends", "Total network");
            button(shell, "Activity").doClick();
            assertColumns(shell, "Sign-ins", "App opens", "People chatted with", "Groups chatted in");
        });
    }

    @Test
    void friendCountDetailShowsAllRelationshipColumns() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = AdminFrame.createContent();
            button(shell, "Analytics").doClick(0);
            button(shell, "Friend Counts").doClick(0);
            JTable table = components(shell).stream().filter(JTable.class::isInstance).map(JTable.class::cast)
                .findFirst().orElseThrow();
            table.setRowSelectionInterval(0, 0);
            table.getActionMap().get("openRow").actionPerformed(null);
            assertColumns(shell, "Full name", "Username", "Friends since");
            assertColumns(shell, "Group name", "Group role", "Joined");
            assertColumns(shell, "Full name", "Username", "Mutual friends");
        });
    }

    @Test
    void lockAndUnlockShareOneAccountStateAndHistory() {
        PreviewData data = new PreviewData();
        String userId = data.users.stream().filter(user -> !user.locked()).findFirst().orElseThrow().id();
        int historySize = data.locks.size();
        data.lock(userId, true);
        data.lock(userId, true);
        assertEquals("Locked", data.status(userId));
        assertEquals(historySize + 1, data.locks.size());
        data.lock(userId, false);
        assertEquals("Active", data.status(userId));
        assertEquals(historySize + 2, data.locks.size());
    }


    @Test
    void selectedYearFollowsAdminNavigationAndChartsKeepTwelveMonths() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = AdminFrame.createContent();
            button(shell, "Account History").doClick(0);
            selectYear(shell, 2026, 2025);
            assertNotNull(button(shell, "Period: 2025"));
            assertEquals(0, table(shell).getRowCount());
            assertTwelveMonths(shell);

            button(shell, "New Accounts").doClick(0);
            assertNotNull(button(shell, "Period: 2025"));
            assertEquals(0, table(shell).getRowCount());
            assertTwelveMonths(shell);
            button(shell, "Spam Reports").doClick(0);
            assertNotNull(button(shell, "Period: 2025"));
            assertEquals(0, table(shell).getRowCount());
            button(shell, "Analytics").doClick(0);
            assertNotNull(button(shell, "Period: 2025"));
            assertTwelveMonths(shell);
            button(shell, "Activity").doClick(0);
            assertNotNull(button(shell, "Period: 2025"));
            assertEquals(0, table(shell).getRowCount());
            assertTwelveMonths(shell);

            button(shell, "Chat Groups").doClick(0);
            openFirstRow(shell);
            button(shell, "Monitoring").doClick(0);
            assertNotNull(button(shell, "Period: 2025"));
            assertTwelveMonths(shell);
            selectYear(shell, 2025, 2026);
            var graphPicker = components(shell).stream().filter(JComboBox.class::isInstance).map(JComboBox.class::cast)
                .findFirst().orElseThrow();
            graphPicker.setSelectedItem("Members joined");
            assertEquals("[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0]", chart(shell).getToolTipText());
            button(shell, "Users").doClick(0);
            openFirstRow(shell);
            button(shell, "Login History").doClick(0);
            assertNotNull(button(shell, "Period: 2026"));
            assertEquals(3, table(shell).getRowCount());
            assertEquals("[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0]", chart(shell).getToolTipText());
        });
    }


    @Test
    void reopeningYearPickerDiscardsUnappliedText() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = AdminFrame.createContent();
            button(shell, "Account History").doClick(0);
            JPopupMenu popup = button(shell, "Period: 2026").getComponentPopupMenu();
            JSpinner spinner = components(popup).stream().filter(JSpinner.class::isInstance).map(JSpinner.class::cast)
                .findFirst().orElseThrow();
            var field = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            field.setText("2025");
            var opened = new PopupMenuEvent(popup);
            for (var listener : popup.getPopupMenuListeners()) listener.popupMenuWillBecomeVisible(opened);
            assertEquals("2026", field.getText());
        });
    }

    @Test
    void reportedAccountLockCountAndHistoryUseTheSameYear() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PreviewData data = new PreviewData();
            data.locks.add(new PreviewData.LockAction("ethanp", LocalDateTime.of(2025, 11, 1, 12, 0), true));
            var period = new PeriodPicker(2026);
            var screen = new Moderation(data, period, "Spam Reports");
            button(screen, "By account").doClick(0);
            JTable accounts = table(screen);
            assertEquals("@ethanp", accounts.getValueAt(0, 1));
            assertEquals(1L, accounts.getValueAt(0, 4));
            openFirstRow(screen);
            JTable actions = components(screen).stream().filter(JTable.class::isInstance).map(JTable.class::cast)
                .filter(t -> "Action".equals(t.getColumnName(1))).findFirst().orElseThrow();
            assertEquals(2, actions.getRowCount());
            selectYear(screen, 2026, 2025);
            assertEquals(0, table(screen).getRowCount());
            assertEquals(1, components(screen).stream().filter(JTable.class::isInstance).map(JTable.class::cast)
                .filter(t -> "Action".equals(t.getColumnName(1))).findFirst().orElseThrow().getRowCount());
        });
    }

    private static JTable table(Container root) {
        return components(root).stream().filter(JTable.class::isInstance).map(JTable.class::cast)
            .findFirst().orElseThrow();
    }

    private static void openFirstRow(Container root) {
        JTable table = table(root);
        table.setRowSelectionInterval(0, 0);
        table.getActionMap().get("openRow").actionPerformed(null);
    }

    private static void selectYear(Container root, int current, int selected) {
        JPopupMenu picker = button(root, "Period: " + current).getComponentPopupMenu();
        assertNotNull(picker);
        components(picker).stream().filter(JSpinner.class::isInstance).map(JSpinner.class::cast)
            .findFirst().orElseThrow().setValue(selected);
        button(picker, "Apply").doClick(0);
    }

    private static JComponent chart(Container root) {
        return components(root).stream().filter(BarChart.class::isInstance)
            .map(JComponent.class::cast).findFirst().orElseThrow();
    }

    private static void assertTwelveMonths(Container root) {
        assertEquals("Monthly chart, January through December", chart(root).getAccessibleContext().getAccessibleName());
        assertEquals("[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]", chart(root).getToolTipText());
    }

    private static JButton button(Container root, String label) {
        return components(root).stream().filter(JButton.class::isInstance).map(JButton.class::cast)
            .filter(b -> label.equals(b.getText())).findFirst()
            .orElseThrow(() -> new AssertionError("Missing button: " + label));
    }

    private static void assertColumns(Container root, String... columns) {
        assertTrue(components(root).stream().filter(JTable.class::isInstance)
            .map(JTable.class::cast).anyMatch(table -> {
                List<String> headers = new ArrayList<>();
                for (int i = 0; i < table.getColumnCount(); i++) headers.add(table.getColumnName(i));
                return headers.containsAll(List.of(columns));
            }), "Missing columns: " + List.of(columns));
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
